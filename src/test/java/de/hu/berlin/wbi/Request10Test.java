package de.hu.berlin.wbi;

import de.hu.berlin.wbi.objects.MutationMention;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import seth.SETH;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertThat;

/**
 * Created by philippe on 11/2/16.
 * Test cases for bugs reported in issue 10 (see url https://github.com/rockt/SETH/issues/10)
 */
@Ignore("Test is ignored due to missing implementation!")
public class Request10Test extends TestCase {

    private SETH seth;

    @Before
    public void setUp() {
        seth = new SETH("resources/mutations.txt", false, true); //Initializes SETH
    }


    /**
     * Method checks for a specified string if we find a specific mutation
     * @param text
     * @param mutation
     */
    private void assertFoundMutation(String text, String mutation){
        List<MutationMention> mutationMentions = seth.findMutations(text);

        assertTrue(mutationMentions.size() > 0); //Do we actually find a mutation?

        List<String> recognized = mutationMentions.stream().map(a -> a.getText()).collect(Collectors.toList());
        assertTrue(recognized.contains(mutation)); //Check if the mutation is correctly recognized
    }


    @Test
    public void testPmid20806047() throws Exception {
        assertFoundMutation("A novel dominant mutation at the stop codon of FOXE3, c.959G>C (p.X320SerextX72), was identified in a patient with congenital cataract",
                "p.X320SerextX72");
    }

    @Test
    public void testPmid23903049() throws Exception {
        assertFoundMutation("Two novel mutations (p.Leu317Ser and p.His33GInfsX32) are described.",
                "p.His33GInfsX32");
    }

    @Test
    public void testPmid22907560() throws Exception {
        assertFoundMutation("predicted to cause a p.Arg313Hys amino acid change.", "p.Arg313Hys");
    }

    @Test
    public void testPmid18486607() throws Exception {
        assertFoundMutation("p.Arg315Stop", "p.Arg315Stop");
    }

    @Test
    public void testPmid23017188() throws Exception {
        assertFoundMutation("Identification of the CFTR p.Phe508Del founder mutation in the absence of a polythymidine 9T allele in a Hispanic patient.", "p.Phe508Del");
    }

    @Test
    public void testPmid24158885() throws Exception {
        assertFoundMutation("p.Met694IIe", "p.Met694IIe");
    }

    @Test
    public void testPmid23856132() throws Exception {
        assertFoundMutation("The predominant mutation in this population sample was p.R261Q G>A, p.F55>Lfs and p.R243Q G>A.", "p.F55>Lfs");
    }

    @Test
    public void testPmid18708425() throws Exception {
        assertFoundMutation("We report an insertion of two leucines (p.L21tri also designated p.L15_L16ins2L) in the leucine stretch of the signal peptide of PCSK9 that is found in two of 25 families with familial combined hyperlipidaemia (FCHL).", "p.L15_L16ins2L");
    }
}
