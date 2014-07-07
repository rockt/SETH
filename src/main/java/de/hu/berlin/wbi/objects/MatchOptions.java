package de.hu.berlin.wbi.objects;

public enum MatchOptions {
    // Has been normalized to protein sequence? Otherwise nucleotide
    PSM,
    // Location matches exactly?
    LOC,
    // Location matches by methionine
    METHIONE,
    // Wildtype and Mutated alleles are swapped (e.g. Ala123Tyr -> Tyr123Ala)
    SWAPPED
}
