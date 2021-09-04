package com.ravindra.batch

import java.io.File
import com.typesafe.config.{Config, ConfigFactory}
import com.ravindra.utility.Common
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.util.SizeEstimator
import scala.math.{ceil}

object BatchApp extends App with Common {

  case class Arguments(configPath: String = "")

  val parser = new scopt.OptionParser[Arguments]("Parsing application") {

    opt[String]('c', "configFile").
      required().valueName("").action((value, arguments) => arguments.copy(configPath = value))
  }

  def main(arguments: Arguments): Unit = {
    println("config File:" + arguments.configPath)
    //Load the config file
    val config: Config = ConfigFactory.parseFile(new File(arguments.configPath))

    //create a spark session
    val sparkSession: SparkSession = sparkSessions(config.getString("spark_app_name"))

    // Set Spark logging level to ERROR to avoid various other logs on console.
//    sparkSession.sparkContext.setLogLevel("ERROR")

    //read the input file
    val loadCsvInput: DataFrame = readFromCsv(sparkSession,
      config.getString("input_path"),
      config.getString("input_delimiter"),
      config.getString("input_header"))

    //create table
    loadCsvInput.createOrReplaceTempView("table")

    //run the sql
    val tmp = sparkSession.sql(config.getString("sql_query"));

    //calculate the num of partitions
    val numOfPartitions = retunNumOfPartition(tmp, config.getString("block_size").toInt)

    //save the output file
    writeToCsv(tmp, config.getString("output_path"),
      config.getString("output_delimiter"),
      config.getString("output_header"),
      numOfPartitions)

    //stop the spark session
    sparkSessionStop(sparkSession)
  }

  parser.parse(args, Arguments()) match {
    case Some(arguments) => main(arguments)
    case None =>
  }
}