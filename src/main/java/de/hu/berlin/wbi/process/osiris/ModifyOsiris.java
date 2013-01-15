package de.hu.berlin.wbi.process.osiris;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.uchsc.ccp.nlp.ei.mutation.Mutation;
import edu.uchsc.ccp.nlp.ei.mutation.MutationException;
import edu.uchsc.ccp.nlp.ei.mutation.MutationFinder;
public class ModifyOsiris {


	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException, TransformerFactoryConfigurationError, TransformerException, MutationException {
		MutationFinder mf = new MutationFinder("/home/philippe/workspace/SETH/resources/mutations.txt");
		
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
		XPathExpression variantExp = xPath.compile(".//variant");
		
		Document document = builder.parse("data/OSIRIScorpusv01.xml");		
		NodeList documents =  (NodeList) documentExp.evaluate(document, XPathConstants.NODESET);	//Corpus

	
		for (int i =0; i < documents.getLength(); i++) {	//Iterate documents in a corpus
			Node doc = documents.item(i);
					
			//Extract PubMed identifier
			NodeList pmidNode = (NodeList) pmidExp.evaluate(doc, XPathConstants.NODESET);
			if(pmidNode.getLength() != 1)
				throw new RuntimeException("Found " +pmidNode.getLength() +" PMID nodes");
			int pmid = Integer.parseInt(pmidNode.item(0).getTextContent());
			System.out.println(pmid);
						
			
			//Exctract mutations			
			NodeList variantNode = (NodeList) variantExp.evaluate(doc, XPathConstants.NODESET);
			for(int j =0; j < variantNode.getLength(); j++){
				Node variant = variantNode.item(j);
				
				String variantText = variant.getAttributes().getNamedItem("v_lex").getTextContent();
				Map<Mutation, Set<int[]>> mutations = mf.extractMutations(variantText);
				
				if(mutations.keySet().size() > 1)
					throw new RuntimeException("Only up to one mention allowed" +variantText);
				
				Element variantElement = (Element) variant;
				variantElement.setAttribute("v_norm", mutations.size() > 0 ? mutations.keySet().iterator().next().toString() : "null");
				
				if(variant.getAttributes().getNamedItem("v_id").getTextContent().equals("No"))
					continue;
				
				int correctId = Integer.parseInt(variant.getAttributes().getNamedItem("v_id").getTextContent());
				String mutation = variant.getAttributes().getNamedItem("v_lex").getTextContent();
				
				System.out.println("'" +mutation +"' " +correctId);
			}		
		}
		
		DOMSource domSource = new DOMSource(document);
		File outFile = new File("phil.xml");
        Result result = new StreamResult(outFile);
        Transformer xformer = TransformerFactory.newInstance().newTransformer();
        xformer.transform(domSource, result);
	}

}
