package de.hu.berlin.wbi.issues;

import de.hu.berlin.wbi.objects.MutationMention;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import seth.SETH;

import java.util.List;

/**
 * Created by philippe on 12/16/16.
 * Test cases for bugs reported in issue 11 (see url https://github.com/rockt/SETH/issues/17)
 */
@Ignore("Test is ignored due to missing implementation!")
public class Issue17Test extends TestCase {
    private SETH seth;

    @Before
    public void setUp() {
        seth = new SETH("resources/mutations.txt", false, true); //Initializes SETH
    }

    @Test
    public void testPmid17678724() throws Exception {
        List<MutationMention> mutationMentions = seth.findMutations("Two polymorphisms in MHC2TA gene (rs4,774G/C and rs3,087,456A/G) were studied in two groups");
        System.out.println(mutationMentions);
        assertTrue(mutationMentions.size() > 0); //Do we actually find a mutation?

        assertTrue(mutationMentions.get(0).getTool().equals(MutationMention.Tool.DBSNP));
        assertEquals(mutationMentions.get(0).getText(), "rs4,774G/C");
        assertTrue(mutationMentions.get(0).getNormalized().size() ==1 );
        assertEquals(mutationMentions.get(0).getNormalized().get(0).getRsID(), 4774);

        assertTrue(mutationMentions.get(1).getTool().equals(MutationMention.Tool.DBSNP));
        assertEquals(mutationMentions.get(1).getText(), "rs3,087,456A/G");
        assertTrue(mutationMentions.get(0).getNormalized().size() ==1 );
        assertEquals(mutationMentions.get(0).getNormalized().get(1).getRsID(), 3087456);

    }

    @Test
    public void testPmid22419714() throws Exception {
        List<MutationMention> mutationMentions = seth.findMutations("Patients carrying the TCF7L2_rs7903146_T allele had an increased risk of CRC (P(trend) = 0.02), whereas patients harboring the IL13_rs20541_T allele had a reduced risk (P(trend) = 0.02)");
        System.out.println(mutationMentions);
        assertTrue(mutationMentions.size() > 0); //Do we actually find a mutation?

        assertTrue(mutationMentions.get(0).getTool().equals(MutationMention.Tool.DBSNP));
        assertEquals(mutationMentions.get(0).getText(), "rs7903146");
        assertTrue(mutationMentions.get(0).getNormalized().size() ==1 );
        assertEquals(mutationMentions.get(0).getNormalized().get(1).getRsID(), 7903146);


        assertTrue(mutationMentions.get(1).getTool().equals(MutationMention.Tool.DBSNP));
        assertEquals(mutationMentions.get(1).getText(), "rs20541");
        assertTrue(mutationMentions.get(1).getNormalized().size() ==1 );
        assertEquals(mutationMentions.get(1).getNormalized().get(1).getRsID(), 20541);

    }

    @Test
    public void testColon() throws Exception {
        List<MutationMention> mutationMentions = seth.findMutations("Patients carrying the rs123 and :rs321 mutation");
        System.out.println(mutationMentions);
        assertTrue(mutationMentions.size() > 0); //Do we actually find a mutation?

        assertEquals(mutationMentions.get(0).getText(), "rs123");
        assertEquals(mutationMentions.get(1).getText(), "rs321");
    }


    @Test
    public void testCorrectResidues() throws Exception {
        List<MutationMention> mutationMentions = seth.findMutations("Patients carrying the rs123A>T");
        System.out.println(mutationMentions);
        assertTrue(mutationMentions.size() > 0); //Do we actually find a mutation?

        assertEquals(mutationMentions.get(0).getNormalized().get(0).getRsID(),123);
        assertEquals(mutationMentions.get(0).getMutResidue(),"T");
        assertEquals(mutationMentions.get(0).getWtResidue(),"A");
    }
}
