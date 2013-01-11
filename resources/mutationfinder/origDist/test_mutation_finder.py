#!/usr/bin/env python
# test_Mutation.py
# Greg Caporaso gregcaporaso@gmail.com

"""
Copyright (c) 2007 Regents of the University of Colorado
Please refer to licensing agreement at MUTATIONFINDER_HOME/doc/license.txt
"""

from unittest import TestCase, main
from mutation_finder import Mutation, PointMutation, MutationError,\
    PointMutation_from_wNm, BaselineMutationExtractor, MutationFinder,\
    amino_acid_three_to_one_letter_map, extract_mutations_from_string,\
    extract_mutations_from_lines, extract_mutations_from_lines_to_file,\
    filename_from_filepath, build_output_filepath, MutationFinderError,\
    extract_mutations_from_lines_to_dict

class MutationTest(TestCase):
    
    def setUp(self):
        pass

    def test_init(self):
        """Mutation: __init__ functions as expected """
        m = Mutation(42)
        self.assertEqual(m.Position,42)
        m = Mutation('42')
        self.assertEqual(m.Position,42)

    def test_location_property(self):
        """Mutation: Position property functions as expected """
        m = Mutation(42)
        self.assertEqual(m.Position,42)

    def test_abstract_methods(self):
        """Mutation: abstract methods raise error when called"""
        m1 = Mutation('42')
        
        try:
            str(m1)
            raise AssertionError, 'Direct call of str() did not raise error'
        except NotImplementedError:
            pass
 
        try:
            m1 == m1
            raise AssertionError, 'Direct call of == did not raise error'
        except NotImplementedError:
            pass
 
        try:
            m1 != m1
            raise AssertionError, 'Direct call of != did not raise error'
        except NotImplementedError:
            pass
        try:
            hash(m1)
            raise AssertionError, 'Direct call of hash did not raise error'
        except NotImplementedError:
            pass


class PointMutationTests(TestCase):
    
    def setUp(self):

        self.mutation = PointMutation(42,'W','G')
        self.amino_acid_codes = dict([('GLY','G'),('ALA','A'),('LEU','L'),\
            ('MET','M'),('PHE','F'),('TRP','W'),('LYS','K'),('GLN','Q'),\
            ('GLU','E'),('SER','S'),('PRO','P'),('VAL','V'),('ILE','I'),\
            ('CYS','C'),('TYR','Y'),('HIS','H'),('ARG','R'),('ASN','N'),\
            ('ASP','D'),('THR','T'),('GLYCINE','G'),('ALANINE','A'),\
            ('XAA','X'),('GLX','Z'),('ASX','B'),\
            ('LEUCINE','L'),('METHIONINE','M'),('PHENYLALANINE','F'),\
            ('VALINE','V'),('ISOLEUCINE','I'),('TYROSINE','Y'),\
            ('TRYPTOPHAN','W'),('SERINE','S'),('PROLINE','P'),\
            ('THREONINE','T'),('CYSTEINE','C'),('ASPARAGINE','N'),\
            ('GLUTAMINE','Q'),('LYSINE','K'),('HISTIDINE','H'),\
            ('ARGININE','R'),('ASPARTATE','D'),('GLUTAMATE','E'),\
            ('ASPARTIC ACID','D'),('GLUTAMIC ACID','E'),\
            ('G','G'),('A','A'),('V','V'),('L','L'),('I','I'),('M','M'),\
            ('F','F'),('Y','Y'),('W','W'),('S','S'),('P','P'),('T','T'),\
            ('C','C'),('N','N'),('Q','Q'),('K','K'),('H','H'),('R','R'),\
            ('D','D'),('E','E')]) 

    def test_valid_init(self):
        """PointMutation: __init__ functions as expected (valid data)"""
        m = PointMutation(42,'A','C')
        self.assertEqual(m.Position,42)
        self.assertEqual(m.WtResidue,'A')
        self.assertEqual(m.MutResidue,'C') 
        
        m = PointMutation(42,'Ala','Cys')
        self.assertEqual(m.Position,42)
        self.assertEqual(m.WtResidue,'A')
        self.assertEqual(m.MutResidue,'C') 
        
        m = PointMutation(42,'ALA','CYS')
        self.assertEqual(m.Position,42)
        self.assertEqual(m.WtResidue,'A')
        self.assertEqual(m.MutResidue,'C') 
        
        m = PointMutation(42,'A','Cys')
        self.assertEqual(m.Position,42)
        self.assertEqual(m.WtResidue,'A')
        self.assertEqual(m.MutResidue,'C') 
        
        m = PointMutation('42','A','C')
        self.assertEqual(m.Position,42)
        self.assertEqual(m.WtResidue,'A')
        self.assertEqual(m.MutResidue,'C') 

    def test_hash(self):
        """PointMutation: hash functions as expected """
        self.assertEqual(hash(self.mutation), \
            hash(str(type(self.mutation)) + 'W42G'))

    def test_invalid_init(self):
        """PointMutation: __init__ functions as expected (invalid data)"""
        self.assertRaises(MutationError,PointMutation,'hello','A','C')
        self.assertRaises(MutationError,PointMutation,42,'O','C')
        self.assertRaises(MutationError,PointMutation,0,'A','C')
        self.assertRaises(MutationError,PointMutation,-42,'A','C')
        self.assertRaises(MutationError,PointMutation,42,'A','O')
        self.assertRaises(MutationError,PointMutation,42,'A','5')

    def test_str(self):
        """PointMutation: __str__ functions as expected """
        self.assertEqual(str(self.mutation),'W42G')

    def test_eq(self):
        """PointMutation: == functions as expected """
        self.assertEqual(self.mutation == PointMutation(42,'W','G'), True)
        self.assertEqual(self.mutation == PointMutation('42','W','G'), True)
        self.assertEqual(self.mutation == PointMutation(41,'W','G'), False)
        self.assertEqual(self.mutation == PointMutation(42,'Y','G'), False)
        self.assertEqual(self.mutation == PointMutation(42,'W','C'), False)
        
    def test_ne(self):
        """PointMutation: != functions as expected """
        self.assertEqual(self.mutation != PointMutation(42,'W','G'), False)
        self.assertEqual(self.mutation != PointMutation('42','W','G'), False)
        self.assertEqual(self.mutation != PointMutation(41,'W','G'), True)
        self.assertEqual(self.mutation != PointMutation(42,'Y','G'), True)
        self.assertEqual(self.mutation != PointMutation(42,'W','C'), True)

    def test_normalize_residue_identity(self):
        """PointMutation: normalize functions with valid input
        """ 
        # test normalizations from one-letter, three-letter, and full name
        # amino acid mentions to their one-letter codes
        # NOTE: the abbreviated and unabbreviated forms were manually entered
        # twice -- once in the mutation_finder.py file, and once in this
        # test file -- to test for errors which may have occurred during data
        # entry
        for test_input,expected_output in self.amino_acid_codes.items():
            self.assertEqual(self.mutation._normalize_residue_identity(\
                test_input),expected_output)
            # check for case-insensitivity
            self.assertEqual(self.mutation._normalize_residue_identity(\
                test_input.lower()),expected_output)

    def test_normalize_residue_identity_error_handling(self):
        """PointMutation: normalize functions with invalid input """
        # Try some bad values
        for test_input in ['','O','xxala','alaxx','asdasd','42']:
            self.assertRaises(MutationError,\
              self.mutation._normalize_residue_identity,test_input)
        # Try some bad types
        for test_input in [{},[],42,0.42]:
            self.assertRaises(MutationError,\
              self.mutation._normalize_residue_identity,test_input)

class BaselineMutationExtractorTests(TestCase):
    """ Tests of the BaselineMutationExtractor class """

    _single_letter_aa_codes = [aa[0] for aa in amino_acid_three_to_one_letter_map]
    _triple_letter_aa_codes = [aa[1] for aa in amino_acid_three_to_one_letter_map]

    def setUp(self):
        """ Initalize some objects for use in the tests """
        self.me = BaselineMutationExtractor()

    def test_init(self):
        """BME: __init__ returns without error """
        me = BaselineMutationExtractor()

    def test_call_no_mutations(self):
        """BME: extraction functions with no extraction-worthy data """
        self.assertEqual(self.me(''),{})
        self.assertEqual(self.me('There is no mutation data here.'),{})
        self.assertEqual(self.me('T64 is almost a valid mutation.'),{})
        self.assertEqual(self.me('So is 42S.'),{})
        
    def test_call_single_mutation(self):
        """BME:extraction functions when one mutation is present """
        expected = {PointMutation(42,'S','T'):1}
        self.assertEqual(self.me('S42T'),expected)
        self.assertEqual(self.me('The S42T mutation was made.'),expected)
        
    def test_call_boundaries_required(self):
        """BME:match boundaries are recognized """
        expected = {PointMutation(42,'S','T'):1}
        self.assertEqual(self.me('S42T'),expected)
        self.assertEqual(self.me('S42Test'),{})
        self.assertEqual(self.me('S42-Test mutation was made.'),{})
        self.assertEqual(self.me('gfS42T'),{})
        self.assertEqual(self.me('S42Thr'),{})
        
    def test_call_punc_ignored(self):
        """BME:puncuation ignored in mutation words """
        expected = {PointMutation(42,'S','T'):1}
        # internal punctuation
        self.assertEqual(self.me('S42-T'),expected)
        # leading punctuation
        self.assertEqual(self.me('?S42T'),expected)
        # training punctuation
        self.assertEqual(self.me('S42T?'),expected)
        # all punctuation marks
        self.assertEqual(self.me('!@#$%^&*()~`"\';:.,><?/{}[]\|+=-_S42T'),\
            expected)

    def test_call_multiple_mutations(self):
        """BME:extraction functions when more than one mutation is present """
        expected = {PointMutation(42,'S','T'):1,PointMutation(36,'W','Y'):1}
        self.assertEqual(self.me('S42T and W36Y'),expected)
        self.assertEqual(self.me('S42T W36Y'),expected)
        
    def test_call_count(self):
        """BME:counting of mentions works """
        expected = {PointMutation(42,'S','T'):1,PointMutation(36,'W','Y'):1}
        self.assertEqual(self.me('S42T and W36Y'),expected)
        expected = {PointMutation(42,'S','T'):1,PointMutation(36,'W','Y'):2}
        self.assertEqual(self.me('S42T, W36Y, and W36Y'),expected)
        expected = {PointMutation(42,'S','T'):1,PointMutation(36,'W','Y'):3}
        self.assertEqual(self.me('S42T, W36Y, Trp36Tyr, and W36Y'),expected)

    def test_call_three_to_one_letter_map(self):
        """BME:identical Mutation objects created for varied matches"""
        expected = {PointMutation(42,'A','G'):1}
        self.assertEqual(self.me('The A42G mutation was made.'),expected)
        self.assertEqual(self.me('The Ala42Gly mutation was made.'),expected)
        self.assertEqual(self.me('The A42 to glycine mutation was made.'),\
            expected)
        

    def test_regex_case_sensitive(self):
        """BME:regex case sensitive functions as expected"""
        # one-letter abbreviations must be uppercase
        self.assertEqual(self.me._word_regexs[0].match('a64t'),None)
        self.assertEqual(self.me._word_regexs[0].match('A64t'),None)
        self.assertEqual(self.me._word_regexs[0].match('a64T'),None)
        self.assertEqual(self.me._word_regexs[0].match('A64T').group(),'A64T')

        # three-letter abbreviations must be titlecase
        self.assertEqual(self.me._word_regexs[1].match('ala64gly'),None)
        self.assertEqual(self.me._word_regexs[1].match('ALA64GLY'),None)
        self.assertEqual(self.me._word_regexs[1].match('aLa64gLy'),None)
        self.assertEqual(self.me._word_regexs[1].match('Ala64Gly').group(),\
            'Ala64Gly')

        # full names must be lowercase or titlecase
        self.assertEqual(self.me._string_regexs[3].match(\
            'Ala64 to glycine').group(),'Ala64 to glycine')
        self.assertEqual(self.me._string_regexs[3].match(\
            'Ala64 to Glycine').group(),'Ala64 to Glycine')
        self.assertEqual(self.me._string_regexs[3].match(\
            'Ala64 to GLYCINE'),None)
        self.assertEqual(self.me._string_regexs[3].match(\
            'Ala64 to glYcine'),None)

    def test_one_letter_match(self):
        """BME:regex identifies one-letter codes"""
        self.assertEqual(self.me._word_regexs[0].match('A64G').group(),'A64G')
    
    def test_three_letter_match(self):
        """BME:regex identifies three-letter codes"""
        self.assertEqual(self.me._word_regexs[1].match('Ala64Gly').group(),'Ala64Gly')
    
    def test_varied_digit_length(self):
        """BME:regex identifies mutations w/ different location lengths"""
        self.assertEqual(self.me._word_regexs[0].match('A4G').group(),'A4G')
        self.assertEqual(self.me._word_regexs[0].match('A64G').group(),'A64G')
        self.assertEqual(self.me._word_regexs[0].match('A864G').group(),'A864G')
        self.assertEqual(self.me._word_regexs[0].match('A8864G').group(),'A8864G')


    def test_word_boundary_requirement(self):
        """BME:regex requries word boundaries surrounding mutation"""
        for i in range(len(self.me._word_regexs)):
            self.assertEqual(self.me._word_regexs[i].match('TheAla64Glymut'),None)
            self.assertEqual(self.me._word_regexs[i].match('Ala64Gly/p53634'),None)

    def test_mix_one_three_letter_match(self):
        """BME:regex ignores one/three letter code mixes"""
        for i in range(len(self.me._word_regexs)):
            self.assertEqual(self.me._word_regexs[i].match('Ala64G'),None)
            self.assertEqual(self.me._word_regexs[i].match('A64Gly'),None)

    def test_preprocess_words(self):
        """BME:word-level preprocessing functions as expected"""
        
        r = "this is a t64g mutation."
        expected = ['this','is','a','t64g','mutation']
        self.assertEqual(self.me._preprocess_words(r),expected)
        
        r = "this is ! t64g mutation."
        expected = ['this','is','','t64g','mutation']
        self.assertEqual(self.me._preprocess_words(r),expected)
        
        r = ""
        expected = []
        self.assertEqual(self.me._preprocess_words(r),expected)

    def test_preprocess_sentences(self):
        """BME:sentence-level preprocessing functions as expected"""
        r = "This is a test. The T65->Y mutation"
        expected = ['This is a test','The T65Y mutation']
        self.assertEqual(self.me._preprocess_sentences(r),expected)

    def test_replace_regex(self):
        """BME: replace regex functions as expected"""
        self.assertEqual(self.me._replace_regex.sub('',''),'')
        self.assertEqual(self.me._replace_regex.sub('','a46t'),'a46t')
        self.assertEqual(self.me._replace_regex.sub('','a46->t'),'a46t')
        self.assertEqual(self.me._replace_regex.sub('','A234-T'),'A234T')
        self.assertEqual(self.me._replace_regex.sub('','A(234)T'),'A234T')
        self.assertEqual(self.me._replace_regex.sub(\
            '','The Gly64->Thr mutation.'),'The Gly64Thr mutation')

    def test_ten_word_match(self):
        """BME: ten-word pattern functions as expected """
        expected = {PointMutation(42,'S','A'):1}
        self.assertEqual(self.me('Ser42 was mutated to Ala'),expected)
        self.assertEqual(self.me('S42 was mutated to Ala'),expected)
        self.assertEqual(self.me('Ser42 was mutated to alanine'),expected)
        self.assertEqual(self.me('the S42 was mutated to alanine'),expected)
        self.assertEqual(self.me('S42 was mutated to alanine'),expected)
        # Tenth word is alanine, so it's a match
        self.assertEqual(self.me('S42 a a a a a a a a a alanine'),expected)
        # Eleventh word is alanine, so no match
        self.assertEqual(self.me('S42 a a a a a a a a a a alanine'),{})

class MutationFinderTests(TestCase):

    _single_letter_aa_codes = [aa[0] for aa in amino_acid_three_to_one_letter_map]
    _triple_letter_aa_codes = [aa[1] for aa in amino_acid_three_to_one_letter_map]

    def setUp(self):
        """ Initalize some objects for use in the tests """
        self.me = MutationFinder(regular_expressions=regular_expressions)

    def test_init(self):
        """MF: __init__ returns without error """
        me = MutationFinder(regular_expressions=[])
        me = MutationFinder(regular_expressions=regular_expressions)

    def test_call_no_mutations(self):
        """MF: extraction functions with no extraction-worthy data """
        self.assertEqual(self.me(''),{})
        self.assertEqual(self.me('There is no mutation data here.'),{})
        self.assertEqual(self.me('T64 is almost a valid mutation.'),{})
        self.assertEqual(self.me('So is 42S.'),{})
        
    def test_call_single_mutation(self):
        """MF:extraction functions when one mutation is present """
        expected = {PointMutation(42,'S','T'):[(0,4)]}
        self.assertEqual(self.me('S42T'),expected)
        expected = {PointMutation(42,'S','T'):[(4,8)]}
        self.assertEqual(self.me('The S42T mutation was made.'),expected)

    def test_call_multiple_mutations(self):
        """MF:extraction functions when more than one mutation is present """
        expected = {PointMutation(42,'S','T'):[(0,4)],\
                    PointMutation(36,'W','Y'):[(9,13)]}
        self.assertEqual(self.me('S42T and W36Y'),expected)
        
        expected = {PointMutation(42,'S','T'):[(0,8)],\
                    PointMutation(36,'W','Y'):[(13,21)]}
        self.assertEqual(self.me('Ser42Thr and Trp36Tyr'),expected)
 
    def test_call_multiple_mutations_w_positive_lookahead(self):
        """MF:extraction functions when > 1 mutation are look-ahead is req'd """
        expected = {PointMutation(42,'S','T'):[(0,4)],\
                    PointMutation(36,'W','Y'):[(5,9)]}
        self.assertEqual(self.me('S42T W36Y'),expected)
        
        expected = {PointMutation(42,'S','T'):[(0,8)],\
                    PointMutation(36,'W','Y'):[(9,17)]}
        self.assertEqual(self.me('Ser42Thr Trp36Tyr'),expected)
        
    def test_call_spans_tallied(self):
        """MF:spans are tallied in call """
        expected = {PointMutation(42,'S','T'):[(0,4)],\
                    PointMutation(36,'W','Y'):[(9,13)]}
        self.assertEqual(self.me('S42T and W36Y'),expected)
        expected = {PointMutation(42,'S','T'):[(0,4)],\
                    PointMutation(36,'W','Y'):[(6,10),(16,20)]}
        self.assertEqual(self.me('S42T, W36Y, and W36Y'),expected)
        expected = {PointMutation(42,'S','T'):[(0,4)],\
                    PointMutation(36,'W','Y'):[(6,10),(26,30),(12,20)]}
        self.assertEqual(self.me('S42T, W36Y, Trp36Tyr, and W36Y'),expected)

    def test_call_spans_calculated_correctly_for_different_matches(self):
        """MF:spans are correctly calculated for various mention formats"""
        expected = {PointMutation(42,'A','G'):[(4,8)]}
        self.assertEqual(self.me('The A42G mutation was made.'),expected)
        expected = {PointMutation(42,'A','G'):[(4,15)]}
        self.assertEqual(self.me('The Ala42-->Gly mutation was made.'),expected)
        expected = {PointMutation(42,'A','G'):[(4,12)]}
        self.assertEqual(self.me('The Ala42Gly mutation was made.'),expected)
        expected = {PointMutation(42,'A','G'):[(4,20)]}
        self.assertEqual(self.me('The Ala42 to Glycine mutation.'),expected)
        

    def test_regex_case_insensitive_flag_one_letter(self):
        """MF:one-letter abbreviations case-sensitive"""
        self.assertEqual(self.me._regular_expressions[0].match('a64t'),None)
        self.assertEqual(self.me._regular_expressions[0].match('A64t'),None)
        self.assertEqual(self.me._regular_expressions[0].match('a64T'),None)
        self.assertEqual(self.me._regular_expressions[0].match('A64T')\
            .group(),'A64T')

    def test_regex_case_insensitive_flag_three_letter(self):
        """MF:toggle regex case insensitive functions for non-built-in regexs"""
        # IGNORECASE flag on
        self.assertEqual(self.me._regular_expressions[1].match('ala64gly')\
            .group(),'ala64gly')
        self.assertEqual(self.me._regular_expressions[1].match('Ala64Gly')\
            .group(),'Ala64Gly')
        self.assertEqual(self.me._regular_expressions[1].match('aLa64gLy')\
            .group(),'aLa64gLy')
        self.assertEqual(self.me._regular_expressions[1].match('ALA64GLY')\
            .group(),'ALA64GLY')

    def test_one_letter_match(self):
        """MF:regex identifies one-letter codes"""
        self.assertEqual(self.me._regular_expressions[0].match('A64G')\
            .group(),'A64G')


    def test_one_letter_match_loc_restriction(self):
        """MF:single-letter regex ignored positions < 10"""
        self.assertEqual(self.me._regular_expressions[0].match('A64G')\
            .group(),'A64G')
        self.assertEqual(self.me._regular_expressions[0].match('E2F'),None)
        self.assertEqual(self.me._regular_expressions[0].match('H9A'),None)
    
    def test_three_letter_match(self):
        """MF:regex identifies three-letter codes"""
        self.assertEqual(self.me._regular_expressions[1].match('Ala6Gly')\
            .group(),'Ala6Gly')
        self.assertEqual(self.me._regular_expressions[1].match('Ala64Gly')\
            .group(),'Ala64Gly')
    
    def test_varied_digit_length(self):
        """MF:regex identifies mutations w/ different location lengths"""
        self.assertEqual(self.me._regular_expressions[0].match('A64G')\
            .group(),'A64G')
        self.assertEqual(self.me._regular_expressions[1].match('Ala64Gly')\
            .group(),'Ala64Gly')
        self.assertEqual(self.me._regular_expressions[0].match('A864G')\
            .group(),'A864G')
        self.assertEqual(self.me._regular_expressions[1].match('Ala864Gly')\
            .group(),'Ala864Gly')
        self.assertEqual(self.me._regular_expressions[0].match('A8864G')\
            .group(),'A8864G')
        self.assertEqual(self.me._regular_expressions[1].match('Ala8864Gly')\
            .group(),'Ala8864Gly')

    def test_post_process(self):
        """MF:post processing steps function as expected """
        mutations = {PointMutation(460,'W','W'):[(0,5)]}
        expected = {}
        self.me._post_process(mutations)
        self.assertEqual(mutations,expected)
        
        mutations = {PointMutation(460,'W','W'):[(0,5)],\
            PointMutation(460,'W','G'):[(6,11)]}
        expected = {PointMutation(460,'W','G'):[(6,11)]}
        self.me._post_process(mutations)
        self.assertEqual(mutations,expected)


    def test_unacceptable_general_word_boundaries(self):
        """MF:regexs disallow unacceptable word boundaries"""

        starts = list('abcdefghijklmnopqrstuvwxyz0123456789~@#$%^&*_+=])')
        ends = list('abcdefghijklmnopqrstuvwxyz0123456789~@#$%^&*_+=([')
        mutation_texts = ['A64G','Ala64Gly','Ala64-->Gly']

        for mutation_text in mutation_texts:
            for start in starts:
                for end in ends:
                    text = ''.join([start,mutation_text,end])
                    self.assertEqual(self.me(text),{})

        
    def test_acceptable_general_word_boundaries(self):
        """MF:regexs allow acceptable word boundaries"""
        ends = ['.',',','',' ','\t','\n',')',']','"',"'",':',';','?','!','/','-']
        starts = [' ','\t','\n','"',"'",'(','[','','/',',','-']
        mutation_texts = ['A64G','Ala64Gly','Ala64-->Gly']

        for mutation_text in mutation_texts:
            for start in starts:
                for end in ends:
                    text = ''.join([start,mutation_text,end])
                    expected = {PointMutation(64,'A','G'):\
                        [(text.index('A'),text.index('A')+len(mutation_text))]}
                    self.assertEqual(self.me(text),expected)

    def test_mix_one_three_letter_match(self):
        """MF:regex ignores one/three letter code mixes"""
        
        self.assertEqual(self.me('Ala64G'),{})
        self.assertEqual(self.me('A64Gly'),{})

    def test_full_name_matches(self):
        """MF:regex identifies full name mentions of amino acids """
        expected = {PointMutation(64,'A','G'):[(0,15)]}
        self.assertEqual(self.me('alanine64-->Gly'),expected)
        expected = {PointMutation(64,'A','G'):[(0,15)]}
        self.assertEqual(self.me('Ala64-->glycine'),expected)

    def test_single_residue_fails_non_xNy(self):
        """MF:single residue matches fail in non-xNy format """        
        self.assertEqual(self.me('A64-->glycine'),{})
        self.assertEqual(self.me('Ala64-->G'),{})
        
    def test_text_based_matches_w_N_m(self):
        """MF:regex identifies wN m text descriptions """
        texts = ['Ala64 to Gly','Alanine64 to Glycine',\
            'Ala64 to glycine','alanine64 to Gly']

        for text in texts:
            self.assertEqual(self.me(text),\
                {PointMutation(64,'A','G'):[(0,len(text))]})

        texts = ['The Ala64 to Gly substitution',\
                 'The Ala64 to glycine substitution',\
                 'The Ala64 to Gly substitution']
        
        for text in texts:
            self.assertEqual(self.me(text),\
                {PointMutation(64,'A','G'):[(4,len(text)-13)]})

    def test_text_match_spacing(self):
        """MF:mis-spaced text matches fail """
        self.assertEqual(self.me('TheAla40toGlymutation'),{})
        self.assertEqual(self.me('arg40tomet'),{})
        self.assertEqual(self.me('ala25tohis'),{})


class ScriptTests(TestCase):
    """ Tests of the script functions """

    def setUp(self):
        self.bme = BaselineMutationExtractor()
        self.mf = MutationFinder(regular_expressions=regular_expressions)
        self.fake_input_file = fake_input_file.split('\n')
        self.fake_output_file = fake_output_file.split('\n')
        self.fake_normalized_output_file = \
            fake_normalized_output_file.split('\n')
        self.fake_output_file_w_spans = fake_output_file_w_spans.split('\n')

    def test_PointMutation_from_wNm(self):
        """PointMutation_from_wNm: functions with varied valid input"""
        self.assertEqual(PointMutation_from_wNm('A5T'),\
            PointMutation(5,'A','T'))
        self.assertEqual(PointMutation_from_wNm('A56T'),\
            PointMutation(56,'A','T'))
        self.assertEqual(PointMutation_from_wNm('A568T'),\
            PointMutation(568,'A','T'))
        self.assertEqual(PointMutation_from_wNm('A5699T'),\
            PointMutation(5699,'A','T'))
        self.assertEqual(PointMutation_from_wNm('A56990T'),\
            PointMutation(56990,'A','T'))
    
    def test_PointMutation_from_wNm(self):
        """PointMutation_from_wNm: handles invalid input"""
        self.assertRaises(MutationError,PointMutation_from_wNm,'')
        self.assertRaises(MutationError,PointMutation_from_wNm,'T')
        self.assertRaises(MutationError,PointMutation_from_wNm,'AT')
        self.assertRaises(MutationError,PointMutation_from_wNm,'65T')
        self.assertRaises(MutationError,PointMutation_from_wNm,'AGT')
        self.assertRaises(MutationError,PointMutation_from_wNm,'A65')
        self.assertRaises(MutationError,PointMutation_from_wNm,'A65O')
        self.assertRaises(MutationError,PointMutation_from_wNm,'Ala65Lys')

    def test_extract_mutations_from_string(self):
        """extract_mutations_from_string: functions with valid input """
        # Test that for each of the input strings the mutation extractor and
        # extract_mutations_from_string() return identical data
        for line in self.fake_input_file:
            fields = line.split('\t')
            try:
                self.assertEqual(self.bme(fields[1]), \
                 extract_mutations_from_string(fields[1],self.bme))
                self.assertEqual(self.mf(fields[1]), \
                 extract_mutations_from_string(fields[1],self.mf))
            except IndexError:
                pass

    def test_extract_mutations_from_lines(self):
        """extract_mutations_from_lines: functions with valid input """
        expected = [('id1',{PointMutation(64,'A','G'):[(4,24)]}),\
           ('id2',{PointMutation(42,'W','A'):[(15,19),(21,29)],\
                   PointMutation(88,'G','Y'):[(35,39),(41,49)]}),\
           ('id3',{}),('id4',{}),('id5',{})] 
        actual = list(extract_mutations_from_lines(self.fake_input_file,\
            self.mf))
        self.assertEqual(actual,expected)

        # Test with empty list passed in
        self.assertEqual([],list(extract_mutations_from_lines([],self.mf)))
  
    def test_extract_mutations_from_lines_to_dict(self):
        """extract_mutations_from_lines_to_dict: functions w/o spans """
        expected = dict([('id1',{PointMutation(64,'A','G'):1}),\
           ('id2',{PointMutation(42,'W','A'):2,\
                   PointMutation(88,'G','Y'):2}),\
           ('id3',{}),('id4',{}),('id5',{})]) 
        actual = extract_mutations_from_lines_to_dict(\
            self.fake_input_file,self.mf,store_spans=False)
        self.assertEqual(actual,expected)

        # Test with empty list passed in
        self.assertEqual({},extract_mutations_from_lines_to_dict([],self.mf))
    

    def test_extract_mutations_from_lines_to_dict_w_spans(self):
        """extract_mutations_from_lines_to_dict: functions with spans """
        expected = dict([('id1',{PointMutation(64,'A','G'):[(4,24)]}),\
           ('id2',{PointMutation(42,'W','A'):[(15,19),(21,29)],\
                   PointMutation(88,'G','Y'):[(35,39),(41,49)]}),\
           ('id3',{}),('id4',{}),('id5',{})]) 
        actual = extract_mutations_from_lines_to_dict(\
            self.fake_input_file,self.mf,store_spans=True)
        self.assertEqual(actual,expected)

        # Test with empty list passed in
        self.assertEqual({},extract_mutations_from_lines_to_dict([],self.mf))
    
    def test_extract_mutations_from_lines_to_file(self):
        """extract_mutations_from_lines_to_file: no spans"""
        from tempfile import mktemp
        from os import remove
        actual_output_filepath = mktemp()
        extract_mutations_from_lines_to_file(self.fake_input_file,\
            actual_output_filepath,self.mf)
        actual_output = list(open(actual_output_filepath))
        # remove the temp file that was created
        remove(actual_output_filepath)
        # compare the lines in the fake output file with those in the 
        # real output file
        for i,line in zip(range(len(actual_output)),actual_output):
            self.assertEqual(line.strip(),self.fake_output_file[i])

    def test_extract_mutations_from_lines_to_file_invalid_params(self):
        """extract_mutations_from_lines_to_file: error when spans=True and normalized=True"""
        from tempfile import mktemp
        from os import remove
        actual_output_filepath = mktemp()
        self.assertRaises(AssertionError,extract_mutations_from_lines_to_file,\
            self.fake_input_file,actual_output_filepath,self.mf,store_spans=True,\
            output_normalized_mutations=True)     
        # Note: don't need to remove anything here b/c the temp file is not
        # actually created   
 
    def test_extract_mutations_from_lines_to_file_normalized(self):
        """extract_mutations_from_lines_to_file: normalized"""
        from tempfile import mktemp
        from os import remove
        actual_output_filepath = mktemp()
        extract_mutations_from_lines_to_file(self.fake_input_file,\
            actual_output_filepath,self.mf,store_spans=False,\
            output_normalized_mutations=True)
        actual_output = list(open(actual_output_filepath))
        # remove the temp file that was created
        remove(actual_output_filepath)
        # compare the lines in the fake output file with those in the 
        # real output file
        for i,line in zip(range(len(actual_output)),actual_output):
            self.assertEqual(line.strip(),self.fake_normalized_output_file[i])
     

    def test_extract_mutations_from_lines_to_file_w_spans(self):
        """extract_mutations_from_lines_to_file: spans"""
        from tempfile import mktemp
        from os import remove
        actual_output_filepath = mktemp()
        extract_mutations_from_lines_to_file(self.fake_input_file,\
            actual_output_filepath,self.mf,store_spans=True)
        actual_output = list(open(actual_output_filepath))
        # remove the temp file that was created
        remove(actual_output_filepath)
        # compare the lines in the fake output file with those in the 
        # real output file
        for i,line in zip(range(len(actual_output)),actual_output):
            self.assertEqual(line.strip(),self.fake_output_file_w_spans[i])
     
    def test_extract_mutations_from_lines_to_file_w_non_default_extractor(self):
        """extract_mutations_from_lines_to_file: functions w baseline extractor
        """
        from tempfile import mktemp
        from os import remove
        actual_output_filepath = mktemp()
        # BaselineMutationExtractor does not provide spans -- requesting
        # them results in a properly handled error
        self.assertRaises(MutationFinderError,\
            extract_mutations_from_lines_to_file,self.fake_input_file,\
            actual_output_filepath,BaselineMutationExtractor(),\
            store_spans=True)
        # remove the temp file that was created
        remove(actual_output_filepath)
        
        
        actual_output_filepath = mktemp()
        extract_mutations_from_lines_to_file(self.fake_input_file,\
            actual_output_filepath,BaselineMutationExtractor())
        
        actual_output = list(open(actual_output_filepath))
        # remove the temp file that was created
        remove(actual_output_filepath)
        # compare the lines in the fake output file with those in the 
        # real output file
        expected_output = ['id1','id2\tG88Y\tG88Y\tW42A\tW42A',\
            'id3','id4','id5']
        for actual,expected in zip(actual_output,expected_output):
            self.assertEqual(actual.strip(),expected)
     
   
    def test_filename_from_filepath(self):
        """filename_from_filepath: functions as expected """

        expected = 'input.txt'

        self.assertEqual(filename_from_filepath('input.txt'),expected) 
        self.assertEqual(filename_from_filepath('~/input.txt'),expected) 
        self.assertEqual(filename_from_filepath('/input.txt'),expected) 
        self.assertEqual(filename_from_filepath('/home/input.txt'),expected) 
        self.assertEqual(filename_from_filepath(\
            '/home/greg/input.txt'),expected) 
        self.assertEqual(filename_from_filepath(\
            '/home/greg//input.txt'),expected) 

    def test_build_output_filepath(self):
        """build_output_filepath: functions as expected with valid input"""
        # slashes appended correctly
        self.assertEqual('./input.txt.mf',\
            build_output_filepath('./','input.txt')) 
        self.assertEqual('./input.txt.mf',\
            build_output_filepath('.','input.txt')) 

        self.assertEqual('/home/greg/input.txt.mf',\
            build_output_filepath('/home/greg','input.txt'))

    def test_build_output_filepath_invalid(self):
        """build_output_filepath: correctly handles blank input_filepath"""
        # slashes appended correctly
        self.assertRaises(MutationFinderError,\
            build_output_filepath,'./','') 

        

if __name__ == "__main__":

    # The first four default regular expressions
    regular_expressions = [\
        '(^|[\s\(\[\'"/,\-])(?P<wt_res>[CISQMNPKDTFAGHLRWVEY])(?P<pos>[1-9][0-9]+)(?P<mut_res>[CISQMNPKDTFAGHLRWVEY])(?=([.,\s)\]\'":;\-?!/]|$))[CASE_SENSITIVE]',\
        '(^|[\s\(\[\'"/,\-])(?P<wt_res>(CYS|ILE|SER|GLN|MET|ASN|PRO|LYS|ASP|THR|PHE|ALA|GLY|HIS|LEU|ARG|TRP|VAL|GLU|TYR)|(GLUTAMINE|GLUTAMIC ACID|LEUCINE|VALINE|ISOLEUCINE|LYSINE|ALANINE|GLYCINE|ASPARTATE|METHIONINE|THREONINE|HISTIDINE|ASPARTIC ACID|ARGININE|ASPARAGINE|TRYPTOPHAN|PROLINE|PHENYLALANINE|CYSTEINE|SERINE|GLUTAMATE|TYROSINE))(?P<pos>[1-9][0-9]*)(?P<mut_res>(CYS|ILE|SER|GLN|MET|ASN|PRO|LYS|ASP|THR|PHE|ALA|GLY|HIS|LEU|ARG|TRP|VAL|GLU|TYR)|(GLUTAMINE|GLUTAMIC ACID|LEUCINE|VALINE|ISOLEUCINE|LYSINE|ALANINE|GLYCINE|ASPARTATE|METHIONINE|THREONINE|HISTIDINE|ASPARTIC ACID|ARGININE|ASPARAGINE|TRYPTOPHAN|PROLINE|PHENYLALANINE|CYSTEINE|SERINE|GLUTAMATE|TYROSINE))(?=([.,\s)\]\'":;\-?!/]|$))',\
        '(^|[\s\(\[\'"/,\-])(?P<wt_res>(CYS|ILE|SER|GLN|MET|ASN|PRO|LYS|ASP|THR|PHE|ALA|GLY|HIS|LEU|ARG|TRP|VAL|GLU|TYR)|(GLUTAMINE|GLUTAMIC ACID|LEUCINE|VALINE|ISOLEUCINE|LYSINE|ALANINE|GLYCINE|ASPARTATE|METHIONINE|THREONINE|HISTIDINE|ASPARTIC ACID|ARGININE|ASPARAGINE|TRYPTOPHAN|PROLINE|PHENYLALANINE|CYSTEINE|SERINE|GLUTAMATE|TYROSINE))(?P<pos>[1-9][0-9]*)-->(?P<mut_res>(CYS|ILE|SER|GLN|MET|ASN|PRO|LYS|ASP|THR|PHE|ALA|GLY|HIS|LEU|ARG|TRP|VAL|GLU|TYR)|(GLUTAMINE|GLUTAMIC ACID|LEUCINE|VALINE|ISOLEUCINE|LYSINE|ALANINE|GLYCINE|ASPARTATE|METHIONINE|THREONINE|HISTIDINE|ASPARTIC ACID|ARGININE|ASPARAGINE|TRYPTOPHAN|PROLINE|PHENYLALANINE|CYSTEINE|SERINE|GLUTAMATE|TYROSINE))(?=([.,\s)\]\'":;\-?!/]|$))',\
        '(^|[\s\(\[\'"/,\-])(?P<wt_res>(CYS|ILE|SER|GLN|MET|ASN|PRO|LYS|ASP|THR|PHE|ALA|GLY|HIS|LEU|ARG|TRP|VAL|GLU|TYR)|(GLUTAMINE|GLUTAMIC ACID|LEUCINE|VALINE|ISOLEUCINE|LYSINE|ALANINE|GLYCINE|ASPARTATE|METHIONINE|THREONINE|HISTIDINE|ASPARTIC ACID|ARGININE|ASPARAGINE|TRYPTOPHAN|PROLINE|PHENYLALANINE|CYSTEINE|SERINE|GLUTAMATE|TYROSINE))(?P<pos>[1-9][0-9]*) to (?P<mut_res>(CYS|ILE|SER|GLN|MET|ASN|PRO|LYS|ASP|THR|PHE|ALA|GLY|HIS|LEU|ARG|TRP|VAL|GLU|TYR)|(GLUTAMINE|GLUTAMIC ACID|LEUCINE|VALINE|ISOLEUCINE|LYSINE|ALANINE|GLYCINE|ASPARTATE|METHIONINE|THREONINE|HISTIDINE|ASPARTIC ACID|ARGININE|ASPARAGINE|TRYPTOPHAN|PROLINE|PHENYLALANINE|CYSTEINE|SERINE|GLUTAMATE|TYROSINE))(?=([.,\s)\]\'":;\-?!/]|$))']

    fake_input_file = """id1\tThe alanine64 to glycine mutation.
id2\tWe constructed W42A (Trp42Ala) and\tG88Y (Gly88Tyr).
id3\tNo mutation mentions here.
id4\t
id5"""

    fake_output_file = """id1\tA64G
id2\tG88Y\tG88Y\tW42A\tW42A
id3
id4
id5"""

    fake_output_file_w_spans = """id1\tA64G:4,24
id2\tG88Y:35,39\tG88Y:41,49\tW42A:15,19\tW42A:21,29
id3
id4
id5"""

    fake_normalized_output_file = """id1\tA64G
id2\tG88Y\tW42A
id3
id4
id5"""




    main()
 
    
