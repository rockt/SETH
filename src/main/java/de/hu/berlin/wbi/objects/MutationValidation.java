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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * This object represents a {@link MutationMention} from the validation corpus, where
 * we already know which {@link dbSNP} entry is associated with this gene.
 * Therefore this class can be used to evaluate precision, recall and F1 for any
 * normalization method.
 * 
 * @author Philippe Thomas
 * 
 */
public class MutationValidation extends MutationMention {

	/** manually annotated {@link dbSNP} entry from gold-standard. */
	private final int dbSNP;

	/** Either NSM or PSM. */
	private final String mutationType;
	
	/** Contains the actual text of the mutation mention (e.g. 271 from Arginine to Cysteine)*/ 
	private final String entity;

	/**
	 * Constructor 
	 * 
	 * @param dbSNP -- Goldstandard dbSNP-IdW
	 * @param location -- Location in the text
	 * @param wtResidue -- Wildtype Allele (e.g. R)
	 * @param mutResidue -- Mutated Allele (e.g. C)
	 * @param position -- Mutation mocation (e.g. 271)
	 */
	public MutationValidation(int dbSNP, EntityOffset location, String wtResidue,
			String mutResidue, String position, String mutationType, String entity) {
		super();
		this.dbSNP = dbSNP;
		this.mutationType = mutationType;

		this.location = location;
		this.wtResidue = wtResidue;
		this.mutResidue = mutResidue;
		this.position = position;
		this.entity = entity;
	}

	/**
	 * Gets the associated dbSNP entry.
	 * 
	 * @return the associated dbSNP entry
	 */
	public int getDbSNP() {
		return dbSNP;
	}

	/**
	 * Gets the either NSM or PSM.
	 * 
	 * @return the either NSM or PSM
	 */
	public String getMutationType() {
		return mutationType;
	}
	
	/**
	 * Gets the actual entity mention
	 * 
	 * @return tagged string
	 */
	public String getEntity() {
		return entity;
	}

	/**
	 * Reads the mutation from the annotated corpus and returns these mutations.
	 * 
	 * @param file    Annotated corpus
	 * @return list of correct mutations
	 * @throws IOException
	 */
	public static  Map<Integer,List<MutationValidation>> readMutationsValidation(String file)
			throws  IOException {
		Map<Integer,List<MutationValidation>> result = new HashMap<Integer, List<MutationValidation>>();
		BufferedReader br;

		if (file.endsWith(".gz"))
			br = new BufferedReader(new InputStreamReader(new GZIPInputStream(
					new FileInputStream(file))));
		else
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					file)));
		String line;
		while ((line = br.readLine()) != null) {

			if (line.startsWith("#")) {
				System.err.println("Skipping line " + line);
				continue;
			}
		
			String[] array = line.split("\t");
			
			if (array.length == 7) {
				
				//Some logic, which modifies the mutation strings of the gold-standard
				if(array[2].startsWith("p.")){
					array[2] = array[2].substring(2);
				}					
				else if(array[2].startsWith("c.") || array[2].startsWith("g.")){
					Pattern p = Pattern.compile("([\\-\\*]?[1-9][0-9]*[+\\-]?[1-9]*[0-9]*)([ATGC])[\\>\\<]([ATGC])");
					Matcher m= p.matcher(array[2]);
					m.find();
					
					String compareString = m.group(1) +m.group(2) +">" +m.group(3);
					if(!compareString.equals(array[2].substring(2)))
						throw new RuntimeException("Error parsing '" +array[2] +"' result '" +compareString +"' is not correct");
					
					array[2]= m.group(2) +m.group(1) + m.group(3);
				}
											
				int pmid = Integer.parseInt(array[0]); 
				MutationMention tmp = new MutationMention(array[2]);
				MutationValidation mv = new MutationValidation(
						Integer.parseInt(array[5].substring(2)),
						 new EntityOffset(
								Integer.parseInt(array[3]),
								Integer.parseInt(array[4])), tmp.wtResidue,
						tmp.mutResidue, tmp.position, array[6],array[1].substring(1,array[1].length()-1));	
				
				if(result.containsKey(pmid)){
					result.get(pmid).add(mv);
				}
				
				else{
					List<MutationValidation> tmpList  = new ArrayList<MutationValidation>();
					tmpList.add(mv);
					result.put(pmid, tmpList);					
				}

			} else {
				throw new RuntimeException("Quit parsing " +file +"\nLine " +line +" has not 7 fields " +array.length);
			}
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
		return "MutationValidation [dbSNP=" + dbSNP + " ,"
				+ wtResidue + position + mutResidue + "]";
	}

}
