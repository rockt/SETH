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

        String xmlFolder = "/media/philippe/Elements/SETH/dbSNP/";
        String outFolder = "";
        if (args.length == 1 || args.length == 2)
            xmlFolder = args[0];

        if(args.length == 2)
            outFolder = args[1];

        BufferedWriter mergeItemWriter = new BufferedWriter(new OutputStreamWriter( new FileOutputStream(new File(outFolder+"mergeItems.tsv")), "UTF-8"));
        BufferedWriter citationItemWriter = new BufferedWriter(new OutputStreamWriter( new FileOutputStream(new File(outFolder+"citations.tsv")), "UTF-8"));
        BufferedWriter hgvsWriter = new BufferedWriter(new OutputStreamWriter( new FileOutputStream(new File(outFolder+"hgvs.tsv")), "UTF-8"));
        BufferedWriter psmWriter = new BufferedWriter(new OutputStreamWriter( new FileOutputStream(new File(outFolder+"PSM.tsv")), "UTF-8"));

        System.err.println("Extracting data from the following file/folder '" +xmlFolder +"'");
        System.err.println("Writing data to folder '" +outFolder+"'");



        File[] files = new File(xmlFolder).listFiles();
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
                    if(rsID == 10634176){
                        System.out.println(merges);
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

                    //Problem in the entries from placement_with_allele we do not get the locus-ID
                    //Solution: We could build a mapping from RefSeq 2 locus
                    //CUrrently we neglect these entries
                    /**
                    final JSONArray  mutations = obj.getJSONObject("primary_snapshot_data").getJSONArray("placements_with_allele");
                    for (int i = 0, size = mutations.length(); i < size; i++) {
                        final JSONObject  mutation = mutations.getJSONObject(i);
                        //System.out.println(mutation);
                        String seqID = mutation.getString("seq_id");
                        String seq_type = mutation.getJSONObject("placement_annot").getString("seq_type");
                        String mol_type = mutation.getJSONObject("placement_annot").getString("mol_type");
                        //System.out.println(seq_type +"\t" +mol_type);

                        //We ignore offsets on predicted reference sequences
                        if(seq_type.contains("pred"))
                            continue;

                        temp.add(seq_type +"\t" +mol_type);


                        final JSONArray alleles = mutation.getJSONArray("alleles");
                        for (int j = 0, sizeJ = alleles.length(); j < sizeJ; j++) {
                            final JSONObject allele = alleles.getJSONObject(j);
                            final String hgvs = allele.getString("hgvs");
                            hgvsWriter.append(rsID +"\t" +hgvs +"\n"); //locus SNP HGVS refSeq
                            if (allele.getJSONObject("allele").has("spdi")) {
                                final JSONObject spdi = allele.getJSONObject("allele").getJSONObject("spdi");
                                //System.out.println(spdi);
                            }
                        }
                    }
                     */

                    Set<String> hgvsElements = new HashSet<>();
                    Set<String> psmElements = new HashSet<>();
                    final JSONArray allele_annotations = obj.getJSONObject("primary_snapshot_data").getJSONArray("allele_annotations");
                    for (int i = 0, size = allele_annotations.length(); i < size; i++) {
                        final JSONObject  allele_annotation = allele_annotations.getJSONObject(i);

                            final JSONArray assembly_annotations = allele_annotation.getJSONArray("assembly_annotation");
                            for (int j = 0; j< assembly_annotations.length(); j++) {


                                
                                if(assembly_annotations.getJSONObject(j).has("genes")){
                                    final JSONArray  genes = assembly_annotations.getJSONObject(j).getJSONArray("genes");

                                    for(int k =0; k < genes.length(); k++){
                                        final JSONObject  gene = genes.getJSONObject(k);

                                        final int entrez = gene.getInt("id");
                                        final JSONArray rnas = gene.getJSONArray("rnas");

                                        for (int l =0; k < rnas.length(); k++){
                                            final JSONObject  rna = rnas.getJSONObject(l);

                                            if(rna.has("hgvs")) {
                                                final String hgvs = rna.getString("hgvs");
                                                String split[] = hgvs.split(":");
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
