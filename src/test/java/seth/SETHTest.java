package seth;

import de.hu.berlin.wbi.objects.MutationMention;
import junit.framework.Assert;
import org.junit.Test;
import seth.ner.wrapper.Type;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SETHTest {

    @Test
    public void testFindMutations() throws Exception {
        SETH seth = new SETH(getClass().getResource("/resources/mutations.txt").getPath(), false, "resources/patterns.txt");
        String sentence = "Causative GJB2 mutations were identified in 31 (15.2%) patients, and two common mutations, c.35delG and L90P (c.269T>C), accounted for 72.1% and 9.8% of GJB2 disease alleles.";
        List<MutationMention> mentions = seth.findMutations(sentence);

        Assert.assertEquals(mentions.size(), 3);

        // c.35delG and L90P (c.269T>C)
        mentions.sort(new Comparator<MutationMention>() {
            @Override
            public int compare(MutationMention o1, MutationMention o2) {
                return Integer.compare(o1.getStart(), o2.getStart());
            }
        });

        Assert.assertEquals(mentions.get(0).getText(), "c.35delG");
        Assert.assertEquals(mentions.get(1).getText(), "L90P");
        Assert.assertEquals(mentions.get(2).getText(), "c.269T>C");

        Assert.assertEquals(mentions.get(0).getType(), Type.DELETION);
        Assert.assertEquals(mentions.get(1).getType(), Type.SUBSTITUTION);
        Assert.assertEquals(mentions.get(2).getType(), Type.SUBSTITUTION);

        Assert.assertEquals(mentions.get(0).getWtResidue(), "G");
        Assert.assertEquals(mentions.get(1).getWtResidue(), "L");
        Assert.assertEquals(mentions.get(2).getWtResidue(), "T");

        Assert.assertEquals(mentions.get(0).getMutResidue(), null);
        Assert.assertEquals(mentions.get(1).getMutResidue(), "P");
        Assert.assertEquals(mentions.get(2).getMutResidue(), "C");

        Assert.assertEquals(mentions.get(0).getPosition(), "35");
        Assert.assertEquals(mentions.get(1).getPosition(), "90");
        Assert.assertEquals(mentions.get(2).getPosition(), "269");
    }
}
