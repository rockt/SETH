package seth.seth.eval;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: philippe
 * Date: 15.04.13
 * Time: 13:58
 * To change this template use File | Settings | File Templates.
 */
public class EvaluateVerspoor {

    public static void main(String[] args) throws IOException {

        if(args.length != 2)
            printErrorMessage();

        String goldstandardFile  = args[0];
        String predictFile = args[1];


        Map<String, List<Entity>> predictMap = readPredictions(predictFile);
        Map<String, List<Entity>> goldstandardMap =  readGoldStandard(goldstandardFile);

        Performance performance = new Performance();
        for(String pmid : goldstandardMap.keySet()){

            List<Entity> predicted = predictMap.get(pmid);
            List<Entity> goldstandard = goldstandardMap.get(pmid);

            if(predicted != null){
                for(Entity entity : predicted){
                    if(goldstandard.contains(entity)){
                        performance.addTP();

                        goldstandard.remove(entity);
                    }
                    else{
                        performance.addFP();
                        //System.out.println("FP" +pmid +" " +entity);
                    }
                }
            }
            performance.addFN(goldstandard.size());
            for(Entity entity : goldstandard){
               System.out.println("FN" +pmid +" " +entity);
            }

        }

        performance.calculate();
        DecimalFormat df = new DecimalFormat( "0.00" );
        System.err.println("Precision " +df.format(performance.getPrecision()));
        System.err.println("Recall " +df.format(performance.getRecall()));
        System.err.println("F1 " +df.format(performance.getF1()));

    }


    /**
     * Loads SETH predictions
     * @return Predicted Entities
     * @throws IOException
     */
    private static Map<String, List<Entity>> readPredictions(String corpusResult) throws IOException {
        System.out.println("Reading predictions from " +corpusResult);
        Map<String, List<Entity>> entityMap = new HashMap<String, List<Entity>>();

        BufferedReader br = new BufferedReader(new FileReader(new File(corpusResult)));
        while(br.ready()){
            String array[] = br.readLine().split("\t");
            String pmid = array[0];

            Entity entity = new Entity("", "SNP", Integer.parseInt(array[1]), Integer.parseInt(array[2]), array[3], array[4]);

            if(entityMap.containsKey(pmid))
                entityMap.get(pmid).add(entity);
            else{
                List<Entity> tmpList = new ArrayList<Entity>();
                tmpList.add(entity);
                entityMap.put(pmid, tmpList);
            }

        }

        int sum=0;
        for(String pmid : entityMap.keySet()){
            sum+= entityMap.get(pmid).size();
        }

        System.out.println(entityMap.size() +" articles with " +sum +" predictions loaded");

        return entityMap;
    }


    /**
     * Loads goldstandard annotations
     * @return  Goldstandard entities
     * @throws IOException
     */
    public static Map<String, List<Entity>> readGoldStandard(String goldFolder) throws IOException {
        System.out.println("Reading goldstandard annotations from " +goldFolder);

        File folder = new File(goldFolder);
        if(folder.isFile())
            throw new RuntimeException("Provide folder");

        Map<String, List<Entity>> entityMap = new HashMap<String, List<Entity>>();

        for(File file : folder.listFiles()){
            if(!file.getName().endsWith("ann"))
                continue;

            String pmid = file.getName().substring(0,file.getName().indexOf("."));

            BufferedReader br = new BufferedReader(new FileReader(file));
            while (br.ready()){
                String line = br.readLine();
                String array[] = line.split("\t");

                String annotation [] = array[1].split(" ");

                if(annotation[0].equals("SNP") || annotation[0].equals("mutation")){
                    Entity entity = new Entity(array[0], "SNP", Integer.parseInt(annotation[1]), Integer.parseInt(annotation[2]), array[2], "goldstandard");

                    if(entityMap.containsKey(pmid))
                        entityMap.get(pmid).add(entity);

                    else{
                        List<Entity> tmpList = new ArrayList<Entity>();
                        tmpList.add(entity);
                        entityMap.put(pmid, tmpList);
                    }

                }
            }
            if(!entityMap.containsKey(pmid))
                entityMap.put(pmid, new ArrayList<Entity>());

        }

        int sum=0;
        for(String pmid : entityMap.keySet()){
            sum+= entityMap.get(pmid).size();
        }

        System.out.println(entityMap.size() +" articles with " +sum +" annotations loaded");

        return entityMap;
    }

    private static void printErrorMessage(){
        System.err.println("ERROR: Invalid number of input parameters. Execution requires two input parameters.");
        System.err.println("PARAMETERS:  goldstandard-folder SETH-prediction");
        System.exit(1);
    }
}
