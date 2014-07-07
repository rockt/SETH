package de.hu.berlin.wbi.objects;


import java.util.EnumSet;

public class TranscriptNormalized extends Transcript{

    private UniprotFeature feature = null;

    private EnumSet<MatchOptions> matchType;

    public EnumSet<MatchOptions> getMatchType() { return matchType; }


    TranscriptNormalized(Transcript transcript, EnumSet matchType,  UniprotFeature feature){
        super();
        this.entrez = transcript.getEntrez();
        this.uniprot = transcript.getUniprot();
        this.enst     = transcript.getEnst();
        this.protein_sequence = this.getProtein_sequence();
        this.CDC_sequence   = this.getCDC_sequence();

        this.matchType = matchType;
        this.feature = feature;
    }

}
