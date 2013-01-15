Copyright (c) 2007 Regents of the University of Colorado
Please refer to licensing agreement at MUTATIONFINDER_HOME/doc/license.txt

Instructions for working with the mutation_finder.py and performance.py scripts
---

----
INSTALLATION NOTES
----
Download MutationFinder from http://mutationfinder.sourceforge.net. Unpack
the project with the command:

tar -xvzf MutationFinder<version_number>.tar.gz

You will now have a new directory called MutationFinder in your current
working directory.

After downloading and unpacking the system, if you plan to use the
mutation_finder.py script from any location outside of the install
directory, it is necessary to update the mutation_finder_home variable
in mutation_finder.py. Change the value of this variable to the full
path where your mutation_finder.py file lives.

For example, change:
mutation_finder_home = './'

to:
mutation_finder_home = '/path/to/MutationFinder'

____
RUNNING MutationFinder
____
If you have a file formatted as described in (Caporaso et al., 2007),
you can apply MutationFinder with the following steps:

> cd MutationFinder
> ./mutation_finder.py /path/to/your/input/file

A new file will be created in the current working directory called 
 input_filename.mf 

A non-default output directory can be specified with the -o flag. Run:

> ./mutation_finder.py -h

for more information on parameters which can be passed to mutation_finder.py

____ 
INPUT FILE FORMAT
____
The input files to be processed by MutationFinder should contain one 'document'
per line. Each line should be tab-delimited and contain two fields: a document 
identifier and the document text. See the devo_set.txt and test_set.txt files in
the MutationFinder/corpora directory for examples.

----
USAGE EXAMPLES
----
Examples for using the code (tested on MacOS X and Linux -- Windows tests 
 to follow). These assume that your current working directory is the 
 directory where the code has been unpacked.

# Apply MutationFinder to the test set discussed in (Caporaso et al., 2007);
# the results will be written to test_set.txt.mf
> ./mutation_finder.py test_set.txt
# Compare the output of MutationFinder to the gold standard data and 
# print the results
> ./performance.py test_set.txt.mf test_gold_std.txt 
# Run the unit tests for the mutation_finder.py script
> ./test_mutation_finder.py -v
# Run the unit tests for the performance.py script
> ./test_performance.py -v

Additional information on using these scripts is available by passing '-h'
to either script via the command line:

> mutation_finder.py -h
> performance.py -h

---
NOTES ON THE INCLUDED FILES
---

MutationFinder/
 |
 |- mutation_finder.py: the mutation finder script -- this is the system
 |   presented in (Caporaso et al., 2007) and can be applied to any
 |   text conforming to the format discussed in that paper. (For examples
 |   of the input format, see devo_set.txt and test_set.txt.) The '-b'
 |   option allows the user to apply the baseline system rather than 
 |   MutationFinder to the input texts. 
 |- test_mutation_finder.py: tests of the mutation finder script
 |- regex.txt: the collection of regular expressions used in MutationFinder. This
 |   file is read in by MutationFinder. Lines beginning with '#' are comments. 
 |   These are perl-style regular expressions, but make use of named capturing 
 |   groups. Lines ending with the text:
 |       [CASE_SENSITIVE]
 |   will yield case sensitive regular expressions. In the default regex.txt
 |   file, there is only one case sensitive regular expression on the first
 |   line. Refer to this as an example for how to define a case sensitive
 |   regular expression. 
 |
 |   If you are unfamiliar with the idea of named capturing groups, a regular
 |   expression feature introduced in Python, there is an introductory
 |   discussion of it here:
 |       http://www.regular-expressions.info/named.html
 |   In case that page disappears, you should be able to get information
 |   by googling for 'named group regular expression'.
 |- performance.py: the performance judgment script -- this compares the 
 |   output of the extraction system with to the gold standard answers
 |   and provides data on the three performance metrics discussed in
 |   (Caporaso et al., 2007): Extracted Mentions, Normalized Mutations,
 |   and Document Retrieval 
 |- test_performance.py: unit tests of the performance judgment script
|
 |- doc/ : documentation and licensing information
    |- README.txt: this file, contains general information about the package,
    |   and discussion and usage examples for python implementation of 
    |   MutationFinder
    |- README_java.txt: discussion and usage examples for java implementation
    |   of MutationFinder
    |- README_perl.txt: discussion and usage examples for perl implementation
    |   of MutationFinder
    |- license.txt: the license agreement associated with the MutationFinder 
    |   release and all associated corpora files, unit tests, and scripts.
 |
 |- corpora/ : test and development corpora and gold standards
    |- devo_set.txt: the development set texts, one abstract per line with 
    |   identifiers set as the PubMed identifiers of the source articles
    |- devo_gold_std.txt: the gold standard 'answers' -- these are the human-
    |   annotated mutations identified in the development set texts
    |- test_set.txt: the test set texts, one abstract per line with 
    |   identifiers set as the PubMed identifiers of the source articles
    |- test_gold_std.txt: the gold standard 'answers' -- these are the human-
    |   annotated mutations identified in the test set texts
 |- java/ source code for java implementation -- see 
 |    MutationFinder/doc/README_java.txt
 |- perl/ source code for perl implementation -- see 
 |    MutationFinder/doc/README_perl.txt



Please direct any questions to the author: gregcaporaso@gmail.com


----
Citing MutationFinder
----
Please cite MutationFinder with the following reference:
MutationFinder: A high-performance system for extracting point mutation 
mentions from text;  J. Gregory Caporaso, William A. Baumgartner Jr., David 
A. Randolph, K. Bretonnel Cohen, and Lawrence Hunter; Bioinformatics, 2007; 
doi: 10.1093/bioinformatics/btm235;

The article is publicly available from the Bioinformatics Journal's website - 
search under the doi cited above. (Once it goes to press we'll provide a direct
url for accessing the article. It is currently available via Advance Access,
and I expect the URL may change.) The article is an Open Access publication.
