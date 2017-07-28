package cnveth;


import de.hu.berlin.wbi.objects.MutationMention;
import java.util.*;

/**
 * this class allows to detect possible mentions of copy number variations
 */
class CNVETH {

    /**
     * CNVETH object to detect possible mentions of CNV keywords
     */
    private final CNVRecognizer cnveth;

    public CNVETH(){
        this.cnveth = new CNVRecognizer();
    }

    /**
     * Extract mentions of CNV keywords and assigns Type CNV
     * @param text Text to find CNV's
     * @return detected CNV's
     */
    public Set<MutationMention> findMutations(String text){
        HashSet<MutationMention> mutations = new HashSet<>();

        mutations.addAll(cnveth.extractMutations(text));
        return mutations;
    }

    public static void main(String[] args){
        String text = "CNV of geneX,copy number Variation in geneX. Amplifications of z! And increased copy number on region V copy number gains of gene x and copy number losses on w";

        CNVETH cnveth = new CNVETH();
        Set<MutationMention> mutations = cnveth.findMutations(text);

//        System.out.println("text: " + text);
        System.out.println("number of mutations: " + mutations.size());
        System.out.println("mutations: " + mutations);

    }
}
