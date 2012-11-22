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
  if (args.length != 3) {
    println("Usage: uniprotxml idmapping output")
    System.exit(-1)
  }

  val mapping = new mutable.HashMap[String, String]()
  val writer = new BufferedWriter(new FileWriter(args(2)))
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

  line = uniprotReader readLine()
  var ids = List[String]()
  var offsets = mutable.Map[String,String]()
  var typ = ""
  var pos = ""
  while (line != null) {
    counter ++;
    line = line.trim
    if (line.startsWith("<accession>")) ids = line.substring(11, line.length-12) :: ids
    else if (line.startsWith("<feature type")) typ = line.split("\"")(1)
    else if (line.startsWith("<position")) {
      pos = line.split("\"")(1)
      offsets += typ -> pos
    }
    else if (line.startsWith("<begin position")) {
      pos = line.split("\"")(1)
      offsets += typ -> pos
    }
    else if (line.startsWith("<end position")) {
      pos = pos + "-" + line.split("\"")(1)
      offsets += typ -> pos
    }
    else if (line.startsWith("</entry>")) {
      for {
        id <- ids
        (typ, pos) <- offsets
      } if (mapping.contains(id)) writer.write("%s\t%s\t%s\n".format(mapping(id), typ, pos))
      ids = Nil
      offsets = mutable.Map[String,String]()
    }
    line = uniprotReader.readLine()
  }

  uniprotReader.close()
  writer.close()
}

