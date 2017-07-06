package ru.digestjobtracker.util

import java.util.Calendar

import ru.digestjobtracker.database.tables.{Job, JobDAO, UserDAO}
import ru.digestjobtracker.exceptions.AlgoTypeException
import ru.digestjobtracker.util.JobUtils.MaxRunningThreads

import scala.collection.mutable
import scala.io.Source

import java.util.concurrent.ConcurrentHashMap

/**
  * Created by ndmelentev on 05.07.17.
  */
class JobUtils {

  private val usersJobs = new ConcurrentHashMap[UserDAO, mutable.Queue[JobDAO]]()
  private val cancelledUsersJobs = new ConcurrentHashMap[UserDAO, mutable.ListBuffer[Int]]()
  @volatile private var runningThreads = 0

  {
    processJobs()
  }

  def getUserJobs(userDAO: UserDAO): mutable.Queue[JobDAO] = {
    usersJobs.getOrDefault(userDAO, mutable.Queue.empty)
  }

  def addUserJob(userDAO: UserDAO, src: String, algo: String): Unit = {
    val userJobs = usersJobs.getOrDefault(userDAO, mutable.Queue.empty)
    val jobDAO = Job().insertJob(userDAO.fieldID, 1, src, algo)
    userJobs += JobDAO(fieldID = jobDAO.fieldID, fieldUserID = userDAO.fieldID, fieldState = 1, fieldSrc = src, fieldAlgo = algo)
    if (usersJobs.containsKey(userDAO)) {
      usersJobs.replace(userDAO, userJobs)
    } else {
      usersJobs.put(userDAO, userJobs)
    }
  }

  def cancelUserJob(userDAO: UserDAO, jobId: String): Unit = {
    val userJobs = cancelledUsersJobs.getOrDefault(userDAO, mutable.ListBuffer.empty)
    userJobs += jobId.toInt
    if (cancelledUsersJobs.containsKey(userDAO)) {
      cancelledUsersJobs.replace(userDAO, userJobs)
    } else {
      cancelledUsersJobs.put(userDAO, userJobs)
    }
  }

  def processJobs(): Unit = {
    new Thread(new Runnable {
      override def run(): Unit = {
        while (true) {
          val iter = usersJobs.keySet().iterator()
          while ( {
            iter.hasNext
          }) {
            val userDAO = iter.next()
            val userJobs = usersJobs.get(userDAO)
            val userCancelledJobs = cancelledUsersJobs.get(userDAO)
            while ( {
              userJobs.nonEmpty && runningThreads <= MaxRunningThreads
            }) {
              val jobDAO = userJobs.dequeue()
              if (userCancelledJobs == null || !userCancelledJobs.contains(jobDAO.fieldID)) {
                usersJobs.replace(userDAO, userJobs)
                runningThreads += 1
                startUserJob(jobDAO)
              }
            }
          }
          Thread.sleep(1000)
        }
      }
    }).start()
  }

  def startUserJob(jobDAO: JobDAO): Unit = {

    // state 2 - create job instance in db
    val timestampCreate = Calendar.getInstance().getTimeInMillis
    Job().updateJob(jobDAO.fieldID, jobDAO.fieldState + 1, timestampCreate)

    new Thread(new Runnable {
      override def run(): Unit = {
        try {
          val text = Source.fromURL(jobDAO.fieldSrc)("UTF-8").mkString
          try {
            val hexResult = AlgoUtils.getHex(text, jobDAO.fieldAlgo)
            val timestampEnd = Calendar.getInstance().getTimeInMillis
            // state 3 - ok algo result
            Job().updateJobOk(jobDAO.fieldID, jobDAO.fieldState + 1, timestampCreate, Option(timestampEnd), Option(hexResult))
            runningThreads -= 1
          } catch {
            case e: AlgoTypeException =>
              e.printStackTrace()
              // state 4 - error while executing algo
              val timestampEnd = Calendar.getInstance().getTimeInMillis
              Job().updateJobKo(jobDAO.fieldID, jobDAO.fieldState + 1, timestampCreate, Option(timestampEnd), Option(e.toString))
              runningThreads -= 1
            case e: Exception =>
              e.printStackTrace()
              // state 4 - error while executing algo
              val timestampEnd = Calendar.getInstance().getTimeInMillis
              Job().updateJobKo(jobDAO.fieldID, jobDAO.fieldState + 1, timestampCreate, Option(timestampEnd), Option(e.toString))
              runningThreads -= 1
          }
        } catch {
          case e: Exception =>
            e.printStackTrace()
            // state 4 - error while executing algo
            val timestampEnd = Calendar.getInstance().getTimeInMillis
            Job().updateJobKo(jobDAO.fieldID, jobDAO.fieldState + 1, timestampCreate, Option(timestampEnd), Option(e.toString))
            runningThreads -= 1
            throw e
        }
      }
    }).start()
  }
}

object JobUtils {

  private val MaxRunningThreads = 10
  private var jobUtils: JobUtils = _

  def getInstance(): JobUtils = {
    if (jobUtils == null) {
      jobUtils = new JobUtils
    }
    jobUtils
  }
}
