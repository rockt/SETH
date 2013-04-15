package seth.ner.wrapper;

import de.hu.berlin.wbi.objects.MutationMention;

import java.util.List;
/**
 * Minimal example to apply {@link SETHNER} (exluding MutationFinder) on free text
 * User: rockt
 * Date: 11/9/12
 * Time: 11:12 AM
 */

public class SETHNERApp {
    public static void main(String[] args) {
        System.out.println(args[0]);
        SETHNER seth = new SETHNER();
        List<MutationMention> result = seth.extractMutations(args[0]);
        for (MutationMention mutation : result) {
            System.out.println(mutation);
        }
        System.out.println("Extracted " + result.size() + " mutations.");
    }
}