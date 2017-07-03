package ru.digestjobtracker

import java.io.IOException

import io.netty.handler.codec.http.HttpMethod
import org.restexpress.RestExpress
import ru.digestjobtracker.routes.Route

object Server {
  private val defaultServerPort = 8889

  @throws[Exception]
  def startServer(args: Array[String]): RestExpress = {
    val server = new RestExpress

    val r = new Route()
    server.uri("v1/route", r).method(HttpMethod.GET).noSerialization

    if (args.length >= 1) {
      val port = Integer.valueOf(args(0))
      server.bind(port)
    } else {
      server.bind(defaultServerPort)
    }

    server
  }

  @throws[IOException]
  def main(args: Array[String]): Unit = {
    try {
      startServer(args)
      print("Listening...")
      while (true) {
        try {
          Thread.sleep(1000)
        } catch {
          case ex: Exception =>
            ex.printStackTrace()
        }
      }
    } catch {
      case ex: Exception =>
        ex.printStackTrace()
    }
  }
}