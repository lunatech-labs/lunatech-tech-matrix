package controllers

import io.kanaka.monadic.dsl._
import javax.inject.{Inject, Singleton}

import common.{ApiErrors, Authentication}
import play.api.libs.json.Json
import play.api.mvc._
import services.UserService
import models.ImplicitFormats._
import models.{AccessLevel, TechFilter}

import scala.concurrent._
import ExecutionContext.Implicits.global

@Singleton
class UserController @Inject()(userService: UserService, auth: Authentication, components: ControllerComponents) extends AbstractController(components) {

  def getUserById(userId: Int): Action[Unit] = auth.UserAction().async(components.parsers.empty) { _ =>
    for {
      user <- userService.getUserById(userId) ?| ApiErrors.USER_NOT_FOUND
    } yield Ok(Json.toJson(user))
  }

  def getAllUsers: Action[Unit] = auth.UserAction(AccessLevel.Management).async(components.parsers.empty) { _ =>
    userService.getAll.map ( users => Ok(Json.toJson(users)) )
  }

  def searchUsers:Action[Seq[TechFilter]] = auth.UserAction().async(components.parsers.json[Seq[TechFilter]]) { r =>
    for {
      users <- userService.searchUsers(r.request.body)
    } yield Ok(Json.toJson(users))
  }

  def removeUser(userId:Int): Action[Unit] = auth.UserAction(AccessLevel.Management).async(components.parsers.empty) { _ =>
    for {
      _ <- userService.removeUser(userId) ?| ApiErrors.USER_NOT_FOUND
    } yield NoContent

  }
}
