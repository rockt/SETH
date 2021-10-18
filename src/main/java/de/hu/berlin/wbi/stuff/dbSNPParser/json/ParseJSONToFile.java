package de.hu.berlin.wbi.stuff.dbSNPParser.json;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class ParseJSONToFile {

    public static void main(String[] args) throws IOException, CompressorException {

        String jsonFolder = "/media/philippe/Elements/SETH/dbSNP/";
        String outFolder = "";
        if (args.length == 1 || args.length == 2)
            jsonFolder = args[0];

        if(args.length == 2)
            outFolder = args[1];

        BufferedWriter mergeItemWriter = new BufferedWriter(new OutputStreamWriter( new FileOutputStream(new File(outFolder+"mergeItems.tsv")), "UTF-8"));
        BufferedWriter citationItemWriter = new BufferedWriter(new OutputStreamWriter( new FileOutputStream(new File(outFolder+"citations.tsv")), "UTF-8"));
        BufferedWriter hgvsWriter = new BufferedWriter(new OutputStreamWriter( new FileOutputStream(new File(outFolder+"hgvs.tsv")), "UTF-8"));
        BufferedWriter psmWriter = new BufferedWriter(new OutputStreamWriter( new FileOutputStream(new File(outFolder+"PSM.tsv")), "UTF-8"));

        System.err.println("Extracting data from the following file/folder '" +jsonFolder +"'");
        System.err.println("Writing data to folder '" +outFolder+"'");



        File[] files = new File(jsonFolder).listFiles();
        Set<String> temp = new HashSet<>();
        Arrays.sort(files);
        for (File file : files) {
            if (!file.getAbsolutePath().endsWith(".bz2"))
                continue;

            System.out.println("Parsing " + file.getAbsolutePath());

            FileInputStream fin = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fin);
            CompressorInputStream input = new CompressorStreamFactory().createCompressorInputStream(bis);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(input));

            String line = bufferedReader.readLine();
            while(line != null){


                try{
                    final JSONObject obj = new JSONObject(line);
                    final int rsID = obj.getInt("refsnp_id");

                    if(rsID == 334){
                        //System.out.println(line);
                    }

                    //Step 1: Analyze all merged items
                    final JSONArray merges = obj.getJSONArray("dbsnp1_merges");
                    if(rsID == 3034552 || rsID==3034557){
                        System.out.println("rsId=" +rsID);
                        System.out.println(merges);
                        System.out.println("-----");
                    }
                    for (int i = 0, size = merges.length(); i < size; i++) {
                        JSONObject merge = merges.getJSONObject(i); //{"merged_rsid":"2557863","revision":"130","merge_date":"2008-05-24T00:59Z"}
                        mergeItemWriter.append(rsID +"\t" +merge.getInt("merged_rsid") +"\t" +merge.getInt("revision") +"\n"); //newSNP oldSNP dbSNP-Version
                    }

                    //Step 2: Analyze all citations
                    final JSONArray citations = obj.getJSONArray("citations"); // A set of integers
                    for (int i = 0, size = citations.length(); i < size; i++) {
                        final int  citation = citations.getInt(i);
                        citationItemWriter.append(rsID +"\t" +citation +"\n");
                    }


                    Set<String> hgvsElements = new HashSet<>();
                    Set<String> psmElements = new HashSet<>();
                    final JSONArray allele_annotations = obj.getJSONObject("primary_snapshot_data").getJSONArray("allele_annotations");
                    for (int i = 0, size = allele_annotations.length(); i < size; i++) {
                        final JSONObject  allele_annotation = allele_annotations.getJSONObject(i);
                        //System.out.println(allele_annotation);

                            final JSONArray assembly_annotations = allele_annotation.getJSONArray("assembly_annotation");
                            for (int j = 0; j< assembly_annotations.length(); j++) {


                                
                                if(assembly_annotations.getJSONObject(j).has("genes")){
                                    final JSONArray  genes = assembly_annotations.getJSONObject(j).getJSONArray("genes");

                                    for(int k =0; k < genes.length(); k++){
                                        final JSONObject  gene = genes.getJSONObject(k);
                                        //System.out.println(gene);

                                        final int entrez = gene.getInt("id");
                                        final JSONArray rnas = gene.getJSONArray("rnas");

                                        for (int l =0; l < rnas.length(); l++){
                                            final JSONObject  rna = rnas.getJSONObject(l);

                                            if(rna.has("hgvs")) {
                                                final String hgvs = rna.getString("hgvs");
                                                String split[] = hgvs.split(":");

                                                //See http://www.ncbi.nlm.nih.gov/books/NBK21091/table/ch18.T.refseq_accession_numbers_and_mole/?report=objectonly
                                                //Skip XM, XR, and XP, which are are automatically derived annotation pipelines
                                                //NC_ NM_ NG_ NR_ NP_ NT_ NW_
                                                if(split[0].startsWith("XM_") || split[0].startsWith("XR_") || split[0].startsWith("XP_") || split[0].startsWith("GPC_") ||split[0].startsWith("YP_"))
                                                    continue;

                                                if (split.length != 2) {
                                                    System.out.println("Split size " + split.length + " instead of 2 for '" + hgvs + "'");
                                                    System.out.println("rs" +rsID);
                                                    continue;
                                                }
                                                hgvsElements.add(entrez+"\t" +rsID +"\t" +split[1] +"\t" +split[0]);
                                                //hgvsWriter.append(entrez+"\t" +rsID +"\t" +split[0] +"\t" +split[1] +"\n"); //Locus dbSNP hgvs refseq
                                            }

                                            if(rna.has("protein")) {

                                                if(rna.getJSONObject("protein").getJSONObject("variant").has("spdi")){
                                                    final JSONObject spdi = rna.getJSONObject("protein").getJSONObject("variant").getJSONObject("spdi");
                                                    if(rsID == 334){
                                                        System.out.println(spdi);
                                                    }

                                                    int pos = 1 + spdi.getInt("position");
                                                    String wildtype = spdi.getString("deleted_sequence");
                                                    String mutated =spdi.getString("inserted_sequence");
                                                    psmElements.add(rsID +"\t" +entrez +"\t" +pos +"\t" +mutated +"\t" +wildtype);
                                                    //psmWriter.append(rsID +"\t" +entrez +"\t" +pos +"\t" +mutated +"\t" +wildtype +"\n");
                                                }
                                                else{
                                                    //System.out.println(rna.getJSONObject("protein").getJSONObject("variant"));
                                                }

                                            }
                                        }
                                    }
                                }
                            }
                    }
                    for (String hgvs : hgvsElements){
                        hgvsWriter.append(hgvs);
                        hgvsWriter.append("\n");
                    }

                    for(String psm : psmElements){
                        psmWriter.append(psm);
                        psmWriter.append("\n");
                    }

                }catch(JSONException ex){ //Catch Exceptions from parsing the JSON
                    ex.printStackTrace();
                    //System.err.println(line);
                    //System.err.println("----------------");
                }



                line = bufferedReader.readLine();
            }

        }
        mergeItemWriter.close();
        citationItemWriter.close();
        hgvsWriter.close();
        psmWriter.close();
        for(String string : temp)
            System.out.println(string);
    }

}
