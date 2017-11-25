package com.github.gvolpe.smartbackpacker.http

import cats.effect.IO
import com.github.gvolpe.smartbackpacker.model.VisaRestrictionsIndex
import com.github.gvolpe.smartbackpacker.persistence.VisaRestrictionsIndexDao
import com.github.gvolpe.smartbackpacker.service.VisaRestrictionIndexService
import com.github.gvolpe.smartbackpacker.{IOAssertion, model}
import org.http4s.{HttpService, Request, Status, Uri}
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpecLike, Matchers}

class VisaRestrictionIndexHttpEndpointSpec extends FlatSpecLike with Matchers with VisaRestrictionIndexFixture {

  forAll(examples) { (countryCode, expectedStatus, httpService) =>
    it should s"try to retrieve visa restriction index for $countryCode" in IOAssertion {
      val request = Request[IO](uri = Uri(path = s"/visa-restriction-index/$countryCode"))

      httpService(request).value.map { task =>
        task.fold(fail("Empty response")){ response =>
          response.status should be (expectedStatus)
        }
      }
    }
  }

}

trait VisaRestrictionIndexFixture extends PropertyChecks {

  object MockVisaRestrictionIndexDao extends VisaRestrictionsIndexDao[IO] {
    override def findIndex(countryCode: model.CountryCode): IO[Option[model.VisaRestrictionsIndex]] =
      IO {
        if (countryCode.value == "AR") Some(VisaRestrictionsIndex(0,0,0))
        else None
      }
  }

  object FailedVisaRestrictionIndexDao extends VisaRestrictionsIndexDao[IO] {
    override def findIndex(countryCode: model.CountryCode): IO[Option[model.VisaRestrictionsIndex]] =
      IO.raiseError(new Exception("test"))
  }

  private val goodHttpService: HttpService[IO] = new VisaRestrictionIndexHttpEndpoint(
    new VisaRestrictionIndexService[IO](MockVisaRestrictionIndexDao)
  ).service

  private val badHttpService: HttpService[IO] = new VisaRestrictionIndexHttpEndpoint(
    new VisaRestrictionIndexService[IO](FailedVisaRestrictionIndexDao)
  ).service

  val examples = Table(
    ("countryCode", "expectedStatus", "httpService"),
    ("AR", Status.Ok, goodHttpService),
    ("XX", Status.NotFound, goodHttpService),
    ("IE", Status.BadRequest, badHttpService)
  )

}