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
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import de.hu.berlin.wbi.objects.Gene;
import de.hu.berlin.wbi.objects.MutationMention;
import de.hu.berlin.wbi.objects.DatabaseConnection;
import de.hu.berlin.wbi.objects.UniprotFeature;
import de.hu.berlin.wbi.objects.dbSNP;
import de.hu.berlin.wbi.objects.dbSNPNormalized;

/**
 * This is an example class for using snp-normalizer
 * 
 * @author Philippe Thomas
 * 
 */
public class Normalize {

	public static void main(String[] args) {
		if (args.length != 2) {
			printUsage();
			System.exit(1);
		}

		String propertyFile = args[0];
		String mutationsFile = args[1];

		System.err.println("Normalising mutations from '" + mutationsFile
				+ "' and properties from '" + propertyFile + "'");

		// Read properties and connect to database
		Properties property = new Properties();
		try {
			property.loadFromXML(new FileInputStream(new File(propertyFile)));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		DatabaseConnection mysql = new DatabaseConnection(property);
		mysql.connect();

		// Read mutation-file
		Map<Integer,List<MutationMention>> mutations = null;
		try {
			mutations = MutationMention.readMutations(mutationsFile);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		System.err.println(mutations.size()
				+ " mutations for normalisation loaded");

		try {
			dbSNP.init(mysql, property.getProperty("database.PSM"), property.getProperty("database.hgvs_view"));
			UniprotFeature.init(mysql, property.getProperty("database.uniprot"));
			Gene.init(mysql, property.getProperty("database.geneTable"), property.getProperty("database.gene2pubmed"));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		// Try to normalize each mutation
		int possible = 0;
        int nMutatons = 0;
		for(int pmid : mutations.keySet()){

			// Get all Genes found for this article
			Set<Gene> genes = Gene.queryGenesForArticle(pmid);
			
			for (MutationMention mutation : mutations.get(pmid)) {
                nMutatons++;
			
				// List of possible normalizations for this SNP
				Set<dbSNPNormalized> normalized = new HashSet<dbSNPNormalized>();
				// Iterate over all different genes and try to normalize the SNP to
				// all dbSNP entries for this gene
				for (Gene gene : genes) {
					List<dbSNP> potentialSNPs = dbSNP.getSNP(gene.getGeneID());
					final List<UniprotFeature> features = UniprotFeature.getFeatures(gene.getGeneID());
                    mutation.normalizeSNP(potentialSNPs, features, false);

                    normalized.addAll(mutation.getNormalized());
				}

				// Print the normalisation-result to stdout
				StringBuilder sb = new StringBuilder(pmid + "\t"
						+ mutation.toNormalized() + "\t"
						+ mutation.getLocation().getStart() + "\t"
						+ mutation.getLocation().getStop());
				for (dbSNPNormalized norm : normalized) {
					// Print results to stdout
					sb.append("\trs");
                    sb.append(norm.getRsID());
                }
				System.out.println(sb);

				if (normalized.size() >= 1)
					possible++;
			}
		}
		
		System.err.println("Normalization possible for " + possible + "/"
				+ nMutatons + " mentions");
		mysql.disconnect();
	}

	private static void printUsage() {

		System.err.println("Normalises previously identified mutations");
		System.err.println("Usage: java de.hu.berlin.wbi.process.Normalize property-file corpus-file\n");
		System.err.println("Arguments:");
		System.err.println("\t property-file\t-> File containing the property settings to access the database");
		System.err.println("\t corpus-file\t-> File containing the mutations to normalize");

	}

}
