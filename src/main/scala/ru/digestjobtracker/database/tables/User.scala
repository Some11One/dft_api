package ru.digestjobtracker.database.tables

import java.sql.{ResultSet, SQLException}

import ru.digestjobtracker.database.DatabaseSettings
import ru.digestjobtracker.database.tables.User.{FieldID, FieldName, Table}
import ru.digestjobtracker.exceptions.UserNotFoundException

import scala.collection.mutable.ListBuffer

case class User() extends DatabaseSettings {

  /**
    * Insert new User instance into DB
    *
    * @param name user name
    * @return user id
    */
  def insertUser(name: String): UserDAO = {
    val s = connect().createStatement()
    try {
      s.execute(s"INSERT INTO $Table ($FieldName) VALUES ('$name');")
      val userID = s.executeQuery(s"SELECT max($FieldID) FROM $Table")
      if (userID.next()) {
        UserDAO(userID.getString("max").toInt, name)
      } else {
        throw new UserNotFoundException()
      }
    } finally {
      if (s != null) s.close()
    }
  }

  /**
    * Remove User instance from DB
    *
    * @param userId user id
    */
  def deleteUser(userId: String): Unit = {
    val s = connect().createStatement()
    try {
      s.execute(s"DELETE FROM $Table WHERE $FieldID = $userId;")
    } finally {
      if (s != null) s.close()
    }
  }

  /**
    * Get user from DB
    *
    * @param userId user id
    * @return user
    */
  def selectUser(userId: String): UserDAO = {
    val s = connect().createStatement()
    try {
      val res: ResultSet = s.executeQuery(s"SELECT * FROM $Table WHERE $FieldID = $userId")
      if (res.next()) {
        UserDAO(res.getString(FieldID).toInt, res.getString(FieldName))
      } else {
        throw new UserNotFoundException()
      }
    } finally {
      if (s != null) s.close()
    }
  }

  /**
    * Get all users from DB
    *
    * @return list of users
    */
  def selectAllUsers(): ListBuffer[UserDAO] = {
    val users = new ListBuffer[UserDAO]()
    val s = connect().createStatement()
    try {
      val res: ResultSet = s.executeQuery(s"SELECT * FROM $Table")
      while (res.next()) {
        users += UserDAO(res.getString(FieldID).toInt, res.getString(FieldName))
      }
      users
    } finally {
      if (s != null) s.close()
    }
  }

  /**
    * Create table of users
    */
  def createTable(): Unit = {
    val s = connect().createStatement()
    try {
      s.execute(s"DROP TABLE $Table")
      s.execute(s"CREATE TABLE $Table ( $FieldID SERIAL, $FieldName varchar);")
    } catch {
      case e: SQLException =>
        e.printStackTrace()
    } finally {
      if (s != null) s.close()
    }
  }
}

object User {
  val Table = "Users"

  val FieldID = "user_id" // pk
  val FieldName = "name"
}

case class UserDAO(fieldID: Int, fieldName: String)