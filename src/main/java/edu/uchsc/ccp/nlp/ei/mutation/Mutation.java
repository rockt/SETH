
package edu.uchsc.ccp.nlp.ei.mutation;

import java.util.Map;

/**
 * Mutation is a base class for different types of mutations. Currently the only Mutation type defined is a PointMutation, but in the future Insertion
 * and Deletion objects may also be defined.
 * 
 * @author William A. Baumgartner, Jr. <br>
 *         william.baumgartner@uchsc.edu
 * @version 1.0
 * 
 */

/*
 * Copyright (c) 2007 Regents of the University of Colorado
 * Please refer to the licensing agreement at 
 */

public class Mutation {

	private int id; //By which pattern has the mutation been found?
    private String position;

    /**
     * 
     * @param position --
     *            the sequence position or start position of the mutation
     */
    public Mutation(int position) throws MutationException {
        this.position = Integer.toString(position);
    }
    /**
     * 
     * @param position --
     *            the sequence position or start position of the mutation
     */
    public Mutation(String position) {
    	this.position = position;
    }

    /**
     * Retrieves the mutation sequence position
     * 
     * @return an int representing the position or start position of the mutation
     */
    public String getPosition() {
        return position;
    }
    
    /**
    *  Retrieves the pattern id
    * 
    * @return an int representing the patternId
    */
    public int getId() {
		return id;
	}
    
	public void setId(int id) {
		this.id = id;
	}
	/*
     * This is a simple utility method to combine to mappings. In the context of MutationFinder, this method is used to combine mappings among amino
     * acid codes. The mappings contained in "additionalMappings" are added to "inputMap"
     */
    protected void updateMapping(Map<String, String> inputMap, Map<String, String> additionalMappings) {
        for (String key : additionalMappings.keySet()) {
            if (!inputMap.containsKey(key)) {
                inputMap.put(key, additionalMappings.get(key));
            } else {
                warn("Key already exists, original value retained while updating a mapping: " + key + " Old value: " + inputMap.get(key) + " Attempted new value: " + additionalMappings.get(key));
            }
        }
    }
    
    protected void warn(String message) {
        System.err.println("WARNING -- " + this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')) + message);
    }
    
    protected void error(String message) {
        System.err.println("ERROR -- " + this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')) + message);
    }
    
    /**
     * To be overridden by subclasses
     */
    @Override
    public boolean equals(Object obj){
        throw new UnsupportedOperationException();
    }

    /**
     * To be overridden by subclasses
     */
    @Override
    public int hashCode() {
        throw new UnsupportedOperationException();
    }

    /**
     * To be overridden by subclasses
     */
    @Override
    public String toString() {
        throw new UnsupportedOperationException();
    }

}
