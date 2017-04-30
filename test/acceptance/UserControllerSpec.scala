package acceptance

import play.api.test._
import play.api.test.Helpers._
import data.TestData._
import data.TestDatabaseProvider


class UserControllerSpec extends AcceptanceSpec {

  var dataMap: Map[String, Int] = _

  override def beforeAll {
    setupDatabase()
  }

  before {
    dataMap = insertUserData()
  }

  after {
    cleanUserData()
  }

  override def afterAll {
    dropDatabase()
  }

  info("The GET /users/4 for getting the info about an user")
  feature("It should return UserSkillResponse object") {
    scenario("Everything is fine") {
      val request = FakeRequest("GET", s"/users/${dataMap(ID_USER_SNAPE)}").withHeaders(("X-AUTH-TOKEN", authToken))
      val response = route(app, request).get

      status(response) mustEqual 200
      (request, response) must validateAgainstSwagger(swaggerPath)

    }
  }
  feature("It should return 404 when user is not found"){
    scenario("user is not found") {
      val request = FakeRequest("GET", s"/users/8763").withHeaders(("X-AUTH-TOKEN", authToken))
      val response = route(app, request).get

      status(response) mustEqual 404
      (request, response) must validateResponseAgainstSwagger(swaggerPath)
    }
  }

}
