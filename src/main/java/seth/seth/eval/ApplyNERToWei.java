package seth.seth.eval;

import de.hu.berlin.wbi.objects.MutationMention;
import seth.SETH;

import java.io.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: philippe
 * Date: 10.04.13
 * Time: 18:19
 *
 * This class is used to perform named entity recogntion on the corpus of Wei et al. 2013
 */
public class ApplyNERToWei {

    private static Pattern titlePattern = Pattern.compile("^[1-9][0-9]+\\|t\\|.*");     //Pattern used to detect Title-lines
    private static Pattern abstractPattern = Pattern.compile("^[1-9][0-9]+\\|a\\|.*");  //Pattern detects abstract lines
    private static Pattern pmidPattern = Pattern.compile("^[1-9][0-9]+");  //Pattern used to extract PMID-identifiers


    public static void main(String[] args) throws IOException {

        if(args.length != 3)
            printErrorMessage();

        String inFile = args[0];
        String regexFile = args[1];
        String outFile = args[2];


        SETH seth = new SETH(regexFile, false, true);

        BufferedReader br = new BufferedReader(new FileReader(inFile));
        String title = null;     //Paper title
        String abstr = null;     //Paper abstract
        int pmid =0;

        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        while(br.ready()){
            String line = br.readLine();

            if(titlePattern.matcher(line).matches()){
                title = line;
            }

            else if(abstractPattern.matcher(line).matches()){
                abstr = line;
            }

            else if(line.trim().matches("")){
                Matcher m = pmidPattern.matcher(title);
                m.find();
                pmid = Integer.parseInt(m.group());
                if(!abstr.contains(m.group())){
                    throw new RuntimeException();
                }

                title = title.substring(m.group().length() +3);
                abstr = abstr.substring(m.group().length() +3);

                List<MutationMention> titleMutations = seth.findMutations(title);
                List<MutationMention> abstrMutations = seth.findMutations(abstr);


                //Perform named entity recognition on title
                for(MutationMention mm : titleMutations){
                    bw.append(String.valueOf(pmid)).append("\t").append(String.valueOf(mm.getStart())).append("\t").append(String.valueOf(mm.getEnd())).append("\t").append(mm.getText()).append("\t").append(String.valueOf(mm.getTool())).append("\n");
                }

                //Perform named entity recognitionon abstract
                for(MutationMention mm : abstrMutations){
                    bw.append(String.valueOf(pmid)).append("\t").append(String.valueOf(title.length() + mm.getStart() + 1)).append("\t").append(String.valueOf(title.length() + mm.getEnd() + 1)).append("\t").append(mm.getText()).append("\t").append(String.valueOf(mm.getTool())).append("\n");
                }

                title = null;
                abstr = null;
                pmid = 0;
            }
        }
        bw.close();
    }

    private static void printErrorMessage(){
        System.err.println("ERROR: Invalid number of input parameters. Execution requires three  input parameters.");
        System.err.println("PARAMETERS:  corpusFile regexFile outputFile");
        System.exit(1);
    }
}
