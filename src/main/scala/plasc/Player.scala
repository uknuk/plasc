package plasc

import scala.io.Source
import java.io.FileNotFoundException
import scalafx.scene.control.{Label, TabPane, Tab, ScrollPane}
import scalafx.scene.layout.{FlowPane, HBox}
import scalafx.scene.input.{KeyCode, KeyEvent}
import scalafx.geometry.{Orientation}

object Player {
  val lastFile = ".mhlast"

  val labels = Array("art", "alb","track")
    .map(k => (k, new Label {
      style = "-fx-text-fill: green"
    })
  ).toMap;

  val control = new HBox(10);
  val tScroll = new ScrollPane {
       pannable = true
       fitToWidth = true
    }

  val flows =  Array("tracks", "alb", "art")
    .map(k => (k, new FlowPane(Orientation.HORIZONTAL)))
      .toMap

  tScroll.content = flows("tracks");

  labels.values.map(lbl => control.children.add(lbl))

  val root =  new TabPane {
    tabs = List(
      new Tab {
        text = "Player"
      },
      new Tab {
        text = "Search"
      }
    )

    // onKeyReleased = { e: KeyEvent =>
    //   if (e.code == KeyCode.F1) {
    //     val sm = selectionModel()
    //     sm.select(1 - sm.selectedIndex())
    //   }
    // }

  }

  root.selectionModel().select(1)
  loadLast

  def loadLast() : Unit = {
    try {
      val data = Source.fromFile(sys.env("HOME") + s"/${this.lastFile}").getLines.toList
      println(data)
    }
    catch {
      case ex: FileNotFoundException => {
        return
      }
    }

  }

}


    

