package seth.ner.wrapper;

import de.hu.berlin.wbi.objects.MutationMention;
import scala.collection.Iterator;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Tim Rocktaeschel
 * Date: 11/9/12
 * Time: 2:15 PM
 */
public class SETHNER {
    seth.ner.SETHNER mutationExtractor = new seth.ner.SETHNER();

    public List<MutationMention> extractMutations(String text) {
        List<MutationMention> result = new ArrayList<MutationMention>();
        Iterator<seth.ner.Mutation> iter = mutationExtractor.extractMutations(text).toIterator();
        while (iter.hasNext()) {
            seth.ner.Mutation mutation = iter.next();
            result.add(convert(mutation));
        }
        return result;
    }

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
