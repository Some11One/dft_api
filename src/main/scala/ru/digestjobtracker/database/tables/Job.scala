package ru.digestjobtracker.database.tables

import java.sql.ResultSet

import ru.digestjobtracker.database.DatabaseSettings
import ru.digestjobtracker.database.tables.Job._
import ru.digestjobtracker.exceptions.JobNotFoundException

import scala.collection.mutable.ListBuffer

case class Job() extends DatabaseSettings {

  /**
    * Select all user jobs
    *
    * @param userId user id
    * @return user jobs
    */
  def selectAllUserJobs(userId: String): ListBuffer[JobDAO] = {
    val jobs = new ListBuffer[JobDAO]()
    val s = connect().createStatement()
    try {
      val res: ResultSet = s.executeQuery(s"SELECT * FROM $Table WHERE $FieldUserID = $userId")
      while (res.next()) {

        val timestampCreateElem = res.getString(FieldTimestampCreate)
        var timestampCreate: Option[Long] = None
        if (timestampCreateElem != null) {
          timestampCreate = Option(timestampCreateElem.toLong)
        }

        val timestampEndElem = res.getString(FieldTimestampEnd)
        var timestampEnd: Option[Long] = None
        if (timestampEndElem != null) {
          timestampEnd = Option(timestampEndElem.toLong)
        }

        jobs += JobDAO(fieldID = res.getString(FieldID).toInt, fieldUserID = userId.toInt, fieldState = res.getString(FieldState).toInt,
          fieldTimestampCreate = timestampCreate, fieldTimestampEnd = timestampEnd,
          fieldSrc = res.getString(FieldSrc), fieldAlgo = res.getString(FieldAlgo),
          fieldHex = Option(res.getString(FieldHex)), fieldError = Option(res.getString(FieldError)))
      }
      jobs
    } finally {
      if (s != null) s.close()
    }
  }

  /**
    * Remove job instance from DB
    *
    * @param jobId job id
    */
  def deleteJob(jobId: String): Unit = {
    val s = connect().createStatement()
    try {
      s.execute(s"DELETE FROM $Table WHERE $FieldID = $jobId")
    } finally {
      if (s != null) s.close()
    }
  }

  /**
    * Update job when it began to run
    *
    * @param jobId           job id
    * @param state           new state (see 'insertJob' method params) = 2
    * @param timestampCreate time when job began to run
    */
  def updateJob(jobId: Int, state: Int, timestampCreate: Long): Unit = {
    val s = connect().createStatement()
    try {
      val res = s.executeQuery(s"SELECT * FROM $Table WHERE $FieldID = $jobId")
      if (res.next()) {
        s.execute(s"UPDATE $Table SET ( $FieldTimestampCreate, $FieldState) = ('$timestampCreate', '$state') WHERE $FieldID = $jobId")
      } else {
        throw new JobNotFoundException()
      }
    } finally {
      if (s != null) s.close()
    }
  }

  /**
    * Update job after it ran - OK (no errors)
    *
    * @param jobId           job it
    * @param state           new state (see 'insertJob' method params) = 3
    * @param timestampCreate time when job began to run
    * @param timestampEnd    time when job ended
    * @param hex             result of 'algo'
    */
  def updateJobOK(jobId: Int, state: Int, timestampCreate: Long,
                  timestampEnd: Option[Long], hex: Option[String]): Unit = {
    val s = connect().createStatement()
    try {
      val res = s.executeQuery(s"SELECT * FROM $Table WHERE $FieldID = $jobId")
      if (res.next()) {
        s.execute(s"UPDATE $Table SET ( $FieldTimestampCreate, $FieldTimestampEnd, $FieldState, $FieldHex) = ('$timestampCreate', '${timestampEnd.get}', '$state', '${hex.get}') WHERE $FieldID = $jobId")
      } else {
        throw new JobNotFoundException()
      }
    } finally {
      if (s != null) s.close()
    }
  }

  /**
    * Update job after it ran - KO (errors)
    *
    * @param jobId           job it
    * @param state           new state (see 'insertJob' method params) = 4
    * @param timestampCreate time when job began to run
    * @param timestampEnd    time when job ended
    * @param error           text of an error which occurred while job was running
    */
  def updateJobKO(jobId: Int, state: Int, timestampCreate: Long,
                  timestampEnd: Option[Long], error: Option[String]): Unit = {
    val s = connect().createStatement()
    try {
      val res = s.executeQuery(s"SELECT * FROM $Table WHERE $FieldID = $jobId")
      if (res.next()) {
        s.execute(s"UPDATE $Table SET ( $FieldTimestampCreate, $FieldTimestampEnd, $FieldState, $FieldError) = ('$timestampCreate', '${timestampEnd.get}', '$state', '${error.get}') WHERE $FieldID = $jobId")
      } else {
        throw new JobNotFoundException()
      }
    } finally {
      if (s != null) s.close()
    }
  }

  /**
    * Insert job into DB
    *
    * @param userId user id who initiated job
    * @param state  job state - 1, 2, 3 or 4. 1 - job is not running, 2 - job is running, 3 - job is done, 4 - error while running a job
    * @param src    source for a file to feed into 'algo'
    * @param algo   hex algo to run on 'src'
    * @return job instance
    */
  def insertJob(userId: Int, state: Int, src: String, algo: String): JobDAO = {
    val s = connect().createStatement()
    try {
      s.execute(s"INSERT INTO $Table ($FieldUserID, $FieldState, $FieldSrc, $FieldAlgo) VALUES ('$userId', '$state', '$src', '$algo');")
      val jobID = s.executeQuery(s"SELECT max($FieldID) FROM $Table")
      if (jobID.next()) {
        JobDAO(fieldID = jobID.getString("max").toInt, fieldUserID = userId, fieldState = state, fieldSrc = src, fieldAlgo = algo)
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

  val FieldID = "job_id" // pk
  val FieldState = "state"
  val FieldTimestampCreate = "timestamp_create"
  val FieldTimestampEnd = "timestamp_end"
  val FieldSrc = "src"
  val FieldAlgo = "algo"
  val FieldHex = "hex"
  val FieldError = "error"

  val FieldUserID = "user_id" // fk
}

case class JobDAO(fieldID: Int, fieldUserID: Int, fieldState: Int,
                  fieldTimestampCreate: Option[Long] = None, fieldTimestampEnd: Option[Long] = None,
                  fieldSrc: String, fieldAlgo: String, fieldHex: Option[String] = None, fieldError: Option[String] = None)
