package edu.uchsc.ccp.nlp.ei.mutation;

import java.util.Collections;
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
     * build a mapping from three-letter code to one-letter code
     */
    public static Map<String, String> populateAminoAcidThreeToOneLookupMap = null;
    static {
        HashMap<String, String> tmpMap = new HashMap<>();
        /* populate the three-letter to one-letter lookup map */
        tmpMap.put("ALA", "A");
        tmpMap.put("GLY", "G");
        tmpMap.put("LEU", "L");
        tmpMap.put("MET", "M");
        tmpMap.put("PHE", "F");
        tmpMap.put("TRP", "W");
        tmpMap.put("LYS", "K");
        tmpMap.put("GLN", "Q");
        tmpMap.put("GLU", "E");
        tmpMap.put("SER", "S");
        tmpMap.put("PRO", "P");
        tmpMap.put("VAL", "V");
        tmpMap.put("ILE", "I");
        tmpMap.put("CYS", "C");
        tmpMap.put("TYR", "Y");
        tmpMap.put("HIS", "H");
        tmpMap.put("ARG", "R");
        tmpMap.put("ASN", "N");
        tmpMap.put("ASP", "D");
        tmpMap.put("THR", "T");
        
//http://en.wikipedia.org/wiki/Amino_acid#Table_of_standard_amino_acid_abbreviations_and_side_chain_properties        
        tmpMap.put("ASX", "B");
        tmpMap.put("GLX", "Z");
        tmpMap.put("XLE", "J");
        tmpMap.put("TER", "X");	//TER and STP are often used to describe a stop codon
        tmpMap.put("STP", "X");

        populateAminoAcidThreeToOneLookupMap = Collections.unmodifiableMap(tmpMap);
    }

    /**
     * This method simple fills the two mappings between amino acid name and the amino acid one-letter code
     * 
     * build a mapping from amino acid full name to one-letter code
     */
    public static Map<String, String> populateAminoAcidNameToOneLookupMap = null;
    static {
        /* populate the amino acid name to one-letter lookup map */
        HashMap<String, String> tmpMap = new HashMap<>();
        tmpMap.put("ALANINE", "A");
        tmpMap.put("GLYCINE", "G");
        tmpMap.put("LEUCINE", "L");
        tmpMap.put("METHIONINE", "M");
        tmpMap.put("PHENYLALANINE", "F");
        tmpMap.put("TRYPTOPHAN", "W");
        tmpMap.put("LYSINE", "K");
        tmpMap.put("GLUTAMINE", "Q");
        tmpMap.put("GLUTAMIC ACID", "E");
        tmpMap.put("GLUTAMATE", "E");
        tmpMap.put("ASPARTATE", "D");
        tmpMap.put("SERINE", "S");
        tmpMap.put("PROLINE", "P");
        tmpMap.put("VALINE", "V");
        tmpMap.put("ISOLEUCINE", "I");
        tmpMap.put("CYSTEINE", "C");
        tmpMap.put("TYROSINE", "Y");
        tmpMap.put("HISTIDINE", "H");
        tmpMap.put("ARGININE", "R");
        tmpMap.put("ASPARAGINE", "N");
        tmpMap.put("ASPARTIC ACID", "D");
        tmpMap.put("THREONINE", "T");
        
        tmpMap.put("TERM", "X");
        tmpMap.put("STOP", "X");
        tmpMap.put("AMBER", "X");
        tmpMap.put("UMBER", "X");
        tmpMap.put("OCHRE", "X");
        tmpMap.put("OPAL", "X");

        populateAminoAcidNameToOneLookupMap = Collections.unmodifiableMap(tmpMap);
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
     * @return returns map from mutations and one or several matching locations in text
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
