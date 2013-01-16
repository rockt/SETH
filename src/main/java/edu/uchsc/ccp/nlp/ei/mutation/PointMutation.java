package edu.uchsc.ccp.nlp.ei.mutation;

import java.util.HashMap;
import java.util.Map;

/**
 * A class for storing information about protein point mutations
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

public class PointMutation extends Mutation {

    private Map<String, String> abbreviationLookup;

    private char wtResidue;

    private char mutResidue;

    /**
     * Initialize the object and call the base class constructor.
     * 
     * @param position
     *            the sequence position or start position of the mutation
     * @param wtResidue
     *            the wild-type (pre-mutation) residue identity (a string)
     * @param mutResidue
     *            the mutant (post-mutation) residue identity (a string)
     * @throws MutationException
     *             Residues identities are validated to ensure that they are within the canonical set of amino acid residues are normalized to their
     *             one-letter abbreviations.
     */
    public PointMutation(int position, String wtResidue, String mutResidue) throws MutationException {
        super(position);
        initialize(wtResidue, mutResidue);
    }

    public PointMutation(String position, String wtResidue, String mutResidue) throws MutationException {
        super(position);
        initialize(wtResidue, mutResidue);
    }

    private void initialize(String wtResidue, String mutResidue) throws MutationException {
        abbreviationLookup = populateAminoAcidOneToOneLookupMap();
        updateMapping(abbreviationLookup, MutationFinder.populateAminoAcidThreeToOneLookupMap());
        updateMapping(abbreviationLookup, MutationFinder.populateAminoAcidNameToOneLookupMap());

        this.wtResidue = normalizeResidueIdentity(wtResidue);
        this.mutResidue = normalizeResidueIdentity(mutResidue);
    }

    /**
     * Normalize three-letter and full residue names to their one-letter abbreviations. If a residue identity is passed in which does not fall into
     * the set of canonical amino acids a MutationError is raised.
     * 
     * @param residue
     * @return
     */
    public char normalizeResidueIdentity(String residue) throws MutationException {
        /* convert the residue to uppercase so lookup is case-insensitive */
        residue = residue.toUpperCase();
        if (abbreviationLookup.containsKey(residue)) {
            return abbreviationLookup.get(residue).charAt(0);
        } else {
            throw new MutationException("Input residue not recognized, must be a standard residue: " + residue);
        }
    }

    /*
     * This method defines a mapping between each amino acid one letter code and itself. For simplicty of the normalization procedure, we include a
     * one-letter to one-letter 'mapping'. This eliminates the need for an independent validation step, since any valid identity which is passed in
     * will be a key in this map, and it avoids having to analyze which format the input residue was passed in as.
     */
    protected Map<String, String> populateAminoAcidOneToOneLookupMap() {
        /* populate the three-letter to one-letter lookup map */
        Map<String, String> amino_acid_three_to_one_map = new HashMap<String, String>();
        amino_acid_three_to_one_map = new HashMap<String, String>();
        amino_acid_three_to_one_map.put("A", "A");
        amino_acid_three_to_one_map.put("G", "G");
        amino_acid_three_to_one_map.put("L", "L");
        amino_acid_three_to_one_map.put("M", "M");
        amino_acid_three_to_one_map.put("F", "F");
        amino_acid_three_to_one_map.put("W", "W");
        amino_acid_three_to_one_map.put("K", "K");
        amino_acid_three_to_one_map.put("Q", "Q");
        amino_acid_three_to_one_map.put("E", "E");
        amino_acid_three_to_one_map.put("S", "S");
        amino_acid_three_to_one_map.put("P", "P");
        amino_acid_three_to_one_map.put("V", "V");
        amino_acid_three_to_one_map.put("I", "I");
        amino_acid_three_to_one_map.put("C", "C");
        amino_acid_three_to_one_map.put("Y", "Y");
        amino_acid_three_to_one_map.put("H", "H");
        amino_acid_three_to_one_map.put("R", "R");
        amino_acid_three_to_one_map.put("N", "N");
        amino_acid_three_to_one_map.put("D", "D");
        amino_acid_three_to_one_map.put("T", "T");

        return amino_acid_three_to_one_map;
    }

    /**
     * Return the mutant residue for this point mutation.
     * 
     * @return
     */
    public char getMutResidue() {
        return mutResidue;
    }

    /**
     * Return the wild-type residue for this point mutation.
     * 
     * @return
     */
    public char getWtResidue() {
        return wtResidue;
    }

    /**
     * Two PointMutation objects are equal if their Position, WtResidue, and MutResidue values are all equal.
     * 
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PointMutation) {
            PointMutation pm = (PointMutation) obj;
            return (this.getPosition() == pm.getPosition() && this.getWtResidue() == pm.getWtResidue() && this.getMutResidue() == pm
                    .getMutResidue());
        } else {
            error("Invalid comparision. Trying to compare a PointMutation with: " + obj.getClass().getName());
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (this.getClass().getName() + this.toString()).hashCode();
    }

    /**
     * Override toString(), returns mutation as a string in wNm format
     * 
     * @return
     */
    @Override
    public String toString() {
        return (this.getWtResidue() + Integer.toString(this.getPosition()) + this.getMutResidue());
    }

    /**
     * Create PointMutation from wNm-format, single-letter abbreviated mention. This wrapper function creates a PointMutation object from a mutation
     * mention formatted in wNm-format, where w and m are the wild-type and mutant amino acids in their SINGLE-LETTER abbreviations, and N is an
     * integer representing the sequence position.
     * 
     * @param wNm
     *            a 3-character String representing a mutation mention in the wNm format
     * @return
     */
    public static PointMutation createPointMutationFrom_wNm(String wNm) throws MutationException {

        PointMutation pm;
        try {
            pm = new PointMutation(Integer.parseInt(wNm.substring(1, wNm.length() - 1)), Character.toString(wNm.charAt(0)), Character
                    .toString(wNm.charAt(wNm.length() - 1)));
            return pm;
        } catch (Exception e) {
            throw new MutationException("Improperly formatted mutation mention: " + wNm);
        }

    }

}
