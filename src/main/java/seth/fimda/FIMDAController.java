package seth.fimda;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

@RestController
public class FIMDAController {

    private AnalysisEngine ae;
    private JCas jcas;

    FIMDAController() throws IOException, InvalidXMLException, ResourceInitializationException {

        //System.out.println("INIT AnalysisEngine");

        //get Resource Specifier from XML file
        XMLInputSource in = new XMLInputSource("src/main/resources/seth/fimda/MutationAnnotator.xml");
        ResourceSpecifier specifier = UIMAFramework.getXMLParser().parseResourceSpecifier(in);

        //create AE here
        ae = UIMAFramework.produceAnalysisEngine(specifier);

        //create a JCas, given an Analysis Engine (ae)
        jcas = ae.newJCas();
    }

    @RequestMapping("/annotate")
    public ResponseEntity<String> findMutations(@RequestParam(value="text", defaultValue="p.A123T and Val158Met") String text) {

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> result;

        try {
            //analyze a document
            jcas.setDocumentText(text);
            ae.process(jcas);
            // serialize XCAS
            CAS cas = jcas.getCas();
            JsonCasSerializer jcs = new JsonCasSerializer();
            jcs.setPrettyPrint(true); // do some configuration
            StringWriter sw = new StringWriter();
            jcs.serialize(cas, sw); // serialize into sw
            result = new ResponseEntity<>(sw.toString(), httpHeaders, HttpStatus.OK);
        } catch (AnalysisEngineProcessException | IOException e) {
            //e.printStackTrace();
            result = new ResponseEntity<>(e.toString(), httpHeaders, HttpStatus.BAD_REQUEST);
        } finally {
            jcas.reset();
        }

        return result;

    }

}
