package de.hu.berlin.wbi.stuff.xml;

/**
Copyright 2010, 2011 Philippe Thomas
This file is part of snp-normalizer.

snp-normalizer is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
any later version.

snp-normalizer is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with snp-normalizer.  If not, see <http://www.gnu.org/licenses/>.
*/
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.hu.berlin.wbi.objects.DatabaseConnection;

/**
 * Class is used to parse dbSNP XML 
 * 
 * @author Philippe Thomas 
 */
public class ParseXML extends DefaultHandler{

    private StringBuilder hgvs = null;
    private SNP snp;

    private static PreparedStatement psHGVS;
    private static PreparedStatement psmHGVS;

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
		psHGVS =  dbconn.getConn().prepareStatement("INSERT INTO " +property.getProperty("database.hgvs_view") +" (locus_id, snp_id, hgvs, refseq) VALUES (?, ?, ?, ?)");
		psmHGVS = dbconn.getConn().prepareStatement("INSERT INTO " +property.getProperty("database.PSM")  +" (snp_id, locus_id, aa_Position, residue, wildtype) VALUES (?, ?, ?, ?, ?)");

		String xmlFolder ="/home/philippe/workspace/snp-normalizer/data/dat/";
		if(args.length != 1)
			xmlFolder = args[1];

		for(File file : new File(xmlFolder).listFiles()){
			if(!file.getAbsolutePath().endsWith(".gz"))
				continue;

			System.out.println("Parsing " +file.getAbsolutePath());

			InputStream gzipStream = new GZIPInputStream(new FileInputStream(file));	
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			DefaultHandler handler = new ParseXML();
			saxParser.parse( gzipStream, handler );

		}
	}

	@Override
	public void startElement( String namespaceURI, String localName,
			String qName, Attributes atts )
	{

		if(qName.equals("Rs")){
			snp = new SNP(Integer.parseInt(atts.getValue("rsId")));		
		}

		if(qName.equals("FxnSet")){

			String geneId = atts.getValue("geneId");
			String aaPos = atts.getValue("aaPosition");			
			String type = atts.getValue("fxnClass");				
			String residue = atts.getValue("residue");
			//			String symbol = atts.getValue("symbol");			//Gene symbol name (Currently not used)
			//				String mrnaAccession = atts.getValue("mrnaAcc");	//mRNA accession number  (Currently not used)
			//				String protAccession = atts.getValue("protAcc");	//protein accession number  (Currently not used)
			//				String allele = atts.getValue("allele");			//allele (Currently not used)

			if(aaPos != null && geneId != null && !geneId.equals("null") && residue != null){							

				if(type.equals("reference"))
					snp.addPSM(new PSM(Integer.parseInt(geneId), Integer.parseInt(aaPos), residue, null));
				else if(type.equals("missense") 						
						|| type.equals("frameshift-variant") 
						|| type.equals("stop-gained") 
						|| type.equals("stop-lost")
						|| type.equals("intron-variant")){					
					snp.addPSM(new PSM(Integer.parseInt(geneId), Integer.parseInt(aaPos), null, residue));
				}
				else if( type.equals("cds-indel") || type.equals("utr-variant-5-prime") || type.equals("nc-transcript-variant") || type.equals("splice-acceptor-variant") || type.equals("utr-variant-3-prime")){}	//Currently we can't handle insdels on AA level and intron-variant
				else if(type.equals("synonymous-codon")){
					snp.addPSM(new PSM(Integer.parseInt(geneId), Integer.parseInt(aaPos), residue, residue));
				}
				else
					throw new RuntimeException("Can't handle type " +type +" '" +residue +"' for rs" +snp.getRsId());
			}
			else if(geneId != null && !geneId.equals("null")){
				snp.addPSM(new PSM(Integer.parseInt(geneId), 0, null, null));
			}
		}

		if(qName.equals("hgvs")){
			hgvs = new StringBuilder();	
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName){

		if(qName.equals("hgvs")){			
			snp.addHgvs(hgvs.toString());
			hgvs = null;
		}

		if(qName.equals("Rs")){

			Set<Integer> genes = new HashSet<Integer>();
			for(PSM psm : snp.getPsms()){//snp_id, locus_id, aa_Position, residue, wildtype
				genes.add(psm.getEntrez());

				if(!psm.isValid())
					continue;			

				try {

                    if(psm.getMutations().length() > 128 || psm.getWildtype().length() > 128)    //These entries lead to an SQL-Exception otherwise
                       continue;

					psmHGVS.setInt(1, snp.getRsId());
					psmHGVS.setInt(2, psm.getEntrez());
					psmHGVS.setInt(3, psm.getAaLoc());
					psmHGVS.setString(4, psm.getMutations());	
					psmHGVS.setString(5, psm.getWildtype());
					int returnValue = psmHGVS.executeUpdate();

					if(returnValue != 1)
						throw new RuntimeException("Returnvalue=" +returnValue +" for query" +psmHGVS.toString());

				} catch (SQLException e) {
					e.printStackTrace();
					System.err.println(psmHGVS.toString());
					System.exit(1);
				}
			}

			for(String hgvs : snp.getHgvs()){
				
				String split[] = hgvs.split(":");
				if(split.length != 2){
                    System.out.println("Split size " +split.length +" instead of 2 for '" +hgvs +"'");
                    System.out.println("rs" +snp.getRsId());
                    continue;
				//	throw new RuntimeException("Split size " +split.length +" instead of 2 for '" +hgvs +"'");
                }

				if(split[1].length() >= 256)	//Exclude  this HGVS entry
					continue;
				
				try {
					for(Integer gene : genes){

						psHGVS.setInt(1, gene);
						psHGVS.setInt(2, snp.getRsId());
						psHGVS.setString(3, split[1]);
						psHGVS.setString(4, split[0]);
						int returnValue = psHGVS.executeUpdate();		

						if(returnValue != 1)
							throw new RuntimeException("Returnvalue=" +returnValue +" for query " +psHGVS.toString());
					}

				} catch (SQLException e) {
					e.printStackTrace();
					System.err.println(psHGVS.toString());
					System.exit(1);
				}
			}
		}
	}

	@Override
	public void characters(char ch[], int start, int length){		
		if(hgvs != null){
			hgvs.append(new String(ch, start, length));
		}
	}
}
