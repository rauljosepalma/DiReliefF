// // TODO DELETE???

// package org.apache.spark.ml.feature

// import org.apache.spark.SparkContext
// import org.apache.spark.sql.SQLContext
// import org.apache.spark.SparkException
// import org.apache.spark.SparkConf
// import org.apache.spark.Accumulable
// import org.apache.spark.rdd.RDD
// import org.apache.spark.mllib.regression.LabeledPoint
// import org.apache.spark.mllib.linalg.Vectors
// import org.apache.spark.mllib.linalg.Vector
// import org.apache.spark.sql.{DataFrame, SQLContext}
// import org.apache.spark.ml.attribute.{Attribute, AttributeGroup,NominalAttribute, NumericAttribute, AttributeType}
// import org.apache.spark.sql.Row
// import org.apache.spark.sql.types.{DoubleType, StructField, StructType}

// import scala.collection.Map
// import scala.collection.mutable
// import scala.collection.mutable.StringBuilder
// import scala.collection.immutable.IndexedSeq
// import scala.util.{Failure, Success, Try}


// import rauljosepalma.sparkmltools._


// // import java.io.BufferedReader;

// // import java.io.File;
// // import java.io.FileInputStream;

// // import java.io.InputStreamReader;

// object Main {

  
 


//   // Parse libsvm to csv
//   // val lines = sc.textFile("/home/raul/Desktop/Datasets/Large/EPSILON/epsilon_normalized.libsvm")
//   // val splitted = lines.map(_.split(" "))
//   // val regex = "[0-9]+:".r
//   // val noidx = splitted.map(_.map(regex.replaceAllIn(_, "")))
//   // val classLast = noidx.map(a => a.tail :+ a.head)
//   // val outLines = classLast.map(_.mkString(","))
//   // outLines.coalesce(1).saveAsTextFile("/home/raul/Desktop/Datasets/Large/EPSILON/EPSILON_train.csv")
 

//   // Find distinct classes in data
//   // def findClasses(data: RDD[LabeledPoint]): Array[Int] = (
//   //   data
//   //     .map { lp => lp.label.toInt }
//   //     .distinct()
//   //     .collect()
//   //     .sorted 
//   // )

//   // End of data preparation methods

//   // args:
//   // args(0): file location
//   // args(1): k (num of neighbors)
//   // args(2): m (sample size)
//   // Optional
//   // args(3): num of feats.
//   // args(4): class label
//   // args(5): class label
//   def main(args: Array[String]): Unit = {

//     // val conf = new SparkConf().setAppName("sparkfs").setMaster("local")
//     // val conf = new SparkConf().setAppName("sparkfs")
//     implicit val sc = new SparkContext()
//     implicit val sqlCtxt = new SQLContext(sc)
//     // implicit val sc = SparkContext.getOrCreate()
//     // implicit val sqlCtxt = SQLContext.getOrCreate(sc)

//     val t0 = System.currentTimeMillis()

//     // Reduce verbosity
//     // sc.setLogLevel("WARN")
    
//     val df = DataFrameIO.readDFFromAny(args(0), nFeats=args(1), Array(args(2), args(3)))

//     // // ReliefF Model
//     // // args(0) Dataset full location
//     // // args(1) Directory where to save results. ex.: /root/results/
//     // // args(2) k (num of neighbors)
//     // // args(3) m (num of samples)
//     // // args(4) Use cache true or false
//     // // args(5) Use ramp function true or false
//     // // args(6) ContextMerit behavior true or false

//     // Gets the datasets basename
//     val baseName = args(0).split('_').head.split('/').last
//     // Ex.: /root/ECBDL14_k10m40_feats_weights.txt
//     // val basePath = args(1) + "/" + baseName + "_k" + args(2) + "m" + args(3) + "ramp" + args(5)

//     val model = (new ReliefFSelector()
//       .setNumNeighbors(args(4).toInt)
//       .setSampleSize(args(5).toInt)
//       .setUseCache(args(6).toBoolean)
//       .setUseRamp(args(7).toBoolean)
//       .setContextualMerit(args(8).toBoolean)
//       .setFeaturesCol("features")
//       .setOutputCol("selectedFeatures")
//       .setLabelCol("label")
//       .fit(df))

//         // val result = block    // call-by-name
//     val t1 = System.currentTimeMillis()
//     println("DiReliefF Elapsed time: " + (t1 - t0) + "ms")
//     println("WEIGHTS:")
//     println(model.featuresWeights.mkString(","))

//     // model.saveResults(basePath)

//     // println("Weights:")
//     // model.featuresWeights.zipWithIndex.foreach{ case (w,i) => 
//     //   println(s"$w, ${i+1}")
//     // }
//     // val reducedDf = model.transform(df)

//   }
// }