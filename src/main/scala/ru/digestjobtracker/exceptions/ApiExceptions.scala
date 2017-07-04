package ru.digestjobtracker.exceptions

import scala.util.control.NoStackTrace

/**
  * Created by ndmelentev on 04.07.17.
  */
sealed abstract class ApiException(val status: Int, val code: Int, message: String)
  extends RuntimeException(message) with NoStackTrace {
  def this(status: Int, code: Int) = this(status, code, "")
}

// bad request

//class NoDeviceUidException extends ApiException(400, 2, "No 'device_uid' header")

// not found

class UserNotFoundException extends ApiException(404, 1, "User not found")

