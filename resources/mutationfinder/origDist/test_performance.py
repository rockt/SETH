#!/usr/bin/env python
# Author: Greg Caporaso (gregcaporaso@gmail.com)
# test_performance.py

""" Description
File created on 07 Feb 2007.

This file contains tests of the classes and functions in the 
MutationFinder performance.py script.

Copyright (c) 2007 Regents of the University of Colorado
Please refer to licensing agreement at MUTATIONFINDER_HOME/doc/license.txt
"""
from unittest import TestCase, main
from performance import PerformanceCalculatorResult,PerformanceCalculator,\
    PerformanceCalculatorError,parse_extraction_data
from mutation_finder import PointMutation

class PerformanceCalculatorResultTests(TestCase):
    """ Tests of the PerformanceCalculatorResult object 

        Note: I like to use abbreviation of the class names to make 
            the makes more clear -- in this case that abbreviation is
            PCR, which is a litle weird since that's a very common 
            acronym. Just to make it clear, this reference is to
            PerformanceCalculatorResult, not Polymerase Chain Reaction.

    """

    def setUp(self):
        """ Initialize data for tests """
        self._pcr = PerformanceCalculatorResult(42,1,0,5)

    def test_init_all_ints(self):
        """PCR: init functions as expected with valid & complete input """
        pcr = PerformanceCalculatorResult(42,1,0,5)
        self.assertEqual(pcr.TruePositive,42)
        self.assertEqual(pcr.FalsePositive,1)
        self.assertEqual(pcr.FalseNegative,0)
        self.assertEqual(pcr.TrueNegative,5)

        self.assertAlmostEqual(pcr.Precision, 0.9767,3)
        self.assertAlmostEqual(pcr.Recall,1.0,3)
        self.assertAlmostEqual(pcr.FMeasure,0.9882,3)
        
    def test_init_missing_TN(self):
        """PCR: init functions with missing or non-int TrueNeagtive """
        # TN = n/a 
        pcr = PerformanceCalculatorResult(42,1,0,'n/a')
        self.assertEqual(pcr.TruePositive,42)
        self.assertEqual(pcr.FalsePositive,1)
        self.assertEqual(pcr.FalseNegative,0)
        self.assertEqual(pcr.TrueNegative,None)

        self.assertAlmostEqual(pcr.Precision, 0.9767,3)
        self.assertAlmostEqual(pcr.Recall,1.0,3)
        self.assertAlmostEqual(pcr.FMeasure,0.9882,3)
        # TN = None
        pcr = PerformanceCalculatorResult(42,1,0,None)
        pcr = PerformanceCalculatorResult(42,1,0)

    def test_calculate_precision(self):
        """PCR: precision calculations function as expected """
        # Max and min tests
        pcr = PerformanceCalculatorResult(50,0,0,None)
        self.assertAlmostEqual(pcr._calculate_precision(),1.000,3)
        pcr = PerformanceCalculatorResult(0,50,0,None)
        self.assertAlmostEqual(pcr._calculate_precision(),0.000,3)
        # Intermediate tests
        pcr = PerformanceCalculatorResult(42,1,0,None)
        self.assertAlmostEqual(pcr._calculate_precision(),0.9767,3)
        pcr = PerformanceCalculatorResult(1,42,0,None)
        self.assertAlmostEqual(pcr._calculate_precision(),0.0232,3)
        pcr = PerformanceCalculatorResult(42,42,0,None)
        self.assertAlmostEqual(pcr._calculate_precision(),0.500,3)
        pcr = PerformanceCalculatorResult(42,126,0,None)
        self.assertAlmostEqual(pcr._calculate_precision(),0.250,3)
        pcr = PerformanceCalculatorResult(126,42,0,None)
        self.assertAlmostEqual(pcr._calculate_precision(),0.750,3)
        # FN/TN has no effect
        pcr = PerformanceCalculatorResult(126,42,20,None)
        self.assertAlmostEqual(pcr._calculate_precision(),0.750,3)
        pcr = PerformanceCalculatorResult(126,42,20,50)
        self.assertAlmostEqual(pcr._calculate_precision(),0.750,3)

    def test_calculate_recall(self):
        """PCR: recall calculations function as expected """
        # Max and min tests
        pcr = PerformanceCalculatorResult(50,0,0,None)
        self.assertAlmostEqual(pcr._calculate_recall(),1.000,3)
        pcr = PerformanceCalculatorResult(0,0,50,None)
        self.assertAlmostEqual(pcr._calculate_recall(),0.000,3)
        # Intermediate tests
        pcr = PerformanceCalculatorResult(42,0,1,None)
        self.assertAlmostEqual(pcr._calculate_recall(),0.9767,3)
        pcr = PerformanceCalculatorResult(1,0,42,None)
        self.assertAlmostEqual(pcr._calculate_recall(),0.0232,3)
        pcr = PerformanceCalculatorResult(42,0,42,None)
        self.assertAlmostEqual(pcr._calculate_recall(),0.500,3)
        pcr = PerformanceCalculatorResult(42,0,126,None)
        self.assertAlmostEqual(pcr._calculate_recall(),0.250,3)
        pcr = PerformanceCalculatorResult(126,0,42,None)
        self.assertAlmostEqual(pcr._calculate_recall(),0.750,3)
        # FP/TN have no effect
        pcr = PerformanceCalculatorResult(126,20,42,None)
        self.assertAlmostEqual(pcr._calculate_recall(),0.750,3)
        pcr = PerformanceCalculatorResult(126,20,42,50)
        self.assertAlmostEqual(pcr._calculate_recall(),0.750,3)

    def test_f_measure(self):
        """PCR: f-measure calculations function as expected """
        # Max and min tests
        pcr = PerformanceCalculatorResult(50,0,0,None)
        self.assertAlmostEqual(pcr._calculate_f_measure(),1.000,3)
        pcr = PerformanceCalculatorResult(0,50,0,None)
        self.assertEqual(pcr._calculate_f_measure(),None)
        pcr = PerformanceCalculatorResult(0,0,50,None)
        self.assertEqual(pcr._calculate_f_measure(),None)
        

    def test_calculation_divide_by_zeros(self):
        """PCR: P/R/F calculations correctly handle divide-by-zero errors"""
        # Divide by zero handled correctly
        pcr = PerformanceCalculatorResult(0,0,0,None)
        self.assertEqual(pcr._calculate_precision(),None)
        self.assertEqual(pcr._calculate_recall(),None)
        self.assertEqual(pcr._calculate_f_measure(),None)
        
    def test_pretty_precision_recall_fmeasure(self):
        """PCR: PRF formatting functions as expected """
        expected = 'Precision\tRecall\tF-measure\n0.9767\t1.0000\t0.9882'
        self.assertEqual(self._pcr.pretty_precision_recall_fmeasure(),expected)

    def test_pretty_confusion_matrix(self):
        """PCR: Confusion matrix formatting functions as expected """
        expected = 'tp\tfn\nfp\ttn\n--\n42\t0\n1\t5'
        self.assertEqual(self._pcr.pretty_confusion_matrix(),expected) 

    def test_str(self):
        """PCR: str() functions as expected """
        expected = '\n'.join([\
           'tp\tfn\nfp\ttn\n--\n42\t0\n1\t5',\
           'Precision\tRecall\tF-measure\n0.9767\t1.0000\t0.9882'])
        self.assertEqual(str(self._pcr),expected)
       
class PerformanceCalculatorTests(TestCase):
    """ Tests of the PerformanceCalculator class """

    def setUp(self):
        """ Set up variables for the tests""" 
        self._gold_standard_data = gold_standard_data
        self._blank_mutation_data =\
            {'3476160':{},'14500716':{},'12206666':{},'11327835':{}} 
        self._pc = PerformanceCalculator(self._gold_standard_data)

    def test_init(self):
        """PC: init functions as expected """
        # instantiate the class
        pc = PerformanceCalculator(self._gold_standard_data)
        # Check one of the values
        self.assertEqual(\
            pc._gold_standard['12206666'][PointMutation(95,'D','N')],2)

    def test_extracted_mentions_blank_input(self):
        """PC: Extracted Mentions functions with no extracted mutations
        """
        # Functions with no mutations
        perf = self._pc.calculate_extracted_mentions(\
            self._blank_mutation_data)
        self.assertEqual(perf.TruePositive,0)
        self.assertEqual(perf.FalsePositive,0)
        self.assertEqual(perf.FalseNegative,14)
        self.assertEqual(perf.TrueNegative,None)

    def test_extracted_mentions_blank_gs(self):
        """PC: Extracted Mentions functions with no gold standard mutations
        """
        # Functions with an empty gold standard dict
        pc = PerformanceCalculator(self._blank_mutation_data)
        perf = pc.calculate_extracted_mentions(\
            self._gold_standard_data)
        self.assertEqual(perf.TruePositive,0)
        self.assertEqual(perf.FalsePositive,14)
        self.assertEqual(perf.FalseNegative,0)
        self.assertEqual(perf.TrueNegative,None)
 
    def test_extracted_mentions_perfect_input(self):
        """PC: Extracted Mentions functions with perfect extracted mutations
        """
        # Functions with perfect input (i.e. equal to the gold standard)
        perf = self._pc.calculate_extracted_mentions(\
            self._gold_standard_data)
        self.assertEqual(perf.TruePositive,14)
        self.assertEqual(perf.FalsePositive,0)
        self.assertEqual(perf.FalseNegative,0)
        self.assertEqual(perf.TrueNegative,None)
  
    def test_extracted_mentions_invalid_input(self):
        """PC: Extracted Mentions handles invalid input correctly
        """
        # Handles gold-standard and extractor output which does
        # not completely overlap correctly -- this is explictly 
        # not allowed because it is not clear how it should be
        # handled
        pc = PerformanceCalculator({'1':{},'2':{}})
        self.assertRaises(PerformanceCalculatorError,\
            pc.calculate_extracted_mentions,{'1':{},'3':{}})
        self.assertRaises(PerformanceCalculatorError,\
            pc.calculate_extracted_mentions,{'1':{}})
        self.assertRaises(PerformanceCalculatorError,\
            pc.calculate_extracted_mentions,{'1':{},'2':{},'3':{}})
 

    def test_extracted_mentions_varied_input(self):
        """PC: Extracted Mentions functions with varied extracted mutations
        """
        # 
        perf = self._pc.calculate_extracted_mentions(\
            {'3476160':{PointMutation(22,'T','C'):1},\
             '14500716':{},'12206666':{},'11327835':{}})
        self.assertEqual(perf.TruePositive,1)
        self.assertEqual(perf.FalsePositive,0)
        self.assertEqual(perf.FalseNegative,13)
        self.assertEqual(perf.TrueNegative,None)
        # An extra count results in a false positive
        perf = self._pc.calculate_extracted_mentions(\
            {'3476160':{PointMutation(22,'T','C'):2},\
             '14500716':{},'12206666':{},'11327835':{}})
        self.assertEqual(perf.TruePositive,1)
        self.assertEqual(perf.FalsePositive,1)
        self.assertEqual(perf.FalseNegative,13)
        self.assertEqual(perf.TrueNegative,None)
        # ...and two extra counts results in two false positives
        perf = self._pc.calculate_extracted_mentions(\
            {'3476160':{PointMutation(22,'T','C'):3},\
             '14500716':{},'12206666':{},'11327835':{}})
        self.assertEqual(perf.TruePositive,1)
        self.assertEqual(perf.FalsePositive,2)
        self.assertEqual(perf.FalseNegative,13)
        self.assertEqual(perf.TrueNegative,None)
        
        # One missing count results in one false negative (see 11327835)
        # and one less true positive
        perf = self._pc.calculate_extracted_mentions(\
             {'3476160': {PointMutation(87,'S','C'): 1,\
                        PointMutation(22,'T','C'): 1},\
             '14500716': {},\
             '12206666': {PointMutation(95,'D','A'): 4,\
                         PointMutation(95,'D','N'): 2,\
                         PointMutation(95,'D','E'): 2},\
             '11327835': {PointMutation(64,'H','A'): 3}})
        self.assertEqual(perf.TruePositive,13)
        self.assertEqual(perf.FalsePositive,0)
        self.assertEqual(perf.FalseNegative,1)
        self.assertEqual(perf.TrueNegative,None)
        # Two missing counts results in two false negatives (see 11327835)
        # and two less true positives
        perf = self._pc.calculate_extracted_mentions(\
            {'3476160': {PointMutation(87,'S','C'): 1,\
                        PointMutation(22,'T','C'): 1},\
             '14500716': {},\
             '12206666': {PointMutation(95,'D','A'): 4,\
                         PointMutation(95,'D','N'): 2,\
                         PointMutation(95,'D','E'): 2},\
             '11327835': {PointMutation(64,'H','A'): 2}})
        self.assertEqual(perf.TruePositive,12)
        self.assertEqual(perf.FalsePositive,0)
        self.assertEqual(perf.FalseNegative,2)
        self.assertEqual(perf.TrueNegative,None)

        # One extra mutation tallied as one FalsePositive 
        perf = self._pc.calculate_extracted_mentions(\
            {'3476160':{PointMutation(42,'L','Y'):1},\
             '14500716':{},'12206666':{},'11327835':{}})
        self.assertEqual(perf.TruePositive,0)
        self.assertEqual(perf.FalsePositive,1)
        self.assertEqual(perf.FalseNegative,14)
        self.assertEqual(perf.TrueNegative,None)
        # Two extra mutations tallied as two FalsePositives 
        perf = self._pc.calculate_extracted_mentions(\
            {'3476160':{PointMutation(42,'L','Y'):2},\
             '14500716':{},'12206666':{},'11327835':{}})
        self.assertEqual(perf.TruePositive,0)
        self.assertEqual(perf.FalsePositive,2)
        self.assertEqual(perf.FalseNegative,14)
        self.assertEqual(perf.TrueNegative,None)
        perf = self._pc.calculate_extracted_mentions(\
            {'3476160':{PointMutation(42,'L','Y'):1},\
             '14500716':{PointMutation(33,'P','T'):1},\
             '12206666':{},'11327835':{}})
        self.assertEqual(perf.TruePositive,0)
        self.assertEqual(perf.FalsePositive,2)
        self.assertEqual(perf.FalseNegative,14)
        self.assertEqual(perf.TrueNegative,None)

    def test_document_retrieval_blank_input(self):
        """PC: Document Retrieval functions with no extracted mutations
        """
        # Functions with no extracted mutations
        perf = self._pc.calculate_document_retrieval(\
            self._blank_mutation_data)
        self.assertEqual(perf.TruePositive,0)
        self.assertEqual(perf.FalsePositive,0)
        self.assertEqual(perf.FalseNegative,3)
        self.assertEqual(perf.TrueNegative,1)

    def test_document_retrieval_blank_gs(self):
        """PC: Document Retrieval functions with no gold standard mutations
        """
        # Functions with an empty gold standard dict
        pc = PerformanceCalculator(self._blank_mutation_data)
        perf = pc.calculate_document_retrieval(\
            self._gold_standard_data)
        self.assertEqual(perf.TruePositive,0)
        self.assertEqual(perf.FalsePositive,3)
        self.assertEqual(perf.FalseNegative,0)
        self.assertEqual(perf.TrueNegative,1)
 
    def test_document_retrieval_perfect_input(self):
        """PC: Document Retrieval functions with perfect extracted mutations
        """
        # Functions with perfect input (i.e. equal to the gold standard)
        perf = self._pc.calculate_document_retrieval(\
            self._gold_standard_data)
        self.assertEqual(perf.TruePositive,3)
        self.assertEqual(perf.FalsePositive,0)
        self.assertEqual(perf.FalseNegative,0)
        self.assertEqual(perf.TrueNegative,1)
  
    def test_document_retrieval_invalid_input(self):
        """PC: Document Retrieval handles invalid input correctly
        """
        # Handles gold-standard and extractor output which does
        # not completely overlap correctly -- this is explictly 
        # not allowed because it is not clear how it should be
        # handled
        pc = PerformanceCalculator({'1':{},'2':{}})
        self.assertRaises(PerformanceCalculatorError,\
            pc.calculate_document_retrieval,{'1':{},'3':{}})
        self.assertRaises(PerformanceCalculatorError,\
            pc.calculate_document_retrieval,{'1':{}})
        self.assertRaises(PerformanceCalculatorError,\
            pc.calculate_document_retrieval,{'1':{},'2':{},'3':{}})
 
    def test_document_retrieval_varied_input(self):
        """PC: Document Retrieval functions with varied extracted mutations
        """
        # 
        perf = self._pc.calculate_document_retrieval(\
            {'3476160':{PointMutation(22,'T','C'):1},\
             '14500716':{},'12206666':{},'11327835':{}})
        self.assertEqual(perf.TruePositive,1)
        self.assertEqual(perf.FalsePositive,0)
        self.assertEqual(perf.FalseNegative,2)
        self.assertEqual(perf.TrueNegative,1)
        # An extra count has no effect
        perf = self._pc.calculate_document_retrieval(\
            {'3476160':{PointMutation(22,'T','C'):2},\
             '14500716':{},'12206666':{},'11327835':{}})
        self.assertEqual(perf.TruePositive,1)
        self.assertEqual(perf.FalsePositive,0)
        self.assertEqual(perf.FalseNegative,2)
        self.assertEqual(perf.TrueNegative,1)
        # ...and two extra counts has no effect
        perf = self._pc.calculate_document_retrieval(\
            {'3476160':{PointMutation(22,'T','C'):3},\
             '14500716':{},'12206666':{},'11327835':{}})
        self.assertEqual(perf.TruePositive,1)
        self.assertEqual(perf.FalsePositive,0)
        self.assertEqual(perf.FalseNegative,2)
        self.assertEqual(perf.TrueNegative,1)
        
        # One missing count has no effect
        perf = self._pc.calculate_document_retrieval(\
            {'3476160': {PointMutation(87,'S','C'): 1,\
                        PointMutation(22,'T','C'): 1},\
             '14500716': {},\
             '12206666': {PointMutation(95,'D','A'): 4,\
                         PointMutation(95,'D','N'): 2,\
                         PointMutation(95,'D','E'): 2},\
             '11327835': {PointMutation(64,'H','A'): 3}})
        self.assertEqual(perf.TruePositive,3)
        self.assertEqual(perf.FalsePositive,0)
        self.assertEqual(perf.FalseNegative,0)
        self.assertEqual(perf.TrueNegative,1)
        # Two missing counts has no effect
        perf = self._pc.calculate_document_retrieval(\
            {'3476160': {PointMutation(87,'S','C'): 1,\
                        PointMutation(22,'T','C'): 1},\
             '14500716': {},\
             '12206666': {PointMutation(95,'D','A'): 4,\
                         PointMutation(95,'D','N'): 2,\
                         PointMutation(95,'D','E'): 2},\
             '11327835': {PointMutation(64,'H','A'): 2}})
        self.assertEqual(perf.TruePositive,3)
        self.assertEqual(perf.FalsePositive,0)
        self.assertEqual(perf.FalseNegative,0)
        self.assertEqual(perf.TrueNegative,1)

        # Incorrect mutations still counts as a TP document 
        perf = self._pc.calculate_document_retrieval(\
            {'3476160':{PointMutation(42,'L','Y'):1},\
             '14500716':{},'12206666':{},'11327835':{}})
        self.assertEqual(perf.TruePositive,1)
        self.assertEqual(perf.FalsePositive,0)
        self.assertEqual(perf.FalseNegative,2)
        self.assertEqual(perf.TrueNegative,1)
        # False Positive/TrueNegative tallied correctly 
        perf = self._pc.calculate_document_retrieval(\
            {'3476160':{},'14500716':{PointMutation(42,'L','Y'):1},\
             '12206666':{},'11327835':{}})
        self.assertEqual(perf.TruePositive,0)
        self.assertEqual(perf.FalsePositive,1)
        self.assertEqual(perf.FalseNegative,3)
        self.assertEqual(perf.TrueNegative,0)


    def test_normalized_mutations_blank_input(self):
        """PC: Normalized Mutations functions with no extracted mutations
        """
        # Functions with no extracted mutations
        perf = self._pc.calculate_normalized_mutations(\
            self._blank_mutation_data)
        self.assertEqual(perf.TruePositive,0)
        self.assertEqual(perf.FalsePositive,0)
        self.assertEqual(perf.FalseNegative,6)
        self.assertEqual(perf.TrueNegative,None)

    def test_normalized_mutations_blank_gs(self):
        """PC: Normalized Mutations functions with no gold standard mutations
        """
        # Functions with an empty gold standard dict
        pc = PerformanceCalculator(self._blank_mutation_data)
        perf = pc.calculate_normalized_mutations(\
            self._gold_standard_data)
        self.assertEqual(perf.TruePositive,0)
        self.assertEqual(perf.FalsePositive,6)
        self.assertEqual(perf.FalseNegative,0)
        self.assertEqual(perf.TrueNegative,None)
 
    def test_normalized_mutations_perfect_input(self):
        """PC: Normalized Mutations functions with perfect extracted mutations
        """
        # Functions with perfect input (i.e. equal to the gold standard)
        perf = self._pc.calculate_normalized_mutations(\
            self._gold_standard_data)
        self.assertEqual(perf.TruePositive,6)
        self.assertEqual(perf.FalsePositive,0)
        self.assertEqual(perf.FalseNegative,0)
        self.assertEqual(perf.TrueNegative,None)
  
    def test_normalized_mutations_invalid_input(self):
        """PC: Normalized Mutations handles invalid input correctly
        """
        # Handles gold-standard and extractor output which does
        # not completely overlap correctly -- this is explictly 
        # not allowed because it is not clear how it should be
        # handled
        pc = PerformanceCalculator({'1':{},'2':{}})
        self.assertRaises(PerformanceCalculatorError,\
            pc.calculate_normalized_mutations,{'1':{},'3':{}})
        self.assertRaises(PerformanceCalculatorError,\
            pc.calculate_normalized_mutations,{'1':{}})
        self.assertRaises(PerformanceCalculatorError,\
            pc.calculate_normalized_mutations,{'1':{},'2':{},'3':{}})
 

    def test_normalized_mutations_varied_input(self):
        """PC: Normalized Mutations functions with varied extracted mutations
        """
        # 
        perf = self._pc.calculate_normalized_mutations(\
            {'3476160':{PointMutation(22,'T','C'):1},\
             '14500716':{},'12206666':{},'11327835':{}})
        self.assertEqual(perf.TruePositive,1)
        self.assertEqual(perf.FalsePositive,0)
        self.assertEqual(perf.FalseNegative,5)
        self.assertEqual(perf.TrueNegative,None)
        # An extra count has no effect
        perf = self._pc.calculate_normalized_mutations(\
            {'3476160':{PointMutation(22,'T','C'):2},\
             '14500716':{},'12206666':{},'11327835':{}})
        self.assertEqual(perf.TruePositive,1)
        self.assertEqual(perf.FalsePositive,0)
        self.assertEqual(perf.FalseNegative,5)
        self.assertEqual(perf.TrueNegative,None)
        # ...and two extra counts have no effect
        perf = self._pc.calculate_normalized_mutations(\
            {'3476160':{PointMutation(22,'T','C'):3},\
             '14500716':{},'12206666':{},'11327835':{}})
        self.assertEqual(perf.TruePositive,1)
        self.assertEqual(perf.FalsePositive,0)
        self.assertEqual(perf.FalseNegative,5)
        self.assertEqual(perf.TrueNegative,None)
        
        # One missing count has no effect
        perf = self._pc.calculate_normalized_mutations(\
            {'3476160': {PointMutation(87,'S','C'): 1,\
                        PointMutation(22,'T','C'): 1},\
             '14500716': {},\
             '12206666': {PointMutation(95,'D','A'): 4,\
                         PointMutation(95,'D','N'): 2,\
                         PointMutation(95,'D','E'): 2},\
             '11327835': {PointMutation(64,'H','A'): 3}})
        self.assertEqual(perf.TruePositive,6)
        self.assertEqual(perf.FalsePositive,0)
        self.assertEqual(perf.FalseNegative,0)
        self.assertEqual(perf.TrueNegative,None)
        # Two missing counts have no effect
        perf = self._pc.calculate_normalized_mutations(\
            {'3476160': {PointMutation(87,'S','C'): 1,\
                        PointMutation(22,'T','C'): 1},\
             '14500716': {},\
             '12206666': {PointMutation(95,'D','A'): 4,\
                         PointMutation(95,'D','N'): 2,\
                         PointMutation(95,'D','E'): 2},\
             '11327835': {PointMutation(64,'H','A'): 2}})
        self.assertEqual(perf.TruePositive,6)
        self.assertEqual(perf.FalsePositive,0)
        self.assertEqual(perf.FalseNegative,0)
        self.assertEqual(perf.TrueNegative,None)

        # One extra mutation tallied as one FalsePositive 
        perf = self._pc.calculate_normalized_mutations(\
            {'3476160':{PointMutation(42,'L','Y'):1},\
             '14500716':{},'12206666':{},'11327835':{}})
        self.assertEqual(perf.TruePositive,0)
        self.assertEqual(perf.FalsePositive,1)
        self.assertEqual(perf.FalseNegative,6)
        self.assertEqual(perf.TrueNegative,None)
        # Two counts of one extra mutation tallied as one FalsePositive 
        perf = self._pc.calculate_normalized_mutations(\
            {'3476160':{PointMutation(42,'L','Y'):2},\
             '14500716':{},'12206666':{},'11327835':{}})
        self.assertEqual(perf.TruePositive,0)
        self.assertEqual(perf.FalsePositive,1)
        self.assertEqual(perf.FalseNegative,6)
        self.assertEqual(perf.TrueNegative,None)
        # Two extra mutations tallied as two FalsePositives 
        perf = self._pc.calculate_normalized_mutations(\
            {'3476160':{PointMutation(42,'L','Y'):1},\
             '14500716':{PointMutation(33,'P','T'):1},\
             '12206666':{},'11327835':{}})
        self.assertEqual(perf.TruePositive,0)
        self.assertEqual(perf.FalsePositive,2)
        self.assertEqual(perf.FalseNegative,6)
        self.assertEqual(perf.TrueNegative,None)

class ScriptTests(TestCase):
    """ Tests of the script functionality """
    
    def setUp(self):
        """ Set up variables for the tests """

        self._fake_gold_standard_file = fake_gold_standard_file.split('\n')
        self._fake_gold_standard_file_w_spans = \
            fake_gold_standard_file_w_spans.split('\n')
        self._gold_standard_data = gold_standard_data

    def test_parse_extraction_data(self):
        """ parse_extraction_data correctly parses input file format 
        """
        self.assertEqual(parse_extraction_data(self._fake_gold_standard_file),\
            self._gold_standard_data)
        # Spans handled correctly
        self.assertEqual(parse_extraction_data(\
            self._fake_gold_standard_file_w_spans),self._gold_standard_data)

    

if __name__ == "__main__":

    fake_gold_standard_file = """3476160\tT22C\tS87C
        14500716
        12206666\tD95A\tD95A\tD95A\tD95E\tD95E\tD95A\tD95N\tD95N
        11327835\tH64A\tH64A\tH64A\tH64A"""

    fake_gold_standard_file_w_spans = \
        """3476160\tT22C:0,4\tS87C:9,15
        14500716
        12206666\tD95A:4,12\tD95A:22,33\tD95A:44,55\tD95E:55,66\tD95E:0,4\tD95A:99,100\tD95N:1000,1006\tD95N:99,104
        11327835\tH64A:66,72\tH64A:42,45\tH64A:6,9999\tH64A:88,99"""


    gold_standard_data = {\
            '3476160': {PointMutation(87,'S','C'): 1,\
                        PointMutation(22,'T','C'): 1},\
            '14500716': {},\
            '12206666': {PointMutation(95,'D','A'): 4,\
                         PointMutation(95,'D','N'): 2,\
                         PointMutation(95,'D','E'): 2},\
            '11327835': {PointMutation(64,'H','A'): 4}\
            }

    main()
