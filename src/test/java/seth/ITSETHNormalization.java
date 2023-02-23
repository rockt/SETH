package seth;

import de.hu.berlin.wbi.objects.DatabaseConnection;
import de.hu.berlin.wbi.objects.MutationMention;
import de.hu.berlin.wbi.objects.UniprotFeature;
import de.hu.berlin.wbi.objects.dbSNP;
import de.hu.berlin.wbi.objects.dbSNPNormalized;
import junit.framework.TestCase;
import org.junit.Ignore;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

@Ignore("Test is ignored due to unavailable database!")
public class ITSETHNormalization extends TestCase {
    private DatabaseConnection conn;

    @Override
    protected void setUp() throws Exception {
        Properties properties = new Properties();
        properties.loadFromXML(getClass().getResourceAsStream("/resources/seth_properties.xml"));
        // Assumption is that the database is in {{SETH_DEV_HOME}}/db/dbSNP137
        // and that you run mvn install/integration-test from there.
        properties.setProperty("database.host","jdbc:derby:db/");

        try {
            conn = new DatabaseConnection(properties);
            conn.connect();

            dbSNP.init(conn,
                properties.getProperty("database.PSM"),
                properties.getProperty("database.hgvs_view"));
            UniprotFeature.init(conn,
                properties.getProperty("database.uniprot"));

        } catch (IllegalArgumentException | IllegalStateException | SQLException e) {
            StringWriter sw = new StringWriter(2048);
            e.printStackTrace(new PrintWriter(sw));
            System.err.println("Skipping normalization test: " + sw.toString());
            conn = null;
        }
    }

    public void testDbNormalization() {
        if (conn == null)
            return;

        SETH seth = new SETH(getClass().getResource("/resources/mutations.txt").getPath(), false, true);
        String sentence = "Causative GJB2 mutations were identified in 31 (15.2%) patients, and two common mutations, c.35delG and L90P (c.269T>C), accounted for 72.1% and 9.8% of GJB2 disease alleles.";
        List<MutationMention> mentions = seth.findMutations(sentence);

        assertEquals(mentions.size(), 3);

        // c.35delG and L90P (c.269T>C)
        Collections.sort(mentions, new Comparator<MutationMention>() {
            @Override
            public int compare(MutationMention o1, MutationMention o2) {
                return Integer.compare(o1.getStart(), o2.getStart());
            }
        });

        int gene = 2706;	//Entrez Gene ID associated with the current sentence
        final List<dbSNP> potentialSNPs = dbSNP.getSNP(gene);	//Get a list of dbSNPs which could potentially represent the mutation mention
        final List<UniprotFeature> features = UniprotFeature.getFeatures(gene);    //Get all associated UniProt features

        for (MutationMention m : mentions)
            m.normalizeSNP(potentialSNPs, features, true);

        // c.35delG
        assertTrue(mentions.get(0).getNormalized().size() >= 1);
        dbSNPNormalized n1 = mentions.get(0).getNormalized().get(0);
        // We do this here because for dbsnp138+, rsid 1801002 no longer contains
        // this deletion.
        assertTrue(n1.getRsID() == 80338939 || n1.getRsID() == 1801002);

        // L90P
        assertEquals(mentions.get(1).getNormalized().size(), 1);
        dbSNPNormalized n2 = mentions.get(1).getNormalized().get(0);
        assertEquals(n2.getRsID(), 80338945);

        // c.269T>C
        assertEquals(mentions.get(2).getNormalized().size(), 1);
        dbSNPNormalized n3 = mentions.get(2).getNormalized().get(0);
        assertEquals(n3.getRsID(), 80338945);
    }
}
