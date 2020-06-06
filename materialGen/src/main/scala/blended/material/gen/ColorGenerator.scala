package blended.material.gen

import java.util.regex.Pattern

class ColorGenerator(sourceFile : String, targetFile: String) extends AbstractGenerator(sourceFile, targetFile) {

  private[this] val colorNames = {

    val pattern : Pattern = Pattern.compile("var _([^=].*)=.*")

    source.filter { line =>
      val matcher = pattern.matcher(line)
      matcher.matches()
    }.map { line =>
      val matcher = pattern.matcher(line)
      matcher.find()
      matcher.group(1).trim()
    }.filterNot(s => s.matches(".*inter.*") || s.matches("common"))
  }

  private[this] val prelude =
    s"""package ${MaterialGenerator.pkgName}
       |
       |import scala.scalajs.js
       |
       |import scala.scalajs.js.annotation.JSImport
       |
       |object Colors {
       |  @js.native
       |  @JSImport("@material-ui/core/colors", JSImport.Namespace)
       |  private object MatColors extends js.Object {
       |""".stripMargin

  override def doGenerate(): String = {

    val colors = colorNames.map(s => s"    val $s : js.Dynamic = js.native")
    val objects = colorNames.map(s => s"  object $s { def apply(s : String) : js.Dynamic = MatColors.$s.selectDynamic(s) }")

    prelude +
      colors.mkString("", "\n", "\n") +
      "  }\n\n" +
      objects.mkString("", "\n", "\n") +
      "}\n"
  }

}
