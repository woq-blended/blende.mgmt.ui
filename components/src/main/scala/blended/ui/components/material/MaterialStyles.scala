package blended.ui.components.material

import com.github.ahnfelt.react4s.{CssClass, S}

import scala.scalajs.js

abstract class MaterialStyles {

  val theme : js.Dynamic

  // Applied to the Browser window as a hole
  object RootStyles extends CssClass (
    S.flexGrow.number(1),
    S.height.percent(100),
    S.width.percent(100),
    S.zIndex.number(1),
    S.overflow("hidden"),
    S.position.absolute(),
    S.display("flex")
  )

  // Applied to the Application Bar
  object AppBarStyles extends CssClass (
    S.position.fixed(),
    S.zIndex.number(theme.zIndex.drawer.asInstanceOf[Int] + 1)
  )

  // Applied to the side Menu
  object MenuDrawerStyles extends CssClass (
    S.position.relative(),
    S.height.percent(100),
    S.width.px(200)
  )

  // Applied to the (everything next to the menu drawer including the space UNDER the AppBar)
  object ContentStyles extends CssClass (
    S.position.relative(),
    S.width.percent(100),
    S.display("flex"),
    S.flexDirection("column"),
  )

  // Applied the the content Area (same as content without the AppBar)
  object ContentArea extends CssClass (
    ContentStyles,
    S.height.percent(100),
    S.background(theme.palette.background.default.asInstanceOf[String]),
    S.padding.pt(theme.spacing.unit.asInstanceOf[Int] * 3)
  )

}
