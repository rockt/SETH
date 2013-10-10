package cnveth;
// this class allows to detect possible mentions of copy number variations

import de.hu.berlin.wbi.objects.MutationMention;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class CNVETH {

    // CNVETH object to detect possible mentions of CNV keywords
    private final CNVRecognizer cnveth;

    public CNVETH(){

        this.cnveth = new CNVRecognizer();

    }

    public Set<MutationMention> findMutations(String text){
        Set<MutationMention> mutations = new HashSet<MutationMention>();
        //Extract mentions of CNV keywords
        mutations.addAll(cnveth.extractMutations(text));

        return mutations;
    }

    public static void main(String[] args)throws SQLException, IOException {
        String text = "CNV of geneX,copy number Variation in geneX. Amplifications of (z)! And increased copy number on region V";

        CNVETH cnveth = new CNVETH();
        Set<MutationMention> mutations = cnveth.findMutations(text);

//        System.out.println("text: " + text);
        System.out.println("number of mutations: " + mutations.size());
        System.out.println("mutations: " + mutations);

    }
}
