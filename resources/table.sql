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
       confidence INT NOT NULL, 
       species INT NOT NULL, 
       beginGene INT NOT NULL, 
       endGene INT NOT NULL, 
       entity VARCHAR(100) NOT NULL,  
       UNIQUE(pmid, id, confidence, species, beginGene, endGene, entity)
);

CREATE INDEX pmid_genes  ON genes (pmid);
CREATE INDEX id_genes  ON genes (id);

-- Mapping from Entrez-Gene to PubMed (downloaded from NCBI)
CREATE TABLE gene2pubmed(
       taxId INT UNSIGNED NOT NULL,
       geneId INT UNSIGNED NOT NULL,
       pmid INT UNSIGNED NOT NULL,
       UNIQUE(taxId, geneId, pmid))
       ENGINE = InnoDB
       DEFAULT CHARACTER SET = utf8
       COLLATE = utf8_bin
;
CREATE INDEX id_gene2pubmed  ON gene2pubmed (geneId);
CREATE INDEX pmid_gene2pubmed  ON gene2pubmed (pmid);

