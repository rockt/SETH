package de.hu.berlin.wbi.objects;

import edu.uchsc.ccp.nlp.ei.mutation.MutationExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import seth.ner.wrapper.Type;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Class to represent a mutation mention found by an NER tool like
 * MutationFinder or SETH
 *
 * @author Philippe Thomas
 *
 */
public class MutationMention implements Comparable<MutationMention>{

    private final static Logger logger = LoggerFactory.getLogger(MutationMention.class);

    /** Type of Mutation (e.g., substitution, insertion, ...) */
    protected Type type;

    /** Refers to which tool has been used for extraction (e.g., MutationFinder, SETH, ...) */
    protected Tool tool;

    /** Enumeration of all named entity recognition components */
    public enum Tool{
        /** Mutations found by MutationFinder */
        MUTATIONFINDER,
        /** Mutations found by SETH-NER (Backus Naur grammar)*/
        SETH,
        /** Mutations found by our regular expressions for deprecated nomenclatures */
        REGEX,
        /** Mutations detected by the dbSNP recognition component*/
        DBSNP,
        /** Mutations found by the CNVRecognizer class*/
        CNVETH,
        /** Fallack for future tools*/
        UNKNOWN
    }

    /** Location in the text. */
    protected EntityOffset location;

    /** Text mention in the text. */
    protected String text;

    /** Used reference sequence (e.g. c., g., ...) */
    protected String ref;

    /** Wildtype residue. (can be null)*/
    protected String wtResidue;

    /** Mutated residue. (can be null)*/
    protected String mutResidue;

    /** Location of the mutation wrt residue or nucleotides*/
    protected String position;

    /** Flag indicating if this SNP is a nucleotide sequence mutation (NSM). */
    protected boolean nsm;

    /** Flag indicating if this SNP is a protein sequence mutation (PSM). */
    protected boolean psm;

    /** Flag indicating if this SNP is ambiguous.
     *  I.e., it can be a PSM or NSM*/
    protected boolean ambiguous;

    /** Which regular expression has been used to extract this SNP from text (MutationFinder only)*/
    protected int patternId;

    /** Pattern is used to extract wildtype-location-residue from a string e.g. A123T  */
    private static final Pattern pattern = Pattern.compile("^([A-Z])([\\-\\+\\*]?[1-9][0-9]*[\\-\\+]?[0-9]*)([A-Z])$");

    /** Contains all ambigious characters used for nucleotides as well as amino acids */
    private static final ArrayList<String> atgc = new ArrayList<String>(Arrays.asList("A", "T", "G", "C"));

    /** List with normalized SNPs*/
    protected List<dbSNPNormalized> normalized;

    /** Set of transcripts with which we successfully associated this Element*/
    protected Set<TranscriptNormalized> transcripts;


    /**
     * This method evaluates if some of the {@link Transcript} provided in <b>candidateTranscripts</b>
     * equals the text-mined {@link MutationMention}. In difference to {@link #normalizeSNP(java.util.List, java.util.List, boolean)}
     * this method tries to normalize to sequence-ID and not to dbSNP-IDs
     *
     * @param candidateTranscripts List of dbSNP-entries which might actually describe this SNP
     * @param features List of associated uniprot features
     * @param append Sometimes normalization has to be invoked several times (for several genes) Flag indicates if the result is appended or not
     *
     *
     *
     */
    public void normalizeSequences(Set<Transcript> candidateTranscripts, List<UniprotFeature> features, boolean append){

        if(append == false || transcripts == null)
            transcripts = new HashSet<TranscriptNormalized>();

        for(Transcript transcript : candidateTranscripts){

            try{
                int loc = Integer.parseInt(this.position) -1;
//                System.out.println("loc '" +loc +"'");
//                System.out.println("wt '" +this.wtResidue+"'");
//                System.out.println("mut '" +transcript.getProtein_sequence().charAt(loc) +"'");
//                System.out.println(this.wtResidue.charAt(0) == transcript.getProtein_sequence().charAt(loc));
//                  System.out.println(this.wtResidue.equals(String.valueOf(transcript.getProtein_sequence().charAt(loc))));
//                System.out.println(transcript.getUniprot());
//                System.out.println("-----------------");


                if(this.wtResidue.equals(String.valueOf(transcript.getProtein_sequence().charAt(loc))))  {
                    EnumSet<MatchOptions> match = EnumSet.of(MatchOptions.PSM, MatchOptions.LOC);
                    transcripts.add(new TranscriptNormalized(transcript, match, null));
                }
                else if(this.wtResidue.equals(String.valueOf(transcript.getProtein_sequence().charAt(loc+1)))){
                    EnumSet<MatchOptions> match = EnumSet.of(MatchOptions.PSM, MatchOptions.METHIONE);
                    transcripts.add(new TranscriptNormalized(transcript, match, null));
                }

                if(this.wtResidue.equals(String.valueOf(transcript.getCDC_sequence().charAt(loc))))  {
                    EnumSet<MatchOptions> match = EnumSet.of(MatchOptions.LOC);
                    transcripts.add(new TranscriptNormalized(transcript, match, null));
                }

                loop:for(UniprotFeature feature : features){
                    if(feature.getGeneId() != transcript.getEntrez())
                        continue;

                    if(this.wtResidue.equals(String.valueOf(transcript.getProtein_sequence().charAt(loc- feature.getEndLoc() +feature.getStartLoc() -1))))  {
                        EnumSet<MatchOptions> match = EnumSet.of(MatchOptions.PSM);
                        transcripts.add(new TranscriptNormalized(transcript, match, feature));
                        break loop;
                    }
                }
            }
            catch(Exception e){ //Several Exceptions can happen (e.g. NumberFormatException, StringIndexOutOfBounds...
                logger.debug("Problem normalizing sequence",e);
                continue;
            }
        }
    }

    /**
     * Method allows us to "manually" normalize a mutation mention to a dbSNP ID
     * Method is currently not invoked, but might be called for the dbSNPRecognized
     * @param id  dbSNP ID we want to normalize to
     */
    public void normalizeSNP(int id){
        normalized = new ArrayList<dbSNPNormalized>(1);

        dbSNP snp = new dbSNP();
        snp.setRsID(id);
        EnumSet<MatchOptions> match = EnumSet.of(MatchOptions.LOC);
        normalized.add(new dbSNPNormalized(snp, match, null));
    }

    /**
     * This method evaluates if some of the {@link de.hu.berlin.wbi.objects.dbSNP} provided in <b>candidates</b>
     * actually equals the text-mined {@link MutationMention}
     *
     * @param candidates List of dbSNP-entries which might actually describe this SNP
     * @param features List of associated uniprot features
     * @param append Sometimes normalization has to be invoked several times (for several genes) Should we append result or not?
     *
     */
    public void normalizeSNP(List<dbSNP> candidates, List<UniprotFeature> features, boolean append) {

        if(append == false || normalized == null)
           normalized = new ArrayList<dbSNPNormalized>(1);

        //Check if we have sufficient information to normalize the current mutation
        if (wtResidue == null || mutResidue == null || position == null)
            return;

        //Iterate over all possible Candidates and try to normalize this specific mention
        for (dbSNP candidate : candidates) {

            //Normalize candidate using information in 'hgvs' table
            if(candidate.getHgvs() != null){

                //Normalize PSM using the 'hgvs' information from dbSNP..
                if(normalizePSMSimpleHGVS(candidate)) {
                    EnumSet<MatchOptions> match = EnumSet.of(
                        MatchOptions.LOC, MatchOptions.PSM
                    );                    
                    normalized.add(new dbSNPNormalized(candidate, match, null));
                }

                if(normalizePSMSimpleHGVSSwap(candidate)) {
                    EnumSet<MatchOptions> match = EnumSet.of(
                       MatchOptions.LOC, MatchOptions.PSM, MatchOptions.SWAPPED
                    );
                    normalized.add(new dbSNPNormalized(candidate, match, null));
                }

                if(normalizePSMMethionineHGVS(candidate)) {
                    EnumSet<MatchOptions> match = EnumSet.of(
                        MatchOptions.METHIONE, MatchOptions.PSM);
                    normalized.add(new dbSNPNormalized(candidate, match, null));
                }

                if(normalizePSMMethionineSwapHGVS(candidate)) {                    
                    EnumSet<MatchOptions> match = EnumSet.of(MatchOptions.METHIONE, MatchOptions.PSM, MatchOptions.SWAPPED);
                    normalized.add(new dbSNPNormalized(candidate, match, null));
                }

                //Now we try to normalize to DNA using HGVS information
                if (forwardNSMSimple(candidate)) {
                    EnumSet<MatchOptions> match = EnumSet.of(MatchOptions.LOC);
                    normalized.add(new dbSNPNormalized(candidate, match, null));
                } else if (reverseNSMSimple(candidate)) {
                    EnumSet<MatchOptions> match = EnumSet.of(
                        MatchOptions.LOC, MatchOptions.SWAPPED
                    );
                    normalized.add(new dbSNPNormalized(candidate, match, null));
                }
            }

            //Normalize candidate using information in 'PSM' table
            if(candidate.getResidues() != null){

                boolean forward= checkPSM_WildType(candidate, true) && checkPSM_Mutated(candidate, true) ;
                boolean reverse= checkPSM_WildType(candidate, false) && checkPSM_Mutated(candidate, false) ;

                if (forward || reverse){

                    if (normalizePSMSimple(candidate) ) { //Exact match?
                        EnumSet<MatchOptions> match = EnumSet.of(
                            MatchOptions.LOC, MatchOptions.PSM
                        );
                        if (!forward) {
                            match.add(MatchOptions.SWAPPED);
                        }
                        normalized.add(new dbSNPNormalized(candidate, match, null));
                    }

                    if(normalizePSMMethionine(candidate)) { //Methionine Offset of 1?
                        EnumSet<MatchOptions> match = EnumSet.of(
                            MatchOptions.METHIONE, MatchOptions.PSM
                        );
                        if (!forward) {
                            match.add(MatchOptions.SWAPPED);
                        }
                        normalized.add(new dbSNPNormalized(candidate, match, null));
                    }

                    UniprotFeature feature = normalizePSMVariableOffset(candidate, features);	//match using UniProt features
                    if(feature != null) {
                        EnumSet<MatchOptions> match = EnumSet.of(MatchOptions.PSM);
                        if (!forward) {
                            match.add(MatchOptions.SWAPPED);
                        }
                        normalized.add(new dbSNPNormalized(candidate, match, feature));
                    }
                }
            }
        }

        normalized = cleanResults(normalized);
    }

    /**
     * Sometimes one SNP mention can be normalized to the same dbSNP identifier
     * several times. As some information is stored redundant.
     * Therefore we have to remove some duplicates from the list and
     * also we return only the results with highest score (no impact for evaluation)
     * @param snpList   List of SNPs to be cleansed
     * @return Cleansed SNP list
     */
    private static List<dbSNPNormalized> cleanResults(List<dbSNPNormalized> snpList) {

        //We only return those dbSNP entries with highest confidence (actually no impact is visible)
        Collections.sort(snpList);
        int value = snpList.size() > 0 ? snpList.get(0).getConfidence() : Integer.MIN_VALUE;

        List<dbSNPNormalized> topResult = new ArrayList<dbSNPNormalized>();
        Set<Integer> seen = new HashSet<Integer>();
        for(dbSNPNormalized snp : snpList){
            if(value == snp.getConfidence() && !seen.contains(snp.getRsID())){
                topResult.add(snp);
                seen.add(snp.getRsID());
            }
        }

        return topResult;
    }


    /**
     * Test if the candidate has the same wildtype residue
     */
    private boolean checkPSM_WildType(dbSNP candidate, boolean forward){

        if(forward)
            return candidate.getWildtype().equals(wtResidue);

        return candidate.getWildtype().equals(mutResidue);
    }

    /**
     *Test if the candidate has the same mutated residue
     */
    private boolean checkPSM_Mutated(dbSNP candidate, boolean forward){
        if(forward)
            return candidate.getResidues().contains(mutResidue);

        return candidate.getResidues().contains(wtResidue);
    }

    /**
     * Check if the {@link MutationMention} mention can be exactly normalized to a PSM-candidate
     *
     * @param candidate  dbSNP candidate
     * @return true if the mutation mention can be normalized to a PSM-candidate
     */
    private boolean normalizePSMSimple(dbSNP candidate) {

        return Integer.toString(candidate.getAaPosition()).equals(position);
    }


    /**
     * Check if the {@link MutationMention} mention can be normalized to a PSM-candidate with an offset of "one" (Leading methione was not used)
     *
     * @param candidate    dbSNP candidate
     * @return true if the mutation mention can be normalized to a PSM-candidate with a +/-1 offset
     */
    private boolean normalizePSMMethionine(dbSNP candidate) {

        return (Integer.toString(candidate.getAaPosition() - 1).equals(position) ||
                Integer.toString(candidate.getAaPosition() + 1).equals(position) );
    }

    /**
     * Check if the {@link MutationMention} mention can be normalized to a PSM-candidate with a variable offset, derived from UniProt
     *
     * @param candidate     dbSNP candidate
     * @param features List of possible UniProt features
     * @return null if candidate can not be normalized, or the feature which was used to normalize the candidate
     */
    private UniprotFeature normalizePSMVariableOffset(dbSNP candidate, List<UniprotFeature> features) {


        for(UniprotFeature feature : features){
            if(feature.getGeneId() != candidate.getGeneID())
                continue;

            if(Integer.toString(candidate.getAaPosition() - feature.getEndLoc() +feature.getStartLoc() -1).equals(position))
                return feature;
        }

        return null;
    }

    /**
     * Check if the {@link MutationMention} mention can be exactly normalized to a PSM-candidate
     * In difference to normalizePSMSimple, this method uses information from the associated HGVS objects
     * This makes a big difference as both information sources (XML and HGVS) are complementary
     *
     * @param candidate   dbSNP candidate
     * @return true if the mutation mention can be normalized to a PSM-candidate
     */
    private boolean normalizePSMSimpleHGVS(dbSNP candidate){

        for(HGVS hgvs : candidate.getHgvs()){
            if (hgvs.getMutation() == null || hgvs.getWildtype() == null || hgvs.getLocation() == null)
                continue;

            if(hgvs.getType() != 'p')
                continue;

            if (hgvs.getLocation().equals(position) == false)
                continue;

            if (wtResidue.equals(hgvs.getWildtype()) == true && mutResidue.equals(hgvs.getMutation()))
                return true;
        }
        return false;
    }


    /**
     * Check if the {@link MutationMention} mention can be exactly normalized to a PSM-candidate (reverse allele order)
     * In difference to normalizePSMSimple, this method uses information from the associated HGVS objects
     * This makes a big difference as both information sources (XML and HGVS) are complementary
     *
     * @param candidate   dbSNP candidate
     * @return true if the mutation mention can be normalized to a PSM-candidate
     */
    private boolean normalizePSMSimpleHGVSSwap(dbSNP candidate){

        for(HGVS hgvs : candidate.getHgvs()){
            if (hgvs.getMutation() == null || hgvs.getWildtype() == null || hgvs.getLocation() == null)
                continue;

            if(hgvs.getType() != 'p')
                continue;

            if (hgvs.getLocation().equals(position) == false)
                continue;

            if (wtResidue.equals(hgvs.getMutation()) == true && mutResidue.equals(hgvs.getWildtype()))
                return true;
        }
        return false;
    }

    /**
     * Check if the {@link MutationMention} mention can be normalized to a PSM-candidate with an offset of 1
     * In difference to normalizePSMSimple, this method uses information from the associated HGVS objects
     *
     * @param candidate    dbSNP candidate
     * @return true if the mutation mention can be normalized to a PSM-candidate with a +/-1 offset
     */
    private boolean normalizePSMMethionineHGVS(dbSNP candidate) {

        int loc =-1;
        try{
            loc = Integer.parseInt(position);
        }catch(NumberFormatException nfe){
            return false;
        }

        for(HGVS hgvs : candidate.getHgvs()){
            if (hgvs.getMutation() == null || hgvs.getWildtype() == null || hgvs.getLocation() == null)
                continue;

            if(hgvs.getType() != 'p')
                continue;

            if (wtResidue.equals(hgvs.getWildtype()) == false || mutResidue.equals(hgvs.getMutation()) == false)
                continue;

            if(Integer.toString(loc - 1).equals(hgvs.getLocation()) == false && Integer.toString(loc + 1).equals(hgvs.getLocation()) == false)
                continue;

            return true; // if we come that far everything is fine
        }

        return false;
    }

    /**
     * Check if the {@link MutationMention} mention can be normalized to a PSM-candidate with an offset of 1
     * In difference to normalizePSMSimple, this method uses information from the associated HGVS objects
     *
     * @param candidate    dbSNP candidate
     * @return true if the mutation mention can be normalized to a PSM-candidate with a +/-1 offset
     */
    private boolean normalizePSMMethionineSwapHGVS(dbSNP candidate) {

        int loc =-1;
        try{
            loc = Integer.parseInt(position);
        }catch(NumberFormatException nfe){
            return false;
        }

        for(HGVS hgvs : candidate.getHgvs()){
            if (hgvs.getMutation() == null || hgvs.getWildtype() == null || hgvs.getLocation() == null)
                continue;

            if(hgvs.getType() != 'p')
                continue;

            if (wtResidue.equals(hgvs.getMutation()) == false || mutResidue.equals(hgvs.getWildtype()) == false)
                continue;           

            if(Integer.toString(loc - 1).equals(hgvs.getLocation()) == false && Integer.toString(loc + 1).equals(hgvs.getLocation()) == false)
                continue;

            return true; // if we come that far everything is fine
        }

        return false;
    }

    /**
     * Normalization of nucleotide sequence mutations (NSM) for
     * genes located on the forward strand.
     *
     * @param candidate     dbSNP candidate
     * @return true if the mutation mention can be normalized to a NSM and the gene is in fwd direction
     */
    private boolean forwardNSMSimple(dbSNP candidate) {

        for (HGVS hgvs : candidate.getHgvs()) {

            if (hgvs.getMutation() == null || hgvs.getWildtype() == null || hgvs.getLocation() == null)
                continue;

            //Normalize only snps located on genomic or cDNA
            if (hgvs.getType() != 'g' && hgvs.getType() != 'c')
                continue;

            //Test if the wildtype between textmining and hgvs object is the same
            if (hgvs.getWildtype().equals(wtResidue) == false)
                continue;

            // Check mutated residue
            boolean contained = false;
            if (hgvs.getMutation().contains(mutResidue)) {
                contained = true;
            }
            else if(mutResidue.equals(wtResidue) && hgvs.getWildtype().contains(wtResidue)){ //If mutated residue equals wildtype residue, we have to compare with the wildtype residue too
                contained = true;
            }

            if (contained == false)
                continue;

            // Check location
            if (hgvs.getLocation().equals(position) == false)
                continue;

            return true; // if we come that far everything is fine
        }

        return false;
    }

    /**
     *
     *
     * @param candidate     dbSNP candidate
     * @return return true if the mutation mention can be normalized to a NSM and the gene is in fwd direction
     */
    private boolean reverseNSMSimple(dbSNP candidate) {
        for (HGVS hgvs : candidate.getHgvs()) {

            if (hgvs.getMutation() == null || hgvs.getWildtype() == null || hgvs.getLocation() == null)
                continue;

            if (hgvs.getType() != 'g' && hgvs.getType() != 'c') // Normalize
                continue;

            if (hgvs.getMutation().equals(wtResidue) == false) // Check Wildtype
                continue;

            boolean contained = false; // Check mutated residue
            if (hgvs.getWildtype().contains(mutResidue)) {
                contained = true;
            }
            else if(mutResidue.equals(wtResidue) && hgvs.getWildtype().contains(wtResidue)){//If mutated residue equals wildtype residue, we have to compare with the wildtype residue too
                contained = true;
            }
            if (contained == false)
                continue;

            if (hgvs.getLocation().equals(position) == false) // Check location
                continue;

            return true;
        }

        return false;
    }

    /**
     * This method loads Mutations from a file which are in the following format
     * described in data/snps.txt (PMID\tWMutM\tstart\tstop)
     *
     * @param file with mutations
     * @return Mao from pmids to all contained mutations
     * @throws java.io.FileNotFoundException In case the file is not found
     * @throws java.io.IOException In case of arbitrary problems
     */
    public static Map<Integer,List<MutationMention>> readMutations(String file)
            throws IOException {
        Map<Integer,List<MutationMention>> result = new HashMap<Integer, List<MutationMention>>();
        BufferedReader br;

        if (file.endsWith(".gz"))
            br = new BufferedReader(new InputStreamReader(new GZIPInputStream(
                    new FileInputStream(file))));
        else
            br = new BufferedReader(new InputStreamReader(new FileInputStream(
                    file)));
        String line;
        while ((line = br.readLine()) != null) {

            if (line.startsWith("#")) {
                System.err.println("Skipping line " + line);
                continue;
            }

            String[] array = line.split("\t");
            if (array.length == 4) {
                int pmid = Integer.parseInt(array[0]);
                MutationMention tmp = new MutationMention(array[1]);
                tmp.setLocation(new EntityOffset(Integer.parseInt(array[2]),
                        Integer.parseInt(array[3])));
                tmp.tool = Tool.MUTATIONFINDER;


                if(result.containsKey(pmid))
                    result.get(pmid).add(tmp);

                else{
                    List<MutationMention> tmpList = new ArrayList<MutationMention>();
                    tmpList.add(tmp);
                    result.put(pmid, tmpList);
                }

            } else {
                System.err.println(array.length);
                System.err.println("Error parsing line " + line);
            }
        }

        return result;
    }

    /**
     * Empty constructor
     */
    public MutationMention(){}

    /**
     * Generates a Mutation object from a String like "A123T".
     *
     * @param mutation   Mutationstring (e.g. A123T)
     */
    public MutationMention(String mutation) {
        super();

        Matcher matcher = pattern.matcher(mutation);

        if (matcher.find() == false) {
            System.err.println("Mutation '" + mutation + "' currently not supported for normalization");
        } else {
            if (matcher.group(2).startsWith("+"))
                this.position = matcher.group(2).substring(1);
            else
                this.position = matcher.group(2);
            this.wtResidue = matcher.group(1);
            this.mutResidue = matcher.group(3);
            this.text = mutation;

            this.tool = Tool.UNKNOWN;
            this.type = Type.SUBSTITUTION;

            setMutationLocation();
        }

    }

    /**
     *
     * @param location   Location in text
     * @param mutation    Mutationstring (e.g. A123T)
     */
    public MutationMention(EntityOffset location, String mutation) {
        super();

        Matcher matcher = pattern.matcher(mutation);

        if (matcher.find() == false) {
            System.err.println("Skipping '" + mutation + "'");
        }

        else {
            this.location = location;

            if (matcher.group(2).startsWith("+"))
                this.position = matcher.group(2).substring(1);
            else
                this.position = matcher.group(2);

            this.wtResidue = matcher.group(1);
            this.mutResidue = matcher.group(3);

            this.tool = Tool.UNKNOWN;
            this.type = Type.SUBSTITUTION;

            setMutationLocation();
        }
    }


    public MutationMention(int start, int end, String text, String ref, String location, String wild, String mutated, Type type, Tool tool) {
        this.location = new EntityOffset(start, end);
        this.text = text;
        this.ref = ref;
        this.position = location;
        this.wtResidue = (wild == null || wild.equals("")) ? null : wild;           //SETH saves empty wild-types as '', while others save it as null --> Unify
        this.mutResidue = (mutated == null || mutated.equals("")) ? null : mutated;
        this.type = type;
        this.tool = tool;

        setMutationLocation();
    }


    /**
     * Gets the location of the mutation
     *
     * @return the location of the mutation
     */
    public EntityOffset getLocation() {
        return location;
    }

    /**
     * Gets the start-location of the mutation
     * @return  start-location
     */
    public int getStart(){
        return location.getStart();
    }

    /**
     * Gets the end-location of the mutation
     * @return    end-location
     */
    public int getEnd(){
        return location.getStop();
    }

    /**
     * Sets the position of the mutation
     *
     * @param location
     *            the new position of the mutation
     */
    private void setLocation(EntityOffset location) {
        this.location = location;
    }

    /**
     * Gets the residue.
     *
     * @return the residue
     */
    public String getWtResidue() {
        return wtResidue;
    }

    /**
     * Sets the residue.
     *
     * @param wtResidue
     *            the new residue
     */
    public void setWtResidue(String wtResidue) {
        this.wtResidue = wtResidue;
    }

    /**
     * Gets the residues.
     *
     * @return the residues
     */
    public String getMutResidue() {
        return mutResidue;
    }

    /**
     * Sets the residues.
     *
     * @param mutResidue
     *            the new residues
     */
    public void setMutResidue(String mutResidue) {
        this.mutResidue = mutResidue;
    }

    /**
     * Gets the position.
     *
     * @return the position
     */
    public String getPosition() {
        return position;
    }

    /**
     * Sets the position.
     *
     * @param position
     *            the new position
     */
    public void setPosition(String position) {
        this.position = position;
    }


    /**
     * @return the flag indicating if this SNP is a NSM
     */
    public boolean isNsm() {
        return nsm;
    }

    public void setNsm(boolean nsm) {
        this.nsm = nsm;
    }

    /**
     * @return the flag indicating if this SNP is a PSM
     */
    public boolean isPsm(){
        return psm;
    }

    public void setPsm(boolean psm) {
        this.psm = psm;
    }

    /**
     * @return the flag indicating if this SNP is ambiguous
     */
    public boolean isAmbiguous() {
        return ambiguous;
    }

    public void setAmbiguous(boolean ambiguous) {
        this.ambiguous = ambiguous;
    }

    /**
     * Returns Mutation {@link Type} (e.g. substitution, ...)
     * @return mutation type
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns {@link Tool} used to find mutation  (MutationFinder or SETH)
     * @return {@link Tool} used to find mutation
     */
    public Tool getTool() {
        return tool;
    }

    /**
     * Returns text-mined text (e.g. 'substitution of Ala to Pro at 123')
     * @return text
     */
    public String getText() {
        return text;
    }

    /**
     * Returns reference sequence (e.g. 'c.' for cDNA)
     * @return reference sequence
     */
    public String getRef() {
        return ref;
    }

    /**
     * Returns all dbSNP entries associated with this mutation mention
     * @return dbSNP entries
     */
    public List<dbSNPNormalized> getNormalized() {
        return normalized;
    }

    /**
     * Returns the best dbSNP entries associated with this mutation mention
     * The result is based on the ranking of dbSNPNormalized
     * @return dbSNP entries
     */
    public List<dbSNPNormalized> getBestNormalized(){

        if(normalized == null || normalized.size() <= 1)
            return normalized;

        Collections.sort(normalized);
        List<dbSNPNormalized> result = new ArrayList<dbSNPNormalized>();

        int bestConfidence = normalized.get(0).getConfidence();
        loop:for(dbSNPNormalized snp : normalized){
            if(snp.getConfidence() < bestConfidence)
                break loop;

            result.add(snp);
        }

        if(result.size() == 0)
            throw new RuntimeException("Invalid resultsize");

        return result;
    }


    /**
     * Returns all Transcripts covering this SNP
     * @return Transcripts
     */
    public Set<TranscriptNormalized> getTranscripts() {
        return transcripts;
    }

    /**
     * Retrieves the pattern ID used for extracting the current Mutation mention
     * @return  pattern Id
     */
    public int getPatternId() {
        return patternId;
    }

    /**
     * Sets pattern ID
     * @param patternId   pattern ID
     */
    public void setPatternId(int patternId) {
        this.patternId = patternId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MutationMention that = (MutationMention) o;

        if (nsm != that.nsm) return false;
        if (psm != that.psm) return false;
        if (ambiguous != that.ambiguous) return false;
        if (location != null ? !location.equals(that.location) : that.location != null) return false;
        if (mutResidue != null ? !mutResidue.equals(that.mutResidue) : that.mutResidue != null) return false;
        if (normalized != null ? !normalized.equals(that.normalized) : that.normalized != null) return false;
        if (position != null ? !position.equals(that.position) : that.position != null) return false;
        if (ref != null ? !ref.equals(that.ref) : that.ref != null) return false;
        if (text != null ? !text.equals(that.text) : that.text != null) return false;
        if (tool != that.tool) return false;
        if (type != that.type) return false;
        if (wtResidue != null ? !wtResidue.equals(that.wtResidue) : that.wtResidue != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (tool != null ? tool.hashCode() : 0);
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (ref != null ? ref.hashCode() : 0);
        result = 31 * result + (wtResidue != null ? wtResidue.hashCode() : 0);
        result = 31 * result + (mutResidue != null ? mutResidue.hashCode() : 0);
        result = 31 * result + (position != null ? position.hashCode() : 0);
        result = 31 * result + (nsm ? 1 : 0);
        result = 31 * result + (psm ? 1 : 0);
        result = 31 * result + (ambiguous ? 1 : 0);
        result = 31 * result + (normalized != null ? normalized.hashCode() : 0);
        return result;
    }


    /**
     * Generates a human genome variation nomenclature
     * compliant representation of the mutation mention
     *
     * @return a mutation mention in HGVS
     */
    public String toHGVS(){

        //Mentions in DBSNP-Nomenclature
        if(tool.equals(Tool.DBSNP)){
            return this.text;
        }
        //Mentions recognized by SETH are already in HGVS
        else if(tool.equals(Tool.SETH) ){
            return cleanSETHString(this.text);
        }

        switch (type){
            case SUBSTITUTION:
                if(psm)
                    return "p." +AminoAcidHelper.getThreeLetter(wtResidue) +position +AminoAcidHelper.getThreeLetter(mutResidue);

                if(nsm)
                    return "c." +position +wtResidue +">" +mutResidue;

                if(ambiguous)
                    return "?." +position +wtResidue +">" +mutResidue;
                break;

            case  DELETION:
                if(psm)
                    return "p." +(wtResidue == null ? "" : AminoAcidHelper.getThreeLetter(wtResidue)) +position +"del" +(mutResidue == null ? "" : AminoAcidHelper.getThreeLetter(mutResidue));
                if(nsm)
                    return "c." +(mutResidue == null ? "" : mutResidue) +position +"del" +(wtResidue == null ? "" : wtResidue);
                if(ambiguous)
                    return "?." +(mutResidue == null ? "" : mutResidue) +position +"del" +(wtResidue == null ? "" : wtResidue);
                break;

            case  INSERTION:
                if(psm)
                    return "p." +(mutResidue == null ? "" : AminoAcidHelper.getThreeLetter(mutResidue)) +position +"ins" +(wtResidue == null ? "" : AminoAcidHelper.getThreeLetter(wtResidue));
                if(nsm)
                    return "c." +(wtResidue == null ? "" : wtResidue) +position +"ins" +(mutResidue == null ? "" : mutResidue);
                if(ambiguous)
                    return "?." +(wtResidue == null ? "" : wtResidue) +position +"ins" +(mutResidue == null ? "" : mutResidue);
                break;

            case  FRAMESHIFT:
                if(psm)
                    return "p." +(wtResidue == null ? "" : AminoAcidHelper.getThreeLetter(wtResidue)) +position +"fs" +(mutResidue == null ? "" : AminoAcidHelper.getThreeLetter(mutResidue));
                if(nsm)
                    return "c." +(wtResidue == null ? "" : wtResidue) +position +"fs" +(mutResidue == null ? "" : mutResidue);
                if(ambiguous)
                    return "?." +(wtResidue == null ? "" : wtResidue) +position +"fs" +(mutResidue == null ? "" : mutResidue);
                break;

            default:
                break;
        }

        return "??"; //In case we don't know
    }

    /**
     * Some mutation mentions recognized by SETH are not following the human mutation nomenclature.
     * For instance SETH recognizes mutations using three letter AA-abbreviations, contains whitespaces, or parenthesis
     *
     * @param mutation MutationMention found by SETH
     * @return cleaned MutationMention
     */
    private static String cleanSETHString(String mutation){
        Map<String, String> tmpMap = MutationExtractor.populateAminoAcidThreeToOneLookupMap;
        for(String key : tmpMap.keySet())
            mutation =mutation.replaceAll("(?i)"+key, tmpMap.get(key));

        return mutation.replaceAll("[\\s\\(\\)]", "");
    }



    /**
     *  Checks if the mutation mention is a NSM
     * @return true if the mutation is a nucleotide sequence mutation
     */
    private boolean isNucleotide() {
        if (!this.isAminoAcid()) {

            if(this.position == null)
                return false;

            if (this.position.contains("*") || this.position.contains("+") || this.position.contains("-"))
                return true;

            try{
                int position = Integer.parseInt(this.position);
                // Mutations in locations above this cutoff have to be nucleotides as Titin (33,000 AA) is the currently longest known protein
                if(position > 35000)
                    return true;
            }catch(NumberFormatException nfe){
                return true; //Protein mutations should always have integers as position
            }
        }

        return false;
    }


    /**
     * Checks if the mutation mention is a PSM
     * @return true if the mutation mention is a protein sequence mutation
     */
    private boolean isAminoAcid() {

        if(this.tool.equals(Tool.SETH)){
            return (this.text.contains("p."));
        }

        switch (type){
            case SUBSTITUTION:
                if (!atgc.contains(this.wtResidue) || !atgc.contains(this.mutResidue))
                    return true;
                else
                    return false;

            case INSERTION:
                return !atgc.contains(this.mutResidue);

            case DELETION:
                return !atgc.contains(this.mutResidue);

            case DELETION_INSERTION:
                return !atgc.contains(this.mutResidue);

            case DUPLICATION:
                return !atgc.contains(this.mutResidue);

            default:
                return false;
        }
    }


    /**
     * Check if mutation is ambiguous
     * @return true if the mutation is ambiguous (NSM or PSM)
     */
    private boolean isAmgib() {

        if(this.tool.equals(Tool.SETH)){
            return false;
        }

        if(this.tool.equals(Tool.DBSNP)){
            return true;
        }

        if(this.position == null)
            return true;

        if (this.isAminoAcid())
            return false;

        if (this.isNucleotide())
            return false;

        return true;
    }

    /**
     * This method sets the three boolean flags nsm, psm, and ambiguous
     * Types can be changes using the set.. Methods
     */
    private void setMutationLocation(){
        this.nsm = isNucleotide();
        this.psm = isAminoAcid();
        this.ambiguous = isAmgib();
    }



    /**
     * To String method for MutationMention
     *
     * @see Object#toString()
     */
   @Override
    public String toString() {
        return "MutationMention [span=" + location.getStart() +"-" +location.getStop() +", pattern=" +patternId
                + ", mutResidue=" + mutResidue + ", location=" +position + ", wtResidue=" + wtResidue +", text=" +text
                + ", type=" + getType() + ", tool=" + getTool() + "]";
    }

    /**
     * Similar to @toString but prints all inforamtion
     * @return String containing all properties of a mutation
     */
    public String toFullString() {
        return "MutationMention{" +
                "type=" + type +
                ", tool=" + tool +
                ", location=" + location +
                ", text='" + text + '\'' +
                ", ref='" + ref + '\'' +
                ", wtResidue='" + wtResidue + '\'' +
                ", mutResidue='" + mutResidue + '\'' +
                ", position='" + position + '\'' +
                ", nsm=" + nsm +
                ", psm=" + psm +
                ", ambiguous=" + ambiguous +
                ", patternId=" + patternId +
                ", normalized=" + normalized +
                ", transcripts=" + transcripts +
                '}';
    }



    /**
     * @return SNP-description following the <wt><aa><mt> nomenclature
     */
    public String toNormalized() {

        if(this.getTool().equals(Tool.MUTATIONFINDER)){
            return wtResidue + position + mutResidue;
        }

        else if(this.getTool().equals(Tool.REGEX)){
            //This is a special case, as deletions can have several different abbreviations (e.g., delta, del, or Δ)
            if(this.getType().equals(Type.DELETION))
                return  wtResidue+position +"del";

            else if(this.getType().equals(Type.INSERTION))
                return  mutResidue+position +"ins";

            else if(this.getType().equals(Type.DELETION_INSERTION))
                return  mutResidue+position +"insdel";

            else if(this.getType().equals(Type.CONVERSION))
                return  mutResidue+position +"con";

            else if(this.getType().equals(Type.DUPLICATION))
                return  mutResidue+position +"dup";

            else if(this.getType().equals(Type.INVERSION))
                return  mutResidue+position +"inv";

            else
                return text;
        }

        else
            return text;
    }

    @Override
    public int compareTo(MutationMention other) {

        //First Compare by text-lication
        if(!this.location.equals(other.location))
            return this.getLocation().compareTo(other.getLocation());

        //Second compare by Tool-Type
        if(!this.tool.equals(other.tool))
            return this.tool.compareTo(other.tool);

        //Third, compare by Type
        if(!this.type.equals(other.type))
            return this.type.compareTo(other.type);


        return 0;
    }

}
