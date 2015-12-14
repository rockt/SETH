package de.hu.berlin.wbi.process;
/**
Copyright 2010, 2011 Philippe Thomas
This file is part of snp-normalizer.

snp-normalizer is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
any later version.

snp-normalizer is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with snp-normalizer.  If not, see <http://www.gnu.org/licenses/>.
*/
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import de.hu.berlin.wbi.objects.DatabaseConnection;
import de.hu.berlin.wbi.objects.Gene;
import de.hu.berlin.wbi.objects.MutationValidation;
import de.hu.berlin.wbi.objects.UniprotFeature;
import de.hu.berlin.wbi.objects.dbSNP;
import de.hu.berlin.wbi.objects.dbSNPNormalized;

/**
 * Class is used to evaluate snp-normalizer; 
 * Corpus is not provided due to licence restrictions but a link is provided in the paper..
 * @author Philippe Thomas
 */
public class Evaluate {
	
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
				}
								
				fp+=ids.size();			//All remaining ids are false positives	
				for(int id:ids)
					System.err.println("FP" +"\t" +pmid +"\t" +id +" for " +mutation.getDbSNP() +" " +mutation.toString());
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
