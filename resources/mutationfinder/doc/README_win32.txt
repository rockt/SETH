Copyright (c) 2007 Regents of the University of Colorado
Please refer to licensing agreement at MUTATIONFINDER_HOME/doc/license.txt

Instructions for working with the mutation_finder.exe and 
performance.exe scripts
---

----
INSTALLATION NOTES
----
Download MutationFinder from
http://mutationfinder.sourceforge.net. Uncompress the
tar/gz file with 7-Zip (www.7zip.org) or a similar utility.

You will be asked where to unpack the zip file. For the purpose of 
this example, we will assume that you have unpacked it to: 

C:\


Open a command prompt by clicking

Start > Run

then typing:

cmd

and pressing the Enter key.


You may now navigate to a new directory called C:\MutationFinder (or
the MutationFinder directory under the directory where you unpacked 
the distribution). 

C:\> cd C:\MutationFinder\bin\Win32

You can run MutationFinder from this directory.

----
RUNNING MutationFinder
----
If you have a file formatted as described in (Caporaso et al., 2007),
you can apply MutationFinder with the following steps:

    C:\>cd C:\MutationFinder\bin\Win32
    C:\MutationFinder\bin\Win32>.\mutation_finder \path\to\your\input\file

A new file will be created in the current working directory called 
"input_filename.mf."

A non-default output directory can be specified with the -o flag. Run:

    C:\MutationFinder\bin\Win32>.\mutation_finder -h

for more information on parameters which can be passed to mutation_finder.

---- 
INPUT FILE FORMAT
----
The input files to be processed by MutationFinder should contain one 'document'
per line. Each line should be tab-delimited and contain two fields: a document 
identifier and the document text. See the devo_set.txt and test_set.txt files in
the MutationFinder/corpora directory for examples.

----
USAGE EXAMPLES
----
# Apply MutationFinder to the test set discussed in (Caporaso et al., 2007);
# the results will be written to test_set.txt.mf
C:\MutationFinder\bin\Win32>.\mutation_finder C:\MutationFinder\corpora\test_set.txt

# Compare the output of MutationFinder to the gold standard data and 
# print the results
C:\MutationFinder\bin\Win32>.\performance test_set.txt.mf C:\MutationFinder\corpora\test_gold_std.txt 

Additional information on using these programs is available by passing '-h'
to either on the command line:

    C:\MutationFinder>.\mutation_finder.py -h
    C:\MutationFinder>.\performance.py -h


Please direct any questions to the author: gregcaporaso@gmail.com.

----
OBTAINING THE SOURCE
----
MutationFinder is open-source software, distributed under the terms 
of the MIT license. The source code is freely available at 
http://mutationfinder.sourceforge.net.

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
