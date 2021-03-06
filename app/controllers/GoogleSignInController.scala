package controllers

import javax.inject.{Inject, Singleton}

import com.typesafe.scalalogging.LazyLogging
import common.ApiErrors
import play.api.libs.json._
import play.api.mvc._
import services.UserService
import services.oauth.OauthService
import io.kanaka.monadic.dsl._
import models.ImplicitFormats._

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class GoogleSignInController @Inject()(userService: UserService, oauth: OauthService, components: ControllerComponents) extends AbstractController(components) with LazyLogging {

  def authenticate(): Action[JsValue] = Action.async(components.parsers.json) { implicit request =>
    for {
      token <- request.body.\("token").validate[String]                                                      ?| (err => ApiErrors.badRequest(JsError.toJson(err)))
      gUser <- oauth.verifyToken(token)                                                                      ?| ApiErrors.UNAUTHORIZED
      user <- userService.getOrCreateUserByEmail(gUser.givenName, gUser.familyName, gUser.email)             ?| ApiErrors.USER_NOT_FOUND
    } yield Ok(Json.obj("user" -> Json.toJson(user)))
  }
}
