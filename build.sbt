val Organization = "me.huzi.gitbucket"
val ProjectName = "gitbucket-desktopnotify-plugin"
val ProjectVersion = "5.0.0"

organization := Organization
name := ProjectName
version := ProjectVersion
scalaVersion := "2.13.18"
gitbucketVersion := "4.48.0"

scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % "test"

// Tests run against a throwaway GITBUCKET_HOME
Test / fork := true
Test / javaOptions += s"-Dgitbucket.home=${(Test / target).value / "gitbucket-home"}"
