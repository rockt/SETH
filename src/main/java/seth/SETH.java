package seth;

import de.hu.berlin.wbi.objects.MutationMention;
import edu.uchsc.ccp.nlp.ei.mutation.MutationException;
import edu.uchsc.ccp.nlp.ei.mutation.MutationFinder;
import edu.uchsc.ccp.nlp.ei.mutation.PointMutation;
import seth.ner.wrapper.SETHNER;
import seth.ner.wrapper.Type;

import java.util.*;

//S.E.T.H. — SNP Extraction Tool for Human Variations
public class SETH {

    private SETHNER seth;
    private MutationFinder mf;

    public SETH(String regexFile) {
        super();

        this.mf = new MutationFinder(regexFile);
        this.seth = new SETHNER();
    }

    public List<MutationMention> findMutations(String text){
        List<MutationMention> mutations = new ArrayList<MutationMention>();

        //Extract variations following the latest HGVS nomenclature
        mutations.addAll(seth.extractMutations(text));

        //Extract mutations, using a modified version of MutationFinder
       try {
            Map<edu.uchsc.ccp.nlp.ei.mutation.Mutation, Set<int[]>> map = mf.extractMutations(text);

           for(edu.uchsc.ccp.nlp.ei.mutation.Mutation mutation : map.keySet()){
               PointMutation pm = (PointMutation) mutation;

               for(int [] location : map.get(mutation)){
                   String originalMatch =  text.substring(location[0], location[1]);

                   mutations.add(new MutationMention(location[0], location[1], originalMatch, "NA", pm.getPosition(),
                           String.valueOf(pm.getWtResidue()), String.valueOf(pm.getMutResidue()),
                           Type.SUBSTITUTION, MutationMention.Tool.MUTATIONFINDER));
               }
           }

        } catch (MutationException e) {
            e.printStackTrace();
            System.exit(1);
        }

        return mutations;
    }

    public static void main(String[] args){
        String text = "p.A123T and Ala123Tyr";
        SETH seth = new SETH("resources/mutations.txt");

        for(MutationMention mutation : seth.findMutations(text)){
            System.out.println("'" +text.subSequence(mutation.getStart(), mutation.getEnd()) +"'");
        }

    }
}
