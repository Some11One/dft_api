package ru.digestjobtracker.routes

import org.restexpress.{Request, Response}
import ru.digestjobtracker.database.tables.Job
import ru.digestjobtracker.exceptions._
import ru.digestjobtracker.managers.JobManager
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
    try {
      val m = JobManager().read(request.getHeader(Job.FieldUserID))
      userJobsResponse(m._1, m._2)
    } catch {
      case e: ApiException =>
        e.printStackTrace()
        simpleErrorResponse(e)
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
    val queryMap = request.getQueryStringMap
    try {
      JobManager().create(request.getHeader(Job.FieldUserID), queryMap.getOrDefault("src", ""), queryMap.getOrDefault("algo", ""))
      simpleSuccessResponse()
    } catch {
      case e: ApiException =>
        e.printStackTrace()
        simpleErrorResponse(e)
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
    val queryMap = request.getQueryStringMap
    try {
      JobManager().delete(request.getHeader(Job.FieldUserID), queryMap.getOrDefault(Job.FieldID, null))
      simpleSuccessResponse()
    } catch {
      case e: ApiException =>
        e.printStackTrace()
        simpleErrorResponse(e)
    }
  }
}
