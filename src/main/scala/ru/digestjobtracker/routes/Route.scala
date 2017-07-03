package ru.digestjobtracker.routes

import org.restexpress.{Request, Response}
import ru.digestjobtracker.database.tables.User

class Route {

  def read(request: Request, response: Response): String = {
    val user = new User()
    user.insert()
    user.selectAll()
  }
}
