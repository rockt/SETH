package de.hu.berlin.wbi.objects;


import java.util.EnumSet;

/**
 * Represents a {@link MutationMention} mention which has been successfully
 * normalized to a {@link Transcript} .
 *
 * @author Philippe Thomas
 *
 */
public class TranscriptNormalized extends Transcript implements Comparable<TranscriptNormalized>{

    private UniprotFeature feature = null;

    private final EnumSet<MatchOptions> matchType;

    public EnumSet<MatchOptions> getMatchType() { return matchType; }


    /**
     *
     * @param transcript  a transcript
     * @param matchType   EnumSet of MatchOptions enum values
     * @param feature    niProt feature used for normalization
     */
    TranscriptNormalized(Transcript transcript, EnumSet<MatchOptions> matchType,  UniprotFeature feature){
        super();
        this.entrez = transcript.getEntrez();
        this.uniprot = transcript.getUniprot();
        this.ensg = transcript.getEnsg();
        this.enst     = transcript.getEnst();
        this.ensp = transcript.getEnsp();
        this.protein_sequence = transcript.getProtein_sequence();
        this.CDC_sequence   = transcript.getCDC_sequence();

        this.matchType = matchType;
        this.feature = feature;
    }

    public boolean isFeatureMatch(){
        return feature != null;
    }

    public UniprotFeature getFeature() {
        return feature;
    }

    @Override
    public int compareTo(TranscriptNormalized that) {
        return that.getConfidence() - this.getConfidence();
    }


    /**
     * @return confidence for Normalization
     */
    public int getConfidence() {
        int conf = 0;

        conf += isPsm() ? 3 : 2;
        conf += isExactMatch() ? 2 : 0;

        return conf;
    }

    /**
     * @return true if snp is a protein sequence mutation
     */
    public boolean isPsm() {
        return matchType.contains(MatchOptions.PSM);
    }

    /**
     * @return true if the normalization required no heuristics
     */
    public boolean isExactMatch() {
        return (matchType.contains(MatchOptions.LOC)
                && !matchType.contains(MatchOptions.SWAPPED));
    }
}
