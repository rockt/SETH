---
layout: default
---

SETH is a software that performs named entity recognition (NER) of genetic variants (with an emphasis on single nucleotide polymorphisms (SNPs) and other short sequence variations) from natural language texts. 
SETH allows to recognize the following mutation subtypes: substitution, deletion, insertion, duplication, insertion-deletion (insdel), inversion, conversion, translocation, frameshift, short-sequence repeat, and literal dbSNP mention.
Recognized mutation mentions can be grounded to the Human Mutation Nomenclature (HGVS) and normalized to dbSNP identifiers or UniProt sequences. For NER SETH builds on four individual components:

1.) Mutations following the [HGVS nomenclature](http://www.hgvs.org/mutnomen/) (den Dunnen and Antonarakis, 2000) are recognized by implementing an Extended Backus–Naur (EBNF) grammar proposed by Laros *et al.* (2011) using Scala combinators. We modified this grammar to allow to detect frequently observed deviations from the nomenclature 

2.)To get hold of substitutions not following the nomenclature, SETH integrates MutationFinder (Caporaso *et al.*, 2007).
SETH modifies MutationFinder's original capabilities in order to match a wider scope of substitutions (DNA substitutions, nonsense mutations, and ambiguous mutations) not following the HGVS nomenclature. This is done by modifying the original MutationFinder implementation together with additional and modified regular expressions.

3.) Mutations (substitutions, deletions, insersions, frameshifts, ...) not following the HGVS nomenclature, but earlier proposals for a nomenclature,  are recognized using a separate set of regular expressions.

4.) Mutations described as literal dbSNP-identifiers are recongized using a regular expression.

Results from the four different components are collected, merged,  and represented as the following object  [MutationMention](https://github.com/rockt/SETH/blob/master/src/main/java/de/hu/berlin/wbi/objects/MutationValidation.java).
The general NER-workflow is also depicted in the following figure.

![Workflow](https://raw.githubusercontent.com/rockt/SETH/ad7b9fbccd976a6775a03daf332b08ee52a08a0f/images/dataflow.png "NER worflow of SETH").

If possible, extracted SNP mentions are linked to [dbSNP](http://www.ncbi.nlm.nih.gov/SNP/) or [UniProt-KB seqeuence](http://www.uniprot.org/help/uniprotkb). 
This process  is referred to as named entity normalization (NEN). 
For normalization SETH requires a list of potential entrez gene candidates/identifiers as well as a local dbSNP or UniProt database. 
Gene names may either come from dedicated gene name recognition and normaluzation tools, such as [GNAT](http://gnat.sourceforge.net/).
Alternatively, we recomend the use of NCBI's gene2pubmed [database](ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/gene2pubmed.gz).
SETH currently uses these two data-sources but can easily extended with other gene-NER tools.

# Get SETH

## Download ready-to-use [seth.jar](https://www.informatik.hu-berlin.de/forschung/gebiete/wbi/resources/chemspot/seth.jar)

## Build SETH on your own
	git clone https://github.com/rockt/SETH.git
	cd SETH
	mvn clean compile assembly:single
	mv ./target/seth-1.2-Snapshot-jar-with-dependencies.jar seth.jar

# Examples for NER

## Command-line Usage

    java -cp seth.jar seth.ner.wrapper.SETHNERAppMut "Causative GJB2 mutations were identified in 31 (15.2%) patients, and two common mutations, c.35delG and L90P (c.269T>C), accounted for 72.1% and 9.8% of GJB2 disease alleles."
	MutationMention [span=91-99, location=35, wtResidue=G, text=c.35delG, type=DELETION, tool=SETH]
	MutationMention [span=104-108, mutResidue=P, location=90, wtResidue=L, text=L90P, type=SUBSTITUTION, tool=MUTATIONFINDER]
	MutationMention [span=110-118, mutResidue=C, location=269, wtResidue=T, text=c.269T>C, type=SUBSTITUTION, tool=SETH]
           
    java -cp seth.jar seth.ner.wrapper.SETHNERAppMut "G-banding and spectral karyotyping showed 46,XX,t(9;11)(p22;p15)."
	MutationMention [span=42-64, text=46,XX,t(9;11)(p22;p15), type=COPY_NUMBER_VARIATION, tool=SETH]	

# NEN
Given mentions of SNPs and a list of genes (*i.e.* [Entrez gene](http://www.ncbi.nlm.nih.gov/gene) identifiers), SETH normalizes SNPs to
dbSNP identifiers.
To extract gene mentions, we use the output of the tool [GNAT](http://gnat.sourceforge.net/) (Hakenberg *et al.*, 2011)
together with the gene2pubmed information from NCBI.
Parts of the [dbSNP database](http://www.ncbi.nlm.nih.gov/projects/SNP/) have to be locally installed for speeding up
the normalization process.
For user convenience, we provide a [dump as embedded Derby database](https://docs.google.com/file/d/0B9uTfq0OyHAsdDNMQzNxWDRhZVE/edit?usp=sharing) (~2GB).
Please note, that this derby database dump contains onlyhuman results for human databases (UniProt, dbSNP, GNAT, gene2Pubmed). Otherwise the resulting derby database becomes too large for distribution.
At the end of this readme we describe the process to build the database. 
This process can be adapted for other species...
Feel free to contact us if you observe any problems, or if you would like to host database dumps for other species or database systems.

## Command-line Usage
To use SETH's NEN component from the command line, you need to provid a [XML property file](https://github.com/rockt/SETH/blob/master/resources/seth_properties.xml) 
that handles the connection to the Derby database. 
Subsequently, you can provide a tab-seperated [file](https://github.com/rockt/SETH/blob/master/resources/snpExample.txt) (with PubMed ID, mutation mention, start- and end-position) 
SETH should normalize to dbSNP (*i.e.* rs numbers).

    java -cp seth.jar de.hu.berlin.wbi.process.Normalize resources/property.xml resources/snpExample.txt

	Normalising mutations from 'resources/snpExample.txt' and properties from 'resources/property.xml'
	16 mutations for normalisation loaded
	    PMID    Mutation	Start	End	dbSNP
	15345705      G1651A      419   425
	15290009       V158M      149   158    rs4680
	15645182        A72S       15    23    rs6267
	14973783       V103I      208   213 rs2229616
	15457404       V158M      937   946    rs4680
	15098000       V158M      349   358    rs4680
	12600718       R219K      599   604 rs2230806
	12600718      R1587K      945   951 rs2230808
	15245581       T321C     1356  1361 rs3747174
	15464268        S12T      621   625    rs1937
	11933203       C707T      685   691
	14984467     C17948T      577   585
	15268889      G2014A     1316  1322
	15670788      C2757G      726   733
	15670788      C5748T      755   762
	15564288        R72P     1204  1212 rs1042522
	15564288       K751Q      747   756   rs13181
	15564288       D312N      722   731 rs1799793
	14755442      E2578G      699   709 rs1009382
	15615772       P141L     1040  1045 rs2227564
	
	Normalization possible for 14/20 mentions

# Code Example
SETH allows simple integration into your Java-Projects. In this section we provide some examples to perform NER and NEN.


## A complete pipeline performing all steps (NER+NEN) can be found here:
[Java](https://github.com/rockt/SETH/blob/master/src/main/java/seth/SETH.java#L1190-L230)



## Example using only the backus naur grammar in Scala EBNF for HGVS mutation nomenclature implemented as parser combinators
[Scala](https://github.com/rockt/SETH/blob/master/src/main/scala/seth/ner/SETHNER.scala#L128-L356)


# Reproducing our results

## Evaluate NER

<!---
#### Human Mutation corpus I (210 abstracts)
    java -cp seth.jar seth.seth.eval.ApplyNER resources/humu/corpus.txt resources/mutations.txt false resources/humu.seth
    java -cp seth.jar seth.seth.eval.EvaluteNaER resources/humu.seth resources/humu/yearMapping.txt  resources/humu/annotations/
Precision 0.98
Recall    0.84
F₁        0.90

#### Human Mutation corpus II (420 abstracts)
    java -cp seth.jar seth.seth.eval.ApplyNER resources/american/corpus.txt resources/mutations.txt false resources/american.seth
    java -cp seth.jar seth.seth.eval.EvaluateNER resources/american.seth resources/american/yearMapping.txt resources/american/annotations/
Precision 0.88
Recall    0.82
F₁        0.85
-->

#### SETH corpus (630 abstracts)
    java -cp seth.jar seth.seth.eval.ApplyNER resources/SETH-corpus/corpus.txt resources/mutations.txt false resources/SETH-corpus.seth
    java -cp seth.jar seth.seth.eval.EvaluateNER resources/SETH-corpus.seth resources/SETH-corpus/yearMapping.txt  resources/SETH-corpus/annotations/
    
Precision 0.98
Recall    0.86
F₁        0.91


#### MutationFinder-development  corpus using original MutationFinder evaluation scripts (Caporaso *et al.*, 2007) 
    java -cp seth.jar seth.seth.eval.ApplyNER resources/mutationfinder/corpus/devo_text.txt resources/mutations.txt true resources/devo_text.seth
    python resources/mutationfinder/origDist/performance.py resources/devo_text.seth  resources/mutationfinder/corpus/devo_gold_std.txt
    
Precision 0.97
Recall    0.83
F₁        0.89

#### MutationFinder-test  corpus using original MutationFinder evaluation scripts (Caporaso *et al.*, 2007) 
    java -cp seth.jar seth.seth.eval.ApplyNER resources/mutationfinder/corpus/test_text.txt resources/mutations.txt true resources/test_text.seth
    python resources/mutationfinder/origDist/performance.py resources/test_text.seth  resources/mutationfinder/corpus/test_gold_std.txt
    
Precision 0.97
Recall    0.81
F₁        0.88

#### Corpus of Wei *et al.* (2013)
    java -cp seth.jar seth.seth.eval.ApplyNERToWei resources/Wei2013/train.txt  resources/mutations.txt  resources/Wei2013.seth
    java -cp seth.jar seth.seth.eval.EvaluateWei resources/Wei2013/train.txt resources/Wei2013.seth
    
Precision 0.94
Recall    0.80
F₁        0.86

#### Corpus of Wei *et al.* (2013); test
    java -cp seth.jar seth.seth.eval.ApplyNERToWei resources/Wei2013/test.txt  resources/mutations.txt  resources/Wei2013.seth
    java -cp seth.jar seth.seth.eval.EvaluateWei resources/Wei2013/test.txt resources/Wei2013.seth
    
Precision 0.95
Recall    0.77
F₁        0.85



#### Corpus of Verspoor *et al.* (2013)
    java -cp seth.jar seth.seth.eval.ApplyNerToVerspoor resources/Verspoor2013/corpus/ resources/mutations.txt resources/Verspoor2013.seth
    java -cp seth.jar seth.seth.eval.EvaluateVerspoor resources/Verspoor2013/annotations/ resources/Verspoor2013.seth
    
Precision 0.87
Recall    0.14
F₁        0.24

## Evaluate NEN

#### Corpus of Thomas *et al.* (2011)
    java -cp seth.jar de.hu.berlin.wbi.process.Evaluate myProperty.xml resources/thomas2011/corpus.txt
    
Precision 0.96
Recall    0.58
F₁        0.72

#### Corpus of OSIRIS (Furlong *et al.*, 2008)
    java -cp seth.jar de.hu.berlin.wbi.process.osiris.EvaluateOsiris myProperty.xml resources/OSIRIS/corpus.xml
    
Precision 0.96
Recall    0.86
F₁        0.91


<!--
#### Cosmic corpus as introduced in Yepes and Verspoor (2014)


|**Data set** | **Tool (setting)** | **Predictions** | **Matched** | **Without gene** |
|:---------|:---------------|------------:|--------:|-------------:|
|table     | SETH-NER       | 704         | 179     | 207          |
|table     | MutationFinder | ---         | 462     | 564          |
|table     | MF++           | 1304        | 481     | 578          |
|table     | Regex          | 107         | 14      | 14           |
|table     | Full-SETH      | 2102        | 646     | 771          |

|pdf.all   | SETH-NER       | 1539        | 149     | 237          |
|pdf.all   | MF++           | 2434        | 697     | 978          |
-->


# References
- Caporaso, J. G. *et al.* (2007). 
**MutationFinder: a high-performance system for extracting point mutation mentions from text.** 
Bioinformatics, 23(14), 1862–1865. 
- den Dunnen, J. T. and Antonarakis, S. E. (2000). 
**Mutation nomenclature extensions and suggestions to describe complex mutations: a discussion.**
Human Mutat, 15(1), 7–12.
- Furlong, L. I. *et al.* (2008). 
**Osirisv1.2: a named entity recognition system for sequence variants of genes in biomedical literature.** 
BMC Bioinformatics, 9, 84.
- Hakenberg, J. *et al.* (2011). 
**The GNAT library for local and remote gene mention normalization.** 
Bioinformatics 27(19):2769-71
- Laros, J. F. J. *et al.* (2011). 
**A formalized description of the standard human variant nomenclature in Extended Backus-Naur Form.** 
BMC bioinformatics, 12 Suppl 4(Suppl 4), S5.
- Thomas, P. E. *et al.* (2011). 
**Challenges in the association of human single nucleotide polymorphism mentions with unique database identifiers.** 
BMC Bioinformatics, 12 Suppl 4, S4.
- Verspoor, K. *et al.* (2013). 
**Annotating the biomedical literature for the human variome.** 
Database (Oxford).
- Wei, C.-H. *et al.* (2013). 
**tmvar: a text mining approach for extracting sequence variants in biomedical literature.** 
Bioinformatics, 29(11), 1433–1439.
- Yepes, A. J. and Verspoor, K. 
**Mutation extraction tools can be combined for robust recognition of genetic variants in the literature**
F1000Research 2014, 3:18 

# Rebuilding the database used for SNP normalization
**WARNING:** We provide a stand-alone (embedded) [Derby database](https://docs.google.com/file/d/0B9uTfq0OyHAsdDNMQzNxWDRhZVE/edit?usp=sharing). 
The following steps are only needed if you want to build the database from scratch.
This database is **only** required for normalization to either dbSNP or UniProt...


The import script is tailored towards a mySQL database, but theoretically any other database can be used. 
However, in this case you have to adopt the following description to your database type. 
We would be happy to get feedback about using SETH with other databases.

## Set up the database with all necessary tables
	CREATE DATABASE dbSNP137 CHARACTER SET latin1;
	mysql <dbName> -h <hostname> -u <username> -p<password> resources/table.sql


## Download the necessary files 

### Download a XML dump from dbSNP
	wget ftp://ftp.ncbi.nih.gov/snp/organisms/human_9606/XML/ds\*.gz
	
### Download gene2pubmed links from NCBI-Entrez gene
	wget ftp://ftp.ncbi.nih.gov/gene/DATA/gene2pubmed.gz
	gunzip gene2pubmed.gz
	
### Download UniProt-KB
	wget ftp://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/complete/uniprot_sprot.xml.gz
	
### Download UniProt to Entrez gene mapping 
	wget ftp://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/idmapping/idmapping.dat.gz
	
## Import the data files needed for normalization

### Parse dbSNP-XML dump
This takes some compute resources and disk-space. 

	time java -cp seth.jar de.hu.berlin.wbi.stuff.xml.ParseXML property.xml /path/with/dbSNP-XML/files/... #Parse file
	zcat HGVS.tsv.gz | cut -f 1-3 > hgvs2.tsv #Remove refseq information for derby-DB (gets too large otherwise)
	split -l100000000 hgvs2.tsv '_tmp'; 
	ls -1 _tmp* | while read FILE; do echo $FILE; sort $FILE -o $FILE ; done; #Individual sort
	sort -u -m _tmp* -o hgvs.tsv.sorted #Merge sort

Import	

	mysqlimport  --fields-terminated-by='\t' --delete --local --verbose --host <hostname> --user=<username> --password=<password> <dbName> PSM.tsv
	mysqlimport  --fields-terminated-by='\t' --delete --local --verbose --host <hostname> --user=<username> --password=<password> <dbName> hgvs.tsv.sorted

	
### Parse UniProt-XML for protein-sequence mutations (PSM) and post-translational modifications (*e.g.* signaling peptides) 
	
Requires as input the UniProt-KB dump (uniprot_sprot.xml.gz) and the mapping from Entrez to UniProt (idmapping.dat.gz).
Produces uniprot.dat and PSM.dat files

Using Java:

	java -cp seth.jar seth.Uniprot2Tab uniprot_sprot.xml.gz idmapping.dat.gz uniprot.dat PSM.dat

Using Scala:

	scala Uniprot2Tab.scala uniprot_sprot.xml.gz idmapping.dat.gz uniprot.dat PSM.dat

	
### Import  gene2pubmed, UniProt and PSM into the mySQL Database

	mysqlimport  --fields-terminated-by='\t' --delete --local --verbose --host <hostname> --user=<username> --password=<password> <dbName> gene2pubmed
	mysqlimport  --fields-terminated-by='\t' --delete --local --verbose --host <hostname> --user=<username> --password=<password> <dbName> uniprot.dat
	mysqlimport  --fields-terminated-by='\t' --local --verbose --host <hostname> --user=<username> --password=<password> <dbName> PSM.dat

Additionally, we included results from the gene name recognition tool GNAT applied on all of PubMed and PubMed Central.
This data is only meant as a starting point, we recommend integrating other gene-NER tools.
Updated gene-ner results are available on the GeneView web site (http://bc3.informatik.hu-berlin.de/download)

## Database migration 
Finally, to allow for a better portability of SETH, we converted the original mySQL database into an embedded Derby database.
For this we used Apache [ddlUtils](http://db.apache.org/ddlutils/) 


# Cite SETH

### BibTeX
	@misc{thomas2013seth,
	  title={ {SETH: SNP Extraction Tool for Human Variations} },
	  author={Thomas, Philippe and Rockt{\"a}schel, Tim and Mayer, Yvonne and Leser, Ulf},
	  howpublished = {\url{http://rockt.github.io/SETH/}},
	  year = {2014}
	}
	
### Text	
	Thomas, P., Rocktäschel, T., Mayer, Y., and Leser, U. (2014). SETH: SNP Extraction Tool for Human Variations.
	http://rockt.github.io/SETH/.

#Bug reports
Issues and feature requests can be filed [online](https://github.com/rockt/SETH/issues)

# Contact
For questions and  remarks please contact Philippe Thomas:

thomas \[at\] informatik \[dot\] hu-berlin \[dot\] de
