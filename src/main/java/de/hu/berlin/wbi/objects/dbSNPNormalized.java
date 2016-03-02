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

import java.util.EnumSet;

/**
 * Represents a {@link MutationMention} mention which has been successfully
 * normalized to a {@link dbSNP} . Therefore it extends {@link dbSNP}.
 * 
 * @author Philippe Thomas
 * 
 */
public class dbSNPNormalized extends dbSNP implements Comparable<dbSNPNormalized>{

    /**
	 * {@link MutationMention} mention is derived using a specific uniprotfeature
	 */
	private UniprotFeature feature = null;
		
    private EnumSet<MatchOptions> matchType;

    public EnumSet<MatchOptions> getMatchType() { return matchType; }

	/**
	 * @param dbsnp          dbSNP Object
	 * @param feature        UniProt feature used for normalization
     * @param matchType      EnumSet of MatchOptions enum values
	 */
	public dbSNPNormalized(dbSNP dbsnp, EnumSet<MatchOptions> matchType, UniprotFeature feature)
    {
		super();
		this.rsID = dbsnp.getRsID();
		this.geneID = dbsnp.getGeneID();
		this.residues = dbsnp.getResidues();
		this.wildtype = dbsnp.getWildtype();
		this.aaPosition = dbsnp.getAaPosition();
		this.hgvs = dbsnp.getHgvs();
        this.matchType = matchType;
        this.feature = feature;

	}

    /**
     * @return confidence for Normalization
     */
    public int getConfidence() {
        int conf = 0;

        conf += isPsm() ? 2 : 0; //Rank PSMs higher than NSM

        conf += isAlleleOrder() ? 1 : 0; //Allele order has not been changed (e.g., A->T stays A->T)

        conf += isExactPosition() ? 3 : 0;  //Exact position match

        conf += isMethioneMatch() ? 1:0; //Methionine match is still better than an arbitrary feature

        return conf;
    }

	/**
	 * (non-Javadoc)
	 * 
	 * @see de.hu.berlin.wbi.objects.dbSNP#toString()
	 */
	@Override
	public String toString() {
		return "dbSNPNormalized ["
				+ "rsID=" + rsID 
				+ " ,GeneID " + geneID 
				+ ", residues=" + residues
				+ ", wildtype=" +wildtype
				+ " ,aaPosition=" + aaPosition
				+ " ,matchType=" +matchType
				+ " ,feature=" +feature
				+ "]";

	}
	
	/**
	 * Returns true if Alleles are in the same order in dbSNP as in the text mention
	 */
	public boolean isAlleleOrder() {
		return (!matchType.contains(MatchOptions.SWAPPED));
	}

	/**
	 * @return true if the location of the mutation is exact
	 */
	public boolean isExactPosition() {
		return (matchType.contains(MatchOptions.LOC));
	}

	/**
	 * @return true if position has an offset of +/-1 but otherwise there's a match
	 */
	public boolean isMethioneMatch() {
		return (matchType.contains(MatchOptions.METHIONE));
	}
	
	public boolean isFeatureMatch(){
		return feature != null;
	}

    public UniprotFeature getFeature() {
        return feature;
    }

    /**
	 * @return true if snp is a protein sequence mutation
	 */
	public boolean isPsm() {
		return matchType.contains(MatchOptions.PSM);
	}
	
	@Override
	public int compareTo(dbSNPNormalized that) {

        if(that.getConfidence() != this.getConfidence())
		    return that.getConfidence() - this.getConfidence();

	//Enforces an implicit ranking 
	//It is important to know that older dbSNP entries have a smaller ID
        return this.getRsID() - that.getRsID();   
    }
}
