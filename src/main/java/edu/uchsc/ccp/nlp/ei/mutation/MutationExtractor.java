package edu.uchsc.ccp.nlp.ei.mutation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

public abstract class MutationExtractor {

    /**
     * This method simple fills the two mappings between amino acid one letter code and the amino acid three letter code
     * 
     * @return a mapping from three-letter code to one-letter code
     */
    public static Map<String, String> populateAminoAcidThreeToOneLookupMap() {
        /* populate the three-letter to one-letter lookup map */
        Map<String, String> amino_acid_three_to_one_map = new HashMap<String, String>();
        amino_acid_three_to_one_map = new HashMap<String, String>();
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
        amino_acid_three_to_one_map.put("TER", "X");	//TER and STP are often used to describe a stop codon
        amino_acid_three_to_one_map.put("STP", "X");
 
        return amino_acid_three_to_one_map;
    }

    /**
     * This method simple fills the two mappings between amino acid name and the amino acid one-letter code
     * 
     * @return a mapping from amino acid full name to one-letter code
     */
    public static Map<String, String> populateAminoAcidNameToOneLookupMap() {
        /* populate the amino acid name to one-letter lookup map */
        Map<String, String> amino_acid_name_to_one_map = new HashMap<String, String>();
        amino_acid_name_to_one_map = new HashMap<String, String>();
        amino_acid_name_to_one_map.put("ALANINE", "A");
        amino_acid_name_to_one_map.put("GLYCINE", "G");
        amino_acid_name_to_one_map.put("LEUCINE", "L");
        amino_acid_name_to_one_map.put("METHIONINE", "M");
        amino_acid_name_to_one_map.put("PHENYLALANINE", "F");
        amino_acid_name_to_one_map.put("TRYPTOPHAN", "W");
        amino_acid_name_to_one_map.put("LYSINE", "K");
        amino_acid_name_to_one_map.put("GLUTAMINE", "Q");
        amino_acid_name_to_one_map.put("GLUTAMIC ACID", "E");
        amino_acid_name_to_one_map.put("GLUTAMATE", "E");
        amino_acid_name_to_one_map.put("ASPARTATE", "D");
        amino_acid_name_to_one_map.put("SERINE", "S");
        amino_acid_name_to_one_map.put("PROLINE", "P");
        amino_acid_name_to_one_map.put("VALINE", "V");
        amino_acid_name_to_one_map.put("ISOLEUCINE", "I");
        amino_acid_name_to_one_map.put("CYSTEINE", "C");
        amino_acid_name_to_one_map.put("TYROSINE", "Y");
        amino_acid_name_to_one_map.put("HISTIDINE", "H");
        amino_acid_name_to_one_map.put("ARGININE", "R");
        amino_acid_name_to_one_map.put("ASPARAGINE", "N");
        amino_acid_name_to_one_map.put("ASPARTIC ACID", "D");
        amino_acid_name_to_one_map.put("THREONINE", "T");
        
        amino_acid_name_to_one_map.put("TERM", "X");
        amino_acid_name_to_one_map.put("STOP", "X");
        amino_acid_name_to_one_map.put("AMBER", "X");
        amino_acid_name_to_one_map.put("UMBER", "X");
        amino_acid_name_to_one_map.put("OCHRE", "X");
        amino_acid_name_to_one_map.put("OPAL", "X");

        return amino_acid_name_to_one_map;
    }

    
    /**
     * Extract point mutations mentions from raw_text and return them in a map.
     * 
     * The result of this method is a mapping of PointMutation objects to a set of spans (int arrays of size 2) where they were identified. Spans are
     * presented in the form of character-offsets in text. 
     * 
     * Example result: <br>
     * raw_text: 'We constructed A42G and L22G, and crystalized A42G.' <br>
     * result = {PointMutation(42,'A','G'):[(15,19),(46,50)], <br>
     * PointMutation(22,'L','G'):[(24,28)]}<br>
     * 
     * Note that the spans won't necessarily be in increasing order, due to the order of processing regular expressions.
     * 
     * @param rawText
     *            the text to be processed
     * @return
     */
    public abstract Map<Mutation, Set<int[]>> extractMutations(String rawText) throws MutationException;
    
    
    /*
     * Prints a warning message
     */
    protected void warn(String message) {
        System.err.println("WARNING -- " + this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')) + message);
    }

    /*
     * Prints and error message
     */
    protected void error(String message) {
        System.err.println("ERROR -- " + this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')) + message);
    }
}
