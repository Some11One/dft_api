package ru.digestjobtracker.util

import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap

import ru.digestjobtracker.database.tables.{Job, JobDAO, UserDAO}
import ru.digestjobtracker.util.JobUtils.MaxRunningThreads

import scala.collection.mutable
import scala.io.Source

/**
  * Created by ndmelentev on 05.07.17.
  *
  * Class to run jobs on server's background and write results in DB
  *
  */
class JobUtils {

  private val usersJobs = new ConcurrentHashMap[UserDAO, mutable.Queue[JobDAO]]()
  private val cancelledUsersJobs = new ConcurrentHashMap[UserDAO, mutable.ListBuffer[Int]]()

  @volatile private var runningThreads = 0

  {
    processJobs()
  }

  /**
    * Return all running user jobs
    *
    * @param userDAO user instance
    * @return jobs queue
    */
  def getUserJobs(userDAO: UserDAO): mutable.Queue[JobDAO] = {
    usersJobs.getOrDefault(userDAO, mutable.Queue.empty)
  }

  /**
    * Add job to user job's queue
    *
    * @param userDAO user instance
    * @param src     source of a file to use in 'algo'
    * @param algo    hex algorithm
    */
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

  /**
    * Cancel user job. Adds it to separate queue which will nullify further tasks running on this job
    *
    * @param userDAO user instance
    * @param jobId   job id
    */
  def cancelUserJob(userDAO: UserDAO, jobId: String): Unit = {
    val userJobs = cancelledUsersJobs.getOrDefault(userDAO, mutable.ListBuffer.empty)
    userJobs += jobId.toInt
    if (cancelledUsersJobs.containsKey(userDAO)) {
      cancelledUsersJobs.replace(userDAO, userJobs)
    } else {
      cancelledUsersJobs.put(userDAO, userJobs)
    }
  }

  /**
    * Starts separate thread to look for jobs queue updates. If found any - starts job in a separate thread
    *
    */
  def processJobs(): Unit = {
    new Thread(new Runnable {
      override def run(): Unit = {
        while (true) {
          val usersIterator = usersJobs.keySet().iterator()
          while ( {
            usersIterator.hasNext
          }) {
            val userDAO = usersIterator.next()
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

  /**
    * Starts job in a separate thread
    *
    * @param jobDAO job instance
    */
  def startUserJob(jobDAO: JobDAO): Unit = {

    // state 2 - create job instance in db
    val timestampCreate = Calendar.getInstance().getTimeInMillis
    Job().updateJob(jobDAO.fieldID, jobDAO.fieldState + 1, timestampCreate)

    new Thread(new Runnable {
      override def run(): Unit = {
        try {
          val text = Source.fromURL(jobDAO.fieldSrc)("UTF-8").mkString
          val hexResult = AlgoUtils.getHex(text, jobDAO.fieldAlgo)

          // state 3 - ok algo result
          val timestampEnd = Calendar.getInstance().getTimeInMillis
          Job().updateJobOK(jobDAO.fieldID, jobDAO.fieldState + 1, timestampCreate, Option(timestampEnd), Option(hexResult))
          runningThreads -= 1

        } catch {
          case e: Exception =>
            e.printStackTrace()

            // state 4 - error while executing algo
            val timestampEnd = Calendar.getInstance().getTimeInMillis
            Job().updateJobKO(jobDAO.fieldID, jobDAO.fieldState + 1, timestampCreate, Option(timestampEnd), Option(e.toString))
            runningThreads -= 1
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
