package ru.digestjobtracker.routes

import org.restexpress.{Request, Response}
import ru.digestjobtracker.database.tables.User
import ru.digestjobtracker.exceptions.UserNotFoundException
import ru.digestjobtracker.managers.UserManager
import ru.digestjobtracker.util.ManagerUtils.{simpleErrorResponse, userListingResponse}

class UserRoute {

  /**
    * GET, get user
    *
    * Request headers:
    * 'user_id' - header, representing user's unique identifier. If none specified - all users will be loaded from DB
    *
    */
  def read(request: Request, response: Response): String = {
    try {
      userListingResponse(UserManager().read(request.getHeader(User.FieldID)))
    } catch {
        case e: UserNotFoundException =>
          e.printStackTrace()
          simpleErrorResponse(e)
      }
    }

  /**
    * POST, creating user
    *
    * Request query params:
    * 'name' - user's name. If none specified - blank user name will be used
    *
    */
  def create(request: Request, response: Response): String = {
    try {
      userListingResponse(UserManager().create(request.getHeader(User.FieldName)))
    } catch {
      case e: UserNotFoundException =>
        e.printStackTrace()
        simpleErrorResponse(e)
    }
  }
}
