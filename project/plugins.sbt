// Automatic reload of app on changes via SBT
addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")

// Automatic copyright headers
addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.0.0")

// Packaging for distribution
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.9")

// For Heroku distribution
addSbtPlugin("com.heroku" % "sbt-heroku" % "2.1.2")
