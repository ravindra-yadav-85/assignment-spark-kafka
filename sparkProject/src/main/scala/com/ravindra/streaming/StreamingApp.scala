package com.ravindra.streaming

import java.io.File
import com.ravindra.utility.Common
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.spark.sql.{DataFrame, SaveMode}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

object StreamingApp extends App with Common{

  case class Arguments(configPath: String = "")

  val parser = new scopt.OptionParser[Arguments]("Parsing application") {

    opt[String]('c', "configFile").
      required().valueName("").action((value, arguments) => arguments.copy(configPath = value))
  }

  def main(arguments: Arguments): Unit = {

    println("config File:" + arguments.configPath)
    //Load the config file
    val config: Config = ConfigFactory.parseFile(new File(arguments.configPath))

    // Create Spark Session
    val sparkSession = sparkSessions(config.getString("spark_app_name"))

    // Set Spark logging level to ERROR to avoid various other logs on console.
//    sparkSession.sparkContext.setLogLevel("ERROR")

    val schema = StructType(List(
      StructField("account_no", LongType, true),
      StructField("date", TimestampType, true),
      StructField("transaction_details", StringType, true),
      StructField("value_date", TimestampType, true),
      StructField("transaction_type", StringType, true),
      StructField("amount", DoubleType, true),
      StructField("balance", DoubleType, true)
    ))

    // Create Streaming DataFrame by reading data from File Source.
    val initDF = readFileStream(sparkSession, config.getString("input_file_format"),
      config.getString("input_header"),
      config.getString("max_files_per_trigger").toInt,
      config.getString("input_stream_dir"),
      schema)

    // Check if DataFrame is streaming or Not.
    println("Is this Streaming DataFrame : " + initDF.isStreaming)

    // Print Schema of DataFrame
    println("Schema of DataFame initDF ...")
    println(initDF.printSchema())

    val resultDF = initDF.withColumn("month", to_timestamp(trunc(col("date"),"Month"),"yyyy-mm-dd HH:mm:ss"))
      .withWatermark("month","10 minutes")
      .groupBy(col("account_no"), window(col("month"), "1 days").as("window"))
      .agg(sum("amount").as("total"))
      .filter(col("total") >= 1000000000).drop(col("window"))

        resultDF
//          .repartition(1)
          .writeStream
//          .trigger(Trigger.ProcessingTime("1 minute"))
//          .format("csv")
          .outputMode("append")
          .foreachBatch(writeToFile)
          .option("startingOffset", "earliest")
          .option("forceDeleteTempCheckpointLocation", true)
//          .option("path","sparkProject/data/output/realtime")
          .option("spark.sql.streaming.fileSource.log.cleanupDelay","1")
          .option("spark.sql.streaming.fileSource.log.compactInterval","1")
          .option("cleanSource", config.getString("clean_source"))
          .option("sourceArchiveDir", config.getString("source_archive_dir"))
          .option("checkpointLocation", config.getString("checkpoint_dir"))
          .start()
          .awaitTermination()

    sparkSession.stop()

    def writeToFile = (df: DataFrame, batchId: Long) => {
      //calculate the num of partitions
      val numOfPartitions = retunNumOfPartition(df, config.getString("block_size").toInt)

      df
        .repartition(numOfPartitions)
        .write.option("delimiter", config.getString("output_delimiter"))
        .option("header", config.getString("output_header"))
        .mode(SaveMode.Overwrite)
        .csv(config.getString("output_path"))
    }
  }

  parser.parse(args, Arguments()) match {
    case Some(arguments) => main(arguments)
    case None =>
  }
}