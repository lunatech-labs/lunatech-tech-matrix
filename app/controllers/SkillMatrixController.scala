package controllers

import javax.inject.{Inject, Singleton}

import common.{ApiErrors, Authentication, UserRequest}
import io.kanaka.monadic.dsl._
import models._
import play.api.libs.json._
import play.api.mvc._
import services.{SkillMatrixService, TechService}
import models.ImplicitFormats._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._

@Singleton
class SkillMatrixController @Inject()(skillMatrixService: SkillMatrixService,
                                      techService: TechService,
                                      auth: Authentication,
                                      components: ControllerComponents
                                     ) extends AbstractController(components) {

  def addSkill(): Action[JsValue] = auth.UserAction().async(components.parsers.json) { request =>
      for {
        skillMatrixItem: SkillMatrixItem <- request.body.validate[SkillMatrixItem]                                             ?| (err =>  ApiErrors.badRequest(JsError.toJson(err)))
        skillId <- skillMatrixService.addUserSkill(request.user.getUserId, skillMatrixItem.tech, skillMatrixItem.skillLevel)   ?| (err => ApiErrors.internalServerError(err.getMessage))
        tech <- techService.getTechByName(skillMatrixItem.tech)                                                                ?| ApiErrors.INTERNAL_SERVER_ERROR
      } yield Created(Json.toJson(constructSkillMatrixItem(tech, skillId, skillMatrixItem)))
  }

  //TODO: rethink the update operation. In the body of the request we only need the new skill level
  def updateSkill(skillId: Int): Action[JsValue] = auth.UserAction().async(components.parsers.json) { implicit request =>
    for {
      skillMatrixItem: SkillMatrixItem <- request.body.validate[SkillMatrixItem]                                                        ?| (err =>  ApiErrors.badRequest(JsError.toJson(err)))
      _ <- validateTechIdPresentForUpdateOperation(skillMatrixItem)                                                                     ?| ApiErrors.badRequest(getBadRequestResponseForUpdateOperation)
      _ <- skillMatrixService.updateUserSkill(skillId, request.user.getUserId, skillMatrixItem.tech.id.get, skillMatrixItem.skillLevel) ?| ApiErrors.SKILL_NOT_FOUND
    }
      yield Ok(Json.toJson(SkillMatrixItem(skillMatrixItem.tech, skillMatrixItem.skillLevel, Some(skillId))))
  }

  def deleteSkill(skillId: Int): Action[Unit] = auth.UserAction().async(components.parsers.empty) { implicit request =>
    for {
      _ <- skillMatrixService.deleteUserSkill(request.user.getUserId, skillId) ?| ApiErrors.SKILL_NOT_FOUND
    } yield NoContent
  }

  def getUserSkills(userId: Int): Action[Unit] = auth.UserAction().async(components.parsers.empty) { _ =>
    for {
      userSkills <- skillMatrixService.getUserSkills(userId) ?| ApiErrors.USER_NOT_FOUND
    } yield Ok(Json.toJson(userSkills))
  }

  def getUserSkillsByEmail(email: String): Action[Unit] = auth.UserAction(AccessLevel.Management).async(components.parsers.empty) { _ =>
    for {
      userSkills <- skillMatrixService.getUserSkills(email) ?| ApiErrors.USER_NOT_FOUND
    } yield Ok(Json.toJson(userSkills))
  }

  def getSkillMatrix: Action[Unit] = auth.UserAction().async(components.parsers.empty) { _ =>
    skillMatrixService.getAllSkills.map( result => Ok(Json.toJson(result)))
  }

  def getSkillMatrixByTechId(techId: Int): Action[Unit] = auth.UserAction().async(components.parsers.empty) { _ =>
    for {
      skillMatrixForTech <- skillMatrixService.getSkillMatrixByTechId(techId) ?| ApiErrors.TECH_NOT_FOUND
    } yield Ok(Json.toJson(skillMatrixForTech))
  }

  def getMySkills: Action[AnyContent] = auth.UserAction().async { implicit request =>
    skillMatrixService.getUserSkills(request.user.getUserId).map( result => Ok(Json.toJson(result)) )
  }

  private val getBadRequestResponseForUpdateOperation: JsValue = {
    Json.parse(
      """{
        "obj.tech.id": [
      {
        "msg": [
        "error.path.missing"
        ],
        "args": []
      }
        ]
      }""".stripMargin)
  }
  private def validateTechIdPresentForUpdateOperation(skillMatrixItem: SkillMatrixItem): Future[Option[Int]] = skillMatrixItem.tech.id match {
    case None => Future(None)
    case id => Future(id)
  }
  private def constructSkillMatrixItem(tech: Tech, skillId: Int, skillMatrixItem: SkillMatrixItem) =
    SkillMatrixItem(
      tech,
      skillMatrixItem.skillLevel,
      Some(skillId)
    )

}
