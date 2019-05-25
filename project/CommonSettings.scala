import phoenix.ProjectConfig
import sbt.Keys._
import sbt._
import xerial.sbt.Sonatype.SonatypeKeys._
import com.typesafe.sbt.SbtScalariform.autoImport._


trait CommonSettings extends ProjectConfig {

  override def settings: Seq[sbt.Setting[_]] = super.settings ++ Seq(
    organization := "de.wayofquality.blended.app",
    homepage := Some(url("https://github.com/woq-blended/blended.mgmt.ui")),

    publishMavenStyle := true,

    licenses += ("Apache 2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),

    scmInfo := Some(
      ScmInfo(
        url("https://github.com/woq-blended/blended.mgmt.ui"),
        "scm:git@github.com:woq-blended/blended.git"
      )
    ),

    developers := List(
      Developer(id = "atooni", name = "Andreas Gies", email = "andreas@wayofquality.de", url = url("https://github.com/atooni")),
      Developer(id = "lefou", name = "Tobias Roeser", email = "tobias.roser@tototec.de", url = url("https://github.com/lefou"))
    ),

    sonatypeProfileName := "de.wayofquality",

    javacOptions in Compile ++= Seq(
      "-source", "1.8",
      "-target", "1.8"
    ),

    scalaVersion := "2.12.8",
    scalacOptions ++= Seq("-deprecation", "-feature", "-Xlint", "-Ywarn-nullary-override"),

    scalariformAutoformat := false,
    scalariformWithBaseDirectory := true
  )
}