package ru.digestjobtracker.database.tables

import java.sql.SQLException

import ru.digestjobtracker.database.DatabaseSettings

class Job extends DatabaseSettings {

  private val testUserId = 42

  override def insert(): Unit = {
    val s = connect().createStatement()
    try {
      //      s.execute(s"insert into Jobs (user_id) VALUES ('$testUserId');")
    } catch {
      case e: SQLException =>
        e.printStackTrace()
    } finally {
      if (s != null) s.close()
    }
  }

  /**
    * state - 0, 1, 2
    */
  def createTable(): Unit = {
    val s = connect().createStatement()
    try {
      s.execute("CREATE TABLE Jobs ( job_id int4 NOT NULL, user_id int4, state int4, timestamp_create int8, timestamp_end int8, scr text, algo text, hex text, error text, CONSTRAINT Jobs_pk PRIMARY KEY (job_id, user_id));")
    } catch {
      case e: SQLException =>
        e.printStackTrace()
    } finally {
      if (s != null) s.close()
    }
  }


}
