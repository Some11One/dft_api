package ru.dijestjobtracker.managers

import org.scalatest.FunSuite
import ru.digestjobtracker.exceptions.{NoAlgoException, NoSrcException, NoUserException, UserNotFoundException}
import ru.digestjobtracker.managers.{JobManager, UserManager}

/**
  * Created by ndmelentev on 06.07.17.
  */
class JobManagerIntTest extends FunSuite {

  val jobManager: JobManager = JobManager()
  val userManager: UserManager = UserManager()

  test("read all user jobs when empty") {
    val user = userManager.create("Tom")
    val userId = user.head.fieldID.toString
    assert(jobManager.read(userId)._2.isEmpty)
  }

  test("read all user jobs when he does not exist or when none provided") {
    val userId = "42"
    userManager.delete(userId)
    intercept[UserNotFoundException] {
      jobManager.read(userId)
    }
    intercept[NoUserException] {
      jobManager.read(null)
    }
  }

  test("create job") {
    intercept[NoUserException] {
      jobManager.create(null, null, null)
    }

    val userId = userManager.create("Tom").head.fieldID.toString
    intercept[NoSrcException] {
      jobManager.create(userId, "", null)
    }

    val src = "https://yandex.ru/search/?text=donut&from=os&clid=1836587&lr=213"
    intercept[NoAlgoException] {
      jobManager.create(userId, src, "")
    }

    val algo = "md5"
    jobManager.create(userId, src, algo)
    Thread.sleep(1000) // wait for job to complete

    val jobs = jobManager.read(userId)._2
    var jobCreated = false
    for (job <- jobs) {
      if (job.fieldAlgo == algo && job.fieldSrc == src && job.fieldUserID == userId.toInt) {
        jobCreated = true
      }
    }

    assert(jobCreated)

  }
}
