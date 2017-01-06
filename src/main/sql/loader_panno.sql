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
-- candidate   dbSNP
-- hgncId  mimId   hprdId  aliases
-- location  distance fullLocation
-- 

(sourceGene, sourceVariant, @transvarGene, entrezGeneId, strand, geneModel, @transcriptId, @transcriptType, @proteinId, 
 exonStart, exonEnd, intronStart, intronEnd, variantType, 
 @isBestCandidate, hgvsG, @hgvsC, @hgvsP, chrom, chromStart, chromEnd, 
 @codonNumber, @codonPos, @referenceCodon, @alternateCodon, candidateCodons, 
 @cdsStart, @cdsEnd, proteinStart, proteinEnd, 
 consequence, candidate, @dbSNP, @hgncId, @mimId, @hprdId, @aliases, 
 location, distance, fullLocation)
set alternateCodon = IF(@alternateCodon='null', NULL, @alternateCodon),
    cdsStart = IF(@cdsStart=0,NULL,@cdsStart),
    cdsEnd = IF(@cdsEnd=0,NULL,@cdsEnd),
    codonNumber = IF(@codonNumber=0,NULL,@codonNumber),
    codonPos = IF(@codonPos=0,NULL,@codonPos),
    referenceCodon = IF(@referenceCodon='null',NULL,@referenceCodon),
    transvarGene = IF(@transvarGene='null',NULL,@transvarGene),
    transcriptId = IF(@transcriptId='null',NULL,@transcriptId),
    transcriptType = IF(@transcriptType='null',NULL,@transcriptType),
    isBestCandidate = IF(@isBestCandidate='true',1,-1),
    hgvsC = IF(@hgvsC='null',NULL,@hgvsC),
    hgvsP = IF(@hgvsP='null',NULL,@hgvsP),
    hprdId = IF(@hprdId='null',NULL,@hprdId),
    hgncId = IF(@hgncId='null',NULL,@hgncId),
    mimId = IF(@mimId='null',NULL,@mimId),
    proteinId = IF(@proteinId='null',NULL,@proteinId),
    dbSNP = IF(@dbSNP='null',NULL,@dbSNP),
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

-- Update master table of uniq genomic variants (uniq key is genome build, chromosome and HGVS_G
-- First, insert all new chrom:HGVS_G into the uniq variant table
insert ignore into uniq_variants (genomeBuild, chrom, hgvsG, `chromStart`, `chromEnd`)
select 'hg19', chrom, hgvsG, `chromStart`, `chromEnd`
 from transvar_hgvsc where chrom is not null and hgvsG is not null; 
-- Second, copy the newly created genomic variant IDs (gvids) into the HGVS_P table
update transvar_hgvsp t, uniq_variants u
   set t.gvid = u.gvid
 where t.gvid is null
   and t.chrom = u.chrom and t.hgvsG = u.hgvsG;
;

