package ru.digestjobtracker.managers

import ru.digestjobtracker.database.tables.{User, UserDAO}

/**
  * Created by ndmelentev on 06.07.17.
  */
case class UserManager() {
  def read(userId: String): Seq[UserDAO] = {
    if (userId == null) {
      User().selectAllUsers()
    } else {
      Seq(User().selectUser(userId))
    }
  }

  def create(name: String): Seq[UserDAO] = {
    if (name == null) {
      Seq(User().insertUser(""))
    } else {
      Seq(User().insertUser(name))
    }
  }

  def delete(userId: String): Unit = {
    User().deleteUser(userId)
  }
}
