package de.hu.berlin.wbi.objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;


/**
 * Class to represents a transcript (Protein as well as coding sequence)
 * Therefore one gene usually contains one or more {@link Transcript}
 *
 * @author Philippe Thomas
 *
 */
public class Transcript {

    private final static Logger logger = LoggerFactory.getLogger(Transcript.class);

    /** Retrieves Sequences for an Entrez-Gene Query  */
    private static PreparedStatement sequenceQuery;

    /** Gene-ID (Entrez in our case). */
    protected int entrez;

    /** UniProt-ID. */
    protected String uniprot;

    /** Ensemble gene ID. */
    protected String ensg;

    /** Ensemble transcript ID. */
    protected String enst;

    /** Ensemble protein ID. */
    protected String ensp;

    /** Protein Sequence */
    protected String protein_sequence;

    /** Coding Sequence */
    protected String CDC_sequence;

    /**
     * Initializes the prepared statements  for retrieving
     * sequence information for one Gene
     *
     * @param connection   Database connection
     * @throws SQLException SQL-Exception accessing the database
     */
    public static void init(DatabaseConnection connection, String table)throws SQLException {
        Transcript.sequenceQuery = connection.getConn().prepareStatement("SELECT entrez_id, uniprot_acc, ENSG, ENST, ENSP,  protein_sequence, coding_sequence FROM " +table +" WHERE entrez_id = ?" );
    }

    public static Set<Transcript> getTranscripts(int entrez) {
        Set<Transcript> sequences = new HashSet<Transcript>();

        try{
            sequenceQuery.setInt(1, entrez);
            sequenceQuery.execute();
            final ResultSet rs = sequenceQuery.getResultSet();

            while (rs.next()) {
                Transcript sequence = new Transcript(entrez, rs.getString("uniprot_acc"), rs.getString("ENSG"), rs.getString("ENST"), rs.getString("ENSP"), rs.getString("protein_sequence"), rs.getString("coding_sequence"));
                sequences.add(sequence);
            }

        }catch (SQLException e) {
            logger.error("SQL-Exception",e);
            throw new RuntimeException(e);
        }

        return sequences;
    }

    public Transcript(){}



    public Transcript(int entrez, String uniprot, String ensg, String enst, String ensp, String protein_sequence, String CDC_sequence) {
        this.entrez = entrez;
        this.uniprot = uniprot;
        this.ensg = ensg;
        this.enst = enst;
        this.ensp = ensp;
        this.protein_sequence = protein_sequence;
        this.CDC_sequence = CDC_sequence;
    }

    @Override
    public String toString() {
        return "Transcript{" +
                "entrez=" + entrez +
                ", uniprot='" + uniprot + '\'' +
                ", ensg='" + ensg + '\'' +
                ", enst='" + enst + '\'' +
                ", ensp='" + ensp + '\'' +
                ", protein_sequence='" + protein_sequence + '\'' +
                ", CDC_sequence='" + CDC_sequence + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Transcript that = (Transcript) o;

        if (entrez != that.entrez) return false;
        if (CDC_sequence != null ? !CDC_sequence.equals(that.CDC_sequence) : that.CDC_sequence != null) return false;
        if (ensg != null ? !ensg.equals(that.ensg) : that.ensg != null) return false;
        if (ensp != null ? !ensp.equals(that.ensp) : that.ensp != null) return false;
        if (enst != null ? !enst.equals(that.enst) : that.enst != null) return false;
        if (protein_sequence != null ? !protein_sequence.equals(that.protein_sequence) : that.protein_sequence != null)
            return false;
        if (uniprot != null ? !uniprot.equals(that.uniprot) : that.uniprot != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = entrez;
        result = 31 * result + (uniprot != null ? uniprot.hashCode() : 0);
        result = 31 * result + (ensg != null ? ensg.hashCode() : 0);
        result = 31 * result + (enst != null ? enst.hashCode() : 0);
        result = 31 * result + (ensp != null ? ensp.hashCode() : 0);
        result = 31 * result + (protein_sequence != null ? protein_sequence.hashCode() : 0);
        result = 31 * result + (CDC_sequence != null ? CDC_sequence.hashCode() : 0);
        return result;
    }

    public int getEntrez() {
        return entrez;
    }

    public String getUniprot() {
        return uniprot;
    }

    public String getEnst() {
        return enst;
    }

    public String getProtein_sequence() {
        return protein_sequence;
    }

    public String getCDC_sequence() {
        return CDC_sequence;
    }

    public String getEnsg() {
        return ensg;
    }

    public String getEnsp() {
        return ensp;
    }
}
