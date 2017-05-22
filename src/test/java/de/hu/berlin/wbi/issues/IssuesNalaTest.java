package de.hu.berlin.wbi.issues;

import de.hu.berlin.wbi.objects.MutationMention;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import seth.SETH;

import java.util.List;

/**
 * Some problems reported in the Nala Paper on some mutation mentions, which should be extracted by SETH
 * Created by philippe on 5/22/17.
 */
public class IssuesNalaTest {

    private SETH seth;
    @Before
    public void setUp() {
        seth = new SETH("resources/mutations.txt", false, true); //Initializes SETH
    }


    @Test
    public void testExample1() throws Exception {
        List<MutationMention> mutationMentions = seth.findMutations("c.388+3insT  c.388+3delT and c.35delG and c.35insG");
        System.out.println(mutationMentions);
        Assert.assertEquals(mutationMentions.size(), 1); //Do we actually find a mutation?
    }

    @Test //Done
    public void testExample2() throws Exception {
        List<MutationMention> mutationMentions = seth.findMutations("delPhe1388");
        System.out.println(mutationMentions);
        Assert.assertEquals(mutationMentions.size(), 1); //Do we actually find a mutation?

    }

    @Test
    public void testExample3() throws Exception {
        List<MutationMention> mutationMentions = seth.findMutations("G643 to A ");
        System.out.println(mutationMentions);
        Assert.assertEquals(mutationMentions.size(), 1); //Do we actually find a mutation?

    }

    @Test //Fixed
    public void testExample4() throws Exception {
        List<MutationMention> mutationMentions = seth.findMutations("glycine was substituted by lysine at residue 18");
        System.out.println(mutationMentions);
        Assert.assertEquals(mutationMentions.size(), 1); //Do we actually find a mutation?

    }
}