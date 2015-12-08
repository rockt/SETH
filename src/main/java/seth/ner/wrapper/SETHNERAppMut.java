package seth.ner.wrapper;

import de.hu.berlin.wbi.objects.MutationMention;
import seth.SETH;

import java.util.List;
/**
 * Minimal example to apply {@link SETHNER} (including MutationFinder) on free text
 * @author rockt
 * Date: 11/9/12
 * Time: 11:12 AM
 */

public class SETHNERAppMut {
    public static void main(String[] args) {
        System.out.println(args[0]);
        SETH seth = new SETH("resources/mutations.txt", true, true);

        List<MutationMention> result = seth.findMutations(args[0]);
        for (MutationMention mutation : result) {
            System.out.println(mutation);
        }
        System.out.println("Extracted " + result.size() + " mutations.");
    }
}