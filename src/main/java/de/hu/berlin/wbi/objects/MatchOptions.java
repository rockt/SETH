package de.hu.berlin.wbi.objects;

public enum MatchOptions {
    // Flag if the mutation has been normalized to protein-sequence, false if nucleotide-sequence
    PSM,
    // True when the wild type and mutated residues match
    RESIDUES,
    // True when the locations are equal
    LOC,
    // Mention is derived one position after leading methionine.
    METHIONE,
    // Wildtype and Mutated alleles are swapped (e.g. Ala123Tyr -> Tyr123Ala)
    SWAPPED
}
