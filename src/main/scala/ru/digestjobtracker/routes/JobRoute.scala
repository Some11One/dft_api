package ru.digestjobtracker.routes

import org.restexpress.{Request, Response}
import ru.digestjobtracker.database.tables.{Job, User}
import ru.digestjobtracker.exceptions._
import ru.digestjobtracker.util.JobUtils
import ru.digestjobtracker.util.ManagerUtils.{simpleErrorResponse, simpleSuccessResponse}

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
    //    val userId = request.getHeader(Job.FieldUserID)
    //    if (userId == null) {
    //      simpleErrorResponse(new NoUserException)
    //    } else {
    //      try {
    //        val userDAO = User().selectUser(userId)
    //
    //        // todo: check if there are any jobs running in the background, then extract others from DB
    //
    //
    //
    //        ""
    //      } catch {
    //        case e: UserNotFoundException =>
    //          e.printStackTrace()
    //          simpleErrorResponse(e)
    //      }
    //    }
    Job().createTable()
    ""
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
        if (src == "") {
          simpleErrorResponse(new NoSrcException)
        }

        val algo = queryMap.getOrDefault("algo", "")
        if (algo == "") {
          simpleErrorResponse(new NoAlgoException)
        }

        JobUtils.getInstance().addUserJob(userDAO, src, algo)
        simpleSuccessResponse()
      } catch {
        case e: UserNotFoundException =>
          e.printStackTrace()
          simpleErrorResponse(e)
      }
    }
  }

  /**
    * Delete, remove job entity from DB
    */
  def delete(request: Request, response: Response): String = {
    //    todo: Job().delete()
    ""
  }
}
