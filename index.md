---
layout: default
---

SETH is a software that performs named entity recognition (NER) of single nucleotide polymorphisms (SNPs) and copy
number variations (CNVs) from natural language texts. SETH's NER component is based on Scala parser combinatiors,
which are able to identify structured mentions of mutations that obey nomenclature (den Dunnen and Antonarakis, 2000) by
implementing an EBNF grammar proposed by Laros *et al.* (2011).
To get hold of unstructured mentions, SETH integrates MutationFinder (Caporaso *et al.*, 2007).
Extracted structured and unstructured mentions of SNPs are linked to [dbSNP](http://www.ncbi.nlm.nih.gov/SNP/),
a process referred to as named entity normalization (NEN).


# NER
Given an input text, SETH extracts mentions of mutations. (**TODO**: find better example)

## Command-line Usage
**TODO**: Develop a command-line version that uses MutationFinder in addition. Also output the which software extracted
the mention.

    $ java -cp seth.jar seth.ner.SETHNERApp "Two common mutations, c.35delG and L90P (c.269T>C)."

    MATCH: c.35delG
        start: 22
        end:   30
        loc:   35
        ref:   c.
        wild:  G
        type:  DELETION

    MATCH: c.269T>C
        start: 41
        end:   49
        loc:   269
        ref:   c.
        wild:  T
        mut:   C
        type:  SUBSTITUTION


# NEN
Given mentions of SNPs and a list of genes (*i.e.* entrez-gene identifiers), SETH normalizes SNPs to
dbSNP identifiers.
To extract gene mentions, we use the output of the tool [GNAT](http://gnat.sourceforge.net/) (Hakenberg *et al.*, 2011)
together with the gene2pubmed information from NCBI.
Parts of the [dbSNP database](http://www.ncbi.nlm.nih.gov/projects/SNP/) have to be locally installed for speeding up
the normalization process
(see database.txt for more information).
For user convenience, we provide a [database dump](https://docs.google.com/file/d/0B9uTfq0OyHAsdDNMQzNxWDRhZVE/edit?usp=sharing) (~2GB).

## Command-line Usage
    $ java -cp lib/mysql-connector-java-5.0.3-bin.jar:lib/snp-normalizer.jar de.hu.berlin.wbi.process.Normalize property.xml resources/snpExample.txt

**TODO**: output?

# Code Examples
## NER
### Java [seth.ner.wrapper.SETHNERApp](https://github.com/rockt/SETH/blob/master/src/main/java/seth/ner/wrapper/SETHNERApp.java#L13-L24)
### Scala [seth.ner.SETHNERApp](https://github.com/rockt/SETH/blob/master/src/main/scala/seth/ner/SETHNER.scala#L18-L28)
## NEN
### Java [de.hu.berlin.wbi.process.MinimalExample](https://github.com/rockt/SETH/blob/master/src/main/java/de/hu/berlin/wbi/process/MinimalExample.java#L50-L71)
## NER and NEN
### Java [seth.SETH](https://github.com/rockt/SETH/blob/master/src/main/java/seth/SETH.java#L104-L149)

# Reproducing our results

## Evaluate NER

#### Human Mutation corpus I (~200 abstracts)
    java -cp seth.jar seth.seth.eval.ApplyNER resources/humu/corpus.txt resources/mutations.txt false resources/humu.seth
    java -cp seth.jar seth.seth.eval.EvaluateNER resources/humu.seth resources/humu/yearMapping.txt  resources/humu/annotations/
Precision 0.98
Recall    0.84
F1        0.90

#### Human Mutation corpus II (~400 abstracts)
    java -cp seth.jar seth.seth.eval.ApplyNER resources/american/corpus.txt resources/mutations.txt false resources/american.seth
    java -cp seth.jar seth.seth.eval.EvaluateNER resources/american.seth resources/american/yearMapping.txt resources/american/annotations/
Precision 0.88
Recall    0.82
F1        0.85

#### MutationFinder corpus using original MutationFinder scripts (Caporaso *et al.*, 2007)
    java -cp seth.jar seth.seth.eval.ApplyNER resources/mutationfinder/corpus/devo_text.txt resources/mutations.txt true resources/devo_text.seth
    java -cp seth.jar seth.seth.eval.ApplyNER resources/mutationfinder/corpus/test_text.txt resources/mutations.txt true resources/test_text.seth
    python resources/mutationfinder/origDist/performance.py resources/devo_text.seth  resources/mutationfinder/corpus/devo_gold_std.txt
Precision 0.97
Recall    0.83
F1        0.89

    python resources/mutationfinder/origDist/performance.py resources/test_text.seth  resources/mutationfinder/corpus/test_gold_std.txt
Precision 0.97
Recall    0.81
F1        0.88

#### Corpus of Wei *et al.* (2013)
    java -cp seth.jar seth.seth.eval.ApplyNERToWei resources/Wei2013/train.txt  resources/mutations.txt  resources/Wei2013.seth
    java -cp seth.jar seth.seth.eval.EvaluateWei resources/Wei2013/train.txt resources/Wei2013.seth
Precision 0.94
Recall    0.81
F1        0.87

#### Corpus of Verspoor *et al.* (2013)
    java -cp seth.jar seth.seth.eval.ApplyNerToVerspoor resources/Verspoor2013/corpus/ resources/mutations.txt resources/Verspoor2013.seth
    java -cp seth.jar seth.seth.eval.EvaluateVerspoor resources/Verspoor2013/annotations/ resources/Verspoor2013.seth
Precision 0.86
Recall    0.14
F1        0.24

## Evaluate NEN

#### Corpus of Thomas *et al.* (2011)
    java -cp seth.jar de.hu.berlin.wbi.process.Evaluate myProperty.xml resources/thomas2011/corpus.txt
Precision 0.95
Recall    0.58
F1        0.72

#### Corpus of Osiris (Furlong *et al.*, 2008)
    java -cp seth.jar de.hu.berlin.wbi.process.osiris.EvaluateOsiris myProperty.xml resources/OSIRIS/corpus.xml
Precision 0.98
Recall    0.85
F1        0.91

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


# Appendix
## Rebuilding the database
In this document we describe how to set up the database required for snp-normalization
A stand alone (embedded) derby database is available at the project web site
This document (database.txt) is only required if someone wants to build the database for normalization from scratch


Step 1: Setting up a database for parts of dbSNP database and the genes found for the relevant articles.
     Data is stored in a local mySQL database, but any other database can be used. However, in this case you have to adopt the following description to your database type. We would be happy to get feedback about using snp-normalizer with other databases.

     #a.) Download the necessary dbSNP-data-files (This tutorial currently covers dbSNP Version 137)
     # Download XML dump from dbSNP
     wget ftp://ftp.ncbi.nih.gov/snp/organisms/human_9606/XML/ds*.gz

     # Download gene2pubmed links from Entrez gene
     wget ftp://ftp.ncbi.nih.gov/gene/DATA/gene2pubmed.gz
     gunzip gene2pubmed.gz

     # Download uniprot
     wget ftp://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/complete/uniprot_sprot.xml.gz

     #Download UniProt mapping
     wget ftp://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/idmapping/idmapping.dat.gz

     #b.) Setting up the database with the tables "b134_SNPContigLocusId_37_2", ...,  and "genes"
     CREATE DATABASE dbSNP137 CHARACTER SET latin1;
     mysql <dbName> -h <hostname> -u <username> -p<password> < data/table.sql

    #c.) Import the data files needed for normalisation
    #Parse dbSNP XML dump
    time java -cp lib/snp-normalizer.jar:lib/mysql-connector-java-5.0.3-bin.jar de.hu.berlin.wbi.stuff.xml.ParseXML property.xml /path/with/dbSNP-XML/files/...

    #Parse UniProt-XML for protein-sequence mutations (PSM) and for post-translational modifications (e.g. signaling peptides)
    scala Uniprot2Tab.scala uniprot_sprot.xml.gz idmapping.dat.gz uniprot.dat PSM.dat

    #Inpute gene2pubmed, UniProt, and PSM
    mysqlimport  --fields-terminated-by='\t' --delete --local --verbose --host <hostname> --user=<username> --password=<password> <dbName> gene2pubmed
    mysqlimport  --fields-terminated-by='\t' --delete --local --verbose --host <hostname> --user=<username> --password=<password> <dbName> uniprot.dat
    mysqlimport  --fields-terminated-by='\t' --local --verbose --host <hostname> --user=<username> --password=<password> <dbName> PSM.dat

    #Additionally we included results from the gene name recognition tool GNAT applied on all of PubMed and PubMed Central (as of 09/12/2012).
    #Updated results are available on the GeneView web site (http://bc3.informatik.hu-berlin.de/download)

Step 2:
     The mySQL database is dumped into XML files using apache ddlUtils (http://db.apache.org/ddlutils/) and later transfered into an embedded database.
     Ant scripts can be found in  "./data/ddlUtils/build.xml"

     ##Execution of ddlUtils
     export ANT_OPTS=-Xmx24g

     #Exports mySQL Database to DDL XML (takes 20 minutes)
     time ant -v export-source-db

     #Converts DDL-XML to derby database (several houts)
     time ant import-target-db
