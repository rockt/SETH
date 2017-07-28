package de.hu.berlin.wbi.issues;

import de.hu.berlin.wbi.objects.MutationMention;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import seth.SETH;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by philippe on 11/2/16.
 * Test cases for bugs reported in issue 10 (see url https://github.com/rockt/SETH/issues/10)
 */
//@Ignore("Test is ignored due to missing implementation!")
public class Issue10Test {

    private SETH seth;

    @Before
    public void setUp() {
        seth = new SETH("resources/mutations.txt", false, true); //Initializes SETH
    }


    /**
     * Method checks for a specified string if we find a specific mutation
     * @param text text for tagging
     * @param mutation expected mutation string
     */
    private void assertFoundMutation(String text, String mutation){
        List<MutationMention> mutationMentions = seth.findMutations(text);

        Assert.assertTrue(mutationMentions.size() > 0); //Do we actually find a mutation?
        List<String> recognized = mutationMentions.stream().map(MutationMention::getText).collect(Collectors.toList());
        System.out.println(recognized);
        Assert.assertTrue(recognized.contains(mutation)); //Check if the mutation is correctly recognized
    }


    @Test
    public void testPmid20806047() throws Exception {
        assertFoundMutation("A novel dominant mutation at the stop codon of FOXE3, c.959G>C (p.X320SerextX72)",
                "p.X320SerextX72");
    }

    @Test
    @Ignore("Test is ignored due to missing implementation!")
    public void testPmid23903049() throws Exception {
        assertFoundMutation("Two novel mutations (p.Leu317Ser and p.His33GInfsX32) are described.",
                "p.His33GInfsX32");
    }

    @Test
    @Ignore("Test is ignored due to missing implementation!")
    public void testPmid22907560() throws Exception {
        //Hys is no valid amino acid
        assertFoundMutation("predicted to cause a p.Arg313Hys amino acid change.",
                "p.Arg313Hys");
    }

    @Test
    public void testPmid18486607() throws Exception {
        //Fixed
        assertFoundMutation("p.Arg315Stop", "p.Arg315Stop");
    }

    @Test
    public void testPmid23017188() throws Exception {
        //Del with upper case
        assertFoundMutation("Identification of the CFTR p.Phe508Del founder ",
                "p.Phe508Del");
    }

    @Test
    @Ignore("Test is ignored due to missing implementation!")
    public void testPmid24158885() throws Exception {
        //Amino acids with different upper/lower case
        assertFoundMutation("p.Met694IIe",
                "p.Met694IIe");
    }

    @Test
    @Ignore("Test is ignored due to missing implementation!")
    public void testPmid23856132() throws Exception {
        assertFoundMutation("The predominant mutation in this population sample was p.F55>Lfs ",
                "p.F55>Lfs");
    }

    @Test
    @Ignore("Test is ignored due to missing implementation!")
    public void testPmid18708425() throws Exception {
        assertFoundMutation("We report an insertion of two leucines (p.L21tri also designated p.L15_L16ins2L) ",
                "p.L15_L16ins2L");
    }
}
