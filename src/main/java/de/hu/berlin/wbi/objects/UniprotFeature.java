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
import java.util.ArrayList;
import java.util.List;


/**
 * Represents a <a href="www.uniprot.org/">UniProt</a>
 * entry; loaded from database.
 * 
 * @author Philippe Thomas
 */
public class UniprotFeature{

	/** Gene identifier (Currently Entrez-Gene) */
	private final int geneId;

	/** Modification type (e.g. signal peptide)*/
	private final String modificationType;

	/** Start location */
	private final int startLoc;

	/** End location */
	private final int endLoc;

	/**  Database connection */
	private static DatabaseConnection conn = null;

	/**  Prepared statement for UniProt query*/
	private static PreparedStatement uniprotQuery;

	public UniprotFeature(int geneId, String modificationType, int startLoc,
			int endLoc) {
		super();
		this.geneId = geneId;
		this.modificationType = modificationType;
		this.startLoc = startLoc;
		this.endLoc = endLoc;
	}

	/**
	 * Initializes prepared statements to retrieve UniProt features,
     * like signaling peptide information
	 * @param mysql    mySQL database
	 * @param uniProt_Table Table name
	 * @throws SQLException
	 */
	public static void init(DatabaseConnection mysql, String uniProt_Table) throws SQLException {
		if (UniprotFeature.conn != null)
			throw new IllegalStateException("ALREADY INITIALIZED");
		UniprotFeature.conn = mysql;

		UniprotFeature.uniprotQuery = UniprotFeature.conn
		.getConn()
		.prepareStatement(
				"SELECT entrez, modification, location"
				+ " FROM  " +uniProt_Table 
				+ " WHERE entrez = ? " 
		);
	}	 

	/**
	 * Returns all UniProt features for one specific Entrez-Gene Identifier
	 * @param geneID -- Entrez Gene ID
	 * @return List of UniProt features
	 */
	public static List<UniprotFeature> getFeatures(int geneID){
		if (conn == null)
			throw new IllegalStateException("NOT YET INITIALIZED");
		final List<UniprotFeature> result = new ArrayList<UniprotFeature>(128);

		try {
			uniprotQuery.setInt(1, geneID);
			if (uniprotQuery.execute()) {
				final ResultSet rs = uniprotQuery.getResultSet();
				while (rs.next()) {

					String tmp [] = rs.getString("location").split("-");

					if(tmp.length > 2)//Ignore complicated UniProt features with large span
						continue;

					String mod =  rs.getString("modification"); //Not part of SQL query, because derby has problems with the original IN query
					if(mod.equals("signal peptide") || mod.equals("splice variant") || mod.equals("transit peptide") || mod.equals("peptide") || mod.equals("propeptide")){

						UniprotFeature feature;
						if(tmp.length == 2)
							feature = new UniprotFeature(rs.getInt("entrez"), rs.getString("modification"), Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1]));
						else						
							feature = new UniprotFeature(rs.getInt("entrez"), rs.getString("modification"), Integer.parseInt(tmp[0]), Integer.parseInt(tmp[0]));					
						result.add(feature);
					}
				}
				rs.close();				
			}

		}catch (SQLException e) {
			throw new RuntimeException(e);
		}

		return result;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see de.hu.berlin.wbi.objects.MutationMention#toString()
	 */
	@Override
	public String toString() {
		return modificationType +" " +startLoc +"-" +endLoc;
	}


	/**
	 * Returns associated Enzrez Gene ID
	 * @return  Entrez Gene ID
	 */
	public int getGeneId() {
		return geneId;
	}

	/**
	 * Returns modification type (e.g. signaling peptide)
	 * @return  modification type
	 */
	public String getModificationType() {
		return modificationType;
	}

	/**
	 * Returns start location of the modification on the peptide
	 * @return  start location
	 */
	public int getStartLoc() {
		return startLoc;
	}

	/**
	 * Returns end location of the modification on the peptide
	 * @return   end location
	 */
	public int getEndLoc() {
		return endLoc;
	}

}
