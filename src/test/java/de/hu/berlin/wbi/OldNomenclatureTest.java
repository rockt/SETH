package de.hu.berlin.wbi;

import de.hu.berlin.wbi.objects.MutationMention;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import seth.SETH;

import static junit.framework.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: philippe
 * Date: 12.04.13
 * Time: 13:57
 *
 * JUnit tests derived from publications describing the current state of mutation nomenclature
 *
 */
public class OldNomenclatureTest extends TestCase {

    private SETH seth;

    @Before
    public void setUp() {
        seth = new SETH("resources/mutations.txt", false, true);
    }

    private void assertSingleMutation(String text, String mutation){
        List<MutationMention> mutationMentions = seth.findMutations(text);

        assertEquals(1, mutationMentions.size());
        assertEquals(mutation,mutationMentions.get(0).getText());
    }

    private void assertSingleMutation(String text) {

        List<MutationMention> mutationMentions = seth.findMutations(text);

        assertEquals(1, mutationMentions.size());
        System.out.println(text +" '" +mutationMentions.get(0).getText() +"'");
        assertEquals(text,mutationMentions.get(0).getText());
    }



    /**
     *   JUnit test classes generated from the Publication:
     *   Beaudet AL, Tsui LC: A suggested nomenclature for designating mutations. Hum Mutat 2(4):245-248, 1993.
     *   http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=PubMed&list_uids=8401532&dopt=Abstract
     */
    @Test
    public void testBeaudet1993() throws Exception {



        assertSingleMutation("435insA");
        assertSingleMutation("1154insTC");
        assertSingleMutation("1154ins12");

        //Deletions
        assertSingleMutation("441delA");
        assertSingleMutation("241delAT");
        assertSingleMutation("deltaF508");
        assertSingleMutation("ΔF508");
        assertSingleMutation("241delAT");
        assertSingleMutation("852del22");

        assertSingleMutation("(441delA", "441delA");
        assertSingleMutation(",441delA ", "441delA");
        assertSingleMutation("(241delAT)", "241delAT");
        assertSingleMutation(" deltaF508,", "deltaF508");
        assertSingleMutation("c [ΔF508 ", "ΔF508");
        assertSingleMutation("d 241delAT ", "241delAT");
        assertSingleMutation("e 852del22 ", "852del22");



        //Substititions
        assertSingleMutation("G85A");
        assertSingleMutation("D44G");
        assertSingleMutation("A455E");
        assertSingleMutation("S549R");
        assertSingleMutation("Gly to Ala substitution at codon 86");
        assertSingleMutation("Tyr to stop at codon 76");

        assertSingleMutation("G85A, ", "G85A");
        assertSingleMutation(" D44G; ", "D44G");
        assertSingleMutation("(A455E)", "A455E");
        assertSingleMutation("S549R and", "S549R");
        assertSingleMutation(" Gly to Ala substitution at codon 86", "Gly to Ala substitution at codon 86");
        assertSingleMutation("Tyr to stop at codon 76 ", "Tyr to stop at codon 76");

        //Intronic substitions
        assertSingleMutation("621+1G->T");
        assertSingleMutation("621+1G→T");
        assertSingleMutation("622-2A->C");
        assertSingleMutation("622-2A-->C");
        assertSingleMutation("622-2A -> C");
        assertSingleMutation("622-2A→C");
        assertSingleMutation("622-2A→ C");
        assertSingleMutation("622-2A →C");
        assertSingleMutation("622-2A → C");

        //Protein vs. DNA Alleles
        assertSingleMutation("M/V470");
        assertSingleMutation("M/V 470");
        assertSingleMutation("Met/Val470");
        assertSingleMutation("Met/Val 470");
        assertSingleMutation("1716G/A");
        assertSingleMutation("1716 G/A");
        assertSingleMutation("125G/C");
        assertSingleMutation("125 G/C");


        assertSingleMutation("A->G at 263");
        assertSingleMutation("C->A at 1496");
        assertSingleMutation("Asp->Gly at 44");
        assertSingleMutation("Ala->Glu at 455");
        assertSingleMutation("Ser->Arg at 549");
        assertSingleMutation("Gln->Stop at 39");

        //Free text deletion insertion currently not supported
        //assertSingleMutation("Deletion of AT at 241");
        //assertSingleMutation("Deletion of 22 bp from 852");
        //assertSingleMutation("Insertion of TC after 1154");
        //assertSingleMutation("Deletion of Phe508");

    }



    /**
     *   JUnit test classes generated from the Publication:
     *   Antonarakis SE, McKusick VA: Discussion on mutation nomenclature. Hum Mutat 4(2):166, 1994.
     *   http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=PubMed&list_uids=7981723&dopt=Abstract
     */
    @Test
    public void testBla() throws Exception{

        assertSingleMutation("G54C");
        assertSingleMutation("Arg250Ter");
        assertSingleMutation("R250X");

        assertSingleMutation("Phe508del");
        assertSingleMutation("Phe508del, ", "Phe508del");

    }

    /**
     *   JUnit test classes generated from the Publication:
     *   Update on nomenclature for human gene mutations. Ad Hoc Committee on Mutation Nomenclature. Hum Mutat 8(3):197-202, 1996.
     *   http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=PubMed&list_uids=8889576&dopt=Abstract
     */
    @Test
    public void testFoo() throws  Exception{

        assertSingleMutation("G54C");
        assertSingleMutation("54G→C");
        assertSingleMutation("ΔF508");
        assertSingleMutation("F508del");
        assertSingleMutation("T702del");

        assertSingleMutation("G54C ", "G54C");
        assertSingleMutation("54G→C,", "54G→C");
        assertSingleMutation("(ΔF508)", "ΔF508");
        assertSingleMutation(" F508del ", "F508del");
        assertSingleMutation(" T702del ", "T702del");

        assertSingleMutation("IVS4+1G>T");
        assertSingleMutation("IVS4+1G->T");
        assertSingleMutation("IVS4+1G-->T");
        assertSingleMutation("IVS4+1G→T");

        assertSingleMutation(" IVS4+1G>T ", "IVS4+1G>T");
        assertSingleMutation("(IVS4+1G->T) ", "IVS4+1G->T");
        assertSingleMutation("IVS4+1G-->T,", "IVS4+1G-->T");
        assertSingleMutation(", IVS4+1G→T, ", "IVS4+1G→T");

        assertSingleMutation("IVS3-2A>T");
        assertSingleMutation("IVS3-2A->T");
        assertSingleMutation("IVS3-2A-->T");
        assertSingleMutation("IVS3-2A→T");

        //assertSingleMutation("1271IVS+1G>T");
        //assertSingleMutation("1271IVS+1G->T");
        //assertSingleMutation("1271IVS+1G-->T");
        //assertSingleMutation("1271IVS+1G→T");


        assertSingleMutation("2472G→T");
        assertSingleMutation("2472G-->T");

        assertSingleMutation("Y76X");
        assertSingleMutation("411delA");
        assertSingleMutation("241delAT");
        assertSingleMutation("852del22");

        assertSingleMutation("435insA");
        assertSingleMutation("3320ins5");
    }


    /**
     *   JUnit test classes generated from the Publication:
     *   Beutler E, McKusick VA, Motulsky AG, Scriver CR, Hutchinson F: Mutation nomenclature: nicknames, systematic names, and unique identifiers. Hum Mutat 8(3):203-206, 1996.
     *   http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=PubMed&list_uids=8889577&dopt=Abstract
     */
    @Test
    public void testBar() throws  Exception{

        //assertSingleMutation("E6V");
        assertSingleMutation("R408W");
        assertSingleMutation("1347 C->T");
//        assertSingleMutation("Δ508");
//        assertSingleMutation("delta508");
//        assertSingleMutation("Delta508");
//        assertSingleMutation("1507del");
        assertSingleMutation("241delAT");
        assertSingleMutation("852del22");
        assertSingleMutation("1154insTC");
        assertSingleMutation("IVS4+1G->T");
        assertSingleMutation("IVS4-2A->C");
        assertSingleMutation("M/V470");
        assertSingleMutation("1716G/A");

        /**
         *      assertSingleMutation("pR408W");
         *      assertSingleMutation("c1347 C->T");
         *      assertSingleMutation("g1347 C->T");
         */

    }

    /**
     *   JUnit test classes generated from the Publication:
     *   Antonarakis SE: Recommendations for a nomenclature system for human gene mutations. Hum Mutat 11(1):1-3, 1998.
     *   http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=PubMed&list_uids=9450896&dopt=Abstract
     */
    @Test
    public void testFooBar() throws  Exception{

        assertSingleMutation("1997delT");
//        assertSingleMutation("1997-1999del");
        assertSingleMutation("1997-1999delTTC");
        assertSingleMutation("1997-1998delTG");
        assertSingleMutation("1997-1998insT");
        assertSingleMutation("1997-1998insTG");
        assertSingleMutation("IVS4-2A>C");

        assertSingleMutation("c.1997+1G>T");
        assertSingleMutation("c.1997-2A>C");
        assertSingleMutation("Y97S");
        assertSingleMutation("R97X");
        assertSingleMutation("T97del");
        //assertSingleMutation("T97-98ins");

    }

    /**
     *   JUnit test classes generated from the Publication:
     *   den Dunnen JT, Antonarakis SE.: Mutation nomenclature extensions and suggestions to describe complex mutations: a discussion. Hum Mutat 15(1):7-12, 2000.
     *   http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=PubMed&list_uids=10612815&dopt=Abstract
     */
    @Test
    public void testDenDunnen2000() throws Exception{

        assertSingleMutation("c.1997G>T");
        assertSingleMutation("c.1997-1999del");
        assertSingleMutation("c.1997-1999delTTC");
//        assertSingleMutation("c.1998-1999insTG"); //@FIXME
        assertSingleMutation("IVS4-2A>C");
        assertSingleMutation("IVS4+1G>T");
        assertSingleMutation("13_14delTT");
//        assertSingleMutation("13_14del");
        assertSingleMutation("13_14insT");
        //assertSingleMutation("4_15inv");
        assertSingleMutation("r.15c>u");
        //assertSingleMutation("T97-C102del");
        //assertSingleMutation("T97-C102del6");

    }

    /**
     *   JUnit test classes generated from the Publication:
     *   den Dunnen JT, Antonarakis SE: Nomenclature for the description of human sequence variations. Hum Genet 109(1):121-124, 2001.
     *   http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=PubMed&list_uids=11479744&dopt=Abstract
     */
    @Test
    public void testDenDunnen2001() throws Exception{

        assertSingleMutation("g.76A>T");
        assertSingleMutation("c.76A>T");
        assertSingleMutation("m.76A>T");
        assertSingleMutation("r.76a>u");
        assertSingleMutation("p.K76A");
        assertSingleMutation("p.T26P");

        assertSingleMutation("IVS2+1G>T");
        assertSingleMutation("c.88+1G>T");
        assertSingleMutation("IVS2-2A>C");
        assertSingleMutation("c.89-2A>C");

        assertSingleMutation("c.76_78del");
        assertSingleMutation("c.76_78delACT");
        assertSingleMutation("c.77-79dup");
        assertSingleMutation("c.77_79dup");
        assertSingleMutation("c.82_83dupTG");
        assertSingleMutation("c.82_83insTG");

        assertSingleMutation("c.112_117delinsTG");
        assertSingleMutation("c.203_506inv");

        assertSingleMutation("r.76a>c");
        assertSingleMutation("r.88+2t>c");

//        assertSingleMutation("p.?");
        assertSingleMutation("p.K29del");
        assertSingleMutation("p.C28_M30del");
        assertSingleMutation("p.G31_Q33dup");
        assertSingleMutation("p.K29_M29insQSK");
        assertSingleMutation("p.C28_K29delinsW");
        assertSingleMutation("p.C28_K29delinsWV");
        assertSingleMutation("p.R97fsX121");
//        assertSingleMutation("p.R97Xfs");

    }

    /**
     *   JUnit test classes generated from the Publication:
     *   Wildeman M, van Ophuizen E, den Dunnen JT, Taschner PE: Improving sequence variant descriptions in mutation databases and literature using the Mutalyzer sequence variation nomenclature checker. Hum Mutat 29(1)6-13, 2008.
     *   http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=PubMed&list_uids=18000842&dopt=Abstract
     */
    @Test
    public void testWildemann2008() throws Exception{

        //assertSingleMutation("AB026906.1:c.169+62C>G");
        assertSingleMutation("c.3_4insG");
        assertSingleMutation("c.3_400del");
        assertSingleMutation("c.510-54A>G");
        assertSingleMutation("c.*19G>T");
        assertSingleMutation("c.76_77delinsTT");
        //assertSingleMutation("AL449423.14(CDKN2A_v001):c.-271_234del");
        //assertSingleMutation("AL449423.14(CDKN2A_v002):c.5_400del");
        //assertSingleMutation("AL449423.14(CDKN2A_v003):c.1_3352del");

        assertSingleMutation("g.7872G>T");
        // assertSingleMutation("AL449423.14:g.61866_85191del");

        //assertSingleMutation("CAH70600.1(CDKN2A_i001):p.0?");
        //assertSingleMutation("CAH70601.1(CDKN2A_i002):p.Gly2AspfsX41");
        // assertSingleMutation("CAH70599.1(CDKN2A_i003):p.0?");
    }

    /**
     *   JUnit test classes generated from the Publication:
     *   Taschner PE, den Dunnen JT: Describing structural changes by extending HGVS sequence variation nomenclature. Hum Mutat 32(5):507-511, 2011.
     *   http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=PubMed&list_uids=21309030&dopt=Abstract
     */
    @Test
    public void testTaschner2011() throws Exception{
        assertSingleMutation("c.76A>C");
        assertSingleMutation("c.77T>G");
        assertSingleMutation("c.76_77delinsCG");
        assertSingleMutation("g.5delT");
        assertSingleMutation("g.5_10del");
        assertSingleMutation("g.5_6insTA");
    }


    /**
     *   JUnit test classes generated from the Publication:
     *   A formalized description of the standard human variant nomenclature in Extended Backus-Naur Form
     *   http://www.ncbi.nlm.nih.gov/pmc/articles/PMC3194197/
     */
    @Test
    public void testLaros2011() throws Exception{
        assertSingleMutation("NM_003002.2:c.274G>T");
        assertSingleMutation("c.274G>T");
        //assertSingleMutation("g.100_200inv{158A>C}");
       // assertSingleMutation("g.100_200delinsAB23456.7");
    }

    @Test
    public void testErrors() throws  Exception{
        assertSingleMutation("+2740 A>G");
        assertSingleMutation("IVS19-37G/C");
        assertSingleMutation("3097delG");
        assertSingleMutation("1782-83delAG");
        assertSingleMutation("1494del6");
//        assertSingleMutation("-491 A to T");
//        assertSingleMutation("c.G1714C");
        assertSingleMutation("Y248del");
        assertSingleMutation("1009delA");
        assertSingleMutation("1009 delA");
        //assertSingleMutation("IVS8+4 A>G");
        //assertSingleMutation("delTTCA");
        //assertSingleMutation("V311fs");
        assertSingleMutation("1631delC");
        assertSingleMutation("DeltaG91");
//        assertSingleMutation("Delta32");
        assertSingleMutation("3432delT");
        assertSingleMutation("-134delA");
        //assertSingleMutation("c2403T --> C");

    }

}
