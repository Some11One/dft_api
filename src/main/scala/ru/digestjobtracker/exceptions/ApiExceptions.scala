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

class NoAlgoException extends ApiException(400, 1, "'algo' query parameter not found")

class AlgoTypeException extends ApiException(400, 2, "'algo' query parameter can only be 'md5', 'sha-1' or 'sha-256'")

class NoSrcException extends ApiException(400, 3, "'src' query parameter not found")

class NoUserException extends ApiException(404, 4, "'user_id' header not specified")

// not found

class UserNotFoundException extends ApiException(404, 50, "User not found")

class JobNotFoundException extends ApiException(404, 51, "Job not found")

