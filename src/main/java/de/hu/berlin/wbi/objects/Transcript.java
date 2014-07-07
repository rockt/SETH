package de.hu.berlin.wbi.objects;

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

    /** Retrieves Sequences for an Entrez-Gene Query  */
    private static PreparedStatement sequenceQuery;

    /** Gene-ID (Entrez in our case). */
    protected int entrez;

    /** UniProt-ID. */
    protected String uniprot;

    /** Ensemble transcript ID. */
    protected String enst;

    /** Protein Sequence */
    protected String protein_sequence;

    /** Coding Sequence */
    protected String CDC_sequence;

    /**
     * Initializes the prepared statements  for retrieving
     * sequence information for one Gene
     *
     * @param connection   Database connection
     * @throws SQLException
     */
    public static void init(DatabaseConnection connection)throws SQLException {
        Transcript.sequenceQuery = connection.getConn().prepareStatement("SELECT entrez_id, uniprot_acc, ensembl_sequences.ENST,  protein_sequence, coding_sequence FROM var_reference.ensembl_genes JOIN var_reference.ensembl_sequences ON ensembl_genes.ENST = ensembl_sequences.ENST  WHERE uniprot_acc != \"\" AND entrez_id = ?" );
    }

    public static Set<Transcript> getTranscripts(int entrez) {
        Set<Transcript> sequences = new HashSet<Transcript>();

        try{
            sequenceQuery.setInt(1, entrez);
            sequenceQuery.execute();
            final ResultSet rs = sequenceQuery.getResultSet();

            while (rs.next()) {
                Transcript sequence = new Transcript(entrez, rs.getString("uniprot_acc"), rs.getString("ENST"), rs.getString("protein_sequence"), rs.getString("coding_sequence"));
            }

        }catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return sequences;
    }

    public Transcript(){}

    public Transcript(int entrez, String uniprot, String enst, String protein_sequence, String CDC_sequence) {
        this.entrez = entrez;
        this.uniprot = uniprot;
        this.enst = enst;
        this.protein_sequence = protein_sequence;
        this.CDC_sequence = CDC_sequence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Transcript that = (Transcript) o;

        if (entrez != that.entrez) return false;
        if (!CDC_sequence.equals(that.CDC_sequence)) return false;
        if (!enst.equals(that.enst)) return false;
        if (!protein_sequence.equals(that.protein_sequence)) return false;
        if (!uniprot.equals(that.uniprot)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = entrez;
        result = 31 * result + uniprot.hashCode();
        result = 31 * result + enst.hashCode();
        result = 31 * result + protein_sequence.hashCode();
        result = 31 * result + CDC_sequence.hashCode();
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
}
