package de.hu.berlin.wbi;

import de.hu.berlin.wbi.objects.MutationMention;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import seth.SETH;

import java.util.List;

/**
 * Created by philippe on 11/10/16.
 * A set of tests to improve some SETH false negatives
 */
@Ignore("Test is ignored due to missing implementation!")
public class ImproveNER {

    private SETH seth;

    @Before
    public void setUp() {
        seth = new SETH("resources/mutations.txt", false, true); //Initializes SETH
    }

    /**
     * Wrapper function, testing that we identify the provided string as a mutation
     * @param text text for which we expect exactly one mutation to be found
     */
    private void assertSingleMutation(String text) {

        List<MutationMention> mutationMentions = seth.findMutations(text);

        Assert.assertEquals(1, mutationMentions.size());
        Assert.assertEquals(text, mutationMentions.get(0).getText());
    }

    @Test
    public void test1() throws Exception {

        //Some tests where 1-letter and 3-letter versions are interchangebly used
        //<aa><num><aa>
        assertSingleMutation("L292ter");
        assertSingleMutation("E246STOP");
        assertSingleMutation("L212Glu");
        assertSingleMutation("Cys149N");
    }

    @Test
    public void test2() throws Exception {

        //Slight derivation of A123T
        //<aa><num> <aa>
        assertSingleMutation("L330 I");
        assertSingleMutation("K562 R");
        assertSingleMutation("V281 L");
    }

    @Test
    public void test3() throws Exception {
        //<aa><num>--><aa>
        assertSingleMutation("C161-->T");
        assertSingleMutation("R352-->W");
        assertSingleMutation("T29-->C");
        assertSingleMutation("G604-->T");
        assertSingleMutation("M235-->T");

        //<aa><num> --> <aa>
        assertSingleMutation("G190 --> E");
        assertSingleMutation("G252 --> C");
        assertSingleMutation("V36 --> E");

        //<aa><num>/<aa>
        assertSingleMutation("A431/P");
        assertSingleMutation("K562/S");
        assertSingleMutation("K562/R");
        assertSingleMutation("F344/N");
        assertSingleMutation("F344/N");
    }


    @Test
    public void test4() throws Exception {
        //Problems with Backus Naur
        assertSingleMutation("c.609 C>T");
        assertSingleMutation("c.844 C>T");
        assertSingleMutation("c.1088 C>T");
        assertSingleMutation("c.1426 A>G");
        assertSingleMutation("c.C2011T");
        assertSingleMutation("c.C2812T");
        assertSingleMutation("c.3435C/T");
        assertSingleMutation("c.2677A/T");
        assertSingleMutation("c.6823insA");
    }

    @Test
    public void test5() throws Exception {

        //<num> (<aa>/<aa>)
        assertSingleMutation("-819 (C/T)");
        assertSingleMutation("-819(C/T)");
        assertSingleMutation("+4766 (C/T)");
        assertSingleMutation("+4766(C/T)");
        assertSingleMutation("4766 (C/T)");
        assertSingleMutation("4766(C/T)");
    }

    @Test
    public void test6() throws Exception {

        //<num> (<aa> to <aa>)
        assertSingleMutation("301 (T to G)");
        assertSingleMutation("3420 (C to T)");
        assertSingleMutation("20203 (C to A");
        assertSingleMutation("+500 (G to A");
        assertSingleMutation("-500 (G to A");
    }

    @Test
    public void test7() throws Exception {

        //<num><aa>--><aa>
        assertSingleMutation("47F-->Y");
        assertSingleMutation("19D-->G");
        assertSingleMutation("19D-->K");

        //<aa>/<aa>-<num>
        assertSingleMutation("C/T-13910");
        assertSingleMutation("G/A-22018");
        assertSingleMutation("G/C-174");
    }

    @Test
    public void test8() throws Exception {

        //<aa> to <aa> at position <num>
        assertSingleMutation("G to A at position 18");
        assertSingleMutation("A to G at position 192");
        assertSingleMutation("C to A at position 662");

//      <aa> to <aa> transition at nucleotide <num>
        assertSingleMutation("C to T transition at nucleotide 268");
        assertSingleMutation("G to A transition at nucleotide 17");

        //<aa> to <aa> transition at position <num>
        assertSingleMutation("A to G transition at position 15386");
        assertSingleMutation("G to C transition at position 8478");


        //<num> <aa> leads to <aa>
        assertSingleMutation("121 Glu leads to Val");
        assertSingleMutation("98 Val leads to Met");
    }

    @Test
    public void test9() throws Exception {

        //IVS
        assertSingleMutation("IVS-II-745 (C-->G)");
        assertSingleMutation("IVS-I-110 (G-->A)");
        assertSingleMutation("IVS-I-1 (G-->A)");
        assertSingleMutation("IVS-I-6 (T-->C)");
    }

    @Test
    public void test10() throws Exception {

        //Different Amino Acid patterns
        //<aa>-to-<aa> mutation at amino acid <num>
        assertSingleMutation("arginine-to-alanine mutation at amino acid 217");
        assertSingleMutation("arginine to alanine mutation at amino acid 217");
        assertSingleMutation("threonine with methionine at amino acid position 790"); // <aa> with <aa> at amino acid position <num>
        assertSingleMutation("serine into phenylalanine at amino acid residue 179"); // <aa> into <aa> at amino acid residue <num>
        assertSingleMutation("glutamic acid with alanine at amino acid 420"); //<aa> with <aa> at amino acid <num>
        assertSingleMutation("tyrosine by phenylalanine at amino acid 145"); //<aa> by <aa> at amino acid <num>
        assertSingleMutation("valine by isoleucine at amino acid position");//<aa> by <aa> at amino acid position <num>
    }

    @Test
    public void test11() throws Exception {

        //Nucleotide substitutions
        assertSingleMutation("G to A substitution at position 187"); // <aa> to <aa> substitution at position <num>
        assertSingleMutation("T to C substitution at nucleotide position 533"); // <aa> to <aa> substitution at nucleotide position <num>
        assertSingleMutation("G-to-A substitution at nucleotide 6986"); //<aa>-to-<aa> substitution at nucleotide <num>
        assertSingleMutation("A to G substitution at nucleotide 750"); // <aa> to <aa> substitution at nucleotide <num>
        assertSingleMutation("G-->A substitution at position -75"); //<aa>--><aa> substitution at position <num>
        assertSingleMutation("G-->A substitution at nucleotide 1144"); // <aa>--><aa> substitution at nucleotide <num>
    }

    @Test
    public void test12() throws Exception {

        //Nucleotide transition
        assertSingleMutation("G-->A transition at nucleotide 242"); // <aa>--><aa> transition at nucleotide <num>
        assertSingleMutation("C-->T transition at position 13251"); // <aa>--><aa> transition at position <num>
        assertSingleMutation("T-to-A transition at nucleotide 2451"); // <aa>-to-<aa> transition at nucleotide <num>
        assertSingleMutation("G-to-A transition at position 4450");             //   <aa>-to-<aa> transition at position <num>
        assertSingleMutation("C to T transition at nucleotide position 452"); //<aa> to <aa> transition at nucleotide position <num>
        assertSingleMutation("A-to-G transition at nucleotide position 3243"); // <aa>-to-<aa> transition at nucleotide position <num>
        assertSingleMutation("C to T transition at codon 301"); //<aa> to <aa> transition at codon <num>
        assertSingleMutation("C to T transition in codon 82"); // <aa> to <aa> transition in codon <num>
    }

    @Test
    public void test13() throws Exception {

        //More Nucleotides
        assertSingleMutation("G to A at nucleotide 418");//        <aa> to <aa> at nucleotide <num>
        assertSingleMutation("A to G mutation at nucleotide 3243"); //<aa> to <aa> mutation at nucleotide <num>
        assertSingleMutation("A to G mutation at position 3243"); //<aa> to <aa> mutation at position <num>
        assertSingleMutation("G-->A mutation at position 20210");//                <aa>--><aa> mutation at position <num>

        assertSingleMutation("arginine-to-glutamine change at codon 583"); //<aa>-to-<aa> change at codon <num>
        assertSingleMutation("G to A at codon 408");//<aa> to <aa> at codon <num>
        assertSingleMutation("G to A at nucleotide position 210"); //<aa> to <aa> at nucleotide position <num>

    }
}