package de.hu.berlin.wbi.stuff.dbSNPParser.json;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class ParseJSONToFile {

    public static void main(String[] args) throws IOException, CompressorException {

        String jsonFolder = "/media/philippe/Elements/SETH/dbSNP/";
        String outFolder = "";
        if (args.length >= 1 )
            jsonFolder = args[0];

        if(args.length >= 2)
            outFolder = args[1];

        boolean writeRefSeq = true;
        if (args.length == 3 && args[2].trim().equals("--noRefSeq")) {
            writeRefSeq = false;
            System.out.println("Ignoring refseq in output");
        }

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
            CompressorInputStream input = new BZip2CompressorInputStream(bis, true);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(input));

            String line = bufferedReader.readLine();
            int nLines = 0;
            while(line != null){


                try{
                    final JSONObject obj = new JSONObject(line);
                    final int rsID = obj.getInt("refsnp_id");

                    if(rsID == 386833587){
                        System.out.println(line);
                    }

                    //Step 1: Analyze all merged items
                    final JSONArray merges = obj.getJSONArray("dbsnp1_merges");

                    for (int i = 0; i < merges.length(); i++) {
                        JSONObject merge = merges.getJSONObject(i); //{"merged_rsid":"2557863","revision":"130","merge_date":"2008-05-24T00:59Z"}
                        mergeItemWriter.append(rsID +"\t" +merge.getInt("merged_rsid") +"\t" +merge.getInt("revision") +"\n"); //newSNP oldSNP dbSNP-Version
                    }

                    //Step 2: Analyze all citations
                    final JSONArray citations = obj.getJSONArray("citations"); // A set of integers
                    for (int i = 0; i < citations.length(); i++) {
                        final int  citation = citations.getInt(i);
                        citationItemWriter.append(rsID +"\t" +citation +"\n");
                    }


                    Set<String> hgvsElements = new HashSet<>();
                    Set<String> psmElements = new HashSet<>();
                    final JSONArray allele_annotations = obj.getJSONObject("primary_snapshot_data").getJSONArray("allele_annotations");
                    for (int i = 0; i < allele_annotations.length(); i++) {
                        final JSONObject  allele_annotation = allele_annotations.getJSONObject(i);
                            //System.out.println(allele_annotation);
                            final JSONArray assembly_annotations = allele_annotation.getJSONArray("assembly_annotation");

                            if(rsID == 386833587){
                                System.out.println(assembly_annotations);
                            }

                            for (int j = 0; j< assembly_annotations.length(); j++) {
                                final JSONObject assembly_annotation = assembly_annotations.getJSONObject(j);
                                
                                if(assembly_annotation.has("genes")){
                                    final JSONArray  genes = assembly_annotation.getJSONArray("genes");

                                    for(int k =0; k < genes.length(); k++){
                                        final JSONObject  gene = genes.getJSONObject(k);
                                        final int entrez = gene.getInt("id");
                                        final JSONArray rnas = gene.getJSONArray("rnas");

                                        for (int l =0; l < rnas.length(); l++){
                                            final JSONObject  rna = rnas.getJSONObject(l);

                                            if(rna.has("hgvs")) {
                                                final String hgvs = rna.getString("hgvs");
                                                String split[] = hgvs.split(":");

                                                if (split.length != 2) {
                                                    System.out.println("Split size " + split.length + " instead of 2 for '" + hgvs + "'");
                                                    System.out.println("rs" +rsID);
                                                    continue;
                                                }

                                                //Skip extremely long HGVS-sequences -> This helps to keep the size of the database "small"
                                                if (split[1].length() <= 255){

                                                    //See http://www.ncbi.nlm.nih.gov/books/NBK21091/table/ch18.T.refseq_accession_numbers_and_mole/?report=objectonly
                                                    //Skip XM, XR, and XP, which are are automatically derived annotation pipelines
                                                    //NC_ NM_ NG_ NR_ NP_ NT_ NW_
                                                    if(split[0].startsWith("XM_") || split[0].startsWith("XR_") || split[0].startsWith("XP_") || split[0].startsWith("GPC_") ||split[0].startsWith("YP_")){ }
                                                    else{
                                                        hgvsElements.add(entrez+"\t" +rsID +"\t" +split[1] +"\t" +(writeRefSeq ? split[0] : "-"));
                                                    }
                                                }
                                            }

                                            if(rna.has("protein")) {

                                                if(rna.getJSONObject("protein").getJSONObject("variant").has("spdi")){
                                                    final JSONObject spdi = rna.getJSONObject("protein").getJSONObject("variant").getJSONObject("spdi");

                                                    if(rsID == 386833587){
                                                        System.out.println(spdi);
                                                    }

                                                    int pos = 1 + spdi.getInt("position");
                                                    String wildtype = spdi.getString("deleted_sequence");
                                                    String mutated =spdi.getString("inserted_sequence");

                                                    //Only elements below 128 length and where wiltype != mutated
                                                    if (wildtype.length() <= 128 &&  mutated.length() <= 128 && wildtype.equals(mutated) == false){
                                                        psmElements.add(rsID +"\t" +entrez +"\t" +pos +"\t" +mutated.replaceAll("\\*", "X") +"\t" +wildtype.replaceAll("\\*", "X"));
                                                    }
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
                    System.err.println(line);
                    System.err.println("----------------");
                }

                line = bufferedReader.readLine();
                nLines++;
            }
            System.out.println("Read " +nLines +" lines from " +file.getAbsolutePath());
        }
        mergeItemWriter.close();
        citationItemWriter.close();
        hgvsWriter.close();
        psmWriter.close();
        for(String string : temp)
            System.out.println(string);
    }

}
