#!/usr/bin/env python
# Author: Greg Caporaso (gregcaporaso@gmail.com)
# performance.py

from __future__ import division
from optparse import OptionParser
from sys import exit
from mutation_finder import PointMutation_from_wNm

""" Description
File created on 02 Feb 2007.

This file can be used as a script for judging the performance of a 
MutationExtractor object compared to a gold standard, or the objects 
can be imported to automate comparisons of MutationExtractors.

Script usage:
> performance.py /path/to/extracted/mutations /path/to/gold/standard
> performance.py -h

Direct use of objects (e.g.):
from mutation_finder import extract_mutations_from_lines_to_dict

pc = PerformanceCalculator(parse_extraction_data(open(\
    '/path/to/gold/standard')))
pcr = pc.calculate_extracted_mentions(\
    extract_mutations_from_lines_to_dict(open(\
    '/path/to/input/text')))

print pcr.FMeasure

Copyright (c) 2007 Regents of the University of Colorado
Please refer to licensing agreement at MUTATIONFINDER_HOME/doc/license.txt

"""

version_number = '0.9'

class PerformanceCalculatorResult(object):
    """ An object to store results from a PerformanceCalculator 

        This is the data type returned by the PerformanceCalculator methods,
            and keeps track of the true positives, false positives, false 
            negatives, and true negatives. Additionally it calculates
            precision, recall, and f-measure from these data, and provides
            pretty printing of all of these metrics. After initializing 
            a PerformanceCalculatorResult, such as:
            
            pcr = PerformanceCalculatorResult(tp,fp,fn,tn)

            All of the values can be accessed as:
            
            pcr.TruePositive
            pcr.FalsePositive
            pcr.FalseNegative
            pcr.TrueNegative
            pcr.Precision
            pcr.Recall
            pcr.FMeasure

            This will allow for convenient programmatic access to these
            metrics, and therefore is useful for automated comparisons of 
            extraction systems, as would be done if you were automatically 
            optimizing a system on any of the individual metrics.

        Pretty formatting results in strings formatted as:

        > print pcr.pretty_confusion_matrix
        tp      fn
        fp      tn
        --
        574     333
        2       n/a
        
        > print pcr.pretty_precision_recall_fmeasure
        Precision       Recall  F-measure
        0.9965  0.6329  0.7741 
      
        > print str(pcr)
        tp      fn
        fp      tn
        --
        574     333
        2       n/a
        Precision       Recall  F-measure
        0.9965  0.6329  0.7741       
      
    All fields in each line are tab-delimited.  

    """

    def __init__(self,TruePositive,FalsePositive,\
                      FalseNegative,TrueNegative=None):
        """ Initialize the object

            TruePositive: the count of true positives (must be castable to
                an int)
            FalsePositive: the count of false positives (must be castable to
                an int)
            FalseNegative: the count of false negatives (must be castable to
                an int)
            TrueNegative: the count of true negatives -- since this is not
                always calculatable, if the value is not castable to an int
                it is stored as None

            Each of these values, in addition to Precision, Recall, and 
                FMeasure are accessible as properties of objects. They should 
                not be modified after instantiation.

        """
        self._tp = int(TruePositive)
        self._fp = int(FalsePositive)
        self._fn = int(FalseNegative)
        try:
            self._tn = int(TrueNegative)
        except (TypeError,ValueError):
            self._tn = None

        # Calculate and store Precision, Recall, and F-measure 
        self._p = self._calculate_precision()
        self._r = self._calculate_recall()
        self._f = self._calculate_f_measure()

    ## Define properties for accessing the individual values
    def _get_tp(self):
        return self._tp
    TruePositive = property(_get_tp)
 
    def _get_fp(self):
        return self._fp
    FalsePositive = property(_get_fp)

    def _get_fn(self):
        return self._fn
    FalseNegative = property(_get_fn)

    def _get_tn(self):
        return self._tn
    TrueNegative = property(_get_tn)

    def _get_p(self):
        return self._p 
    Precision = property(_get_p)

    def _get_r(self):
        return self._r
    Recall = property(_get_r)

    def _get_f(self):
        return self._f
    FMeasure = property(_get_f)

    def _calculate_precision(self):
        """ Calculate and return precision
        """
        try:
            return self._tp / (self._tp + self._fp)
        except ZeroDivisionError:
            return None

    def _calculate_recall(self):
        """ Calculate and return recall
        """
        try:
            return self._tp / (self._tp + self._fn)
        except ZeroDivisionError:
            return None

    def _calculate_f_measure(self):
        """ Calculate and return F-measure
        """
        try:
            return (2. * self._p * self._r)/(self._p + self._r)
        except (TypeError,ZeroDivisionError):
            return None

    def pretty_confusion_matrix(self):
        """ Return a nicely formatted confusion matrix as a string"""
        tn = self._tn or 'n/a'
        result = ["tp\tfn","fp\ttn","--"]
        result.append('\t'.join([str(self._tp),str(self._fn)]))
        result.append('\t'.join([str(self._fp),str(tn)]))
        return '\n'.join(result)

    def pretty_precision_recall_fmeasure(self):
        """ Return a nicely formated string containing p, r, and f """
        result = ["Precision\tRecall\tF-measure"]
        try:
            result.append('%(pre)0.4f\t%(re)0.4f\t%(fm)0.4f'\
             % {'pre':self._p,'re':self._r,'fm':self._f})
        except TypeError:
            if self._p and self._r:
                result.append(''.join(['%(pre)0.4f\t%(re)0.4f\t',str(self._f)])\
                 % {'pre':self._p,'re':self._r})
            else:
                result.append('\t'.join(map(str,[self._p,self._r,self._f])))
        return '\n'.join(result)

    def __str__(self):
        """ Return a nicely formatted confusion matrix and P/R/F """
        return '\n'.join([self.pretty_confusion_matrix(),\
                          self.pretty_precision_recall_fmeasure()])

class PerformanceCalculatorError(RuntimeError):
    pass       

class PerformanceCalculator(object):
    """ A class for calculating performance metrics of MutationExtractors 

        Three performance metrics, described in (Caporaso et al., 2007)
            are calculated by comparing the results of a mutation
            extraction system (extractor_output) with a gold standard 
            data set (gold_standard). These are in identical format,
            a 2D dict, where top-level keys represent document identifiers
            and 2nd-level keys are mutations in wNm format with their 
            counts as values. Counts must be positive integers for the
            results to be meaningful, but this IS NOT EXPLICITLY TESTED.
        Example format for gold_standard and extractor_results dicts:

            {'3476160': {'S87C': 1, 'T22C': 1}, '14500716': {}, 
            '12206666': {'D95A': 4, 'D95N': 2, 'D95E': 2},
            '11327835': {'H64A': 4}}

        In this example, PubMed Identifiers (PMIDs) serve as the top-level
            keys. 

        For these calculations to be accurate, the gold standard and the
            extractor output must represent extraction of mutations from
            an identical collection of texts. Since this is an easy 
            mistake to make (e.g. accidentally creating a 'sparse' input,
            where only documents with mutations are provided) this IS 
            EXPLICITLY TESTED. An error is 
            raised if the keys in the two dicts (self._gold_standard and
            extractor_output) are not identical. This is determined in two 
            steps: first, the lengths of both are calculated, and must be 
            equal; next, when iterating over the gold standard, if a key
            is found which is not in extractor output, an error is raised.
            These two tests ensure equality between the set of keys in 
            each, and are performed each time extractor_output is passed
            to one of the methods.

    """
    def __init__(self,gold_standard):
        """ Initialize the PerformanceCalculator with the gold standard"""
        self._gold_standard = gold_standard

    def calculate_extracted_mentions(self,extractor_output):
        """ Calculate the Extracted Mentions performance metric discussed
                in (Caporaso et al., 2007) and return a 
                PerformanceCalculatorResult object.
        """
        # Ensure that the lengths of the two dicts are identicial -- 
        # this is the first step in ensuring that their keys are identical
        if len(self._gold_standard) != len(extractor_output):
            raise PerformanceCalculatorError, \
                'Gold standard and extractor output must contain identical identifiers'
        #self.validate_data_sets(extractor_output)
        tp = 0
        fp = 0
        fn = 0
        # iterate over the documents
        for expected_id,expected_mutations in self._gold_standard.items():
            # Raise an error if a gold standard key is not present in the 
            # extractor output -- this is the second step in ensuring that
            # their keys are identical
            try:
                extractor_output_mutations = extractor_output[expected_id]
            except KeyError:
                raise PerformanceCalculatorError,\
                    'Gold standard and extractor output must contain identical identifiers'
            # iterate over the mutations found by the system
            for mutation,n in extractor_output_mutations.items():
                # if a mutation was found that exists in the document
                if mutation in expected_mutations:
                    # get the number of times it occurs in the document
                    n_expected = expected_mutations[mutation]
                    # if the number of times it occurs is equal to the number 
                    # of times it was found, add the number of times it was 
                    # found to the number of true positives
                    if n_expected == n:
                        tp += n
                    # if there is a difference in the number of times it was 
                    # found and the number of times it appears, the situation 
                    # is a little trickier
                    else:
                        # if it was found more than it exists, add the number 
                        # of times it exists to the number of true positives 
                        # (signifying that all mentions were found) and add 
                        # the extra 'finds' to the number of false positives 
                        # (signifying that the extraction system pulled some 
                        # things that were not the mutation)
                        if n_expected < n:
                            tp += n_expected
                            fp += n - n_expected
                        # if all mentions were not found, add the number of 
                        # mentions that were found to the number of true 
                        # positives, and add the number that were missed to 
                        # the false negatives
                        else:
                            tp += n
                            fn += n_expected - n
                # if a mutation was found that was not in the expected, add 
                # the number of times it was found to the number of false 
                # positives
                else:
                    fp += n
            for mutation,n in expected_mutations.items():
                # if a mutation exists in the expected mutations, but was 
                # not found, add the number of times it exists in the 
                # expected to the number of false negatives
                if mutation not in extractor_output_mutations:
                    fn += n
        # Return performance metrics
        return PerformanceCalculatorResult(tp,fp,fn,None)

    def calculate_normalized_mutations(self,extractor_output):
        """ Calculate the Normalized Mutations performance metric discussed
                in (Caporaso et al., 2007) and return a 
                PerformanceCalculatorResult object.

        """
        # Ensure that the lengths of the two dicts are identicial -- this 
        # is the first step in ensuring that their keys are identical
        if len(self._gold_standard) != len(extractor_output):
            raise PerformanceCalculatorError, \
                'Gold standard and extractor output must contain identical identifiers'

        tp = 0
        fp = 0
        fn = 0
        # iterate over the expected results
        for expected_id,expected_mutations in self._gold_standard.items():
            # Raise an error if a gold standard key is not present in the 
            # extractor output -- this is the second step in ensuring that
            # their keys are identical
            try:
                extractor_output_mutations = \
                    extractor_output[expected_id].keys()
            except KeyError:
                raise PerformanceCalculatorError,\
                 'Gold standard and extractor output must contain identical identifiers'
            # if a mutation was found by the system ...
            for mutation in extractor_output_mutations:
                # ... and it exists in the document, it's a true positive
                if mutation in expected_mutations:
                    tp += 1
                # ... and it does not exist in the doucment, it's 
                # a false positive
                else:
                    fp += 1
            # for each mutation that exists in the current document
            for mutation in expected_mutations:
                # if the mutation was not found by the system, 
                # it's a false negative
                if mutation not in extractor_output_mutations:
                    fn += 1
        # Return the performance metrics
        return PerformanceCalculatorResult(tp,fp,fn,None)

    def calculate_document_retrieval(self,extractor_output):
        """ Calculate the Document Retrieval performance metric discussed
                in (Caporaso et al., 2007) and return a 
                PerformanceCalculatorResult object.

        """
        # Ensure that the lengths of the two dicts are identicial -- this 
        # is the first step in ensuring that their keys are identical
        if len(self._gold_standard) != len(extractor_output):
            raise PerformanceCalculatorError, \
                'Gold standard and extractor output must contain identical identifiers'

        tp = 0
        tn = 0
        fp = 0
        fn = 0
        # iterate over the documents
        for expected_id,expected_mutations in self._gold_standard.items():
            # Raise an error if a gold standard key is not present in the 
            # extractor output -- this is the second step in ensuring that
            # their keys are identical
            try:
                extracted_mutations = extractor_output[expected_id]
            except KeyError:
                raise PerformanceCalculatorError, \
                 'Gold standard and extractor output must contain identical identifiers'

            # if mutations exist in the current document ...
            if expected_mutations:
                # ... and the system found mutations, 
                # this is a true positive
                if extracted_mutations:
                    tp += 1
                # ... and the system did not find mutations, 
                # this is a false negative
                else:
                    fn += 1
            # if mutations don't exist in the current document ...
            else:
                # ... and mutations were found by the system, 
                # this is a false positive
                if extracted_mutations:
                    fp += 1
                # ... and no mutations were found by the system, 
                # this is a true negative
                else:
                    tn += 1
        # Return performance values
        return PerformanceCalculatorResult(tp,fp,fn,tn)

### Code for script functionality

def parse_extraction_data(extraction_data):
    """ Parses mutation extraction output and gold standard files

        Parse a file containing extracted mutations data into a 2D dict.

        Example input (each line represents a line in a file and elements in
            each line are tab-delimited):
        3476160 T22C    S87C
        14500716
        12206666    D95A    D95A    D95A    D95E    D95E    D95A    D95N    D95N
        11327835    H64A    H64A    H64A    H64A

        Example output:
             {'3476160': {'S87C': 1, 'T22C': 1}, '14500716': {}, 
            '12206666': {'D95A': 4, 'D95N': 2, 'D95E': 2},
            '11327835': {'H64A': 4}}

       
    """
    result = {}
    for line in extraction_data:
        fields = line.strip().split('\t')
        current_mutations = {}
        for mutation_entry in fields[1:]:
            try:
                # If spans are present, strip them out
                mutation = PointMutation_from_wNm(\
                    mutation_entry[:mutation_entry.index(':')])
            except ValueError:
                mutation = PointMutation_from_wNm(mutation_entry)
            try:
                current_mutations[mutation] += 1
            except KeyError:
                current_mutations[mutation] = 1
        result[fields[0]] = current_mutations
    return result

def parse_command_line_parameters():
    """ Parses command line arguments """
    usage = 'usage: %prog [options] EXTRACTOR_OUTPUT_FILE GOLD_STANDARD_FILE"'
    version = ' '.join(['Version: %prog',version_number])
    parser = OptionParser(usage=usage, version=version)

    # A binary 'verbose' flag
    parser.add_option('-v','--verbose',action='store_true',\
        dest='verbose',help='Print information during execution -- '+\
        'useful for debugging [default: %default]')

    parser.set_defaults(verbose=False)

    opts,args = parser.parse_args()
    min_args = 2
    if len(args) < min_args:
        parser.error('At least two arguments are required')

    return opts,args

if __name__ == "__main__":
    opts,args = parse_command_line_parameters()
    verbose = opts.verbose

    # Open the system output file (extractor_output results)
    try:
        extractor_output = parse_extraction_data(list(open(args[0])))
    except IOError:
        print "**ERROR: Couldn't open extractor output file", args[0]
        exit(-1)

    # Open the gold standard file
    try: 
        gold_standard = parse_extraction_data(list(open(args[1])))
    except IOError:
        print "**ERROR: Couldn't open gold standard file", args[1]
        exit(-1)

    # Construct the performance calculator providing the gold standard
    performance_calculator = PerformanceCalculator(gold_standard)

    # Judge the extractor_output compared to the gold_standard results 
    # on three different measures of precision and recall
    print "-----------------------------------"
    print "Extracted Mentions"
    print "-----------------------------------"
    print str(performance_calculator.\
            calculate_extracted_mentions(extractor_output))
    print "-----------------------------------"
    print "Normalized Mutations"
    print "-----------------------------------"
    print str(performance_calculator.\
            calculate_normalized_mutations(extractor_output))
    print "-----------------------------------"
    print "Document Retrieval"
    print "-----------------------------------"
    print str(performance_calculator.\
            calculate_document_retrieval(extractor_output))
    print "-----------------------------------"



