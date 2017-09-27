name := "spark-relieff"

version := "0.1.0_spark_1.6-SNAPSHOT"

organization := "rauljosepalma"

scalaVersion := "2.10.5"

val sparkVersion = "1.6.0"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-mllib" % sparkVersion)