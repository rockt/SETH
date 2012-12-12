package seth

import io.Source
import java.io.{FileWriter, BufferedWriter, FileInputStream}
import collection.mutable
import java.util.zip.GZIPInputStream

/**
 * User: Tim Rocktaeschel
 * Date: 11/15/12
 * Time: 2:09 PM
 */

class Counter(printStep: Int) {
  var count = 0
  def ++():Unit = {
    count = count + 1
    if (count % printStep == 0) report(count)
  }
  def report(i: Int) {
    print(count + "\r")
  }
  def reset() = count = 0
}

class CounterWithMax(printStep: Int, max: Int) extends Counter(printStep) {
  override def report(i: Int) {
    print(count + "/" + max + "\r")
  }
}

object Uniprot2Tab extends App {
  if (args.length != 4) {
    println("Usage: uniprotxml idmapping outputmapping outputdbsnp")
    System.exit(-1)
  }

  val mapping = new mutable.HashMap[String, String]()
  val mappingWriter = new BufferedWriter(new FileWriter(args(2)))
  val dbSNPWriter = new BufferedWriter(new FileWriter(args(3)))
  val counter = new Counter(100000)

  println("Reading ID mapping...")
  //loading mapping from uniprot to entrezgene
  val idReader = Source fromInputStream(new GZIPInputStream(new FileInputStream(args(1)))) bufferedReader()
  var line = idReader readLine()
  while (line != null) {
    counter ++;
    val splits = line split("\t")
    if (splits(1) == "GeneID") mapping += splits(0) -> splits(2)
    line = idReader readLine()
  }
  idReader close()
  counter.reset()

  println("Reading Uniprot...")
  //fetching all offset data of genes in uniprot
  val uniprotReader = Source fromInputStream (new GZIPInputStream(new FileInputStream(args(0)))) bufferedReader()

  case class Feature(typ: String,
                     var desc: String = "",
                     var pos: String = "",
                     var residue: String = "",
                     var wildtype: String = "") {
    def complete = typ == "sequence variant" && !(dbSNP.isEmpty | pos.isEmpty | residue.isEmpty | wildtype.isEmpty)


    val dbSNPPattern = "rs[1-9][0-9]+".r
    lazy val dbSNP = if (desc.contains("dbSNP:rs")) dbSNPPattern.findFirstIn(desc).get.drop(2) else ""
  }


  line = uniprotReader readLine()
  var ids = List[String]()
  var features = List[Feature]()
  var typ = ""
  var pos = ""
  var lastFeature: Feature = _
  while (line != null) {
    counter ++;
    line = line.trim
    if (line.startsWith("<accession>")) ids = line.substring(11, line.length-12) :: ids
    else if (line.startsWith("<feature type")) {
      val splits = line.split("\"")
      lastFeature = if (splits.size > 5) Feature(splits(1), splits(5)) else Feature(splits(1))
    }
    else if (line.startsWith("<original>")) lastFeature.wildtype = line.substring(10, line.length-11)
    else if (line.startsWith("<variation>")) lastFeature.residue = line.substring(11, line.length-12)
    else if (line.startsWith("<position")) lastFeature.pos = line.split("\"")(1)
    else if (line.startsWith("<begin position")) {
      pos = line.split("\"")(1)
      lastFeature.pos = line.split("\"")(1)
    }
    else if (line.startsWith("<end position")) lastFeature.pos = pos + "-" + line.split("\"")(1)
    else if (line.startsWith("</feature")) features = lastFeature :: features
    else if (line.startsWith("</entry>")) {
      for {
        id <- ids
        feature <- features
      } if (mapping.contains(id)) {
        mappingWriter.write("%s\t%s\t%s\n".format(mapping(id), feature.typ, feature.pos))
        if (feature.complete)
          dbSNPWriter.write("%s\t%s\t%s\t%s\t%s\n".format(
            feature.dbSNP, mapping(id), feature.pos, feature.residue, feature.wildtype)
          )
      }
      ids = Nil
      features = Nil
    }
    line = uniprotReader.readLine()
  }

  uniprotReader.close()
  mappingWriter.close()
  dbSNPWriter.close()
}

