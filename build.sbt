val Http4sVersion         = "0.20.11"
val CirceVersion          = "0.11.1"
val MonixVersion          = "3.0.0"
val LogbackVersion        = "1.2.3"
val TypesafeConfigVersion = "1.3.4"
val SpymemcachedVersion   = "2.12.3"

lazy val root = (project in file("."))
  .enablePlugins(AutomateHeaderPlugin)
  .enablePlugins(JavaServerAppPackaging)
  .enablePlugins(SystemdPlugin)
  .enablePlugins(DebianPlugin)
  .settings(
    organization := "org.alexn",
    name := "vimeo-utils",
    version := "0.0.4",
    scalaVersion := "2.12.10",
    scalacOptions ++= Seq("-Ypartial-unification"),
    libraryDependencies ++= Seq(
      "org.http4s"     %% "http4s-blaze-server"  % Http4sVersion,
      "org.http4s"     %% "http4s-blaze-client"  % Http4sVersion,
      "org.http4s"     %% "http4s-circe"         % Http4sVersion,
      "org.http4s"     %% "http4s-dsl"           % Http4sVersion,
      "io.circe"       %% "circe-generic"        % CirceVersion,
      "io.circe"       %% "circe-generic-extras" % CirceVersion,
      "io.circe"       %% "circe-parser"         % CirceVersion,
      "io.monix"       %% "monix"                % MonixVersion,
      "ch.qos.logback" %  "logback-classic"      % LogbackVersion,
      "com.typesafe"   %  "config"               % TypesafeConfigVersion,
      "net.spy"        % "spymemcached"          % SpymemcachedVersion
    ),
    addCompilerPlugin("org.spire-math" %% "kind-projector"     % "0.9.10"),
    addCompilerPlugin("com.olegpy"     %% "better-monadic-for" % "0.3.1"),
    // Macro enhancements, soon to be integrated in the Scala compiler
    // (in the future 2.13 version)
    addCompilerPlugin(
      // For JSON decoder derivation
      "org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full
    ),

    // For automatic headers, enabled by sbt-header
    headerLicense := Some(HeaderLicense.Custom(
      """|Copyright (c) 2019 Alexandru Nedelcu.
         |
         |This program is free software: you can redistribute it and/or modify
         |it under the terms of the GNU General Public License as published by
         |the Free Software Foundation, either version 3 of the License, or
         |(at your option) any later version.
         |
         |This program is distributed in the hope that it will be useful,
         |but WITHOUT ANY WARRANTY; without even the implied warranty of
         |MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
         |GNU General Public License for more details.
         |
         |You should have received a copy of the GNU General Public License
         |along with this program. If not, see <http://www.gnu.org/licenses/>."""
        .stripMargin)),

    // Needed for packaging
    maintainer := "Alexandru Nedelcu <noreply@alexn.org>",
    packageSummary := "Vimeo Download Plus",
    packageDescription := "A server for discovering and redirecting to Vimeo raw video files",
    debianPackageDependencies := Seq("openjdk-8-jdk"),
    /*
    linuxPackageMappings += {
      val file = sourceDirectory.value / "universal" / "application-sample.conf"
      packageMapping( (file, "/etc/oriel-cmp-backend/application-sample.conf") )
    }*/

    // For Heroku deployment
    herokuAppName in Compile := "vimeo-utils"
  )

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-language:higherKinds",
  "-language:postfixOps",
  "-feature",
  "-Ypartial-unification",
)
