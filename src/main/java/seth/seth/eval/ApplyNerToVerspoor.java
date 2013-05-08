package seth.seth.eval;

import de.hu.berlin.wbi.objects.MutationMention;
import seth.SETH;

import java.io.*;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: philippe
 * Date: 15.04.13
 * Time: 13:49
 *
 * This class is used to perform named entity recogntion on the corpus of Verspoor 2013 et al. 2013
 */
public class ApplyNerToVerspoor {

    public static void main(String[] args) throws IOException {

        if(args.length != 3)
            printErrorMessage();

        File inFolder = new File(args[0]);
        String regexFile = args[1];
        String outFile = args[2];

        SETH seth = new SETH(regexFile, true, true);


        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outFile)));
        for(File file : inFolder.listFiles()){

            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(file));
            while(br.ready()){
                sb.append(br.readLine());
                sb.append("\n");
            }
            br.close();

            List<MutationMention> mentionList =seth.findMutations(sb.toString());
            for(MutationMention mm : mentionList){
                String id = file.getName().substring(0,file.getName().indexOf("."));
                bw.append(id +"\t" +(mm.getStart()) +"\t" +(mm.getEnd()) +"\t" +mm.getText() +"\t" +mm.getTool() +"\n");
            }

        }
        bw.close();

    }

    private static void printErrorMessage(){
        System.err.println("ERROR: Invalid number of input parameters. Execution requires three  input parameters.");
        System.err.println("PARAMETERS:  corpusFolder regexFile outputFile");
        System.exit(1);
    }
}
