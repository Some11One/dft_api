package ru.digestjobtracker.routes

import org.restexpress.{Request, Response}
import ru.digestjobtracker.database.tables.User
import ru.digestjobtracker.exceptions.UserNotFoundException
import ru.digestjobtracker.util.ManagerUtils.{simpleErrorResponse, userResponse, userListingResponse}

class UserRoute {

  /**
    * GET, get user
    *
    * Request headers:
    * 'id' - header, representing user's unique identifier. If none specified - all users will be loaded from DB
    *
    */
  def read(request: Request, response: Response): String = {
    val id = request.getHeader(User.FieldID)
    if (id == null) {
      userListingResponse(User().selectAllUsers())
    } else {
      try {
        userResponse(User().selectUser(id))
      } catch {
        case e: UserNotFoundException =>
          e.printStackTrace()
          simpleErrorResponse(e)
      }
    }
  }

  /**
    * POST, creating user
    *
    * Request query params:
    * 'name' - user's name. If none specified - blank user name
    *
    */
  def create(request: Request, response: Response): String = {
    try {
      val name = request.getHeader(User.FieldName)
      if (name == null) {
        userResponse(User().insertUser(""))
      } else {
        userResponse(User().insertUser(name))
      }
    } catch {
      case e: UserNotFoundException =>
        e.printStackTrace()
        simpleErrorResponse(e)
    }
  }
}
