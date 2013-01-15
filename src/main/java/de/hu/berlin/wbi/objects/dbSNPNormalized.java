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

/**
 * Represents a {@link MutationMention} mention which has been successfully
 * normalized to a {@link dbSNP} . Therefore it extends {@link dbSNP}.
 * 
 * @author Philippe Thomas
 * 
 */
public class dbSNPNormalized extends dbSNP implements Comparable<dbSNPNormalized>{

	
	/** Flag if the mutation has been normalized to protein-sequence, false if nucleotide-sequence*/
	private boolean psm = false;
	
	/**
	 * True if normalization was exact. That is, when residues and position between {@link dbSNP} entry and
	 * variation mention are equal.
	 */
	private boolean exactMatch = false;
	
	/**
	 * Residues are correct but {@link MutationMention} mention is derived one position after
	 * leading methionine.
	 */
	private boolean methioneMatch = false;

	/**
	 * Residues are correct but {@link MutationMention} mention is derived using a specific uniprotfeature
	 */
	private UniprotFeature feature = null;
		
	/** Indicates if wildtype and mutated allele is swapped (e.g. Ala123Tyr -> Tyr123Ala)  **/
	private boolean alleleOrder = false;
	
	/** Indicator for overall confidence**/
	private int confidence = -1;

	/**
	 * @param dbsnp          dbSNP Object
	 * @param exactMatch     normalization was exact?
	 * @param methioneMatch  Indicates normalization offset of 1
	 * @param psm            PSM or NSM
	 * @param feature        UniProt feature used for normalization
	 * @param alleleOrder    Reverse alleleOrder?
	 */
	public dbSNPNormalized(dbSNP dbsnp, boolean exactMatch,
			boolean methioneMatch, boolean psm, UniprotFeature feature, boolean alleleOrder) {
		super();
		this.rsID = dbsnp.getRsID();
		this.geneID = dbsnp.getGeneID();
		this.residues = dbsnp.getResidues();
		this.aaPosition = dbsnp.getAaPosition();
		this.hgvs = dbsnp.getHgvs();

		this.exactMatch = exactMatch;
		this.methioneMatch = methioneMatch;
		this.psm = psm;
		this.feature = feature;
		this.alleleOrder = alleleOrder;
		
		this.confidence = (psm ? 5 : 4) +(exactMatch ? 4 : 0);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see de.hu.berlin.wbi.objects.dbSNP#toString()
	 */
	@Override
	public String toString() {
		return "dbSNPNormalized [rsID=" + rsID + ", Contig="
				+ " ,GeneID " + geneID 
				+ ", residues=" + residues + " ,aaPosition=" + aaPosition + "]";

	}
	
	/**
	 * Returns true if Alleles are in the same order in dbSNP as in the text mention
	 */
	public boolean isAlleleOrder() {
		return alleleOrder;
	}

	/**
	 * @return true if the normalization required no heuristics 
	 */
	public boolean isExactMatch() {
		return exactMatch;
	}

	/**
	 * @return true if position has an offset of +/-1
	 */
	public boolean isMethioneMatch() {
		return methioneMatch;
	}
	
	public boolean isFeatureMatch(){
		return feature != null;
	}

	/**
	 * @return true if snp is a protein sequence mutation
	 */
	public boolean isPsm() {
		return psm;
	}
	
	/**
	 * @return confidence for Normalization
	 */
	public int getConfidence() {
		return confidence;
	}

	@Override
	public int compareTo(dbSNPNormalized that) {

		return that.confidence - this.confidence;
	}
}
