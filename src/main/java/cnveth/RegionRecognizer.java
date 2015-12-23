package cnveth;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;
import java.lang.String;
import java.io.BufferedReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* this class allows to detect mentions of genomic regions in texts
   (only regions from a list containing all relevant regions)
   source of the list: http://www.genenames.org/cgi-bin/hgnc_downloads
   custom download containing e.g. entrez gene id, chromosome,
   hgnc id, locus group, locus type, synonyms
 */
public class RegionRecognizer{

    private String file = "resources/regions/chromosomalRegions.txt";

    final private static String prefix = "(^|[\\.\\s\\(\\['\"/,\\-])";
    // region description starts with a number between 1 and 22 or with the letters Y or X (all possible chromosomes)
    final private static String startLetters = "([YX][pq][1-9\\.\\-]+[A-Za-z0-9\\.\\-]+|[1-9][pq][1-9\\.\\-]+[A-Za-z0-9\\.\\-]+|1[0-9][pq][1-9\\.\\-]+[A-Za-z0-9\\.\\-]+|2[0-2][pq][1-9\\.\\-]+[A-Za-z0-9\\.\\-]+)";
    final private static Pattern region = Pattern.compile(prefix + startLetters);

    private HashMap<String, ArrayList<String>> region2genes;

    public RegionRecognizer() throws IOException{
        region2genes = this.readRegions();
    }

    public RegionRecognizer(String file) throws IOException{
        this.file = file;
        region2genes = this.readRegions();
    }


    /** function to read chromosomal regions and associated genes into a hash */
    public HashMap<String, ArrayList<String>> readRegions() throws IOException{

        /** store chromosomal locations (as key) with all associated genes (list of entrez IDs as value) */
        HashMap<String, ArrayList<String>> locations = new HashMap<String, ArrayList<String>>();

        BufferedReader regionFile = new BufferedReader(new FileReader(file));
        regionFile.readLine(); /** skip header line */

        String regionLine;

        while((regionLine = regionFile.readLine()) != null){

            String[] line = regionLine.split("\t", -1);
            // chromosomal region
            String loc = line[7];
            if(loc.length() == 0){continue;} // continue if location field is empty
            // associated gene
            String gene = line[8];

            // case: new region
            if(!locations.containsKey(loc)){
                ArrayList<String> genelist = new ArrayList<String>();
                if(gene != null){
                    genelist.add(gene);
                }
                locations.put(loc, genelist);
            }
            // case: more genes for existing region
            else if ((gene.length() != 0)) {
                ArrayList<String> genelist = locations.get(loc);
                genelist.add(gene);
                locations.put(loc, genelist);
            }
            else continue;
        }

//        System.out.println("All genes on loaction 20q13.33: " + locations.get("20q13.33"));
//        System.out.println("Number of chromosomal regions: " + locations.size());
//        System.out.println("Number of genes on chromosomal region 20q13.33: " + locations.get("20q13.33").size());


        return locations;
    }

    public ArrayList<RegionMention> extractRegion(String text){

        ArrayList<RegionMention> regionMentions = new ArrayList<RegionMention>();

        Matcher matcher = region.matcher(text);
        while (matcher.find()){

            String reg = matcher.group(2);
            System.out.println("region:" + reg + "*");
            int begin = matcher.start(2);
            int end = begin + reg.length() - 1;

            if(region2genes.containsKey(reg)){
                System.out.println("region detected!");
                ArrayList<String> genes = region2genes.get(reg);
                RegionMention rm = new RegionMention(reg, begin, end, genes);
                //System.out.println(rm);
                regionMentions.add(rm);
            }
        }

        return regionMentions;
    }

    public static void main(String[] args) throws IOException {

       RegionRecognizer regrec = new RegionRecognizer();
        ArrayList<RegionMention> regionMentions = regrec.extractRegion("22q13.1 blabla 3p22-p21.33");
        System.out.println(regionMentions);
    }
}
