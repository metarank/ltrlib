import Deps._

name := "ltrlib"

version := "0.2.5"

scalaVersion := "2.13.13"

crossScalaVersions := List("2.13.13", "2.12.19", "3.4.1")

organization := "io.github.metarank"

Test / logBuffered := false

Test / parallelExecution := false

scalacOptions ++= Seq("-feature", "-deprecation", "-release:8")

libraryDependencies ++= Seq(
  "org.scalatest"          %% "scalatest"               % scalatestVersion % Test,
  "org.scalatest"          %% "scalatest-propspec"      % scalatestVersion % Test,
  "org.scalactic"          %% "scalactic"               % scalatestVersion % Test,
  "org.scalatestplus"      %% "scalacheck-1-16"         % "3.2.14.0"       % Test,
  "com.github.pathikrit"   %% "better-files"            % "3.9.2",
  "org.slf4j"               % "slf4j-api"               % slf4jversion,
  "org.slf4j"               % "slf4j-simple"            % slf4jversion     % Test,
  "org.apache.commons"      % "commons-math3"           % "3.6.1",
  "io.github.metarank"     %% "cfor"                    % "0.3",
  "io.github.metarank"      % "lightgbm4j"              % "4.1.0-2",
  "io.github.metarank"      % "xgboost-java"            % "2.0.2-1",
  "com.opencsv"             % "opencsv"                 % "5.9",
  "org.scala-lang.modules" %% "scala-collection-compat" % "2.11.0",
  "io.github.metarank"      % "catboost-train-java"     % "1.2.2-1",
  "ai.catboost"             % "catboost-prediction"     % "1.2.3",
  "it.unimi.dsi"            % "fastutil"                % "8.5.13"
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
