package seth.fimda;


import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.util.XMLInputSource;
import org.apache.uima.json.JsonCasSerializer;

import java.io.IOException;
import java.io.StringWriter;

public class FIMDA {

    public static void main(String[] args) throws IOException, InvalidXMLException, ResourceInitializationException, AnalysisEngineProcessException {
        //get Resource Specifier from XML file
        XMLInputSource in = new XMLInputSource("src/main/resources/seth/fimda/MutationAnnotator.xml");
        ResourceSpecifier specifier = UIMAFramework.getXMLParser().parseResourceSpecifier(in);

        //create AE here
        AnalysisEngine ae = UIMAFramework.produceAnalysisEngine(specifier);

        //create a JCas, given an Analysis Engine (ae)
        JCas jcas = ae.newJCas();

        String doc1text = "p.A123T and Val158Met";

        //analyze a document
        jcas.setDocumentText(doc1text);
        ae.process(jcas);
        //doSomethingWithResults(jcas);
        // serialize XCAS and write to output file
        CAS cas = jcas.getCas();
        JsonCasSerializer jcs = new JsonCasSerializer();
        jcs.setPrettyPrint(true); // do some configuration
        StringWriter sw = new StringWriter();
        jcs.serialize(cas, sw); // serialize into sw
        System.out.println(sw.toString());

        jcas.reset();

        //analyze another document
        //jcas.setDocumentText(doc2text);
        //ae.process(jcas);
        //doSomethingWithResults(jcas);
        //jcas.reset();
        //...
        //done
        ae.destroy();

    }
}
