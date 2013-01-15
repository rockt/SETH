package seth.ner.wrapper;

/**
 * Mutation types found by {@link SETHNER}, MutationFinder currently extracts only Substitutions
 * User: Tim Rocktaeschel
 * Date: 11/9/12
 * Time: 2:44 PM
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
    OTHER
}