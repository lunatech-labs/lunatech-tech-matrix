package models.db

import com.typesafe.scalalogging.LazyLogging
import common.DBConnection
import models.{Tech, TechType}
import slick.lifted.{ProvenShape, TableQuery}

import scala.concurrent._

import CustomPostgresProfile.api._

class Techs(tag: Tag) extends Table[models.Tech](tag, "tech") {
  def id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)

  def name: Rep[String] = column[String]("tech_name") // label in lowercase

  def label: Rep[String] = column[String]("tech_label")

  def techType: Rep[TechType] = column[TechType]("tech_type")

  def * : ProvenShape[Tech] = (id.?, name, label, techType) <> ((models.Tech.apply _).tupled, models.Tech.unapply)
}

object Techs extends LazyLogging {
  val techTable: TableQuery[Techs] = TableQuery[Techs]

  def add(tech: Tech)(implicit connection: DBConnection): Future[Int] = {
    logger.info("adding new tech {}", tech)
    val normalizedTech = tech.copy(name = tech.name.toLowerCase) // fornt-end does the same, here is second line of defence
    connection.db.run((techTable returning techTable.map(_.id)) += normalizedTech)
  }

  def search(query: String)(implicit connection: DBConnection): Future[Seq[Tech]] = {
    val likeQuery = "%" + query + "%"
    val searchQuery = techTable.filter(_.name like likeQuery.toLowerCase)
    connection.db.run(searchQuery.result)
  }

  def getTechIdByName(tech: Tech)(implicit connection: DBConnection): Future[Option[Int]] = {
    val getTechIdQuery = techTable.filter(t => t.name === tech.name.toLowerCase).map(_.id).take(1)
    connection.db.run(getTechIdQuery.result.headOption)
  }

  def getTechByName(tech: Tech)(implicit connection: DBConnection): Future[Option[Tech]] = {
    val getTechIdQuery = techTable.filter(t => t.name === tech.name.toLowerCase).take(1)
    connection.db.run(getTechIdQuery.result.headOption)
  }

  def getAllTech(implicit connection: DBConnection): Future[Seq[Tech]] = {
    connection.db.run(techTable.result)
  }

  def getTechById(techId: Int)(implicit connection: DBConnection): Future[Option[Tech]] = {
    val getByIdQuery = techTable.filter(_.id === techId)
    connection.db.run(getByIdQuery.result.headOption)
  }

  def updateTech(techId: Int, tech: Tech)(implicit connection: DBConnection): Future[Int] = {
    logger.info("updating tech {}", tech)
    val action = techTable
      .filter(_.id === techId)
      .map(t => (t.label, t.techType))
      .update((tech.label, tech.techType))

    connection.db.run(action)
  }

  def removeTech(techId: Int)(implicit connection: DBConnection): Future[Int] = {
    val action = techTable.filter(_.id === techId).delete
    connection.db.run(action)
  }
}
