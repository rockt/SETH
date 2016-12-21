package de.hu.berlin.wbi.process.osiris;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

public class CheckOsiris {

	/**
	 * @param args    Parameter for main class
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws XPathExpressionException 
	 * @throws TransformerFactoryConfigurationError 
	 * @throws TransformerException
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException, TransformerFactoryConfigurationError, TransformerException,  SQLException {

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

		Document document = builder.parse("data/Osiris-orig.xml");		
		NodeList documents =  (NodeList) documentExp.evaluate(document, XPathConstants.NODESET);	//Corpus

		HashMap<Integer, Set<Integer>> resultMap = new HashMap<Integer, Set<Integer>>();
		HashMap<Integer, Set<String>> nonNormMap = new HashMap<Integer, Set<String>>();
		for (int i =0; i < documents.getLength(); i++) {	//Iterate documents in a corpus
			Node doc = documents.item(i);

			//Extract PubMed identifier
			NodeList pmidNode = (NodeList) pmidExp.evaluate(doc, XPathConstants.NODESET);
			if(pmidNode.getLength() != 1)
				throw new RuntimeException("Found " +pmidNode.getLength() +" PMID nodes");
			int pmid = Integer.parseInt(pmidNode.item(0).getTextContent());
			//			System.out.println(pmid);

			//			Extract Entrez-Genes in this article
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
				String mutationString = variant.getAttributes().getNamedItem("v_lex").getTextContent();

				if(rsId.equals("No")){
					if(nonNormMap.containsKey(pmid)){
						nonNormMap.get(pmid).add(mutationString);
					}
					else{
						Set<String> tmpSet = new HashSet<String>();
						tmpSet.add(mutationString);
						nonNormMap.put(pmid, tmpSet);
					}
				}

				else{				
					
					if(genes.size() == 0 )
						System.err.println("fdf");
					int correctId = Integer.parseInt(rsId);

					if(resultMap.containsKey(pmid)){
						resultMap.get(pmid).add(correctId);
					}
					else{
						Set<Integer> tmpSet = new HashSet<Integer>();
						tmpSet.add(correctId);
						resultMap.put(pmid, tmpSet);
					}
				}
			}			
		}

		System.out.println(resultMap.size());
		int sum=0;
		for(int pmid : resultMap.keySet()) {
			sum+=resultMap.get(pmid).size();
		}
		System.out.println(sum);



		System.out.println(nonNormMap.size());
		sum=0;
		for(int pmid : nonNormMap.keySet()) {
			sum+=nonNormMap.get(pmid).size();
		}
		System.out.println(sum);

	}

}
