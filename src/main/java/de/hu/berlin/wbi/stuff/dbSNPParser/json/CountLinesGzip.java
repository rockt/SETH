package de.hu.berlin.wbi.stuff.dbSNPParser.json;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import java.io.*;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

public class CountLinesGzip {
    public static void main(String[] args) throws Exception {
        String jsonFolder = "JSON/";


        File[] files = new File(jsonFolder).listFiles();
        Arrays.sort(files);
        for (File file : files) {

            if (!file.getAbsolutePath().endsWith(".gz"))
                continue;

            System.out.println("Parsing " + file.getAbsolutePath());

            InputStream fileStream = new FileInputStream(file);
            InputStream gzipStream = new GZIPInputStream(fileStream);
            Reader decoder = new InputStreamReader(gzipStream);
            BufferedReader bufferedReader = new BufferedReader(decoder);

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
