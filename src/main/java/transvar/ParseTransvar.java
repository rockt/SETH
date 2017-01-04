package transvar;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import transvar.TransvarRecord.LEVEL;

/**
 * TransVar is a tool developed by MDACC to map amino acid and CDS changes back to the underlying DNA change(s).
 * Transvar code is available on Github at <a href="https://github.com/zwdzwd/transvar">github.com/zwdzwd/transvar</a> 
 * and documention at <a href="http://transvar.readthedocs.io/en/latest/">transvar.readthedocs.io/en/latest/</a>.
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
		
		if (args.length == 0 || args[0].toLowerCase().matches("\\-\\-?h(elp)?")) {
			System.out.println("ParseTransvar <transvar-output-file> [options]");
			System.out.println("Options: --fromAA -a       -  transvar output based on amino acid changes ('panno') [default]");
			System.out.println("Options: --fromCDS -c      -  transvar output based on CDS changes ('canno')");
			System.out.println("Options: --fromGenomic -g  -  transvar output based on genomic coordinates ('ganno')");
			System.exit(1);
		}
		
		String input = args[0];
		LEVEL inputLevel = LEVEL.UNKNOWN;
		
		if (args.length >= 2) {
			if (args[1].toLowerCase().matches("\\-?\\-?(a|aa|amino|aminoacid|aachange|fromaa|fromaminoacid|hgvsp|phgvs|p|protein)"))
				inputLevel = LEVEL.PROTEIN;
			else if (args[1].toLowerCase().matches("\\-?\\-?(cds|fromcds|hgvsc|chgvs|c)"))
				inputLevel = LEVEL.CDS;
			else if (args[1].toLowerCase().matches("\\-?\\-?(genome|genomic|g|fromgenomic|fromgenome|hgvsg|ghgvs|chr|fromchr|chromosome)"))
				inputLevel = LEVEL.GENOMIC;
			else {
				System.err.println("Unrecognized option: " + args[1]);
				System.exit(2);
			}
		}
		
		System.out.println(TransvarRecord.toTsvHeader());
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(input), "UTF-8"));
			String line;
			while ((line = br.readLine()) != null) {

				// skip header lines
				if (line.startsWith("input\ttranscript")) continue;
				if (line.split("\t")[6].startsWith("Error")) continue;
				
				TransvarRecord record = TransvarRecord.makeFromTsv(line, inputLevel);
				
				if (!record.transcriptType.equalsIgnoreCase("no_valid_transcript_found"))
					System.out.println(record.toTsv());
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