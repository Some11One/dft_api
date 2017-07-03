package ru.digestjobtracker.database.tables

import org.json.JSONArray
import org.json.JSONObject
import java.sql.{ResultSet, SQLException}

import ru.digestjobtracker.database.DatabaseSettings

class User extends DatabaseSettings {

  private val testUserId = 42

  override def insert(): Unit = {
    val s = connect().createStatement()
    try {
      s.execute(s"insert into Users (user_id) VALUES ('$testUserId');")
    } catch {
      case e: SQLException =>
        e.printStackTrace()
    } finally {
      if (s != null) s.close()
    }
  }

  //  def selectUser(userId: String): Option[JSONObject] = {
  //    val s = connect().createStatement()
  //    try {
  //      val res: ResultSet = s.executeQuery(s"select user_id from Users where user_id = $userId")
  //      if (res.next()) {
  //        Option(new JSONObject().put("user_id", res.getString("user_id")))
  //      } else {
  //        _
  //      }
  //    } catch {
  //      case e: SQLException =>
  //        throw e
  //    } finally {
  //      if (s != null) s.close()
  //    }
  //  }

  def selectAll(): String = {
    val users = new JSONArray()
    val s = connect().createStatement()
    try {
      val res: ResultSet = s.executeQuery(s"select user_id from Users")
      while (res.next()) {
        val user_id = res.getString("user_id")
        users.put(new JSONObject().put("user_id", user_id))
      }
      users.toString()
    } catch {
      case e: SQLException =>
        throw e
    } finally {
      if (s != null) s.close()
    }
  }

  def createTable(): Unit = {
    val s = connect().createStatement()
    try {
      s.execute("CREATE TABLE Users ( user_id int4 NOT NULL, CONSTRAINT Users_pk PRIMARY KEY (user_id));")
    } catch {
      case e: SQLException =>
        e.printStackTrace()
    } finally {
      if (s != null) s.close()
    }
  }
}
