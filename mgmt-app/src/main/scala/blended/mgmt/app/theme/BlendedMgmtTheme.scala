package blended.mgmt.app.theme

import blended.material.ui.Colors
import blended.ui.material.Styles
import com.github.ahnfelt.react4s._

import scala.scalajs.js

object Theme {

  val palette : js.Dynamic  = js.Dynamic.literal(
    "palette" -> js.Dynamic.literal (
      "primary" -> js.Dynamic.literal (
        "main" -> Colors.green("900")
      ),
      "background" -> js.Dynamic.literal (
        "default" -> "#fafafa"
      )
    )
  )

  val theme = Styles.createMuiTheme(palette)
}

object RootStyles extends CssClass (
  S.flexGrow.number(1),
  S.height.percent(100),
  S.width.percent(100),
  S.zIndex.number(1),
  S.overflow("hidden"),
  S.position.absolute(),
  S.display("flex")
)

object AppBarStyles extends CssClass (
  S.position.fixed(),
  S.zIndex.number(Theme.theme.zIndex.drawer.asInstanceOf[Int] + 1)
)

object DrawerStyles extends CssClass (
  S.position.relative(),
  S.height.percent(100),
  S.width.px(200)
)

object ContentStyles extends CssClass (
  S.position.relative(),
  S.width.percent(100),
  S.display("flex"),
  S.flexDirection("column"),
)

object ContentArea extends CssClass (
  ContentStyles,
  S.height.percent(100),
  S.background(Theme.theme.palette.background.default.asInstanceOf[String]),
  S.padding.pt(Theme.theme.spacing.unit.asInstanceOf[Int] * 3)
)



