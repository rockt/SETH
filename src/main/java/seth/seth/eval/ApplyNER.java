package seth.seth.eval;

import seth.SETH;
import de.hu.berlin.wbi.objects.MutationMention;
import java.io.*;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: philippe
 * Date: 13.12.12
 * Time: 15:07
 * To change this template use File | Settings | File Templates.
 *
 * Can be used to apply SETH on both corpora (MutationFinder and the human mutation corpus consisting of 210 abstracts)
 * Input corpora are both in the same format, but output is slightly different, as the MutationFinder corpus
 * provides no information about the actual position of a mutation in the text
 */
public class ApplyNER {

    public static void main(String[] args) throws IOException {

        if(args.length != 4)
            printErrorMessage();

        String corpusFile = args[0];
        String regexFile = args[1];
        boolean mutationFinderFormat = Boolean.parseBoolean(args[2]);
        String annotationOutput = args[3];

        SETH seth = new SETH(regexFile, true, !mutationFinderFormat);
        File file = new File(corpusFile);
        if(!file.isFile())
            throw new RuntimeException("Provide corpus-file, instead of folder");

        BufferedReader br = new BufferedReader(new FileReader(file));
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(annotationOutput)));
        while(br.ready()){
            String line =  br.readLine();

            int firstTabIndex = line.indexOf("\t");
            String pmid = line.substring(0, firstTabIndex);
            String text = line.substring(firstTabIndex + 1);

            if(mutationFinderFormat){
                bw.append(pmid);
            }

            List<MutationMention> mutations = seth.findMutations(text);
            for(MutationMention mutation : mutations){

                if(mutationFinderFormat){
                    bw.append("\t" +mutation.getWtResidue() +mutation.getPosition() +mutation.getMutResidue());
                }

                else{
                    if(mutation.getTool().equals(MutationMention.Tool.SETH))
                        bw.append(pmid +"\t" +mutation.getStart() +"\t" +mutation.getEnd() +"\t" +mutation.getText() +"\t" +"SETH");

                    else if(mutation.getTool().equals(MutationMention.Tool.MUTATIONFINDER))
                        bw.append(pmid +"\t" +mutation.getStart() +"\t" +mutation.getEnd() +"\t" +mutation.getText() +"\t" +"MF");

                    else if(mutation.getTool().equals(MutationMention.Tool.REGEX))
                        bw.append(pmid +"\t" +mutation.getStart() +"\t" +mutation.getEnd() +"\t" +mutation.getText() +"\t" +"REGEX");

                    else
                        throw new RuntimeException();

                    bw.append("\n");
                }
            }

            if(mutationFinderFormat)
                bw.append("\n");
        }
        br.close();
        bw.close();
    }

    private static void printErrorMessage(){
        System.err.println("ERROR: Invalid number of input parameters. Execution requires four input parameters.");
        System.err.println("PARAMETERS:  corpusFile regexFile format outputFile");
        System.exit(1);
    }
}
