package transvar;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * TransVar is a tool developed by MDACC to map amino acid and CDS changes back to the underlying DNA change(s). Transvar code is available on Github at <a href="https://github.com/zwdzwd/transvar">github.com/zwdzwd/transvar</a> and documention at <a href="http://transvar.readthedocs.io/en/latest/">transvar.readthedocs.io/en/latest/</a>.
 * <br/><br/>
 * We use TransVar to help mapping detected variants to their chromosomal location and allele(s); since TransVar handles any amino acid and CDS change, this remediates
 * some of the mutations SETH detected and was not able to map to a gene and/or dbSNP ID.
 * <br/><br/>
 * In the end, mapping each detected variant to the chromosomal location enables us to better integrate variants where nomenclature is ambiguous. 
 * 
 * @author Joerg Hakenberg
 */
public class ParseTransvar {

	public static void main (String[] args) {
		
		String input = args[0];
		boolean fromAmino = args[1].toLowerCase().matches("\\-?\\-?(aa|amino|aminoacid|aachange|fromaa|fromaminoacid|hgvsp|phgvs|p|protein)");
		
		System.out.println(TransvarRecord.toTsvHeader());
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(input), "UTF-8"));
			String line;
			while ((line = br.readLine()) != null) {
				//
				
				TransvarRecord record = TransvarRecord.makeFromTsv(line);
				record.predictedFromAminoAcidChange = fromAmino;
			
				//record.print();
				
				System.out.println(record.toTsv());
				
				//System.exit(1);
			}
				
			br.close();
			br = null;
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
		
	}
}




/*

ABCA1:G948R	NM_005502 (protein_coding)	ABCA1	-	chr9:g.107583774C>T/c.2842G>A/p.G948R	inside_[cds_in_exon_20]	CSQN=Missense;reference_codon=GGG;candidate_codons=AGG,AGA,CGA,CGC,CGG,CGT;candidate_snv_variants=chr9:g.107583774C>G;candidate_mnv_variants=chr9:g.107583772_107583774delCCCinsTCT,chr9:g.107583772_107583774delCCCinsTCG,chr9:g.107583772_107583774delCCCinsGCG,chr9:g.107583772_107583774delCCCinsACG;dbxref=GeneID:19,HGNC:29,HPRD:02501,MIM:600046;aliases=NP_005493;source=RefSeq



*/