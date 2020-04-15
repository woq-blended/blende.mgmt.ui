import org.scalajs.sbtplugin.ScalaJSPlugin.AutoImport.emitSourceMaps
import phoenix.ProjectFactory
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport.webpackBundlingMode
import sbt._

object MgmtUiComponents extends ProjectFactory{

  // scalastyle:off object.name
  object config extends ProjectSettings {
  // scalastyle:on object.name

    override def projectName: String = "blended.mgmt.ui.components"
    override def description: String = "Reusable components for building Material UI's"

    override def projectDir: Option[String] = Some("components")

    override def plugins: Seq[AutoPlugin] = super.plugins ++ Seq(
      ScalaJSBundlerPlugin
    )

    override def deps : Def.Initialize[Seq[ModuleID]] = Def.setting(super.deps.value ++ Seq(
      JsDependencies.blendedJmx.value,
      JsDependencies.react4s.value
    ))

    override def dependsOn: Seq[ClasspathDep[ProjectReference]] = Seq(
      MgmtUiTheme.project,
      MgmtUiCommon.project,
      MgmtUiMaterial.project
    )

    override def settings: Seq[sbt.Setting[_]] = super.settings ++ Seq(
      webpackBundlingMode := scalajsbundler.BundlingMode.LibraryOnly(),
      emitSourceMaps := true
    )
  }
}

