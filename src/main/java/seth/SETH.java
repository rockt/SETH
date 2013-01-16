package seth;

import de.hu.berlin.wbi.objects.*;
import edu.uchsc.ccp.nlp.ei.mutation.MutationException;
import edu.uchsc.ccp.nlp.ei.mutation.MutationFinder;
import edu.uchsc.ccp.nlp.ei.mutation.PointMutation;
import seth.ner.wrapper.SETHNER;
import seth.ner.wrapper.Type;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * This class encapsulates {@link MutationFinder} and {@link SETHNER}.
 * This allows to recognize SNPs written in the latest HGVS nomenclature (e.g. p.Ala123Tyr)
 * and SNPs written in free text (e.g. Ala to Tyr substitution at position 123)
 */
public class SETH {


    /** SETH object for recognizing nomenclature SNPs*/
    private final SETHNER seth;

    /** MutationFinder object for recognizing free-text SNPs*/
    private final MutationFinder mf;

    /**
     * Initializes {@link MutationFinder} and {@link SETHNER}.
     * Requires a file with regular expressions for MutationFinder
     *
     * @param regexFile File location with regular expressions for MutationFinder
     */
    public SETH(String regexFile) {
        super();

        this.mf = new MutationFinder(regexFile);
        this.seth = new SETHNER();
    }

    /**
     * Searches for mutation mentions in applied text and returns them
     * @param text Text possibly containing a mutation mention
     * @return List of {@link MutationMention}
     */
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

                   MutationMention tmpMutation =    new MutationMention(location[0], location[1], originalMatch, "NA", pm.getPosition(),
                           String.valueOf(pm.getWtResidue()), String.valueOf(pm.getMutResidue()),
                           Type.SUBSTITUTION, MutationMention.Tool.MUTATIONFINDER);
                   tmpMutation.setPatternId(pm.getId());
                   mutations.add(tmpMutation);
               }
           }

        } catch (MutationException e) {
            e.printStackTrace();
            System.exit(1);
        }

        return mutations;
    }

    //Minimal example to apply both tools
    public static void main(String[] args)throws SQLException, IOException {
        String text = "p.A123T and Val158Met";

        /** Part1: Recognition of mutation mentions */
        SETH seth = new SETH("resources/mutations.txt");
        List<MutationMention> mutations = seth.findMutations(text);
        try{
            for(MutationMention mutation : mutations){
                System.out.println(mutation.toNormalized());
            }
        }
       catch(Throwable e){
              e.printStackTrace();
       }


       /** Part 2: Normalization of mutation mentions to dbSNP
        * This part communicates with a local database dump
       */
        final Properties property = new Properties();
        property.loadFromXML(new FileInputStream(new File("myProperty.xml"))); //Load property file
        final DatabaseConnection mysql = new DatabaseConnection(property);

        mysql.connect(); //Connect with local derby Database
        dbSNP.init(mysql, property.getProperty("database.PSM"), property.getProperty("database.hgvs_view"));
        UniprotFeature.init(mysql, property.getProperty("database.uniprot"));

        int gene = 1312;	//Entrez Gene ID associated with the current sentence
        final List<dbSNP> potentialSNPs = dbSNP.getSNP(gene);	//Get a list of dbSNPs which could potentially represent the mutation mention
        final List<UniprotFeature> features = UniprotFeature.getFeatures(gene);    //Get all associated UniProt features

        for(MutationMention mutation : mutations){
            mutation.normalizeSNP(potentialSNPs, features, false);
            List<dbSNPNormalized> normalized = mutation.getNormalized();	//Get list of all dbSNP entries with which I could successfully associate the mutation

            // Print information
            for(dbSNPNormalized snp : normalized){
                System.out.println(mutation +" --- rs" +snp.getRsID());
            }
        }



    }
}
