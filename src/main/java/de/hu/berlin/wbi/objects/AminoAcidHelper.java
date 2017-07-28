package de.hu.berlin.wbi.objects;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class to transform a one letter amino-acid into a three letter representation
 *
 * @author Philippe Thomas
 *
 */
public class AminoAcidHelper {

    private static Map<String, String> oneToThreeLookupMap = null;
    static {
        HashMap<String, String> tmpMap = new HashMap<>();

        /* populate the one-letter to three-letter lookup map */
        tmpMap.put("A", "ALA");
        tmpMap.put("G", "GLY");
        tmpMap.put("L", "LEU");
        tmpMap.put("M", "MET");
        tmpMap.put("F", "PHE");
        tmpMap.put("W", "TRP");
        tmpMap.put("K", "LYS");
        tmpMap.put("Q", "GLN");
        tmpMap.put("E", "GLU");
        tmpMap.put("S", "SER");
        tmpMap.put("P", "PRO");
        tmpMap.put("V", "VAL");
        tmpMap.put("I", "ILE");
        tmpMap.put("C", "CYS");
        tmpMap.put("Y", "TYR");
        tmpMap.put("H", "HIS");
        tmpMap.put("R", "ARG");
        tmpMap.put("N", "ASN");
        tmpMap.put("D", "ASP");
        tmpMap.put("T", "THR");

//http://en.wikipedia.org/wiki/Amino_acid#Table_of_standard_amino_acid_abbreviations_and_side_chain_properties
        tmpMap.put("B", "ASX");
        tmpMap.put("Z", "GLX");
        tmpMap.put("J", "XLE");
        tmpMap.put("X", "TER");	//TER used to describe a stop codon

        oneToThreeLookupMap = Collections.unmodifiableMap(tmpMap);
    }

    /**
     * Takes as input a string of length one and returns the three letter code for it
     * @param residue  String of length 1, representing a one letter amino-acid code
     * @return three letter representation or NULL if invalid
     */
    public static String getThreeLetter(String residue){

        assert(residue.length() ==1);
        assert(oneToThreeLookupMap != null);

        if(oneToThreeLookupMap.containsKey(residue)){
            return toCorrectCase(oneToThreeLookupMap.get(residue));
        }
        else
           return null;
    }

    /**
     * Method takes as input a String (Should be a three letter character)
     * and returns a "only first character upper case representation
     * @param aminoAcid Three letter long string representing an amino acid
     * @return The same three letter string, but the first character is uppercase the others smaller case
     */
    private static String toCorrectCase(String aminoAcid){

        return String.valueOf(Character.toUpperCase(aminoAcid.charAt(0))) +
                aminoAcid.substring(1).toLowerCase();
    }

}
