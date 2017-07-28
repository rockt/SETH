package seth.seth.eval;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: philippe
 * Date: 10.04.13
 * Time: 19:46
 * To change this template use File | Settings | File Templates.
 */
public class EvaluateWei {

	public static void main(String[] args) throws IOException {

		if(args.length != 2)
			printErrorMessage();

		String goldstandardFile = args[0];
		String predictFile = args[1];


		Map<Integer, List<Entity>> predictMap = EvaluateNER.readPredictions(predictFile);
		Map<Integer, List<Entity>> goldstandardMap =  readGoldStandard(goldstandardFile);

		System.err.println("Corpus description: " +goldstandardFile);
		System.err.println(goldstandardMap.keySet().size() +" documents");
		int sum = 0;
		for(int pmid : goldstandardMap.keySet()){
			sum+=goldstandardMap.get(pmid).size();
		}
		System.err.println(sum +" entities");

		Set<Integer> allPmids = new HashSet<Integer>(goldstandardMap.keySet());
		allPmids.addAll(goldstandardMap.keySet());

		Performance performance = new Performance();
		for(int pmid : allPmids){
			List<Entity> predicted = predictMap.get(pmid);
			List<Entity> goldstandard = goldstandardMap.get(pmid);

			boolean linebreak=false;
			if(predicted != null){
				for(Entity entity : predicted){
					if(goldstandard.contains(entity)){
						performance.addTP();
						goldstandard.remove(entity);
					}

					else{
						performance.addFP();
						System.out.println("FP" +pmid +" " +entity);
						linebreak = true;
					}
				}       // FN19276632
			}
			performance.addFN(goldstandard.size());
			for(Entity entity : goldstandard){
				System.out.println("FN" +pmid +" " +entity);
				linebreak = true;
			}
			if(linebreak)
				System.out.println("------------------------");
		}


		performance.calculate();
		DecimalFormat df = new DecimalFormat( "0.000" );
		System.err.println("TP = " +performance.getTP() +"; FP=" +performance.getFP() +"; FN=" +performance.getFN());

		System.err.print("Precision= " +df.format(performance.getPrecision()) +"; ");
		System.err.print("Recall= " +df.format(performance.getRecall()) +"; ");
		System.err.println("F1= " +df.format(performance.getF1()));
	}

	/**
	 * Loads goldstandard annotations
	 * @return  Goldstandard entities
	 * @throws IOException
	 */
	public static Map<Integer, List<Entity>> readGoldStandard(String goldFolder) throws IOException {
		System.out.println("Reading goldstandard annotations from " +goldFolder);
		Map<Integer, List<Entity>> entityMap = new HashMap<Integer, List<Entity>>();

		Pattern p = Pattern.compile("^[1-9][0-9]+");

		BufferedReader br = new BufferedReader(new FileReader(goldFolder));
		loop:while(br.ready()){
			String line = br.readLine();
			Matcher m = p.matcher(line);

			if(m.find()){				
				int pmid = Integer.parseInt(m.group().trim());
				
				if(line.charAt(m.end()) == '|'){
					entityMap.put(pmid, new ArrayList<Entity>());
				}
				else{
					String [] array = line.split("\t");
				
					if(array.length != 6)
						throw new RuntimeException("Unexpected number of arrays!");

					Entity entity = new Entity("IDx", "SNP", Integer.parseInt(array[1]), Integer.parseInt(array[2]), array[3], "goldstandard");
					entityMap.get(pmid).add(entity);	
				}				
			}
		}
		br.close();


		return  entityMap;
	}

	private static void printErrorMessage(){
		System.err.println("ERROR: Invalid number of input parameters. Execution requires two input parameters.");
		System.err.println("PARAMETERS:  goldstandard SETH-prediction");
		System.exit(1);
	}
}
