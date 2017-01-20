name := "metrics_cache"

version in ThisBuild := "0.1.0"

organization in ThisBuild := "com.pacb"

scalaVersion in ThisBuild := "2.11.8"

scalacOptions in ThisBuild := Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-feature", "-language:postfixOps")

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2-core" % "3.8.5" % "test"
)

lazy val metrics_cache = (project in file(".")).dependsOn(metrics)

lazy val metrics = RootProject(uri("https://github.com/jfalkner/metrics.git#0.2.3"))
