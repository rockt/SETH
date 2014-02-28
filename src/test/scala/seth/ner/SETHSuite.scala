package seth.ner

import org.scalatest.{GivenWhenThen, FunSpec, FunSuite}
import org.scalatest.matchers.ShouldMatchers
import com.codahale.logula.Logging

/**
 * User: rockt
 * Date: 10/30/12
 * Time: 9:09 AM
 */

class SETHSuite extends FunSpec with ShouldMatchers with GivenWhenThen with Logging {
  val SETH = new SETHNER

  def isValid(input: String, parser: SETH.Parser[Any]) = SETH.isValid(input, parser)
  def accept(input: String)(implicit parser: SETH.Parser[Any]) =
    assert(isValid(input, parser) === true, "should accept " + input)
  def reject(input: String)(implicit parser: SETH.Parser[Any]) =
    assert(isValid(input, parser) === false, "should reject " + input)

  describe("Mutation parser should accept") {
    describe("Basic lexemes") {
      it ("Nt") { implicit val parser = SETH.Nt
        accept("a")
        accept("T")
        reject("e")
        reject("at")
      }
      it ("NtString") { implicit val parser = SETH.NtString
        accept("ACGGT")
        accept("AcGgT") //Phil: Does this make sense?
        reject("APGGT")
      }
      it ("name") { implicit val parser = SETH.name
        accept("MLH1")
        accept("Mlh1")
        accept("ARSE")
        accept("dac")
        accept("18wheeler")
        accept("mL2H3a34N34an32")
        reject("ML.H1")
      }
      it ("Number") { implicit val parser = SETH.Number
        accept("123")
        reject("12v21")
        reject("-1")
        reject("12a")
        reject("a12")
      }
    }

    describe("Locations") {
      it ("Loc") { implicit val parser = SETH.Loc
        accept("-200+15")
        accept("1234+u?")
        accept("?")
        accept("34324+234")
        reject("123?")
        accept("IVS1234-1243")
        accept("IVS324+413")
        accept("301_oXYZ:233+17")
        accept("(301_oXYZ:233+17)")
        accept("(EX123-45)")
        reject("(X123-45)")
        reject("EX")
      }
      it ("Offset") { implicit val parser = SETH.Offset
        accept("+u?")
        accept("-d123")
        accept("+12")
        accept("+u12")
        reject("+ 12")
        reject("-d32?")
        reject("d32")
      }
      it ("RealPtLoc") { implicit val parser = SETH.RealPtLoc
        accept("-200+15")
        accept("1234+u?")
        accept("?")
        accept("*34324+234")
        reject("123?")
        accept("301")
        reject("301_")
        reject("301_o")
      }
      it ("IVSLoc") { implicit val parser = SETH.IVSLoc
        accept("IVS1234-12")
        accept("IVS324+413")
        reject("IVS123")
        reject("ivs1234-12")
        reject("Ivs1234-12")
        reject("34324+234")
      }
      it ("PtLoc") { implicit val parser = SETH.PtLoc
        accept("-200+15")
        accept("1234+u?")
        accept("?")
        accept("34324+234")
        reject("123?")
        accept("IVS1234-1243")
        accept("IVS324+413")
      }
      it ("RealExtent") { implicit val parser = SETH.RealExtent
        accept("1234+u?_oMAien324:1234+u?")
        accept("301_oXYZ:233+17")
        reject("1234+u?_o(MAien324):1234+u?")
        reject("1234+u?_oMAien3241234+u?")
      }
      it ("EXLoc") { implicit val parser = SETH.EXLoc
        accept("EX123")
        accept("EX123-45")
        reject("EX123-45-12")
        reject("X123-45")
        reject("EX")
      }
      it ("Extent") { implicit val parser = SETH.Extent
        accept("15_355")
        accept("1234+u?_oMAien324:1234+u?")
        reject("1234+u?_oMAien3241234+u?")
        accept("EX123")
        accept("EX123-45")
        reject("X123-45")
        reject("EX")
      }
      it ("RangeLoc") { implicit val parser = SETH.RangeLoc
        accept("301_oXYZ:233+17")
        accept("(301_oXYZ:233+17)")
        accept("(EX123-45)")
        reject("(X123-45)")
        reject("EX")
      }
      it ("FarLoc") { implicit val parser = SETH.FarLoc
        pending
        accept("ish.u123_Z324_.324(MLH1_i234):c.EX324-324")
        reject("ish.u123_Z324_.324(MLH1_i234)c.EX324-324")
        accept("NM_004006.1:c.15_355")
        accept("c.301-148_301-147")
      }
      it ("ChromBand") { implicit val parser = SETH.ChromBand
        accept("p234.234")
        accept("q234.234")
        accept("q12")
        reject("234.234")
        reject("p234-234")
      }
      it ("ChromCoords") { implicit val parser = SETH.ChromCoords
        accept("(X;Y)(p12.3;q3.4)")
        accept("(X;4)(q21.2;q35)")
        accept("(Mt;4)(p21.2;q35)")
        reject("(XY;2;4)(p21.2;q35;p12)")
        reject("(1;2;3)(p234.234;q213.3234)")
        reject("X;Y(p234.234)")
      }
    }

    describe("Reference sequences") {
      it ("Ref") { implicit val parser = SETH.Ref
        accept("ish.u123_Z324_.324(MLH1_i234):c.")
        accept("ish.u123_Z324_.324(MLH1_i234):")
        accept("NM_004006.2")
        reject("")
        reject("uiae234.iuae234")
      }
      it ("RefType") { implicit val parser = SETH.RefType
        accept("c.")
        accept("n.")
        //reject("n")
        reject("a.")
        reject("aa")
        reject("aa.")
      }
      it ("RefSeqAcc") { implicit val parser = SETH.RefSeqAcc
        accept("LRG123_p324")
        accept("ish.u123_Z324_.324(MLH1_i234)")
        reject("ish.u123_Z324_.324((MLH1_i234))")
      }
      it ("GenBankRef") { implicit val parser = SETH.GenBankRef
        accept("GI:324(MLH1_i234)")
        reject("LRG_t324")
        accept("ish.u123_Z324_.324(MLH1_i234)")
        reject("ish.u123_Z324_.324((MLH1_i234))")
      }
      it ("GI") { implicit val parser = SETH.GI
        accept("GI324")
        accept("GI:324")
        accept("324")
        reject("GI:a234")
        reject("a234")
      }
      it ("AccNo") { implicit val parser = SETH.AccNo
        accept("ish.u123_Z324_.324")
        reject("ish.uiae123_iae324_.324")
      }
      it ("Version") { implicit val parser = SETH.Version
        accept(".9234")
        reject("234")
        reject(".a234")
      }
      it ("GeneSymbol") { implicit val parser = SETH.GeneSymbol
        accept("MLH1_v1")
        accept("MLH")
        reject("(MLH1_v1)")
        reject("(MLH1_i123)")
        reject("_v1")
      }
      it ("TransVar") { implicit val parser = SETH.TransVar
        accept("_v324")
        reject("_324")
        reject("aie324")
        reject("_va")
      }
      it ("ProtIso") { implicit val parser = SETH.ProtIso
        accept("_i324")
        reject("_i")
        reject("_324")
        reject("aie324")
        reject("_ia")
      }
      it ("LRGTranscriptID") { implicit val parser = SETH.LRGTranscriptID
        accept("t324")
        reject("t")
        reject("t234a")
      }
      it ("LRGProteinID") { implicit val parser = SETH.LRGProteinID
        accept("p324")
        reject("p")
        reject("t234a")
        reject("t234")
      }
      it ("LRG") { implicit val parser = SETH.LRG
        accept("LRG123_p324")
        accept("LRG0_t324")
        accept("LRG123")
        reject("LRG_t324")
      }
      it ("Chrom") { implicit val parser = SETH.Chrom
        accept("MLH1")
        accept("mL2H3a34N34an32")
        reject("ML.H1")
      }
    }

    describe("Single Variations") {
      it ("Subst") { implicit val parser = SETH.Subst
        accept("123A>G")
        reject("123A<G")
        //reject("123A->G")
        //reject("123A-->G")
        reject("A123G")
      }
      it ("Del") { implicit val parser = SETH.Del
        accept("456_458del3")
        accept("413del")
        accept("1598delG")
        accept("124-12_129del18")
        accept("456_458delA")
        accept("4delG")
        accept("456_458delAT")
        accept("(301_oXYZ:233+17)del")
        reject("(301_oXYZ:233+17)")
      }
      it ("Dup") { implicit val parser = SETH.Dup
        accept("78_79dupCG")
        accept("120_123+48dup")
        accept("307_308dupTG")
        accept("78_79dup")
        reject("78_79.CG")
      }
      it ("AbrSSR") { implicit val parser = SETH.AbrSSR
        accept("7TG(3_6)")
        reject("7(TG)3_6")
      }
      it ("VarSSR") { implicit val parser = SETH.VarSSR
        accept("123ACT[123]")
        accept("301_oXYZ:233+17[123]")
        accept("7TG(3_6)")
        reject("301_oXYZ:233+17[123]iaue")
      }
      it ("Ins") { implicit val parser = SETH.Ins
        accept("76_77insT")
        accept("51_52insT")
        accept("51_52insGAGA")
        accept("301_oXYZ:233+17insTAACT")
        reject("301_oXYZ:233+17insTAACT123")
      }
      it ("Indel") { implicit val parser = SETH.Indel
        accept("12_15delGGACinsTA")
        accept("712_717delinsTG")
        accept("712_717del6insTG")
        accept("712_717delAGGGCAinsTG")
        accept("112_117delAGGGCAinsTG")
        accept("112_117del6insTG")
        accept("112_117delinsTG")
        reject("12_15delGGACinsTA123")
      }
      it ("Inv") { implicit val parser = SETH.Inv

        accept("1077_1080inv")
        accept("1077_1080inv4")
        accept("1077_1080invCTAG")
        accept("77_80inv4")
        accept("77_80inv")
        accept("77_80invCTAG")
        accept("77_80inv4")
        accept("301_oXYZ:233+17inv")
        reject("301_oXYZ:233+17")
      }
      it ("Conv") { implicit val parser = SETH.Conv
        accept("301_oXYZ:233+17conish.u123_Z324_.324(MLH1_i234):c.EX324-324")
        reject("301_oXYZ:233+17consh.u123_Z324_.324(MLH1_i234):c.EX324-324")
        reject("301_oXYZ:233+17coish.u123_Z324_.324(MLH1_i234):c.EX324-324")
      }
      it ("TransLoc") { implicit val parser = SETH.TransLoc
        pending
      }
      it ("RawVar") { implicit val parser = SETH.RawVar
        accept("123A>G")
        reject("123A<G")
        accept("456_458del3")
        accept("(301_oXYZ:233+17)del")
        reject("(301_oXYZ:233+17)")
        accept("78_79dupCG")
        accept("78_79dup")
        reject("78_79.CG")
        accept("123ACT[123]")
        accept("301_oXYZ:233+17[123]")
        accept("7TG(3_6)")
        reject("301_oXYZ:233+17[123]iaue")
        accept("76_77insT")
        accept("301_oXYZ:233+17insTAACT")
        reject("301_oXYZ:233+17insTAACT123")
        accept("12_15delGGACinsTA")
        reject("12_15delGGACinsTA123")
        accept("77_80inv4")
        accept("301_oXYZ:233+17inv")
        reject("301_oXYZ:233+17")
        accept("301_oXYZ:233+17conish.u123_Z324_.324(MLH1_i234):c.EX324-324")
        reject("301_oXYZ:233+17consh.u123_Z324_.324(MLH1_i234):c.EX324-324")
        reject("301_oXYZ:233+17coish.u123_Z324_.324(MLH1_i234):c.EX324-324")
      }
      it ("SingleVar") { implicit val parser = SETH.SingleVar
        accept("ish.u123_Z324_.324(MLH1_i234):c.301_oXYZ:233+17conish.u123_Z324_.324(MLH1_i234):c.EX324-324")
        //TODO: accept TransLoc
      }
      it ("ExtendedRawVar") { implicit val parser = SETH.ExtendedRawVar
        accept("123A>G=")
        accept("123A>G?")
        reject("123A>G")
      }
      it ("UnkEffectVar") { implicit val parser = SETH.UnkEffectVar
        accept("ish.u123_Z324_.324(MLH1_i234):c.?")
        accept("ish.u123_Z324_.324(MLH1_i234):c.(=)")
        reject("ish.u123_Z324_.324(MLH1_i234):c.")
      }
      it ("SplicingVar") { implicit val parser = SETH.SplicingVar
        accept("ish.u123_Z324_.324(MLH1_i234):c.spl?")
        accept("ish.u123_Z324_.324(MLH1_i234):c.(spl?)")
        reject("ish.u123_Z324_.324(MLH1_i234):c.(spl?")
        reject("ish.u123_Z324_.324(MLH1_i234):c.")
      }
      it ("NoRNAVar") { implicit val parser = SETH.NoRNAVar
        accept("ish.u123_Z324_.324(MLH1_i234):c.0")
        accept("ish.u123_Z324_.324(MLH1_i234):c.0?")
        reject("ish.u123_Z324_.324(MLH1_i234):c.?")
        reject("ish.u123_Z324_.324(MLH1_i234):c.")
      }
    }

    //FIXED: we want to find each mutation individually
    //    describe("Multiple Variations") {
    //      it ("CAlleleVarSet") { implicit val parser = MutationParser.CAlleleVarSet
    //        accept("123A>G=;123A>G=;123A>G?")
    //      }
    //      it ("UAlleleVarSet") { implicit val parser = MutationParser.UAlleleVarSet
    //        accept("123A>G=;123A>G=;123A>G?")
    //        accept("123A>G=;123A>G=;123A>G??")
    //        accept("123A>G=;123A>G=;123A>G??")
    //        accept("(123A>G=;123A>G=;123A>G?)?")
    //        reject("(123A>G=;123A>G=123A>G?)?")
    //      }
    //      it ("SimpleAlleleVarSet") { implicit val parser = MutationParser.SimpleAlleleVarSet
    //        accept("[(123A>G=;123A>G=;123A>G?)?]")
    //        accept("[(123A>G=;123A>G=;123A>G?)?]")
    //        accept("123A>G=")
    //      }
    //      it ("MosaicSet") { implicit val parser = MutationParser.MosaicSet
    //        accept("[(123A>G=;123A>G=;123A>G?)?]")
    //        accept("[(123A>G=;123A>G=;123A>G?)?]")
    //        accept("123A>G=")
    //        accept("[[(123A>G=;123A>G=;123A>G?)?]/[(123A>G=;123A>G=;123A>G?)?]/[(123A>G=;123A>G=;123A>G?)?]]")
    //        reject("[[(123A>G=;123A>G=;123A>G?)?][(123A>G=;123A>G=;123A>G?)?]/[(123A>G=;123A>G=;123A>G?)?]]")
    //      }
    //      it ("ChimeronSet") { implicit val parser = MutationParser.ChimeronSet
    //        accept("[[[(123A>G=;123A>G=;123A>G?)?]/[(123A>G=;123A>G=;123A>G?)?]/[(123A>G=;123A>G=;123A>G?)?]]//" +
    //          "[[(123A>G=;123A>G=;123A>G?)?]/[(123A>G=;123A>G=;123A>G?)?]/[(123A>G=;123A>G=;123A>G?)?]]]")
    //      }
    //    }


    implicit val parser = SETH.mutation
    describe("Acceptance Tests") {
      describe("DNA Variations") {
        //http://www.hgvs.org/mutnomen/quickref.html
        it("Substitution") {
          accept("c.-7A>C")
          accept("c.3G>T")
          accept("c.88+2T>G")
          accept("c.89-1G>T")
          accept("c.*23T>C")
          accept("NM_004006.1:c.3G>T")
          accept("GJB6:c.3G>T")
          accept("rs2306220:A>G")
          accept("r.67g>u")
        }

        it("Deletion") {
          accept("c.13del")
          accept("c.13_16del")
          accept("c.120_123+4del8")
          accept("c.13-?_300+?del")
          accept("r.13del")
          accept("r.13_16del")
          accept("r.13_300del")
        }

        it("Duplication") {
          accept("c.13dup")
          accept("c.92_94dup")
          accept("r.13dup")
          accept("r.92_94dup")
        }

        it("Insertion") {
          accept("c.51_52insT")
          accept("c.51_52insGAGA")
          accept("r.51_52insu")
          accept("r.51_52insgaga")
        }

        it("Short sequence repeats") {
          accept("c.162CAG(12_34)")
          accept("c.123+74CA(3_6)")
          accept("g.958A(19_23)")
          accept("r.162cag(12_34)")
        }

        it("Inversion") {
          accept("c.77_80inv")
          accept("r.77_80inv")
        }

        it("Complex") {
          accept("c.112_117delinsTG")
          //accept("(c.112_117del6insTG)")
          accept("r.112_117delinsug")
          //accept("(r.112_117del6insug)"))
        }

        it("Two changes in one allele") {
          pending //we want these mutations separately
          accept("c.[76C>T; 83G>C]")
          accept("r.[r.76C>T; r.83G>C]")
        }

        it("Two changes in one individual, alleles unknown") {
          pending //we want these mutations separately
          accept("c.[76C>T(;)181T>C]")
          accept("r.[76C>T(;) 181T>C]")
        }

        it("Recessive disease (changes in different alleles)") {
          pending //we want these mutations separately
          accept("c.[76C>T];[87G>A]")
          accept("c.[76C>T];[?]")
          accept("c.[76C>T];[=]")
          accept("r.[76c>u];[87g>a]")
          accept("r.[76c>u];[?]")
          accept("r.[76c>u];[=]")
        }

        it("Recessive disease (changes in different genes)") {
          pending //we want these mutations separately
          accept("GJB2:c.[76C>T]; GJB6:c.[87G>A]")
          accept("GJB2:r.[76c>u]; GJB6:r.[87g>a]")
        }

        it("Exact location unknown") {
          accept("c.(67_70)insG")
          accept("c.(165_253)del11")
          accept("c.88-?_923+?del")
          accept("r.(67_70)insg")
          accept("r.(165_253)del11")
          accept("r.88_923del")
          reject("r.0?")
        }

        it("Frame shift") {
          //??
        }

        it("Sanity checks") {
          reject("0")
          reject("C1*")
          reject("L-6K")
          reject("M6*")
          reject("R141716A")
          reject("L-11R")
          reject("G>A")
          reject("Y5Y")
        }
      }

      describe("Protein Variations") {
        //http://www.hgvs.org/mutnomen/quickref.html
        it("Substitution") {
          accept("p.Trp26Cys")
          //accept("(p.W26C)")
          accept("p.Trp26*")
          //accept("(p.W26*)")
        }

        it("Deletion") {
          accept("p.Gly4del")
          //accept("(p.G4del)")
          accept("p.Gly4_Gln6del")
          //accept("(p.G4_Q6del)")
          accept("p.Gly4Valfs*14")
          //accept("(p.Gly4fs)")
        }

        it("Duplication") {
          accept("p.Gly4dup")
          //accept("(p.G4dup)")
          accept("p.Gly4_Gln6dup")
          //accept("( p.G4_Q6dup)")
          //accept("(p.G4_Q6dup)")
        }

        it("Insertion") {
          accept("p.Lys2_Leu3insGlnSer")
          //accept("(p.K2_L3insQS)")
        }

        it("Short sequence repeats") {
          accept("p.Gln54(12_34)")
          //accept("(p.Q(12_34))")
        }

        it("Inversion") {
          accept("c.77_80inv")
          accept("r.77_80inv")
        }

        it("Complex") {
          accept("p.Cys28_Lys29delinsTrp")
          //accept("(p.C28_K29delinsW)")
        }

        it("Two changes in one allele") {
          pending //we want these mutations separately
          accept("p.[Trp13*; Ala43Pro]")
        }

        it("Two changes in one individual, alleles unknown") {
          pending //we want these mutations separately
          accept("p.[Trp13*(;)Trp61Arg]")
        }

        it("Recessive disease (changes in different alleles)") {
          pending //we want these mutations separately
          accept("p.[Trp13*];[Cys28Arg]")
          accept("p.[Trp13*];[?]")
          accept("p.[Trp13*];[=]")
        }

        it("Recessive disease (changes in different genes)") {
          pending //we want these mutations separately
          accept("GJB2:p.[Trp13*]; GJB6:p.[Cys28Arg]")
        }

        it("Exact location unknown") {
          accept("p.Gly23fs")
          //accept("p.fs") //??
          accept("p.Thr29fs*16")
        }

        it("Frame shift") {
          accept("p.Gly44Valfs*14")
          //accept("(p.Gly4fs)")
          accept("p.Leu30Serfs*3")
          //accept("(p.Leu30fs)")
        }

        it("Sanity checks") {
          reject("0")
        }
      }

      describe("Bugs") {
        it ("Fixed") {
          accept("NP_001135419.1:p.Asn904=")
          accept("NP_116277.2:p.Tyr1850Cys")
          accept("NM_017774.3:c.286+45798_286+45799N>T")
          accept("NC_000003.11:g.131897971_131897972AC>C")
          accept("NM_004782.3:c.*515_*522ACACACTC>T")
          accept("NM_004006.1:c.15_355C>T")
          accept("NP_001106653.1:p.Glu500Ter")
          accept("p.(Trp26*)")
          accept("r.(123A>C)")
          accept("NP_000268.1:p.Lys452ext*LysValfs")
          accept("NP_031393.2:p.Met1extMet-SerValfs")
          accept("XP_003119069.1:p.Ala239ext*AlaValfs")
          accept("NP_001021.1:p.Met1extMet-AsnAlafs")
          accept("NT_007592.15:g.12940923_(?_12940924)insA")
          accept("NT_007592.15:g.12940923_(?_12940924)insG")
          accept("NM_000348.3:c.*(91_?)_*(91_?)delinsC")
          accept("NM_000348.3:c.*(91_?)_*(91_?)delinsT")
          reject("r.0?")
          assert(SETH.extractMutations("G:C>AT").isEmpty)
          assert(SETH.extractMutations("18:1delta").isEmpty)
          assert(SETH.extractMutations("DFDBA>c").isEmpty)
          assert(SETH.extractMutations("DODAC>N").isEmpty)
          assert(SETH.extractMutations("A:T>T").isEmpty)
        }
        it ("Pending") {
          pending
          accept("c.15_355conNM_004006.1:c.15_355")
        }
        it ("Questionable") {
          pending
          accept("c.13-?_300+?del")
          accept("c.?_-244_32+?del")
          accept("?_-244_32+?del")
          accept("(?_-244)_32+?")
          accept("c.(?_-244)_32+?del")
          accept("7(TG)3_6")(SETH.AbrSSR)
          accept("NC_000009.11:g.97222895delCinsACC")
          accept("c.385G<T")
          accept("c145A>G")
          accept("p.E228 K")
          accept("c.2097-2098insT") //needs to be an underscore
          accept("c.861insG") //no range
          accept("c.164insA") //no range
        }
      }
    }
  }
}

class SingleTest extends FunSuite {
  val SETH = new SETHNER(false)
  //val SETH = new SETHNER(true)
  test("Mutation object creation") {
    val mutation = SETH.extractMutations("p.Pro243Ser")(0)
    println(mutation.toString)
  }
  test("Bugs") {
    //assert(SETH.isValid("c.15_355conNM_004006.1:c.15_355", SETH.mutation) === true) //DONE
    //assert(SETH.isValid("NP_001106653.1p.Glu500Ter", SETH.mutation) === true) //TODO
    //assert(SETH.isValid("NP_001106653.1:p.Glu500Ter", SETH.mutation) === true) //DONE
    //assert(SETH.isValid("p.(Trp26*)", SETH.mutation) === true) //DONE
    //assert(SETH.isValid("r.(123A>C)", SETH.mutation) === true) //DONE
    //assert(SETH.isValid("NP_031393.2:p.Met1extMet-SerValfs", SETH.mutation) === true) //DONE
    //assert(SETH.isValid("91_*(91_?)", SETH.Loc) === true) //DONE
    //assert(SETH.isValid("NT_007592.15:g.12940923_(?_12940924)insA", SETH.mutation) === true) //DONE
    assert(SETH.isValid("c.861insG", SETH.mutation) === true)
    assert(SETH.isValid("AF177763.1:g.203A>C", SETH.mutation) === true)
    assert(SETH.isValid("p.Cys817Valfs", SETH.mutation) === true)

    //println(SETH.extractMutations("p.M1?").head)
  }

  test("Whitespace fix") {
    //val text = "c.111A > T c.222C-->T c 333C --> T"
    //val text = "Mutation analysis was performed in six patients leading to the detection of c.3036_3038delGGT and NM_004006.1:c.3G>T."
    val text = "Causative GJB2 mutations were identified in 31 (15.2%)patients, and two common mutations, c.35delG and L90P (c.269T>C), accounted for 72.1% and 9.8% of GJB2 disease alleles."
    //val text = "a  test"
    //val text = "a test with          whitespaces"
    val mutations = SETH.extractMutations(text)
    for (mutation <- mutations) {
      println("[" + text.substring(mutation.start, mutation.end) + "]\n" + mutation)
    }
  }

  test("More Bugs") {
    assert(SETH.isValid("p.990delM", SETH.mutation) === true)
    assert(SETH.isValid("c.2970-2972 delAAT", SETH.mutation) === true)
    assert(SETH.isValid("c. 529T>C", SETH.mutation) === true)
    assert(SETH.isValid("p. W177R", SETH.mutation) === true)
    assert(SETH.isValid("c.861insG", SETH.mutation) === true)
    assert(SETH.isValid("c.164insA", SETH.mutation) === true)
  }

  test("Structural Abnormalities") {
    val debug = false
    //short forms
    assert(SETH.isValid("47,xx,+21", SETH.StructAbnorm, debug) === true)
    assert(SETH.isValid("45,xx,-15", SETH.StructAbnorm, debug) === true)
    assert(SETH.isValid("46,xx,del(5)(p14)", SETH.StructAbnorm, debug) === true)
    assert(SETH.isValid("46,xx,t(2;3)(q31;p21)", SETH.StructAbnorm, debug) === true)
    assert(SETH.isValid("47,xx,+2,t(2;3)(q31;p21),del(5)(p14)", SETH.StructAbnorm, debug) === true)

    //long froms
    assert(SETH.isValid("46,xx,del(1)(pter->q21)", SETH.StructAbnorm, debug) === true)
    assert(SETH.isValid("46,xx,del(1)(pter->q21::q31->qter)", SETH.StructAbnorm, debug) === true)
    assert(SETH.isValid("chr11:125,940..155,000", SETH.StructAbnorm, debug) === true)
    assert(SETH.isValid("chr5:70,060,034-70,481,083", SETH.StructAbnorm) === true)
    //hard cases
    assert(SETH.isValid("47,XY+21", SETH.StructAbnorm, debug) === true)
    assert(SETH.isValid("46,X,t(X;16)(p11.23;p12.3)", SETH.StructAbnorm, debug) === true)
    assert(SETH.isValid("46,X,der(X)t(X;6)(q22;p23)", SETH.StructAbnorm, debug) === true)
    assert(SETH.isValid("46,X,del(Y)", SETH.StructAbnorm, debug) === true)
    assert(SETH.isValid("46,X,del(Y)(p11.31)", SETH.StructAbnorm, debug) === true)
    assert(SETH.isValid("46,X,idic(Y)(p11.31)", SETH.StructAbnorm, debug) === true)

    assert(SETH.isValid("der(Y)t(Y;1)(q12:q21)", SETH.StructAbnorm, debug) === true)
    assert(SETH.isValid("46,X,der(X)(pter->q21.1::p11.4->pter)", SETH.StructAbnorm, debug) === true)
    assert(SETH.isValid("46,X,der(X)(pter->q21.1::p11.4-->pter)", SETH.StructAbnorm, debug) === true)
    assert(SETH.isValid("46,X,del(X)(p11.23)", SETH.StructAbnorm, debug) === true)
    //assert(SETH.isValid("der(X)del(X)(p11.23)dup(X)(p11.21p11.22)", SETH.StructAbnorm, debug) === true) //this one is really tricky
    //too unspecific
    assert(SETH.isValid("cen", SETH.StructAbnorm, debug) === false)
    assert(SETH.isValid("5-y", SETH.StructAbnorm, debug) === false)
    assert(SETH.isValid("1-14", SETH.StructAbnorm, debug) === false)
    assert(SETH.isValid("p30", SETH.StructAbnorm, debug) === false)
    assert(SETH.isValid("133X", SETH.StructAbnorm, debug) === false)
    assert(SETH.isValid("45,X", SETH.StructAbnorm, debug) === false)
    assert(SETH.isValid("5178Y", SETH.StructAbnorm, debug) === false)
    assert(SETH.isValid("8p12–q12.1", SETH.StructAbnorm, debug) === false)
    assert(SETH.isValid("15q11-q13", SETH.StructAbnorm, debug) === false)
    assert(SETH.isValid("15q11.2", SETH.StructAbnorm, debug) === false)
    assert(SETH.isValid("15q12", SETH.StructAbnorm, debug) === false)
    assert(SETH.isValid("Xp22", SETH.StructAbnorm, debug) === false)
    assert(SETH.isValid("Xq28", SETH.StructAbnorm, debug) === false)
    assert(SETH.isValid("15q26-qter", SETH.StructAbnorm, debug) === false)
    assert(SETH.isValid("17p11.2", SETH.StructAbnorm, debug) === false)
    assert(SETH.isValid("46,xx", SETH.StructAbnorm, debug) === false)
    assert(SETH.isValid("15q11-q13", SETH.StructAbnorm, debug) === false)
    assert(SETH.isValid("1x-y", SETH.StructAbnorm, debug) === false)
    assert(SETH.isValid("2X-4", SETH.StructAbnorm, debug) === false)
    assert(SETH.isValid("4X-8", SETH.StructAbnorm, debug) === false)


    assert(SETH.isValid("1.03 x -29", SETH.StructAbnorm, debug) === false)
    assert(SETH.isValid("1.03 x -29", SETH.StructAbnorm, debug) === false)
    assert(SETH.isValid("0.81x+0.13", SETH.StructAbnorm, debug) === false)

    //open bugs
    assert(SETH.isValid("47 XY+21", SETH.StructAbnorm, debug) === true)
    assert(SETH.isValid("46 X-X", SETH.StructAbnorm, debug) === true)
    assert(SETH.isValid("10.10 x +10", SETH.StructAbnorm, debug) === false)
    assert(SETH.isValid("1 X-4", SETH.StructAbnorm, debug) === false)
    assert(SETH.isValid("2.67 x +2.59", SETH.StructAbnorm, debug) === false)
    assert(SETH.isValid("47 x -7.2", SETH.StructAbnorm, debug) === false)
    assert(SETH.isValid("7 X -1", SETH.StructAbnorm, debug) === false)
    assert(SETH.isValid("3.7 x-1", SETH.StructAbnorm, debug) === false)
    assert(SETH.isValid("3 x -29", SETH.StructAbnorm, debug) === false)
    assert(SETH.isValid("10 X -1.32", SETH.StructAbnorm, debug) === false)
    assert(SETH.isValid("815 X-2", SETH.StructAbnorm, debug) === false)
    //unsure
    //assert(SETH.isValid("4,x-8", SETH.CNV, debug) === false)
  }

  test("VerspoorPaper"){
    val debug = false;
    // DONE:
    assert(SETH.isValid("p.T325_C328insdelS", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.T325_C328delinsS", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.L747_P753>S", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.A67_M72>V", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.N567_L576>I", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.S566_E571>K", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.E57_D58>DY", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.L747_K754>N", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.D148_G149>ES", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.F537_I540>LV", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.Q439_T440>PP", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.M979_K980>I*", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.E749_E758>QP", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.L242_T244>FTCPS", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.H594_P595>PT", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.L747_E749>C", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.M552_V559>I", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.E185_E186>D*", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.E709_T710>A", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.E746_A750>EP", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.S1278_F1279>F", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.I843_S847>L", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.N567_Y578>ED", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.L308_S312>P", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.H776_C777>QS", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.D627_L633>E", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.Q612_R613>HC", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.I245_S246>HRGC", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.S374_E375>R*", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.T751_E758>A", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.E709_T710>D", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.K579_H580>ND", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.L125_H126>FY", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.I1676_V1677>I", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.I245_S246>SHQPC", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.Q556_K558>PT", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.N406_L407>M", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.L146_G147>FC", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.W334_P335>CS", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.L242_T243>CGIREI", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.T751_I759>N", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.L747_P753>Q", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.E224_I227>V", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.K94_T95>NP", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.L243_T244>PIYRCVL", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.W557_V559>F", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.E746_T751>I", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.N771_P772>SVDNR", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.V600_K601>E", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.E746_T751>A", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.L747_A750>P", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.L747_T751>P", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.D523_P524>EA", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.L242_T244>CPP", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.K664_K665>N*", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.R152_I153>SF", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.L243_T244>PLCSA", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.I241_L242>CRPH", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.Q341_G347>H", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.E237_L242>ASWC", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.E746_A750>IP", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.P775>?", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.G244_M246>V", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.L242>FDCIGV", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.Y553_L576>NCLHLYSSQ", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.E746_S752>V", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.G15_L16>GF", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.K896_F897>I", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.L747_K754>GC", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.F375_P376>S", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.E746_A750>QP", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.L1386_Q1387>L*", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.I744_A750>VK", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.D210_L211>EI", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.K558_T574>NRS", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.G284_P287>A", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.L747_P753>CG", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.A254_L255>EKV", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.P551_Q556>T", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.L306_G307>FW", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.L243_T244>PCPL", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.L747_T751>Q", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.L747_T751>S", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.Q119_A123>H", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.E289_A290>DT", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.S80_P81>T", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.Q556_V559>H", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.N1375_A1376>KS", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.K459_S460>N", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.E434_H435>D", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.L248_S251>CQ", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.D770_P772>ASVDNR", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.L64_E69>Q", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.N995_G996>K", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.I247>KCH", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.L242>CWMK", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.L243>GCI", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.T325_C328>S", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.S566_E571>R", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.S215_V218>R", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.E1053_Y1054>DH", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.L242>FSCGP", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.L112_E113>*", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.T244>KKCTN", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.L242>FEC", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.E519>D*", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.I8521>SL", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.L243>RCPS", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.E180>DGRCPHQ", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.Q1168>HE", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.F1607_K1608>LGSQ", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.G607>?", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.S704>?", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.A19>?", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.R583>?", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.N443>?", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.G536>?", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.G1050>?", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.L243>CPSP", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.G346>?", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.P272>?", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.G1283>?", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.P761>?", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.A166>?", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.G248>?", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.P10>QS", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.V8226>?", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.L380>?", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.S1530>?", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.A89>?", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.P325>?", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.P52>?", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.G464>?", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.L948>?", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.R216>?", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.T98>?", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.V192>?", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.C5_D7>Y", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.E1138>?", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.I2752>?", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.P1139>?", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.E746_T751>IP", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.T392>?", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.W5642>?", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.S1280>?", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.W15475>?", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.V600>DLAT", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.(Q1434)fs", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.(I1290)fs", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.(L1515)fs", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.(N216)fs", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.(K1322)fs", SETH.mutation, debug) === true)

    //TODO:
    assert(SETH.isValid("c.2235_2252insdelAAT", SETH.mutation, debug) === true) //remove from test case
    assert(SETH.isValid("c.2235_2252delinsAAT", SETH.mutation, debug) === true)         //remove from test case
    assert(SETH.isValid("p.?fs*?", SETH.mutation, debug) === true)
    assert(SETH.isValid("c.2235_2252>AAT", SETH.mutation, debug) === true)
    assert(SETH.isValid("c.281_282ins?", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.fs?", SETH.mutation, debug) === true)
    assert(SETH.isValid("c.743_755>GTCAAAG", SETH.mutation, debug) === true)
    assert(SETH.isValid("c.7521_7540>TCTCCTACC", SETH.mutation, debug) === true)
    assert(SETH.isValid("c.522+-155delTCAGATTGTGGTGGAGGAAG", SETH.mutation, debug) === true)
    assert(SETH.isValid("c.528_529ins?", SETH.mutation, debug) === true)
    assert(SETH.isValid("c.2239_2264>GCCAA", SETH.mutation, debug) === true)
    assert(SETH.isValid("c.481_482ins?", SETH.mutation, debug) === true)
    assert(SETH.isValid("c.2236_2248>CAAC", SETH.mutation, debug) === true)
    assert(SETH.isValid("c.713+8insAGT", SETH.mutation, debug) === true)
    assert(SETH.isValid("c.1072_1073CC>11", SETH.mutation, debug) === true)
    assert(SETH.isValid("c.405_406ins?", SETH.mutation, debug) === true)
    assert(SETH.isValid("c.252_253ins?", SETH.mutation, debug) === true)
    assert(SETH.isValid("c.461_462ins?", SETH.mutation, debug) === true)
    assert(SETH.isValid("c.236+-6delAGCGAAGGT", SETH.mutation, debug) === true)
    assert(SETH.isValid("c.2230_2249>GTCAA", SETH.mutation, debug) === true)
    assert(SETH.isValid("c.2235_2251>AATTC", SETH.mutation, debug) === true)
    assert(SETH.isValid("c.2235_2248>AATTC", SETH.mutation, debug) === true)
    assert(SETH.isValid("c.457_458ins?", SETH.mutation, debug) === true)
    assert(SETH.isValid("c.4191+-4delCTGAGGTATTAATTATATATA", SETH.mutation, debug) === true)
    assert(SETH.isValid("c.4455+-25delGCCAGCAAGGTGAACCGGGTCCAAAGG", SETH.mutation, debug) === true)
    assert(SETH.isValid("c.3999+2insT", SETH.mutation, debug) === true)
    assert(SETH.isValid("c.184_185ins?", SETH.mutation, debug) === true)
    assert(SETH.isValid("c.182_183ins?", SETH.mutation, debug) === true)
    assert(SETH.isValid("c.2235_2251>AG", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.fs*?", SETH.mutation, debug) === true)
    assert(SETH.isValid("c.710_726>CCTCCTGGTGC", SETH.mutation, debug) === true)
    assert(SETH.isValid("c.2235_2255>GGT", SETH.mutation, debug) === true)
    assert(SETH.isValid("c.105_144>TTC", SETH.mutation, debug) === true)
    assert(SETH.isValid("c.157_158ins?", SETH.mutation, debug) === true)
    assert(SETH.isValid("c.2239_2251>C", SETH.mutation, debug) === true)
    assert(SETH.isValid("c.2237_2255>T", SETH.mutation, debug) === true)
    assert(SETH.isValid("c.?ins?", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.0", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.??ins?", SETH.mutation, debug) === true)
    assert(SETH.isValid("c.??ins?", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.?del", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.0?", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.?fs", SETH.mutation, debug) === true)
    assert(SETH.isValid("c.?", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.?", SETH.mutation, debug) === true)

    // Questionable cases:
    //missing amino acids
    assert(SETH.isValid("p.556_871dup", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.264_265insSSGNL", SETH.mutation, debug) === true)
    assert(SETH.isValid("p.(550_592)ins7", SETH.mutation, debug) === true)
    //too unspecific?
    assert(SETH.isValid("p.", SETH.mutation, debug) === true)
  }

    test("FPs found by Lennart") {
    "Using the S49 T-cell lymphoma system for the study of immunodeficiency diseases, we characterized several variants in purine salvage"
    //MutationFinder extracts: MutationMention [span=10-15, mutResidue=T, location=49, wtResidue=S, text=S49 T, type=SUBSTITUTION, tool=MUTATIONFINDER]
  }
}