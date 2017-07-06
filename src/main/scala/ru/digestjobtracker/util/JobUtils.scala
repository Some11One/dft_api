package ru.digestjobtracker.util

import java.util.Calendar

import ru.digestjobtracker.database.tables.{Job, JobDAO, UserDAO}
import ru.digestjobtracker.exceptions.AlgoTypeException
import ru.digestjobtracker.util.ManagerUtils.simpleErrorResponse

import scala.collection.mutable
import scala.io.Source
import java.util.concurrent.{ConcurrentHashMap, ConcurrentLinkedQueue}

import JobUtils.MaxRunningThreads

/**
  * Created by ndmelentev on 05.07.17.
  */
class JobUtils {

  private val usersJobs = new ConcurrentHashMap[UserDAO, mutable.Queue[JobDAO]]()
  @volatile private var runningThreads = 0

  {
    processJobs()
  }

  def getUserJobs(userDAO: UserDAO): mutable.Queue[JobDAO] = {
    usersJobs.getOrDefault(userDAO, mutable.Queue.empty)
  }

  def addUserJob(userDAO: UserDAO, src: String, algo: String): Unit = {
    val userJobs = usersJobs.getOrDefault(userDAO, mutable.Queue.empty)
    userJobs += JobDAO(fieldUserID = userDAO.fieldID, fieldState = 1, fieldSrc = src, fieldAlgo = algo)
    usersJobs.put(userDAO, userJobs)
  }

  def processJobs(): Unit = {
    new Thread(new Runnable {
      override def run(): Unit = {
        while (true) {
          val keySetIterator = usersJobs.keySet().iterator
          while ( {
            keySetIterator.hasNext
          }) {
            val userDAO = keySetIterator.next()
            val userJobs = usersJobs.get(userDAO)
            for (jobDAO <- userJobs) {
              if (runningThreads <= MaxRunningThreads) {
                runningThreads += 1
                startUserJob(userDAO, jobDAO.fieldSrc, jobDAO.fieldAlgo)
              }
            }
          }
          Thread.sleep(1000)
        }
      }
    }).start()
  }

  def startUserJob(userDAO: UserDAO, src: String, algo: String): Unit = {

    // state 2 - create job instance in db
    val timestampCreate = Calendar.getInstance().getTimeInMillis
    val jobDAO = Job().insertJob(userDAO.fieldID, 2, src, algo, timestampCreate)

    new Thread(new Runnable {
      override def run(): Unit = {
        try {
          val text = Source.fromURL(src)("UTF-8").mkString
          try {
            val hexResult = AlgoUtils.getHex(text, algo)
            val timestampEnd = Calendar.getInstance().getTimeInMillis
            // state 3
            Job().updateJob(jobDAO.fieldID.get, 3, timestampEnd, hexResult, null)
            runningThreads -= 1
          } catch {
            case e: AlgoTypeException =>
              e.printStackTrace()
              // state 4
              val timestampEnd = Calendar.getInstance().getTimeInMillis
              Job().updateJob(jobDAO.fieldID.get, 4, timestampEnd, null, e.getStackTrace.toString)
              runningThreads -= 1
            case e: Exception =>
              e.printStackTrace()
              // state 4
              val timestampEnd = Calendar.getInstance().getTimeInMillis
              Job().updateJob(jobDAO.fieldID.get, 4, timestampEnd, null, e.getStackTrace.toString)
              runningThreads -= 1
          }
        } catch {
          case e: Exception =>
            e.printStackTrace()
            // state 4
            val timestampEnd = Calendar.getInstance().getTimeInMillis
            Job().updateJob(jobDAO.fieldID.get, 4, timestampEnd, null, e.getStackTrace.toString)
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
