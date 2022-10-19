package com.gatorcse.weather

import cats._
import cats.syntax.all._
import cats.effect._
import cats.effect.syntax.all._
import cats.effect._
import com.gatorcse.weather.NOAAWeatherRepository._
import io.circe._
import io.circe.generic.semiauto._
import monix.newtypes.NewtypeWrapped
import monix.newtypes.integrations.DerivedCirceCodec
import org.http4s._
import org.http4s.circe._
import org.http4s.implicits._
import org.http4s.client._
import org.http4s.client.dsl._
import org.http4s.headers._
import org.typelevel.ci._
import org.typelevel.log4cats.Logger

object NOAAWeatherRepository {
  val userAgent = "(personal project, gatorcse@gmail.com)"

  type GridX = GridX.Type
  object GridX extends NewtypeWrapped[Int] with DerivedCirceCodec

  type GridY = GridY.Type
  object GridY extends NewtypeWrapped[Int] with DerivedCirceCodec

  type GridId = GridId.Type
  object GridId extends NewtypeWrapped[String] with DerivedCirceCodec
}

// TODO: Enum, or at least NewType
case class PointInfo(gridId: GridId, gridX: GridX, gridY: GridY)
object PointInfo {
  implicit val infoCodec: Codec[PointInfo] = deriveCodec
}

case class PeriodForecast(
  number: Int,
  shortForecast: String,
  temperature: Int
)

object PeriodForecast {
  implicit val forecastCodec: Codec[PeriodForecast] = deriveCodec
}

case class ZoneForecast(periods: List[PeriodForecast])
object ZoneForecast {
  implicit val zoneForecastCodec: Codec[ZoneForecast] = deriveCodec
}

class NOAAWeatherRepository[F[_]: Concurrent: Logger](client: Client[F])(base: Uri) {
  private val dsl = Http4sClientDsl[F]; import dsl._

  private implicit val pointInfoJson: EntityDecoder[F, PointInfo] = jsonOf
  def infoForPoint(point: Coordinate): F[Option[PointInfo]] = {
    // TODO: trim decimal precision 👇
    val req: Request[F] = Method.GET(base / "points" / s"${point.latitude.value},${point.longitude.value}")
      .withHeaders(
        `User-Agent`(ProductId("Personal Project"), ProductComment("gatorcse@gmail.com")),
        Accept(MediaRangeAndQValue.withDefaultQValue(MediaType.application.`ld+json`))
      )

    client.expectOption[PointInfo](req)
  }

  private implicit val periodInfoJson: EntityDecoder[F, ZoneForecast] = jsonOf
  def weatherAtGrid(pointInfo: PointInfo): F[Option[PeriodForecast]] = {
    val weatherUri = (base / "gridpoints" / pointInfo.gridId.value / s"${pointInfo.gridX.value},${pointInfo.gridY.value}" / "forecast")
      .withQueryParam("units", "us")

    val req: Request[F] = Method.GET(weatherUri)

      .withHeaders(
        `User-Agent`(ProductId("Personal Project"), ProductComment("gatorcse@gmail.com")),
        Accept(MediaRangeAndQValue.withDefaultQValue(MediaType.application.`ld+json`))
      )

    client.expectOption[ZoneForecast](req)
      .nested
      .map(_.periods.minBy(_.number))
      .value
  }
}
