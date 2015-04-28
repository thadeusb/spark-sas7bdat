package sas

import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io.NullWritable
import org.apache.hadoop.mapreduce.Job
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat
import org.apache.spark.rdd.NewHadoopRDD
import org.apache.spark.{Logging, SparkConf, SparkContext}
import org.scalatest._
import sas.mapreduce.SASInputFormat

class SASInputFormatSpec extends FlatSpec with Matchers with Logging {
  val BLOCK_SIZE = 3 * 1024 * 1024

  "SASInputFormat" should "read meta information ahead of time" in {
    val sc = new SparkContext(new SparkConf().setMaster("local[2]").setAppName("SASInputFormat"))
    val job = Job.getInstance
    val jobConf = job.getConfiguration
    jobConf.setInt("fs.local.block.size", BLOCK_SIZE)
    FileInputFormat.setMinInputSplitSize(job, BLOCK_SIZE)
    val path = new Path(getClass.getResource("/random.sas7bdat").getPath)
    FileInputFormat.setInputPaths(job, path)

    val fileStatus = FileSystem.get(jobConf).getFileStatus(path)
    logInfo(s"Block Size: ${fileStatus.getBlockSize}")

    val sasRDD = new NewHadoopRDD[NullWritable, Array[Object]](
      sc,
      classOf[SASInputFormat],
      classOf[NullWritable],
      classOf[Array[Object]],
      jobConf
    )

    sasRDD.count() should ===(1000000)

    true should ===(true)
  }
}