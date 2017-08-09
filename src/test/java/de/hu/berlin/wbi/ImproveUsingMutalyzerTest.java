package de.hu.berlin.wbi;

import de.hu.berlin.wbi.objects.MutationMention;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import seth.ner.wrapper.SETHNER;

import java.util.List;

public class ImproveUsingMutalyzerTest {

    private SETHNER sethnerInexact;
    private SETHNER sethnerExact;

    @Before
    public void setUp() {
        sethnerInexact = new SETHNER(false);
        sethnerExact = new SETHNER(true);
    }



    @Test
    @Ignore("Test is ignored due to missing implementation!")
    public void testFN1() throws Exception {

        //Using →
        assertMatchesInexact("c.1090C>T");
        assertMatchesInexact("c.1090C→T");
        assertMatchesInexact("c.239A→G");
        assertMatchesInexact("c.2113+1G→C");
        assertMatchesInexact("c.847C→T");
        assertMatchesInexact("m.8993T→C");
        assertMatchesInexact("p.Ile1019→Val");
        assertMatchesInexact("p.Cys148→Stop");

        assertMatchesNotExact("c.1090C→T");
        assertMatchesNotExact("c.2113+1G→C");
        assertMatchesNotExact("c.847C→T");
        assertMatchesNotExact("p.Ile1019→Val");
        assertMatchesNotExact("p.Cys148→Stop");

        //[] instead of ()
        //assertMatchesInexact("c.[65_94del30]");
        assertMatchesInexact("c.(65_94del30)");
        assertMatchesInexact("c.(25-28dup)");
        //assertMatchesInexact("c.[25-28dup]");

        // '/' For substitution
        assertMatchesInexact("c.280C/T");
        assertMatchesInexact("g.3034G/A");
        assertMatchesInexact("g.260C>T");
        assertMatchesInexact("g.+260C>T");
        assertMatchesInexact("g.-247C/T");

        assertMatchesNotExact("c.280C/T");
        assertMatchesNotExact("g.-247C/T");

        assertMatchesInexact("p.W112stop");
        assertMatchesInexact("p.Trp80stop");
        assertMatchesInexact("p.Trp80STOP");
        assertMatchesInexact("p.Y164K");
        assertMatchesInexact("p.Y164*");
        assertMatchesInexact("p.Y164(*)");
        assertMatchesInexact("p.Pro530Ter");
        assertMatchesInexact("p.(Pro270Leu)");
        //assertMatchesInexact("p.Pro(270)Leu");


        //Whitespaces
        assertMatchesInexact("c.238-8G>A");
        assertMatchesInexact("p.I296V");
        assertMatchesInexact("n.6761C>T");
        assertMatchesInexact("m.7511T>C");
        assertMatchesInexact("c.*9C>T");
        assertMatchesInexact("c.947A>C");
        assertMatchesInexact("c.947G>A");
        assertMatchesInexact("g.-23449C>A");
        assertMatchesInexact("p.I296V");
        assertMatchesInexact("p.I296 V");


        //Same with whitespaces
        assertMatchesInexact("c.238-8A > T"); //Normal Whitespace (32)
        assertMatchesInexact("c.238-8A > G"); //Non Breaking Space (160)
        assertMatchesInexact("c.238-8A > C"); //Thin  Whitespace (8201)
        assertMatchesInexact("c.238-8G > A"); //Hair Space (8202)
        assertMatchesInexact("c.238-8G > T"); //four-per-em space (8197)
        assertMatchesInexact("c.238-8G > C"); //em space (8195)
        assertMatchesInexact("n.6761C > T");
        assertMatchesInexact("m.7511T > C");
        assertMatchesInexact("c.*9C > T");
        assertMatchesInexact("c.947G > A");
        assertMatchesInexact("g.-23449C > A");
        assertMatchesInexact("c.947 A>C");
        assertMatchesInexact("p.I296V");
    }

    @Test
    @Ignore("Test is ignored due to missing implementation!")
    public void testFN2() throws Exception {
        //Protein-Substitutions which are not allowed normally
        assertMatchesInexact("p.Ala270Ser");
        assertMatchesInexact("p.270Ala>Ser");
        assertMatchesInexact("p.105Ile>Val");
        assertMatchesInexact("p.183G>V");
        assertMatchesInexact("p.148Ile/Met");
        assertMatchesInexact("p.680Asn/Ser");
        assertMatchesInexact("p.284M>I");
        assertMatchesInexact("p.284 M > I");
    }


    @Test
    @Ignore("Test is ignored due to missing implementation!")
    public void testFN3() throws Exception {

        //Nucleotide mutations, which are not using >
        assertMatchesInexact("c.G1025A");
        assertMatchesInexact("c.G139C");
    }

    /**
     * Wrapper function, testing that we identify the provided string as a mutation
     * @param text String with only one mutation
     */
    private void assertMatchesInexact(String text) {

        List<MutationMention> mutationMentions = sethnerInexact.extractMutations(text);

        if(mutationMentions.size() != 1){
            System.out.println(text);
            System.out.println(mutationMentions);
            System.out.println("----");
        }

        Assert.assertEquals(1, mutationMentions.size());
        Assert.assertEquals(text, mutationMentions.get(0).getText());
    }

    /**
     */
    private void assertMatchesNotExact(String text) {

        List<MutationMention> mutationMentions = sethnerExact.extractMutations(text);

        if(mutationMentions.size() != 0){
            System.out.println(text);
            System.out.println(mutationMentions);
            System.out.println("----");
        }
        Assert.assertEquals(0, mutationMentions.size());
    }
}
