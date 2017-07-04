package ru.digestjobtracker.util

import org.json.{JSONArray, JSONObject}
import ru.digestjobtracker.database.tables.User.{FieldID, FieldName}
import ru.digestjobtracker.database.tables.UserDAO
import ru.digestjobtracker.exceptions.ApiException

/**
  * Created by ndmelentev on 04.07.17.
  */
object ManagerUtils {

  def simpleErrorResponse(apiException: ApiException): String = {
    new JSONObject().put("STATUS", apiException.status).put("ERROR", apiException.getMessage).toString()
  }

  def simpleSuccessResponse(): String = {
    new JSONObject().put("STATUS", "200").toString()
  }

  def userResponse(userDAO: UserDAO): String = {
    new JSONObject().put("STATUS", "200").put(FieldID, userDAO.fieldID).put(FieldName, userDAO.fieldName).toString()
  }

  def userListingResponse(users: Seq[UserDAO]): String = {
    val userArray = new JSONArray()
    for (userDAO <- users) {
      userArray.put(new JSONObject().put("STATUS", "200").put(FieldID, userDAO.fieldID).put(FieldName, userDAO.fieldName).toString())
    }
    userArray.toString()
  }
}