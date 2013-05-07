package seth

import ner.{FlattenToMutation, NonGreedy}
import scala.util.parsing.combinator._
import util.parsing.input.Positional

/**
 * User: rockt
 * Date: 11/8/12
 * Time: 12:58 PM
 */


object ParserPrototype extends App with RegexParsers with NonGreedy with Positional with FlattenToMutation with PackratParsers {
  def foo = log("foo" | "fo")("foo")
  def obar = log("obar")("obar")

  def foobar = log(foo ~ obar)("foobar")


  def aa = log(aab | aac)("aa")
  def aab = log("aa" ~ "b")("aab")
  def aac = log("aa" ~ "c")("aac")

  //parseAll(aa, "aab")

  def ab = "a" ~ "b"
  val res = parse(ab, "ab")
  println(res)

  res.get match {
    case a~b => println(a)
    case _ => println("unexpected")
  }

  println("output:")


  object CombinatorParser extends RegexParsers {
    lazy val a = "a"
    lazy val b = "b"
    lazy val c = "c"
    lazy val content = a ~ b ~ c
  }

  def flatten(res: Any): List[String] = res match {
    case x ~ y => flatten(x) ::: flatten(y)
    case None => Nil
    case Some(x) => flatten(x)
    case x:String => List(x)
  }

  val testChar = "abc"
  val output = CombinatorParser.parseAll(CombinatorParser.content, testChar).getOrElse(None)
  println(flatten(output))
}

object ConstructorPrototype extends App {
  case class Test(a: String, var b: String = "")
  println(Test("a1"))
  println(Test("a1", "b1"))



  val dbSNPPattern = "rs[1-9][0-9]+".r
  def dbSNP(s: String) = dbSNPPattern.findFirstIn(s).getOrElse("")

  println(dbSNP("""<feature type="sequence variant" id="VAR_027897" description="(in dbSNP:rs1248696)" evidence="1 9 10 11">"""))
}