package transvar;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.text.similarity.LevenshteinDistance;

/**
 * 
 * 
 * A typical transvar output record looks like the following. The original output is tab-delimited; I replaced those with line breaks. The column that starts
 * with the consequence ('CQSN=') is a semi-colon delimited list of key-value pairs; everything that follows CSQN belongs to the same column:<pre>
ABCA1:G948R
NM_005502 (protein_coding)
ABCA1
-
chr9:g.107583774C>T/c.2842G>A/p.Gly948Arg
inside_[cds_in_exon_20]
CSQN=Missense;
reference_codon=GGG;
candidate_codons=AGG,AGA,CGA,CGC,CGG,CGT;
candidate_snv_variants=chr9:g.107583774C>G;
candidate_mnv_variants=chr9:g.107583772_107583774delCCCinsTCT,chr9:g.107583772_107583774delCCCinsTCG,chr9:g.107583772_107583774delCCCinsGCG,chr9:g.107583772_107583774delCCCinsACG;
dbxref=GeneID:19,HGNC:29,HPRD:02501,MIM:600046;
aliases=NP_005493;
source=RefSeq
</pre>
so we have seven columns in total in transvar's output:
<ul>
<li>input to transvar: geneSymbol:aaChange (or CDS change, etc.)
<li>transcript ID identified by transvar, with transcript biotype in parentheses
<li>strand: '+', '-', or '.'
<li>best candidate that explain the input variant: chromosome, genomic change in HGVS g. annotation, CDS change, protein change (if applicable)
<li>location: exonic/intronic, which exon(s)
<li>consequence: list of key-value pairs for name of consequence (CSQN); reference codon that contains the variant;
    all new codons that explain the input variant (one amino acid change can result from many DNA changes);
    all SNV variants that would explain the input variant, omitting the best candidate already listed in column 4;
    all MNV variants, omitting the one from column 4 if applicable;
    cross-references of the gene to other DBs, such as HGNC ID;
    source: gene model, such as RefSeq or Ensembl (input param to transvar).
</ul>
 * 
 * 
 * 
 * @author jhakenberg
 */
public class TransvarRecord {

	/** The gene originally provided as input to TransVar. */
	public String sourceGene;
	/** The genetic variant provided as input to TransVar. */
	public String sourceVariant;
	
	public enum LEVEL {GENOMIC, CDS, PROTEIN, UNKNOWN};
	/** */
	public LEVEL sourceLevel = LEVEL.UNKNOWN;
	
	/*/*	 */
	//public boolean predictedFromAminoAcidChange = true;
	
	public enum GENE_MODEL {ENSEMBL, REFSEQ, CCDS, UCSC, GENCODE, UNKNOWN};
	public GENE_MODEL geneModel = GENE_MODEL.UNKNOWN;
	
	/** */
	public String transcriptId;
	/** Type of variant: protein coding, etc. */
	public String transcriptType;
	
	/** Gene symbol by TransVar. */
	public String transvarGene;
	/** Strand the CDS for this gene is read from: '+' for forward, '-' for reverse, '.' for unknown/not a coding sequence. */
	public char strand;
	
	public String chr;
	//public String gHgvs;
	//public String cHgvs;
	//public String pHgvs;
	
	public String full_location;
	public String CQSN;
	
	/** Consequence on protein level: missense, nonsense, ... */
	public enum CONSEQUENCE {
		SYNONYMOUS("Synonymous"), MISSENSE("Missense"), NONSENSE("Nonsense"), MULTIAAMISSENSE("MultiAAMissense"), 
		FRAMESHIFT("Frameshift"), INFRAME_DELETION("InFrameDeletion"), INFRAME_INSERTION("InFrameInsertion"), 
		CDS_START_SNV("CdsStartSNV"), CDS_STOP_SNV("CdsStopSNV"),
		CDS_START_DELETION("CdsStartDeletion"), CDS_STOP_DELETION("CdsStopDeletion"), 
		CDS_START_BLOCKSUBSTITUTION("CdsStartBlockSubstitution"), CDS_STOP_BLOCKSUBSTITUTION("CdsStopBlockSubstitution"),
		INTRONIC_SNV("IntronicSNV"), INTRONIC_DELETION("IntronicDeletion"), INTRONIC_INSERTION("IntronicInsertion"),
		INTRONIC_BLOCKSUBSTITUTION("IntronicBlockSubstitution"),
		INTERGENIC_SNV("IntergenicSNV"), INTERGENIC_DELETION("IntergenicDeletion"), INTERGENIC_INSERTION("IntergenicInsertion"),
		INTERGENIC_BLOCKSUBSTITUTION("IntergenicBlockSubstitution"),
		SPLICE_DONOR_DELETION("SpliceDonorDeletion"), SPLICE_ACCEPTOR_DELETION("SpliceAcceptorDeletion"),
		SPLICE_DONOR_INSERTION("SpliceDonorInsertion"), SPLICE_ACCEPTOR_INSERTION("SpliceAcceptorInsertion"), 
		SPLICE_DONOR_SNV("SpliceDonorSNV"), SPLICE_ACCEPTOR_SNV("SpliceAcceptorSNV"),
		SPLICE_DONOR_BLOCKSUBSTITUTION("SpliceDonorBlockSubstitution"), SPLICE_ACCEPTOR_BLOCKSUBSTITUTION("SpliceAcceptorBlockSubstitution"),
		UTR5_SNV("5-UTRSNV"), UTR3_SNV("3-UTRSNV"),
		UTR5_BLOCKSUBSTITUTION("5-UTRBlockSubstitution"), UTR3_BLOCKSUBSTITUTION("3-UTRBlockSubstitution"),
		UNCLASSIFIED_SNV("UnclassifiedSNV"), UNCLASSIFIED_BLOCKSUBSTITUTION("UnclassifiedBlockSubstitution"),
		UNCLASSIFIED("Unclassified");
		
		private String value = "Unclassified";

		CONSEQUENCE (String c) {
			//System.err.println("#INFO parsing '" + c + "'");
			if (c.startsWith("Multi:")) { // CSQN=Multi:SpliceDonorBlockSubstitution,CdsStartBlockSubstitution;
				c = c.split(",")[1];      // arbitrarily pick the 2nd option
				System.err.println("#WARN subst with " + c);
			}
			this.value = c;
		}
		
		public String getValue() {
	        return value;
	    }
		
		@Override public String toString() {
	        return this.getValue();
	    }
		
		public static CONSEQUENCE get (String value) {
			if (value.startsWith("Multi:")) {     // CSQN=Multi:SpliceDonorBlockSubstitution,CdsStartBlockSubstitution;
				value = value.split(",")[1];      // arbitrarily pick the 2nd option
				System.err.println("#WARN subst with " + value);
			}
	        for (CONSEQUENCE v : values())
	            if(v.getValue().equalsIgnoreCase(value)) return v;
	        throw new IllegalArgumentException();
	    }
		
		};
	public CONSEQUENCE consequence = CONSEQUENCE.UNCLASSIFIED;
	
	/**
	 * Location(s) on the cDNA where the variant is found:<br/>
	 * SINGLE_(EXON|INTRON): variant is limited to a single exon (intron) region;<br/>
	 * MULTI_(EXON|INTRON): variant spans from one exon (intron) to another exon (intron)<br/>
	 * EXON_INTRON (INTRON_EXON): variant spans from one exon (intron) to the following intron (exon)<br/>
	 * MULTI_EXON_INTRON, MULTI_INTRON_EXON: variant spans from one exon (intron) into any subsequent intron (exon).
	 */
	public enum LOCATION {
		SINGLE_EXON, SINGLE_INTRON,
		MULTI_EXON, MULTI_INTRON, 
		EXON_INTRON, INTRON_EXON,
		MULTI_EXON_INTRON, MULTI_INTRON_EXON,
		UTR5, UTR3
	}
	
	public LOCATION location;
	
	//public String referenceCodon;
	//public String[] candidateCodons;
	
	//public String[] candidateSnvs;
	//public String[] candidateMnvs;
	
	public int entrezGeneId;
	public int hgncId;
	public String hprdId;
	public String mimId;
	
	public String[] aliases;
	public String proteinId;
	
	public int exonStart;
	public int exonEnd;
	public int intronStart;
	public int intronEnd;
	
	public List<Candidate> candidates = new LinkedList<Candidate>();
	
	/** Original output from TransVar (full line, tab-delimited). */
	String transvarSource = "";
	
	static LevenshteinDistance ld = new LevenshteinDistance();
	
	/**
	 * Static factory to initialize a TransvarRecord from a TransVar output line.<br/><br/>
	 * A tab-delimited TransVar output line looks like<pre>
	 * ABCA1:G948R  ENST00000374736 (protein_coding)  ABCA1  -  chr9:g.107583774C>T/c.2842G>A/p.Gly948Arg  inside_[cds_in_exon_20]  CSQN=Missense;reference_codon=GGG;candidate_codons=AGG,AGA,CGA,CGC,CGG,CGT;candidate_snv_variants=chr9:g.107583774C>G;candidate_mnv_variants=chr9:g.107583772_107583774delCCCinsTCT,chr9:g.107583772_107583774delCCCinsTCG,chr9:g.107583772_107583774delCCCinsGCG,chr9:g.107583772_107583774delCCCinsACG;aliases=ENSP00000363868;source=Ensembl
	 * </pre>referring to <ul>
	 * <li>the input value for protein and variant;
	 * <li>the matched transcript and transcript biotype (coding, pseudogene);
	 * <li>the matched gene
	 * <li>the strand (- for reverse, + for forward, or . for unknown);
	 * <li>the most likely candidate by chromosome, location and ref>alt allele, CDS change, and amino acid change;
	 * <li>the location wrt to the gene listed in column 3: inside or UTR, which exon(s) or intron(s);
	 * <li>a list of all possible consequences (CSQN) and candidates as well as sources (Ensembl, RefSeq) and aliases (that is, protein name like ENSP1234 or NP_1234).
	 * </ul>
	 * @param line - tab-delimited line of transvar output
	 * @return a TransvarRecord with fields populated according to the input line
	 */
	public static TransvarRecord makeFromTsv (String line, LEVEL sourceLevel) {
		//System.err.println(line);
		TransvarRecord r = new TransvarRecord();
		r.transvarSource = line;
		r.sourceLevel = sourceLevel;

		String[] cols = line.split("\t");
		String[] s = cols[0].split(":");
		r.sourceGene = s[0];
		r.sourceVariant = s[1];

		if (cols[6].equals("no_valid_transcript_found") || cols[6].indexOf("invalid_reference_seq") >= 0) {
			r.transcriptType = "no_valid_transcript_found";
			return r;
		}

		String t[] = cols[1].split(" ");
		r.transcriptId = t[0];
		r.transcriptType = t[1].substring(1, t[1].length() - 1);

		r.transvarGene = cols[2];
		r.strand = cols[3].charAt(0);

		// chr9:g.107583774C>T/c.2842G>A/p.Gly948Arg
		String[] hgvs = cols[4].split("/");

		// get the first candidate from the suggested HGVS in column 5, consisting of chromosome and genomic change
		Candidate c1 = Candidate.makeFromG(hgvs[0]);
		c1.isBestCandidate = true;
		c1.setHgvsC(hgvs[1]);
		c1.setHgvsP(hgvs[2]);
		r.chr = c1.chr;

		// get all other columns from the candidate_snv_variants and candidate_mnv_variants fields, see below

		//
		r.parseLocation(cols[5]);


		// generate a key/value map of consequence fields
		Map<String, String> cMap = new HashMap<String, String>();
		String[] CSQN = cols[6].split(";");
		for (String C : CSQN) {
			if (C.indexOf("=") > 0) {
				String key = C.split("=")[0];
				String val = C.split("=")[1];
				cMap.put(key, val);
			} else {
				System.err.println("#WARN ignored consequence field '" + C + "'");
			}
		}
		if (cMap.containsKey("CSQN")) r.consequence = //CONSEQUENCE.valueOf(cMap.get("CSQN").toUpperCase());
				CONSEQUENCE.get(cMap.get("CSQN"));
		// stop gained are reported by transvar as missense
		if ( r.sourceVariant.matches("(p\\.)?[A-Z][a-z]*\\d+([X\\*]|Stop|Ter)")
			 && !(r.sourceVariant.matches("(p\\.)?(X|\\*|Stop|Ter).+"))
			 && r.consequence == CONSEQUENCE.MISSENSE )
			r.consequence = CONSEQUENCE.NONSENSE;

		if (cMap.containsKey("reference_codon"))
			c1.referenceCodon = cMap.get("reference_codon");

		if (cMap.containsKey("candidate_codons"))
			c1.candidateCodons = cMap.get("candidate_codons").split(",");

		// add the suggested 'best' candidate
		r.candidates.add(c1);

		// add additional candidates listed in the candidate_snv_variants and candidate_mnv_variants fields
		if (cMap.containsKey("candidate_snv_variants")) {
			String[] candidateList = cMap.get("candidate_snv_variants").split(",");
			for (String can : candidateList) {
				Candidate c2 = Candidate.makeFromG(can);
				c2.referenceCodon = c1.referenceCodon;
				if (r.sourceLevel == LEVEL.PROTEIN) c2.setHgvsP(c1.getHgvsP());
				else if (r.sourceLevel == LEVEL.CDS) c2.setHgvsC(c1.getHgvsC());
				if (cMap.containsKey("candidate_codons")) c2.candidateCodons = cMap.get("candidate_codons").split(",");
				r.candidates.add(c2);
			}
		}
		if (cMap.containsKey("candidate_mnv_variants")) {
			String[] candidateList = cMap.get("candidate_mnv_variants").split(",");
			for (String can : candidateList) {
				Candidate c2 = Candidate.makeFromG(can);
				c2.referenceCodon = c1.referenceCodon;
				if (r.sourceLevel == LEVEL.PROTEIN) c2.setHgvsP(c1.getHgvsP());
				else if (r.sourceLevel == LEVEL.CDS) c2.setHgvsC(c1.getHgvsC());
				if (cMap.containsKey("candidate_codons")) c2.candidateCodons = cMap.get("candidate_codons").split(",");
				r.candidates.add(c2);
			}
		}

		// analyze all candidate to assign the correct alternate allele and codon
		r.analyzeCandidates();

		r.geneModel = GENE_MODEL.UNKNOWN;
		try {
			if (cMap.containsKey("source")) r.geneModel = GENE_MODEL.valueOf(cMap.get("source").toUpperCase());
		} catch (java.lang.IllegalArgumentException e) {
			;//r.geneModel = GENE_MODEL.UNKNOWN;
		}

		if (cMap.containsKey("dbxref")) {
			Map<String, String> idMap = new HashMap<String, String>();
			String[] ids = cMap.get("dbxref").split(",");
			for (String id : ids) {
				String key = id.split(":")[0];
				String val = id.split(":")[1];
				idMap.put(key, val);
			}
			if (idMap.containsKey("GeneID")) r.entrezGeneId = Integer.parseInt(idMap.get("GeneID"));
			if (idMap.containsKey("HGNC")) r.hgncId = Integer.parseInt(idMap.get("HGNC"));
			if (idMap.containsKey("HPRD")) r.hprdId = idMap.get("HPRD");
			if (idMap.containsKey("MIM")) r.mimId = idMap.get("MIM");
		}

		if (cMap.containsKey("aliases")) r.aliases = cMap.get("aliases").split(",");
		if (r.aliases != null) {
			for (String alias : r.aliases)
				if (alias.startsWith("NP_") || alias.startsWith("XP_") || alias.startsWith("ENSP")) r.proteinId = alias;
		}

		return r;
	}

	
	/**
	 * 
	 * @param transvarLocation
	 * @return
	 */
	boolean parseLocation (String transvarLocation) {
		full_location = transvarLocation;
		// inside_[cds_in_exon_100]
		if (full_location.matches("inside_\\[cds_in_exon_\\d+\\]")) {
			exonStart = Integer.parseInt(full_location.replaceFirst("^inside_\\[cds_in_exon_(\\d+)\\]$", "$1"));
			exonEnd = exonStart;
			location = LOCATION.SINGLE_EXON;
		} else if (full_location.matches("inside_\\[cds_in_exons_\\[\\d+,\\d+\\]\\]")) {
			exonStart = Integer.parseInt(full_location.replaceFirst("^inside_\\[cds_in_exons_\\[(\\d+),(\\d+)\\]\\]$", "$1"));
			exonEnd   = Integer.parseInt(full_location.replaceFirst("^inside_\\[cds_in_exons_\\[(\\d+),(\\d+)\\]\\]$", "$2"));
			location = LOCATION.MULTI_EXON;
		// inside_[intron_between_exon_6_and_7]
		} else if (full_location.matches("inside_\\[intron_between_exon_\\d+_and_\\d+\\]")) {
			int es = Integer.parseInt(full_location.replaceFirst("^inside_\\[intron_between_exon_(\\d+)_and_(\\d+)\\]$", "$1"));
			int ee = Integer.parseInt(full_location.replaceFirst("^inside_\\[intron_between_exon_(\\d+)_and_(\\d+)\\]$", "$2"));
			intronStart = es;
			intronEnd   = ee - 1;
			location = LOCATION.SINGLE_INTRON;
		// inside_[3-UTR;noncoding_exon_3]
		} else if (full_location.matches("inside_\\[[35]-UTR;noncoding_exon_\\d+\\]")) {
			if (full_location.startsWith("inside_[3-UTR")) location = LOCATION.UTR3;
			else if (full_location.startsWith("inside_[5-UTR")) location = LOCATION.UTR5;
			exonStart = Integer.parseInt(full_location.replaceFirst("^inside_\\[[35]-UTR;noncoding_exon_(\\d+)\\]$", "$1"));
			exonEnd = exonStart;
		// inside_[5-UTR;intron_between_exon_15_and_16]
		} else if (full_location.matches("inside_\\[[35]-UTR;intron_between_exon_\\d+_and_\\d+\\]")) {
			if (full_location.startsWith("inside_[3-UTR")) location = LOCATION.UTR3;
			else if (full_location.startsWith("inside_[5-UTR")) location = LOCATION.UTR5;
			int es = Integer.parseInt(full_location.replaceFirst("^inside_\\[[35]-UTR;intron_between_exon_(\\d+)_and_(\\d+)\\]$", "$1"));
			int ee = Integer.parseInt(full_location.replaceFirst("^inside_\\[[35]-UTR;intron_between_exon_(\\d+)_and_(\\d+)\\]$", "$2"));
			intronStart = es;
			intronEnd   = ee - 1;
		// inside_[noncoding_exon_10]
		} else if (full_location.matches("inside_\\[noncoding_exon_\\d+\\]")) {
			exonStart = Integer.parseInt(full_location.replaceFirst("^inside_\\[noncoding_exon_(\\d+)\\]$", "$1"));
			exonEnd = exonStart;
			location = LOCATION.SINGLE_EXON;
		// from_[intron_between_exon_6_and_7]_to_[cds_in_exon_6]
		// from_[intron_between_exon_10_and_11]_to_[cds_in_exon_11]
		} else if (full_location.matches("from_\\[intron_between_exon_\\d+_and_\\d+\\]_to_\\[(?:cds_in|noncoding)_exon_\\d+\\]")) {
			int ies = Integer.parseInt(full_location.replaceFirst("^from_\\[intron_between_exon_(\\d+)_and_(\\d+)\\]_to_\\[(?:cds_in|noncoding)_exon_(\\d+)\\]$", "$1"));
			int iee = Integer.parseInt(full_location.replaceFirst("^from_\\[intron_between_exon_(\\d+)_and_(\\d+)\\]_to_\\[(?:cds_in|noncoding)_exon_(\\d+)\\]$", "$2"));
			int exon = Integer.parseInt(full_location.replaceFirst("^from_\\[intron_between_exon_(\\d+)_and_(\\d+)\\]_to_\\[(?:cds_in|noncoding)_exon_(\\d+)\\]$", "$3"));
			if (ies == exon) {
				intronStart = ies;
				intronEnd = intronStart;
				exonStart = exon;
				exonEnd = exonStart;
				location = LOCATION.EXON_INTRON;
			} else if (iee == exon) {
				exonStart = exon;
				exonEnd = exonStart;
			}
			location = LOCATION.EXON_INTRON;
		// from_[noncoding_exon_2]_to_[intron_between_exon_1_and_2]'
		} else if (full_location.matches("from_\\[(?:cds_in|noncoding)_exon_\\d+\\]_to_\\[intron_between_exon_\\d+_and_\\d+\\]")) {
			int exon = Integer.parseInt(full_location.replaceFirst("^from_\\[(?:cds_in|noncoding)_exon_(\\d+)\\]_to_\\[intron_between_exon_(\\d+)_and_(\\d+)\\]$", "$1"));
			exonStart = exon;
			exonEnd = exonStart;
			intronStart = Integer.parseInt(full_location.replaceFirst("^from_\\[(?:cds_in|noncoding)_exon_(\\d+)\\]_to_\\[intron_between_exon_(\\d+)_and_(\\d+)\\]$", "$2"));
		// from_[noncoding_exon_2]_to_[intergenic_between_ATM(1_bp_downstream)_and_AP001925.1(5,531_bp_downstream)]
		} else if (full_location.matches("from_\\[(?:cds_in|noncoding)_exon_\\d+\\]_to_\\[intergenic.*")) {
			int exon = Integer.parseInt(full_location.replaceFirst("^from_\\[(?:cds_in|noncoding)_exon_(\\d+)\\]_to_\\[intergenic.*$", "$1"));
			exonStart = exon;
		// from_[intergenic_between_GKN1(94,628_bp_downstream)_and_ANTXR1(1_bp_upstream)]_to_[cds_in_exon_1]
		} else if (full_location.matches("from_\\[intergenic.*\\]_to_\\[(?:cds_in|noncoding)_exon_\\d+\\]")) {
			int exon = Integer.parseInt(full_location.replaceFirst("^from_\\[intergenic.*\\]_to_\\[(?:cds_in|noncoding)_exon_(\\d+)\\]$", "$1"));
			exonStart = exon;
		// from_[3-UTR;noncoding_exon_22]_to_[cds_in_exon_22]
		} else if (full_location.matches("from_\\[[35]-UTR;(?:cds_in|noncoding)_exon_(\\d+)\\]_to_\\[cds_in_exon_(\\d+)\\]")) {
			exonStart = Integer.parseInt(full_location.replaceFirst("^from_\\[[35]-UTR;(?:cds_in|noncoding)_exon_(\\d+)\\]_to_\\[cds_in_exon_(\\d+)\\]$", "$1"));
			exonEnd   = Integer.parseInt(full_location.replaceFirst("^from_\\[[35]-UTR;(?:cds_in|noncoding)_exon_(\\d+)\\]_to_\\[cds_in_exon_(\\d+)\\]$", "$2"));
		// from_[cds_in_exon_3]_to_[3-UTR;noncoding_exon_3]
		} else if (full_location.matches("from_\\[(?:noncoding|cds_in)_exon_\\d+\\]_to_\\[[35]-UTR;noncoding_exon_\\d+\\]")) {
			exonStart = Integer.parseInt(full_location.replaceFirst("^from_\\[(?:noncoding|cds_in)_exon_(\\d+)\\]_to_\\[[35]-UTR;noncoding_exon_(\\d+)\\]$", "$1"));
			exonEnd   = Integer.parseInt(full_location.replaceFirst("^from_\\[(?:noncoding|cds_in)_exon_(\\d+)\\]_to_\\[[35]-UTR;noncoding_exon_(\\d+)\\]$", "$2"));
		
		// TODO
		// from_[5-UTR;intron_between_exon_1_and_2]_to_[5-UTR;noncoding_exon_2]
			
			
		} else {
			System.err.println("#WARN did not parse location '" + full_location + "'");
		}
		 
		return true;
	}
	
	
	/**
	 * Transvar specific the location of a variant in terms of inside a (coding/noncoding) exon, intron, intergenic, 3/5'-UTR;
	 * or as stretching from one such location to another. Examples:<br/>
	 * - inside_[cds_in_exon_100]<br/>
	 * - from_[5-UTR;intron_between_exon_1_and_2]_to_[5-UTR;noncoding_exon_2]<br/>
	 * - from_[noncoding_exon_2]_to_[intron_between_exon_1_and_2]<br/>
	 * @param transvarLocation
	 * @return
	 */
	boolean parseTransvarLocation (String transvarLocation) {
		
		if (transvarLocation.startsWith("inside")) {
			String inner = transvarLocation.replaceFirst("^inside_\\[(.+)\\]$", "$1");
			// TODO
		} else if (transvarLocation.startsWith("from")) {
			String from = transvarLocation.replaceFirst("^from\\[(.+)\\]_to_\\[(.+)\\]$", "$1");
			String to   = transvarLocation.replaceFirst("^from\\[(.+)\\]_to_\\[(.+)\\]$", "$2");
			// TODO
		} else {
			System.err.println("#WARN cannot parse transvar location '" + transvarLocation + "'");
			return false;
		}
			
		return true;
	}
	
	
	/**
	 * TODO
	 * @param loc
	 * @return
	 */
	Location parseSingleLocation (String loc) {
		Location l = new Location();
		return l;
	}
	
	

	/**
	 * For all possible* genomic changes that explain the current variant according to TransVar,
	 * return the most likely (something like Occam's razor) that was suggested by TransVar.
	 * If there are more than one equally likely (same number of nucleotide changes, for example),
	 * will return only the first change suggested by TransVar.
	 * @return
	 */
	public Candidate getBestCandidate () {
		if (candidates == null || candidates.size() == 0) return null;
		if (candidates.size() == 1) return candidates.get(0);
		for (Candidate c : candidates) {
			if (c.isBestCandidate) return c;
		}
		return candidates.get(0);
	}
	
	
	/**
	 * 
	 */
	void analyzeCandidates () {
		if (candidates.size() == 0) {
			//System.err.println("#INFO found a variant with no candidate alternate.");
		} else if (candidates.size() == 1) {
			//System.err.println("#INFO found a variant with a single candidate alternate.");

		} else if (candidates.size() >= 2) {
			Candidate first = candidates.get(0);
			List<String> altcodons = new LinkedList<>();
			for (String c : first.candidateCodons)
				altcodons.add(c);
			String refcodon = first.referenceCodon;
			List<String> usedAltCodons = new LinkedList<>();
			
			int initialSize = altcodons.size();
			int maxLoops = altcodons.size() + 1;
			
			if (strand == '+') {
				int loop = 0;
				while (usedAltCodons.size() < initialSize && loop <= maxLoops) {
					loop++;
					//System.err.println("#INFO loop " + loop);
					for (Candidate c : candidates) {
						for (String altc : altcodons) {

							if (altcodons.size() == 1 && c.getHgvsG().matches(".*[ACGT]>[ACGT]")) {
								//System.err.println("00: " + c.getHgvsG() + " / " + refcodon + " / " + altc);
								c.alternateCodon = altc;
								usedAltCodons.add(altc);
								break;
							} else if (c.getHgvsG().matches(".*del" + refcodon + "ins" + altc)) {
								//System.err.println("1a: " + c.getHgvsG() + " / " + refcodon + " / " + altc);
								c.alternateCodon = altc;
								usedAltCodons.add(altc);
								break;
							} else if (c.getHgvsG().matches(
									".*del" + refcodon.substring(0, 2) + "ins" + altc.substring(0, 2))
									&&
									refcodon.substring(2).equalsIgnoreCase(altc.substring(2))
									) {
								//System.err.println("1b: " + c.getHgvsG() + " / " + refcodon + " / " + altc);
								c.alternateCodon = altc;
								usedAltCodons.add(altc);
								break;
							} else if (c.getHgvsG().matches(
									".*del" + refcodon.substring(1, 3) + "ins" + altc.substring(1, 3))
									&&
									refcodon.substring(0, 1).equalsIgnoreCase(altc.substring(0, 1))
									) {
								//System.err.println("1c: " + c.getHgvsG() + " / " + refcodon + " / " + altc);
								c.alternateCodon = altc;
								usedAltCodons.add(altc);
								break;
							}
						}  // for each remaining alt codon
						//}
						altcodons.removeAll(usedAltCodons);
					} // for each Candidate
				} // loop
				
			} else if (strand == '-') {
				// reverseComplement
				
				// 107582281-2 ..C[CGC]C..
				// ref = GCG
				// g.107582282G>A	c.3029C>T
				// g.107582281_107582282delCGinsTA
				// g.107582281_107582282delCGinsGA
				// g.107582281_107582282delCGinsAA
				// g: ..CGC..
				// c: ..GCG..
				
				int loop = 0;
				while (usedAltCodons.size() < initialSize && loop <= maxLoops) {
					loop++;
					//System.err.println("#INFO loop " + loop);
					for (Candidate c : candidates) {
						for (String altc : altcodons) {
							if (altcodons.size() == 1 && c.getHgvsG().matches(".*[ACGT]>[ACGT]")) {
								//System.err.println("00: " + c.getHgvsG() + " / " + refcodon + " / " + altc);
								//System.err.println("#INFO Case REV 00");
								c.alternateCodon = altc;
								usedAltCodons.add(altc);
								break;
								
							} else if (c.getHgvsG().matches(".*del[ACGT]+ins[ACGT]+")) {
								
								String del = c.getHgvsG().replaceFirst("^.*del([ACGT]+)ins([ACGT]+)$", "$1");
								String ins = c.getHgvsG().replaceFirst("^.*del([ACGT]+)ins([ACGT]+)$", "$2");
								String rc_del = reverseComplement(del);
								String rc_ins = reverseComplement(ins);

								if (refcodon.equals(rc_del) && altc.equals(rc_ins)) {
									//System.err.println("#INFO Case REV 1a");
									c.alternateCodon = altc;
									usedAltCodons.add(altc);
									break;
									
								} else if (refcodon.substring(0,2).equals(rc_del) && altc.substring(0,2).equals(rc_ins)
										&& refcodon.substring(2).equals(altc.substring(2))) {
									//System.err.println("#INFO Case REV 1b");
									c.alternateCodon = altc;
									usedAltCodons.add(altc);
									break;
									
								} else if (refcodon.substring(1,3).equals(rc_del) && altc.substring(1,3).equals(rc_ins)
										&& refcodon.substring(0,1).equals(altc.substring(0,1))) {
									//System.err.println("#INFO Case REV 1c");
									c.alternateCodon = altc;
									usedAltCodons.add(altc);
									break;
								}
								
							}
						} // for each remaining alt codon
						altcodons.removeAll(usedAltCodons);
					} // for each Candidate
				} // loop
				
				
			} else {
				System.err.println("#WARN strand information missing ('" + strand + "').");
			}
			
		}
	}


	/**
	 * 
	 */
	public void print() {
		System.out.println("sourceGene="+this.sourceGene);
		System.out.println("sourceVariant="+this.sourceVariant);
		System.out.println("transcriptId="+this.transcriptId);
		System.out.println("transcriptType="+this.transcriptType);
		System.out.println("strand="+this.strand);
		System.out.println("geneModel="+this.geneModel.toString());
		System.out.println("transvarGene="+this.transvarGene);
		System.out.println("consequence="+this.consequence);

		System.out.println("aliases="+this.aliases);
		System.out.println("entrezGeneId="+this.entrezGeneId);
		System.out.println("hgncId="+this.hgncId);
		System.out.println("mimId="+this.mimId);
		System.out.println("hprdId="+this.hprdId);
		
		//System.out.println("candidateSnvs="+this.candidateSnvs);
		//System.out.println("candidateMnvs="+this.candidateMnvs);
		System.out.println("candidateMnvs="+this.candidates);
		
		System.out.println("location="+this.location);
		System.out.println("exonStart="+this.exonStart);
		System.out.println("exonEnd="+this.exonEnd);
	}
	
	
	/**
	 * Returns the header names for tab-delimited output of this record.
	 * @return a tab-delimited string with header names
	 * @see #toTSV() on how to print the values for this record
	 */
	public static String toTsvHeader () {
		return "#sourceGene\tsourceVariant\ttransvarGene\tentrezGeneId"
			 + "\tstrand"
			 + "\tgeneModel\ttranscriptId\ttranscriptType\tproteinId"
			 + "\texonStart\texonEnd\tintronStart\tintronEnd"
			 + "\tvariantType\tisBestCandidate"
			 + "\thgvsG\thgvsC\thgvsP"
			 + "\tchrom\tchromStart\tchromEnd"
			 + "\tcodonNumber\tcodonPos\treferenceCodon\talternateCodon\tcandidateCodons"
			 + "\tcdsStart\tcdsEnd\tproteinStart\tproteinEnd"
			 + "\tconsequence"
			 + "\tcandidate"
			 + "\thgncId\tmimId\thprdId\taliases\tlocation"
			;
	}
	
	
	/**
	 * Return the final tab-delimited information for this record.<br/>
	 * If there are multiple candidate genomic changes that explain the same CDS/AA change,
	 * will return multiple tab-delimited entries, separated by "\n".
	 * @return a tab-delimited string with values; multiple possible entries separated by "\n"
	 * @see #toTsvHeader() for column names
	 */
	public String toTsv () {
		StringBuilder tsv = new StringBuilder();
		
		if (candidates == null) {
			System.err.println("#INFO no suitable candidates found");
					
		} else {
		
			for (Candidate c : candidates) {
				if (tsv.length() > 0) tsv.append("\n");
				tsv.append(sourceGene + "\t" + sourceVariant);
				tsv.append("\t" + transvarGene + "\t" + entrezGeneId);
				tsv.append("\t" + strand);
				tsv.append("\t" + geneModel.toString() + "\t" + transcriptId + "\t" + transcriptType);
				tsv.append("\t" + proteinId);
				tsv.append("\t" + exonStart + "\t" + exonEnd + "\t" + intronStart + "\t" + intronEnd);
				tsv.append("\t" + c.type.toString() + "\t" + c.isBestCandidate);
				tsv.append("\t" + c.getHgvsG() + "\t" + c.getHgvsC() + "\t" + c.getHgvsP());
				tsv.append("\t" + chr + "\t" + c.chromStart + "\t" + c.chromEnd);
				tsv.append("\t" + c.getCodonNumber() + "\t" + c.getCodonPos());
				tsv.append("\t" + c.referenceCodon + "\t" + c.alternateCodon + "\t" + join(c.candidateCodons, ","));
				tsv.append("\t" + c.cdsStart + "\t" + c.cdsEnd);
				tsv.append("\t" + c.proteinStart + "\t" + c.proteinEnd);
				tsv.append("\t" + consequence);
				tsv.append("\t" + c);
				tsv.append("\t" + hgncId + "\t" + mimId + "\t" + hprdId + "\t" + join(aliases, "; ") + "\t" + location);
			}
		
		} // check if any candidates are available
		
		return tsv.toString();
	}
	
	
	/**
	 * Helper method. Should not be here.<br/>
	 * Joins an array of Strings into one String, by using the specified delimiter.
	 * If input array is NULL or empty, returns empty string "".
	 * @param list
	 * @param delimiter
	 * @return
	 */
	String join (String[] list, String delimiter) {
		if (list == null || list.length == 0) return "";
		if (list.length == 1) return list[0];
		StringBuilder s = new StringBuilder();
		s.append(list[0]);
		for (int l = 1; l < list.length; l++) {
			s.append(delimiter);
			s.append(list[l]);
		}
		return s.toString();
	}
	
	
	String emptyIfNull (String in) {
		if (in == null || in.equalsIgnoreCase("null")) return "";
		return in;
	}
	
	String NULLIfNull (String in) {
		if (in == null || in.equalsIgnoreCase("null")) return "NULL";
		return in;
	}
	
	
	/**
	 * 
	 * @param ref
	 * @param alts
	 * @return
	 */
	public static String[] findBestCodons (String ref, String[] alts) {
		Set<String> bestAlts = new LinkedHashSet<>();
		int bestAltLev = 10000;
		for (String alt : alts) {
			int lv = ld.apply(ref, alt);
			if (lv < bestAltLev) {
				bestAltLev = lv;
				bestAlts.clear();
				bestAlts.add(alt);
			}
		}
		String[] best = new String[bestAlts.size()];
		int a = 0;
		for (String alt : bestAlts) {
			best[a++] = alt;
		}
		return best;
	}


	/**
	 * 
	 * @param args
	 */
	public static void main (String[] args) {	
		String[] tlines = {

//				"WNK1:G890R"
//				+ "\tENST00000537687 (protein_coding)"
//				+ "\tWNK1"
//				+ "\t+"
//				+ "\tchr12:g.977560G>A/c.2668G>A/p.Gly890Arg"
//				+ "\tinside_[cds_in_exon_9]"
//				+ "\tCSQN=Missense;reference_codon=GGG;candidate_codons=AGG,AGA,CGA,CGC,CGG,CGT;"
//				+ "candidate_snv_variants=chr12:g.977560G>C;"
//				+ "candidate_mnv_variants=chr12:g.977560_977562delGGGinsAGA,chr12:g.977560_977562delGGGinsCGA,chr12:g.977560_977562delGGGinsCGC,chr12:g.977560_977562delGGGinsCGT;"
//				+ "aliases=ENSP00000444465;source=Ensembl"
//				,
//				
//				"ZFPM2:V339I"
//				+ "\tNM_012082 (protein_coding)"
//				+ "\tZFPM2"
//				+ "\t+"
//				+ "\tchr8:g.106813325G>A/c.1015G>A/p.Val339Ile"
//				+ "\tinside_[cds_in_exon_8]"
//				+ "\tCSQN=Missense;reference_codon=GTC;candidate_codons=ATC,ATA,ATT;"
//				+ "candidate_mnv_variants=chr8:g.106813325_106813327delGTCinsATA,chr8:g.106813325_106813327delGTCinsATT;"
//				+ "dbxref=GeneID:23414,HGNC:16700,MIM:603693;aliases=NP_036214;source=RefSeq"
//				,
//				
//				"ZNF408:R541C"
//				+ "\tNM_024741 (protein_coding)"
//				+ "\tZNF408"
//				+ "\t+"
//				+ "\tchr11:g.46726871C>T/c.1621C>T/p.Arg541Cys"
//				+ "\tinside_[cds_in_exon_5]"
//				+ "\tCSQN=Missense;reference_codon=CGC;candidate_codons=TGT,TGC;"
//				+ "candidate_mnv_variants=chr11:g.46726871_46726873delCGCinsTGT;"
//				+ "dbxref=GeneID:79797,HGNC:20041,HPRD:18338;aliases=NP_079017;source=RefSeq"
//				,
				
//				"A2ML1:R592L\tNM_144670 (protein_coding)\tA2ML1\t+\tchr12:g.9000236G>T/c.1775G>T/p.Arg592Leu"
//				+ "\tinside_[cds_in_exon_15]\tCSQN=Missense;reference_codon=CGG;"
//				+ "candidate_codons=CTT,CTG,CTA,CTC,TTA,TTG;"
//				+ "candidate_mnv_variants="
//				+ "chr12:g.9000236_9000237delGGinsTT,"
//				+ "chr12:g.9000236_9000237delGGinsTA,"
//				+ "chr12:g.9000236_9000237delGGinsTC,"
//				+ "chr12:g.9000235_9000236delCGinsTT,"
//				+ "chr12:g.9000235_9000237delCGGinsTTA;"
//				+ "dbxref=GeneID:144568,HGNC:23336,MIM:610627;aliases=NP_653271;source=RefSeq"
//				,
				
				"ABCA1:A1010V\tNM_005502 (protein_coding)\tABCA1\t-"
				+ "\tchr9:g.107582282G>A/c.3029C>T/p.Ala1010Val"
				+ "\tinside_[cds_in_exon_21]"
				+ "\tCSQN=Missense;"
				+ "reference_codon=GCG;"
				+ "candidate_codons=GTA,GTC,GTG,GTT;"
				+ "candidate_mnv_variants=chr9:g.107582281_107582282delCGinsTA,chr9:g.107582281_107582282delCGinsGA,chr9:g.107582281_107582282delCGinsAA;"
				+ "dbxref=GeneID:19,HGNC:29,HPRD:02501,MIM:600046;aliases=NP_005493;source=RefSeq"
		
		};

		//
		System.out.println(TransvarRecord.toTsvHeader());
		for (String tline : tlines) {
			TransvarRecord r = TransvarRecord.makeFromTsv(tline, LEVEL.PROTEIN);
			//r.sourceLevel = LEVEL.PROTEIN;
			System.out.println(r.toTsv());
		}
		
	}
	
	
	public String reverseComplement (String sequence) {
		String rc = "";
		for (int i = sequence.length() - 1; i >= 0; i--) {
			rc += complement(sequence.charAt(i));
		}
		return rc;
	}
	
	public char complement (char n) {
		if (n == 'A') return 'T';
		if (n == 'C') return 'G';
		if (n == 'G') return 'C';
		if (n == 'T') return 'A';
		return 'N';
	}
	
}


/**
 * 
 *
 */
class Location {
	public enum LOCATION { UTR3, UTR5, INTERGENIC, EXON, INTRON, SPLICE }
	public enum CODING { CDS, NONCODING }
	Set<LOCATION> location = new LinkedHashSet<>();
	public int position;
}


/**
 * A Candidate represents a candidate allele that would explain the observed variant. Each variant can have multiple candidates.<br/>
 * It needs to have chromosome and start/stop coordinates, as well as the genomic change ("g.123456789A>C").<br/>
 * It can optionally have CDS and protein change in HGVS format ("c.1234A>C", "p.Ala123His").
 * @author Joerg Hakenberg
 */
class Candidate implements Comparable<Candidate> {
	
	/** List of supported variant types. A candidate can have a different type from the originally observed variant. For example, an amino acid change, logged as SNV, can also result from an DELINS. 
	 */
	public enum TYPE {SNV, INS, DEL, MNV, DELINS, SV, CNV, FUSION, UNKNOWN};
	/** Type for this {@link TYPE}. */
	public TYPE type = TYPE.UNKNOWN;
	
	/** Chromosome: 1..22, X, Y, MT. */
	public String chr;
	/** Start position, inclusive, on the chromosome. */
	public int chromStart;
	/** Stop position, inclusive, on the chromosome. */
	public int chromEnd;
	/** Reference allele (one or multiple nucleotides), as read on the FWD strand. */
	public String ref;
	/** Alternate allele (one or multiple nucleotides), as read on the FWD strand. */
	public String alt;
	
	/** Change in genomic coordinates ("g.1234567A>C"). */
	private String g;
	/** Change in CDS coordinates ("c.1234A>C"). */
	private String c;
	/** Change in protein coordinates ("p.Ala123Arg"). */
	private String p;

	public int cdsStart;
	public int cdsEnd;
	public int proteinStart;
	public int proteinEnd;
	
	/** Number of the codon -- that is, corresponding to amino acid position. TODO does not support multi-AA/multi-codon changes yet.
	 *  Codon number will always refer to the first affected codon. */
	private int codonNumber;
	/** Position within a codon, if applicable: 1, 2, 3. */
	private int codonPos;
	/** Codon from the reference sequence. */
	public String referenceCodon;
	/** Codon that explains the amino acid/CDS variant. */
	public String alternateCodon;
	/** Other codons that would explain the amino acid/CDS variant. */
	public String[] candidateCodons;
	
	public boolean isBestCandidate = false;
	
	@Override
	public String toString () {
		StringBuilder s = new StringBuilder();
		s.append(type.toString());
		s.append("[chr");
		s.append(chr);
		s.append(":");
		//s.append(start);
		//if (stop > start) {
		//	s.append("_");
		//	s.append(stop);
		//}
		s.append(g);
		s.append("]");
		return s.toString();
	}
	
	/**
	 * Static factor method to intialize a new Candidate from a g. notation of the form "chr8:g.106814417_106814419delATGinsCTT".
	 * @param g_dot - genomic location and allele, in the form "chr8:g.106814417A>T"
	 * @return a Candidate object
	 */
	public static Candidate makeFromG (String g_dot) {
		Candidate c = new Candidate();
		
		c.setHgvsG(g_dot);
		
		return c;
	}
	
	public void setCodonNumber () {
		if (this.c != null && cdsStart > 0) {
			this.codonNumber = computeCodonNumber(cdsStart);
		}
	}
	
	public int getCodonNumber () {
		return this.codonNumber;
	}
	
	/**
	 * 
	 * @return
	 */
	public void setCodonPos () {
		if (this.c != null && cdsStart > 0) {
			this.codonPos = computeCodonPos(cdsStart);
		}
	}
	
	public int getCodonPos () {
		return this.codonPos;
	}
	
	public void setHgvsC (String hgvs) {
		this.c = hgvs;
		if (c.matches("c.(\\d+)_(\\d+).*?")) {
			cdsStart = Integer.parseInt(c.replaceFirst("^c.(\\d+)_(\\d+).*?$", "$1"));
			cdsEnd   = Integer.parseInt(c.replaceFirst("^c.(\\d+)_(\\d+).*?$", "$2"));
		} else if (c.matches("c.(\\d+).*")) {
			cdsStart = Integer.parseInt(c.replaceFirst("^c.(\\d+).*?$", "$1"));
			cdsEnd   = cdsStart;
		}
		setCodonPos();
		setCodonNumber();
	}
	
	public String getHgvsC () {
		return this.c;
	}
	
	public void setHgvsP (String hgvs) {
		this.p = hgvs;
		if (hgvs.matches("p.[A-Za-z\\*\\?]+(\\d+)_[A-Za-z\\*\\?]+(\\d+).*?")) {
			proteinStart = Integer.parseInt(hgvs.replaceFirst("^p.[A-Za-z\\*\\?]+(\\d+)_[A-Za-z\\*\\?]+(\\d+).*?$", "$1"));
			proteinEnd   = Integer.parseInt(hgvs.replaceFirst("^p.[A-Za-z\\*\\?]+(\\d+)_[A-Za-z\\*\\?]+(\\d+).*?$", "$2"));
		} else if (hgvs.matches("p.[A-Za-z\\*\\?]+(\\d+).*?")) {
			proteinStart = Integer.parseInt(hgvs.replaceFirst("^p.[A-Za-z\\*\\?]+(\\d+).*?$", "$1"));
			proteinEnd   = proteinStart;
		}
	}

	public String getHgvsP () {
		return this.p;
	}
	
	public void setHgvsG (String hgvs) {
		String[] loc = hgvs.split(":");
		chr = loc[0].replaceFirst("chr", "");
		
		if (loc[1].startsWith("g.")) g = loc[1];
		
		if (loc[1].matches("g\\.\\d+_\\d+.*del.*?ins.*?")) {
			chromStart = Integer.parseInt(loc[1].replaceFirst("^g\\.(\\d+)_(\\d+).*?$", "$1"));
			chromEnd   = Integer.parseInt(loc[1].replaceFirst("^g\\.(\\d+)_(\\d+).*?$", "$2"));
			if (loc[1].matches("g\\.\\d+_\\d+.*del([ACGTN]+)ins([ACGTN]+).*?")) {
				String ref = loc[1].replaceFirst("^g\\.\\d+_\\d+.*del([ACGTN]+)ins([ACGTN]+).*?$", "$1");
				String alt = loc[1].replaceFirst("^g\\.\\d+_\\d+.*del([ACGTN]+)ins([ACGTN]+).*?$", "$2");
				if (ref.length() == alt.length())
					setVariantType(TYPE.MNV);
				else
					setVariantType(TYPE.DELINS);
			} else
				setVariantType(TYPE.DELINS);
		} else if (loc[1].matches("g\\.\\d+.*del.*?ins.*?")) {
			chromStart = Integer.parseInt(loc[1].replaceFirst("^g\\.(\\d+).*?$", "$1"));
			chromEnd   = chromStart;
			setVariantType(TYPE.DELINS);
			
		} else if (loc[1].matches("g\\.\\d+_\\d+.*del.*?")) {
			chromStart = Integer.parseInt(loc[1].replaceFirst("^g\\.(\\d+)_(\\d+).*?$", "$1"));
			chromEnd   = Integer.parseInt(loc[1].replaceFirst("^g\\.(\\d+)_(\\d+).*?$", "$2"));
			setVariantType(TYPE.DEL);
		} else if (loc[1].matches("g\\.\\d+.*del.*?")) {
			chromStart = Integer.parseInt(loc[1].replaceFirst("^g\\.(\\d+).*?$", "$1"));
			chromEnd   = chromStart;
			setVariantType(TYPE.DEL);
			
		} else if (loc[1].matches("g\\.\\d+_\\d+.*ins.*?")) {
			chromStart = Integer.parseInt(loc[1].replaceFirst("^g\\.(\\d+)_(\\d+).*?$", "$1"));
			chromEnd   = Integer.parseInt(loc[1].replaceFirst("^g\\.(\\d+)_(\\d+).*?$", "$2"));
			setVariantType(TYPE.INS);
		} else if (loc[1].matches("g\\.\\d+.*ins.*?")) {
			chromStart = Integer.parseInt(loc[1].replaceFirst("^g\\.(\\d+).*?$", "$1"));
			chromEnd   = chromStart;
			setVariantType(TYPE.INS);
			
		} else if (loc[1].matches("g\\.\\d+.*")) {
			chromStart = Integer.parseInt(loc[1].replaceFirst("^g\\.(\\d+).*?$", "$1"));
			chromEnd   = chromStart;
			setVariantType(TYPE.SNV);
			
		} else {
			System.err.println("#WARN unknown variant type: " + hgvs);
		}
	}

	public String getHgvsG () {
		return this.g;
	}
	
	/**
	 * 
	 * @param type
	 */
	public void setVariantType (TYPE type) {
		this.type = type;
	}
	 
	/**
	 * Checks if this Candidate is equal to another Candidate.
	 * Equality between candidate alleles is defined by the same chromosome and genomic change.
	 */
	@Override
	public boolean equals (Object o) {
		if (!(o instanceof Candidate)) return false;
		Candidate c = (Candidate)o;
		return (this.chr.equals(c.chr) && this.getClass().equals(c.g));
	}
	
	/**
	 * Hash code depends on chromosome and genomic change.
	 */
	@Override
	public int hashCode () {
		int h = 17;
		h = 17 * h + chr.hashCode();
		h = 17 * h + g.hashCode();
		return h;
	}


	/** 
	 * Compares this Candidate to another candidate. 
	 */
	@Override
	public int compareTo (Candidate c) {
		return 0;
	}
	
	public static int computeCodonNumber (int cdsPos) {
		int t = (cdsPos / 3);
		if (computeCodonPos(cdsPos) == 3) return t;
		return t + 1;
	}
	
	public static int computeCodonPos (int cdsPos) {
		int t = cdsPos % 3;
		if (t == 0) return 3;
		return t;
	}
	
}


