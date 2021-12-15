package de.hu.berlin.wbi.stuff.dbSNPParser.json;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CountLines {
    public static void main(String[] args) throws IOException, CompressorException {
        String jsonFolder = "JSON/";


        File[] files = new File(jsonFolder).listFiles();
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
            int nLines = 0;
            while(line != null){
                line = bufferedReader.readLine();
                nLines++;
            }
            System.out.println("Read " +nLines +" lines from " +file.getAbsolutePath());
        }
    }
}
