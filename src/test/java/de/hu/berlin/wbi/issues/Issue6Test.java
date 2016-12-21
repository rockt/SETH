package de.hu.berlin.wbi.issues;


import de.hu.berlin.wbi.objects.MutationMention;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import seth.SETH;
import seth.ner.wrapper.Type;

import java.util.List;

/**
 * Created by philippe on 11/2/16.
 * Test cases for bugs reported in issue 6 https://github.com/rockt/SETH/issues/6
 */
public class Issue6Test {


    @Test
    public void testIssue6A() throws Exception {
        String mutationString ="p.Glu746_Thr751delinsAla";


        //Correct Type
        SETH sethInexact = new SETH("resources/mutations.txt", false, true);
        List<MutationMention> ms = sethInexact.findMutations(mutationString);
        Assert.assertEquals(ms.size(), 1);
        Assert.assertEquals(ms.get(0).getType(), Type.DELETION_INSERTION);
    }

    @Test
    public void testIssue6B() throws Exception {
        String mutationString ="p.Glu746_Thr751delinsAla";

        //Incorrect Type
        SETH sethExact = new SETH("resources/mutations.txt", true, true);
        List<MutationMention> ms = sethExact.findMutations(mutationString);
        Assert.assertEquals(ms.size(), 1);
        Assert.assertEquals(ms.get(0).getType(), Type.DELETION_INSERTION);


    }

}
