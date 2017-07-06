package ru.digestjobtracker.routes

import org.restexpress.{Request, Response}
import ru.digestjobtracker.database.tables.{Job, JobDAO, User}
import ru.digestjobtracker.exceptions._
import ru.digestjobtracker.util.JobUtils
import ru.digestjobtracker.util.ManagerUtils.{simpleErrorResponse, simpleSuccessResponse, userJobsResponse}

/**
  * Created by ndmelentev on 04.07.17.
  */
class JobRoute {

  /**
    * Get, read user's jobs
    *
    * Request headers:
    * 'user_id' - user id, who wants to start the job
    *
    */
  def read(request: Request, response: Response): String = {
    //    Job().createTable()
    val userId = request.getHeader(Job.FieldUserID)
    if (userId == null) {
      simpleErrorResponse(new NoUserException)
    } else {
      try {
        val userDAO = User().selectUser(userId)
        userJobsResponse(userDAO, Job().selectAllUserJobs(userId))
      } catch {
        case e: UserNotFoundException =>
          e.printStackTrace()
          simpleErrorResponse(e)
      }
    }
  }

  /**
    * POST, creating job
    *
    * Request headers:
    * 'user_id' - user id, who wants to start the job
    *
    * Request query params:
    * 'src' - file URL, local or not
    * 'algo' - hex algorithm type, can be - 'md5', 'sha-1' or 'sha-256'
    *
    */
  def create(request: Request, response: Response): String = {

    val userId = request.getHeader(Job.FieldUserID)
    if (userId == null) {
      simpleErrorResponse(new NoUserException)
    } else {
      try {
        val userDAO = User().selectUser(userId)
        val queryMap = request.getQueryStringMap

        val src = queryMap.getOrDefault("src", "")
        val algo = queryMap.getOrDefault("algo", "")
        if (src == "") {
          simpleErrorResponse(new NoSrcException)
        } else if (algo == "") {
          simpleErrorResponse(new NoAlgoException)
        } else {
          JobUtils.getInstance().addUserJob(userDAO, src, algo)
          simpleSuccessResponse()
        }
      } catch {
        case e: UserNotFoundException =>
          e.printStackTrace()
          simpleErrorResponse(e)
      }
    }
  }

  /**
    * Delete, remove job entity from DB or from job queue
    *
    * Request headers:
    * 'user_id' - user id, who wants to start the job
    *
    * Request query params:
    * 'job_id' - job id to delete
    *
    */
  def delete(request: Request, response: Response): String = {
    val userId = request.getHeader(Job.FieldUserID)
    val jobId = request.getQueryStringMap.getOrDefault(Job.FieldID, null)
    if (userId == null) {
      simpleErrorResponse(new NoUserException)
    } else if (jobId == null) {
      simpleErrorResponse(new NoJobException)
    } else {
      val userJobs = Job().selectAllUserJobs(userId)
      for (jobDAO: JobDAO <- userJobs) {
        if (jobDAO.fieldID == jobId.toInt) {
          try {
            val userDAO = User().selectUser(userId)
            if (jobDAO.fieldState == 1) {
              // cancel job
              JobUtils.getInstance().cancelUserJob(userDAO, jobId)
            }
            Job().deleteJob(jobId)
          } catch {
            case e: UserNotFoundException =>
              e.printStackTrace()
              simpleErrorResponse(e)
          }
        }
      }

      simpleSuccessResponse()
    }
  }
}
