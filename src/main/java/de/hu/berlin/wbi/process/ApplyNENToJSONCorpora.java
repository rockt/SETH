package de.hu.berlin.wbi.process;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import de.hu.berlin.wbi.objects.*;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import seth.SETH;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Apply Named Entity Normalization to previously identfied named entities.
 * Assumes perfect gene named entity recogntion.
 * @author Philippe Thomas
 */
public class ApplyNENToJSONCorpora {
	private final static Logger logger = LoggerFactory.getLogger(ApplyNENToJSONCorpora.class);

	private final static SETH sethNer = new SETH();


	public static void main(String[] args) throws IOException, SQLException {

		if(args.length != 3){
			printUsage();
			System.exit(1);
		}
		
		String propertyFile = args[0];
		String goldstandardFile=args[1];
		String predictionFile=args[2];

		Properties property = new Properties();
		try {
			property.loadFromXML(new FileInputStream(new File(propertyFile)));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		DatabaseConnection mysql = new DatabaseConnection(property);
		mysql.connect();

		try {
			dbSNP.init(mysql, property.getProperty("database.PSM"), property.getProperty("database.hgvs_view"));
			Gene.init(mysql, property.getProperty("database.geneTable"), property.getProperty("database.gene2pubmed"));
			UniprotFeature.init(mysql, property.getProperty("database.uniprot"));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		Set<Integer> dbSNPWithoutGene = new HashSet<>();
		JSONParser parser = new JSONParser();
		//Parse goldstandard
		try{
			JSONArray jsonDocuments = new JSONArray(); //Store all results here

			Object obj = parser.parse(new FileReader(goldstandardFile));
			JSONObject jsonObject =  (JSONObject) obj;
			JSONArray documents = (JSONArray) jsonObject.get("documents");
			Iterator<JSONObject> documentInterator = documents.iterator();
			while(documentInterator.hasNext()){ // Iterate documents
				JSONObject jsonDocument = new JSONObject();

				JSONObject document = documentInterator.next();

				JSONArray entities = (JSONArray) ((JSONObject) document.get("document")).get("entities");
				Integer documentID = Integer.parseInt((String) ((JSONObject) document.get("document")).get("ID"));


				Iterator<JSONObject> entityInterator = entities.iterator();
				JSONArray jsonEntities = new JSONArray();
				//Iterate entities in doc
				while(entityInterator.hasNext()){
					JSONObject entity = entityInterator.next();

					//{"dbSNP":"17235409","end":758,"ID":"T0","text":"D543N","type":"ProteinMutation","begin":753}

					String entityID=(String) entity.get("ID");
					String entityString = (String) entity.get("text");
					if(entity.containsKey("dbSNP")){ //If a mutation is normalizable, we try to normalize it as well

						int rsId = Integer.parseInt((String) entity.get("dbSNP"));
						//Retrieves gene associated with a dbSNP entry
						//This allows us to 1.) ignore the impact of gene-NER and 2.) indirectly checks if we know the dbSNP ID at all

						mysql.query("SELECT DISTINCT locus_id FROM PSM WHERE snp_id = " +rsId  +" UNION SELECT  locus_id FROM hgvs WHERE snp_id = " +rsId);
						ResultSet rs = mysql.getRs();
						Set<Integer> genes = new HashSet<Integer>();
						while(rs.next()){
							int geneId = rs.getInt("locus_id");
							genes.add(geneId);
						}
						rs.close();

						if(genes.size() ==0 ){
							//System.out.println("!No associated gene found for rs" +rsId);
							dbSNPWithoutGene.add(rsId);
						}


						List<MutationMention> mentions = sethNer.findMutations(entityString);
						Set<Integer> dbSNPIds = new HashSet<>();
						for(Integer gene : genes){
							final List<dbSNP> potentialSNPs = dbSNP.getSNP(gene);    //Get a list of dbSNPs which could potentially represent the mutation mention
							final List<UniprotFeature> features = UniprotFeature.getFeatures(gene);    //Get all associated UniProt features

							for(MutationMention mention : mentions){
								mention.normalizeSNP(potentialSNPs, features, false);
								List<dbSNPNormalized> normalized = mention.getNormalized();    //Get list of all dbSNP entries with which I could successfully associate the mutation

								// Print information
								for (dbSNPNormalized snp : normalized) {
									dbSNPIds.add(snp.getRsID());
								}
							}
						}

						//Save all dbSNPIds
						JSONArray jsonDbSNPPredictions = new JSONArray();
						for (Integer dbSNPId : dbSNPIds){
							jsonDbSNPPredictions.add(dbSNPId);
						}

						//Save the entitiy
						JSONObject jsonEntity = new JSONObject();
						jsonEntity.put("ID", entityID);
						jsonEntity.put("text", entityString);
						jsonEntity.put("rs", jsonDbSNPPredictions);


						jsonEntities.add(jsonEntity);
					}
				}
			jsonDocument.put("ID", documentID);
			jsonDocument.put("entities", jsonEntities);
			jsonDocuments.add(jsonDocument);
			}
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(predictionFile)));
			bw.append(jsonDocuments.toJSONString());
			bw.close();
			System.out.println("Cannot retrieve genes for the following dbSNP entries: " +dbSNPWithoutGene);
		}
		catch(Exception ex){
			ex.printStackTrace();
		}

	}

	private static void printUsage() {
		
		System.err.println("Applies normalisation to a goldstandard corpus and writes result to prediction file");
		System.err.println("Usage: java de.hu.berlin.wbi.process.ApplyNENToJSONCorpora property-file goldstandard prediction\n");
		System.err.println("Arguments:");
		System.err.println("\t property-file\t-> File containing the property settings to access the database");
		System.err.println("\t goldstandard\t-> File containing the annotated corpus to evaluate the normalisation process");
		System.err.println("\t prediction\t-> File containing the predictions");

	}
}
