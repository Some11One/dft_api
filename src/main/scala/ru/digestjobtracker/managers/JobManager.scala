package ru.digestjobtracker.managers

import ru.digestjobtracker.database.tables.{Job, JobDAO, User}
import ru.digestjobtracker.exceptions._
import ru.digestjobtracker.util.JobUtils

/**
  * Created by ndmelentev on 06.07.17.
  */
case class JobManager() {

  def read(userId: String): (String, Seq[JobDAO]) = {
    if (userId == null) {
      throw new NoUserException
    } else {
      val userDAO = User().selectUser(userId)
      (userDAO.fieldID.toString, Job().selectAllUserJobs(userId))
    }
  }

  def create(userId: String, src: String, algo: String): Unit = {
    if (userId == null) {
      throw new NoUserException
    } else if (src == "") {
      throw new NoSrcException
    } else if (algo == "") {
      throw new NoAlgoException
    } else {
      val userDAO = User().selectUser(userId)
      JobUtils.getInstance().addUserJob(userDAO, src, algo)
    }
  }

  def delete(userId: String, jobId: String): Unit = {
    if (userId == null) {
      throw new NoUserException
    } else if (jobId == null) {
      throw new NoJobException
    } else {
      val userJobs = Job().selectAllUserJobs(userId)
      for (jobDAO: JobDAO <- userJobs) {
        if (jobDAO.fieldID == jobId.toInt) {
          val userDAO = User().selectUser(userId)
          if (jobDAO.fieldState == 1) {
            // cancel job
            JobUtils.getInstance().cancelUserJob(userDAO, jobId)
          }
          // delete job
          Job().deleteJob(jobId)
        }
      }
    }
  }
}
