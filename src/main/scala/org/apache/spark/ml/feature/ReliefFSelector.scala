package org.apache.spark.ml.feature

import org.apache.spark.annotation.Experimental
import org.apache.spark.SparkException
import org.apache.spark.rdd.RDD

import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.ml.attribute._
import org.apache.spark.ml.param._
import org.apache.spark.ml.param.shared._
import org.apache.spark.ml._
import org.apache.spark.ml.util.Identifiable

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.Row

import scala.math.abs

import scala.collection.mutable

/**
 * Params for [[ReliefFSelector]] and [[ReliefFSelectorModel]].
 */
private[feature] trait ReliefFSelectorParams extends Params
  with HasFeaturesCol with HasOutputCol with HasLabelCol {

  /**
   * Number of neighbors used by the ReliefF algorithm, commonly known as k.
   * The default value of numNeighbors is 10.
   * @group param
   */
  final val numNeighbors = new IntParam(this, "numNeighbors",
    "Number of neighbors used by the ReliefF algorithm, commonly known as k.",
    ParamValidators.gtEq(1))
  setDefault(numNeighbors -> 10)

  // TODO update default value (in two places comment and code)
  /**
   * Number of instances randomly taken from the dataset, commonly known as m.
   * The default value of sampleSize is 70.
   * @group param
   */
  final val sampleSize = new IntParam(this, "sampleSize",
    "Number of instances randomly taken from the dataset, commonly known as m.",
    ParamValidators.gtEq(1))
  setDefault(sampleSize -> 70)

  /**
   * Sets the algorithm to behave as the Contextual Merit algorithm proposed in
   * Hong, S. J.: 1997, ‘Use of Contextual Information for Feature Ranking and
   * Discretization’. IEEE transactions on knowledge and data engineering 9(5),
   * 718–730. The default value of this parameter is false.
   * @group param
   */
  final val contextualMerit = new BooleanParam(this, "contextualMerit",
    "Sets the algorithm to behave as the Contextual Merit algorithm proposed in Hong, S. J.: 1997, ‘Use of Contextual Information for Feature Ranking and Discretization’. IEEE transactions on knowledge and data engineering 9(5), 718–730")
  setDefault(contextualMerit -> false)

  /**
   * Proportion of features to select, it should be bigger than 0.0 and less
   * than or equal to 1.0. It is by default set to 0.15.
   * @group param
   */
  final val selectionThreshold = new DoubleParam(this, "selectionThreshold",
    "Represents a proportion of features to select, it should be bigger than 0.0 and less than or equal to 1.0. It is by default set to 0.15.",
    ParamValidators.inRange(0.0, 1.0, lowerInclusive=false, upperInclusive=true))
  setDefault(selectionThreshold -> 0.15)


    /** @group getParam */
  def getNumNeighbors: Int = $(numNeighbors)
  def getSampleSize: Int = $(sampleSize)
  def getContextualMerit: Boolean = $(contextualMerit)
  def getSelectionThreshold: Double = $(selectionThreshold)

}

@Experimental
final class ReliefFSelector(override val uid: String)
  extends Estimator[ReliefFSelectorModel] with ReliefFSelectorParams 
  // with DefaultParamsWritable 
  {

  def this() = this(Identifiable.randomUID("ReliefFSelector"))

  /** @group setParam */  
  def setNumNeighbors(value: Int): this.type = set(numNeighbors, value)

  /** @group setParam */  
  def setSampleSize(value: Int): this.type = set(sampleSize, value)

  /** @group setParam */  
  def setContextualMerit(value: Boolean): this.type = set(contextualMerit, value)

  /** @group setParam */  
  def setFeaturesCol(value: String): this.type = set(featuresCol, value)

  /** @group setParam */  
  def setOutputCol(value: String): this.type = set(outputCol, value)

  /** @group setParam */  
  def setLabelCol(value: String): this.type = set(labelCol, value)
  
  // If received DataFrame doesn't contains attrs in metadata
  // they are assumed to be numeric, this is important because
  // some high dim datasets, would contain too much repeated ml attrs.
  // 
  // The label column must contain labels starting from 0 and ending in
  // numOfLabels - 1
  override def fit(data: DataFrame): ReliefFSelectorModel = {

    // Transform DataFrame to RDD[LabeledPoint]
    val LPData: RDD[LabeledPoint] = 
      data.select("features", "label").map {
      case Row(features: Vector, label: Double) =>
        LabeledPoint(label, features)
      }
    LPData.cache

    val numOfFeats = LPData.take(1)(0).features.size

    // Create a custom Ordering for the Attributes
    case class AttributeOrdering() extends Ordering[Attribute] {
      def compare(a: Attribute, b: Attribute) = {
        // Attrs in metadata must contain index
        a.index.get.compare(b.index.get)
      }
    }

    // Extract attributes from metadata (if defined)
    val ag = AttributeGroup.fromStructField(data.schema("features"))
    val initialAttrs: Array[Attribute] = ag.attributes match { 
      // Make sure attrs are ordered by index
      case Some(attributes) => attributes.sorted(AttributeOrdering())
      // If no attrs are def, consider them to be numeric
      case None => Array.fill(numOfFeats)(NumericAttribute.defaultAttr)
    }

    // Extract distinct class values (labels) from metadata
    val labelAttr = Attribute.fromStructField(data.schema("label"))
    val classes: Array[Int] = 
      (labelAttr.attrType match {
        case AttributeType.Nominal => 
          // Nominal attr 'label' must contain values metadata, 
          // labels must be numbers between 0 and numClasses - 1
          val numClasses = 
            labelAttr.asInstanceOf[NominalAttribute].getNumValues.get
          Range(0, numClasses).toArray
        // Right now, only dataframes with metadata are accepted
        // case AttributeType.Unresolved => 
        //   require(!labels.isEmpty, 
        //     "If no attr metadata is defined, then labels must be sent as a param")
        //   labels
        case _ =>
          throw new SparkException("Attr 'label' must be nominal")
      })

    // Find mins and maxs for each numeric attr       
    def maxLP(lp1: LabeledPoint, lp2: LabeledPoint): LabeledPoint = {
      val maxFeats: Array[Double] = 
        lp1.features.toArray.zip(lp2.features.toArray)
          .map{ case (f1, f2) => scala.math.max(f1,f2) }
      new LabeledPoint(0.0, Vectors.dense(maxFeats))
    }

    def minLP(lp1: LabeledPoint, lp2: LabeledPoint): LabeledPoint = {
      val minFeats: Array[Double] = 
        lp1.features.toArray.zip(lp2.features.toArray)
          .map{ case (f1, f2) => scala.math.min(f1,f2) }
      new LabeledPoint(0.0, Vectors.dense(minFeats))
    }

    val maxValues: Array[Double] = LPData.reduce(maxLP).features.toArray
    val minValues: Array[Double] = LPData.reduce(minLP).features.toArray

    // Set min and max values for all numeric attributes
    val attrs: Array[Attribute] = initialAttrs.zipWithIndex.map{ 
      case (attr: NumericAttribute, idx: Int) => 
        // if(attr.isNumeric) {
        attr.withMin(minValues(idx)).withMax(maxValues(idx))
      case (attr, _) => 
        attr
    }
    

    // Estimated prior probabilites of each class
    val numOfInstances = LPData.count
    val priors: Map[Int, Double]= (
      LPData
        .map { lp => (lp.label.toInt,1) }
        .reduceByKey { _+_ }
        .map { case (c, count) => (c, count.toDouble / numOfInstances) }
        .collect
        .toMap
    )

    // Calculates the difference on an attr between two instances
    // depending if they are numeric or nominal, using the ramp function
    // proposed in Robnik-Šikonja, M., & Kononenko, I. (2003)
    def diff(idx: Int, i1: LabeledPoint, i2: LabeledPoint): Double = {
      
      // Numeric attribute
      if(attrs(idx).isNumeric){

        val max = attrs(idx).asInstanceOf[NumericAttribute].max.get
        val min = attrs(idx).asInstanceOf[NumericAttribute].min.get

        // // tEqu is the maximum distance for two attrs to be considered equal
        // val tEqu = 0.05 * (max - min)
        // // tDif is the minimum distance for two attrs to be considered different
        // val tDif = 0.10 * (max - min)

        // val dist = abs(i1.features(idx) - i2.features(idx))

        // if(dist <= tEqu){
        //   0.0
        // }else if(dist > tDif){
        //   1.0
        // }else{
        //   (dist - tEqu) / (tDif - tEqu)
        // }
        
        // The traditional way proposed in Kononenko, I. (1994)
        if (abs(max - min) < 1e-6) {
          0 
        } else {
          abs(i1.features(idx) - i2.features(idx))/(max-min)
        }
        
      // Nominal attribute
      }else{
        if (i1.features(idx) == i2.features(idx)) 0.0 else 1.0  
      }

    }

    // The distance is simply the sum of the differences between each feature
    def distance(i1: LabeledPoint, i2: LabeledPoint): Double = {
      // (for(i <- 0 until numOfFeats) yield diff(i, i1, i2)).sum
      (0 until numOfFeats).map(diff(_, i1, i2)).sum
    }

    // Take at least one sample from each class
    // val samples: Array[LabeledPoint] = 
    //   classes.flatMap{ c => 
    //     (LPData
    //       .filter(_.label == c)
    //       .takeSample(withReplacement=false, num=sampleSize))
    //     }
    
    // Take random samples from LPData
    val samples: Array[LabeledPoint] = 
      LPData.takeSample(withReplacement=false, num=$(sampleSize))



    // DEBUG
    // println("SELECTED INSTANCES:")
    // samples.foreach(println)
    

    // Find distances from all instances to the m sample instances
    // There is no need to cache dataWithDistances, because it will traversed
    // only once. Caching the RDD negatively affects performance.
    val dataWithDistances: RDD[(LabeledPoint, Array[Double])] = 
      LPData.map { lp => ( lp, samples.map(distance(_,lp)) ) }
    // LPData is not needed anymore TODO: Test this!!
    // LPData.unpersist
    


    // A BinaryHeap is used to keep the nearest neighbor per partition the
    // head of the queue is the element with more distance (the) worst
    // neighbor, so it can be easily substituted by a another better.
    def nearestNeighborsSelector(
      neighborsMatrix: NeighborsMatrix, 
      instWithDist: (LabeledPoint, Array[Double])):
      NeighborsMatrix = {

        (0 until samples.size).foreach { i =>
          // If ContextualMerit behavior is enabled, ignore instances with same
          // class
          if(!$(contextualMerit) || 
             instWithDist._1.label.toInt != samples(i).label.toInt) {

            neighborsMatrix.get((instWithDist._1.label.toInt, i)) match {
              case Some(neighborsHeap) =>
                neighborsHeap += ((instWithDist._1, instWithDist._2(i)))
              case None =>
                // Create empty NeighborsHeap, define Ordering by distance, and
                // add the element

                // The heap of neighbors of same class accepts one more
                // element, to make space for the original sample. Even when
                // this heap is longer it won't affect the sumsOfDiffs beacuse
                // the contribution of the sample will be 0.
                val capacity = 
                  if(instWithDist._1.label.toInt == samples(i).label.toInt) $(numNeighbors) + 1 
                  else $(numNeighbors)

                val heap = new NeighborsHeap(capacity)
                heap += ((instWithDist._1, instWithDist._2(i)))
                neighborsMatrix((instWithDist._1.label.toInt, i)) = heap
            }
          }
        }

        neighborsMatrix
    }

    def nearestNeighborsCombinator(
      neighborsMatrixA: NeighborsMatrix,
      neighborsMatrixB: NeighborsMatrix):
      NeighborsMatrix = {

      // Update neighborsMatrixA with neighborsMatrixB values
      (0 until samples.size).map { i =>
        classes.map { c =>
          neighborsMatrixA.get((c, i)) match {
            case Some(neighborsHeapA) =>
              
              neighborsMatrixB.get((c, i)) match {
                case Some(neighborsHeapB) =>
                  
                  neighborsHeapA.merge(neighborsHeapB)
                  neighborsMatrixA((c, i)) = neighborsHeapA

                case None =>
                  // do nothing
              }
            case None =>

              neighborsMatrixB.get((c, i)) match {
                case Some(neighborsHeapB) =>
                  neighborsMatrixA((c, i)) = neighborsHeapB
                
                case None =>
                  // If ContextualMerit behavior is enabled, do not create
                  // a NeighborsHeap for same class neighbors.
                  if(!$(contextualMerit) || c != samples(i).label.toInt) {

                    // The heap of neighbors of same class accepts one more
                    // element, to make space for the original sample. Even
                    // when this heap is longer it won't affect the sumsOfDiffs
                    // beacuse the contribution of the sample will be 0.
                    val capacity = 
                      if(c == samples(i).label.toInt) $(numNeighbors) + 1 
                      else $(numNeighbors)

                    neighborsMatrixA((c, i)) = new NeighborsHeap(capacity)
                  }
              }
          }
        }
      }

      neighborsMatrixA
        
    }

    // val emptyMatrix = new NeighborsMatrix(mutable.Map.empty)

    // In the case of ContextualMerit behavior, the nearestNeighbors matrix,
    // simply won't contain heaps for same class neighbors, and that
    // way they will not affect the weights calculation in subsequent steps.
    val nearestNeighbors: mutable.Map[(Int,Int), Array[LabeledPoint]] =
      dataWithDistances.aggregate(new NeighborsMatrix(mutable.Map.empty))(
        nearestNeighborsSelector, nearestNeighborsCombinator)
          .data
            // Turn queue into array and drop distances
            .map{ case (k, q) => (k, q.toArray.map(_._1)) }
            // Remove the samples from the nearestNeighbors
            .map{ case ((c, i), neighbors) => 
              if (c == samples(i).label.toInt)
                ((c, i), neighbors.filter { n => n eq samples(i) } )
              else
                ((c, i), neighbors)      
            }

    // Check and log if not enough neighbors for each class were found
    nearestNeighbors.foreach{ case ((c, i), neighbors) => 
      if(neighbors.size != $(numNeighbors)) {
        logInfo(s"Couldn't find enough neighbors for sample in class $c, ${neighbors.size}/" + $(numNeighbors).toString)
      }
    }

    val sumsOfDiffs: mutable.Map[(Int, Int), IndexedSeq[Double]] =
      nearestNeighbors.map { case ((c,i), neighbors) => 
        ((c,i), (0 until numOfFeats)
                  .map { f => 
                    (neighbors
                        .map { lp => diff(f, lp, samples(i)) }    
                        .sum
                    ) / ( neighbors.size * samples.size )
                  })
      }

    val weights: IndexedSeq[Double] = (
      (0 until numOfFeats).map { f => 
        (0 until samples.size).map { i =>
          classes.map { c => 
            ( if (c == samples(i).label.toInt) {
              // Its a hit
                -sumsOfDiffs(c,i)(f)
              } else {
              // Its a miss
                (priors(c) / (1.0 - priors(samples(i).label.toInt))) * sumsOfDiffs(c,i)(f)
              } /// nearestNeighbors((c,i)).size
            )
          }.sum 
        }.sum
      }
      // Divide by m and k
      // .map { w => w / (samples.size * $(numNeighbors)) }
      // Divide by m
      // .map { w => w / samples.size }
    )

    // DEBUG
    // This should be zero when ContextualMerit is enabled.
    val totalHitsContributions: Double = 
      (0 until numOfFeats).map { f => 
        (0 until samples.size).map { i =>
          classes.map { c =>
            if (c == samples(i).label.toInt) {
            // Its a hit
              sumsOfDiffs(c,i)(f)
            } else {
            // Its a miss
              0.0
            }
          }.sum
        }.sum
      }.sum
    assert(!($(contextualMerit) && totalHitsContributions == 0.0), s"Error totalHitsContributions should be 0.0 when ContextualMerit is enabled, but it is equal to ${totalHitsContributions}")

    copyValues(new ReliefFSelectorModel(uid, weights, totalHitsContributions).setParent(this)) // DEBUG
  }

  // NeighborsHeap uses a BinaryHeap with an Ordering based on the distance
  // were the worst distance is the bigger, both Ordering and ClassTags were
  // needed to be sent, I believe because of all implicits must be sent.
  private class NeighborsHeap(capacity: Int) 
    extends BinaryHeap[(LabeledPoint, Double)](capacity)(
      math.Ordering.by[(LabeledPoint, Double), Double](_._2 * -1.0),
      reflect.classTag[(LabeledPoint, Double)])

  private class NeighborsMatrix(
    val data: mutable.Map[(Int,Int), NeighborsHeap])
    extends java.io.Serializable {

    def get(index: (Int, Int)) = data.get(index)
    def update(
      index: (Int, Int), 
      value: NeighborsHeap) = data(index) = value

  }

  override def copy(extra: org.apache.spark.ml.param.ParamMap): org.apache.spark.ml.Estimator[org.apache.spark.ml.feature.ReliefFSelectorModel] = ???

  def transformSchema(schema: org.apache.spark.sql.types.StructType): org.apache.spark.sql.types.StructType = ???

}


/**
 * :: Experimental ::
 * Model fitted by [[ReliefFSelector]].
 */
@Experimental
final class ReliefFSelectorModel private[ml] (
    override val uid: String,
    val featuresWeights: IndexedSeq[Double],
    // DEBUG
    val totalHitsContributions:Double)
  extends Model[ReliefFSelectorModel] with ReliefFSelectorParams 
    // with MLWritable 
  {

  /** @group setParam */
  def setSelectionThreshold(value: Double): this.type = 
    set(selectionThreshold, value)

  /** @group setParam */
  def setFeaturesCol(value: String): this.type = set(featuresCol, value)

  /** @group setParam */
  def setOutputCol(value: String): this.type = set(outputCol, value)

  /** @group setParam */
  def setLabelCol(value: String): this.type = set(labelCol, value)

  override def transform(data: DataFrame): DataFrame = {

    val selectedFeatures: Array[Int] = {

        // Sorted features from most relevant to least (weight, index)
        val sortedFeats: Array[(Double, Int)] = 
          (featuresWeights.zipWithIndex).sorted(Ordering.by[(Double, Int), Double](_._1 * -1.0)).toArray

        // Slice according threshold
        (sortedFeats
          .slice(0,(sortedFeats.size * $(selectionThreshold)).round.toInt)
          .map(_._2))
      }

    //   if(useKnnSelection) {
        
    //     val weights: Map[Int, Double] = (featuresWeights.indices zip featuresWeights).toMap

    //     knnBestFeatures(weights, 0.5, -0.5)

    //   } 

    val slicer = (new VectorSlicer()
      .setInputCol(featuresCol.name)
      .setOutputCol(outputCol.name)
      .setIndices(selectedFeatures))

    // Return reduced Dataframe
    // (slicer
    //   .transform(data)
    //   .selectExpr("selectedFeatures as features", "label"))
    slicer.transform(data)
  }

  // override def transformSchema(schema: StructType): StructType

  def saveResults(basePath: String): Unit = {
    
    println("Adding weights to file:")
    var file = new java.io.FileWriter(s"${basePath}_feats_weights.txt", true)
    file.write(featuresWeights.head.toString)
    featuresWeights.tail.foreach(weight => file.write("," + weight.toString))
    file.write("\n")
    file.close

    println("saving positive feats:")
    var weights: Map[Int, Double] = (featuresWeights.indices zip featuresWeights).toMap
    var bestFeatures: Array[Int] = 
      weights.filter{ case (k: Int, w: Double) => w > 0.0 }
             .map{ case (k: Int, w: Double) => k }.toArray
    file = new java.io.FileWriter(s"${basePath}_feats_positive.txt", true)
    bestFeatures.foreach(feat => file.write(feat.toString + "\n"))
    file.close
    println("total: " + bestFeatures.size)

    println("saving 10% best feats:")
    val sortedFeats: Array[(Int, Double)] = 
      (featuresWeights.indices zip featuresWeights).sorted(Ordering.by[(Int, Double), Double](_._2 * -1.0)).toArray
    val bestFeats10Perc = 
      sortedFeats.slice(0,(sortedFeats.size * 0.10).round.toInt).map(_._1)
    file = new java.io.FileWriter(s"${basePath}_feats_10perc.txt", true)
    bestFeats10Perc.foreach(feat => file.write(feat.toString + "\n"))
    file.close
    println("total: " + bestFeats10Perc.size)

    println("saving 25% best feats:")
    val bestFeats25Perc = 
      sortedFeats.slice(0,(sortedFeats.size * 0.25).round.toInt).map(_._1)
    file = new java.io.FileWriter(s"${basePath}_feats_25perc.txt", true)
    bestFeats25Perc.foreach(feat => file.write(feat.toString + "\n"))
    file.close
    println("total: " + bestFeats25Perc.size)
    
    println("saving 50% best feats:")
    val bestFeats50Perc = 
      sortedFeats.slice(0,(sortedFeats.size * 0.50).round.toInt).map(_._1)
    file = new java.io.FileWriter(s"${basePath}_feats_50perc.txt", true)
    bestFeats50Perc.foreach(feat => file.write(feat.toString + "\n"))
    file.close
    println("total: " + bestFeats50Perc.size)
    
    println("saving 75% best feats:")
    val bestFeats75Perc = 
      sortedFeats.slice(0,(sortedFeats.size * 0.75).round.toInt).map(_._1)
    file = new java.io.FileWriter(s"${basePath}_feats_75perc.txt", true)
    bestFeats75Perc.foreach(feat => file.write(feat.toString + "\n"))
    file.close
    println("total: " + bestFeats75Perc.size)

    println("saving hits contribution:")
    file = new java.io.FileWriter(s"${basePath}_hits_contrib.txt", true)
    file.write(totalHitsContributions.toString)
    file.close

    // println("saving knn best feats:")
    // weights = (featuresWeights.indices zip featuresWeights).toMap
    // bestFeatures = knnBestFeatures(weights, 0.5, -0.5)
    // file = new java.io.FileWriter(s"${basePath}_feats_knn.txt", true)
    // bestFeatures.foreach(feat => file.write(feat.toString + "\n"))
    // file.close
    // println("total: " + bestFeatures.size)

  }

  override def copy(extra: org.apache.spark.ml.param.ParamMap): org.apache.spark.ml.feature.ReliefFSelectorModel = ???

  def transformSchema(schema: org.apache.spark.sql.types.StructType): org.apache.spark.sql.types.StructType = ???

}

// final class ReliefFSelectorModel(
//   val featuresWeights: IndexedSeq[Double],
//   val useKnnSelection: Boolean,
//   val selectionThreshold: Double = 0.0) {

//   require(selectionThreshold <= 1.0 && selectionThreshold >= 0.0,
//     "Error selectionThreshold must be between 0.0 and 1.0")

//   def transform(data: DataFrame): DataFrame = {

//     val selectedFeatures: Array[Int] = 
//       if(useKnnSelection) {
        
//         val weights: Map[Int, Double] = (featuresWeights.indices zip featuresWeights).toMap

//         knnBestFeatures(weights, 0.5, -0.5)

//       } else {

//         // Sorted features from most relevant to least
//         val sortedFeats: Array[(Int, Double)] = 
//           (featuresWeights.indices zip featuresWeights).sorted(Ordering.by[(Int, Double), Double](_._2 * -1.0)).toArray

//         // Slice according threshold
//         (sortedFeats
//           .slice(0,(sortedFeats.size * selectionThreshold).round.toInt)
//           .map(_._1))
//       }


//     var slicer = new VectorSlicer().setInputCol("features").setOutputCol("selectedFeatures")
//     slicer.setIndices(selectedFeatures)

//     // Return reduced Dataframe
//     (slicer
//       .transform(data)
//       .selectExpr("selectedFeatures as features", "label"))
//   }



//   def knnBestFeatures(weights: Map[Int, Double], centerA: Double, centerB: Double):
//    Array[Int] = {
//     // Map of feature indexes and weights
//     val clusterA: Map[Int, Double] = weights.filter { 
//       case (idx, weight) => 
//         val distanceA = math.pow(weight - centerA, 2)
//         val distanceB = math.pow(weight - centerB, 2)

//         (distanceA < distanceB)
//     }

//     val clusterB = weights -- clusterA.map(_._1)

//     val newCenterA = clusterA.map(_._2).sum / clusterA.size
//     val newCenterB = clusterB.map(_._2).sum / clusterB.size

//     if((abs(newCenterA - centerA) > 1e-6) || (abs(newCenterB - centerB) > 1e-6))
//       knnBestFeatures(weights, newCenterA, newCenterB)
//     else {
//       if (centerA > centerB)
//         clusterA.map(_._1).toArray
//       else
//         clusterB.map(_._1).toArray
//     }

//   }
// }

/*

  TODO
  To Extract the attributes:

   val ag = AttributeGroup.fromStructField(df.schema("features"))
   val attrs = ag.attributes match { case Some(attributes) => attributes }


*/
  

//   def rankFeatures: IndexedSeq[Double] = {

//   }
// }