package org.apache.spark.mllib.feature

import org.apache.spark.SparkContext
import org.apache.spark.Partitioner
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.regression.LabeledPoint

import scala.math.round

// Creates a correlations matrix for all features and class from dataset (RDD)
// Stores correlations either on an RDD or in a local matrix
class CorrelationsMatrix(
  data: RDD[LabeledPoint],
  sc: SparkContext,
  correlator: Correlator,
  distributedStorage: Boolean) extends java.io.Serializable {

  // nPartitions must be >= 1
  class CorrelationsMatrixPartitioner(
    nPartitions: Int,
    nFeats: Int) extends Partitioner {

    // Total number of elements in linear storage
    private val nElements = (nFeats - 2)*nFeats + 
                            ((2 - nFeats)*(nFeats - 1))/2 + 1

    private val partitionSize = round(nElements/nPartitions).toInt
    
    // For a given key returns the partitionId it corresponds to.
    def getPartition(key: Any): Int = {

      val (i,j) = key.asInstanceOf[(Int,Int)]
      // Calculate position on linear storage
      val position = (i - 1)*nFeats + j - (i - 1)*i/2 - i - 1

      val partitionId = round(position/partitionSize - 0.5).toInt

      // When the last partition has more elements than the partition size,
      // the element has to be included on it
      if(partitionId <= nPartitions - 1) partitionId else (nPartitions - 1)
    }

    def numPartitions: Int = nPartitions

  }

  // TODO? Non-purely functional code
  private var distrCorrs: RDD[((Int, Int),Double)] = _
  private var localCorrs: IndexedSeq[Double] = _

 
  // Here nFeats includes the class
  val nFeats = data.take(1)(0).features.size + 1

  // DEBUG
  // val sameCorrs = (0 until nFeats).map(f=>correlator.correlate(f,f))
  // println("SAME FEAT CORRS=")
  // sameCorrs.foreach(println)

  if(distributedStorage) {
    // TODO Sark does not allows to start a task from another task, so it is
    // not possible to create an RDD from the results of task from another, so
    // it is neccesary to create a local matrix with a part of the
    // correlations, then paralleliza and so on, itelratively create a bigger
    // matrix.
    throw new UnsupportedOperationException("Not Implemented")
    
    // // Correlate each feature f with f + 1 until last
    // distrCorrs = sc
    //   .parallelize(0 until nFeats)
    //   .flatMap {
    //     featA => 
    //       (featA + 1 until nFeats)
    //         .map{ featB => ((featA, featB), 
    //                         correlator.correlate(featA,featB)) 
    //         }
    //   }
    //   // Since parallelize acts lazily, there should be no problem
    //   // with defining the Partitioner "after" the flatMap
    //   // TODO experiment with num of partitions
    //   .partitionBy(
    //     new CorrelationsMatrixPartitioner(sc.defaultParallelism, nFeats))

    //distrCorrs.cache()
  } else {
    // TODO! Confirm if the correleations are in correct order!!!

    // Correlate each feature f with f + 1 until last
    localCorrs = 
      (0 until nFeats)
        .flatMap {
          featA => 
            (featA + 1 until nFeats)
              .map{ featB => correlator.correlate(featA,featB) }
        }
  }

  def apply(i:Int, j:Int): Double = {
    
    if(distributedStorage){
      // TODO
      throw new UnsupportedOperationException("Not Implemented")
      Double.NegativeInfinity
      // // Only pairs (i,j) where i < j are stored
      // if(i < j) {
      //   distrCorrs.lookup((i,j))(0) 
      // // Correlation of each feat with itself
      // } else if(i == j){
      //   correlator.sameFeature
      // } else {
      //   distrCorrs.lookup((j,i))(0)
      // }
    // Return from local matrix
    }else{
      // Correlation of each feat with itself
      if(i == j) {
        correlator.sameFeature
      // Only pairs (i,j) where i < j are stored
      } else if(i < j){
        // Calculate position on linear storage
        val position = i*nFeats - 0.5*i*(i + 1) - i + j - 1
        assert(position.isWhole, "No whole position in correlation matrix")
        localCorrs(position.toInt) 
      // Invert i and j if i > j
      } else {
        // Calculate position on linear storage
        val position = j*nFeats - 0.5*j*(j + 1) - j + i - 1
        assert(position.isWhole, "No whole position in correlation matrix")
        localCorrs(position.toInt)
      }
    }
  }
}