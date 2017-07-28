package de.hu.berlin.wbi.issues;

import de.hu.berlin.wbi.objects.MutationMention;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import seth.SETH;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by philippe on 05.11.16.
 * Test cases for bugs reported in issue 11 (see url https://github.com/rockt/SETH/issues/13)
 */
public class Issue13Test {

    private SETH seth;

    @Before
    public void setUp() {
        seth = new SETH("resources/mutations.txt", false, true); //Initializes SETH
    }


    /**
     * Method checks for a specified string if we find the correct HGVS representation
     * @param text text for tagging
     * @param hgvs expected HGVS mention
     */
    private void assertCorrectHGVSNormalization(String text, String hgvs){
        List<MutationMention> mutationMentions = seth.findMutations(text);

        Assert.assertTrue(mutationMentions.size() > 0); //Do we actually find a mutation?

        List<String> recognized = mutationMentions.stream().map(MutationMention::toHGVS).collect(Collectors.toList());
        Assert.assertTrue(recognized.contains(hgvs)); //Check if the mutation is correctly recognized
    }


    @Test
    public void testPmid23560553() throws Exception {
        assertCorrectHGVSNormalization("c.499C>A", "c.499C>A"); // REturns ??
    }

    @Test
    public void testPmid23588064() throws Exception {
        assertCorrectHGVSNormalization("c.7148dupT", "c.7148dupT"); // REturns ??
    }

    @Test
    public void testPmid22288660() throws Exception {
        assertCorrectHGVSNormalization("m.16278C>T", "m.16278C>T"); // REturns ??
    }

    @Test
    public void testPmid23612258() throws Exception {
        assertCorrectHGVSNormalization("c.1431_1433dupAAA", "c.1431_1433dupAAA"); // REturns ??
    }

    @Test
    public void testPmid23588064Pos() throws Exception {
        assertCorrectHGVSNormalization("c.2071-2093del", "c.2071-2093del");
    }

    @Test
    public void testPmid23405979() throws Exception {
        assertCorrectHGVSNormalization("c.904-906delGAG", "c.904-906delGAG");
    }

    @Test
    public void testPmid23623699a() throws Exception {
        assertCorrectHGVSNormalization("c.300+1G>C", "c.300+1G>C");
    }

    @Test
    public void testPmid23623699b() throws Exception {
        assertCorrectHGVSNormalization("c.1224_1225delCA", "c.1224_1225delCA");
    }

}
