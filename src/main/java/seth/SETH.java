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
 * This class encapsulates modified {@link MutationFinder}, {@link SETHNER}, {@link OldNomenclature}, and {@link dbSNPRecognizer}.
 * This allows to recognize SNPs written in the latest HGVS nomenclature (e.g. p.Ala123Tyr),
 * SNPs written in free text (e.g. Ala to Tyr substitution at position 123),
 * SNPs written in deprecated nomenclature
 * and dbSNP mentions
 *
 * @author Philippe Thomas
 */
public class SETH {


    /**
     * SETH object for recognizing nomenclature SNPs
     */
    private final SETHNER seth;

    /**
     * MutationFinder object for recognizing free-text Substitutions
     */
    private final MutationFinder mf;

    /**
     * Set of regular expressions for finding free text deletions, insertions, etc.
     */
    private final OldNomenclature bl;

    /**
     * Detects dbSNP mentions (e.g., rs334)
     */
    private final dbSNPRecognizer snpRecognizer;

    /**
     * Parameter free constructor for SETH. This constructor initializes SETH with reasonable default settings.
     * The constructor {@link SETH#SETH(String, boolean, boolean)} provides more flexibility for advanced configuration
     * MutationFinder is initialized using the regular expressions contained in Java-Archive 'resources/mutations.txt'
     * SETH-NER (HGVS) is initialized with fuzzy match-option
     */
    public SETH(){
        super();
        this.mf = new MutationFinder();
        this.seth = new SETHNER(false);
        this.snpRecognizer = new dbSNPRecognizer();
        this.bl = new OldNomenclature();
    }

    /**
     * Initializes {@link MutationFinder} and {@link SETHNER}.
     * Requires a file with regular expressions for MutationFinder
     *
     * @param regexFile       File location with regular expressions for MutationFinder
     * @param exactGrammar    If true, SETH uses the exact EBNF grammar, otherwise matching is fuzzy
     * @param oldNomenclature If true, SETH searches for deletions, insertions, frameshifts in old nomenclature
     */
    public SETH(String regexFile, boolean exactGrammar, boolean oldNomenclature) {
        super();

        this.mf = new MutationFinder(regexFile);
        this.seth = new SETHNER(exactGrammar);
        this.snpRecognizer = new dbSNPRecognizer();

        if (oldNomenclature)
            this.bl = new OldNomenclature();
        else
            this.bl = null;
    }

    /**
     * Initializes {@link MutationFinder} and {@link SETHNER}.
     * Requires a file with regular expressions for MutationFinder
     *
     * @param regexFile       File location with regular expressions for MutationFinder
     * @param exactGrammar    If true, SETH uses the exact EBNF grammar, otherwise matching is fuzzy
     * @param oldNomenclature If true, SETH searches for deletions, insertions, frameshifts in old nomenclature
     * @param backusNaur      If true, SETH uses not the backusNaur parser(not recommended); improves runtime, but SETH-performance drops considerably
     */
    public SETH(String regexFile, boolean exactGrammar, boolean oldNomenclature, boolean backusNaur) {
        super();

        this.mf = new MutationFinder(regexFile);
        this.snpRecognizer = new dbSNPRecognizer();

        if(backusNaur)
            this.seth = new SETHNER(exactGrammar);
        else
            this.seth = null;

        if (oldNomenclature)
            this.bl = new OldNomenclature();
        else
            this.bl = null;
    }

    /**
     * Searches for mutation mentions in applied text and returns them
     *
     * @param text Text possibly containing a mutation mention
     * @return List of {@link MutationMention}
     */
    public List<MutationMention> findMutations(String text) {
        Set<MutationMention> mutations = new HashSet<MutationMention>();

        //Extract variations following the latest HGVS nomenclature
        if(seth != null)
            mutations.addAll(seth.extractMutations(text));

        //Extract variations following dbSNP nomenclature (e.g. rs123A>T)
        if(snpRecognizer != null)
            mutations.addAll(snpRecognizer.extractMutations(text));

        //Extracts variations following different Nomenclature forms
        if (bl != null)
            mutations.addAll(bl.extractMutations(text));

        //Extract mutations, using a modified version of MutationFinder
        try {
            Map<edu.uchsc.ccp.nlp.ei.mutation.Mutation, Set<int[]>> map = mf.extractMutations(text);

            for (edu.uchsc.ccp.nlp.ei.mutation.Mutation mutation : map.keySet()) {
                PointMutation pm = (PointMutation) mutation;

                for (int[] location : map.get(mutation)) {
                    String originalMatch = text.substring(location[0], location[1]);

                    // MutationFinder only returns protein matches
                    MutationMention tmpMutation = new MutationMention(location[0], location[1], originalMatch, null, pm.getPosition(),
                            String.valueOf(pm.getWtResidue()), String.valueOf(pm.getMutResidue()),
                            Type.SUBSTITUTION, MutationMention.Tool.MUTATIONFINDER);
                    tmpMutation.setPatternId(pm.getId());
                    if (pm.isMatchesLongForm()) {
                        tmpMutation.setPsm(true);
                        tmpMutation.setAmbiguous(false);
                        tmpMutation.setNsm(false);
                    }

                    mutations.add(tmpMutation);
                }
            }

        } catch (MutationException e) {
            e.printStackTrace();
            System.exit(1);
        }


        //Post-processing:
        //Some mentions can be found be different tools. Thus, we remove all duplicates by preserving the longest element
        //E.g., IVS2 + 1G>A is recognized  Mutationfinder (2+1G>A) and correctly Tool.Regex
        List<MutationMention> result = new ArrayList<MutationMention>(mutations.size());
        for (MutationMention mm : mutations) {

            boolean contained = false;
            loop:for (MutationMention m : result) {

                //This variable is needed to check  whether two mutations are identical. (Important as MF finds overlapping ,mutations Trp-64 to Phe or Tyr)
                boolean equal = false;

                //Debugging purpose
                //System.out.println("'" + mm.getPosition() + "' -- '" + m.getPosition() + "'");
                //System.out.println("'" + mm.getMutResidue() + "' -- '" + m.getMutResidue() + "'");
                //System.out.println("'" + mm.getWtResidue() + "' -- '" + m.getWtResidue() + "'");


                //This potentially lead to problematic null pointer exceptions
                //equal = mm.getPosition().equals(m.getPosition()) && mm.getMutResidue().equals(m.getMutResidue()) && mm.getWtResidue().equals(m.getWtResidue());


                //Better handling of potentially null values
                equal =  Objects.equals(mm.getPosition(), m.getPosition()) && Objects.equals(mm.getMutResidue(), m.getMutResidue()) && Objects.equals(mm.getWtResidue(), m.getWtResidue());

                //In case the two recognized mutation mentions are equally long Tool.DBSNP wins over Tool.SETH  (both tools find mentions like rs123:A>T)
                if (mm.getStart() == m.getStart() && mm.getEnd() == m.getEnd()) {

                    if (mm.getTool() == MutationMention.Tool.SETH && m.getTool() == MutationMention.Tool.DBSNP) {
                        contained = true;
                        break loop;
                    } else if (m.getTool() == MutationMention.Tool.SETH && mm.getTool() == MutationMention.Tool.DBSNP) {
                        result.remove(m);
                        break loop;
                    }
                    //else we do return two mutations!!
                }

                //If the new mention is smaller than the mention contained in the result, ignore the smaller one
                else if (mm.getStart() >= m.getStart() && mm.getEnd() <= m.getEnd() && equal) {
                    contained = true;
                    break loop;
                }

                //If the new mention is longer, remove the old (smaller) mention and add the new one
                else if (m.getStart() >= mm.getStart() && m.getEnd() <= mm.getEnd() && equal) {
                    result.remove(m);
                    break loop;
                }
            }

            if (!contained)
                result.add(mm);

        }

        return result;
    }


    /**
     * Minimal example to perform named entity recognition and normalization using SETH
     *
     * @param args
     * @throws SQLException
     * @throws IOException
     */
    public static void main(String[] args) throws SQLException, IOException {
        String text = "p.A123T and Val158Met";

        /** Part1: Recognition of mutation mentions */
        SETH seth = new SETH("resources/mutations.txt", true, true);
        //SETH seth = new SETH();
        List<MutationMention> mutations = seth.findMutations(text);
        try {
            for (MutationMention mutation : mutations) {
                System.out.println(mutation.toNormalized());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }


        /** Part 2: Normalization of mutation mentions to dbSNP
         * This part communicates with a local database dump
         */
        final Properties property = new Properties();
        property.loadFromXML(new FileInputStream(new File("myProperty.xml"))); //Load property file
        final DatabaseConnection dbConnection = new DatabaseConnection(property);

        //Connect with local derby database and initialize prepared statements
        dbConnection.connect();
        dbSNP.init(dbConnection, property.getProperty("database.PSM"), property.getProperty("database.hgvs_view"));
        UniprotFeature.init(dbConnection, property.getProperty("database.uniprot"));
        Gene.init(dbConnection, property.getProperty("database.geneTable"), property.getProperty("gene2Pubmed"));

        /**
         * For mutation normalization, SETH requires target gene(s) to normalize the mutation
         * For simpler use, SETH ships with a set of precomputed NER results from GNAT and gene2Pubmed
         * but in practise an arbitrary gene name recognition tool can be used. As long it normalizes to Entrez-Gene IDs
         */
        //Set<Gene> recognizedGenes = Gene.queryGenesForArticle(1572656); //Retrieve all genes recognized by GNAT and gene2pubmed
        Set<Integer> genes = new HashSet<Integer>(Collections.singletonList(1312));  //Manually define a list of Entrez Gene IDs, against which we want to compare; Alternatively you can include a custom gene-NER here

        for(int gene : genes){
            final List<dbSNP> potentialSNPs = dbSNP.getSNP(gene);    //Get a list of dbSNPs which could potentially represent the mutation mention
            final List<UniprotFeature> features = UniprotFeature.getFeatures(gene);    //Get all associated UniProt features

            for (MutationMention mutation : mutations) {
                System.out.println(mutation);
                mutation.normalizeSNP(potentialSNPs, features, false);
                List<dbSNPNormalized> normalized = mutation.getNormalized();    //Get list of all dbSNP entries with which I could successfully associate the mutation

                // Print information
                for (dbSNPNormalized snp : normalized) {
                    System.out.println(mutation + " --- rs" + snp.getRsID());
                }
            }
        }



    }
}
