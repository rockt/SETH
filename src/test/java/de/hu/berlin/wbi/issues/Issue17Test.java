package de.hu.berlin.wbi.issues;

import de.hu.berlin.wbi.objects.MutationMention;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import seth.SETH;

import java.util.List;

/**
 * Created by philippe on 12/16/16.
 * Test cases for bugs reported in issue 11 (see url https://github.com/rockt/SETH/issues/17)
 */
public class Issue17Test {
    private SETH seth;

    @Before
    public void setUp() {
        seth = new SETH("resources/mutations.txt", false, "resources/patterns.txt"); //Initializes SETH
    }

    @Test
    public void testPmid17678724() throws Exception {
        List<MutationMention> mutationMentions = seth.findMutations("Polymorphism in MHC2TA gene (rs4,774,123G/C) are studied in two groups");
        //System.out.println(mutationMentions);
        Assert.assertEquals(1, mutationMentions.size()); //Do we actually find a mutation?

        MutationMention mm =  mutationMentions.get(0);

        Assert.assertTrue(mm.getTool().equals(MutationMention.Tool.DBSNP));
        Assert.assertEquals(mm.getText(), "rs4,774,123G/C");
        Assert.assertEquals(mm.getNormalized().size(), 1);
        Assert.assertEquals(mm.getNormalized().get(0).getRsID(), 4774123);
        Assert.assertEquals(mm.getMutResidue(), "C");
        Assert.assertEquals(mm.getWtResidue(), "G");
    }


    @Test
    public void testPmid22419714() throws Exception {
        List<MutationMention> mutationMentions = seth.findMutations("Patients carrying the TCF7L2_rs7903146_T allele had an increased risk of CRC (P(trend) = 0.02)");
        Assert.assertTrue(mutationMentions.size() > 0); //Do we actually find a mutation?

        Assert.assertTrue(mutationMentions.get(0).getTool().equals(MutationMention.Tool.DBSNP));
        Assert.assertEquals(mutationMentions.get(0).getText(), "rs7903146");
        Assert.assertEquals(mutationMentions.get(0).getNormalized().size(),1);
        Assert.assertEquals(mutationMentions.get(0).getNormalized().get(0).getRsID(), 7903146);
    }

    @Test
    public void testColon() throws Exception {
        List<MutationMention> mutationMentions = seth.findMutations("Patients carrying the :rs321 mutation");
        Assert.assertTrue(mutationMentions.size() == 1); //Do we actually find a mutation?

        Assert.assertEquals(mutationMentions.get(0).getText(), "rs321");
    }


    @Test
    public void testCorrectResidues() throws Exception {
        List<MutationMention> mutationMentions = seth.findMutations("Patients carrying the rs123A>T");
        Assert.assertTrue(mutationMentions.size() > 0); //Do we actually find a mutation?

        Assert.assertEquals(mutationMentions.get(0).getNormalized().get(0).getRsID(), 123);
        Assert.assertEquals(mutationMentions.get(0).getMutResidue(), "T");
        Assert.assertEquals(mutationMentions.get(0).getWtResidue(), "A");
    }

    @Test
    public void testRsWithAllele() throws Exception {
        List<MutationMention> mutationMentions = seth.findMutations("Patients carrying the rs123A/T");
        Assert.assertTrue(mutationMentions.size() > 0); //Do we actually find a mutation?

        Assert.assertEquals(mutationMentions.get(0).getNormalized().get(0).getRsID(), 123);
        Assert.assertEquals(mutationMentions.get(0).getMutResidue(), "T");
        Assert.assertEquals(mutationMentions.get(0).getWtResidue(), "A");
    }

    @Test
    public void testRsWithAllele2() throws Exception {
        List<MutationMention> mutationMentions = seth.findMutations("Patients carrying the rs123A\\T");
        Assert.assertTrue(mutationMentions.size() > 0); //Do we actually find a mutation?

        Assert.assertEquals(mutationMentions.get(0).getNormalized().get(0).getRsID(), 123);
        Assert.assertEquals(mutationMentions.get(0).getMutResidue(), "T");
        Assert.assertEquals(mutationMentions.get(0).getWtResidue(), "A");
    }

    @Test
    public void shortRsNames() throws Exception { //dbSNP mentions of length 1 are most likely wrong
        List<MutationMention> mutationMentions = seth.findMutations("This is most likely a false positive: rs1 ");
        Assert.assertEquals(mutationMentions.size(), 0);
    }

    @Test
    public void startingWithZero() throws Exception { //dbSNP mentions starting with a zero are wrong
        List<MutationMention> mutationMentions = seth.findMutations("This is defenitely a false positive: rs001 ");
        Assert.assertEquals(mutationMentions.size(), 0);
    }

    @Test
    public void notStartingWithZero() throws Exception {
        List<MutationMention> mutationMentions = seth.findMutations("This is most likely correct: rs1001 ");
        Assert.assertEquals(mutationMentions.size(), 1);
    }


    @Test
    public void redundant() throws Exception {
        List<MutationMention> mutationMentions = seth.findMutations("Mention with hyphens: (rs1001), colon :rs123, slash /rs754;  ");
        Assert.assertEquals(mutationMentions.size(), 3);
    }
}
