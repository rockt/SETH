package seth;

import de.hu.berlin.wbi.objects.MutationMention;
import edu.uchsc.ccp.nlp.ei.mutation.MutationFinder;
import seth.ner.wrapper.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class can be used to find mutation mentions (deletions, IVS-substitions, insertions, and frameshifts)
 * written in  deprecated nomenclature
 * @author Philippe Thomas
 */
class OldNomenclature {

    final private static String prefix="(^|[\\s\\(\\)\\[\\'\"/,])"; //>
    final private static String suffix="(?=([\\.,\\s\\)\\(\\]\\'\":;\\-/]|$))";//|:?[ATGC]>[ATGC]
    //final private static Pattern dbSNP = Pattern.compile(prefix +"(rs[1-9][0-9]*)" +suffix);

    //852delA, 852delAT, 852del, 852del22, 852-123delA, 852+123delT
    final private static Pattern dnaDeletionPattern = Pattern.compile(prefix +"(?<group>(?<pos>[+-]?[1-9][0-9]*(?:\\s?[+-_]\\s?[1-9][0-9]*)?)\\s?(del)\\s?(?<wt>([ATGC]|[0-9])+))" +suffix);
    //IVS123+12A->T, IVS123-12A->T
    private final Pattern ivsPattern = Pattern.compile(prefix +"(?<group>(?<pos>IVS[\\-]?[1-9][0-9]*\\s?[+-]\\s?[1-9][0-9]*)\\s?(?<wt>[ATGCatgc])\\s?(?:-{0,2}>|→|/|\\\\)\\s?(?<mut>[ATGCatgc]))" +suffix); //IVS nomenclature is a separate case

    //F123del, Phe123del
    final private static Pattern protDeletionPattern = Pattern.compile(prefix +"(?<group>(?<wt>[CISQMNPKDTFAGHLRWVEYBZJX])(?<pos>[1-9][0-9]*)del)" +suffix);
    final private static Pattern protDeletionPatternLong = Pattern.compile(prefix +"(?<group>(?<wt>CYS|ILE|SER|GLN|MET|ASN|PRO|LYS|ASP|THR|PHE|ALA|GLY|HIS|LEU|ARG|TRP|VAL|GLU|TYR)(?<pos>[1-9][0-9]*)del)" +suffix, Pattern.CASE_INSENSITIVE);

    //deltaF508, DeltaF508, delF12, DelCys12
    final private static Pattern codonDeletionPattern = Pattern.compile(prefix +"(?<group>([dD]el(?:ta)?|Δ)(?<wt>[CISQMNPKDTFAGHLRWVEYBZJX])(?<pos>[1-9][0-9]*))" +suffix);
    final private static Pattern codonDeletionPattern2 = Pattern.compile(prefix +"(?<group>([dD]el(?:ta)?|Δ)(?<wt>CYS|ILE|SER|GLN|MET|ASN|PRO|LYS|ASP|THR|PHE|ALA|GLY|HIS|LEU|ARG|TRP|VAL|GLU|TYR)(?<pos>[1-9][0-9]*))" +suffix, Pattern.CASE_INSENSITIVE);


    //852insA, 852insAT, 852ins, 852ins22, 852-123insA, 852+123insT
    final private static Pattern dnaInsertionPattern = Pattern.compile(prefix +"(?<group>(?<pos>[+-]?[1-9][0-9]*(?:\\s?[+-_]\\s?[1-9][0-9]*)?)\\s?(ins)\\s?(?<mut>([ATGC]|[0-9])+))" +suffix);

    //A123fsX1,
    final private static Pattern frameshiftPattern = Pattern.compile(prefix +"(?<group>(?<wt>[CISQMNPKDTFAGHLRWVEYBZJX])(?<pos>[1-9][0-9]*)(fs)(?<mut>X[0-9]*)?)" +suffix);

    /**
     * Extracts mentions of mutations from natural language text written in deprecated nomenclature
     * @param text Input natural language text
     * @return A list of {@link MutationMention} objects
     */
    public List<MutationMention> extractMutations(String text) {
        List<MutationMention> result = new ArrayList<MutationMention>();

        Matcher m = dnaDeletionPattern.matcher(text);
        while(m.find()){
            int start = m.start(2);
            int end   = m.start(2)+m.group("group").length();//m.end(m.groupCount());
            MutationMention mm = new MutationMention(start, end, text.substring(start, end), "c.", m.group("pos"),  m.group("wt"), null, Type.DELETION, MutationMention.Tool.REGEX);

            mm.setPsm(false);
            mm.setNsm(true);
            mm.setAmbiguous(false);
            result.add(mm);
        }

        m = protDeletionPattern.matcher(text);
        while(m.find()){
            int start = m.start(2);
            int end   = m.start(2)+m.group("group").length();//m.end(m.groupCount());
            MutationMention mm = new MutationMention(start, end, text.substring(start, end), "p.", m.group("pos"),  m.group("wt"), null, Type.DELETION, MutationMention.Tool.REGEX);

            mm.setPsm(true);
            mm.setNsm(false);
            mm.setAmbiguous(false);
            result.add(mm);
        }
        m = protDeletionPatternLong.matcher(text);
        while(m.find()){
            Map<String, String> map = MutationFinder.populateAminoAcidThreeToOneLookupMap;
            int start = m.start(2);
            int end   = m.start(2)+m.group("group").length();//m.end(m.groupCount());
            MutationMention mm = new MutationMention(start, end, text.substring(start, end), "p.", m.group("pos"),  map.get(m.group("wt").toUpperCase()), null, Type.DELETION, MutationMention.Tool.REGEX);

            mm.setPsm(true);
            mm.setNsm(false);
            mm.setAmbiguous(false);
            result.add(mm);
        }

        m = codonDeletionPattern.matcher(text);
        while(m.find()){
            int start = m.start(2);
            int end   = m.start(2)+m.group("group").length();//m.end(m.groupCount());
            MutationMention mm = new MutationMention(start, end, text.substring(start, end), "p.", m.group("pos"),  m.group("wt"), null, Type.DELETION, MutationMention.Tool.REGEX);

            mm.setPsm(true);
            mm.setNsm(false);
            mm.setAmbiguous(false);
            result.add(mm);
        }

        m = codonDeletionPattern2.matcher(text);  //TODO
        while(m.find()){
            Map<String, String> map = MutationFinder.populateAminoAcidThreeToOneLookupMap;
            int start = m.start(2);
            int end   = m.start(2)+m.group("group").length();//m.end(m.groupCount());
            MutationMention mm = new MutationMention(start, end, text.substring(start, end), "p.", m.group("pos"),  map.get(m.group("wt").toUpperCase()), null, Type.DELETION, MutationMention.Tool.REGEX);

            mm.setPsm(true);
            mm.setNsm(false);
            mm.setAmbiguous(false);
            result.add(mm);
        }


        m = ivsPattern.matcher(text);
        while(m.find()){
            int start = m.start(2);
            int end   = m.start(2)+m.group("group").length();//m.end(m.groupCount());
            MutationMention mm = new MutationMention(start, end, text.substring(start, end), "c.", m.group("pos"),  m.group("wt"), m.group("mut"), Type.SUBSTITUTION, MutationMention.Tool.REGEX);

            mm.setPsm(false);
            mm.setNsm(true);
            mm.setAmbiguous(false);
            result.add(mm);
        }


        m = dnaInsertionPattern.matcher(text);
        while(m.find()){
            int start = m.start(2);
            int end   = m.start(2)+m.group("group").length();//m.end(m.groupCount());
            MutationMention mm = new MutationMention(start, end, text.substring(start, end), "c.", m.group("pos"),  null, m.group("mut"), Type.INSERTION, MutationMention.Tool.REGEX);

            mm.setPsm(false);
            mm.setNsm(true);
            mm.setAmbiguous(false);
            result.add(mm);
        }

        m = frameshiftPattern.matcher(text);
        while(m.find()){
            int start = m.start(2);
            int end   = m.start(2)+m.group("group").length();//m.end(m.groupCount());
            MutationMention mm = new MutationMention(start, end, text.substring(start, end), null, m.group("pos"),  m.group("wt"), m.group("mut"), Type.FRAMESHIFT, MutationMention.Tool.REGEX);
            result.add(mm);
        }

        return result;
    }

}
