package ru.digestjobtracker.database.tables

import java.sql.ResultSet

import ru.digestjobtracker.database.DatabaseSettings
import ru.digestjobtracker.database.tables.Job._
import ru.digestjobtracker.exceptions.JobNotFoundException

import scala.collection.mutable.ListBuffer

case class Job() extends DatabaseSettings {

  def selectAllUserJobs(userId: String): ListBuffer[JobDAO] = {
    val jobs = new ListBuffer[JobDAO]()
    val s = connect().createStatement()
    try {
      val res: ResultSet = s.executeQuery(s"SELECT * FROM $Table WHERE $FieldUserID = $userId")
      while (res.next()) {

        val timestampEndElem = res.getString(FieldTimestampEnd)
        var timestampEnd: Option[Long] = None
        if (timestampEndElem != null) {
          timestampEnd = Option(timestampEndElem.toLong)
        }

        jobs += JobDAO(fieldID = Option(res.getString(FieldID).toInt), fieldUserID = userId.toInt, fieldState = res.getString(FieldState).toInt,
          fieldTimestampCreate = Option(res.getString(FieldTimestampCreate).toLong), fieldTimestampEnd = timestampEnd,
          fieldSrc = res.getString(FieldSrc), fieldAlgo = res.getString(FieldAlgo))
      }
      jobs
    } finally {
      if (s != null) s.close()
    }
  }

  def updateJob(jobId: Int, state: Int, timestampEnd: Long, hex: String, error: String): Unit = {
    val s = connect().createStatement()
    try {
      val res = s.executeQuery(s"SELECT * FROM $Table WHERE $FieldID = $jobId")
      if (res.next()) {
        s.execute(s"UPDATE $Table SET ( $FieldTimestampEnd, $FieldState, $FieldHex, $FieldError) = ($timestampEnd, $state $hex, $error) WHERE id = $jobId")
      } else {
        throw new JobNotFoundException()
      }
    } finally {
      if (s != null) s.close()
    }
  }


  def insertJob(userId: Int, state: Int, src: String, algo: String, timestampCreate: Long): JobDAO = {
    val s = connect().createStatement()
    try {
      s.execute(s"INSERT INTO $Table ($FieldUserID, $FieldState, $FieldSrc, $FieldAlgo, $FieldTimestampCreate) VALUES ('$userId, $state, $src, $algo, $timestampCreate');")
      val jobID = s.executeQuery(s"SELECT max($FieldID) FROM $Table")
      if (jobID.next()) {
        JobDAO(fieldID = Option(jobID.getString("max").toInt), fieldUserID = userId, fieldState = state, fieldSrc = src, fieldAlgo = algo)
      } else {
        throw new JobNotFoundException()
      }
    } finally {
      if (s != null) s.close()
    }
  }

  /**
    * Create table of jobs
    */
  def createTable(): Unit = {
    val s = connect().createStatement()
    try {
      s.execute(s"DROP TABLE $Table")
      s.execute(s"CREATE TABLE $Table ( $FieldID Serial, $FieldUserID int4, $FieldState int4, " +
        s"$FieldTimestampCreate int8, $FieldTimestampEnd int8, $FieldSrc text, $FieldAlgo text, $FieldHex text, $FieldError text);")
    } finally {
      if (s != null) s.close()
    }
  }
}

object Job {
  val Table: String = "Jobs"

  val FieldID = "id"
  val FieldUserID = "user_id"
  val FieldState = "state"
  val FieldTimestampCreate = "timestamp_create"
  val FieldTimestampEnd = "timestamp_end"
  val FieldSrc = "src"
  val FieldAlgo = "algo"
  val FieldHex = "hex"
  val FieldError = "error"
}

case class JobDAO(fieldID: Option[Int] = None, fieldUserID: Int, fieldState: Int,
                  fieldTimestampCreate: Option[Long] = None, fieldTimestampEnd: Option[Long] = None,
                  fieldSrc: String, fieldAlgo: String, fieldHex: Option[String] = None, fieldError: Option[String] = None)
