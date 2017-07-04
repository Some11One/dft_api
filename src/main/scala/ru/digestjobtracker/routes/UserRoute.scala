package ru.digestjobtracker.routes

import org.restexpress.{Request, Response}
import ru.digestjobtracker.database.tables.User
import ru.digestjobtracker.exceptions.UserNotFoundException
import ru.digestjobtracker.util.ManagerUtils.{simpleErrorResponse, userResponse, userListingResponse}

class UserRoute {

  /**
    * GET, get user
    *
    * Request can have:
    * 'id' - header, representing user's unique identifier (Text). If none - all users will be loaded from DB
    *
    */
  def read(request: Request, response: Response): String = {
    val userID = request.getHeader(User.FieldID)
    if (userID == null) {
      userListingResponse(User().selectAll())
    } else {
      try {
        userResponse(User().selectUser(userID))
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
    * 'name' - representing user's name
    *
    */
  def create(request: Request, response: Response): String = {
    try {
      val name = request.getHeader("name")
      if (name == null) {
        val user = User().insert("")
        userResponse(user)
      } else {
        val user = User().insert(name)
        userResponse(user)
      }
    } catch {
      case e: UserNotFoundException =>
        e.printStackTrace()
        simpleErrorResponse(e)
    }
  }
}
