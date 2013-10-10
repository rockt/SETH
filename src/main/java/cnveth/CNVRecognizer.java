package cnveth;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import de.hu.berlin.wbi.objects.MutationMention;
import seth.ner.wrapper.Type;


public class CNVRecognizer {

    final private static String prefix = "(^|[\\.\\s\\(\\[\\'\"/,\\-])";
    final private static String cnvKeywords = "(copy number variation|CNV|amplification|polymorphism|increased copy number)";
    final private static String preposition = "(of|on|in)";
    final private static String suffix = "([\\s\\(\\[\\'\"]|$)";
    final private static Pattern cnv = Pattern.compile(prefix + cnvKeywords + "s?" + " " + preposition + suffix, Pattern.CASE_INSENSITIVE);

    public List<MutationMention> extractMutations(String text) {
        List<MutationMention> result = new ArrayList<MutationMention>();

        Matcher matcher = cnv.matcher(text);
        System.out.println("TEXT: "+text);
        while (matcher.find()){

            int begin = matcher.start(2);
            int end = matcher.end(3);
            //System.out.println("groups:"+matcher.groupCount()+" begin: "+begin+" end:"+end+"\n");

            MutationMention mm = new MutationMention(begin, end, text.substring(begin, end), null, null, null, null, Type.COPY_NUMBER_VARIATION, MutationMention.Tool.CNVETH);
            result.add(mm);
        }

        return result;
    }


}