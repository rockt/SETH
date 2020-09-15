package de.hu.berlin.wbi.process;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.lang.StringBuilder;

import de.hu.berlin.wbi.objects.DatabaseConnection;
import de.hu.berlin.wbi.objects.Gene;
import de.hu.berlin.wbi.objects.MutationValidation;
import de.hu.berlin.wbi.objects.UniprotFeature;
import de.hu.berlin.wbi.objects.dbSNP;
import de.hu.berlin.wbi.objects.dbSNPNormalized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class is used to evaluate snp-normalizer; 
 * Corpus is not provided due to licence restrictions but a link is provided in the paper..
 * @author Philippe Thomas
 */
public class Evaluate {
	private final static Logger logger = LoggerFactory.getLogger(Evaluate.class);


	public static void main(String[] args) throws IOException {
		
		if(args.length != 2){
			printUsage();
			System.exit(1);
		}
		
		String propertyFile = args[0];
		String mutationsFile=args[1]; 
					
		 Map<Integer,List<MutationValidation>>  mutations = null;	//Gold standard-mutations
		
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
			mutations = MutationValidation.readMutationsValidation(mutationsFile); //Read the annotated mutations from a file
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		} 
		
		try {
			dbSNP.init(mysql, property.getProperty("database.PSM"), property.getProperty("database.hgvs_view"));
			Gene.init(mysql, property.getProperty("database.geneTable"), property.getProperty("database.gene2pubmed"));
			UniprotFeature.init(mysql, property.getProperty("database.uniprot"));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}


		//This piece of code checks if we have newer dbSNP/rs-Ids for our normalized mutations
		if (property.containsKey("database.mergeItems")){
			try {
				for(int pmid : mutations.keySet()){
					for(MutationValidation mutation:mutations.get(pmid)) {    //Iterate over mutations loaded from "mutationsFile"


						mysql.query("SELECT * FROM mergeItems WHERE old_snp_id = " +mutation.getDbSNP() +";");
						final ResultSet rs = mysql.getRs();
						while (rs.next()) {

							int newSNPId = rs.getInt("new_snp_id");
							//int oldSNPId = rs.getInt("old_snp_id");
							int versionId = rs.getInt("dbSNP_version");

							mutation.setDbSNP(newSNPId);
						}
					}
				}
			}catch (SQLException e) {
				logger.error("Problem accessing database\nMaybe the table +'" +"' does not exist", e);
				throw new RuntimeException(e);
			}
		}

        System.err.println("Corpus description: " +mutationsFile);
        System.err.println(mutations.keySet().size() +" documents");
        int sum = 0;
        for(int pmid : mutations.keySet()){
        	sum+=mutations.get(pmid).size();
        }
        System.err.println(sum +" entities");

		
		//Perform the actual validation..
		int tp=0; int fp=0; int fn=0;
		
		for(int pmid : mutations.keySet()){
			final Set<Gene> genes = Gene.queryGenesForArticle(pmid);	//Get all genes found for this article
			
			for(MutationValidation mutation:mutations.get(pmid)){ 	//Iterate over mutations loaded from "mutationsFile"
				
				List<dbSNPNormalized> normalized  = new ArrayList<dbSNPNormalized>();		
				for(Gene gene:genes){
					if(gene.getSpecies() != 9606)		//Normalisation currently only for human genes
						continue;
					
					final List<dbSNP> potentialSNPs = dbSNP.getSNP(gene.getGeneID());
					final List<UniprotFeature> features = UniprotFeature.getFeatures(gene.getGeneID());
                    mutation.normalizeSNP(potentialSNPs, features, true); //Normalization happens here
				}
                if(mutation.getNormalized() != null)
                    normalized.addAll(mutation.getBestNormalized());

				Set<Integer> ids = new HashSet<Integer>(); //Contains the rsID's found by the normalisation procedure 
				for(dbSNPNormalized dbSNPNorm:normalized){
					ids.add(dbSNPNorm.getRsID());
				}
	 
				if(ids.contains(mutation.getDbSNP())){		//Check if found rsID's  is correct
					tp++;			
					ids.remove(mutation.getDbSNP());
				}			
				else{										//Otherwise we have a false negative					
					fn++;
//					System.out.println("FN" +"\t" +pmid +"\t" +mutation.toString() +"\t" +mutation.getDbSNP());
				}
								
				fp+=ids.size();			//All remaining ids are false positives	

				if(ids.size() != 0){

				    StringBuilder sb = new StringBuilder("FP" +"\t" +pmid +"\t" +mutation.toString() +"\t" +mutation.getDbSNP() +"\t found: ");
				    for(dbSNPNormalized dbSNPNorm:normalized){
				    	sb.append("'").append(dbSNPNorm.getRsID()).append("'/").append(dbSNPNorm.getConfidence()).append("/").append(dbSNPNorm.getMatchType()).append("/").append(dbSNPNorm.getGeneID()).append(" '");
				    }
				    System.out.println(sb);
				}
			}
		}
		
		
        double recall = (double) tp/(tp+fn);
        double precision = (double) tp/(tp+fp);
        double f1 = 2*(precision*recall)/(precision+recall);

		DecimalFormat df = new DecimalFormat( "0.00" );
		System.err.println("TP " +tp);
		System.err.println("FP " +fp);
		System.err.println("FN " +fn);
        System.err.println("Precision " +df.format(precision));
		System.err.println("Recall " +df.format(recall));
        System.err.println("F1 " +df.format(f1));

	}

	private static void printUsage() {
		
		System.err.println("Evaluates normalisation results using the manually annotated corpus");
		System.err.println("Usage: java de.hu.berlin.wbi.process.Evaluate property-file corpus-file\n");
		System.err.println("Arguments:");
		System.err.println("\t property-file\t-> File containing the property settings to access the database");
		System.err.println("\t corpus-file\t-> File containing the annotated corpus to evaluate the normalisation process");
		
	}
}
