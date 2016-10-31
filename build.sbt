name := "metrics_cache"

version in ThisBuild := "0.0.1"

organization in ThisBuild := "com.pacb"

scalaVersion in ThisBuild := "2.11.8"

scalacOptions in ThisBuild := Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-feature", "-language:postfixOps")

libraryDependencies ++= Seq(
  "org.specs2" % "specs2_2.11" % "2.4.1-scalaz-7.0.6" % "test"
)

lazy val metrics_cache = (project in file(".")).dependsOn(file_backed_logs, metrics)

lazy val file_backed_logs = RootProject(uri("https://github.com/jfalkner/file_backed_logs.git#v0.0.8"))
lazy val metrics = RootProject(uri("https://github.com/jfalkner/metrics.git#0.1.3"))
