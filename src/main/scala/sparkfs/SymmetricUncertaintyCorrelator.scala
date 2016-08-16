package sparkfs

import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.regression.LabeledPoint

import scala.math.log

// sameFeature represents default value for two indentical features
abstract class Correlator(data: RDD[LabeledPoint], val sameFeature: Double)
  extends java.io.Serializable {
  def correlate(featA: Int, featB: Int): Double
}


// Received data must be discretized
class SymmetricUncertaintyCorrelator(data: RDD[LabeledPoint]) 
  extends Correlator(data, sameFeature=1.0) {

  private val nInstances: Long = data.count
  // Here nFeats includes the class
  private val nFeats = data.take(1)(0).features.size + 1
  // By convention it is considered that class is stored after the last feature
  private val iClass: Int = nFeats - 1
  private val entropies: IndexedSeq[Double] = (0 until nFeats).map(entropy)

  //DEBUG
  println("ENTROPIES=")
  entropies.foreach(println)

  private def entropy(iFeat: Int): Double = {

    data
      .map { 
        lp => {
          // Common feature or class
          val feat = if (iFeat < iClass) lp.features(iFeat) else lp.label
          (feat, 1.0) 
        }
      }
      .reduceByKey(_+_)
      // Since Symm Uncer is normalized the information units are not
      // important, so the log can be base e.
      .map { case (k,v) => v * log(v) }
      .sum*(-1.0/nInstances) + log(nInstances)
  }

  private def conditionalEntropy(iConditionedFeat: Int, iFeat: Int): Double = {
    
    assert(iConditionedFeat != iFeat, 
      "Trying to evaluate a conditional entropy of a feature with itself")

    data
      .map { 
        lp => {
          val conditionedFeat = 
            if (iConditionedFeat < iClass) lp.features(iConditionedFeat)
            else lp.label
          val feat =
            if (iFeat < iClass) lp.features(iFeat)
            else lp.label

          ((feat, conditionedFeat), 1.0)
        }
      }
      .reduceByKey(_+_)
      .map { case (k,v) => v * log(v) }
      .sum*(-1.0/nInstances) - entropy(iFeat) + log(nInstances)
  }

  override def correlate(iFeatA: Int, iFeatB: Int): Double = {
    
    val infoGain = entropies(iFeatA) - conditionalEntropy(iFeatA, iFeatB)

    2.0 * (infoGain / (entropies(iFeatA) + entropies(iFeatB)))
  }
}