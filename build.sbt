import Deps._

name := "ltrlib"

version := "0.1.3"

scalaVersion := "2.12.15"

crossScalaVersions := List("2.13.6", "2.12.15")

organization := "io.github.metarank"

Test / logBuffered := false

Test / parallelExecution := false

scalacOptions ++= Seq("-feature", "-deprecation")

libraryDependencies ++= Seq(
  "org.scalatest"          %% "scalatest"               % scalatestVersion % Test,
  "org.scalatest"          %% "scalatest-propspec"      % scalatestVersion % Test,
  "org.scalactic"          %% "scalactic"               % scalatestVersion % Test,
  "org.scalatestplus"      %% "scalacheck-1-14"         % "3.2.2.0"        % Test,
  "com.github.pathikrit"   %% "better-files"            % "3.9.1",
  "org.slf4j"               % "slf4j-api"               % slf4jversion,
  "org.slf4j"               % "slf4j-simple"            % slf4jversion     % Test,
  "org.apache.commons"      % "commons-math3"           % "3.6.1",
  "io.github.metarank"     %% "cfor"                    % "0.2",
  "io.github.metarank"      % "lightgbm4j"              % "3.2.1-1",
  "io.github.metarank"      % "xgboost-java"            % "1.3.3-3",
  "com.o19s"                % "RankyMcRankFace"         % "0.2.0",
  "org.scala-lang.modules" %% "scala-collection-compat" % "2.6.0"
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
