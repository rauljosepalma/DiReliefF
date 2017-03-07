name := "spark-relieff"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.10.5"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "1.6.0" % "provided",
  "org.apache.spark" %% "spark-mllib" % "1.6.0" % "provided",
  "com.databricks" %% "spark-csv" % "1.5.0")

spName := "com.github.rauljosepalma/spark-relieff"

sparkVersion := "1.6.0"

sparkComponents += "mllib"