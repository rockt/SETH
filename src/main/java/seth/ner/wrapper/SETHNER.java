package seth.ner.wrapper;

import scala.collection.Iterator;
import seth.Mutation;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Tim Rocktaeschel
 * Date: 11/9/12
 * Time: 2:15 PM
 */
public class SETHNER {
    seth.ner.SETHNER mutationExtractor = new seth.ner.SETHNER();

    public List<Mutation> extractMutations(String text) {
        List<Mutation> result = new ArrayList<Mutation>();
        Iterator<seth.ner.Mutation> iter = mutationExtractor.extractMutations(text).toIterator();
        while (iter.hasNext()) {
            seth.ner.Mutation mutation = iter.next();
            result.add(convert(mutation));
        }
        return result;
    }

    private Mutation convert(seth.ner.Mutation mutation) {
        return new Mutation(
                mutation.start(),
                mutation.end(),
                mutation.text(),
                mutation.ref(),
                mutation.loc(),
                mutation.wild(),
                mutation.mutated(),
                mutation.typ(),
                Mutation.Tool.SETH
        );
    }
}
