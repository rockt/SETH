package seth.wrapper;

import scala.collection.Iterator;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Tim Rocktaeschel
 * Date: 11/9/12
 * Time: 2:15 PM
 */
public class SETH {
    seth.SETH mutationExtractor = new seth.SETH();

    public List<Mutation> extractMutations(String text) {
        List<Mutation> result = new ArrayList<Mutation>();
        Iterator<seth.Mutation> iter = mutationExtractor.extractMutations(text).toIterator();
        while (iter.hasNext()) {
            seth.Mutation mutation = iter.next();
            result.add(convert(mutation));
        }
        return result;
    }

    private Mutation convert(seth.Mutation mutation) {
        return new Mutation(
                mutation.start(),
                mutation.end(),
                mutation.text(),
                mutation.ref(),
                mutation.loc(),
                mutation.wild(),
                mutation.mutated(),
                mutation.typ()
        );
    }
}
