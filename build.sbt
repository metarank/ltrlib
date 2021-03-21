import Deps._

name := "ltrlib"

version := "0.1"

scalaVersion := "2.13.5"

organization := "me.dfdx"

logBuffered in Test := false

scalacOptions ++= Seq("-feature", "-deprecation")

libraryDependencies ++= Seq(
  "org.scalatest"        %% "scalatest"       % scalatestVersion % Test,
  "org.scalactic"        %% "scalactic"       % scalatestVersion % Test,
  "org.scalatestplus"    %% "scalacheck-1-14" % "3.2.2.0"        % Test,
  "com.github.pathikrit" %% "better-files"    % "3.9.1",
  "org.slf4j"             % "slf4j-api"       % "1.7.30",
  "org.slf4j"             % "slf4j-simple"    % "1.7.30"         % Test,
  "org.apache.commons"    % "commons-math3"   % "3.6.1",
  "io.github.metarank"   %% "cfor"            % "0.2"
)
