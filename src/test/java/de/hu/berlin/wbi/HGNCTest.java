package de.hu.berlin.wbi;

import de.hu.berlin.wbi.objects.MutationMention;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import seth.SETH;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: philippe
 * Date: 03.07.14
 * Time: 09:39
 *
 * Tests if a recognized mutation is correctly normalized to mutation nomenclature
 */
public class HGNCTest extends TestCase {

    private SETH seth;

    @Before
    public void setUp() {
        seth = new SETH("resources/mutations.txt", true, true); //Initializes SETH
    }

    private void assertSingleMutation(String text, String hgnc){
        List<MutationMention> mutationMentions = seth.findMutations(text);

        assertEquals(1, mutationMentions.size()); //We support only texts with one mutation
        assertTrue(hgnc.equals(mutationMentions.get(0).toHGVS())); //Is the string contained in the provided text?

    }


    @Test
    public void testBeaudet1993() throws Exception {

        assertSingleMutation("deltaT2", "p.Thr2del");
        assertSingleMutation("Gly376 with alanine", "p.Gly376Ala"); //protein-mutation due to Gly&Alanine
        assertSingleMutation("G376A", "?.376G>A");      //Unknown mutation (G/A) can be DNA/protein
        assertSingleMutation("Thr92Ala", "p.Thr92Ala"); //Protein (Thr)
        assertSingleMutation("Arg648Stop", "p.Arg648Ter"); //Protein
        assertSingleMutation("G-455A", "c.-455G>A"); //DNA (-455)
        assertSingleMutation("DeltaF508", "p.Phe508del");
        assertSingleMutation("185delAG", "c.185delAG");
        assertSingleMutation("5382insC", "c.5382insC");
        assertSingleMutation("D281fs", "?.D281fs");
        assertSingleMutation("G165fsX8", "?.G165fsX8");
        assertSingleMutation("rs123", "rs123");

    }


}
