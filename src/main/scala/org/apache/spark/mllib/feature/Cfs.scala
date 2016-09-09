package org.apache.spark.mllib.feature

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.regression.LabeledPoint


import scala.collection.immutable.BitSet
import scala.collection.mutable.WeakHashMap
import scala.math.sqrt

case class FeaturesSubset(val feats: BitSet, nFeats: Int) 
  extends SearchState {
  // Returns an indexed sequence of all possible new subsets with one more feat
  override def expand: IndexedSeq[SearchState] = {
    (0 until nFeats)
      .filter { !this.feats.contains(_) } 
      .map { f => FeaturesSubset(this.feats + f, nFeats) }
  }
}

class EvaluatedFeaturesSubset(featsSubset: FeaturesSubset, merit: Double) 
  extends EvaluatedSearchState(featsSubset, merit) {
  
  // override def toString(): String = {
  //    "Feats: " + featsSubset.feats.toString + "\n" +
  //    "Merit: " + merit.toString
  // }  
}
   

class CfsSubsetEvaluator(correlations: CorrelationsMatrix) 
  extends SearchStateEvaluator {

  // A WeakHashMap does not creates strong references, so its elements
  // can be garbage collected if there are no other references to it than this,
  // in the case of BestFirstSearch, the subsets are stored in the queue
  var cache: WeakHashMap[FeaturesSubset,EvaluatedSearchState] = WeakHashMap()

  // Evals a given subset of features
  override def evaluate(state: SearchState): EvaluatedSearchState = {

    val subset = state.asInstanceOf[FeaturesSubset]

    if(cache.contains(subset)){
      cache(subset)
    }else{
      // We are applying a simplified version of the heuristic formula
      val iClass = correlations.nFeats - 1
      val numerator = subset.feats.map(correlations(_,iClass)).sum
      val interFeatCorrelations = 
        subset.feats.toSeq.combinations(2)
          .map{ e => correlations(e(0), e(1)) }.sum

      // DEBUG
      // val k = subset.feats.size
      // println("k*(k-1)/2 = " + (k*(k-1.0)/2.0))
      // println("combinations size= " + subset.feats.toSeq.combinations(2).size)

      val denominator = sqrt(subset.feats.size + 2.0 * interFeatCorrelations)

      // TODO Check if this is really needed
      // Take care of aproximations problems and return EvaluatedState
      // if (denominator == 0.0) {
      //   new EvaluatedSearchState(state, Double.NegativeInfinity)
      // } else if (numerator/denominator < 0.0) {
      //   new EvaluatedSearchState(state, -numerator/denominator)
      // } else {
      //   new EvaluatedSearchState(state, numerator/denominator)
      // }
      
      val evaluated = new EvaluatedSearchState(state, numerator/denominator)
      cache(subset) = evaluated
      
      evaluated
    }
  }
}

// data must be cached and discretized
class CfsFeatureSelector(data: RDD[LabeledPoint], sc: SparkContext) {

  private val nFeats = data.take(1)(0).features.size
  // By convention it is considered that class is stored after the last feature
  // in the correlations matrix
  private val iClass = nFeats

  // TODO This values could be selected according to params sent to this
  // method
  private val correlations = 
    new CorrelationsMatrix(
      data = data, 
      sc = sc,
      correlator = new SymmetricUncertaintyCorrelator(data),
      distributedStorage = false)
  

  // Searches a subset of features given the data
  // Returns a BitSet containing the selected features
  def searchFeaturesSubset(addLocalFeats:Boolean): BitSet = {

    // DEBUG
    println("CORRELATIONS MATRIX=")
    (0 to nFeats).map {
      fA => {
        println( (0 to nFeats).map(fB => "%1.4f".format(correlations(fA,fB))) )
      }
    }

    val subsetEvaluator = new CfsSubsetEvaluator(correlations)

    val searcher = 
      new BestFirstSearcher(
        evaluator = subsetEvaluator,
        maxFails = 3)

    // END TODO

    val subset: BitSet = searcher.search(
      initState = new FeaturesSubset(BitSet(), nFeats))
    match { case FeaturesSubset(feats, nFeats) => feats }

    // Add locally predictive feats is requested
    if (addLocalFeats) {
      // Descending ordered remaning feats according to their correlation with
      // the class
      val remaningFeats: Seq[Int] = 
        (0 until nFeats)
          .filter(!subset.contains(_))
          .sortWith{
            (a, b) => correlations(a, iClass) > correlations(b, iClass)}
      
      addLocallyPredictiveFeats(subset, remaningFeats)
    // Do not add locallu predictive feats
    } else {
      subset
    }
  }

  // Receives the actual subset of selected feats and the remaning feats in
  // descending order according to their correlation to the class
  private def addLocallyPredictiveFeats(
    selectedFeats: BitSet, 
    orderedCandFeats: Seq[Int]): BitSet = {

    if (orderedCandFeats.isEmpty){
      selectedFeats
    } else {
      // Check if next candidate feat is more correlated to the class than
      // to any of the selected feats
      val candFeat = orderedCandFeats.head
      val candFeatClassCorr = correlations(candFeat,iClass)
      val tempSubset = 
        selectedFeats
          .filter { f => (correlations(f,candFeat) > candFeatClassCorr) }
      // Add feat to the selected set
      if(tempSubset.isEmpty){
        addLocallyPredictiveFeats(
          selectedFeats + candFeat, orderedCandFeats.tail)
      // Ignore feat
      } else {
        addLocallyPredictiveFeats(selectedFeats, orderedCandFeats.tail)
      }
    }
  }


}
