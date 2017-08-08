package de.hu.berlin.wbi;

import de.hu.berlin.wbi.objects.MutationMention;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import seth.oldNomenclature.OldNomenclature;

import java.util.List;

/**
 * Created by philippe on 6/28/17.
 */
public class InsDelFinderTest {

    private OldNomenclature oldNomenclature;

    @Before
    public void setUp() {
        oldNomenclature =  new OldNomenclature("resources/patterns.txt"); //Initializes SETH
    }

    /**
     * Wrapper function, testing that we identify the provided string as a mutation
     * @param text String with only one mutation
     */
    private void assertSingleMutation(String text) {

        List<MutationMention> mutationMentions = oldNomenclature.extractMutations(text);

        if(mutationMentions.size() != 1){
            System.out.println(text);
            System.out.println(mutationMentions);
            System.out.println("----");
        }

        Assert.assertEquals(1, mutationMentions.size());
        Assert.assertEquals(text, mutationMentions.get(0).getText());
    }

    private void assertNoMutation(String text){
        List<MutationMention> mutationMentions = oldNomenclature.extractMutations(text);

        if(mutationMentions.size() != 0){
            System.out.println(text);
            System.out.println(mutationMentions);
            System.out.println(mutationMentions.get(0).getPatternId());
            System.out.println("----");
        }
        Assert.assertEquals(0, mutationMentions.size());
    }
/**

    InsP3
            InsP6
    ΔR2
            ΔR1
    ΔT1
            ΔN6
    ΔR5
            ΔD1
    L1 insertions
    E1-deleted
    6N del
    ΔE1
            ΔT2
*/
    @Test
    public void testFN1() throws Exception {

        //<number><kw><aa>
        assertSingleMutation("962insG");
        assertSingleMutation("962-963insG");
        assertSingleMutation("737-739delCCA");
        assertSingleMutation("-11delC");
        assertSingleMutation("4216-4217delAG");
        assertSingleMutation("368-369 insTCCTGCCCACCACGCTCACCACG");
        assertSingleMutation("417 ins T");

        assertSingleMutation("Arg(402-403ins");
    }


    @Test
    public void testFN2() throws Exception {

        //Some problem with one digit numbers
        assertSingleMutation("3insT");
        assertSingleMutation("-3insT");
        assertSingleMutation("+3insT");

        assertSingleMutation("DeltaS60");
        assertSingleMutation("DeltaS6");
        assertSingleMutation("delS1");

        assertSingleMutation("G10del");
        assertSingleMutation("G1del");
    }

    @Test
    public void testFN4() throws Exception {
        assertSingleMutation("Deleting Tyr(490");
        assertSingleMutation("Deleting Tyr 490");
        assertSingleMutation("converted GA(12");
        assertSingleMutation("Inserting Pro(510");
        assertSingleMutation("translocations T(10");
    }

    @Test
    public void testMentionsFs() throws Exception {
        //Frameshift
        assertSingleMutation("M245Vfs");
        assertSingleMutation("M245VfsX");
        assertSingleMutation("P91QfsX");

        assertSingleMutation("Q612fsX648");
        assertSingleMutation("C1024fsX1037");
        assertSingleMutation("L116fsX");
        assertSingleMutation("D272fs");
        assertSingleMutation("Q374fsX384");
        assertSingleMutation("P412fsX446");
    }

    @Test
    public void testPatterns() throws  Exception{
        //Tests from patterns
        assertSingleMutation("123delT");
        assertSingleMutation("123del(T");
        assertSingleMutation("Tyr(12)ins");
        assertSingleMutation("Del(Tyr)-15");
        assertSingleMutation("Tyr exon 5 inserted");
        assertSingleMutation("Tyr deletion after position 5");
        assertSingleMutation("Tyr deletion at codon 5");
        assertSingleMutation("Tyr deletion at position 5");
        assertSingleMutation("Tyr deletion after position 5");
        assertSingleMutation("Tyr deletion after codon 5");
        assertSingleMutation("Tyr deletion at the 5th amino acid");
        assertSingleMutation("Tyr-14 was deleted");
        assertSingleMutation("Deletion after Ala12");
        assertSingleMutation("Deletion in the Ala12");
        assertSingleMutation("Deletion of Ala12");
        assertSingleMutation("Deletion of the Cysteine12");
        assertSingleMutation("123 nucleotide deleted in the Ala");
        assertSingleMutation("Deletion of an Alanine at position 12");

        //<kw> of <aa> at <number>
        assertSingleMutation("Deletion of Alanine at 123");
        assertSingleMutation("deletion of an Ala at position 24"); //<kw> of (a|an) <aa> at (codon|position)? <number>
        assertSingleMutation("Deletion of an Alanine at 12");
        assertSingleMutation("Deletion of Ala at 12"); //<kw> of <aa> at (codon|position)? <number>
        assertSingleMutation("Deletion of a Cysteine at 12");
        assertSingleMutation("Deletion of a Cysteine at position 12");
        assertSingleMutation("Deletion of a Cysteine residue at position 12");
        assertSingleMutation("Deletion of a Cysteine residue at codon 12");
    }

    @Test
    public void testPatterns2() throws  Exception{
        assertSingleMutation("delAla12");
        assertSingleMutation("del Ala 12");
        assertSingleMutation("123delATG");
        assertSingleMutation("Deletion of Ala12");
        assertSingleMutation("Deletion of Ala-12");
        assertSingleMutation("Deletion of Ala 12");
        assertSingleMutation("Deletion of the Tyr 12");
        assertSingleMutation("Deletion of the Tyr-12");
        assertSingleMutation("del of ATG at position 123");
        assertSingleMutation("deletion of Ala(12");
        assertSingleMutation("deletion of a tyr residue at codon 5");
        //assertSingleMutation("deletion of a tyr residue at codon 5 or 4");
        assertSingleMutation("insertion in the Ala12");
        assertSingleMutation("Ala 12 was deleted");
        assertSingleMutation("Ala inserted at position 4");
        assertSingleMutation("Ala deletion at codon 5");
        assertSingleMutation("Thr exon 55 del");
        assertSingleMutation("12 nucleotide deleted in the Tyr"); //<
        assertSingleMutation("deletion Ala(12");
        assertSingleMutation("del Ala12");
        assertSingleMutation("123-Ala deletion");
        assertSingleMutation("123 Ala deletion");
        assertSingleMutation("deletion of a Tyrosine residue at position 12");
        assertSingleMutation("deletion of a alanine residue at position 12");
        assertSingleMutation("Insertion after Ala 12");
        assertSingleMutation("deletion (Ala12");
        assertSingleMutation("Ala deletion at the 521th amino acid");
        assertSingleMutation("Tyr deletion after position 32");

    }


    /**
     *   JUnit test classes generated for the pattern
     *   <kw> of <aa><number>
     */
    @Test
    public void testMention1() throws Exception {
        assertSingleMutation("deletion of Ala12");
        assertSingleMutation("del of Ala12");
        assertSingleMutation("Del of Ala12");
        assertSingleMutation("del of A12");
        assertSingleMutation("del of ATGC12");
    }



    /**
     *   JUnit test classes generated for the pattern
     *   <aa><number><kw>
     */
    @Test
    public void testMention2() throws Exception {
        assertSingleMutation("Tyr12del");
        assertSingleMutation("A12del");
        assertSingleMutation("Tyr15ins");
        assertSingleMutation("T15ins");
        assertSingleMutation("Alanine54dup");
        assertSingleMutation("T-15ins");
        assertSingleMutation("T5_7ins");

    }

    /**
     *   JUnit test classes generated for the pattern
     *    <kw> of <aa>\-<number>
     */
    @Test
    public void testMention3() throws Exception {
        assertSingleMutation("deletion of Ala-12");
        assertSingleMutation("duplication of A-12");
    }

    /**
     *   JUnit test classes generated for the pattern
     *   <aa>\(<number>\) <kw>
     */
    @Test
    public void testMention4() throws Exception {
        assertSingleMutation("Thr(12) del");
    }

    /**
     *   JUnit test classes generated for mutations,
     *   frequently detected by previous module
     */
    @Test
    public void testMention5() throws Exception {
        assertSingleMutation("F508del");
        assertSingleMutation("DeltaF508");
        assertSingleMutation("delF508");
        assertSingleMutation("deltaF508");
        assertSingleMutation("insF508");
        assertSingleMutation("ins/delF508");

        assertSingleMutation("35delG");
        assertSingleMutation("Lys107del");
        assertSingleMutation("713-8delC");

    }


    /**
     * Bugs, we observed during development
     */
    @Test
    public void testMention6() throws Exception {
        assertSingleMutation("Trp53Δ");
        assertSingleMutation("ΔF508");
        assertSingleMutation("translocation of Arg-143");
        assertSingleMutation("deletion of Phe508");
        assertSingleMutation("deletion of Gly-457");
        assertSingleMutation("T-1201 insertion");
        assertSingleMutation("deletion of Gly-457");
        assertSingleMutation("translocation of T308");

        assertNoMutation("*3del");
        assertNoMutation("2282del4");
        assertNoMutation("delta1");
        //assertNoMutation("conversion of L-3, 4");
        //assertNoMutation("ΔH -47.57");
        assertNoMutation("N-24:1 delta");
        assertNoMutation("Deletion of P2Y2");
        assertNoMutation("Deletion of P2X7");
        assertNoMutation("Deletion of S100A4");
        assertNoMutation("deletion of E2F1");
        assertNoMutation("deletion of S1P1");
    }

    /**
     * We observed that the component extracts frequent false positives
     * which are quite ambiguous
     */
    @Test
    public void testMention7() throws Exception {
        //assertNoMutation("ΔR2");
        assertSingleMutation("ΔTyr2");
//        assertNoMutation("L1 insertions");
        assertSingleMutation("Lys1 insertions");
        //assertNoMutation("InsP3");
        //assertNoMutation("insertion of S3");
        //assertNoMutation("2C delta");
        //assertNoMutation("2D insertion");
        assertSingleMutation("conversion of Tyr-1");
    }

    @Test
    public void testMentionIVS() throws Exception {
        assertSingleMutation("IVS3+2T>C");
        assertSingleMutation("IVS1-1724 C>G");
        assertSingleMutation("IVS9-28A/G");
    }

    @Test
    public void testNormalization()throws Exception {

        assertNormalizedMutation("InsP8", "P8ins");
        assertNormalizedMutation("648dupGTT", "GTT648dup");
        assertNormalizedMutation("G417AfsX7", "G417AfsX7");
        assertNormalizedMutation("M2-deleted", "M2del");
        assertNormalizedMutation("256delGGACAACCTCAAGGGCACCT", "GGACAACCTCAAGGGCACCT256del");
        assertNormalizedMutation("860insACCT", "ACCT860ins");
        assertNormalizedMutation("648dupGTT", "GTT648dup");
        assertNormalizedMutation("converting serine 208", "S208con");
        assertNormalizedMutation("S2 inversion", "S2inv");
    }


    private void assertNormalizedMutation(String text, String normalized) {

        List<MutationMention> mutationMentions = oldNomenclature.extractMutations(text);

        if(mutationMentions.size() != 1){
            System.out.println(text);
            System.out.println(mutationMentions);
            System.out.println("----");
        }

        Assert.assertEquals(1, mutationMentions.size());
        Assert.assertEquals(normalized, mutationMentions.get(0).toNormalized());
    }
}
