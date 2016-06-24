package plasc

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.{Orientation}
import scalafx.scene.Scene
import scalafx.scene.control.{Label, TabPane, Tab}
import scalafx.scene.layout.FlowPane



object Plasc extends JFXApp {
  val lbl = Array("art", "alb","track", "sel").map(k => (k,
    new Label {
      style = "-fx-text-fill: green"
    }
  )).toMap

  val panes = Array("trafl", "alb", "art").map(k => (k, new FlowPane(Orientation.HORIZONTAL)))
    .toMap

  stage = new PrimaryStage {
    title = "Plasc"
    scene = new Scene(1024, 572) {
      root = new TabPane {
        tabs = List(
          new Tab {
            text = "Player"
          },
          new Tab {
            text = "Search"
          }
        )
      }
    }
  }
}
