package edu.uchsc.ccp.nlp.ei.mutation;

/**
 * Exception thrown by MutationFinder
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

public class MutationException extends Exception {

    private static final long serialVersionUID = 2878203770705173330L;

    private final String errorMessage;

    public MutationException(String message) {
        super();
        this.errorMessage = message;
    }

    @Override
    public String toString() {
        return errorMessage;
    }

}
