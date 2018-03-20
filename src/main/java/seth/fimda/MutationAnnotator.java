package seth.fimda;

import de.hu.berlin.wbi.objects.MutationMention;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import seth.SETH;

import java.util.List;

public class MutationAnnotator extends JCasAnnotator_ImplBase {


    public void process(JCas aJCas) {

        // The JCas object is the data object inside UIMA where all the
        // information is stored. It contains all annotations created by
        // previous annotators, and the document text to be analyzed.

        // get document text from JCas
        String docText = aJCas.getDocumentText();

        SETH seth = new SETH("resources/mutations.txt", true, true);
        //SETH seth = new SETH();
        List<MutationMention> mutations = seth.findMutations(docText);
        try {
            for (MutationMention mutation : mutations) {
                //System.out.println(mutation.toNormalized());

                // match found - create the match as annotation in
                // the JCas with some additional meta information
                // TODO: expand this!
                MutationAnnotation annotation = new MutationAnnotation(aJCas);
                annotation.setBegin(mutation.getStart());
                annotation.setEnd(mutation.getEnd());
                annotation.setPosition(mutation.getPosition());
                annotation.setMtResidue(mutation.getMutResidue());
                annotation.setWtResidue(mutation.getWtResidue());
                annotation.setMtType(mutation.getType().toString());

                annotation.addToIndexes();

            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
