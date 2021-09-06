package com.ravindra.utility

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Dataset, SaveMode, SparkSession}
import org.apache.spark.util.SizeEstimator

trait Common {

  /**
   * Creates a SparkSession
   * * @param appName
   */
  def sparkSessions(appName: String): SparkSession = {
    val session = SparkSession
      .builder()
      .master("local")
      .appName(appName)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    session
  }
  /**
   * stop sparksession
   * @param sparkSession
   */
  def sparkSessionStop(sparkSession: SparkSession): Unit = {
    sparkSession.stop();
  }

  /**
   * reading table format input
   * @param sparkSession
   * @param inputPath
   * @param inputDelimiter
   */
  def readFromCsv(sparkSession: SparkSession, inputPath: String, inputDelimiter: String, inputHeader: String): DataFrame ={
    return sparkSession.read.option("delimiter", inputDelimiter).option("header", inputHeader).csv(inputPath)
  }

  /**
   * reading table format input
   * @param dataFrame
   * @param outputPath
   * @param outputDelimiter
   */

  def writeToCsv(dataFrame: DataFrame, outputPath: String, outputDelimiter: String, outputHeader: String, numOfPartitions: Int): Unit ={
    dataFrame.repartition(numOfPartitions).write.option("delimiter", outputDelimiter).option("header", outputHeader).mode(SaveMode.Overwrite).csv(outputPath)
  }

  def writeToCsv(dataFrame: DataFrame, batchId: Long, outputPath: String, outputDelimiter: String, outputHeader: String, numOfPartitions: Int): Unit ={
    dataFrame.repartition(numOfPartitions).write.option("delimiter", outputDelimiter).option("header", outputHeader).mode(SaveMode.Overwrite).csv(outputPath)
  }

  /**
   * returns the size of rdd
   * @param rdd
   */

  def calcRDDSize(rdd: RDD[String]): Long = {
    rdd.map(_.getBytes("UTF-8").length.toLong)
      .reduce(_+_) //add the sizes together
  }

  /**
   * return num of partitions
   * @param df
   * @param blockSize
   */
  def retunNumOfPartition(df: DataFrame, blockSize: Int): Int = {
    val rddOfDataframe = df.rdd.map(_.toString())
    val sizes = SizeEstimator.estimate(rddOfDataframe)
    var numOfPartitions = ((sizes/1048576)/blockSize).toInt
    if (numOfPartitions == 0) numOfPartitions=1

    numOfPartitions
  }

  /**
   * return the file stream dataframe
   * @param sparkSession
   * @param inputFormat
   * @param header
   * @param maxFilesPerTrigger
   * @param streamPath
   * @param schemas
   */
  def readFileStream(sparkSession: SparkSession, inputFormat: String, header: String, maxFilesPerTrigger: Int,
                     streamPath: String, schemas: StructType): DataFrame ={
     val dataFrame = sparkSession
      .readStream
      .format(inputFormat)
      .option("maxFilesPerTrigger", maxFilesPerTrigger) // This will read maximum of 2 files per mini batch. However, it can read less than 2 files.
      .option("header", header)
      .option("path", streamPath)
      .schema(schemas)
      .load()

    dataFrame
  }
}
