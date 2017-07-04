package ru.digestjobtracker.database.tables

import java.sql.{ResultSet, SQLException}

import ru.digestjobtracker.database.DatabaseSettings
import ru.digestjobtracker.database.tables.User.{FieldID, FieldName, Table}
import ru.digestjobtracker.exceptions.UserNotFoundException

import scala.collection.mutable.{ListBuffer, Seq}

case class User() extends DatabaseSettings {

  def insert(name: String): UserDAO = {
    val s = connect().createStatement()
    try {
      s.execute(s"INSERT INTO $Table ($FieldName) VALUES ('$name');")
      val userID = s.executeQuery(s"SELECT max($FieldID) FROM $Table")
      if (userID.next()) {
        UserDAO(userID.getString("max"), name)
      } else {
        throw new UserNotFoundException()
      }
    } finally {
      if (s != null) s.close()
    }
  }

  def selectUser(userId: String): UserDAO = {
    val s = connect().createStatement()
    try {
      val res: ResultSet = s.executeQuery(s"SELECT * FROM $Table WHERE $FieldID = $userId")
      if (res.next()) {
        UserDAO(res.getString(FieldID), res.getString(FieldName))
      } else {
        throw new UserNotFoundException()
      }
    } finally {
      if (s != null) s.close()
    }
  }

  def selectAll(): ListBuffer[UserDAO] = {
    val users = new ListBuffer[UserDAO]()
    val s = connect().createStatement()
    try {
      val res: ResultSet = s.executeQuery(s"SELECT * FROM $Table")
      while (res.next()) {
        users += UserDAO(res.getString(FieldID), res.getString(FieldName))
      }
      users
    } finally {
      if (s != null) s.close()
    }
  }

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

  val FieldID = "id"
  val FieldName = "name"
}

case class UserDAO(fieldID: String, fieldName: String)