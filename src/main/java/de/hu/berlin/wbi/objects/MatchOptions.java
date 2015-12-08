package de.hu.berlin.wbi.objects;

/**
 * Represents the different possibilities to match a  {@link MutationMention}
 * to a dbSNP entry or UniProt entry
 */
public enum MatchOptions {
    /** Has been normalized to protein sequence? Otherwise nucleotide */
    PSM,
    /** Location matches exactly? */
    LOC,
    /** Location matches by methionine */
    METHIONE,
    /** Wildtype and Mutated alleles are swapped (e.g. Ala123Tyr -> Tyr123Ala) */
    SWAPPED
}
