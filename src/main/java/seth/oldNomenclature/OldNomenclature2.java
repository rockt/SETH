package seth.oldNomenclature;

import de.hu.berlin.wbi.objects.MutationMention;
import edu.uchsc.ccp.nlp.ei.mutation.MutationFinder;
import org.apache.oro.text.regex.Perl5Compiler;
import seth.ner.wrapper.Type;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * This class can be used to find mutation mentions (deletions, IVS-substitions, insertions, and frameshifts)
 * written in  deprecated nomenclature
 * @author Philippe Thomas
 */
public class OldNomenclature2 {

    final private  Logger logger = LoggerFactory.getLogger(OldNomenclature2.class);

    final private static String prefix="(^|[\\s\\(\\)\\[\\'\"/,])"; //>
    final private static String suffix="(?=([\\.,\\s\\)\\(\\]\\'\":;\\-/]|$))";

    private static List<Pattern> patterns; //All patterns used for finding mutations
    private  Map<String, Type> modificationToType;
    private final Map<String, String> abbreviationLookup;

    public OldNomenclature2(String regexFile){
        this();
        loadPatterns(regexFile);
    }

    public OldNomenclature2(){
        super();

        abbreviationLookup= new HashMap<>();
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

        modificationToType = new HashMap<>();
        modificationToType.put("deletion", Type.DELETION);
        modificationToType.put("deletions", Type.DELETION);
        modificationToType.put("deleted", Type.DELETION);
        modificationToType.put("del", Type.DELETION);
        modificationToType.put("delta", Type.DELETION);
        modificationToType.put("deleting", Type.DELETION);
        modificationToType.put("Δ", Type.DELETION);

        modificationToType.put("insertion", Type.INSERTION);
        modificationToType.put("insertions", Type.INSERTION);
        modificationToType.put("inserted", Type.INSERTION);
        modificationToType.put("ins", Type.INSERTION);
        modificationToType.put("inserting", Type.INSERTION);

        modificationToType.put("duplication", Type.DUPLICATION);
        modificationToType.put("duplications", Type.DUPLICATION);
        modificationToType.put("duplicated", Type.DUPLICATION);
        modificationToType.put("duplicating", Type.DUPLICATION);
        modificationToType.put("dup", Type.DUPLICATION);

        modificationToType.put("inversion", Type.INVERSION);
        modificationToType.put("inversions", Type.INVERSION);
        modificationToType.put("inverted", Type.INVERSION);
        modificationToType.put("inv", Type.INVERSION);
        modificationToType.put("inverting", Type.INVERSION);

        modificationToType.put("translocation", Type.TRANSLOCATION);
        modificationToType.put("translocations", Type.TRANSLOCATION);
        modificationToType.put("translocated", Type.TRANSLOCATION);

        modificationToType.put("insdel", Type.DELETION_INSERTION);
        modificationToType.put("ins/del", Type.DELETION_INSERTION);

        modificationToType.put("frameshift", Type.FRAMESHIFT);
        modificationToType.put("fs", Type.FRAMESHIFT);

    }

    private void loadPatterns(String file){
        patterns = new ArrayList<>();

        StringBuilder modifications = new StringBuilder();
        for(String key :modificationToType.keySet()){
            modifications.append(key);
            modifications.append("|");
        }
        modifications.deleteCharAt(modifications.length()-1);


        try{
            BufferedReader br = new BufferedReader(new FileReader(new File(file)));
            while(br.ready()){
                String line = br.readLine();
                if(line.startsWith("#") || line.matches("^\\s*$"))
                    continue;

                StringBuilder sb = new StringBuilder(line);
                sb.replace(sb.indexOf("<aa>"), sb.indexOf("<aa>")+"<aa>".length(), "(?<amino>[ATGC]+|[ISQMNPKDFHLRWVEYBZJX*]|CYS|ILE|SER|GLN|MET|ASN|PRO|LYS|ASP|THR|PHE|ALA|GLY|HIS|LEU|ARG|TRP|VAL|GLU|TYR|ALANINE|GLYCINE|LEUCINE|METHIONINE|PHENYLALANINE|TRYPTOPHAN|LYSINE|GLUTAMINE|GLUTAMIC ACID|GLUTAMATE|ASPARTATE|SERINE|PROLINE|VALINE|ISOLEUCINE|CYSTEINE|TYROSINE|HISTIDINE|ARGININE|ASPARAGINE|ASPARTIC ACID|THREONINE|TERM|STOP|AMBER|UMBER|OCHRE|OPAL)");
                sb.replace(sb.indexOf("<number>"), sb.indexOf("<number>")+"<number>".length(), "(?<pos>[+-]?[1-9][0-9]*(?:\\s?[+-_]\\s?[1-9][0-9]*)?)");
                sb.replace(sb.indexOf("<kw>"), sb.indexOf("<kw>")+"<kw>".length(), "(?<mod>" +modifications.toString() +")");

                sb.insert(0, prefix +"(?<group>");
                sb.append(")" +suffix);

                patterns.add(Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE));
            }
            br.close();
        }catch(IOException iox){
            logger.error("Problem loading patterns for old nomenclature!", iox);
        }
        logger.info("Loaded {} patterns", patterns.size());
    }

    // TODO: Check if we get same results as with old nomenclature
    public List<MutationMention> extractFromString(String text){

        List<MutationMention> result = new ArrayList<MutationMention>();

        for(Pattern pattern : patterns){
            Matcher m = pattern.matcher(text);
            while(m.find()){

                logger.debug("Found mutation mention '{}'", m.group("group"));

                int start = m.start(2);
                int end   = m.start(2) + m.group("group").length();

                Type type = modificationToType.get(m.group("mod").toLowerCase());
                String amino = m.group("amino");
                String location = m.group("pos");

                String shortAminoName = amino.toUpperCase();
                if (abbreviationLookup.containsKey(shortAminoName)) {
                    shortAminoName = abbreviationLookup.get(shortAminoName);
                }

                MutationMention mm;
                switch (type) {
                    case DELETION:
                        mm = new MutationMention(start, end, text.substring(start, end), null, location, shortAminoName, null, type, MutationMention.Tool.REGEX);
                        break;
                    default:
                        mm = new MutationMention(start, end, text.substring(start, end), null, location, null, shortAminoName, type, MutationMention.Tool.REGEX);
                }

                if(amino.length() > 1 && !amino.equals(shortAminoName)){
                    mm.setPsm(true);
                    mm.setNsm(false);
                    mm.setAmbiguous(false);
                }

                else if(this.isLikelyNsm(location)){
                    mm.setPsm(false);
                    mm.setNsm(true);
                    mm.setAmbiguous(false);
                }

                result.add(mm);
            }
        }

        return result;
    }

    /**
     * If a location is negative or throws a number format exception, we assume that it is a NSM and not a PSM
     * @param location
     * @return
     */
    private boolean isLikelyNsm(String location){

        try{
            int pos = Integer.parseInt(location);
            if(pos < 0)
                return true;
        }catch(NumberFormatException nfe){
            System.out.println(location);
            return true;
        }

        return false;
    }

}
