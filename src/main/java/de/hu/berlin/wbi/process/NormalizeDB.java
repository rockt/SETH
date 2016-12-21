package de.hu.berlin.wbi.process;

import java.io.File;
import java.io.FileInputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import de.hu.berlin.wbi.objects.EntityOffset;
import de.hu.berlin.wbi.objects.Gene;
import de.hu.berlin.wbi.objects.MutationMention;
import de.hu.berlin.wbi.objects.DatabaseConnection;
import de.hu.berlin.wbi.objects.UniprotFeature;
import de.hu.berlin.wbi.objects.dbSNP;
import de.hu.berlin.wbi.objects.dbSNPNormalized;

/**
 * Class is used to normalize all mutation mentions stored in a database to SNPs 
 * 
 * @author Philippe Thomas 
 */
public class NormalizeDB {

	/**
	 * @param args Path to property XML
	 * @throws SQLException SQL-Exception accessing the database
	 */
	public static void main(String[] args) throws SQLException {

		final String propertyFile = args[0];
		// Read properties and connect to database
		final Properties property = new Properties();
		try {
			property.loadFromXML(new FileInputStream(new File(propertyFile)));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		final DatabaseConnection mysql = new DatabaseConnection(property);
		mysql.connect();

		final Map<Integer,List<MutationMention>> mutations = new HashMap<Integer, List<MutationMention>>();

		final String snpTable = property.getProperty("database.SNPTable");
		try {
			mysql.query("SELECT * FROM " + snpTable + " WHERE begin != -1");
			ResultSet rs = mysql.getRs();
			while (rs.next()) {
				int pmid = rs.getInt("pmid");
				MutationMention mutation = new MutationMention(new EntityOffset(rs.getInt("begin"), rs.getInt("end")), rs.getString("normalized"));

				//if (mutation != null){ //Condition always true

					if(mutations.containsKey(pmid)){
						mutations.get(pmid).add(mutation);
					}
					else{
						List<MutationMention> mutList = new ArrayList<MutationMention>();
						mutList.add(mutation);
						mutations.put(pmid, mutList);
					}
				//}
			}
			mysql.closeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}

		int nMutations= 0;
		for(List<MutationMention> mmList : mutations.values())
			nMutations+= mmList.size();

		System.err.println(mutations.size()+ " articles with " +nMutations +" entries for normalisation loaded");

		dbSNP.init(mysql, property.getProperty("database.PSM"), property.getProperty("database.hgvs_view"));
		Gene.init(mysql, property.getProperty("database.geneTable"), property.getProperty("database.gene2pubmed"));
		UniprotFeature.init(mysql, property.getProperty("database.uniprot"));

		PreparedStatement ps = mysql
		.getConn()
		.prepareStatement(
				"UPDATE "
				+ snpTable
				+ " SET rs = ? WHERE pmid = ?  AND begin = ? AND end = ? ;");

		// Try to normalize each mutation
		final long start = System.currentTimeMillis();
		int possible = 0; int ambig = 0;  int ambigLevel = 0;

		int i =0;
		for(int pmid : mutations.keySet()){
			// Get all Genes found for this article
			final Set<Gene> genes = Gene.queryGenesForArticle(pmid);

			//Pre-fetch all possible SNPs and all UniProt-features for this gene 		
			//This should be much faster than before, as we produce only one query instead of many (number of mutations) 		
			final Map<Gene, List<dbSNP>> geneToDbSNP = new HashMap<Gene, List<dbSNP>>(); 		
			final Map<Gene, List<UniprotFeature>> geneToUniProtFeature = new HashMap<Gene, List<UniprotFeature>>(); 		
			for (Gene gene : genes) { 		
				final List<dbSNP> potentialSNPs = dbSNP.getSNP(gene.getGeneID()); 		
				final List<UniprotFeature> features = UniprotFeature.getFeatures(gene.getGeneID()); 		

				geneToDbSNP.put(gene, potentialSNPs); 		
				geneToUniProtFeature.put(gene, features); 		
			}

			for (MutationMention mutation : mutations.get(pmid)) {
				i++;
				final List<dbSNPNormalized> normalized = new ArrayList<dbSNPNormalized>();

				// Iterate over all different genes and try to normalize the SNP to
				// all dbSNP entries for this gene
				for (Gene gene : genes) {
					final List<dbSNP> potentialSNPs = geneToDbSNP.get(gene);
					final List<UniprotFeature> features = geneToUniProtFeature.get(gene);
                    mutation.normalizeSNP(potentialSNPs, features, false);

					normalized.addAll(mutation.getNormalized());
				}

				// It is interesting that some rsID's can map to different genes;
				// therefore we have to prevent this problem (e.g rs1050501 in
				// article 20508037/SNP begin=1285)
				final Set<Integer> rsIDs = new HashSet<Integer>();
				boolean nsm=false; boolean psm= false;
				for (dbSNPNormalized norm : normalized) {
					if(norm.isPsm())
						psm= true;
					else
						nsm= true;
					rsIDs.add(norm.getRsID());
				}
				if(nsm && psm)
					ambigLevel++;

				if (normalized.size() > 0) {
					possible++;	//SNP can be normalized to at least one dbSNP id

					StringBuilder sb = new StringBuilder(rsIDs.size() * 10);
					for (Integer rsID : rsIDs) {
						if (sb.length() > 0)
							sb.append("|");
						sb.append(rsID);
					}

					ps.setString(1, sb.toString());
					ps.setInt(2, pmid);
					ps.setInt(3, mutation.getLocation().getStart());
					ps.setInt(4, mutation.getLocation().getStop());
					ps.executeUpdate();
				}

				if(normalized.size() > 1)
					ambig++;

				if (i % 1000 == 0) {
					System.gc();
					System.out.println(i + "/" + nMutations + " " 
							+ "( normalization: "+ possible + "=" + (100*possible/(i == 0 ? 1 : i)) +"%" 
							+ "; memory: "+ (Runtime.getRuntime().totalMemory()/1024/1024) + " MB"
							+ "; speed: "+ 1000*i / (System.currentTimeMillis() - start) +" mention/sec" 
							+ "; time left: " + formatAsHHMMSS((nMutations-i) * (System.currentTimeMillis() - start)/1000 /(i == 0 ? 1 : i) ) 
							+ " )" );
				}

			}
		}


		System.err.println("Normalization possible for " + possible + "/" + nMutations + " mentions");
		System.err.println("Normalization ambigous for " +ambig +"/" +possible);
		System.err.println("Normalization PSM/NSM ambig for " +ambigLevel +"/" +ambig);
		mysql.disconnect();
	}

	/**
	 * Format duration in seconds to HH:MM:SS. 
	 * @param secs number of seconds
	 * @return Human readable time format
	 */
	static private String formatAsHHMMSS(long secs)
	{
		final long hours = secs / 3600,
		remainder = secs % 3600,
		minutes = remainder / 60,
		seconds = remainder % 60;

		return ( (hours < 10 ? "0" : "") + hours
				+ ":" + (minutes < 10 ? "0" : "") + minutes
				+ ":" + (seconds< 10 ? "0" : "") + seconds );
	}    

}
