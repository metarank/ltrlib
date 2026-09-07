import Deps._

name := "ltrlib"

version := "0.2.6"

scalaVersion := "3.9.0"

organization := "io.github.metarank"

Test / logBuffered := false

Test / parallelExecution := false

// lightgbm4j JNI lib can be loaded only once per JVM, so tests run in a fresh forked JVM
Test / fork := true

scalacOptions ++= Seq("-feature", "-deprecation", "-release:17")

javacOptions ++= Seq("--release", "17")

libraryDependencies ++= Seq(
  "org.scalatest"        %% "scalatest"           % scalatestVersion % Test,
  "org.scalactic"        %% "scalactic"           % scalatestVersion % Test,
  "com.github.pathikrit" %% "better-files"        % "3.9.2",
  "org.slf4j"             % "slf4j-api"           % slf4jversion,
  "org.slf4j"             % "slf4j-simple"        % slf4jversion     % Test,
  "org.apache.commons"    % "commons-math3"       % "3.6.1",
  "io.github.metarank"   %% "cfor"                % "0.3",
  "io.github.metarank"    % "lightgbm4j"          % "4.6.0-2",
  "io.github.metarank"    % "xgboost-java"        % "2.0.2-1",
  "com.opencsv"           % "opencsv"             % "5.12.0",
  "io.github.metarank"    % "catboost-train-java" % "1.2.2-1",
  "ai.catboost"           % "catboost-prediction" % "1.2.10",
  "it.unimi.dsi"          % "fastutil"            % "8.5.19"
)

publishMavenStyle := true

// sbt 2 built-in Sonatype Central Portal publishing: `publishSigned` stages into target/sona-staging,
// then `sonaRelease` uploads and releases. Credentials via SONATYPE_USERNAME / SONATYPE_PASSWORD
// (Central Portal user token) or a Credentials entry for host central.sonatype.com.
publishTo := {
  val centralSnapshots = "https://central.sonatype.com/repository/maven-snapshots/"
  if (isSnapshot.value) Some("central-snapshots" at centralSnapshots) else localStaging.value
}

licenses := Seq("APL2" -> uri("http://www.apache.org/licenses/LICENSE-2.0.txt"))

homepage := Some(uri("https://github.com/metarank/ltrlib"))
scmInfo := Some(
  ScmInfo(
    uri("https://github.com/metarank/ltrlib"),
    "scm:git@github.com:metarank/ltrlib.git"
  )
)
developers := List(
  Developer(id = "romangrebennikov", name = "Roman Grebennikov", email = "grv@dfdx.me", url = uri("https://dfdx.me/"))
)
