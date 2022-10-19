package com.gatorcse.weather

import cats._
import cats.data.OptionT
import cats.syntax.all._
import eu.timepit.refined._
import eu.timepit.refined.api._
import eu.timepit.refined.numeric._
import io.circe._
import io.circe.refined._
import io.circe.generic.semiauto._
import monix.newtypes._
import monix.newtypes.integrations.DerivedCirceCodec
import org.http4s.dsl.impl.QueryParamDecoderMatcher
import org.http4s.{ParseFailure, QueryParamDecoder}
import org.typelevel.log4cats.Logger

case class Coordinate(latitude: Coordinate.Latitude, longitude: Coordinate.Longitude)
object Coordinate {

  private type LongitudeRange = Interval.Closed[-180, 180]
  private type LatitudeRange = Interval.Closed[-90, 90]

  type Longitude = Longitude.Type
  object Longitude extends NewtypeWrapped[Float Refined LongitudeRange] with DerivedCirceCodec {
    implicit val longQueryDecoder: QueryParamDecoder[Longitude] =
      QueryParamDecoder[Float].emap { f => refineV[LongitudeRange](f).map(unsafeCoerce).left.map(ParseFailure.apply("Failed to parse Longitude", _)) }

    object matcher extends QueryParamDecoderMatcher[Longitude]("long")
  }

  type Latitude = Latitude.Type
  object Latitude extends NewtypeWrapped[Float Refined LatitudeRange] with DerivedCirceCodec {
    implicit val latQueryDecoder: QueryParamDecoder[Latitude] =
      QueryParamDecoder[Float].emap { f => refineV[LatitudeRange](f).map(unsafeCoerce).left.map(ParseFailure.apply("Failed to parse Latitude", _)) }

    object matcher extends QueryParamDecoderMatcher[Latitude]("lat")
  }

  implicit val coordinateCodec: Codec[Coordinate] = deriveCodec
}

case class WeatherReport(short: String, temp: String) // TODO: Enum?
object WeatherReport {
  def fromForecast(forecast: PeriodForecast): WeatherReport = {
    val temp = forecast.temperature match {
      case t if t < 40 => "cold"
      case t if t < 60 => "cool"
      case t if t < 80 => "warm"
      case _ => "hot"
    }
    WeatherReport(forecast.shortForecast, temp)
  }
  implicit val reportCodec: Codec[WeatherReport] = deriveCodec
}

trait WeatherService[F[_]] {
  def weatherAt(coordinate: Coordinate): F[Option[WeatherReport]]
}

class NOAAWeatherService[F[_]: Monad: Logger](api: NOAAWeatherRepository[F]) extends WeatherService[F] {
  override def weatherAt(coordinate: Coordinate): F[Option[WeatherReport]] =
    OptionT(api.infoForPoint(coordinate))
      .flatMapF(api.weatherAtGrid)
      .map(WeatherReport.fromForecast)
      .value
}
