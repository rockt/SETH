package seth;

import de.hu.berlin.wbi.objects.MutationMention;
import seth.ner.wrapper.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: philippe
 * Date: 11.04.13
 * Time: 16:01
 *
 * This class can be used to find mutation mentions (deletions, IVS-substitions, insertions, and frameshifts)
 * written in  deprecated nomenclature
 */
public class OldNomenclature {

    final private static String prefix="(^|[\\s\\(\\)\\[\\'\"/,])"; //>
    final private static String suffix="(?=([\\.,\\s\\)\\(\\]\\'\":;\\-/]|$))";//|:?[ATGC]>[ATGC]
    //final private static Pattern dbSNP = Pattern.compile(prefix +"(rs[1-9][0-9]*)" +suffix);

    //852delA, 852delAT, 852del, 852del22, 852-123delA, 852+123delT
    final private static Pattern dnaDeletionPattern = Pattern.compile(prefix +"(?<group>(?<pos>[+-]?[1-9][0-9]*(?:\\s?[+-_]\\s?[1-9][0-9]*)?)\\s?(del)\\s?(?<wt>([ATGC]|[0-9])+))" +suffix);
    //IVS123+12A->T, IVS123-12A->T
    final private static Pattern ivsPattern = Pattern.compile(prefix +"(?<group>(?<pos>IVS[-]?[1-9][0-9]*\\s?[+-]\\s?[1-9][0-9]*)\\s?(?<wt>[ATGC])\\s?(?:-{0,2}>|→|/|\\\\)\\s?(?<mut>[ATGC]))" +suffix);

    //F123del, Phe123del
    final private static Pattern protDeletionPattern = Pattern.compile(prefix +"(?<group>(?<wt>[CISQMNPKDTFAGHLRWVEYBZJX])(?<pos>[1-9][0-9]*)del)" +suffix);
    final private static Pattern protDeletionPatternLong = Pattern.compile(prefix +"(?<group>(?<wt>CYS|ILE|SER|GLN|MET|ASN|PRO|LYS|ASP|THR|PHE|ALA|GLY|HIS|LEU|ARG|TRP|VAL|GLU|TYR)(?<pos>[1-9][0-9]*)del)" +suffix, Pattern.CASE_INSENSITIVE);

    //deltaF508, DeltaF508,
    final private static Pattern codonDeletionPattern = Pattern.compile(prefix +"(?<group>([dD]elta|Δ)(?<wt>[CISQMNPKDTFAGHLRWVEYBZJX])(?<pos>[1-9][0-9]*))" +suffix);
    //final private static Pattern codonDeletionPattern2 = Pattern.compile(prefix +"(?<group>([dD]elta|Δ)(?<pos>[1-9][0-9]*))" +suffix);      //delta123  (too unspecific for us to normalize)


    //852insA, 852insAT, 852ins, 852ins22, 852-123insA, 852+123insT
    final private static Pattern dnaInsertionPattern = Pattern.compile(prefix +"(?<group>(?<pos>[+-]?[1-9][0-9]*(?:\\s?[+-_]\\s?[1-9][0-9]*)?)\\s?(ins)\\s?(?<mut>([ATGC]|[0-9])+))" +suffix);

    //A123fsX1,
    final private static Pattern frameshiftPattern = Pattern.compile(prefix +"(?<group>(?<wt>[CISQMNPKDTFAGHLRWVEYBZJX])(?<pos>[1-9][0-9]*)(fs)(?<mut>X[0-9]*)?)" +suffix);

    /**
     * Extracts mentions of mutations from natural language text
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
            result.add(mm);
        }

        m = protDeletionPattern.matcher(text);
        while(m.find()){
            int start = m.start(2);
            int end   = m.start(2)+m.group("group").length();//m.end(m.groupCount());
            MutationMention mm = new MutationMention(start, end, text.substring(start, end), "p.", m.group("pos"),  m.group("wt"), null, Type.DELETION, MutationMention.Tool.REGEX);
            result.add(mm);
        }
        m = protDeletionPatternLong.matcher(text);
        while(m.find()){
            int start = m.start(2);
            int end   = m.start(2)+m.group("group").length();//m.end(m.groupCount());
            MutationMention mm = new MutationMention(start, end, text.substring(start, end), "p.", m.group("pos"),  m.group("wt"), null, Type.DELETION, MutationMention.Tool.REGEX);
            result.add(mm);
        }

        m = codonDeletionPattern.matcher(text);
        while(m.find()){
            int start = m.start(2);
            int end   = m.start(2)+m.group("group").length();//m.end(m.groupCount());
            MutationMention mm = new MutationMention(start, end, text.substring(start, end), "p.", m.group("pos"),  m.group("wt"), null, Type.DELETION, MutationMention.Tool.REGEX);
            result.add(mm);
        }


        /**
        m = codonDeletionPattern2.matcher(text);
        while(m.find()){
            int start = m.start(2);
            int end   = m.start(2)+m.group("group").length();//m.end(m.groupCount());
            MutationMention mm = new MutationMention(start, end, text.substring(start, end), "p.", m.group("pos"),  null, null, Type.DELETION, MutationMention.Tool.REGEX);
            result.add(mm);
        }
        */

        m = ivsPattern.matcher(text);
        while(m.find()){
            int start = m.start(2);
            int end   = m.start(2)+m.group("group").length();//m.end(m.groupCount());
            MutationMention mm = new MutationMention(start, end, text.substring(start, end), "p.", m.group("pos"),  m.group("wt"), m.group("mut"), Type.SUBSTITUTION, MutationMention.Tool.REGEX);
            result.add(mm);
        }


        m = dnaInsertionPattern.matcher(text);
        while(m.find()){
            int start = m.start(2);
            int end   = m.start(2)+m.group("group").length();//m.end(m.groupCount());
            MutationMention mm = new MutationMention(start, end, text.substring(start, end), "c.", m.group("pos"),  null, m.group("mut"), Type.INSERTION, MutationMention.Tool.REGEX);
            result.add(mm);
        }

        m = frameshiftPattern.matcher(text);
        while(m.find()){
            int start = m.start(2);
            int end   = m.start(2)+m.group("group").length();//m.end(m.groupCount());
            MutationMention mm = new MutationMention(start, end, text.substring(start, end), "c.", m.group("pos"),  m.group("wt"), m.group("mut"), Type.FRAMESHIFT, MutationMention.Tool.REGEX);
            result.add(mm);
        }

        return result;
    }

}
