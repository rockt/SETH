package de.hu.berlin.wbi.issues;


import de.hu.berlin.wbi.objects.MutationMention;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import seth.ner.wrapper.SETHNER;
import seth.ner.wrapper.Type;

import java.util.List;

/**
 * Created by philippe on 11/2/16.
 * Test cases for bugs reported in issue7 https://github.com/rockt/SETH/issues/7
 */
public class Issue7Test {


    @Test
    public void testIssue7Inexact() throws Exception {
        SETHNER sethner = new SETHNER(false);
        List<MutationMention> result = sethner.extractMutations("p.F55>L");
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(result.get(0).getType(), Type.SUBSTITUTION);
    }

    @Test
    public void testIssue7Exact() throws Exception {
        SETHNER sethner = new SETHNER(true);
        List<MutationMention> result = sethner.extractMutations("p.F55>L");
        Assert.assertEquals(0, result.size());
    }

}
