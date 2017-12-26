package com.github.gvolpe.smartbackpacker.repository

import com.github.gvolpe.smartbackpacker.model.{Airline, AirlineName, Country, CountryCode, Health, VisaRequirementsData, VisaRestrictionsIndex}

object algebra {

  trait AirlineRepository[F[_]] {
    def findAirline(airlineName: AirlineName): F[Option[Airline]]
  }

  trait HealthRepository[F[_]] {
    def findHealthInfo(countryCode: CountryCode): F[Option[Health]]
  }

  trait VisaRequirementsRepository[F[_]] {
    def findVisaRequirements(from: CountryCode, to: CountryCode): F[Option[VisaRequirementsData]]
  }

  trait VisaRestrictionsIndexRepository[F[_]] {
    def findRestrictionsIndex(countryCode: CountryCode): F[Option[VisaRestrictionsIndex]]
  }

  trait CountryRepository[F[_]] {
    def findAll: F[List[Country]]
    def findSchengen: F[List[Country]]
  }

}