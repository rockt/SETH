package seth;

import de.hu.berlin.wbi.objects.MutationMention;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Test;
import seth.ner.wrapper.Type;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;

public class SETHTest {

    @Test
    public void testFindMutations() throws Exception {
        SETH seth = new SETH(getClass().getResource("/resources/mutations.txt").getPath(), false, true);
        String sentence = "Causative GJB2 mutations were identified in 31 (15.2%) patients, and two common mutations, c.35delG and L90P (c.269T>C), accounted for 72.1% and 9.8% of GJB2 disease alleles.";
        List<MutationMention> mentions = seth.findMutations(sentence);

        assertEquals(mentions.size(), 3);

        // c.35delG and L90P (c.269T>C)
        Collections.sort(mentions, new Comparator<MutationMention>() {
            @Override
            public int compare(MutationMention o1, MutationMention o2) {
                return Integer.compare(o1.getStart(), o2.getStart());
            }
        });

        assertEquals(mentions.get(0).getText(), "c.35delG");
        assertEquals(mentions.get(1).getText(), "L90P");
        assertEquals(mentions.get(2).getText(), "c.269T>C");

        assertEquals(mentions.get(0).getType(), Type.DELETION);
        assertEquals(mentions.get(1).getType(), Type.SUBSTITUTION);
        assertEquals(mentions.get(2).getType(), Type.SUBSTITUTION);

        assertEquals(mentions.get(0).getWtResidue(), "G");
        assertEquals(mentions.get(1).getWtResidue(), "L");
        assertEquals(mentions.get(2).getWtResidue(), "T");

        assertNull(mentions.get(0).getMutResidue());
        assertEquals(mentions.get(1).getMutResidue(), "P");
        assertEquals(mentions.get(2).getMutResidue(), "C");

        assertEquals(mentions.get(0).getPosition(), "35");
        assertEquals(mentions.get(1).getPosition(), "90");
        assertEquals(mentions.get(2).getPosition(), "269");
    }
}
