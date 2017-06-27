package edu.uchsc.ccp.nlp.ei.mutation;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

/**
 * 
 * This is the Java implementation of MutationFinder (original version in Python by J. Gregory Caporaso). <br>
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


public class MutationFinder extends MutationExtractor {

    /*
     * Define the case-sensitive flag used for the regular_expression input file. This flag is appended to each regular-expression that is
     * case-sensitive
     */
    private final String CASE_SENSITIVE = "[CASE_SENSITIVE]";

    protected final static String MUT_RES = "?P<mut_res>";

    protected final static String WT_RES = "?P<wt_res>";

    protected final static String POS = "?P<pos>";

    protected final static Set<String> ambiguous  = new HashSet<String>();     //Contains a list of ambiguous "mutation"-names (e.g., T47D generally refers to a cell-line and not to a mutation)

    /*
     * regular_expressions: an interative set of regular expressions to be applied for extracting mutations. These are in the default python syntax
     * (i.e., perl regular expressions), with the single exception being that regular expressions which should be performed in a case sensitive manner
     * should be followed by the string '[CASE_SENSITIVE]', with no spaces between it and the regular expression. This can be a list, a file, or any
     * other object which supports iteration. For an example, you should refer to the regex.txt file in the MutationFinder directory.
     * 
     * Since Java does not implement "named groups", we must process the Python regular expressions and store a mapping of the three components of a
     * point mutation to the groups that they are members of. These mappings are stored in the Map<String, Integer>, where the String is one of
     * MUT_RES, WT_RES, or POS, and the Integer represents its parenthetical group.
     */
    private Map<MyPattern, Map<String, Integer>> regular_expressions;

    /**
     * Initialization of MutationFinder requires a set of regular expressions that will be used to detect mutations.
     * This constructor loads the regular expressions from the attached Java Archive at resources/mutations.txt.<br>
     * <br>
     */
    public MutationFinder() {
        loadRegularExpressionsFromJar("/resources/mutations.txt");
        initializeAmbiguousMentions();
    }

    /**
     * Initialization of MutationFinder requires a set of regular expressions that will be used to detect mutations. This constructor loads the
     * regular expressions from a file designated by the filename input parameter.<br>
     * <br>
     * 
     * @param fileName
     *            Since the original development of MutationFinder was conducted in Python, the input file contains regular expressions that are
     *            Python-specific (due to the fact that that Java does not handle explicitly named groups). These regular expressions must therefore
     *            be converted prior to use in the Java implementation. This conversion is handled by this constructor.
     */
    public MutationFinder(String fileName) {
        File file = new File(fileName);
        loadRegularExpressionsFromFile(file);
        initializeAmbiguousMentions();
    }

    /**
     * Initialization of MutationFinder requires a set of regular expressions that will be used to detect mutations. This constructor loads the
     * regular expressions from a file designated by the Java File input parameter.<br>
     * <br>
     * 
     * @param file
     *            Since the original development of MutationFinder was conducted in Python, the input file contains regular expressions that are
     *            Python-specific (due to the fact that that Java does not handle explicitly named groups). These regular expressions must therefore
     *            be converted prior to use in the Java implementation. This conversion is handled by this constructor.
     */
    public MutationFinder(File file) {
        loadRegularExpressionsFromFile(file);
    }

    /**
     * Initialization of MutationFinder requires a set of regular expressions that will be used to detect mutations. This constructor loads the
     * regular expressions from a Set of Strings representing the regular expressions.<br>
     * <br>
     * 
     * @param unprocessed_python_regexes
     *            Since the original development of MutationFinder was conducted in Python, the set of regular expressions used is Python-specific in
     *            that Java does not handle explicitly named groups. These regular expressions must therefore be converted prior to use in the Java
     *            implementation. This conversion is handled by this constructor.
     */
    public MutationFinder(Set<String> unprocessed_python_regexes) {
        regular_expressions = new HashMap<MyPattern, Map<String, Integer>>();
        int elementNumber =0;
        for (String python_regex : unprocessed_python_regexes) {
            processPythonRegex(python_regex, elementNumber++);
        }
    }

    /*
     * The regular expressions are in Python format which is incompatible with Java due to the explicitly named groups. To make them compatible, we
     * must remove the ?P<wt_res> ?P<pos> ?P<mt_res> tags, and log the group number (based on counting parentheses) that they are members of. The
     * regular expressions are then compiled, with or without the case-insensitive flag.
     */
    private void processPythonRegex(String regexStr, int line) {
        Map<String, Integer> groupMappings = extractMappingsFromPythonRegex(regexStr);

        /* remove the ?P<wt_res>, ?P<pos>, and ?P<mt_res> tags from the regular expression string */
        regexStr = removeTagsFromPythonRegex(regexStr);

        Perl5Compiler compiler = new Perl5Compiler();
        try {
            /* convert the regular expression string into a Pattern class here and add it to the regular_expressions Set */
            if (regexStr.endsWith(CASE_SENSITIVE)) {
                regular_expressions.put(new MyPattern(compiler.compile(regexStr.substring(0, regexStr.lastIndexOf('['))), regexStr, line), groupMappings);
            } else {
                regular_expressions.put(new MyPattern(compiler.compile(regexStr, Perl5Compiler.CASE_INSENSITIVE_MASK), regexStr, line), groupMappings);
            }
        } catch (MalformedPatternException mpe) {
            mpe.printStackTrace();
        }
    }

    /*
     * Loads regular_expressions from file. Each line in the file is a single regular expression. Those that should be performed in a case sensitive
     * manner should be followed by the string '[CASE_SENSITIVE]', with no spaces between it and the regular expression.
     */
    private void loadRegularExpressionsFromFile(File file) {

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            loadRegularExpressionsFromStream(br);
        } catch (FileNotFoundException fnfe) {
            error("The file containing regular expressions could not be found: " + file.getAbsolutePath() + File.separator + file.getName());
            //fnfe.printStackTrace();
            loadRegularExpressionsFromJar("/resources/mutations.txt");
        }
        finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    // ignore exception
                }
            }
        }
   }


    /*
    * Loads regular_expressions from Java-Archive. Each line in the file is a single regular expression. Those that should be performed in a case sensitive
    * manner should be followed by the string '[CASE_SENSITIVE]', with no spaces between it and the regular expression.
    */
    private void loadRegularExpressionsFromJar(String file) {
        System.out.println("Loading regular expressions from Java Archive at location '" +file +"'");
        InputStream is = this.getClass().getResourceAsStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        try{
            loadRegularExpressionsFromStream(br);
        }catch(Exception ex){
         error("Error in fallback code for reading mutation-finder file from Java Archive");
         ex.printStackTrace();
        }
        finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    // ignore exception
                }
            }
        }
    }


    /**
     * Helper method which loads a set of regular expressions from a BufferedReader
     * This method is used for loading regex from a file or the java-archive
     * @param br buffer to read the regex from
     */
    private void loadRegularExpressionsFromStream(BufferedReader br) {

        regular_expressions = new HashMap<MyPattern, Map<String, Integer>>();
        String line;
        int lineNumber = 0;
        try {
            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (!line.startsWith("#")) {
                    processPythonRegex(line, lineNumber);

                    if (lineNumber % 100 == 0) {
                        System.err.println("Loading regex's: " + lineNumber);
                    }
                }
            }
        } catch (IOException ioe) {
            error("IO Exception while processing regular expression.");
            ioe.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    // ignore exception
                }
            }
        }
        System.err.println("Completed loading of regular expressions: " + regular_expressions.size() + " loaded.");
    }

    /**
     * Add some ambiguous mentions, which often refer to other concepts instead of mutations
     */
    private void initializeAmbiguousMentions(){
        ambiguous.add("T47D");
        ambiguous.add("T98G");
        ambiguous.add("L5178Y");
        ambiguous.add("J774A");
        ambiguous.add("H295R");
        ambiguous.add("F442A");
        ambiguous.add("C33A");
        ambiguous.add("C57L");
        ambiguous.add("J558L");
        ambiguous.add("A375P");
        ambiguous.add("A375M");
        ambiguous.add("C-33A");
        ambiguous.add("V38A");
        ambiguous.add("R201C");
        ambiguous.add("B10R");
        ambiguous.add("K562R");
        ambiguous.add("B10S");
        ambiguous.add("R3327H");
        ambiguous.add("H322M");
        ambiguous.add("N1003A");
        ambiguous.add("H295A");
        ambiguous.add("A5H");
        ambiguous.add("T42A");
        ambiguous.add("V15B");
        ambiguous.add("H510A");
        ambiguous.add("T2C");


        ambiguous.add("S100B");//A frequent protein
    }

    /*
     * This method extracts the parenthetical groups for the three named groups in each python regex (MUT_RES, WT_RES, and POS), and returns a mapping
     * between them and their group numbers. It also removes them from the input string so that the pattern becomes Java-complient.
     */
    public static Map<String, Integer> extractMappingsFromPythonRegex(String pythonRegex) {
        Map<String, Integer> groupMappings = new HashMap<String, Integer>();

        /* find mapping for MUT_RES */
        groupMappings.put(MUT_RES, countRegExParenthesesBeforeIndex(pythonRegex, pythonRegex.indexOf(MUT_RES)));

        /* find mapping for WT_RES */
        groupMappings.put(WT_RES, countRegExParenthesesBeforeIndex(pythonRegex, pythonRegex.indexOf(WT_RES)));

        /* find mapping for POS */
        groupMappings.put(POS, countRegExParenthesesBeforeIndex(pythonRegex, pythonRegex.indexOf(POS)));

        return groupMappings;
    }

    public static String removeTagsFromPythonRegex(String regexStr) {
        return regexStr.replaceAll("\\?P<mut_res>", "").replaceAll("\\?P<wt_res>", "").replaceAll("\\?P<pos>", "");
    }

    /*
     * This method counts the number of open parentheses that are part of the regular expression (i.e. not escaped, "\(") before the given index.
     */
    private static int countRegExParenthesesBeforeIndex(String regexStr, int index) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("[^\\\\]\\((?!\\?:)"); //Means !\(; everything instead of '\' followed by (; than not followed(?!) by ?:
        java.util.regex.Matcher m = p.matcher(regexStr);
        int count = 0;
        int currentIndex = -1;
        while (m.find() && currentIndex < index) {
            count++;
            currentIndex = m.start();
        }
        return count -1; //The first expression is not counted
    }

    /**
     * Perform precision increasing post-processing steps. Remove false positives indicated by: mutant and wild-type residues being identical (e.g.
     * A42A)
     */
    private void postProcess(Map<Mutation, Set<int[]>> mutations) {
        List<Mutation> mutationsToDelete = new ArrayList<Mutation>();
        for (Mutation mutation : mutations.keySet()) {
            if (mutation instanceof PointMutation) {
                PointMutation pm = (PointMutation) mutation;
                if (pm.getWtResidue() == pm.getMutResidue()) {
                    mutationsToDelete.add(pm);
                }
            }
        }

        /* now delete false positive mutations */
        for (Mutation mutation : mutationsToDelete) {
            mutations.remove(mutation);
        }
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
    public Map<Mutation, Set<int[]>> extractMutations(String rawText) throws MutationException {
        Map<Mutation, Set<int[]>> extractedMutations = new HashMap<Mutation, Set<int[]>>();

        /* cycle through each pattern and look for matches */
        for (MyPattern pattern : regular_expressions.keySet()) {
            PatternMatcher m = new Perl5Matcher();
            PatternMatcherInput input = new PatternMatcherInput(rawText);

            /* recall the group numbers for the POS, WT_RES, and MUT_RES tags within the patterns */
            final Map<String, Integer> groupMappings = regular_expressions.get(pattern);

            //Minor speed gain, if this is not declared in the while loop
            int pos_group = groupMappings.get(POS);
            int wtres_group = groupMappings.get(WT_RES);
            int mutres_group = groupMappings.get(MUT_RES);

            /* create a new PointMutation for each match */
            while (m.contains(input, pattern.getPattern())) {
            	int id = pattern.getId();

                MatchResult result = m.getMatch();
//                System.out.println(result.group(pos_group) +" " +result.group(wtres_group) +" " +result
//                        .group(mutres_group));
                try{

                	PointMutation pm = new PointMutation(result.group(pos_group).replaceAll("\\s", ""), result.group(wtres_group), result
	                        .group(mutres_group));

                    //Add only mutation if it is valid (basically checks comination of location and extracted residues)
	                if(pm.isValid() == false)
	                	continue;

                    //If a mutation is deemed to be ambiguous, we check if the extracted wildtype or mutated residue are in fact long forms (e.g., Ala)
                    if(ambiguous.contains(pm.toString()) && result.group(wtres_group).trim().length() == 1 && result.group(mutres_group).trim().length() == 1)
                        continue;

	                pm.setId(id);

	                // /*
	                // * The span of the mutation is calculated as the min # start span of the three components and the max end span # of the three
	                // * components -- these are then packed up as an int array of size 2.
	                // */
	                //
	                int[] span = new int[2];
	                span[0] = Math.min(result.beginOffset(pos_group), Math.min(result.beginOffset(wtres_group), result
	                        .beginOffset(mutres_group)));
	                span[1] = Math.max(result.endOffset(pos_group), Math.max(result.endOffset(wtres_group), result.endOffset(mutres_group)));

	                /* now store the mutation and the span */
	                if (extractedMutations.containsKey(pm)) {
	                    extractedMutations.get(pm).add(span);
	                } else {
	                    Set<int[]> spans = new HashSet<int[]>();
	                    spans.add(span);
	                    extractedMutations.put(pm, spans);
	                }
                }catch(NumberFormatException nfe){
                	System.err.println("Problem parsing: " +result.group(pos_group) +" " +result.group(wtres_group) +" " +result.group(mutres_group));
                }
            }
        }

        /* run the post-processing filter to remove known/suspected false positives */
        postProcess(extractedMutations);

        return extractedMutations;
    }



    /**
     * The main method demonstrates the execution of MutationFinder. Three input arguments are required, the regular expression file used by
     * MutationFinder, an input file to process, and output file to write the generated results.<br>
     * <br>
     * The input file in this case contains one document per line, where each line takes the format:<br>
     * <br>
     * documentID<tab>documentText<br>
     * <br>
     * The output file will contain the mutations found for each document (one document per line) where each line takes the format:<br>
     * <br>
     * documentID<tab>mutation<tab>mutation<tab>mutation...<br>
     * 
     * @param args
     *            args[0] - the regular expression file<br>
     *            args[1] - the input file containing text to process<br>
     *            args[2] - the output file
     */
    public static void main(String[] args) {
        try {
            /* Ensure that there are three input arguments */
            if (args.length == 3) {
                String regularExpressionFile = args[0];
                String inputFileLocation = args[1];
                String outputFileLocation = args[2];

                System.out.print("Initializing MutationFinder...");
                MutationFinder mf = new MutationFinder(regularExpressionFile);
                /* open up input file */
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFileLocation)));

                /* open up output file */
                PrintStream ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(outputFileLocation)));

                String line;
                while ((line = br.readLine()) != null) {
                    /* find the first tab character, this marks the end of the id and the beginning of the text */
                    int firstTabIndex = line.indexOf("\t");
                    String id = line.substring(0, firstTabIndex);
                    String text = line.substring(firstTabIndex + 1);

                    String outputLine = id;
                    Map<Mutation, Set<int[]>> mutations = mf.extractMutations(text);
                    for (Mutation mutation : mutations.keySet()) {
                        for (int i = 0; i < mutations.get(mutation).size(); i++) {
                            outputLine += ("\t" + ((PointMutation) mutation).toString());
                        }
                    }
                    ps.println(outputLine);
                }

                br.close();
                ps.close();
            } else {
                System.err
                        .println("ERROR: Invalid number of input parameters. Execution of MutationFinder requires three input parameters.");
                System.err.println("USAGE: MutationFinder regularExpressionFile inputFile outputFile");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
