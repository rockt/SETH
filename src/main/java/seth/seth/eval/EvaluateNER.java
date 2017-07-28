package seth.seth.eval;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: philippe
 * Date: 21.12.12
 * Time: 11:12
 * Evaluates prediction results of SETH on the corpus consisting of 210 articles from human mutation   and the XY articles from ?? journal
 *
 */
class EvaluateNER {

    public static void main(String[] args) throws IOException {

        if(args.length != 3)
            printErrorMessage();

        String predictFile=args[0];
        String yearFile = args[1];
        String goldFolder = args[2];

        Map<Integer, List<Entity>> predictMap = readPredictions(predictFile);
        Map<Integer, Integer>   yearMap  =   readYearFile(yearFile);
        Map<Integer, List<Entity>> goldstandardMap =  readGoldStandard(goldFolder);
        
        System.err.println("Corpus description: " +goldFolder);
        System.err.println(goldstandardMap.keySet().size() +" documents");
        int sum = 0;
        for(int pmid : goldstandardMap.keySet()){
            sum+= goldstandardMap.get(pmid).size();
        }
        System.err.println(sum +" entities");


        Map<Integer, Performance> mfPerformance = new HashMap<Integer, Performance>(21);
        Map<Integer, Performance> sethPerformance = new HashMap<Integer, Performance>(21);
        Map<Integer, Performance> regexPerformance = new HashMap<Integer, Performance>(21);
        Map<Integer, Performance> dbSNPPerformance = new HashMap<Integer, Performance>(21);

        for(int year : yearMap.values()){
            mfPerformance.put(year, new Performance());
            sethPerformance.put(year, new Performance());
            regexPerformance.put(year, new Performance());
            dbSNPPerformance.put(year, new Performance());
        }

        Performance performance = new Performance();

        for(int pmid : goldstandardMap.keySet()){
            List<Entity> predicted = predictMap.get(pmid);
            List<Entity> goldstandard = goldstandardMap.get(pmid);

            if(predicted != null){
                for(Entity entity : predicted){
                    if(goldstandard.contains(entity)){
                        performance.addTP();

                        if(entity.getTool().equals("MF"))
                            mfPerformance.get(yearMap.get(pmid)).addTP();
                        else if(entity.getTool().equals("SETH"))
                            sethPerformance.get(yearMap.get(pmid)).addTP();
                        else if(entity.getTool().equals("REGEX"))
                            regexPerformance.get(yearMap.get(pmid)).addTP();
                        else if(entity.getTool().equals("DBSNP"))
                            dbSNPPerformance.get(yearMap.get(pmid)).addTP();
                        else
                            throw new RuntimeException("Unknown tool " +entity.getTool());

                        goldstandard.remove(entity);
                    }
                    else{
                        performance.addFP();
                        System.out.println("FP" +pmid +" " +entity);

                        if(entity.getTool().equals("MF"))
                            mfPerformance.get(yearMap.get(pmid)).addFP();
                        else if(entity.getTool().equals("SETH"))
                            sethPerformance.get(yearMap.get(pmid)).addFP();
                        else if(entity.getTool().equals("REGEX"))
                            regexPerformance.get(yearMap.get(pmid)).addFP();
                        else if(entity.getTool().equals("DBSNP"))
                              dbSNPPerformance.get(yearMap.get(pmid)).addFP();
                        else
                            throw new RuntimeException("Unknown tool " +entity.getTool());
                    }
                }
            }
            performance.addFN(goldstandard.size());
            for(Entity entity : goldstandard){
                System.out.println("FN" +pmid +" " +entity);
            }
        }
        

        performance.calculate();
        DecimalFormat df = new DecimalFormat( "0.000" );
        System.err.print("Precision= " +df.format(performance.getPrecision()) +"; ");
        System.err.print("Recall= " +df.format(performance.getRecall()) +"; ");
        System.err.println("F1= " +df.format(performance.getF1()));


        List<Integer> years = new ArrayList<Integer>(mfPerformance.keySet()) ;
        Collections.sort(years);


        BufferedWriter bw = new BufferedWriter(new FileWriter(new File("result.tsv")));
        for(int year : years){
            bw.append(String.valueOf(year)).append("\t").append(String.valueOf(mfPerformance.get(year).getTP())).append("\t").append("MF\n");
            bw.append(String.valueOf(year)).append("\t").append(String.valueOf(sethPerformance.get(year).getTP())).append("\t").append("SETH\n");
            bw.append(String.valueOf(year)).append("\t").append(String.valueOf(regexPerformance.get(year).getTP())).append("\t").append("REGEX\n");
            bw.append(String.valueOf(year)).append("\t").append(String.valueOf(dbSNPPerformance.get(year).getTP())).append("\t").append("DBSNP\n");
        }
        bw.close();


    }
    private static Map<Integer, Integer > readYearFile(String file) throws IOException {
        Map<Integer, Integer> result = new HashMap<Integer, Integer>();

        BufferedReader br = new BufferedReader(new FileReader(new File(file)));

        while(br.ready()){
            String array [] = br.readLine().split("\t");

            if(array.length != 2)
                throw new RuntimeException("Error in" + Arrays.toString(array));

            int year = Integer.parseInt(array[0]);
            int pmid = Integer.parseInt(array[1]);


            if(result.containsKey(pmid))
                throw new RuntimeException("Duplicate pmid " +pmid);

            result.put(pmid, year);
        }
        br.close();

        System.out.println(result.size() +" year mappings loaded");

        return result;
    }

    /**
     * Loads SETH predictions
     * @return Predicted Entities
     * @throws IOException
     */
    public static Map<Integer, List<Entity>> readPredictions(String corpusResult) throws IOException {
        System.out.println("Reading predictions from " +corpusResult);
        Map<Integer, List<Entity>> entityMap = new HashMap<Integer, List<Entity>>();

        BufferedReader br = new BufferedReader(new FileReader(new File(corpusResult)));
        while(br.ready()){
            String array[] = br.readLine().split("\t");
            int pmid = Integer.parseInt(array[0]);

            Entity entity = new Entity("", "SNP", Integer.parseInt(array[1]), Integer.parseInt(array[2]), array[3], array[4]);

            if(entityMap.containsKey(pmid))
                entityMap.get(pmid).add(entity);
            else{
                List<Entity> tmpList = new ArrayList<Entity>();
                tmpList.add(entity);
                entityMap.put(pmid, tmpList);
            }
        }
        br.close();

        int sum=0;
        for(int pmid : entityMap.keySet()){
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
    public static Map<Integer, List<Entity>> readGoldStandard(String goldFolder) throws IOException {
        System.out.println("Reading goldstandard annotations from " +goldFolder);

        File folder = new File(goldFolder);
        if(folder.isFile())
            throw new RuntimeException("Provide folder");

        Map<Integer, List<Entity>> entityMap = new HashMap<Integer, List<Entity>>();

        for(File file : folder.listFiles()){
            if(!file.getName().endsWith("ann"))
                continue;

            int pmid = Integer.parseInt(file.getName().substring(0,file.getName().indexOf(".")));

            BufferedReader br = new BufferedReader(new FileReader(file));
            while (br.ready()){
                String line = br.readLine();
                String array[] = line.split("\t");

                String annotation [] = array[1].split(" ");

                if(annotation[0].equals("SNP") || annotation[0].equals("mutation")){
                    Entity entity = new Entity(array[0], annotation[0], Integer.parseInt(annotation[1]), Integer.parseInt(annotation[2]), array[2], "goldstandard");

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
        for(int pmid : entityMap.keySet()){
            sum+= entityMap.get(pmid).size();
        }

        System.out.println(entityMap.size() +" articles with " +sum +" annotations loaded");

        return entityMap;
    }

    private static void printErrorMessage(){
        System.err.println("ERROR: Invalid number of input parameters. Execution requires three input parameters.");
        System.err.println("PARAMETERS:  goldFolder SETH-prediction yearMapping");
        System.exit(1);
    }


}
