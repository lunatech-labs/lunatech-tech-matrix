package models

import models.EnumTypes.SkillLevel.SkillLevel
import models.EnumTypes.SkillType.SkillType
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

/**
  * Created by tatianamoldovan on 10/02/2017.
  */
case class SkillMatrixItem(skill: Skill, skillLevel: SkillLevel)
object SkillMatrixItem {
  implicit val skillMatrixReads: Reads[SkillMatrixItem] = (
    (JsPath \ "skill").read[Skill] and
      (JsPath \ "skillLevel").read[SkillLevel]
    )(SkillMatrixItem.apply _)
}
