package com.gatorcse.weather

import cats._
import cats.syntax.all._
import cats.effect._
import cats.effect.syntax.all._
import com.comcast.ip4s._
import fs2.io.net.Network
import org.http4s.HttpApp
import org.http4s.dsl.io._
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.implicits._
import org.http4s.ember.server._
import org.http4s.ember.server.EmberServerBuilder
import org.typelevel.log4cats.slf4j.Slf4jLogger


object Server extends IOApp.Simple {

  override def run: IO[Unit] = runF[IO]

  /**
   * Ripped from the http4s docs. In production, should take host/port
   * from Environment Variables before defaulting to local:8080
   */
  def runF[F[_]: Async: Network](): F[Unit] =
    makeApp[F].flatMap { app =>
      EmberServerBuilder
      .default[F]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(app)
      .build
    }
      .use(_ => Async[F].never[Unit])
      .as(ExitCode.Success)

  def makeApp[F[_]: Async: Network]: Resource[F, HttpApp[F]] =
    EmberClientBuilder.default[F].build.map { client =>
      implicit val logger = Slf4jLogger.getLogger[F]
      val noaaApi = new NOAAWeatherRepository[F](client)(uri"https://api.weather.gov")
      val service: WeatherService[F] = new NOAAWeatherService[F](noaaApi)
      val routes = new WeatherController[F](service).routes
      routes.orNotFound
    }
}
