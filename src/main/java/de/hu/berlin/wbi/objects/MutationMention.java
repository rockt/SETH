package de.hu.berlin.wbi.objects;

/**
 Copyright 2010, 2011 Philippe Thomas
 This file is part of snp-normalizer.

 snp-normalizer is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 any later version.

 snp-normalizer is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with snp-normalizer.  If not, see <http://www.gnu.org/licenses/>.
 */

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
public class MutationMention {

    /** Type of Mutation (e.g. substitution, insertion, ...) */
    protected Type type;

    /** Refers to which tool has been used for extraction */
    protected Tool tool;

    /** Location in the text. */
    protected EntityOffset location;

    /** Text mention. */
    protected String text;

    /** Used reference sequence (e.g. c., g., ...) */
    protected String ref;

    /** Wildtype residue. */
    protected String wtResidue;

    /** Mutated residue. */
    protected String mutResidue;

    /** Location of the mutation wrt residue or nucleotides*/
    protected String position;

    /** Flag indicating if this SNP is possibly a NSM. */
    protected boolean nsm;

    /** List with normalized SNPs*/
    protected List<dbSNPNormalized> normalized;

    /** Which regular expression has been used to extract this SNP from text (MutationFinder only)*/
    protected int patternId;

    /** Refers to which tool has been used for extraction */
    public enum Tool{
        MUTATIONFINDER, SETH, REGEX, DBSNP
    }

    /** Pattern is used to extract wildtype-location-residue from a string e.g. A123T  */
    private static final Pattern pattern = Pattern.compile("^([A-Z])([\\-\\+\\*]?[1-9][0-9]*[\\-\\+]?[0-9]*)([A-Z])$");



    public void normalizeSNP(int id){
        dbSNP snp = new dbSNP();
        snp.setRsID(id);
        normalized.add(new dbSNPNormalized(snp, true, false, false, null, true));
    }

    /**
     * This method evaluates if some of the {@link de.hu.berlin.wbi.objects.dbSNP} provided in <b>candidates</b>
     * actually equals the text-mined {@link MutationMention}
     *
     * @param candidates List of dbSNP-entries which might actually describe this SNP
     * @param features List of associated uniprot features
     * @param append Sometimes normalization has to be invoked several times (for several genes) Should we append result or not?
     *
     *
     * @return normalized mutations
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
                if(normalizePSMSimpleHGVS(candidate))
                    normalized.add(new dbSNPNormalized(candidate, true, false, true, null, true));

                if(normalizePSMSimpleHGVSSwap(candidate))
                    normalized.add(new dbSNPNormalized(candidate, true, false, true, null, false));

                if(normalizePSMMethionineHGVS(candidate))
                    normalized.add(new dbSNPNormalized(candidate, false, true, true, null, true));

                if(normalizePSMMethionineSwapHGVS(candidate))
                    normalized.add(new dbSNPNormalized(candidate, false, true, true, null, false));

                //Now we try to normalize to DNA using HGVS information
                if (forwardNSMSimple(candidate) || reverseNSMSimple(candidate))
                    normalized.add(new dbSNPNormalized(candidate, true, false, false, null, true));
            }

            //Normalize candidate using information in 'PSM' table
            if(candidate.getResidues() != null){

                boolean forward= checkPSM_WildType(candidate, true) && checkPSM_Mutated(candidate, true) ;
                boolean reverse= checkPSM_WildType(candidate, false) && checkPSM_Mutated(candidate, false) ;

                if (forward || reverse){

                    if (normalizePSMSimple(candidate) ) //Exact match?
                        normalized.add(new dbSNPNormalized(candidate, true, false, true, null, forward));

                    if(normalizePSMMethionine(candidate)) //Methionine Offset of 1?
                        normalized.add(new dbSNPNormalized(candidate, false, true, true, null, forward));

                    UniprotFeature feature = normalizePSMVariableOffset(candidate, features);	//match using UniProt features
                    if(feature != null){
                        normalized.add(new dbSNPNormalized(candidate, false, false, true, feature, forward));
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
     * also we return only the results with highest association score (no impact for evaluation)
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

            if (wtResidue.equals(hgvs.getMutation()) == false || mutResidue.equals(hgvs.getWildtype()) == false)
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

            if (wtResidue.equals(hgvs.getWildtype()) == false || mutResidue.equals(hgvs.getMutation()) == false)
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
     * @return @return true if the mutation mention can be normalized to a NSM and the gene is in bwd direction
     */
    private boolean reverseNSMSimple(dbSNP candidate) {
        for (HGVS hgvs : candidate.getHgvs()) {

            if (hgvs.getMutation() == null || hgvs.getWildtype() == null
                    || hgvs.getLocation() == null)
                continue;

            if (hgvs.getType() != 'g' && hgvs.getType() != 'c') // NOrmalize
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
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
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

                if(!result.containsKey(pmid))
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
        }
    }


    public MutationMention(int start, int end, String text, String ref, String location, String wild, String mutated, Type type, Tool tool) {
        this.location = new EntityOffset(start, end);
        this.text = text;
        this.ref = ref;
        this.position = location;
        this.wtResidue = wild;
        this.mutResidue = mutated;
        this.type = type;
        this.tool = tool;
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
     * (non-Javadoc)
     *
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "MutationMention [span=" + location.getStart() +"-" +location.getStop()
                + ", mutResidue=" + mutResidue + ", location=" +position + ", wtResidue=" + wtResidue +", text=" +text
                + ", type=" + getType() + ", tool=" + getTool() + "]";
    }

    /**
     * @return SNP-description following the <wt><aa><mt> nomenclature
     */
    public String toNormalized() {

        if(this.getTool().equals(Tool.MUTATIONFINDER))
           return wtResidue + position + mutResidue;

        else
            return text;
    }

    /**
     * Checks if is flag indicating if this SNP is possibly a NSM.
     *
     * @return the flag indicating if this SNP is possibly a NSM
     */
    public boolean isNsm() {
        return nsm;
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
}
