package de.hu.berlin.wbi.stuff.dbSNPParser.xml;

import java.io.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;


import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import de.hu.berlin.wbi.stuff.dbSNPParser.objects.MergeItem;
import de.hu.berlin.wbi.stuff.dbSNPParser.objects.PSM;
import de.hu.berlin.wbi.stuff.dbSNPParser.objects.SNP;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

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
	private static BufferedWriter mergeItemWriter;


	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, SQLException {


		String xmlFolder = "/media/philippe/Elements/SETH/dbSNP/";
		String outFolder = "";
		if (args.length == 1 || args.length == 2)
			xmlFolder = args[0];

		if(args.length == 2)
			outFolder = args[1];

		psHGVS  = new BufferedWriter(new OutputStreamWriter( new FileOutputStream(new File(outFolder+"hgvs.tsv")), "UTF-8"));
		psmHGVS = new BufferedWriter(new OutputStreamWriter( new FileOutputStream(new File(outFolder+"PSM.tsv")), "UTF-8"));
		mergeItemWriter = new BufferedWriter(new OutputStreamWriter( new FileOutputStream(new File(outFolder+"mergeItems.tsv")), "UTF-8"));

		System.err.println("Extracting data from the following file/folder '" +xmlFolder +"'");
		System.err.println("Writing data to folder '" +outFolder+"'");

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
        mergeItemWriter.close();
    }

	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) {

		if (qName.equals("Rs")) {
			snp = new SNP(Integer.parseInt(atts.getValue("rsId")));
		}

        //Merge History contains other mutations which are associated with this rs-ID
        if (qName.equals("MergeHistory")) {
            snp.addMergeItem(new MergeItem(Integer.parseInt(atts.getValue("rsId")), Integer.parseInt(atts.getValue("buildId"))));
        }

        if (qName.equals("FxnSet")) {

			String geneId = atts.getValue("geneId");
			String aaPos = atts.getValue("aaPosition");
			String type = atts.getValue("fxnClass");
			String residue = atts.getValue("residue");
			String protAccession = atts.getValue("protAcc");	//protein accession number  (Currently not used)			
			//String symbol = atts.getValue("symbol");			//Gene symbol name (Currently not used)
			//String mrnaAccession = atts.getValue("mrnaAcc");	//mRNA accession number  (Currently not used)
			//String allele = atts.getValue("allele");			//allele (Currently not used)
			
			//We require some basic properties for a PSM
			if (aaPos != null && geneId != null && residue != null && !geneId.equals("null") ) {

				//Skip all PSM's which have been derived from an artificial RefSeq
				if(! (protAccession.startsWith("XM_") || protAccession.startsWith("XR_") || protAccession.startsWith("XP_") || protAccession.startsWith("GPC_") || protAccession.startsWith("YP_")) ){

					if (type.equals("reference")){
						snp.addPSM(new PSM(Integer.parseInt(geneId), 1+Integer.parseInt(aaPos), residue, null));
					}
						
					else if (type.equals("missense") || type.equals("frameshift-variant") || type.equals("stop-gained") || type.equals("stop-lost") ) {
						snp.addPSM(new PSM(Integer.parseInt(geneId), 1+Integer.parseInt(aaPos), null, residue));
					}
					//Currently we can't handle insdels on AA level and intron-variant
					else if (type.equals("intron-variant") ||type.equals("cds-indel") || type.equals("utr-variant-5-prime") || type.equals("nc-transcript-variant") || type.equals("splice-acceptor-variant") || type.equals("utr-variant-3-prime")) {}
					else if (type.equals("synonymous-codon")) {
						snp.addPSM(new PSM(Integer.parseInt(geneId), 1+Integer.parseInt(aaPos), residue, residue));
					} else
						throw new RuntimeException("Can't handle type " + type + " '" + residue + "' for rs" + snp.getRsId());
				}
			} else if (geneId != null && !geneId.equals("null")) {
//				snp.addPSM(new PSM(Integer.parseInt(geneId), 0, null, null));
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


					psmHGVS.append(String.valueOf(snp.getRsId()));
					psmHGVS.append("\t");
					psmHGVS.append(String.valueOf(psm.getEntrez()));
					psmHGVS.append("\t");
					psmHGVS.append(String.valueOf(psm.getAaLoc()));
					psmHGVS.append("\t");
					psmHGVS.append(psm.getMutations().replaceAll("\\*", "X"));
					psmHGVS.append("\t");
					psmHGVS.append(psm.getWildtype().replaceAll("\\*", "X"));
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
						psHGVS.append(String.valueOf(gene)).append("\t").append(String.valueOf(snp.getRsId())).append("\t").append(split[1]).append("\t").append(split[0]);
						psHGVS.append("\n");
					}
				} catch (IOException e) {
					e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
					System.exit(1);
				}
			}
            try {
                for (MergeItem mergeItems : snp.getMergeItems()) {
                    mergeItemWriter.append(String.valueOf(snp.getRsId())).append("\t").append(String.valueOf(mergeItems.getRsId())).append("\t").append(String.valueOf(mergeItems.getBuildId()));
                    mergeItemWriter.append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                System.exit(1);
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

