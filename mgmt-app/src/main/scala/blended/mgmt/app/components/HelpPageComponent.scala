package blended.mgmt.app.components

import com.github.ahnfelt.react4s._

case class HelpPageComponent() extends Component[NoEmit] {

  override def render(get: Get): Node = E.div(
    E.h1(Text("Help"))
  )
}
