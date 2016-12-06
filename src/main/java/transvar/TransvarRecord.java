package transvar;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.text.similarity.LevenshteinDistance;

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
	
	public enum GENE_MODEL {ENSEMBL, REFSEQ, UCSC, UNKNOWN};
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
	
	public String location;
	public String CQSN;
	
	/** Consequence on protein level: missense, nonsense, ... */
	public String consequence;
	
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
		TransvarRecord r = new TransvarRecord();
		r.transvarSource = line;
		r.sourceLevel = sourceLevel;

		String[] cols = line.split("\t");
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

		if (cMap.containsKey("reference_codon")) {
			c1.referenceCodon = cMap.get("reference_codon");
		}
		
		if (cMap.containsKey("candidate_codons")) {
			//r.candidateCodons = cMap.get("candidate_codons").split(",");
			//r.bestAltCodons = findBestCodons(c1.referenceCodon, r.candidateCodons);
			c1.candidateCodons = cMap.get("candidate_codons").split(",");
		}
		
		// add the suggested 'best' candidate
		r.candidates.add(c1);

		// add additional candidates listed in the candidate_snv_variants and candidate_mnv_variants fields
		if (cMap.containsKey("candidate_snv_variants")) {
			String[] candidateList = cMap.get("candidate_snv_variants").split(",");
			for (String can : candidateList) {
				Candidate c2 = Candidate.makeFromG(can);
				c2.referenceCodon = c1.referenceCodon;
				if (r.sourceLevel == LEVEL.PROTEIN) c2.setHgvsP(c1.getHgvsP());
				else c2.setHgvsC(c1.getHgvsC());
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
				else c2.setHgvsC(c1.getHgvsC());
				if (cMap.containsKey("candidate_codons")) c2.candidateCodons = cMap.get("candidate_codons").split(",");
				r.candidates.add(c2);
			}
		}
		
		// analyze all candidate to assign the correct alternate allele and codon
		r.analyzeCandidates();

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
				if (alias.startsWith("NP_") || alias.startsWith("XP_") || alias.startsWith("ENSP")) r.proteinId = alias;
		}

		return r;
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
			System.err.println("#INFO found a variant with no candidate alternate.");
		} else if (candidates.size() == 1) {
			System.err.println("#INFO found a variant with a single candidate alternate.");

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
						if (altcodons.size() == 1) {
							String lastAlt = altcodons.get(0);
							c.alternateCodon = lastAlt;
							usedAltCodons.add(lastAlt);
						} else {
							for (String altc : altcodons) {
								//System.err.println("#INFO ref12=" + refcodon.substring(0, 2) + " / alt12=" + altc.substring(0, 2));
								if (c.getHgvsG().matches(".*del" + refcodon + "ins" + altc)) {
									c.alternateCodon = altc;
									usedAltCodons.add(altc);
									break;
								} else if (c.getHgvsG().matches(
										".*del" + refcodon.substring(0, 2) + 
										"ins" + altc.substring(0, 2))) {
									c.alternateCodon = altc;
									usedAltCodons.add(altc);
									break;
								} else if (c.getHgvsG().matches(
										".*del" + refcodon.substring(1, 3) + 
										"ins" + altc.substring(1, 3))) {
									c.alternateCodon = altc;
									usedAltCodons.add(altc);
									break;
								}
							}
						}
						altcodons.removeAll(usedAltCodons);
					}
				}
			} else {
				System.err.println("#WARN not handling reverse strand yet to determine alternate codons.");
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
			 + "\texonStart\texonEnd"
			 + "\tvariantType\tisBestCandidate"
			 + "\thgvsG\thgvsC\thgvsP"
			 + "\tchrom"
			 + "\tcandidate\tcdsStart\tcdsEnd\tcodonNumber\tcodonPos\treferenceCodon\talternateCodon\tcandidateCodons"
			 + "\tproteinStart\tproteinEnd"
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
				tsv.append("\t" + exonStart + "\t" + exonEnd);
				tsv.append("\t" + c.type.toString() + "\t" + c.isBestCandidate);
				tsv.append("\t" + c.getHgvsG() + "\t" + c.getHgvsC() + "\t" + c.getHgvsP());
				tsv.append("\t" + chr);
				
				// TODO get the alternate codon from the (list of) alternate codons by comparing to the current candidateSnv -- they are sorted alphabetically :( and not in the same order as the candidateSnvs :( :(
				tsv.append("\t" + c);
				tsv.append("\t" + c.cdsStart + "\t" + c.cdsEnd);
				tsv.append("\t" + c.getCodonNumber() + "\t" + c.getCodonPos());
				tsv.append("\t" + c.referenceCodon);
				tsv.append("\t" + c.alternateCodon);
				tsv.append("\t" + join(c.candidateCodons, ","));
				tsv.append("\t" + c.proteinStart + "\t" + c.proteinEnd);
				
				tsv.append("\t" + consequence);
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
//		System.out.println(Candidate.computeCodonPos(1799) + "\t" + Candidate.computeCodonNumber(1799));
//		System.out.println();
//		System.out.println(Candidate.computeCodonPos(1) + "\t" + Candidate.computeCodonNumber(1));
//		System.out.println(Candidate.computeCodonPos(2) + "\t" + Candidate.computeCodonNumber(2));
//		System.out.println(Candidate.computeCodonPos(3) + "\t" + Candidate.computeCodonNumber(3));
//		System.out.println(Candidate.computeCodonPos(4) + "\t" + Candidate.computeCodonNumber(4));
//		System.out.println(Candidate.computeCodonPos(5) + "\t" + Candidate.computeCodonNumber(5));
//		System.out.println(Candidate.computeCodonPos(6) + "\t" + Candidate.computeCodonNumber(6));
//		System.out.println(Candidate.computeCodonPos(7) + "\t" + Candidate.computeCodonNumber(7));
		
		String[] tlines = {
				
				"WNK1:G890R"
				+ "\tENST00000537687 (protein_coding)"
				+ "\tWNK1"
				+ "\t+"
				+ "\tchr12:g.977560G>A/c.2668G>A/p.Gly890Arg"
				+ "\tinside_[cds_in_exon_9]"
				+ "\tCSQN=Missense;reference_codon=GGG;candidate_codons=AGG,AGA,CGA,CGC,CGG,CGT;"
				+ "candidate_snv_variants=chr12:g.977560G>C;"
				+ "candidate_mnv_variants=chr12:g.977560_977562delGGGinsAGA,chr12:g.977560_977562delGGGinsCGA,chr12:g.977560_977562delGGGinsCGC,chr12:g.977560_977562delGGGinsCGT;"
				+ "aliases=ENSP00000444465;source=Ensembl"
				
				, "ZFPM2:V339I"
				+ "\tNM_012082 (protein_coding)"
				+ "\tZFPM2"
				+ "\t+"
				+ "\tchr8:g.106813325G>A/c.1015G>A/p.Val339Ile"
				+ "\tinside_[cds_in_exon_8]"
				+ "\tCSQN=Missense;reference_codon=GTC;candidate_codons=ATC,ATA,ATT;"
				+ "candidate_mnv_variants=chr8:g.106813325_106813327delGTCinsATA,chr8:g.106813325_106813327delGTCinsATT;"
				+ "dbxref=GeneID:23414,HGNC:16700,MIM:603693;aliases=NP_036214;source=RefSeq"
				
				, "ZNF408:R541C"
				+ "\tNM_024741 (protein_coding)"
				+ "\tZNF408"
				+ "\t+"
				+ "\tchr11:g.46726871C>T/c.1621C>T/p.Arg541Cys"
				+ "\tinside_[cds_in_exon_5]"
				+ "\tCSQN=Missense;reference_codon=CGC;candidate_codons=TGT,TGC;"
				+ "candidate_mnv_variants=chr11:g.46726871_46726873delCGCinsTGT;"
				+ "dbxref=GeneID:79797,HGNC:20041,HPRD:18338;aliases=NP_079017;source=RefSeq"

		};

		//
		System.out.println(TransvarRecord.toTsvHeader());
		for (String tline : tlines) {
			TransvarRecord r = TransvarRecord.makeFromTsv(tline, LEVEL.PROTEIN);
			//r.sourceLevel = LEVEL.PROTEIN;
			System.out.println(r.toTsv());
		}
		
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
	 */
	public enum TYPE {SNV, INS, DEL, MNV, DELINS, SV, CNV, FUSION, UNKNOWN};
	/** Type for this {@link TYPE}. */
	public TYPE type = TYPE.UNKNOWN;
	
	/** Chromosome: 1..22, X, Y, MT. */
	public String chr;
	/** Start position, inclusive, on the chromosome. */
	public int start;
	/** Stop position, inclusive, on the chromosome. */
	public int stop;
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
			start = Integer.parseInt(loc[1].replaceFirst("^g\\.(\\d+)_(\\d+).*?$", "$1"));
			stop  = Integer.parseInt(loc[1].replaceFirst("^g\\.(\\d+)_(\\d+).*?$", "$2"));
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
			start = Integer.parseInt(loc[1].replaceFirst("^g\\.(\\d+).*?$", "$1"));
			stop  = start;
			setVariantType(TYPE.DELINS);
			
		} else if (loc[1].matches("g\\.\\d+_\\d+.*del.*?")) {
			start = Integer.parseInt(loc[1].replaceFirst("^g\\.(\\d+)_(\\d+).*?$", "$1"));
			stop  = Integer.parseInt(loc[1].replaceFirst("^g\\.(\\d+)_(\\d+).*?$", "$2"));
			setVariantType(TYPE.DEL);
		} else if (loc[1].matches("g\\.\\d+.*del.*?")) {
			start = Integer.parseInt(loc[1].replaceFirst("^g\\.(\\d+).*?$", "$1"));
			stop  = start;
			setVariantType(TYPE.DEL);
			
		} else if (loc[1].matches("g\\.\\d+_\\d+.*ins.*?")) {
			start = Integer.parseInt(loc[1].replaceFirst("^g\\.(\\d+)_(\\d+).*?$", "$1"));
			stop  = Integer.parseInt(loc[1].replaceFirst("^g\\.(\\d+)_(\\d+).*?$", "$2"));
			setVariantType(TYPE.INS);
		} else if (loc[1].matches("g\\.\\d+.*ins.*?")) {
			start = Integer.parseInt(loc[1].replaceFirst("^g\\.(\\d+).*?$", "$1"));
			stop  = start;
			setVariantType(TYPE.INS);
			
		} else if (loc[1].matches("g\\.\\d+.*")) {
			start = Integer.parseInt(loc[1].replaceFirst("^g\\.(\\d+).*?$", "$1"));
			stop  = start;
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