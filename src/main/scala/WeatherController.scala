package com.gatorcse.weather

import cats._
import cats.syntax.all._
import cats.effect._
import cats.syntax.all._
import com.gatorcse.weather.Coordinate.{Latitude, Longitude}
import org.http4s._
import org.http4s.implicits._
import org.http4s.dsl.Http4sDsl
import org.http4s.circe._
import io.circe.syntax._
import org.typelevel.log4cats.Logger

class WeatherController[F[_]: Monad: Logger](weatherService: WeatherService[F]) {

  val dsl = Http4sDsl[F]; import dsl._
  def routes = HttpRoutes.of[F] {
    case GET -> Root / "weather" :? Latitude.matcher(lat) +& Longitude.matcher(long) => {
      import org.http4s.circe.CirceEntityEncoder._
        Logger[F].info("handling request") *>
        weatherService.weatherAt(Coordinate(lat, long)).flatMap {
          case Some(report) => Ok(report.asJson)
          case None => NotFound()
        }
    }
  }
}
