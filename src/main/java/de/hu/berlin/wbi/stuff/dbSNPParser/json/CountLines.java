package de.hu.berlin.wbi.stuff.dbSNPParser.json;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import java.io.*;
import java.util.Arrays;

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
            CompressorInputStream input = new BZip2CompressorInputStream(bis, true);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(input));

            String prevLine ="";
            String line;
            int nLines = 0;
            while ((line = bufferedReader.readLine()) != null) {
                nLines++;
                prevLine = line;
            }
            System.out.println("Read " +nLines +" lines from " +file.getAbsolutePath());
            System.out.print(prevLine.substring(0, 100));
        }
    }
}
