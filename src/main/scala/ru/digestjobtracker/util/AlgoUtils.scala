package ru.digestjobtracker.util

import org.apache.commons.codec.digest.DigestUtils
import ru.digestjobtracker.exceptions.AlgoTypeException

/**
  * Created by ndmelentev on 04.07.17.
  */
object AlgoUtils {

  /**
    * Calculate Hex of a String
    *
    * @param src  source String
    * @param algo type of algorithm, can be - 'md5', 'sha-1' or 'sha-256'
    * @return hex
    */
  def getHex(src: String, algo: String): String = {
    algo match {
      case "md5" => md5Apache(src)
      case "sha-1" => sha1Apache(src)
      case "sha-256" => sha256Apache(src)
      case _ => throw new AlgoTypeException
    }
  }

  private def md5Apache(st: String): String = {
    val md5Hex = DigestUtils.md5Hex(st)
    md5Hex
  }

  private def sha1Apache(st: String): String = {
    val sha1Hex = DigestUtils.sha1(st)
    sha1Hex.toString
  }

  private def sha256Apache(st: String): String = {
    val sha265Hex = DigestUtils.sha256(st)
    sha265Hex.toString
  }

}
