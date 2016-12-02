package transvar;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TransvarRecord {

	/** The gene originally provided as input to TransVar. */
	public String sourceGene;
	/** The genetic variant provided as input to TransVar. */
	public String sourceVariant;
	
	/**	 */
	public boolean predictedFromAminoAcidChange = true;
	
	public enum GENE_MODEL {ENSEMBL, REFSEQ, UCSC, UNKNOWN};
	public GENE_MODEL geneModel = GENE_MODEL.UNKNOWN;
	
	/** */
	public String transcriptId;
	/** Type of variant: protein coding, etc. */
	public String transcriptType;
	
	/** Gene symbol by TransVar. */
	public String transvarGene;
	/** */
	public String strand;
	
	public String chr;
	//public String gHgvs;
	//public String cHgvs;
	//public String pHgvs;
	
	public String location;
	public String CQSN;
	
	/** Consequence on protein level: missense, nonsense, ... */
	public String consequence;
	
	//public String referenceCodon;
	public String[] candidateCodons;
	
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
	
	public List<Candidate> candidates = new LinkedList<Candidate>();
	
	
	/**
	 * Static factory to initialize a TransvarRecord from a TransVar output line.
	 * @param line
	 * @return
	 */
	public static TransvarRecord makeFromTsv (String line) {
		TransvarRecord r = new TransvarRecord();
		
		String[] cols = line.split("\t");
		
		for (String col : cols)
			System.out.println(col);
		
		String[] s = cols[0].split(":");
		r.sourceGene = s[0];
		r.sourceVariant = s[1];
		
		if (cols[6].equals("no_valid_transcript_found")) {
			r.transcriptType = "no_valid_transcript_found";
			return r;
		}
		
		String t[] = cols[1].split(" ");
		r.transcriptId = t[0];
		r.transcriptType = t[1].substring(1, t[1].length() - 1);
		
		r.transvarGene = cols[2];
		r.strand = cols[3];

		// chr9:g.107583774C>T/c.2842G>A/p.Gly948Arg
		String[] hgvs = cols[4].split("/");
		
		// get the first candidate from the suggested HGVS in column 5
		Candidate c1 = Candidate.makeFromG(hgvs[0]);
		c1.c = hgvs[1];
		c1.p = hgvs[2];
		r.chr = c1.chr;		
		//r.chr = hgvs[0].split(":")[0].replaceFirst("chr", "");
		//r.gHgvs = hgvs[0].split(":")[1];
		//r.cHgvs = hgvs[1];
		//r.pHgvs = hgvs[2];
		
		// get all other columns from the candidate_snv_variants and candidate_mnv_variants fields, see below
		
		r.location = cols[5];
		if (r.location.matches("inside_\\[cds_in_exon_\\d+\\]")) {
			r.exonStart = Integer.parseInt(r.location.replaceFirst("^inside_\\[cds_in_exon_(\\d+)\\]$", "$1"));
			r.exonEnd = r.exonStart;
		} else if (r.location.matches("inside_\\[cds_in_exons_\\[\\d+,\\d+\\]\\]")) {
			r.exonStart = Integer.parseInt(r.location.replaceFirst("^inside_\\[cds_in_exons_\\[(\\d+),(\\d+)\\]\\]$", "$1"));
			r.exonEnd   = Integer.parseInt(r.location.replaceFirst("^inside_\\[cds_in_exons_\\[(\\d+),(\\d+)\\]\\]$", "$2"));
		} 
		
		// generate a key/value map of consequence fields
		Map<String, String> cMap = new HashMap<String, String>();
		String[] CSQN = cols[6].split(";");
		for (String C : CSQN) {
			String key = C.split("=")[0];
			String val = C.split("=")[1];
			cMap.put(key, val);
		}
		if (cMap.containsKey("CSQN")) r.consequence = cMap.get("CSQN");
		
		//if (cMap.containsKey("reference_codon")) r.referenceCodon = cMap.get("reference_codon");
		//if (cMap.containsKey("candidate_codons")) r.candidateCodons = cMap.get("candidate_codons").split(",");
		//if (cMap.containsKey("candidate_snv_variants")) r.candidateSnvs = cMap.get("candidate_snv_variants").split(",");
		//if (cMap.containsKey("candidate_mnv_variants")) r.candidateMnvs = cMap.get("candidate_mnv_variants").split(",");

		if (cMap.containsKey("reference_codon")) {
			c1.referenceCodon = cMap.get("reference_codon");
		}
		
		// add the suggested 'best' candidate
		r.candidates.add(c1);
		
		// add additional candidates listed in the candidate_snv_variants and candidate_mnv_variants fields
		if (cMap.containsKey("candidate_snv_variants")) {
			String[] candidateList = cMap.get("candidate_snv_variants").split(",");
			for (String can : candidateList) {
				Candidate c2 = Candidate.makeFromG(can);
				if (r.predictedFromAminoAcidChange) c2.p = c1.p;
				else c2.c = c1.c;
				r.candidates.add(c2);
			}
		}
		if (cMap.containsKey("candidate_mnv_variants")) {
			String[] candidateList = cMap.get("candidate_mnv_variants").split(",");
			for (String can : candidateList) {
				Candidate c2 = Candidate.makeFromG(can);
				if (r.predictedFromAminoAcidChange) c2.p = c1.p;
				else c2.c = c1.c;
				r.candidates.add(c2);
			}
		}
		

		if (cMap.containsKey("source")) r.geneModel = GENE_MODEL.valueOf(cMap.get("source").toUpperCase());
		
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
				if (alias.startsWith("NP_")) r.proteinId = alias;
				else if (alias.startsWith("ENSP")) r.proteinId = alias;
		}
		
		return r;
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
		
		System.out.println("candidateSnvs="+this.candidateSnvs);
		System.out.println("candidateMnvs="+this.candidateMnvs);
		
		System.out.println("location="+this.location);
		System.out.println("exonStart="+this.exonStart);
		System.out.println("exonEnd="+this.exonEnd);
	}
	
	
	public static String toTsvHeader () {
		return "#sourceGene\tsourceVariant\ttransvarGene\tentrezGeneId\tstrand\tgeneModel\ttranscriptId\ttranscriptType"
			 + "\tproteinId\texonStart\texonEnd\thgvsG\thgvsC\thgvsP\tchrom\tcandidate\tisSnv\treferenceCodon\talternateCodon\tcandidateCodons"
			 + "\thgncId\tmimId\thprdId\taliases\tlocation"
			;
	}
	
	public String toTsv () {
		StringBuilder tsv = new StringBuilder();
		
		if (candidateSnvs != null)
		for (String c : candidateSnvs) {
			if (tsv.length() > 0) tsv.append("\n");
			tsv.append(sourceGene + "\t" + sourceVariant);
			tsv.append("\t" + transvarGene + "\t" + entrezGeneId);
			tsv.append("\t" + strand);
			tsv.append("\t" + geneModel.toString() + "\t" + transcriptId + "\t" + transcriptType);
			tsv.append("\t" + proteinId);
			tsv.append("\t" + exonStart + "\t" + exonEnd);
			tsv.append("\t" + gHgvs + "\t" + cHgvs + "\t" + pHgvs);
			tsv.append("\t" + chr + "\t" + c + "\ttrue");
			tsv.append("\t" + referenceCodon);
			// TODO get the alternate codon from the (list of) alternate codons by comparing to the current candidateSnv -- they are sorted alphabetically :( and not in the same order as the candidateSnvs :( :(
			String alternateCodon = "?";
			tsv.append("\t" + alternateCodon);
			tsv.append("\t" + join(candidateCodons, "; "));
			tsv.append("\t" + consequence);
			tsv.append("\t" + hgncId + "\t" + mimId + "\t" + hprdId + "\t" + join(aliases, "; ") + "\t" + location);
		}
		
		if (candidateMnvs != null)
		for (String c :  candidateMnvs) {
			if (tsv.length() > 0) tsv.append("\n");
			tsv.append(sourceGene + "\t" + sourceVariant);
			tsv.append("\t" + transvarGene + "\t" + entrezGeneId);
			tsv.append("\t" + strand);
			tsv.append("\t" + geneModel.toString() + "\t" + transcriptId + "\t" + transcriptType);
			tsv.append("\t" + proteinId);
			tsv.append("\t" + exonStart + "\t" + exonEnd);
			tsv.append("\t" + gHgvs + "\t" + cHgvs + "\t" + pHgvs);
			tsv.append("\t" + chr + "\t" + c + "\tfalse" + "\t" + join(candidateCodons, "; "));
			tsv.append("\t" + referenceCodon);
			// TODO get the alternate codon from the (list of) alternate codons by comparing to the current candidateSnv -- they are sorted alphabetically :( and not in the same order as the candidateSnvs :( :(
			String alternateCodon = "?";
			tsv.append("\t" + alternateCodon);
			tsv.append("\t" + consequence);
			tsv.append("\t" + hgncId + "\t" + mimId + "\t" + hprdId + "\t" + join(aliases, "; ") + "\t" + location);
		}
		
		
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
	
}


/**
 * A Candidate represents a candidate allele that would explain the observed variant. Each variant can have multiple candidates.<br/>
 * It needs to have chromosome and start/stop coordinates, as well as the genomic change ("g.123456789A>C").<br/>
 * It can optionally have CDS and protein change in HGVS format ("c.1234A>C", "p.Ala123His").
 * @author Joerg Hakenberg
 */
class Candidate implements Comparable<Candidate> {
	
	/** List of supported variant types. A candidate can have a different type from the originally observed variant. For example, an amino acid change, logged as SNV, can also result from an DELINS. 
	 *  An MNV is a DELINS of equal size for ref and alt sequence, we omit it for simplicity. */
	public enum TYPE {SNV, INS, DEL, DELINS, SV, CNV, FUSION, UNKNOWN};
	/** Type for this {@link TYPE}. */
	public TYPE type = TYPE.UNKNOWN;
	
	/** Chromosome: 1..22, X, Y, MT. */
	public String chr;
	/** Start position, inclusive, on the chromosome. */
	public int start;
	/** Stop position, inclusive, on the chromosome. */
	public int stop;
	
	/** Change in genomic coordinates ("g.1234567A>C"). */
	public String g;
	/** Change in CDS coordinates ("c.1234A>C"). */
	public String c;
	/** Change in protein coordinates ("p.Ala123Arg"). */
	public String p;
	
	/** Codon from the reference sequence. */
	public String referenceCodon;
	/** Codon that explains the amino acid/CDS variant. */
	public String alternateCodon;
	
	/**
	 * Static factor method to intialize a new Candidate from a g. notation of the form "chr8:g.106814417_106814419delATGinsCTT".
	 * @param g_dot
	 * @return
	 */
	public static Candidate makeFromG (String g_dot) {
		Candidate c = new Candidate();
		String[] loc = g_dot.split(":");
		c.chr = loc[0].replaceFirst("chr", "");
		if (loc[1].startsWith("g.")) c.g = loc[1];
		if (loc[1].matches("g\\.\\d+_\\d+.*?")) {
			c.start = Integer.parseInt(loc[1].replaceFirst("^g\\.(\\d+)_(\\d+).*?$", "$1"));
			c.stop  = Integer.parseInt(loc[1].replaceFirst("^g\\.(\\d+)_(\\d+).*?$", "$2"));
			c.type = TYPE.SNV;
		} else if (loc[1].matches("g\\.\\d+.*")) {
			c.start = Integer.parseInt(loc[1].replaceFirst("^g\\.(\\d+).*?$", "$1"));
			c.stop  = c.start;
			c.type = TYPE.DELINS;
		}
		return c;
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
	
}


/*

Columns:

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

*/