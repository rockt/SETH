package cnveth;

import java.util.ArrayList;

class RegionMention {

    /** region mention */
    protected String region;

    /** start position in text */
    protected int begin;

    /** end position in text */
    protected int end;

    /** possible genes from this region with entrez ID*/
    protected ArrayList<String> genes;

    public RegionMention(String region, int begin, int end, ArrayList<String> genes){

        this.region = region;
        this.begin = begin;
        this.end = end;
        this.genes = genes;

    }

    public String getRegion() {
        return region;
    }

    public int getBegin() {
        return begin;
    }

    public int getEnd() {
        return end;
    }

    public ArrayList<String> getGenes() {
        return genes;
    }

    @Override
    public String toString() {
        return "RegionMention [span=" + begin +"-" + end + ", region=" + region + ", genes=" + genes + "]";
    }

}
