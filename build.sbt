import java.nio.file.{Files, StandardCopyOption}
import com.typesafe.sbt.packager.SettingsHelper._
import com.typesafe.sbt.osgi.SbtOsgi.autoImport._

lazy val jsDeps = JsDependencies
lazy val npmDeps = NpmDependencies
lazy val javaDeps = JavaDependencies

// The location for the local maven repository
lazy val m2Repo = "file://" + System.getProperty("maven.repo.local", System.getProperty("user.home") + "/.m2/repository")

lazy val global = Def.settings(
  Global/scalariformAutoformat := false,
  Global/scalariformWithBaseDirectory := true,

  Global/testlogDirectory := target.value / "testlog",

  Global/useGpg := false,
  Global/pgpPublicRing := baseDirectory.value / "project" / ".gnupg" / "pubring.gpg",
  Global/pgpSecretRing := baseDirectory.value / "project" / ".gnupg" / "secring.gpg",
  Global/pgpPassphrase := sys.env.get("PGP_PASS").map(_.toArray)
)

// General settings for subprojects to be published
lazy val doPublish = Seq(
  publishMavenStyle := true,
  publishArtifact in Test := false,
  credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if(isSnapshot.value) {
      Some("snapshots" at nexus + "content/repositories/snapshots")
    } else {
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
    }
  }
)

// General settings for subprojects not to be published
lazy val noPublish = Seq(
  publishArtifact := false,
  publishLocal := {}
)

// General settings for subprojects using NPM
lazy val npmSettings = Seq(
  useYarn := true,
  npmDependencies.in(Compile) := Seq(
    npmDeps.react,
    npmDeps.reactDom,
    npmDeps.jsDom,
    npmDeps.materialUi,
    npmDeps.materialIcons,
    npmDeps.jsonWebToken
  )
)

// *******************************************************************************************************
// Overall settings for this build
// *******************************************************************************************************
inThisBuild(Seq(
  organization := ProjectDeprecated.organization,
  version := "0.5-SNAPSHOT",
  scalaVersion := crossScalaVersions.value.head,
  crossScalaVersions := Seq("2.12.8"),
  scalacOptions in Compile ++= Seq(
    "-deprecation",
    "-feature"
  ),
  javacOptions in Compile ++= Seq(
    "-source", "1.8",
    "-target", "1.8"
  ),
  licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),
  publishArtifact in Test := false,

  resolvers ++= Seq(
    Resolver.sonatypeRepo("snapshots"),
    "Maven2 Local" at m2Repo
  )
))


// *******************************************************************************************************
// The root project
// *******************************************************************************************************
lazy val root = project.in(file("."))
  .settings(noPublish)
  .settings(global)
  .aggregate(common, router, components, materialGen, material, app, server, uitest, sampleApp)

lazy val router = MgmtUiRouter.project
lazy val common = MgmtUiCommon.project
lazy val components = MgmtUiComponents.project
lazy val materialGen = MgmtUiMaterialGen.project
lazy val material = MgmtUiMaterial.project
lazy val sampleApp = MgmtUiSampleApp.project

// *******************************************************************************************************
// The sub project for the Blended Management Console
// *******************************************************************************************************
lazy val app = project.in(file("mgmt-app"))
  .settings(
    name := "mgmt-app",

    webpackBundlingMode := scalajsbundler.BundlingMode.LibraryAndApplication(),
    fastOptJS/emitSourceMaps := true,
    fullOptJS/emitSourceMaps := false,
    scalaJSUseMainModuleInitializer := true,

    Compile/fastOptJS/webpack := {
      val result = (Compile/fastOptJS/webpack).toTask.value
      val dir = baseDirectory.value / "index-dev.html"
      val t = target.value / ("scala-" + scalaBinaryVersion.value) / "scalajs-bundler" / "main" / "index-dev.html"

      Files.copy(dir.toPath, t.toPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES)
      result
    },

    libraryDependencies ++= Seq(
      jsDeps.akkaJsActor.value,
      jsDeps.scalaJsDom.value,
      jsDeps.react4s.value,
      jsDeps.prickle.value,
      jsDeps.blendedUpdaterConfig.value,
      jsDeps.blendedSecurity.value,
      jsDeps.scalaTestJs.value % "test"
    ),

    topLevelDirectory := None,

    Universal/mappings ++= (Compile/fullOptJS/webpack).value.map{ f =>
      f.data -> s"assets/${f.data.getName()}"
    } ++ Seq(
      target.value / ("scala-" + scalaBinaryVersion.value) / "scalajs-bundler" / "main" / "node_modules" / "react" / "umd" / "react.production.min.js" -> "assets/react.production.min.js",
      target.value / ("scala-" + scalaBinaryVersion.value) / "scalajs-bundler" / "main" / "node_modules" / "react-dom" / "umd" / "react-dom.production.min.js" -> "assets/react-dom.production.min.js"
    ),

    makeDeploymentSettings(Universal, Universal/packageBin, "zip"),
    addArtifact(Universal/packageBin/artifact, Universal/packageBin),

    publish := publish.dependsOn(Universal/publish).value,
    publishM2 := publishM2.dependsOn(Universal/publishM2).value,
    publishLocal := publishLocal.dependsOn(Universal/publishLocal).value
  )
  .settings(doPublish:_*)
  .settings(npmSettings:_*)
  .enablePlugins(ScalaJSBundlerPlugin,UniversalPlugin,UniversalDeployPlugin)
  .dependsOn(router, common, components)

// *******************************************************************************************************
// The sub project for the deployable Akka Http Server Module
// *******************************************************************************************************
lazy val server = project.in(file("server"))
  .settings(

    libraryDependencies ++= Seq(
      javaDeps.dominoOsgi,
      javaDeps.blendedDomino,
      javaDeps.blendedAkkaHttp,
      javaDeps.akkaStream
    ),

    name := "blended.mgmt.ui.server",
    moduleName := "blended.mgmt.ui.server",

    Compile/resourceGenerators += Def.task {
      val jsFiles : Seq[(File, String)]= ((app/Compile/fullOptJS/webpack).value.map(f => f.data).filterNot(_.getName().contains("bundle")) ++
      Seq(
        (app/target).value / ("scala-" + scalaBinaryVersion.value) / "scalajs-bundler" / "main" / "node_modules" / "react" / "umd" / "react.production.min.js",
        (app/target).value / ("scala-" + scalaBinaryVersion.value) / "scalajs-bundler" / "main" / "node_modules" / "react-dom" / "umd" / "react-dom.production.min.js"
      )).map { f => (f, "assets/" + f.getName()) } ++
      Seq(
        (app/baseDirectory).value / "src" / "universal" / "index.html" -> "index.html"
      )

      jsFiles.map { case (srcFile, mapping) =>
        val targetFile = (Compile/resourceManaged).value / "webapp" / mapping
        Files.createDirectories(targetFile.getParentFile().toPath())
        Files.copy(srcFile.toPath, targetFile.toPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES)
        targetFile
      }
    }.taskValue,

    Compile/packageBin := {
      val foo = OsgiKeys.bundle.value
      (Compile/packageBin).value
    },
    publishM2 := publishM2.dependsOn(Compile/packageBin).value
  )
  .settings(OsgiHelper.osgiSettings(
    bundleActivator = "blended.mgmt.ui.server.internal.UiServerActivator",
    privatePackage = Seq("blended.mgmt.ui.server.internal")
  ))
  .enablePlugins(SbtOsgi)

// *******************************************************************************************************
// The sub project for the Selenium Tests for the Management Console
// *******************************************************************************************************
lazy val uitest = project.in(file("mgmt-app-test"))
  .settings(
    name := "uitest",

    Test/fork := true,
    Test/javaOptions ++= Seq(
      "-DappUnderTest=" + ((app/target).value / ("scala-" + scalaBinaryVersion.value) / "scalajs-bundler" / "main"),
      "-Dwebdriver.chrome.driver=/opt/chromedriver"
    ),

    Test/test := {
      (Test/test).dependsOn((app/Compile/fastOptJS/webpack).toTask).value
    },

    libraryDependencies ++= Seq(
      javaDeps.scalaTest % "test",
      javaDeps.selenium % "test",
      javaDeps.akkaHttp % "test",
      javaDeps.akkaHttpTestkit % "test"
    )
  )
  .settings(noPublish)

