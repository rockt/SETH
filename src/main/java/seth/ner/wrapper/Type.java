package seth.ner.wrapper;

/**
 * Mutation types found by {@link SETHNER}, MutationFinder currently extracts only Substitutions
 */
public enum Type {
    /** Replacement of wildtype by mutation */
    SUBSTITUTION,

    /** Literal dbSNP Mention (e.g., rs1232) */
    DBSNP_MENTION,

    /** Deletion of a amino-acid or nucleotide (e.g., p.F123del or DeltaF508)  */
    DELETION,

    /** Wide range mutations. I.e., trisomy or deletions of chromosomes and regions (e.g., 46,XX,-13 or chr17:123533-12131314) */
    STRUCTURAL_ABNORMALITY,

    /** Mutation which leads to a shifted reading frame and therefore to a completely different translation product (e.g., R123fs or p.N310fs) */
    FRAMESHIFT,

    /** Addition of a nucleotide into the DNA which can can lead to added amino acids (e.g., 123insA)*/
    INSERTION,

    /** Duplication of genetic material (e.g., c.123dupC) */
    DUPLICATION,

    /**Mutation resulting in both insertion and deletion (e.g., p.Val134_K144delinsE)  */
    DELETION_INSERTION,

    /** Short reperat of the same fragment (e.g., c1210-12T[9]) */
    SHORT_SEQUENCE_REPEAT,

    /** Used to specify silent mutations, with no impact on protein sequence (e.g., p.Arg12=)  */
    SILENT,

    /** Inversion of DNA fragment (e.g., c12_15inv)  */
    INVERSION,

    /** Gene conversion*/
    CONVERSION,

    TRANSLOCATION,
    COPY_NUMBER_VARIATION,

    /**Fall back mutation-type */
    OTHER
}