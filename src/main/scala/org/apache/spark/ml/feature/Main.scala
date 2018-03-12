package org.apache.spark.ml.feature

import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

object Main {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("sparkfs").setMaster("local")
    val sc: SparkContext = SparkContext.getOrCreate(conf)
    val sqlCtxt: SQLContext = SQLContext.getOrCreate(sc)

    // Reduce verbosity
    sc.setLogLevel("WARN")

    val df = sqlCtxt.createDataFrame(
      Seq(
        (Vectors.dense(1.0, 1.0), 0.0),
        (Vectors.dense(2.0, 2.0), 0.0),
        (Vectors.dense(3.0, 3.0), 0.0),
        (Vectors.dense(4.0, 4.0), 0.0),
        (Vectors.dense(5.0, 5.0), 0.0),
        (Vectors.dense(6.0, 6.0), 0.0),
        (Vectors.dense(7.0, 7.0), 0.0),
        (Vectors.dense(8.0, 8.0), 0.0),
        (Vectors.dense(9.0, 9.0), 0.0),
        (Vectors.dense(10.0, 10.0), 0.0),
        (Vectors.dense(1.0, 2.0), 1.0),
        (Vectors.dense(2.0, 3.0), 1.0),
        (Vectors.dense(3.0, 4.0), 1.0),
        (Vectors.dense(4.0, 5.0), 1.0),
        (Vectors.dense(5.0, 6.0), 1.0),
        (Vectors.dense(6.0, 7.0), 1.0),
        (Vectors.dense(7.0, 8.0), 1.0),
        (Vectors.dense(8.0, 9.0), 1.0),
        (Vectors.dense(9.0, 10.0), 1.0),
        (Vectors.dense(10.0, 11.0), 1.0)
      )
    )
      .toDF("features", "label")

    val model = new ReliefFSelector()
      .setNumNeighbors(5)
      .setSampleSize(5)
      .setFeaturesCol("features")
      .setOutputCol("selectedFeatures")
      .setLabelCol("label")
      .fit(df)

    println("Weights:")
    model.featuresWeights.zipWithIndex.foreach { case (w, i) =>
      println(s"$w, ${i + 1}")
    }
  }
}