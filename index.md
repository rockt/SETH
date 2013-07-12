---
layout: default
---

SETH is a software that extracts and normalizes mentions of single nucleotide polymorphisms and copy number variations
from natural language texts.
It implements grammars for mutation mentions as Scala parser combinatiors.
Extracted mentions are linked to dbSNP.

# Named Entity Recognition (NER)
Minimal example

    seth.ner.SETHNERApp

# Named Entity Normalization (NEN)
The implementation requires as input:
- SNPs identified in the text of interest (a.g. R123K)
- Genes mentioned in the text of interest. These have to be normalized to a entrez-gene identifier. The current implementation uses the result of the tool GNAT (http://gnat.sourceforge.net/  and http://www.ncbi.nlm.nih.gov/pubmed/18689813) together with the gene2pubmed information from NCBI.
- Parts of the dbSNP database (http://www.ncbi.nlm.nih.gov/projects/SNP/) have to be locally installed (see database.txt for more information). For user convenience we provide a database dump:
https://docs.google.com/file/d/0B9uTfq0OyHAsdDNMQzNxWDRhZVE/edit?usp=sharing

        tar xfa

Minimal example

    de.hu.berlin.wbi.process.MinimalExample

Another example

    java -cp lib/mysql-connector-java-5.0.3-bin.jar:lib/snp-normalizer.jar de.hu.berlin.wbi.process.Normalize property.xml resources/snpExample.txt

# NER and NEN
Minimal example

    seth.SETH



# Code Example
    import seth.ner.SETHNER
    val extractor = new SETHNER

    //example sentence taken from: http://www.ncbi.nlm.nih.gov/pubmed/18823551
    val text = "Mutations in the DBD (p.G56R, p.N65_C67del, p.R76Q, p.R76W, p.G88V, p.R97H, p.R104Q, and p.R104W) and LBD (p.A256E, p.L263P, p.P276fs, p.V302I, p.R309G, p.R311Q, p.R334G, p.L336P, p.Q350X, p.L353V, p.R385P, and p.M407K) demonstrated a variable reduction in NR2E3-mediated increase in transcriptional activity when mutant NR2E3 construct was cotransfected with both NRL and CRX."

    val mutations = extractor.extractMutations(text)
    for (mutation <- mutations)
      println(
        mutation.text,
        mutation.start,
        mutation.end,
        mutation.typ,
        mutation.ref,
        mutation.loc,
        mutation.wild,
        mutation.mutated
      )

# Installation

TODO

# Reproducing our results

## Evaluate NER

#### MutationFinder corpus using original MutationFinder scripts
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

#### Human Mutation corpus
     java -cp seth.jar seth.seth.eval.ApplyNER resources/humu/corpus.txt resources/mutations.txt false resources/humu.seth
     java -cp seth.jar seth.seth.eval.EvaluateNER resources/humu.seth resources/humu/yearMapping.txt  resources/humu/annotations/
Precision 0.98
Recall    0.84
F1        0.90

#### Larger Human Mutation corpus
     java -cp seth.jar seth.seth.eval.ApplyNER resources/american/corpus.txt resources/mutations.txt false resources/american.seth
     java -cp seth.jar seth.seth.eval.EvaluateNER resources/american.seth resources/american/yearMapping.txt resources/american/annotations/
Precision 0.88
Recall    0.82
F1        0.85

#### Corpus of Wei et al. (2013) REF ??
     java -cp seth.jar seth.seth.eval.ApplyNERToWei resources/Wei2013/train.txt  resources/mutations.txt  resources/Wei2013.seth
     java -cp seth.jar seth.seth.eval.EvaluateWei resources/Wei2013/train.txt resources/Wei2013.seth
Precision 0.94
Recall    0.81
F1        0.87

#### Corpus of Verspoor 2013 REF ??
     java -cp seth.jar seth.seth.eval.ApplyNerToVerspoor resources/Verspoor2013/corpus/ resources/mutations.txt resources/Verspoor2013.seth
     java -cp seth.jar seth.seth.eval.EvaluateVerspoor resources/Verspoor2013/annotations/ resources/Verspoor2013.seth
Precision 0.86
Recall    0.14
F1        0.24

## Evaluate NEN

#### Corpus by Thomas et al. (??)
     java -cp seth.jar de.hu.berlin.wbi.process.Evaluate myProperty.xml resources/thomas2011/corpus.txt
Precision 0.95
Recall    0.58
F1        0.72

#### Corpus by Osiris (REF ??)
     java -cp seth.jar de.hu.berlin.wbi.process.osiris.EvaluateOsiris myProperty.xml resources/OSIRIS/corpus.xml
Precision 0.98
Recall    0.85
F1        0.91

# References
J. F. J. Laros, A. Blavier, J. T. den Dunnen, and P. E. M. Taschner.
A formalized description of the standard human variant nomenclature in Extended Backus-Naur Form.
BMC bioinformatics, vol. 12 Suppl 4, p. S5. 2011.

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
