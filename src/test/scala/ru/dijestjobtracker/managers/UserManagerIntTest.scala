package ru.dijestjobtracker.managers

import org.scalatest.FunSuite
import ru.digestjobtracker.managers.UserManager
import ru.digestjobtracker.exceptions.UserNotFoundException

import scala.collection.mutable.ListBuffer

/**
  * Created by ndmelentev on 06.07.17.
  */
class UserManagerIntTest extends FunSuite {

  val userManager: UserManager = UserManager()

  test("delete all users, insert user and read him") {
    val userId = null // to fetch all users
    val users = userManager.read(userId)
    for (user <- users) {
      userManager.delete(user.fieldID.toString)
    }
    assert(userManager.read(userId).isEmpty)

    val newUserName = "Tom"
    val newUser = userManager.create(newUserName)
    val updateUsers = userManager.read(newUser.head.fieldID.toString)
    assert(updateUsers.head.fieldName == newUserName)
  }

  test("read user that is not in DB") {
    val userId = "42"
    userManager.delete(userId)
    intercept[UserNotFoundException] {
      userManager.read(userId)
    }
  }

  test("create some users") {
    val names = Seq("Tom", "Jerry", "Ms")
    val userIds = ListBuffer[String]()
    for (name <- names) {
      userIds += userManager.create(name).head.fieldID.toString
    }

    assert(userManager.read(userIds.head).head.fieldName == names.head)
    assert(userManager.read(userIds(1)).head.fieldName == names(1))
    assert(userManager.read(userIds(2)).head.fieldName == names(2))
  }


}
