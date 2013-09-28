package seth.ner.wrapper;

import de.hu.berlin.wbi.objects.MutationMention;
import edu.uchsc.ccp.nlp.ei.mutation.MutationException;
import seth.SETH;

import java.util.List;
/**
 * Minimal example to apply {@link SETHNER} (including MutationFinder) on free text
 * User: rockt
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
//    public static void main(String[] args) throws MutationException {
//
//        SETH seth = new SETH("resources/mutations.txt",false, true);
//
//        // (23605251,47,6633,6643,'482.xx-483','482.xx-483',NULL,'','','','','COPY_NUMBER_VARIATION',6489,6497,'Pneumonia','C0032285','HP:0006532',NULL)
//
//        List<MutationMention> mentions=
//                seth.findMutations("482.xx-483"); //R403stop Glu99X E590 K Tyr22Xc.861insG p.990delM
//
//        for(MutationMention mention : mentions){
//            System.out.println(mention);
//            System.out.println(mention.toNormalized());
//        }
//
//}
}