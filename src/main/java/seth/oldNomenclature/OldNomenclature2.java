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

/**
 * This class can be used to find mutation mentions (deletions, IVS-substitions, insertions, and frameshifts)
 * written in  deprecated nomenclature
 * @author Philippe Thomas
 */
public class OldNomenclature2 {

    final private static String prefix="(^|[\\s\\(\\)\\[\\'\"/,])"; //>
    final private static String suffix="(?=([\\.,\\s\\)\\(\\]\\'\":;\\-/]|$))";



    private static List<Pattern> patterns; //All patterns
    private final Map<String, Type> modificationToType;



    public OldNomenclature2(String regexFile){
        this();

        loadPatterns(regexFile);
    }


    public OldNomenclature2(){
        super();

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
                sb.replace(sb.indexOf("<aa>"), sb.indexOf("<aa>")+"<aa>".length(), "(?<amino>CYS|ILE|SER|GLN|MET|ASN|PRO|LYS|ASP|THR|PHE|ALA|GLY|HIS|LEU|ARG|TRP|VAL|GLU|TYR|ALANINE|GLYCINE|LEUCINE|METHIONINE|PHENYLALANINE|TRYPTOPHAN|LYSINE|GLUTAMINE|GLUTAMIC ACID|GLUTAMATE|ASPARTATE|SERINE|PROLINE|VALINE|ISOLEUCINE|CYSTEINE|TYROSINE|HISTIDINE|ARGININE|ASPARAGINE|ASPARTIC ACID|THREONINE|TERM|STOP|AMBER|UMBER|OCHRE|OPAL)");
                sb.replace(sb.indexOf("<number>"), sb.indexOf("<number>")+"<number>".length(), "(?<pos>[+-]?[1-9][0-9]*(?:\\s?[+-_]\\s?[1-9][0-9]*)?)");
                sb.replace(sb.indexOf("<kw>"), sb.indexOf("<kw>")+"<kw>".length(), "(?<mod>" +modifications.toString() +")");

                sb.insert(0, prefix +"(?<group>");
                sb.append(")" +suffix);

                patterns.add(Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE));
            }
            br.close();
        }catch(IOException iox){
            System.err.println("Problem loading patterns for old nomenclature!");
            iox.printStackTrace();
        }
    }

    // TODO: Check if we get same results as with old nomenclature
    // TODO Add normalization of amino acids
    // TODO Add ATGC and so on for one letter abbreviations

    public List<MutationMention> extractFromString(String text){

        List<MutationMention> result = new ArrayList<MutationMention>();

        for(Pattern pattern : patterns){
            Matcher m = pattern.matcher(text);
            while(m.find()){
                int start = m.start(2);
                int end   = m.start(2)+m.group("group").length();

                Type type = modificationToType.get(m.group("mod"));

                MutationMention mm;
                switch (type) {
                    case DELETION:
                        mm = new MutationMention(start, end, text.substring(start, end), "??", m.group("pos"), m.group("amino"), null, type, MutationMention.Tool.REGEX);
                        break;
                    default:
                        mm = new MutationMention(start, end, text.substring(start, end), "??", m.group("pos"), null, m.group("amino"), type, MutationMention.Tool.REGEX);
                }


                //TODO!!
                //mm.setPsm(false);
                //mm.setNsm(true);
                //mm.setAmbiguous(false);
                result.add(mm);
            }
        }

        return result;
    }

}
