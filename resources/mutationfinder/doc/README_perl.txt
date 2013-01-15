Copyright (c) 2007 Regents of the University of Colorado
Please refer to licensing agreement at MUTATIONFINDER_HOME/doc/license.txt

Instructions for working with the mutation_finder.pl script.
---

----
INSTALLATION NOTES
----
Download MutationFinder from http://mutationfinder.sourceforge.net. Unpack
the project with the command:

    tar -xvzf MutationFinder<version_number>.tar.gz

You will now have a new directory called MutationFinder in your current
working directory. The mutation_finder.pl script resides in the perl
subdirectory.

----
RUNNING MutationFinder
----
The following modules must be included in your Perl distribution in order
to run mutation_finder.pl:

    strict
    Getopt::Long
    FindBin
    File::Basename
    Data::Dumper

Given a file formatted as described in (Caporaso et al., 2007),
you may apply MutationFinder with the following commands:

    cd MutationFinder/perl
    ./mutation_finder.pl /path/to/your/input/file

A new file will be created in the current working directory called
input_filename.mf.

A non-default output directory can be specified with the -o flag. 

Run

    ./mutation_finder.pl --help

for more information on usage.

----
INPUT FILE FORMAT
----
The input files to be processed by MutationFinder should contain one "document"
per line. Each line should be tab-delimited and contain two fields: a document
identifier and the document text. See the devo_set.txt and test_set.txt files in
the MutationFinder/corpora directory for examples.

----
USAGE EXAMPLES
----
The following examples assume that your current working directory is 
MutationFinder/perl.

# Apply MutationFinder to the test set discussed in (Caporaso et al., 2007);
# the results will be written to test_set.txt.mf
> ./mutation_finder.pl test_set.txt
# Compare the output of MutationFinder to the gold standard data and
# print the results
> ../performance.py test_set.txt.mf test_gold_std.txt

----
CITING MutationFinder
----
Please cite MutationFinder with the following reference:
MutationFinder: A high-performance system for extracting point mutation
mentions from text;  J. Gregory Caporaso, William A. Baumgartner Jr., David
A. Randolph, K. Bretonnel Cohen, and Lawrence Hunter; Bioinformatics, 2007;
doi: 10.1093/bioinformatics/btm235;

The article is publicly available from the Bioinformatics Journal's website.
Search under the doi cited above. (Once it goes to press we'll provide a direct
URL for accessing the article. It is currently available via Advance Access,
and I expect the URL may change.) The article is an Open Access publication.

