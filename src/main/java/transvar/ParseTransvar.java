package transvar;

import java.io.BufferedReader;
import java.io.File;
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
 * some of the mutations SETH detected and was not able to map to a gene and/or dbSNP ID. In the end, mapping each detected variant to the chromosomal location enables 
 * us to better integrate variants where nomenclature is ambiguous.
 * <br/><br/>
 * This is a command line interface to parse TransVar output into tab-delimited
 * files for import into a database table etc. Call with --help to see command line options.
 * 
 * @author Joerg Hakenberg
 */
public class ParseTransvar {
	
	public static void main (String[] args) {
		// print help screen if needed
		if (args.length == 0 || args[0].toLowerCase().matches("\\-\\-?h(elp)?")) {
			System.out.println("ParseTransvar <transvar-output-file> [options]");
			System.out.println("Options: --fromAA -a       -  transvar output is based on amino acid changes ('panno') [default]");
			System.out.println("         --fromCDS -c      -  transvar output is based on CDS changes ('canno')");
			System.out.println("         --fromGenomic -g  -  transvar output is based on genomic coordinates ('ganno')");
			System.out.println("         --no-header -N    -  Do not print a header line");
			System.exit(1);
		}
		
		// default settings
		String inputFile = "";
		LEVEL inputLevel = LEVEL.PROTEIN;
		boolean printHeader = true;
		
		// parse command line
		for (int a = 0; a < args.length; a++) {
			if (args[a].toLowerCase().matches("\\-?\\-?(a|aa|amino|aminoacid|aachange|fromaa|fromaminoacid|hgvsp|phgvs|p|protein)"))
				inputLevel = LEVEL.PROTEIN;
			else if (args[a].toLowerCase().matches("\\-?\\-?(c|cds|fromcds|hgvsc|chgvs)"))
				inputLevel = LEVEL.CDS;
			else if (args[a].toLowerCase().matches("\\-?\\-?(g|genome|genomic|fromgenomic|fromgenome|hgvsg|ghgvs|chr|fromchr|chromosome)"))
				inputLevel = LEVEL.GENOMIC;
			else if (args[a].toLowerCase().matches("\\-?\\-?(n|no\\-?header)"))
				printHeader = false;

			else {
				File temp = new File(args[a]);
				if (temp.isFile() && temp.canRead()) {
					inputFile = args[a];
				} else {
					System.err.println("Unrecognized option '" + args[1] + "' or cannot read file of that name.");
					System.exit(2);
				}
			}
		}
		
		// print header
		if (printHeader)
			System.out.println(TransvarRecord.toTsvHeader());
		// parse each line of input
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF-8"));
			String line;
			while ((line = br.readLine()) != null) {

				// skip header lines
				if (line.startsWith("input\ttranscript")) continue;
				// skip error messages
				if (line.split("\t")[6].startsWith("Error")) continue;
				// skip entries with no valid transcripts --> variant on CDS/protein level appears to be "impossible"
				if (line.split("\t")[6].startsWith("no_valid_transcript_found")) continue;
				
				// generate a Record using a factory method
				TransvarRecord record = TransvarRecord.makeFromTsv(line, inputLevel);
				// and just print to STDOUT
				String tsv = record.toTsv();
				if (tsv.length() > 0)
					System.out.println(tsv);
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