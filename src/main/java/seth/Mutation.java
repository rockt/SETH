package seth;

import seth.ner.wrapper.Type;

import java.util.List;

/**
 * User: Tim Rocktaeschel
 * Date: 11/9/12
 * Time: 2:14 PM
 */

public class Mutation {
    private int start;
    private int end;
    private String text;
    private String ref;
    private String location;
    private String wild;
    private String mutated;
    private Type type;
    private List<String> dbSNP; //TODO

    public Mutation(int start, int end, String text, String ref, String location, String wild, String mutated, Type type) {
        this.start = start;
        this.end = end;
        this.text = text;
        this.ref = ref;
        this.location = location;
        this.wild = wild;
        this.mutated = mutated;
        this.type = type;
    }

    public String toString() {
        return text +
            "\n\tstart: " + start +
            "\n\tend:   " + end +
            "\n\tloc:   " + location +
            "\n\tref:   " + ref +
            "\n\twild:  " + wild +
            "\n\tmut:   " + mutated +
            "\n\ttype:  " + type;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public String getText() {
        return text;
    }

    public String getLocation() {
        return location;
    }

    public String getWild() {
        return wild;
    }

    public String getMutated() {
        return mutated;
    }

    public Type getType() {
        return type;
    }

    public String getRef() {
        return ref;
    }

    public void normalize(List<Integer> entrezIDs) {
        //TODO
    }
}
