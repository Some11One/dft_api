package ru.digestjobtracker.util

import org.json.{JSONArray, JSONObject}
import ru.digestjobtracker.database.tables.Job
import ru.digestjobtracker.database.tables.Job._
import ru.digestjobtracker.database.tables.User.{FieldID, FieldName}
import ru.digestjobtracker.database.tables.{JobDAO, UserDAO}
import ru.digestjobtracker.exceptions.ApiException

/**
  * Created by ndmelentev on 04.07.17.
  *
  * Class to easily construct server responses
  *
  */
object ManagerUtils {

  def simpleErrorResponse(apiException: ApiException): String = {
    new JSONObject().put("STATUS", apiException.status).put("ERROR", apiException.getMessage).toString()
  }

  def simpleSuccessResponse(): String = {
    new JSONObject().put("STATUS", "200").toString()
  }

  def userListingResponse(users: Seq[UserDAO]): String = {
    val userArray = new JSONArray()
    for (userDAO <- users) {
      userArray.put(new JSONObject().put("STATUS", "200").put(FieldID, userDAO.fieldID).put(FieldName, userDAO.fieldName).toString())
    }
    userArray.toString()
  }

  def userJobsResponse(userId: String, jobs: Seq[JobDAO]): String = {
    val jobArray = new JSONArray()
    for (job <- jobs) {
      jobArray.put(new JSONObject().put(Job.FieldID, job.fieldID).put(FieldState, job.fieldState)
        .put(FieldTimestampCreate, job.fieldTimestampCreate.get).put(FieldTimestampEnd, job.fieldTimestampEnd.getOrElse(""))
        .put(FieldSrc, job.fieldSrc).put(FieldAlgo, job.fieldAlgo)
        .put(FieldHex, job.fieldHex.getOrElse("")).put(FieldError, job.fieldError.getOrElse("")))
    }
    new JSONObject().put("STATUS", "200").put(FieldUserID, userId).put("job_array", jobArray).toString()
  }
}
