ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

val circeVersion = "0.14.3"
val http4sVersion = "1.0.0-M37"
val refinedVersion = "0.10.1"

lazy val root = (project in file("."))
  .settings(
    name := "weather",
    idePackagePrefix := Some("com.gatorcse.weather"),
    scalacOptions += "-Ymacro-annotations",
    scalacOptions ++= Seq("-Vimplicits", "-Vtype-diffs"),
    addCompilerPlugin("org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full),
      addCompilerPlugin("io.tryp" % "splain" % "1.0.1" cross CrossVersion.patch),
    libraryDependencies ++= Seq(
        "org.typelevel" %% "cats-core" % "2.8.0",
        "org.typelevel" %% "cats-effect" % "3.3.14",
        "com.beachape" %% "enumeratum" % "1.7.0",
        "io.circe" %% "circe-core" % circeVersion,
        "io.circe" %% "circe-generic" % circeVersion,
        "io.circe" %% "circe-parser" % circeVersion,
        "io.circe" %% "circe-refined" % circeVersion,
        "org.http4s" %% "http4s-dsl" % http4sVersion,
        "org.http4s" %% "http4s-ember-server" % http4sVersion,
        "org.http4s" %% "http4s-ember-client" % http4sVersion,
        "org.http4s" %% "http4s-circe" % http4sVersion,
        "eu.timepit" %% "refined" % refinedVersion,
        "eu.timepit" %% "refined-cats" % refinedVersion,
        "io.monix" %% "newtypes-core" % "0.2.3",
        "io.monix" %% "newtypes-circe-v0-14" % "0.2.3",
        "org.typelevel" %% "log4cats-slf4j" % "2.4.0",
        "ch.qos.logback" % "logback-classic" % "1.4.0"
  )
)
