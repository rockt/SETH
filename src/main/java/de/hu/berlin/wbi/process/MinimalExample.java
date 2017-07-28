package de.hu.berlin.wbi.process;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Properties;

import de.hu.berlin.wbi.objects.DatabaseConnection;
import de.hu.berlin.wbi.objects.MutationMention;
import de.hu.berlin.wbi.objects.UniprotFeature;
import de.hu.berlin.wbi.objects.dbSNP;
import de.hu.berlin.wbi.objects.dbSNPNormalized;

/**
 * Class contains source code for a minimal example, required to normalize a SNP
 * @author philippe
 *
 */
public class MinimalExample {

	/**
     * Minimal example for SNP normalization
	 * @throws SQLException thrown on problems accessing the database
	 * @throws IOException thrown on problems accessing property file
	 */
	public static void main(String[] args) throws SQLException, IOException {
		final Properties property = new Properties();
		property.loadFromXML(new FileInputStream(new File("myProperty.xml"))); //The connection to the database is stored in a property file
		
		
		final DatabaseConnection mysql = new DatabaseConnection(property);	
		mysql.connect(); //Connect with local mySQL Database
		dbSNP.init(mysql, property.getProperty("database.PSM"), property.getProperty("database.hgvs_view"));
		UniprotFeature.init(mysql, property.getProperty("database.uniprot"));
		
		MutationMention mutation = new MutationMention("V158M"); //This is the SNP we want to normalize
		int gene = 1312;	//This is the gene you believe the SNP is located on. 
				
    	final List<dbSNP> potentialSNPs = dbSNP.getSNP(gene);	//Get a list of dbSNPs which could potentially represent the SNP from (mutation)
    	final List<UniprotFeature> features = UniprotFeature.getFeatures(gene);
        mutation.normalizeSNP(potentialSNPs, features, false);
    	List<dbSNPNormalized> normalized = mutation.getNormalized();	//And here we have  a list of all dbSNPs with which I could successfully associate the mutation
    	
    	for(dbSNPNormalized snp : normalized){
    		System.out.println(mutation +" --- rs=" +snp.getRsID());
    	}
	}

}
