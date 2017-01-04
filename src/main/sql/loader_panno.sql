LOAD DATA LOCAL INFILE 'parsed_panno.tsv'
REPLACE
INTO TABLE transvar_hgvsp
IGNORE 1 lines

-- 
-- sourceGene     sourceVariant   transvarGene    entrezGeneId    strand  geneModel       transcriptId    transcriptType  proteinId
-- exonStart       exonEnd intronStart     intronEnd       variantType
-- isBestCandidate
-- hgvsG   hgvsC   hgvsP
-- chrom   chromStart      chromEnd
-- codonNumber     codonPos        referenceCodon  alternateCodon  candidateCodons
-- cdsStart        cdsEnd  proteinStart    proteinEnd
-- consequence
-- candidate
-- hgncId  mimId   hprdId  aliases location
-- 

(sourceGene, sourceVariant, transvarGene, entrezGeneId, strand, geneModel, transcriptId, transcriptType, @proteinId, 
 exonStart, exonEnd, intronStart, intronEnd, variantType, 
 @isBestCandidate, 
 hgvsG, @hgvsC, @hgvsP, 
 chrom, chromStart, chromEnd, 
 @codonNumber, @codonPos, referenceCodon, @alternateCodon, candidateCodons, 
 @cdsStart, @cdsEnd, proteinStart, proteinEnd, 
 consequence, 
 candidate, 
 hgncId, mimId, @hprdId, @aliases, location)
set alternateCodon = IF(@alternateCodon='null', NULL, @alternateCodon),
    cdsStart = IF(@cdsStart=0,NULL,@cdsStart),
    cdsEnd = IF(@cdsEnd=0,NULL,@cdsEnd),
    codonNumber = IF(@codonNumber=0,NULL,@codonNumber),
    codonPos = IF(@codonPos=0,NULL,@codonPos),
    isBestCandidate = IF(@isBestCandidate='true',1,-1),
    hgvsC = IF(@hgvsC='null',NULL,@hgvsC),
    hgvsP = IF(@hgvsP='null',NULL,@hgvsP),
    hprdId = IF(@hprdId='null',NULL,@hprdId),
    hgncId = IF(@hgncId='null',NULL,@hgncId),
    mimId = IF(@mimId='null',NULL,@mimId),
    proteinId = IF(@proteinId='null',NULL,@proteinId),
    aliases = IF(@aliases='',NULL,IF(@aliases='null',NULL,@aliases))
;

show warnings;


-- Update missing gene IDs (Entrez, HGNC, HPRD, OMIM) from NCBI's gene info table where missing
-- Transvar seems to add those only for RefSeq transcripts.
update transvar_hgvsp t, nlp_search.gene_info g
   set t.entrezGeneId = g.gene_id,
       t.hprdId = g.HPRD,
       t.hgncId = g.HGNC,
       t.mimId = g.MIM
 where t.entrezGeneId <= 0 and t.transvarGene is not null
   and t.transvarGene = g.symbol
   and t.chrom = g.chromosome;


