package seth.oldNomenclature;

import de.hu.berlin.wbi.objects.EntityOffset;
import de.hu.berlin.wbi.objects.MutationMention;
import edu.uchsc.ccp.nlp.ei.mutation.MutationFinder;
import seth.ner.wrapper.Type;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class can be used to find mutation mentions (deletions, IVS-substitutions, insertions, and frameshifts)
 * written in  deprecated nomenclature
 * @author Philippe Thomas
 */
public class OldNomenclature {

    final private  Logger logger = LoggerFactory.getLogger(OldNomenclature.class);

    final private static String prefix="(^|[_\\s\\(\\)\\[\\'\"/,;:])"; //>
    final private static String suffix="(?=([\\.,\\s\\)\\(\\]\\'\":;\\-/]|$))";

    private final List<NomenclaturePattern> patterns = new ArrayList<>(); //A list of patterns for mutation recognition
    private final Pattern ivsPattern = Pattern.compile(prefix +"(?<group>(?<pos>IVS[\\-]?[1-9][0-9]*\\s?[+-]\\s?[1-9][0-9]*)\\s?(?<wt>[ATGCatgc])\\s?(?:-{0,2}>|to|→|/|\\\\)\\s?(?<mut>[ATGCatgc]))" +suffix); //IVS nomenclature is a separate case
    private final Pattern fsPattern = Pattern.compile(prefix +"(?<group>(?<wt>[CISQMNPKDTFAGHLRWVEYBZJ])\\s?(?<pos>[1-9][0-9]*)\\s?(?<mut>[CISQMNPKDTFAGHLRWVEYBZJX])?fsX?(?<length>[1-9][0-9]*)?)" +suffix); //frameshift pattern M245Vfs; M245VfsX; M245fs; M245VfsX123
    private final Map<String, Type> modificationToType = new HashMap<>(); //Map from variation string to mutation type
    private final Map<String, String> abbreviationLookup = new HashMap<>(); //Map from AA to one letter

    private final String defaultPatternsFile = "/resources/patterns.txt";


    /**
     * Initialization of OldNomenclature-Matcher requires a set of regular expressions that will be used to detect deletions/insertions/...
     * This constructor loads the regular expressions from the packed JAR.
     * Important: Substitutions in deprecated nomenclature (e.g., Ala12Tyr) are detected using the @{@link MutationFinder} module
     */
    public OldNomenclature(){
        super();
        initializeHashMaps();

        loadRegularExpressionsFromJar(defaultPatternsFile);
    }

    /**
     * Initialization of OldNomenclature-Matcher requires a set of regular expressions that will be used to detect deletions/insertions/....
     * This constructor loads the regular expressions from a file designated by the filename input parameter.
     * Substitutions in deprecated nomenclature are detected using the @{@link MutationFinder} module
     *
     * @param fileName Name of the file, where the regular expressions can be found
     */
    public OldNomenclature(String fileName){
        super();
        initializeHashMaps();

        loadRegularExpressionsFromFile(new File(fileName));
    }


    /**
     * Method is called in the constructors to initialize the Maps
     */
    private void initializeHashMaps(){
        abbreviationLookup.putAll(MutationFinder.populateAminoAcidThreeToOneLookupMap);
        abbreviationLookup.putAll(MutationFinder.populateAminoAcidNameToOneLookupMap);

        abbreviationLookup.put("A", "A");
        abbreviationLookup.put("G", "G");
        abbreviationLookup.put("L", "L");
        abbreviationLookup.put("M", "M");
        abbreviationLookup.put("F", "F");
        abbreviationLookup.put("W", "W");
        abbreviationLookup.put("K", "K");
        abbreviationLookup.put("Q", "Q");
        abbreviationLookup.put("E", "E");
        abbreviationLookup.put("S", "S");
        abbreviationLookup.put("P", "P");
        abbreviationLookup.put("V", "V");
        abbreviationLookup.put("I", "I");
        abbreviationLookup.put("C", "C");
        abbreviationLookup.put("Y", "Y");
        abbreviationLookup.put("H", "H");
        abbreviationLookup.put("R", "R");
        abbreviationLookup.put("N", "N");
        abbreviationLookup.put("D", "D");
        abbreviationLookup.put("T", "T");
        abbreviationLookup.put("B","B");
        abbreviationLookup.put("Z","Z");
        abbreviationLookup.put("J","J");
        abbreviationLookup.put("X", "X");
        abbreviationLookup.put("*", "X");

        modificationToType.put("DEL", Type.DELETION);
        modificationToType.put("DELTA", Type.DELETION);
        modificationToType.put("Δ", Type.DELETION);
        modificationToType.put("DELETION", Type.DELETION);
        modificationToType.put("DELETIONS", Type.DELETION);
        modificationToType.put("DELETED", Type.DELETION);
        modificationToType.put("DELETING", Type.DELETION);

        modificationToType.put("INS", Type.INSERTION);
        modificationToType.put("INSERTION", Type.INSERTION);
        modificationToType.put("INSERTIONS", Type.INSERTION);
        modificationToType.put("INSERTED", Type.INSERTION);
        modificationToType.put("INSERTING", Type.INSERTION);

        modificationToType.put("DUP", Type.DUPLICATION);
        modificationToType.put("DUPLICATION", Type.DUPLICATION);
        modificationToType.put("DUPLICATIONS", Type.DUPLICATION);
        modificationToType.put("DUPLICATED", Type.DUPLICATION);
        modificationToType.put("DUPLICATING", Type.DUPLICATION);

        modificationToType.put("INV", Type.INVERSION);
        modificationToType.put("INVERSION", Type.INVERSION);
        modificationToType.put("INVERSIONS", Type.INVERSION);
        modificationToType.put("INVERTED", Type.INVERSION);
        modificationToType.put("INVERTING", Type.INVERSION);

        modificationToType.put("TRANSLOCATION", Type.TRANSLOCATION);
        modificationToType.put("TRANSLOCATIONS", Type.TRANSLOCATION);
        modificationToType.put("TRANSLOCATED", Type.TRANSLOCATION);

        modificationToType.put("INSDEL", Type.DELETION_INSERTION);
        modificationToType.put("INS/DEL", Type.DELETION_INSERTION);

        modificationToType.put("FS", Type.FRAMESHIFT);
        modificationToType.put("FSX", Type.FRAMESHIFT);
        modificationToType.put("FRAMESHIFT", Type.FRAMESHIFT);

        modificationToType.put("CONV", Type.CONVERSION);
        modificationToType.put("CONVERSION", Type.CONVERSION);
        modificationToType.put("CONVERSIONS", Type.CONVERSION);
        modificationToType.put("CONVERTING", Type.CONVERSION);
        modificationToType.put("CONVERTED", Type.CONVERSION);
    }


    /*
    * Loads regular_expressions from file.
    */
    private void loadRegularExpressionsFromFile(File file) {

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            loadRegularExpressionsFromStream(br);
        } catch (FileNotFoundException fnfe) {
            logger.warn("The file containing regular expressions could not be found: '" + file.getAbsolutePath() + File.separator + file.getName() +"'\nLoading patterns from JAR '" +defaultPatternsFile +"'");
            loadRegularExpressionsFromJar(defaultPatternsFile);
        }
        finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    // ignore exception
                    logger.warn("Problem closing buffered reader", e);
                }
            }
        }
    }


    /*
    * Loads regular_expressions from Java-Archive.
    */
    private void loadRegularExpressionsFromJar(String file) {
        logger.info("Loading regular expressions from Java Archive at location '" +file +"'");
        InputStream is = this.getClass().getResourceAsStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        try{
            loadRegularExpressionsFromStream(br);
        }catch(Exception ex){
            logger.error("Error in fallback code for reading mutation-finder file from Java Archive", ex);
        }
        finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    // ignore exception
                    logger.warn("Problem closing buffered reader", e);
                }
            }
        }
    }


    /**
     * Helper method which loads a set of regular expressions from a BufferedReader
     * This method is used for loading regex from a file or the java-archive
     * @param br BufferedReader to read the regex from
     */
    private void loadRegularExpressionsFromStream(BufferedReader br) {

        //Replace amino acids with this regex
        String aa ="(?<amino>[ATGC]+|[CISQMNPKDTFAGHLRWVEYBZJX]|(?:[Al]la|[Gg]ly|[Ll]eu|[Mm]et|[Pp]he|[Tt]rp|[Ll]ys|[Gg]ln|[Gg]lu|[Ss]er|[Pp]ro|[Vv]al|[Ii]le|[Cc]ys|[Tt]yr|[Hh]is|[Aa]rg|[Aa]sn|[Aa]sp|[Tt]hr|[Aa]sx|[Gg]lx|[Xx]le|[Tt]er|[Ss]tp" +
                "|[Aa]lanine|[Gg]lycine|[Ll]eucine|[Mm]ethionine|[Pp]henylalanine|[Tt]ryptophan|[Ll]ysine|[Gg]lutamine|[Gg]lutamic [Aa]cid|[Gg]lutamate|[Aa]spartate|[Ss]erine|[Pp]roline|[Vv]aline|[Ii]soleucine|[Cc]ysteine|[T]yrosine|[Hh]istidine|[Aa]rginine|[Aa]sparagine|[Aa]spartic [Aa]cid|[Tt]hreonine|[Tt]erm|[Ss]top|[Aa]mber|[Uu]mber|[Oo]chre|[Oo]pal))";

        //Replace numbers with this regex
        String location = "(?<pos>[\\+\\-]?[1-9][0-9]*(?:\\s?[\\+\\-_]\\s?[1-9][0-9]*)?)";

        //Replace all modification mentions with this regex
        String modification = "(?<mod>"
                +"[Cc]onv|[Cc]onversion|[Cc]onversions|[Cc]onverted|[Cc]onverting" +
                "|Δ|[Dd]el|[Dd]eleted|[Dd]eleting|[Dd]eletion|[Dd]eletions|[Dd]elta" +
                "|[Dd]up|[Dd]uplicated|[Dd]uplicating|[Dd]uplication|[Dd]uplications" +
                "|[Ff]rameshift" + //|fs|fsX //Not required, as fs and fsX is covered by previous regular expression
                "|[Ii]ns|[Ii]ns\\/del|[Ii]nsdel" +
                "|[Ii]nserted|[Ii]nserting|[Ii]nsertion|[Ii]nsertions" +
                "|[Ii]nv|[Ii]nversion|[Ii]nversions" +
                "|[Ii]nverted|[Ii]nverting" +
                "|[Tt]translocated|[Tt]ranslocation|[Tt]ranslocations"
                +")";

        try{
            int nLine=0;
            while(br.ready()){
                String line = br.readLine();
                nLine++;
                if(line.startsWith("#") || line.matches("^\\s*$")) //Skip empty lines and comment lines
                    continue;

                StringBuilder sb = new StringBuilder(line);
                sb.replace(sb.indexOf("<aa>"), sb.indexOf("<aa>")+"<aa>".length(), aa );
                sb.replace(sb.indexOf("<number>"), sb.indexOf("<number>")+"<number>".length(), location );
                sb.replace(sb.indexOf("<kw>"), sb.indexOf("<kw>")+"<kw>".length(), modification);

                sb.insert(0,"(?<group>");
                sb.insert(0, prefix);
                sb.append(")" );
                sb.append(suffix);
                //System.out.println(line+"\t" +sb);

                final NomenclaturePattern pattern = new NomenclaturePattern(Pattern.compile(sb.toString()), sb.toString(), nLine);
                patterns.add(pattern);
            }
            br.close();
        }catch(IOException iox){
            logger.error("Problem loading patterns for old nomenclature!", iox);
        }
        logger.info("Loaded {} patterns", patterns.size());
    }

    /**
    private void filterMentions(List<MutationMention> mentions){
        InsP3
        InsP(3
                InsP6
                InsP(6
                        InsP4
                        Ins-P3
    }
     */


    /**
     * Extracts mentions of mutations from natural language text written in deprecated nomenclature
     * @param text Input natural language text
     * @return A list of {@link MutationMention} objects
     */
    public List<MutationMention> extractMutations(String text){

        List<MutationMention> result = new ArrayList<MutationMention>();

        for(NomenclaturePattern pattern : patterns){ //Iterate patterns
            Matcher m = pattern.getPattern().matcher(text);
            while(m.find()){

                logger.debug("Found mutation mention '{}'", m.group("group"));

                int start = m.start(2);
                int end   = m.start(2) + m.group("group").length();

                Type type = modificationToType.get(m.group("mod").toUpperCase());

                if ( type == null){
                    logger.error("Cannot find modification type for '" +m.group("mod") +"'; skipping mention in text '" +text.substring(start, end) +"'");
                    continue;
                }
                String amino = m.group("amino");
                String location = m.group("pos");

                String shortAminoName = amino.toUpperCase();
                if (abbreviationLookup.containsKey(shortAminoName)) //Not contained for short mentions (e.g., AATTGC)
                    shortAminoName = abbreviationLookup.get(shortAminoName);


                MutationMention mm;
                switch (type) {
                    case DELETION:
                        mm = new MutationMention(start, end, text.substring(start, end), null, location, shortAminoName, null, type, MutationMention.Tool.REGEX);
                        break;
                    default:
                        mm = new MutationMention(start, end, text.substring(start, end), null, location, null, shortAminoName, type, MutationMention.Tool.REGEX);
                }

                int intLocation = Integer.MIN_VALUE; boolean parseable = false;
                try{
                    intLocation = Integer.parseInt(location);
                    parseable = true;
                }catch(NumberFormatException nfe){
                    logger.trace("Location not parseable",nfe); //This is not a problem, simply a test for number
                }


                //Likely PSM, if position is positive and we could ground amino acid name
                if((amino.length() > 1 && !amino.equals(shortAminoName) || amino.matches(".*[^ATGC].*") ) && intLocation > 0){
                    mm.setPsm(true);
                    mm.setNsm(false);
                    mm.setAmbiguous(false);
                }

                //Likely NSM, if position is negative or contains signs such as "12-1; or 15_19"
                else if(this.isLikelyNsm(location)){
                    mm.setPsm(false);
                    mm.setNsm(true);
                    mm.setAmbiguous(false);
                }
                else{
                    mm.setPsm(false);
                    mm.setNsm(false);
                    mm.setAmbiguous(true);
                }

                if(!amino.equals(shortAminoName) && !mm.isPsm()){
                    logger.warn("Unlikely to happen for '{}'", m.group("group"));
                }

                mm.setPatternId(pattern.getId());

                //Here we filter likely false positive mentions (where the String mention is only one char and the location is below 9); e.g., ΔR2; but not ΔR20 or ΔTyr2
                if(amino.length() != 1 || Math.abs(intLocation) > 9 || !parseable)
                    result.add(mm);
               else
                    logger.debug("Skipping '{}'; as likely false positive ", text.substring(start, end));
            }
        }

        Matcher m = ivsPattern.matcher(text);
        while(m.find()){
            int start = m.start(2);
            int end   = m.start(2)+m.group("group").length();
            MutationMention mm = new MutationMention(start, end, text.substring(start, end), "c.", m.group("pos"),  m.group("wt"), m.group("mut"), Type.SUBSTITUTION, MutationMention.Tool.REGEX);

            //IVS is always nucleotide
            mm.setPsm(false);
            mm.setNsm(true);
            mm.setAmbiguous(false);
            mm.setPatternId(-1);
            result.add(mm);
        }

        m = fsPattern.matcher(text);
        while(m.find()){
            int start = m.start("group");
            int end   = m.end("group");
            MutationMention mm = new MutationMention(start, end, text.substring(start, end), "p.", m.group("pos"),  m.group("wt"), m.group("mut"), Type.FRAMESHIFT, MutationMention.Tool.REGEX);

            mm.setPsm(true);
            mm.setNsm(false);
            mm.setAmbiguous(false);
            mm.setPatternId(-2);
            result.add(mm);
        }


        result = removeDuplicates(result, text);



        return result;
    }

    /**
     * This code removes some duplicated items;
     * Mutation mentions containing a "-" are sometimes found several times (by different patterns); usually with and without a negative location
     * Here we remove the mention with negative offset
     * e.g., deletion of Ala-12; the "-" is not used to indicate "-12", but "12"
     */
    private List<MutationMention> removeDuplicates(List<MutationMention> result, String text){

        Map<EntityOffset, List<MutationMention>> groupByOffset = result.stream().collect(Collectors.groupingBy(w -> w.getLocation()));
        for(EntityOffset key : groupByOffset.keySet()){
            List<MutationMention> mentions = groupByOffset.get(key);
            //?Potential? problem in these cases
            if(mentions.size() > 1){
                logger.warn("Found several mentions for String '{}'", text.substring(key.getStart(), key.getStop()));

                List<MutationMention> delete = new ArrayList<>();
                for(int i =0; i <mentions.size(); i++){
                    MutationMention mm1 = mentions.get(i);
                    for(int j =i+1; j < mentions.size(); j++){
                        MutationMention mm2 = mentions.get(j);
                        if(mm1.getPosition().startsWith("-") && !mm2.getPosition().startsWith("-"))
                            delete.add(mm1);
                        else if (!mm1.getPosition().startsWith("-") && mm2.getPosition().startsWith("-"))
                            delete.add(mm2);
                    }
                }

                result.removeAll(delete);
            }
        }
        return result;
    }

    /**
     * If a location is negative or throws a number format exception, we assume that it is a NSM and not a PSM
     * @param location location of the mutation
     * @return true if the location is NSM
     */
    private boolean isLikelyNsm(String location){

        try{
            int pos = Integer.parseInt(location);
            if(pos < 0)
                return true;
        }catch(NumberFormatException nfe){
            logger.trace("Mutation tagged as likely NSM due to {} ", location);
            return true;
        }

        return false;
    }


    public static void main(String[] args) throws IOException {
        final OldNomenclature oldNomenclature = new OldNomenclature("src/main/resources/patterns.txt");

        List<MutationMention> mentions = oldNomenclature.extractMutations("translocation of T308");
        for(MutationMention mm : mentions){
            System.out.println(mm.getPatternId());
        }
    }

}
