package de.hu.berlin.wbi.objects;

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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a gene mention found by any Named Enttiy Recognition and Normalization tool. 
 * In this work we always used GNAT and gene2pubmed
 * 
 * @author Philippe Thomas
 */
public class Gene {

	/** Retrieves GNAT results for a PubMed ID  */
	private static PreparedStatement geneQuery;
	
	/** Retrieves gene2pubmed entries for a PubMed ID*/
	private static PreparedStatement gene2pubmedQuery;

	/** Article ID. */
    private int pmid;

	/** Gene-ID (Entrez in our case). */
    private int geneID;

	/** Confidence for the found gene. (Only for NER) */
    private int confidence;

	/** Associated species. */
    private int species;

	/** Found at which position. (Only for NER) */
    private EntityOffset location;

	/** String in the text. (Only for NER) */
    private String entity;

//    private static String beginColumnName = null;

//    private static String endColumnName = null;

	/**
     * Empty constructor
     */
	public Gene() {
	}

	/**
	 * @param pmid         PubMed-ID
	 * @param geneID       Entrez Gene ID
	 * @param confidence   NER/NEN confidence
	 * @param species      Associated species
	 * @param location     Position in text
	 * @param entity       Entity mention
	 */
	public Gene(int pmid, int geneID, int species) {
		super();
		this.pmid = pmid;
		this.geneID = geneID;
		this.species = species;
	}

	/**
	 * Gets the article ID.
	 * 
	 * @return the article ID
	 */
	public int getPmid() {
		return pmid;
	}

	/**
	 * Gets the gene-ID (Entrez in our case).
	 * 
	 * @return the gene-ID (Entrez in our case)
	 */
	public int getGeneID() {
		return geneID;
	}

	/**
	 * Gets the confidence for this gene.
	 * 
	 * @return the confidence for this gene
	 */
	public int getConfidence() {
		return confidence;
	}


	/**
	 * Gets the species.
	 * 
	 * @return the species
	 */
	public int getSpecies() {
		return species;
	}

	/**
	 * Gets the found at which position.
	 * 
	 * @return the found at which position
	 */
	public EntityOffset getLocation() {
		return location;
	}

	/**
	 * Gets the string in the text.
	 * 
	 * @return the string in the text
	 */
	public String getEntity() {
		return entity;
	}


	/**
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Gene [pmid=" + pmid + ", geneID=" + geneID + ", location="
				+ location.getStart() + "-" + location.getStop() + ", species="
				+ species + ", entity=" + entity + "]";
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + confidence;
		result = prime * result + ((entity == null) ? 0 : entity.hashCode());
		result = prime * result + geneID;
		result = prime * result
				+ ((location == null) ? 0 : location.hashCode());
		result = prime * result + pmid;
		result = prime * result + species;
		return result;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Gene other = (Gene) obj;
		if (confidence != other.confidence)
			return false;
		if (entity == null) {
			if (other.entity != null)
				return false;
		} else if (!entity.equals(other.entity))
			return false;
		if (geneID != other.geneID)
			return false;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		if (pmid != other.pmid)
			return false;
		if (species != other.species)
			return false;
		return true;
	}

	/**
	 * Initializes the prepared statements  for retrieving
     * gene information for one PubMed article
	 * 
	 * @param connection   Database connection
	 * @param geneTable   Table name for GNAT results
     * @param gene2Pubmed Table name for gene2PubMed results
	 * @throws SQLException
	 */
	public static void init(DatabaseConnection connection, String geneTable, String gene2Pubmed)
			throws SQLException {
		if (geneTable != null && !geneTable.equals(""))                                                                                      {
			Gene.geneQuery = connection.getConn().prepareStatement("SELECT * " + "FROM " + geneTable + " WHERE pmid = ? AND species = 9606");
//            checkDatabase();
        }
		else
			Gene.geneQuery =  null;			
		
		if(gene2Pubmed != null && !gene2Pubmed.equals(""))
			Gene.gene2pubmedQuery = connection.getConn().prepareStatement("SELECT geneId, pmid FROM " +gene2Pubmed +" WHERE pmid = ? AND taxId = 9606");
		else
			Gene.gene2pubmedQuery = null;
		
		if(Gene.geneQuery == null && Gene.gene2pubmedQuery == null)
			throw new IllegalStateException("At least one gene Source has to be specified");
	}

	/**
	 * Search for all genes contained in an Article using PubMed ID.
	 * 
	 * @param pmid PubMed-ID
	 * @return all genes contained in a specific article
	 */
	public static Set<Gene> queryGenesForArticle(int pmid) {

		final Set<Integer> entrezs = new HashSet<Integer>();		// Set contains a list of previously seen entrez-ID's 
		final Set<Gene> genes = new HashSet<Gene>();
				
		try {

			//If NER results are available we extract genes from this table
			if(geneQuery != null){
				geneQuery.setInt(1, pmid);
				geneQuery.execute();
				final ResultSet rs = geneQuery.getResultSet();

				while (rs.next()) {
					final Gene tmp = new Gene(
							pmid, rs.getInt("id"),
//							rs.getInt("confidence"), 
							rs.getInt("species")
//							new EntityOffset(rs.getInt(beginColumnName), rs.getInt(endColumnName)),
//							rs.getString("entity")
							);

					if (entrezs.add(tmp.getGeneID())) {
						genes.add(tmp);
					}
				}
				rs.close();
			}
						
			
			//If gene2pubmed is available we extract genes mentioned by this table
			if(gene2pubmedQuery != null){
				gene2pubmedQuery.setInt(1, pmid);
				gene2pubmedQuery.execute();
				final ResultSet rs2 = gene2pubmedQuery.getResultSet();
				while (rs2.next()) {
					final Gene tmp = new Gene(pmid, rs2.getInt("geneId"),
//							1,
							9606
//							null,
//							null
							);

					if (entrezs.add(tmp.getGeneID())) {
						genes.add(tmp);
					}
				}
				rs2.close();	
			}
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		return genes;
	}


    /**
     * This method is used to determine if the Gene table contains the column beginGene (if not, we use begin)
     * Invoked only once
     * @throws SQLException
     */
//    private static void checkDatabase() throws SQLException {
//
//        geneQuery.setInt(1, 25000000);
//        geneQuery.execute();
//        final ResultSet rs = geneQuery.getResultSet();
//
//        ResultSetMetaData meta = rs.getMetaData();
//        int numCol = meta.getColumnCount();
//        for (int i = 1; i < numCol+1; i++){
//            if(meta.getColumnName(i).equals("beginGene"))
//            {   rs.close();
//                beginColumnName = "beginGene";
//                endColumnName = "endGene";
//                return;
//            }
//        }
//        beginColumnName = "begin";
//        endColumnName = "end";
//        rs.close();;
//    }
}
