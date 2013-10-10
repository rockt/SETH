package seth.ner.wrapper;

/**
 * Mutation types found by {@link SETHNER}, MutationFinder currently extracts only Substitutions
 */
public enum Type {
    DELETION,
    INSERTION,
    SUBSTITUTION,
    DUPLICATION,
    DELETION_INSERTION,
    INVERSION,
    CONVERSION,
    TRANSLOCATION,
    FRAMESHIFT,
    SILENT,
    SHORT_SEQUENCE_REPEAT,
    DBSNP_MENTION,
    STRUCTURAL_ABNORMALITY,
    COPY_NUMBER_VARIATION,
    OTHER
}