name := "twitter-stream"

version := "1.0"

lazy val `twitter-stream` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(jdbc, cache, ws, specs2 % Test,
  "com.typesafe.play.extras" %% "iteratees-extras" % "1.5.0")

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

resolvers ++= Seq("scalaz-bintray" at "https://dl.bintray.com/scalaz/releases",
  "Typesafe private" at "https://private-repo.typesafe.com/typesafe/maven-releases")