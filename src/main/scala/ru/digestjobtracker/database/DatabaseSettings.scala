package ru.digestjobtracker.database

import java.sql.{Connection, DriverManager, SQLException}

trait DatabaseSettings {

  private val DB = "jdbc:postgresql://188.166.36.161:5432/photos" // - old db from previous pet project, no means of renaming 'photos'
  private val USER = "postgres"
  private val PASSWORD = "pass17"

  def createTable(): Unit

  def insert(): Unit

  def connect(): Connection = {
    Class.forName("org.postgresql.Driver")
    DriverManager.getConnection(DB, USER, PASSWORD)
  }

}
