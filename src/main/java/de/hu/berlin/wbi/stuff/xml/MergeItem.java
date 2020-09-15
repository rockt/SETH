package de.hu.berlin.wbi.stuff.xml;

public class MergeItem {

    private int rsId;
    private int buildId;

    public MergeItem(int rsId, int buildId) {
        this.rsId = rsId;
        this.buildId = buildId;
    }

    public int getRsId() {
        return rsId;
    }

    public void setRsId(int rsId) {
        this.rsId = rsId;
    }

    public int getBuildId() {
        return buildId;
    }

    public void setBuildId(int buildId) {
        this.buildId = buildId;
    }
}


