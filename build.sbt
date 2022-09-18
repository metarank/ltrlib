import Deps._

name := "ltrlib"

version := "0.1.14"

scalaVersion := "2.12.17"

crossScalaVersions := List("2.13.8", "2.12.17")

organization := "io.github.metarank"

Test / logBuffered := false

Test / parallelExecution := false

scalacOptions ++= Seq("-feature", "-deprecation", "-target:jvm-1.8")

libraryDependencies ++= Seq(
  "org.scalatest"          %% "scalatest"               % scalatestVersion % Test,
  "org.scalatest"          %% "scalatest-propspec"      % scalatestVersion % Test,
  "org.scalactic"          %% "scalactic"               % scalatestVersion % Test,
  "org.scalatestplus"      %% "scalacheck-1-16"         % "3.2.13.0"       % Test,
  "com.github.pathikrit"   %% "better-files"            % "3.9.1",
  "org.slf4j"               % "slf4j-api"               % slf4jversion,
  "org.slf4j"               % "slf4j-simple"            % slf4jversion     % Test,
  "org.apache.commons"      % "commons-math3"           % "3.6.1",
  "io.github.metarank"     %% "cfor"                    % "0.2",
  "io.github.metarank"      % "lightgbm4j"              % "3.3.2-1",
  "io.github.metarank"      % "xgboost-java"            % "1.6.1-2",
  "com.opencsv"             % "opencsv"                 % "5.7.0",
  "org.scala-lang.modules" %% "scala-collection-compat" % "2.8.1"
)

sonatypeProfileName := "io.github.metarank"

publishMavenStyle := true

publishTo := sonatypePublishToBundle.value

licenses := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

homepage := Some(url("https://github.com/metarank/ltrlib"))
scmInfo := Some(
  ScmInfo(
    url("https://github.com/metarank/ltrlib"),
    "scm:git@github.com:metarank/ltrlib.git"
  )
)
developers := List(
  Developer(id = "romangrebennikov", name = "Roman Grebennikov", email = "grv@dfdx.me", url = url("https://dfdx.me/"))
)
