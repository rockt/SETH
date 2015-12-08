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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Represents a <a href="http://www.ncbi.nlm.nih.gov/projects/SNP/">dbSNP</a>
 * entry; loaded from a local database.
 * 
 * @author Philippe Thomas
 */
public class dbSNP {

	/** dbSNP internal id. */
	protected int rsID;

	/** gene associated with this dbSNP entry. */
    protected int geneID;

	/** known mutated amino acid residues for this SNP */
    protected Set<String> residues;

	/** Mutation has one wildtype  */
    protected String wildtype;

	/** location of the mutation in terms of amino acids  */
    protected int aaPosition;

	/** Different Strings describing the variations in <a href="http://www.hgvs.org/mutnomen/"HGVS</a> nomenclature */
    protected Set<HGVS> hgvs;



	/** Used to split residues  */
	private static final Pattern pattern = Pattern.compile("\\|");

    /** Database connection */
    private static DatabaseConnection conn = null;

    /** Prepared statement for HGVS entries*/
    private static PreparedStatement hgvsQuery;

    /** Prepared statement for PSM entries*/
	private static PreparedStatement snpQuery;

	/**
	 * Initializes prepared statements for retrieving SNP information
	 * @param mysql     database connection
	 * @param psmTable  PSM Table name
	 * @param hgvs_view HGVS Table name
	 * @throws SQLException
	 */
	public static void init(DatabaseConnection mysql,
			String psmTable, String hgvs_view)
	throws SQLException {
		if (dbSNP.conn != null)
			throw new IllegalStateException("ALREADY INITIALIZED");
		dbSNP.conn = mysql;

		dbSNP.snpQuery = dbSNP.conn
		.getConn()
		.prepareStatement(
				"SELECT snp_id, locus_id, aa_Position, residue, wildtype "
				+ "FROM "  +psmTable + " WHERE locus_id = ? "
		);
		dbSNP.hgvsQuery = dbSNP.conn
		.getConn()
		.prepareStatement(
				"SELECT snp_id, hgvs AS hgvs_name FROM " +hgvs_view +" WHERE locus_id = ?");

	}

	/**
	 * Retrieves a list of dbSNP entries associated with a specific entrez-gene ID
	 * 
	 * @param geneID Entrez-Gene Identifier
	 * @return list of all snps associated with geneID
	 */
	public static List<dbSNP> getSNP(int geneID) {
		if (conn == null)
			throw new IllegalStateException("NOT YET INITIALIZED");
		final List<dbSNP> result = new ArrayList<dbSNP>(1000);

		// Query searches for SNP's which do have an impact on the amino acid level 	
		try {
			snpQuery.setInt(1, geneID);
			if (snpQuery.execute()) {
				final ResultSet rs = snpQuery.getResultSet();
				while (rs.next()) {
					dbSNP dbSNP = new dbSNP(rs.getInt("snp_id"),
							rs.getInt("locus_id"),
							new HashSet<String>(
									Arrays.asList(pattern.split(rs
											.getString("residue")))),
											rs.getInt("aa_Position") + 1,
											rs.getString("wildtype"));

					result.add(dbSNP);
				}
				rs.close();
			} else {} //Empty result (no SNP found for this gene)
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		//Retrieves information about SNP's which have an impact on nucleotide level from another table (hgvs)
		try {
			hgvsQuery.setInt(1, geneID);
			if (hgvsQuery.execute()) {
				final ResultSet resultSet = hgvsQuery.getResultSet();
				final Map<Integer, Set<HGVS>> tmp = new HashMap<Integer, Set<HGVS>>();   //Mapping from dbSNP to different HGVS representations
				while (resultSet.next()) {		//Iterate over all HGVS-Strings for a specific gene
					int rsId = resultSet.getInt("snp_id");

					Set<HGVS> hgvss = new HashSet<HGVS>();
					if (tmp.containsKey(rsId)) {
						hgvss.addAll(tmp.get(rsId));
					}
					hgvss.add(new HGVS(resultSet.getString("hgvs_name")));
					tmp.put(rsId, hgvss);

				}
				resultSet.close();

				//Now associate all (PSM)-SNPs retrieved in Query1 with the associated HGVS information
				for (dbSNP snp : result) {
					snp.setHgvs(tmp.get(snp.getRsID()));	//Set the HGVS information for all dbSNP objects found in query "snpQuery"
				}

				//Okay, the only problem we are left is, that most SNPs are not located on the protein-coding sequence..
				//Thus we search for those HGVS entries without an associated PSM entry and generate a new dbSNP object
				for(Integer rs: tmp.keySet()){ //Iterate over all found rs's
					boolean contained = false;
					loop:for (dbSNP snp : result) {
						if(rs == snp.rsID){
							contained = true;
							break loop;
						}						
					}
					if(!contained){	//If we did not find the corresponding dbSNP entry, we add a new one without PSM mutations
						dbSNP tmpSNP = new dbSNP();
						tmpSNP.setRsID(rs);
                        tmpSNP.setGeneID(geneID);
                        tmpSNP.setHgvs(tmp.get(rs));
						result.add(tmpSNP);
					}
				}

			} else {}//Empty result (no SNP found for this gene)
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		return result;
	}

	/**
	 * Simple constructor 
	 */
	public dbSNP() {
		super();
	}

	/**
     * Constructor
	 * @param rsID        dbSNP identifier
	 * @param geneID      Entrez Gene identifier
	 * @param residues    PSM residues
	 * @param aaPosition  PSM location
	 * @param wildtype    PSM wildtype allele
	 */
	public dbSNP(int rsID, int geneID, Set<String> residues, 
			int aaPosition, String wildtype) {
		super();
		this.rsID = rsID;
		this.geneID = geneID;
		this.residues = residues;		
		this.aaPosition = aaPosition;
		this.wildtype = wildtype;
	}

	/**
	 * Get dbSNP identifier (rs-id)
     * @return the unique dbSNP identifier
	 */
	public int getRsID() {
		return rsID;
	}

	/**
     * Set the dbSNP identifier (e.g., rs334)
	 * @param rsID   dbSNP ID
	 */
	public void setRsID(int rsID) {
		this.rsID = rsID;
	}

	/**
     * Get the associated entrez gene identifier
	 * @return the associated entrez gene identifier
	 */
	public int getGeneID() {
		return geneID;
	}

	/**
     * Set the associated entrez gene identifier
	 * @param geneID    Entrez Gene ID
	 */
	public void setGeneID(int geneID) {
		this.geneID = geneID;
	}

	/**
	 * @return all possible amino-acid mutations
	 */
	public Set<String> getResidues() {
		return residues;
	}

	/**
	 * @return wildtype residue
	 */
	public String getWildtype() {
		return wildtype;
	}

	/**
	 * 
	 * 
	 * @param residues    PSM residues
	 */
	public void setResidues(Set<String> residues) {
		this.residues = residues;
	}

	/**
	 * @return location of the mutation wrt. the amino acid 
	 */
	public int getAaPosition() {
		return aaPosition;
	}

	/**
	 * @param aaPosition   Amino acid location
	 */
	public void setAaPosition(int aaPosition) {
		this.aaPosition = aaPosition;
	}

	/**
	 * @return list of associated HGVS mutations
	 */
	public Set<HGVS> getHgvs() {
		return hgvs;
	}

	/**
	 * @param hgvs     HGVS entries associated with this SNP
	 */
	public void setHgvs(Set<HGVS> hgvs) {
		this.hgvs = hgvs;
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
		result = prime * result + aaPosition;
		result = prime * result + geneID;	
		result = prime * result + ((hgvs == null) ? 0 : hgvs.hashCode());
		result = prime * result
		+ ((residues == null) ? 0 : residues.hashCode());
		result = prime * result + rsID;
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
		dbSNP other = (dbSNP) obj;
		if (aaPosition != other.aaPosition)
			return false;
		if (geneID != other.geneID)
			return false;
		if (hgvs == null) {
			if (other.hgvs != null)
				return false;
		} else if (!hgvs.equals(other.hgvs))
			return false;
		if (residues == null) {
			if (other.residues != null)
				return false;
		} else if (!residues.equals(other.residues))
			return false;
		if (rsID != other.rsID)
			return false;
		return true;
	}


	/**
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "dbSNP [aaPosition=" + aaPosition + ", wildtype=" + wildtype  + ", residues=" + residues 
		+ ", rsID="+ rsID + ", geneID=" + geneID
		+ ", hgvs=" + hgvs + "]";
	}	
}
