package de.hu.berlin.wbi;

import de.hu.berlin.wbi.objects.MutationMention;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import seth.oldNomenclature.OldNomenclature2;

import java.util.List;

/**
 * Created by philippe on 6/28/17.
 */
public class InsDelFinderTest {

    private OldNomenclature2 oldNomenclature2;

    @Before
    public void setUp() {
        oldNomenclature2 =  new OldNomenclature2("/media/philippe/5f695998-f5a5-4389-a2d8-4cf3ffa1288a/data/pubmed/rawInsDels.sorted.annotated"); //Initializes SETH
    }

    /**
     * Wrapper function, testing that we identify the provided string as a mutation
     * @param text String with only one mutation
     */
    private void assertSingleMutation(String text) {

        List<MutationMention> mutationMentions = oldNomenclature2.extractMutations(text);

        if(mutationMentions.size() != 1){
            System.out.println(text);
            System.out.println(mutationMentions);
            System.out.println("----");
        }

        Assert.assertEquals(1, mutationMentions.size());
        Assert.assertEquals(text, mutationMentions.get(0).getText());
    }

    private void assertNoMutation(String text){
        List<MutationMention> mutationMentions = oldNomenclature2.extractMutations(text);

        if(mutationMentions.size() != 0){
            System.out.println(text);
            System.out.println(mutationMentions);
            System.out.println(mutationMentions.get(0).getPatternId());
            System.out.println("----");
        }
        Assert.assertEquals(0, mutationMentions.size());
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
        assertNoMutation("conversion of L-3, 4");
        assertNoMutation("ΔH -47.57");
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
        assertNoMutation("ΔR2");
        assertSingleMutation("ΔTyr2");
        assertNoMutation("L1 insertions");
        assertSingleMutation("Lys1 insertions");
        assertNoMutation("InsP3");
        assertNoMutation("insertion of S3");
        assertNoMutation("2C delta");
        assertNoMutation("2D insertion");
        assertSingleMutation("conversion of Tyr-1");
    }

    @Test
    public void testMentionIVS() throws Exception {
        assertSingleMutation("IVS3+2T>C");
        assertSingleMutation("IVS1-1724 C>G");
        assertSingleMutation("IVS9-28A/G");
    }
}
