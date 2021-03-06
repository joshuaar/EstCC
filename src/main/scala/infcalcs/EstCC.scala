package infcalcs

import EstimateCC.{ uniWeight, biWeight, getResultsMult, calcWithWeightsMult }
import CTBuild.getBinDelims
import IOFile.loadPairList
import TreeDef.Tree
import EstimateMI.genEstimatesMult

object EstCC extends App with InfConfig {

  // three arguments required: file, column for signal values, column for response values
  val inF = args(0)
  val col1: Int = args(1).toInt
  val col2: Int = args(2).toInt

  // use above parameters to extract data from file
  val p = loadPairList(inF, (col1, col2))

  // build list of weight pairs (unimodal and bimodal) given a list of bin sizes specified in 'InfConfig.scala'
  val w: List[Pair[List[Weight]]] = {
    val signalBins: List[Int] = bins.unzip._1.distinct
    val sBoundList: List[Tree] = signalBins map (x => getBinDelims(p._1, x))
    sBoundList map (x => (uniWeight(x)(p), biWeight(x)(p)))
  }

  // split unimodal and bimodal weight lists
  val uw: List[List[Weight]] = w map (_._1)
  val bw: List[List[Weight]] = w map (_._2)

  // function to add string to an original string 
  def addLabel(s: Option[String], l: String): Option[String] = s flatMap (x => Some(x ++ l))

  // calculate estimated mutual information values given calculated weights
  // also outputs mutual information estimates per bin number for post hoc analysis
  val ccMult =
    ((for (n <- 0 until w.length) yield {
      List(getResultsMult(calcWithWeightsMult(uw(n), p), addLabel(outF, "_u_s" + bins.unzip._1.distinct(n))),
        getResultsMult(calcWithWeightsMult(bw(n), p), addLabel(outF, "_b_s" + bins.unzip._1.distinct(n)))).max
    }) :+ getResultsMult(List(genEstimatesMult(p, bins)), addLabel(outF, "_n"))).max

  // print estimated channel capacity to stdout
  println(ccMult)
}
