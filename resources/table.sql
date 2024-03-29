-- Table describing protein - sequence mutations only (no DNA mutations)
CREATE TABLE PSM(
       snp_id INT NOT NULL,
       locus_id INT NOT NULL,
       aa_Position INT NOT NULL,
       residue VARCHAR(128) NOT NULL,
       wildtype VARCHAR(128) NOT NULL,
       UNIQUE(snp_id, locus_id, aa_Position, residue, wildtype)
);
CREATE INDEX snp_PSM ON PSM(snp_id);
CREATE INDEX locus_PSM ON PSM(locus_id);

-- Contains all dbSNP mutations with respective nomenclature 
CREATE TABLE hgvs(
       locus_id INT NOT NULL,
       snp_id INT NOT NULL,
       hgvs VARCHAR(256) NOT NULL,
       refseq VARCHAR(64) NOT NULL,
       UNIQUE(locus_id, snp_id, hgvs, refseq)
);
CREATE INDEX snp_hgvs ON hgvs(snp_id);

-- Uniprot information about postranslational modifications
CREATE TABLE uniprot(
       entrez INT NOT NULL,
       modification VARCHAR(64) NOT NULL,
       location VARCHAR(32) NOT NULL,
       UNIQUE(entrez, modification, location)
);

-- Genes found by GNAT in all of PubMed 
CREATE TABLE genes (
       pmid INT NOT NULL, 
       id INT NOT NULL, 
       species INT NOT NULL, 
       UNIQUE(pmid, id, species)
);

CREATE INDEX pmid_genes  ON genes (pmid);
CREATE INDEX id_genes  ON genes (id);

-- Mapping from Entrez-Gene to PubMed (downloaded from NCBI)
CREATE TABLE gene2pubmed(
       taxId INT NOT NULL,
       geneId INT NOT NULL,
       pmid INT  NOT NULL,
       UNIQUE(taxId, geneId, pmid)
);
CREATE INDEX id_gene2pubmed  ON gene2pubmed (geneId);
CREATE INDEX pmid_gene2pubmed  ON gene2pubmed (pmid);


-- Transcript information, for normalization to UniProt-KB
CREATE TABLE seth_transcripts (
       entrez_id INT DEFAULT NULL,
       uniprot_acc varchar(9) DEFAULT NULL,
       ENSG varchar(15)  NOT NULL,
       ENST varchar(15)  NOT NULL,
       ENSP varchar(15) DEFAULT NULL,
       protein_sequence TEXT,
       coding_sequence TEXT);

CREATE INDEX entrez_transcript  ON seth_transcripts(entrez_id);

-- Map about which item was merged from which (old_snp_id) to new ID (new_snp_id)
CREATE TABLE mergeItems(
       new_snp_id INT NOT NULL,
       old_snp_id INT NOT NULL,
       dbSNP_version INT NOT NULL,
       UNIQUE(old_snp_id)
);

-- Mapping from NER/NEN results to PubMed (downloaded from NCBI PubTatorCentral)                                                                              
CREATE TABLE gene2pubtatorcentral(
       pmid INT NOT NULL,
       entityType VARCHAR(16) NOT NULL,
       geneIDText TEXT NOT NULL,
       entity TEXT NOT NULL,
       toolText TEXT NOT NULL
);
CREATE INDEX pmid_gene2pubtatorcentral  ON gene2pubtatorcentral (pmid);


