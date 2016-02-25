package de.hu.berlin.wbi.process.osiris;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.hu.berlin.wbi.objects.DatabaseConnection;
import de.hu.berlin.wbi.objects.MutationMention;
import de.hu.berlin.wbi.objects.UniprotFeature;
import de.hu.berlin.wbi.objects.dbSNP;
import de.hu.berlin.wbi.objects.dbSNPNormalized;


public class EvaluateOsiris {

	/**
	 * Evaluate our normalization procedure on the Osiris corpus.
	 * 
	 * @param args  Property XML and Osiris-corpus as parameters
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws XPathExpressionException 
	 * @throws TransformerFactoryConfigurationError 
	 * @throws TransformerException 
	 * @throws SQLException
	 */
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException, TransformerFactoryConfigurationError, TransformerException,  SQLException {

		String propertyFile = args[0];
		String osirisCorpus = args[1];
		Properties property = new Properties();
		try {
			property.loadFromXML(new FileInputStream(new File(propertyFile)));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		} 

		final DatabaseConnection mysql = new DatabaseConnection(property);	
		mysql.connect(); //Connect with local mySQL Database
		dbSNP.init(mysql, property.getProperty("database.PSM"), property.getProperty("database.hgvs_view"));
		UniprotFeature.init(mysql, property.getProperty("database.uniprot"));

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setFeature("http://xml.org/sax/features/namespaces", false);
		factory.setNamespaceAware(true);
		factory.setFeature("http://xml.org/sax/features/validation", false);
		factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
		factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		DocumentBuilder builder = factory.newDocumentBuilder();

		XPath xPath = XPathFactory.newInstance().newXPath();
		XPathExpression documentExp = xPath.compile("/Articles/Article");
		XPathExpression pmidExp = xPath.compile("./Pmid");
		XPathExpression geneExp = xPath.compile(".//gene");
		XPathExpression variantExp = xPath.compile(".//variant");

		Document document = builder.parse(osirisCorpus);		
		NodeList documents =  (NodeList) documentExp.evaluate(document, XPathConstants.NODESET);	//Corpus

		Pattern p = Pattern.compile("^-?[1-9]+[0-9]*");
		int tp=0; int fp=0; int fn=0;

		for (int i =0; i < documents.getLength(); i++) {	//Iterate documents in OSIRIS corpus
			Node doc = documents.item(i);

			HashMap<Integer, Set<String>> mutationVariation = new HashMap<Integer, Set<String>>();

			//Extract current PubMed identifier
			NodeList pmidNode = (NodeList) pmidExp.evaluate(doc, XPathConstants.NODESET);
			if(pmidNode.getLength() != 1)
				throw new RuntimeException("Found " +pmidNode.getLength() +" PMID nodes");
			int pmid = Integer.parseInt(pmidNode.item(0).getTextContent());

			//Extract Entrez-Genes in this article
			List<Integer> genes = new ArrayList<Integer>();
			NodeList geneNode = (NodeList) geneExp.evaluate(doc, XPathConstants.NODESET);
			for(int j =0; j < geneNode.getLength(); j++){
				Node gene = geneNode.item(j);

				if(gene.getAttributes().getNamedItem("g_id").getTextContent().equals("No"))
					continue;

				genes.add(Integer.parseInt(gene.getAttributes().getNamedItem("g_id").getTextContent()));
			}			

			//Exctract mutations			
			NodeList variantNode = (NodeList) variantExp.evaluate(doc, XPathConstants.NODESET);
			for(int j =0; j < variantNode.getLength(); j++){
				Node variant = variantNode.item(j);

				String rsId = variant.getAttributes().getNamedItem("v_id").getTextContent();
				if(rsId.equals("No"))
					continue;

				int correctId = Integer.parseInt(rsId);
				String mutationString = variant.getAttributes().getNamedItem("v_norm").getTextContent();

				Matcher m = p.matcher(mutationString);
				if(m.find())
					mutationString = mutationString.charAt(mutationString.length()-1) +mutationString;

				if(mutationVariation.containsKey(correctId))
					mutationVariation.get(correctId).add(mutationString);
				else{
					Set<String> tmpSet = new HashSet<String>();
					tmpSet.add(mutationString);
					mutationVariation.put(correctId, tmpSet);
				}	
			}
			
	        System.err.println("Corpus description: " +osirisCorpus);
	        System.err.println(mutationVariation.keySet().size() +" documents");
	        int sum = 0;
	        for(int pmid1 : mutationVariation.keySet()){
	        	sum+=mutationVariation.get(pmid1).size();
	        }
	        System.err.println(sum +" entities");

			//Perform normalization (grouped by rs-id)
			for(int rs : mutationVariation.keySet()){
				Set<Integer> rsNorm = new HashSet<Integer>();

				for(String mutationString : mutationVariation.get(rs)){
					if(mutationString.equals("rs" +rs)){
						rsNorm.add(rs);
						continue;
					}				

					MutationMention mutation = new MutationMention(mutationString); //This is the SNP we want to normalize
					for(int gene :  genes){
						final List<dbSNP> potentialSNPs = dbSNP.getSNP(gene);	//Get a list of dbSNPs which could potentially represent the SNP from (mutation)
						final List<UniprotFeature> features = UniprotFeature.getFeatures(gene);
                        mutation.normalizeSNP(potentialSNPs, features, false);
						List<dbSNPNormalized> normalized = mutation.getNormalized();	//And here we have  a list of all dbSNPs with which I could successfully associate the mutation
						for(dbSNPNormalized norm : normalized)
							rsNorm.add(norm.getRsID());
					}					
				}
				if(rsNorm.contains(rs)){		//Check if found rsID's  is correct
					tp++;			
					rsNorm.remove(rs);
				}			
				else{										//Otherwise we have a false negative
					fn++;
					System.out.println(pmid  +" " +mutationVariation.get(rs).toString() +" gene=" +genes.toString() +" rs" +rs);
				}

				fp+=rsNorm.size();			//All remaining ids are false positives	
			}

		}
        double recall = (double) tp/(tp+fn);
        double precision = (double) tp/(tp+fp);
        double f1 = 2*(precision*recall)/(precision+recall);

        DecimalFormat df = new DecimalFormat( "0.00" );
        System.err.println("TP " +tp);
        System.err.println("FP " +fp);
        System.err.println("FN " +fn);
        System.err.println("Precision " +df.format(precision));
        System.err.println("Recall " +df.format(recall));
        System.err.println("F1 " +df.format(f1));
	}

}
