package de.hu.berlin.wbi.stuff;

import java.util.HashMap;
import java.util.Map;

/**
 * A base class for extracting Mutations from text
 * 
 * @author William A. Baumgartner, Jr. <br>
 *         william.baumgartner@uchsc.edu
 * @version 1.0
 * 
 */

/*
 * Copyright (c) 2007 Regents of the University of Colorado
 * Please refer to the licensing agreement at MUTATIONFINDER_HOME/doc/license.txt
 */
      //TODO Remove this class and use from MutationFinder
public abstract class AA_Mapper {

    /**
     * This method simple fills the two mappings between amino acid one letter code and the amino acid three letter code
     * 
     * @return a mapping from three-letter code to one-letter code
     */
    public static Map<String, String> populateAminoAcidThreeToOneLookupMap() {
        /* populate the three-letter to one-letter lookup map */
        Map<String, String> amino_acid_three_to_one_map = new HashMap<String, String>();
        amino_acid_three_to_one_map.put("ALA", "A");
        amino_acid_three_to_one_map.put("GLY", "G");
        amino_acid_three_to_one_map.put("LEU", "L");
        amino_acid_three_to_one_map.put("MET", "M");
        amino_acid_three_to_one_map.put("PHE", "F");
        amino_acid_three_to_one_map.put("TRP", "W");
        amino_acid_three_to_one_map.put("LYS", "K");
        amino_acid_three_to_one_map.put("GLN", "Q");
        amino_acid_three_to_one_map.put("GLU", "E");
        amino_acid_three_to_one_map.put("SER", "S");
        amino_acid_three_to_one_map.put("PRO", "P");
        amino_acid_three_to_one_map.put("VAL", "V");
        amino_acid_three_to_one_map.put("ILE", "I");
        amino_acid_three_to_one_map.put("CYS", "C");
        amino_acid_three_to_one_map.put("TYR", "Y");
        amino_acid_three_to_one_map.put("HIS", "H");
        amino_acid_three_to_one_map.put("ARG", "R");
        amino_acid_three_to_one_map.put("ASN", "N");
        amino_acid_three_to_one_map.put("ASP", "D");
        amino_acid_three_to_one_map.put("THR", "T");
        
//http://en.wikipedia.org/wiki/Amino_acid#Table_of_standard_amino_acid_abbreviations_and_side_chain_properties        
        amino_acid_three_to_one_map.put("ASX", "B");
        amino_acid_three_to_one_map.put("GLX", "Z");
        amino_acid_three_to_one_map.put("XLE", "J");
        amino_acid_three_to_one_map.put("TER", "X");
        amino_acid_three_to_one_map.put("STP", "X");
 
        return amino_acid_three_to_one_map;
    }

}
