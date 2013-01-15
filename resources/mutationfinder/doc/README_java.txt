Copyright (c) 2007 Regents of the University of Colorado
Please refer to licensing agreement at MUTATIONFINDER_HOME/doc/license.txt

MutationFinder (Java Implementation)

** Note: MutationFinder Java implementation requires Java 1.5 or greater. **

To run MutationFinder "out of the box" you must first obtain the file containing
the regular expressions used to detect mutations in text (regex.txt, this file was 
downloaded with MutationFinder). Alternatively, you can create your own set of 
regular expressions to use. Details describing the regular expression file format 
are located in the README file located in the doc directory.

--- Building MutationFinder ---
The Ant build.xml file provided will compile and package MutationFinder. The 
resulting jar file is located in the build/distribution directory. During the 
build process, all JUnit tests are run. For this reason, make sure that
junit.jar and jakarta-oro-2.08.jar are in your CLASSPATH before trying to build. 

--- Using MutationFinder ---
The Java implementation of MutationFinder can be called directly using its API 
from other Java programs, or it can be run from the command line.

To run the Java implementation of MutationFinder from the command line, from the 
project directory, type:

java -cp lib/jakarta-oro-2.0.8.jar:build/distribution/mutationFinder.jar edu.uchsc.ccp.nlp.ei.mutation.MutationFinder [REGEX_FILE] [INPUT_FILE] [OUTPUT_FILE]

where, 
[REGEX_FILE] is the file containing regular expressions used to detect mutations (typically the regex.txt file is used here)

[INPUT_FILE] is a file containing text to process. The input file in this case contains one document per line, where each line takes the format:
				documentID<tab>documentText
				
[OUTPUT_FILE] is a file where mutations that were detected in the input file are written. Mutations for a given document are listed on a single line using the format:
				documentID<tab>mutation<tab>mutation<tab>mutation...

				
To use the Java implementation of MutationFinder via its API, please see the main()
method in MutationFinder.java for an detailed example of how to first initialize MutationFinder 
and then to use it to extract mutation mentions from text. Briefly, to initialize MutationFinder and search the String text for mutations:

// initialize MutationFinder by loading the regular expression file
String regularExpressionFile = "regex.txt";
MutationFinder mf = new MutationFinder(regularExpressionFile);
// search the "text" String for mutations
String text = "This disease is caused by the A54T substitution in gene XYZ."
Map<Mutation, Set<int[]>> mutations = mf.extractMutations(text);

The resulting Map will contain Mutations that were detected in the text linked to a set of int arrays of size two. 
These int arrays store the span information for the detected mutation mentions, e.g. [spanStart, spanEnd].

For the example above, the Map will have a single entry "A54T" linked to the span [30,34].







