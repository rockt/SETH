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
public class Issue17Test extends TestCase {
    private SETH seth;

    @Before
    public void setUp() {
        seth = new SETH("resources/mutations.txt", false, true); //Initializes SETH
    }

    @Test
    public void testPmid17678724() throws Exception {
        List<MutationMention> mutationMentions = seth.findMutations("Polymorphism in MHC2TA gene (rs4,774,123G/C) are studied in two groups");
        System.out.println(mutationMentions);
        assertTrue(mutationMentions.size() == 2); //Do we actually find a mutation?

        for(MutationMention mm: mutationMentions){
            if(!mm.getTool().equals(MutationMention.Tool.DBSNP))
                continue;

            assertTrue(mm.getTool().equals(MutationMention.Tool.DBSNP));
            assertEquals(mm.getText(), "rs4,774,123G/C");
            assertTrue(mm.getNormalized().size() ==1 );
            assertEquals(mm.getNormalized().get(0).getRsID(), 4774123);
            assertEquals(mm.getMutResidue() , "C");
            assertEquals(mm.getWtResidue() , "G");
        }
    }


    @Test
    public void testPmid22419714() throws Exception {
        List<MutationMention> mutationMentions = seth.findMutations("Patients carrying the TCF7L2_rs7903146_T allele had an increased risk of CRC (P(trend) = 0.02)");
        assertTrue(mutationMentions.size() > 0); //Do we actually find a mutation?

        assertTrue(mutationMentions.get(0).getTool().equals(MutationMention.Tool.DBSNP));
        assertEquals(mutationMentions.get(0).getText(), "rs7903146");
        assertTrue(mutationMentions.get(0).getNormalized().size() ==1 );
        assertEquals(mutationMentions.get(0).getNormalized().get(0).getRsID(), 7903146);

    }

    @Test
    public void testColon() throws Exception {
        List<MutationMention> mutationMentions = seth.findMutations("Patients carrying the :rs321 mutation");
        assertTrue(mutationMentions.size() == 1); //Do we actually find a mutation?

        assertEquals(mutationMentions.get(0).getText(), "rs321");
    }


    @Test
    public void testCorrectResidues() throws Exception {
        List<MutationMention> mutationMentions = seth.findMutations("Patients carrying the rs123A>T");
        assertTrue(mutationMentions.size() > 0); //Do we actually find a mutation?

        assertEquals(mutationMentions.get(0).getNormalized().get(0).getRsID(),123);
        assertEquals(mutationMentions.get(0).getMutResidue(),"T");
        assertEquals(mutationMentions.get(0).getWtResidue(),"A");
    }

    @Test
    public void testRsWithAllele() throws Exception {
        List<MutationMention> mutationMentions = seth.findMutations("Patients carrying the rs123A/T");
        assertTrue(mutationMentions.size() > 0); //Do we actually find a mutation?

        assertEquals(mutationMentions.get(0).getNormalized().get(0).getRsID(),123);
        assertEquals(mutationMentions.get(0).getMutResidue(),"T");
        assertEquals(mutationMentions.get(0).getWtResidue(),"A");
    }

    @Test
    public void testRsWithAllele2() throws Exception {
        List<MutationMention> mutationMentions = seth.findMutations("Patients carrying the rs123A\\T");
        assertTrue(mutationMentions.size() > 0); //Do we actually find a mutation?

        assertEquals(mutationMentions.get(0).getNormalized().get(0).getRsID(),123);
        assertEquals(mutationMentions.get(0).getMutResidue(),"T");
        assertEquals(mutationMentions.get(0).getWtResidue(),"A");
    }

    @Test
    public void shortRsNames() throws Exception {
        List<MutationMention> mutationMentions = seth.findMutations("This is most likely a false positive: rs1 ");
        assertTrue(mutationMentions.size() == 0);
    }
}
