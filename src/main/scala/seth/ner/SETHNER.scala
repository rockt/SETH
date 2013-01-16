package seth.ner

/**
 * User: Tim Rocktaeschel
 * Date: 10/29/12
 * Time: 1:56 PM
 */

import scala.util.parsing.combinator._
import util.parsing.input.Positional
import wrapper.Type._
import wrapper.Type
import annotation.tailrec

/**
 * Scala app for command line use of SETH
 */
object SETHNERApp extends App {
  val SETH = new SETHNER
  if (args.size > 0) {
    for (sentence <- args(0).split("\n")) {
      println(sentence)
      for (mutation <- SETH.extractMutations(sentence)) println("MATCH: " + mutation)
    }
  }
  else println("Usage: java -jar seth.jar \"The remaining four variants, namely c.905T>C (rs121965091), c.1177C>T (rs1211965090), c.*156G>A (rs121965092) and c.2251-37C>A (rs2470353), were rare in the case–control analysis.\"")
}

/**
 *
 * @param start Start of the mutation mention in a text
 * @param end End of the mutation mention in a text
 * @param text Textual representation the a mutation
 */
case class Mutation(var start:Int, var end:Int, text: String) {
  //location of the mutation in the genome
  var loc: String = ""
  //reference sequence
  var ref: String = ""
  //wildtype
  var wild: String = ""
  //residue
  var mutated: String = ""
  //type of the mutation
  var typ: Type = _
  override def toString = text +
    "\n\tstart: " + start +
    "\n\tend:   " + end +
    (if(!loc.isEmpty)     "\n\tloc:   " else "") + loc +
    (if(!ref.isEmpty)     "\n\tref:   " else "") + ref +
    (if(!wild.isEmpty)    "\n\twild:  " else "") + wild +
    (if(!mutated.isEmpty) "\n\tmut:   " else "") + mutated +
    (if(typ != null)      "\n\ttype:  " else "") + typ
  def addOffset(offset: Int) {
    this.start = this.start + offset
    this.end = this.end + offset
  }
}

/**
 * Keeps track of the position of a match during parsing
 * @param t
 * @tparam T
 */
case class Span[T](t: T) extends Positional


/**
 * Represents a partial match during parsing as String rather than a parse result
 * @param parse
 */
class ParsedString(val parse: Any) extends FlattenToMutation { val string = "" }

case class Empty() extends ParsedString("")
case class RefSeqString(override val parse: Any) extends ParsedString(parse)
case class LocString(override val parse: Any) extends ParsedString(parse)
case class WildString(override val parse: Any) extends ParsedString(parse)
case class MutatedString(override val parse: Any) extends ParsedString(parse)

trait MutationType
case class SubstString(override val parse: Any) extends ParsedString(parse) with MutationType
case class DelString(override val parse: Any) extends ParsedString(parse) with MutationType
case class DupString(override val parse: Any) extends ParsedString(parse) with MutationType
case class SilentString(override val parse: Any) extends ParsedString(parse) with MutationType
case class InsString(override val parse: Any) extends ParsedString(parse) with MutationType
case class InsDelString(override val parse: Any) extends ParsedString(parse) with MutationType
case class InvString(override val parse: Any) extends ParsedString(parse) with MutationType
case class ConString(override val parse: Any) extends ParsedString(parse) with MutationType
case class TransLocString(override val parse: Any) extends ParsedString(parse) with MutationType
case class FrameShiftString(override val parse: Any) extends ParsedString(parse) with MutationType
case class VariableShortSequenceRepeatString(override val parse: Any) extends ParsedString(parse) with MutationType


//TODO: we could include more semantic constraints
/**
 * NER component of SETH
 *
 * Parses a string and yields all mentions of mutations that obey to the latest variant nomenclature of HGVS.
 */
class SETHNER extends RegexParsers with NonGreedy with Positional with FlattenToMutation with PackratParsers {
  //type P = PackratParser[Any]
  type P = Parser[Any]
  override def skipWhitespace = false

  //represents chars outside of a mutation mention
  val any = """.|\w|\n|\r""".r
  val eof = """\z""".r
  //prevent non-mutation chars from consuming mutations
  lazy val noMutation:P         = nonGreedy(any, mutation)
  //prevent non-mutation chars from consuming eof
  lazy val rest:P               = nonGreedy(any, eof)

  //some text and then a mutation
  lazy val sequence             = noMutation ~> positioned(mutation ^^ { Span(_) })

  //finds mutations by parsing the sentence
  lazy val expr                 = rep(sequence) <~ rest
  //a mutation either refers to the nucleotide or the protein sequence
  lazy val mutation:P           = Var | ProteinVar

  //EBNF akin to Laros et al. (2011)
  //DNA and RNA variant nomenclature
  //Basic lexemes
  //FIXED: added unkown base N / n
  lazy val Nt:P                 = "a" | "c" | "g" | "t" | "u" | "A" | "C" | "G" | "T" | "U" | "n" | "N"
  lazy val NtString:P           = Nt.+
  lazy val name:P               = "([a-z]|[A-Z]|[0-9])+".r
  lazy val Number:P             = "([0-9])+".r

  //Top-level Rule
  //FIXED: we do not want to extract mentions like r.0? and r.spl?
  lazy val Var:P                = SingleVar //| UnkEffectVar | NoRNAVar | SplicingVar //| MultiVar | MultiTranscriptVar

  //Locations
  lazy val Loc:P                = (UncertainLoc | RangeLoc | PtLoc) ^^ { LocString(_) }
  lazy val NoPointLoc:P         = (UncertainLoc | RangeLoc) ^^ { LocString(_) }
  //FIXED: added uncertain location
  lazy val Uncertain:P          = (Number ~ "_?".? | "?_".? ~ Number) ~ Offset.?
  lazy val UncertainPart:P      = Uncertain | "(" ~ Uncertain ~ ")"
  lazy val UncertainLoc:P       = "*".? ~ UncertainPart ~ "_" ~ "*".? ~ UncertainPart
  lazy val Offset:P             = ("+" | "-") ~ ("u" | "d").? ~ (Number | "?")
  lazy val RealPtLoc:P          = (("-" | "*").? ~ Number ~ Offset.?) | "?"
  lazy val IVSLoc:P             = "IVS" ~ Number ~ ("+" | "-") ~ Number
  lazy val PtLoc:P              = IVSLoc | RealPtLoc
  lazy val RealExtent:P         = PtLoc ~ "_".r ~ ("o".? ~ (RefSeqAcc | GeneSymbol) ~ ":").? ~ PtLoc
  lazy val EXLoc:P              = "EX" ~ Number ~ ("-" ~ Number).?
  lazy val Extent:P             = RealExtent | EXLoc
  lazy val RangeLoc:P           = Extent | "(" ~ Extent ~ ")"
  lazy val FarLoc:P             = (RefSeqAcc | GeneSymbol) ~ (":" ~ RefType.? ~ Extent).?
  //FIXED: changed exact location (.XXX) to optional
  lazy val ChromBand:P          = ("p" | "q") ~ Number ~ ("." ~ Number).?
  lazy val ChromCoords:P        = "(" ~ Chrom ~ ";" ~ Chrom ~ ")" ~ "(" ~ ChromBand ~ ";" ~ ChromBand ~ ")"

  //Reference sequences
  lazy val Ref:P                = (((RefSeqAcc | GeneSymbol) ~ ":") ~ RefType.?) |
    (((RefSeqAcc | GeneSymbol) ~ ":").? ~ RefType) | OtherRefs
  lazy val RefType:P            = ("c" | "g" | "m" | "n" | "r") ~ "."
  lazy val RefSeqAcc:P          = GenBankRef | LRG
  lazy val GenBankRef:P         = (GI | AccNo) ~ ("(" ~ GeneSymbol ~ ")").?
  lazy val GI:P                 = ("GI" ~ ":".?).? ~ Number
  lazy val AccNo:P              = "ish.".? ~ ("[a-zA-Z]".r ~ Number ~ "_").+ ~ Version.?
  lazy val Version:P            = "." ~ Number
  //FIXED: removed parenthesis
  lazy val GeneSymbol:P         = name ~ (TransVar | ProtIso).?
  lazy val TransVar:P           = "_v" ~ Number
  lazy val ProtIso:P            = "_i" ~ Number
  lazy val LRGTranscriptID:P    = "t" ~ ("[0-9]".r).+
  lazy val LRGProteinID:P       = "p" ~ ("[0-9]".r).+
  lazy val LRG:P                = "LRG" ~ ("[0-9]".r).+ ~ ("_" ~ (LRGTranscriptID | LRGProteinID)).?
  lazy val Chrom:P              = name

  //FIXED: added more reference sequences
  lazy val OtherRefs:P          = (RS | Accs) ~ ":".r.?
  lazy val NumberPointNumber:P  = "[0-9]".r.+ ~ ("\\.".r ~ "[0-9]".r.*).?
  lazy val RS:P                 = "rs" ~ "[1-9]".r.? ~ "[0-9]".r.*
  lazy val Accs:P               = (AccRefs | AccNumRefs | AccNumRefRange) ~ NumberPointNumber
  //from http://www.ncbi.nlm.nih.gov/books/NBK21091/table/ch18.T.refseq_accession_numbers_and_mole/?report=objectonly
  lazy val AccRefs:P            = ("AC" | "NC" | "NG" | "NT" | "NW" | "NS" | "NZ" | "NP" | "NM" | "NR" | "XM" | "XR") ~ "_"
  //from http://www.ncbi.nlm.nih.gov/Sequin/acc.html
  lazy val AccNumRefs:P         = ("BA | DF | DG | AN | CH | CM | DS | EM | EN | EP | EQ | FA | GG |" +
      "GL | JH | KB | C | AT | AU | AV | BB | BJ | BP | BW | BY | CI |" +
      "CJ | DA | DB | DC | DK | FS | FY | HX | HY | F | H | N | T | R |" +
      "W | AA | AI | AW | BE | BF | BG | BI | BM | BQ | BU | CA | CB |" +
      "CD | CF | CK | CN | CO | CV | CX | DN | DR | DT | DV | DW | DY |" +
      "EB | EC | EE | EG | EH | EL | ES | EV | EW | EX | EY | FC | FD |" +
      "FE | FF | FG | FK | FL | GD | GE | GH | GO | GR | GT | GW | HO |" +
      "HS | JG | JK | JZ | D | AB | V | X | Y | Z | AJ | AM | FM | FN |" +
      "HE | HF | HG | FO | U | AF | AY | DQ | EF | EU | FJ | GQ | GU |" +
      "HM | HQ | JF | JN | JQ | JX | AP | BS | AL | BX | CR | CT | CU |" +
      "FP | FQ | FR | AE | CP | CY | AG | DE | DH | FT | GA | B | AQ |" +
      "AZ | BH | BZ | CC | CE | CG | CL | CW | CZ | DU | DX | ED | EI |" +
      "EJ | EK | ER | ET | FH | FI | GS | HN | HR | JJ | JM | JS | JY |" +
      "AK | AC | DP | E | BD | DD | DI | DJ | DL | DM | FU | FV | FW |" +
      "FZ | GB | HV | HW | A | AX | CQ | CS | FB | GM | GN | HA | HB |" +
      "HC | HD | HH | HI | JA | JB | JC | JD | JE | I | AR | DZ | EA |" +
      "GC | GP | GV | GX | GY | GZ | HJ | HK | HL | G | BV | GF | BR |" +
      "BN | BK | HT | HU | BL | GJ | GK | EZ | HP | JI | JL | JO | JP |" +
      "JR | JT | JU | JV | JW | KA | FX | S | AD | AH | AS | BC | BT |" +
      "J | K | L | M | N").r
  lazy val AccNumRefRange:P     = "([A-E]|G)[A-Z][A-Z][A-Z]".r | "A[A-Z][A-Z][A-Z][A-Z]".r

  //Single Variations
  lazy val Subst:P              = ((RangeLoc | PtLoc).? ^^ { LocString(_) }) ~ (Nt.+ ^^ { WildString(_) }) ~
    (">" ^^ { SubstString(_) }) ~ (Nt.+ ^^ { MutatedString(_) })
  lazy val Del:P                = (Loc ^^ { LocString(_) }) ~ ("del" ^^ { DelString(_) }) ~
    ((Nt.+ ^^ { WildString(_) })| Number).?
  lazy val Dup:P                = Loc ~ ("dup" ^^ { DupString(_) }) ~ ((Nt.+ ^^ { MutatedString(_) })| Number).? //~ Nest.?
  //FIXME: really? this does not accept 7(TG)3_6
  lazy val AbrSSR:P             = (PtLoc ^^ { LocString(_) }) ~ (Nt.+ ^^ { MutatedString(_) }) ~ "(" ~ Number ~ "_" ~ Number ~ ")"
  lazy val VarSSR:P             = ((PtLoc ^^ { LocString(_) }) ~ (Nt.+ ^^ { MutatedString(_) }) ~ "[" ~ Number ~ "]") |
    (RangeLoc ~ "[" ~ Number ~ "]") | AbrSSR
  lazy val Ins:P                = (NoPointLoc ^^ { LocString(_) }) ~ ("ins" ^^ { InsString(_) }) ~
    (Nt.+ ^^ { MutatedString(_) } | Number | RangeLoc | FarLoc) //~ Nest.?
  lazy val Indel:P              = (NoPointLoc ^^ { LocString(_) }) ~ "del" ~ (Nt.+ ^^ { WildString(_) } | Number).? ~
    ("ins" ^^ { InsDelString(_) }) ~ ((Nt.+ ^^ { MutatedString(_) }) | Number | RangeLoc | FarLoc) //~ Nest.?
  lazy val Inv:P                = (NoPointLoc ^^ { LocString(_) }) ~ ("inv" ^^ { InvString(_) }) ~ (Nt.+ ^^ { WildString(_) } | Number).? //~ Nest.?
  lazy val Conv:P               = (NoPointLoc ^^ { LocString(_) }) ~ ("con" ^^ { ConString(_) }) ~ FarLoc //~ Nest.?
  lazy val TransLoc:P           = ("t" ^^ { TransLocString(_) }) ~ ChromCoords ~ "(" ~ FarLoc ~ ")"
  lazy val RawVar:P             = Indel | Subst | Del | Dup | (VarSSR ^^ { VariableShortSequenceRepeatString(_) }) | Ins | Inv | Conv
  lazy val SingleVar:P          = ((Ref ~ RefType.?) ^^ { RefSeqString(_) }) ~ ("(" ~ RawVar ~ ")" | RawVar) | TransLoc
  lazy val ExtendedRawVar:P     = RawVar ~ ("=" | "?") //FIXED: RawVar is now really extended
  lazy val UnkEffectVar:P       = Ref ~ ("(=)" | "?")
  lazy val SplicingVar:P        = Ref ~ ("spl?" | "(spl?)")
  lazy val NoRNAVar:P           = Ref ~ "0" ~ "?".?



  //Protein variant nomenclature
  //Basic lexemes
  lazy val AA1:P                = "A" | "R" | "N" | "D" | "C" | "Q" | "E" | "G" | "H" | "I" | "L" | "K" | "M" | "F" |
    "P" | "S" | "T" | "W" | "Y" | "V"
  lazy val AA3:P                = "Ala" | "Arg" | "Asn" | "Asp" | "Cys" | "Gln" | "Glu" | "Gly" | "His" | "Ile" |
    "Leu" | "Lys" | "Met" | "Phe" | "Pro" | "Ser" | "Thr" | "Trp" | "Tyr" | "Val" |
  //FIXED: added termination, stop codons and ambiguous amino acids
    "Ter" | "Sec" | "Pyl" | "Asx" | "Glx" | "Xle" | "Xaa"

  //FIXED: added '*' and '?'
  lazy val AA:P                 = AA3 | AA1 | "X" | "*" | "?"
  lazy val Name:P               = "[a-zA-Z0-9_\\.]".r.+

  //Top-level Rule
  lazy val ProteinVar:P         = ProteinSingleVar

  //Locations
  lazy val AALoc:P              = (AAPtLoc ~ ("_" ~ AAPtLoc).?) ^^ { LocString(_) } //| AARange
  lazy val AAPtLoc:P            = (AA ^^ { WildString(_) }) ~ (ProteinPtLoc ^^ { LocString(_) })
  lazy val ProteinPtLoc:P       = ("-"|"*").? ~ Number | Number ~ ("+" | "-") ~ Number

  //Reference sequences
  lazy val ProteinRef:P         = ((Name ~ ":").?  ~ "p.") ^^ { RefSeqString(_) }

  //Single Variations
  lazy val ProteinSingleVar:P   = ProteinRef ~ ("(" ~ ProteinRawVar ~ ")" | ProteinRawVar)
  //FIXED: we don't want to extract =, 0 and ?
  lazy val ProteinRawVar:P      = ProteinIndel | ProteinDel | ProteinDup | ProteinFrameShift |
    (ProteinSubst ^^ { SubstString(_) }) | (ProteinVarSSR ^^ { VariableShortSequenceRepeatString(_) }) |
    ProteinIns | ProteinEq
    // | "=" | "?" | "0" | "0?"
  lazy val ProteinSubst:P       = ((AA ^^ { WildString(_) }) ~ (Number ^^ { LocString(_) }) ~ (AA ^^ { MutatedString(_) })) |
      (AAPtLoc ~ (AA  ^^ { MutatedString(_) }) ~ ("extX" ~ "*".? ~ Number).? | ("Met1" | "M1") ~ ("?" | "ext" ~ Number))
  lazy val ProteinDel:P         = AALoc ~ ("del" ^^ { DelString(_) })
  lazy val ProteinDup:P         = AALoc ~ ("dup" ^^ { DupString(_) })
  lazy val ProteinEq:P          = AALoc ~ ("=" ^^ { SilentString(_) })
  lazy val ProteinVarSSR:P      = AALoc ~ "(" ~ Number ~ "_" ~ Number ~ ")"
  lazy val ProteinIns:P         = AALoc ~ ("ins" ^^ { InsString(_) }) ~ ((AA.+) ^^ { MutatedString(_) } | Number)
  lazy val ProteinIndel:P       = AALoc ~ ("delins" ^^ { InsDelString(_) }) ~ ((AA.+ ^^ { MutatedString(_) }) | Number)
  lazy val ProteinFrameShift:P  = LongFS | ShortFS | SubstFS
  lazy val ShortFS:P            = (AAPtLoc ^^ { LocString(_) })~ ("fs" ^^ { FrameShiftString(_) })
  //FIXED: added "*" and RangeLoc
  lazy val LongFS:P             = ((AAPtLoc ~ AA.?) ^^ { LocString(_) }) ~ ("fs" ^^ { FrameShiftString(_) }) ~ ("X" | "*") ~ Number
  //FIXED: added other forms of Frame Shifts
  lazy val SubstFS:P            = ((AA ^^ { WildString(_) }) ~ (Number ^^ { LocString(_) }) ~ "ext" ~ "*".? ~
    ((AA.+ ~ ("-" ~ AA.+).?) ^^ { MutatedString(_) })) ~
    ("fs" ^^ { FrameShiftString(_) })

  /**
   * Parses the input string and extracts all mutation mentions
   * @param input The string from which mutation mentions should be extracted
   * @return A list of spans for mutation mention matches
   */
  def apply(input: String) = //parse(expr, new PackratReader(new CharSequenceReader(input))) match {
    parse(expr, input) match {
      case Success(result, next) => result
      case failure: NoSuccess => List()
    }

  def extractMutations(text: String):List[Mutation] = {
    //tokenize the text using whitespace tokenization
    val spacePos = 0 :: (for (i <- 0 until text.length) yield {
      val ch = text.charAt(i).toString
      if (ch matches ("\\s")) i+1 else 0
    }).toList.filter(_ != 0) ++ List(text.length)

    //calculate the offset of all words
    @tailrec
    def getWords(acc: List[(Int, String)], pos: List[Int]):List[(Int,String)] = pos match {
      case Nil => acc
      case x::Nil => acc
      case x::y::xs => getWords(
        //do not keep words longer than 500 characters
        if (text.substring(x,y).length < 500) (x, text.substring(x,y)) :: acc else acc,
        y :: xs
      )
    }
    val words = getWords(Nil, spacePos).reverse

    //allowed chars left and right to a mutation (from MutationFinder)
    lazy val left = """(?:^|[\s\(\[\'"/,\-])"""
    lazy val right = """(?=([.,\s)\]\'":;\-?!/]|$))"""

    //extract mutations from words
    (for ((offset, word) <- words; parse <- this(word)) yield {
      val mutation = flattenToMutation(parse)
      mutation.addOffset(offset)
      mutation
    }).filter((m:Mutation) => {
      //only keep matches with a valid boundary
      val lChar = (if (m.start - 1 >= 0) text.charAt(m.start - 1) else " ").toString
      val rChar = (if (m.end < text.length) text.charAt(m.end) else " ").toString
      !(lChar.matches(left) && rChar.matches(right))
    }).filter((m:Mutation) => {
      !(m.loc.isEmpty && m.ref.endsWith(":") && m.ref.length < 3) &&
      !(m.ref matches("[0-9]+:"))
    })
  }

  def isValid(input: String, parser:this.Parser[Any]): Boolean = {
    val result = parseAll(parser, input) match {
      case Success(result,_) => result
      case failure: NoSuccess => List()
    }
    result match {
      case List() => false
      case _ => true
    }
  }

  def isValid(input:String): Boolean = isValid(input, mutation)
}

//from http://stackoverflow.com/questions/7812610/non-greedy-matching-in-scala-regexparsers
trait NonGreedy extends Parsers {
  def nonGreedy[T, U](rep: => Parser[T], terminal: => Parser[U]) = Parser { in =>
    def recurse(in: Input, elems: List[T]): ParseResult[List[T]] =
      terminal(in) match {
        case _: Success[_] => Success(elems.reverse, in)
        case _ =>
          rep(in) match {
            case Success(x, rest) => recurse(rest, x :: elems)
            case ns: NoSuccess => ns
          }
      }
    recurse(in, Nil)
  }
}

trait FlattenToString extends Parsers {
  def flattenToString(any: Any): String = any match {
    case a ~ b => flattenToString(a) + flattenToString(b)
    case xs:List[Any] => xs.foldLeft("")(flattenToString(_) + flattenToString(_))
    case None => ""
    case Some(a) => flattenToString(a)
    case a:String => a
    case Span(span) => flattenToString(span)
    case s:ParsedString => flattenToString(s.parse) //s.string
    case _ => throw new IllegalArgumentException("could not flatten " + any)
  }
}

/**
 * Flattens a parse result to mutation mention
 */
trait FlattenToMutation extends FlattenToString {
  //TODO: actually, we only need a list of ParsedStrings
  private def flatten2(any: Any): List[(String, ParsedString)] = {
    def flatten3(any: Any): List[(String, ParsedString)] = any match {
      case a ~ b => flatten2(a) ++ flatten2(b)
      case xs:List[Any] => xs.foldLeft(List(("", Empty().asInstanceOf[ParsedString])))(_ ++ flatten2(_))
      case None => List(("", Empty()))
      case Some(a) => flatten2(a)
      case a:String => List((a, Empty()))
      case Span(span) => flatten2(span)
      case s:ParsedString => List((flattenToString(s.parse), s)) ++ flatten2(s.parse)
      case _ => throw new IllegalArgumentException("could not flatten " + any)
    }
    flatten3(any).filter(!_._1.isEmpty)
  }

  def flattenToMutation(span: Span[Any]): Mutation = {
    val text = flattenToString(span.t)
    val start = span.pos.column - 1
    val end = start + text.length
    val mutation = new Mutation(start, end, text)
    val attributes = flatten2(span.t).map(_._2).filter(_ != Empty())
    for (att <- attributes) att match {
      case loc:LocString => {
        val temp = flattenToString(loc.parse)
        mutation.loc = if (temp.head == '(' && temp.last == ')') temp.tail.dropRight(1) else temp
      }
      //TODO: map to single char representation
      case wild:WildString => mutation.wild = abbreviate(flattenToString(wild.parse))
      //TODO: map to single char representation
      case mutated:MutatedString => mutation.mutated = abbreviate(flattenToString(mutated.parse))
      case ref:RefSeqString => mutation.ref = flattenToString(ref.parse)
      case typ:DelString => mutation.typ = DELETION
      case typ:SubstString => mutation.typ = SUBSTITUTION
      case typ:InsString => mutation.typ = INSERTION
      case typ:DupString => mutation.typ = DUPLICATION
      case typ:InsDelString => mutation.typ = DELETION_INSERTION
      case typ:InvString => mutation.typ = INVERSION
      case typ:ConString => mutation.typ = CONVERSION
      case typ:TransLocString => mutation.typ = TRANSLOCATION
      case typ:FrameShiftString => mutation.typ = FRAMESHIFT
      case typ:SilentString => mutation.typ = SILENT
      case typ:VariableShortSequenceRepeatString => mutation.typ = SHORT_SEQUENCE_REPEAT
      case typ:MutationType => mutation.typ = OTHER //TODO: add more types
    }
    mutation
  }

  private val aminoAcidMap = Map(
    "Ala" -> "A",
    "Arg" -> "R",
    "Asn" -> "N",
    "Asp" -> "D",
    "Cys" -> "C",
    "Gln" -> "Q",
    "Glu" -> "E",
    "Gly" -> "G",
    "His" -> "H",
    "Ile" -> "I",
    "Leu" -> "L",
    "Lys" -> "K",
    "Met" -> "M",
    "Phe" -> "F",
    "Pro" -> "P",
    "Ser" -> "S",
    "Thr" -> "T",
    "Trp" -> "W",
    "Tyr" -> "Y",
    "Val" -> "V",
    "Ter" -> "X",
    "Sec" -> "U",
    "Pyl" -> "O",
    "Asx" -> "B",
    "Glx" -> "Z",
    "Xle" -> "J",
    "Xaa" -> "X"
  )

  private def abbreviate(aminoSeq: String): String = {
    (for (a <- aminoSeq.grouped(3)) yield {
      val tmp = aminoAcidMap.get(a).getOrElse("")
      if (tmp.isEmpty) return aminoSeq
      tmp
    }).mkString("")
  }
}