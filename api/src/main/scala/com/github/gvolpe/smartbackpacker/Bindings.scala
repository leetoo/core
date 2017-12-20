package com.github.gvolpe.smartbackpacker

import cats.effect.Effect
import com.github.gvolpe.smartbackpacker.http.HttpErrorHandler
import com.github.gvolpe.smartbackpacker.repository.algebra.{AirlineRepository, HealthRepository, VisaRequirementsRepository, VisaRestrictionsIndexRepository}
import com.github.gvolpe.smartbackpacker.repository.{PostgresAirlineRepository, PostgresHealthRepository, PostgresVisaRequirementsRepository, PostgresVisaRestrictionsIndexRepository}
import com.github.gvolpe.smartbackpacker.service._
import doobie.util.transactor.Transactor

// It wires all the instances together
class Bindings[F[_] : Effect] {

  private val devDbUrl  = sys.env.getOrElse("JDBC_DATABASE_URL", "")

  private val dbDriver  = sys.env.getOrElse("SB_DB_DRIVER", "org.postgresql.Driver")
  private val dbUrl     = sys.env.getOrElse("SB_DB_URL", "jdbc:postgresql:sb")
  private val dbUser    = sys.env.getOrElse("SB_DB_USER", "postgres")
  private val dbPass    = sys.env.getOrElse("SB_DB_PASSWORD", "")

  def xa: Transactor[F] = {
    if (devDbUrl.nonEmpty) Transactor.fromDriverManager[F](dbDriver, devDbUrl)
    else Transactor.fromDriverManager[F](dbDriver, dbUrl, dbUser, dbPass)
  }

  lazy val httpErrorHandler: HttpErrorHandler[F] = new HttpErrorHandler[F]

  lazy val ApiToken: Option[String] = sys.env.get("SB_API_TOKEN")

  lazy val visaRestrictionsIndexRepo: VisaRestrictionsIndexRepository[F] =
    new PostgresVisaRestrictionsIndexRepository[F](xa)

  lazy val visaRestrictionsIndexService: VisaRestrictionIndexService[F] =
    new VisaRestrictionIndexService[F](visaRestrictionsIndexRepo)

  lazy val airlineRepo: AirlineRepository[F] =
    new PostgresAirlineRepository[F](xa)

  lazy val airlineService: AirlineService[F] =
    new AirlineService[F](airlineRepo)

  lazy val visaRequirementsRepo: VisaRequirementsRepository[F] =
    new PostgresVisaRequirementsRepository[F](xa)

  lazy val countryService: CountryService[F] =
    new CountryService[F](visaRequirementsRepo, ExchangeRateService[F])

  lazy val healthRepo: HealthRepository[F] =
    new PostgresHealthRepository[F](xa)

  lazy val healthService: HealthService[F] =
    new HealthService[F](healthRepo)

}
