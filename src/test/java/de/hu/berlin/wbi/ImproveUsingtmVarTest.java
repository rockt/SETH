package de.hu.berlin.wbi;

import de.hu.berlin.wbi.objects.MutationMention;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import seth.SETH;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by philippe on 03/08/2017
 * A set of tests to improve some SETH false negatives using tmVar
 */
public class ImproveUsingtmVarTest {

    private SETH seth;

    @Before
    public void setUp() {
        seth = new SETH("resources/mutations.txt", false, "resources/patterns.txt"); //Initializes SETH
    }

    /**
     * Wrapper function, testing that we identify the provided string as a mutation
     * @param text text for which we expect exactly one mutation to be found
     */
    private void assertSingleMutation(String text) {
        List<MutationMention> mutationMentions = seth.findMutations(text);
        System.out.println(mutationMentions);

        if(mutationMentions.size() == 0)
            System.out.print("No match for '" +text +"'");

        Assert.assertEquals(1, mutationMentions.size());
        Assert.assertEquals(text, mutationMentions.get(0).getText());
    }



    @Test
    public void testNucleotideMentions2() throws Exception {
        assertSingleMutation("G1141-->T");
        assertSingleMutation("A1166 --> C");
        assertSingleMutation("G1691 --> A");
    }

    @Test
    public void testIVS() throws Exception {
        assertSingleMutation("IVS1+1G>T");
        assertSingleMutation("IVS1-1G>T");
        assertSingleMutation("IVS1-1G/T");
        assertSingleMutation("IVS1-1G→T");
        assertSingleMutation("IVS2-53T-->C");
        assertSingleMutation("IVS2-53t-->c");
        assertSingleMutation("IVS2+1 G to A");
    }

    @Test
    public void testIVS2() throws Exception {
        //Special cases, where we often find more than one mutation
        assertSingleMutation("IVS2 -1G>A");
        assertSingleMutation("IVS2 + 1 G-->A");
        assertSingleMutation("IVS4 + 4A > T");
    }

    @Ignore("Test is ignored due to missing implementation!")
    @Test
    public void testNucleotideMentions() throws Exception {
        //Defenitely basepair
        /**

        -75bp G/A
        896(A/G)
        T-->C substitution at -47
        A>C in nucleotide 7244
        A to G at position 380
        G --> A mutation at 19
        A to G at position 1400
        G to A at nucleotide 418
                -863(C/A)
        c.568G to A
                -173 G to C
        A985-G
        A > G at base 17
        G-->A transition at base 490
                -7*C/T
                +141*A/G

        1021C --> T
                -238G   >   A
         */
        assertSingleMutation("G>T at position 109");
        assertSingleMutation("C-->T in nucleotide 380");
        assertSingleMutation("C-->G substitution at nucleotide -114");
        assertSingleMutation("A-->G 125 bp");
        assertSingleMutation("G to C at base pair 1");
        assertSingleMutation("G-->C transversion (nucleotide position 693)");
        assertSingleMutation("G-->A substitution at position 20210A");
        assertSingleMutation("T to C transition at base pair 6");
        assertSingleMutation("G-->C at the position 2509");
        assertSingleMutation("AAC-->TTC at position 37");
        assertSingleMutation("A --> G point mutation at 1009 bp");
        assertSingleMutation("T-->A transition at position 1783");
        //assertSingleMutation("C1400 was replaced by U, A, or G");
    }

    @Test
    public void testShortMentions() throws Exception {
        assertSingleMutation("A12->T");
        assertSingleMutation("Q10>K");
        assertSingleMutation("816D>V");
        assertSingleMutation("82G>S");

        assertSingleMutation("S277V");
        assertSingleMutation("S277Ter");
        assertSingleMutation("R633Stop");
        assertSingleMutation("L91Ser");
        assertSingleMutation("L96Trp");
        assertSingleMutation("H33Trp");
        assertSingleMutation("L91Asn");

        //<aa><number>[>/]<aa>
        assertSingleMutation("Ala91/Asn");
        assertSingleMutation("Ala91(Asn");
        assertSingleMutation("Ala91>Asn");
        assertSingleMutation("L91>N");
        assertSingleMutation("L91-->N");

        //<number> (<aa>/<aa>)
        assertSingleMutation("91 (Ala-->Tyr");
        assertSingleMutation("91 (Ala->Tyr");
        assertSingleMutation("91 (Ala>Tyr");
        assertSingleMutation("91 (Ala→Tyr");
        assertSingleMutation("91 (Ala/Tyr");
    }


    @Ignore("Test is ignored due to missing implementation!")
    @Test
    public void testAAMentions() throws Exception {
        //<mutaa> for <wtaa> at position <number>
        assertSingleMutation("Ala for Pro at position 7");
        assertSingleMutation("Ala for Term at position 7");
        assertSingleMutation("Ala for Ter at position 7");
        assertSingleMutation("glutamic acid residue in position 266 with aspartic acid");
        assertSingleMutation("Glu(3) was changed into Ile");
        assertSingleMutation("Glu (3) was changed into Ile");
        assertSingleMutation("valine for alanine at amino acid position 77");
        assertSingleMutation("Ala to Val amino acid change at 9");
        assertSingleMutation("serine residue in position 8 is replaced with alanine");
        assertSingleMutation("Ala leads to Val at amino acid position 2");
        assertSingleMutation("Ala leads to Val at amino acid positions 2");
        assertSingleMutation("leucine replaces glutamic acid as residue 2");
        assertSingleMutation("Tyr7-->Gln");
        assertSingleMutation("arginine 4 has been replaced by an alanine");
        assertSingleMutation("Tyr 7 has been replaced with Ala");
        assertSingleMutation("Tyr 77 has been replaced with Ala");
        assertSingleMutation("arginine residue at position 7 was replaceable with alanine");
        assertSingleMutation("arginine for lysine at aa 3");
        assertSingleMutation("isoleucine with leucine at amino acid positions 5");
        assertSingleMutation("Ser(2) replaced by aspartic acid");
        assertSingleMutation("Ser(9) were mutated to Ala");
        assertSingleMutation("Ile --> (79)Val");
        assertSingleMutation("Lys206 is mutated to Glu");
        assertSingleMutation("Ala replacement of Ile-6");
        assertSingleMutation("Ala replacement of Ile 6");
        assertSingleMutation("Ala replacement of Ile6");
        assertSingleMutation("70Val substituted by Ala");
        assertSingleMutation("Ser-384 mutated to proline");
        assertSingleMutation("Ser 384 mutated to proline");
        assertSingleMutation("Lys substituted for Glu 88");
        assertSingleMutation("Lys substituted for Glu-88");
        assertSingleMutation("Lys substituted for Glu88");





       /**
        Ser17 substituted with Asn
        Leu86 was changed into Ala
        N481 --> Y
        168(P-->Q)
        127 Leu>Val
        198Pro--> Leu
        Thr 241 > Met
        Glu3 and Asp4 to histidine
        Tyr for Ser at residue 116
        His for Arg at residue 858
        Phe substitution of Cys(21
        146 histidine  >  aspartic
        Thr304 substituting for Ala
        Ser-48 substituted with Thr
        Ser 230 was changed into Ala
        132 lysine leads to glutamine
        Ser72 is substituted with Ala
        Asp(340) is replaced with Cys
        alanine replaces threonine 245
        His-118 substituted by glycine
        Tyr 705 substituted with Phe
        Asn-496 > Lys
        S727 to proline
        assertSingleMutation(" Leu(3) and Leu(14)) with Phe");
        assertSingleMutation("Tyr(4) and Trp(6) with alanine");
        Lys65 to Ala and Arg
        Tyr26 with Phe or Trp
        Asp6 replaced with Thr
        Glu instead of Val-659
        Gln165 with Glu or Leu
        Gly-130 to Ser and Thr
        Lys(136) to Glu or Thr
        112His leads to Arg
        Tyr82 by Ser or Leu
        */
    }
}
/**
p.Trp80stop
        p.Try290Gln
        p.GLY605Ala
        p.Pro 533 Arg
        p.Tyr177-Val
        p. V122V
        p.49A >T
        p.N413 K
        p.28Arg>Gln
        p.Lys(119)Thr
        p.Arg550X
        p.E169stop
        p.(P155L)


        g. IVS8 +75G>A
        g.499G/ A
        g.596T/C
        g.83097C/T
        g.83159G/A
        g.18 G>C
        g.1728 G --> A
        g.+408C > A


        c. 7471T to C
        c.2439C/T
        c.649 A>G
        c.4330 T --> C
        c.(-203)A>G
        c.1517+6 G-->A
        c.C2011T
        c.665 G > A
        c.228-16 C > G
 */