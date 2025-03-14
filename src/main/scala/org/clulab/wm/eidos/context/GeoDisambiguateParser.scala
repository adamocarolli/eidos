package org.clulab.wm.eidos.context

import ai.lum.common.ConfigUtils._
import com.typesafe.config.Config
import org.apache.commons.io.IOUtils
import org.tensorflow.{Graph, Session, Tensor}
import org.clulab.wm.eidos.utils.Closer.AutoCloser
import org.clulab.wm.eidos.utils.Sourcer

object GeoDisambiguateParser {
  val I_LOC = 0
  val B_LOC = 1
  val O_LOC = 2
  val PADDING_TOKEN = "PADDING_TOKEN"
  val UNKNOWN_TOKEN = "UNKNOWN_TOKEN"
  val TF_INPUT = "words_input"
  val TF_OUTPUT = "time_distributed_1/Reshape_1"

  def fromConfig(config: Config): GeoDisambiguateParser = {
    val   geoNormModelPath: String = config[String]("geoNormModelPath")
    val    geoWord2IdxPath: String = config[String]("geoWord2IdxPath")
    val      geoLoc2IdPath: String = config[String]("geoLoc2IdPath")
    new GeoDisambiguateParser(geoNormModelPath, geoWord2IdxPath, geoLoc2IdPath)
  }
}

class GeoDisambiguateParser(geoNormModelPath: String, word2IdxPath: String, loc2geonameIDPath: String) {
  val network: Session = GeoDisambiguateParser.getClass.getResourceAsStream(geoNormModelPath).autoClose { modelStream =>
    val graph = new Graph
    graph.importGraphDef(IOUtils.toByteArray(modelStream))
    new org.tensorflow.Session(graph)
  }

  lazy private val word2int: Map[String, Int] = readDict(word2IdxPath)
  lazy private val unknownInt = word2int(GeoDisambiguateParser.UNKNOWN_TOKEN)
  lazy private val paddingInt = word2int(GeoDisambiguateParser.PADDING_TOKEN)
  // provide path of geoname dict file having geonameID with max population
  lazy private val loc2geonameID: Map[String, Int] = readDict(loc2geonameIDPath)

  protected def readDict(dictPath: String): Map[String, Int] = {
    (Sourcer.sourceFromResource(dictPath)).autoClose { source =>
      source.getLines.map { line =>
        val words = line.split(' ')

        (words(0).toString -> words(1).toInt)
      }.toMap
    }
  }

  /**
    * Finds locations in a document.
    *
    * @param sentenceWords A series of sentences, where each sentence is an array of word Strings.
    * @return For each input sentence, a list of locations, where each location is a begin word,
    *         and end word (exclusive), and a GeoNames ID (when one was found).
    */
  def findLocations(sentenceWords: Array[Array[String]]): Array[Seq[(Int, Int, Option[Int])]] = if (sentenceWords.isEmpty) Array.empty else {
    // get the word-level features, and pad to the maximum sentence length
    val maxLength = sentenceWords.map(_.length).max
    val sentenceFeatures = for (words <- sentenceWords) yield {
      words.map(word2int.getOrElse(_, this.unknownInt)).padTo(maxLength, this.paddingInt)
    }

    // feed the word features through the Tensorflow model
    import GeoDisambiguateParser.{TF_INPUT, TF_OUTPUT}
    val input = Tensor.create(sentenceFeatures)
    val Array(output: Tensor[_]) = this.network.runner().feed(TF_INPUT, input).fetch(TF_OUTPUT).run().toArray

    // convert word-level probability distributions to word-level class predictions
    val Array(nSentences, nWords, nClasses) = output.shape
    val sentenceWordProbs = Array.ofDim[Float](nSentences.toInt, nWords.toInt, nClasses.toInt)
    output.copyTo(sentenceWordProbs)
    val sentenceWordPredictions: Array[Seq[Int]] = sentenceWordProbs.map(_.map{ wordProbs =>
      val max = wordProbs.max
      wordProbs.indexWhere(_ == max)
    }.toSeq)

    // convert word-level class predictions into span-level geoname predictions
    import GeoDisambiguateParser.{B_LOC, I_LOC, O_LOC}
    for ((words, paddedWordPredictions) <- sentenceWords zip sentenceWordPredictions) yield {
      // trim off any predictions on padding words
      val wordPredictions = paddedWordPredictions.take(words.length)
      for {
        (wordPrediction, wordIndex) <- wordPredictions.zipWithIndex

        // a start is either a B, or an I that is following an O
        if wordPrediction == B_LOC || (wordPrediction == I_LOC &&
          // an I at the beginning of a sentence is not considered to be a start,
          // since as of Jun 2019, such tags seemed to be mostly errors (non-locations)
          wordIndex != 0 && wordPredictions(wordIndex - 1) == O_LOC)
      } yield {

        // the end index is the first B or O (i.e., non-I) following the start
        val end = wordPredictions.indices.indexWhere(wordPredictions(_) != I_LOC, wordIndex + 1)
        val endIndex = if (end == -1) words.length else end

        // look up location phrase in GeoName dict (only containing most populous choices)
        val locationKey = words.slice(wordIndex, endIndex).mkString("_").toLowerCase
        val geoNameID = loc2geonameID.get(locationKey)
        (wordIndex, endIndex, geoNameID)
      }
    }
  }
}
