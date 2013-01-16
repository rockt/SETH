package seth.ner.wrapper;

import de.hu.berlin.wbi.objects.MutationMention;
import scala.collection.Iterator;

import java.util.ArrayList;
import java.util.List;

/**
 * Java wrapper for actual {@link seth.ner.SETHNER} code written in Scala
 */
public class SETHNER {
    private final seth.ner.SETHNER mutationExtractor = new seth.ner.SETHNER();

    /**
     * Extracts mentions of mutations written in the HGVS nomenclature from natural language text
     * @param text Input natural language text
     * @return A list of {@link MutationMention} objects
     */
    public List<MutationMention> extractMutations(String text) {
        List<MutationMention> result = new ArrayList<MutationMention>();
        Iterator<seth.ner.Mutation> iter = mutationExtractor.extractMutations(text).toIterator();
        while (iter.hasNext()) {
            seth.ner.Mutation mutation = iter.next();
            result.add(convert(mutation));
        }
        return result;
    }

    /**
     * Converts a scala {@link seth.ner.Mutation} object to a java {@link MutationMention}
     * @param mutation A scala {@link seth.ner.Mutation} object
     * @return The corresponding java {@link MutationMention} object
     */
    private MutationMention convert(seth.ner.Mutation mutation) {
        return new MutationMention(
                mutation.start(),
                mutation.end(),
                mutation.text(),
                mutation.ref(),
                mutation.loc(),
                mutation.wild(),
                mutation.mutated(),
                mutation.typ(),
                MutationMention.Tool.SETH
        );
    }
}
