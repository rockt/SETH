package de.hu.berlin.wbi.stuff.xml;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.io.FileOutputStream;


import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.hu.berlin.wbi.objects.DatabaseConnection;

/**
 * Created with IntelliJ IDEA.
 * User: philippe
 * Date: 08.12.15
 * Time: 19:52
 * To change this template use File | Settings | File Templates.
 */
public class ParseXMLToFile extends DefaultHandler {

    private StringBuilder hgvs = null;
    private SNP snp;

    private static BufferedWriter psHGVS;
    private static BufferedWriter psmHGVS;

    //private static PreparedStatement psHGVS;
    //private static PreparedStatement psmHGVS;

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, SQLException {
        Properties property = new Properties();
        try {
            property.loadFromXML(new FileInputStream(new File(args[0])));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        DatabaseConnection dbconn = new DatabaseConnection(property);
        dbconn.connect();
        //psHGVS = dbconn.getConn().prepareStatement("INSERT INTO " + property.getProperty("database.hgvs_view") + " (locus_id, snp_id, hgvs, refseq) VALUES (?, ?, ?, ?)");
        //psmHGVS = dbconn.getConn().prepareStatement("INSERT INTO " + property.getProperty("database.PSM") + " (snp_id, locus_id, aa_Position, residue, wildtype) VALUES (?, ?, ?, ?, ?)");

        psHGVS  = new BufferedWriter(new OutputStreamWriter( new GZIPOutputStream(new FileOutputStream(new File("HGVS.tsv.gz"))), "UTF-8"));
        psmHGVS = new BufferedWriter(new OutputStreamWriter( new GZIPOutputStream(new FileOutputStream(new File("PSM.tsv.gz"))), "UTF-8"));

        String xmlFolder = "/home/philippe/workspace/snp-normalizer/data/dat/";
        if (args.length != 1)
            xmlFolder = args[1];

        File[] files = new File(xmlFolder).listFiles();
        Arrays.sort(files);
        for (File file : files) {
            if (!file.getAbsolutePath().endsWith(".gz"))
                continue;

            System.out.println("Parsing " + file.getAbsolutePath());

            InputStream gzipStream = new GZIPInputStream(new FileInputStream(file));
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            DefaultHandler handler = new ParseXMLToFile();
            saxParser.parse(gzipStream, handler);

        }
        psHGVS.close();
        psmHGVS.close();
    }

    @Override
    public void startElement(String namespaceURI, String localName,
                             String qName, Attributes atts) {

        if (qName.equals("Rs")) {
            snp = new SNP(Integer.parseInt(atts.getValue("rsId")));
        }

        if (qName.equals("FxnSet")) {

            String geneId = atts.getValue("geneId");
            String aaPos = atts.getValue("aaPosition");
            String type = atts.getValue("fxnClass");
            String residue = atts.getValue("residue");
//			String symbol = atts.getValue("symbol");			//Gene symbol name (Currently not used)
//				String mrnaAccession = atts.getValue("mrnaAcc");	//mRNA accession number  (Currently not used)
//				String protAccession = atts.getValue("protAcc");	//protein accession number  (Currently not used)
//				String allele = atts.getValue("allele");			//allele (Currently not used)

            if (aaPos != null && geneId != null && !geneId.equals("null") && residue != null) {

                if (type.equals("reference"))
                    snp.addPSM(new PSM(Integer.parseInt(geneId), 1+Integer.parseInt(aaPos), residue, null));
                else if (type.equals("missense")
                        || type.equals("frameshift-variant")
                        || type.equals("stop-gained")
                        || type.equals("stop-lost")
                        || type.equals("intron-variant")) {
                    snp.addPSM(new PSM(Integer.parseInt(geneId), 1+Integer.parseInt(aaPos), null, residue));
                } else if (type.equals("cds-indel") || type.equals("utr-variant-5-prime") || type.equals("nc-transcript-variant") || type.equals("splice-acceptor-variant") || type.equals("utr-variant-3-prime")) {
                }    //Currently we can't handle insdels on AA level and intron-variant
                else if (type.equals("synonymous-codon")) {
                    snp.addPSM(new PSM(Integer.parseInt(geneId), 1+Integer.parseInt(aaPos), residue, residue));
                } else
                    throw new RuntimeException("Can't handle type " + type + " '" + residue + "' for rs" + snp.getRsId());
            } else if (geneId != null && !geneId.equals("null")) {
                snp.addPSM(new PSM(Integer.parseInt(geneId), 0, null, null));
            }
        }

        if (qName.equals("hgvs")) {
            hgvs = new StringBuilder();
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {

        if (qName.equals("hgvs")) {
            snp.addHgvs(hgvs.toString());
            hgvs = null;
        }

        if (qName.equals("Rs")) {

            Set<Integer> genes = new HashSet<Integer>();
            for (PSM psm : snp.getPsms()) {//snp_id, locus_id, aa_Position, residue, wildtype
                genes.add(psm.getEntrez());

                if (!psm.isValid())
                    continue;

                try {

                    if (psm.getMutations().length() > 128 || psm.getWildtype().length() > 128)    //These entries lead to an SQL-Exception otherwise
                        continue;

                    if (psm.getMutations().equals(psm.getWildtype())) // We only want non-synonymous mutations
                        continue;


                    psmHGVS.append(snp.getRsId() +"\t" +psm.getEntrez() +"\t" +psm.getAaLoc() +"\t"
                    +psm.getMutations() +"\t" +psm.getWildtype());
                    psmHGVS.append("\n");
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    System.exit(1);
                }
            }

            for (String hgvs : snp.getHgvs()) {

                String split[] = hgvs.split(":");
                if (split.length != 2) {
                    System.out.println("Split size " + split.length + " instead of 2 for '" + hgvs + "'");
                    System.out.println("rs" + snp.getRsId());
                    continue;
//	throw new RuntimeException("Split size " +split.length +" instead of 2 for '" +hgvs +"'");
                }

                if (split[1].length() >= 256)    //Exclude  this HGVS entry
                    continue;

		//See http://www.ncbi.nlm.nih.gov/books/NBK21091/table/ch18.T.refseq_accession_numbers_and_mole/?report=objectonly
		//Skip XM, XR, and XP, which are are automatically derived annotation pipelines 
		//NC_ NM_ NG_ NR_ NP_ NT_ NW_
		if(split[0].startsWith("XM_") || split[0].startsWith("XR_") || split[0].startsWith("XP_") || split[0].startsWith("GPC_") ||split[0].startsWith("YP_"))  
		    continue;

                try {
                    for (Integer gene : genes) {
                        psHGVS.append(gene +"\t" +snp.getRsId() +"\t" +split[1] +"\t" +split[0]);
                        psHGVS.append("\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    System.exit(1);
                }
            }
        }
    }

    @Override
    public void characters(char ch[], int start, int length) {
        if (hgvs != null) {
            hgvs.append(new String(ch, start, length));
        }
    }
}

