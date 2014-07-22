package de.hu.berlin.wbi.objects;


import java.util.EnumSet;

public class TranscriptNormalized extends Transcript implements Comparable<TranscriptNormalized>{

    private UniprotFeature feature = null;

    private EnumSet<MatchOptions> matchType;

    public EnumSet<MatchOptions> getMatchType() { return matchType; }


    TranscriptNormalized(Transcript transcript, EnumSet matchType,  UniprotFeature feature){
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

    public int getConfidence() {
        int conf = 0;

        conf += isPsm() ? 3 : 2;
        conf += isExactMatch() ? 2 : 0;

        return conf;
    }

    public boolean isPsm() {
        return matchType.contains(MatchOptions.PSM);
    }

    public boolean isExactMatch() {
        return (matchType.contains(MatchOptions.LOC)
                && !matchType.contains(MatchOptions.SWAPPED));
    }
}
