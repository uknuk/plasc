package plasc

import scala.io.Source
import java.io.{File, FileNotFoundException}
import scalafx.scene.control.{Label, TabPane, Tab, ScrollPane, Button}
import scalafx.scene.layout.{FlowPane, HBox, VBox}
import scalafx.scene.input.{KeyCode, KeyEvent}
import scalafx.geometry.{Orientation}
import scalafx.event.ActionEvent
import scalafx.Includes._

object Player {
  var art = ""
  var selected = ""
  var alb = ""
  var pos = 0

  val labels = Array("art", "alb","track", "sel")
    .map(k => (k, new Label {
      style = "-fx-text-fill: green"
    })
  ).toMap

  val control = new HBox(10);

  val panes =  Array("tracks", "alb", "art")
    .map(k => (k, new FlowPane(Orientation.HORIZONTAL)))
    .toMap

  labels.values.map(lbl => control.children.add(lbl))


  val lbl = new Label("Tracks") {
    style =  "-fx-text-fill: green"
  }

  val grid = new VBox(10)
  grid.children.addAll(control, lbl, makeScroll(panes("tracks")), labels("sel"), panes("alb"))

  val root =  new TabPane {
    tabs = List(
      new Tab {
        text = "Player"
        content = grid
      },
      new Tab {
        text = "Search"
        content = makeScroll(panes("art"))
      }
    )
    onKeyReleased = handle { e: KeyEvent => {
      println(e)
      if (e.code == KeyCode.F1) {
        val sm = selectionModel()
        sm.select(1 - sm.selectedIndex())
      }
    }
    }
  }

  root.selectionModel().select(1)
  loadArtists
  loadLast
  //loadArtists

  def loadLast() : Unit = {
    try {
      val data = Source.fromFile(sys.env("HOME") + "/.mhlast").getLines.toList
      this.art = data(0)
      this.selected = this.art
      this.alb = data(1)
      this.pos = data(2).toInt
      loadAlbums(new File(this.art))
    }
    catch {
      case ex: FileNotFoundException => {
        return
      }
    }
  }

  def loadAlbums(dir :File) = {
    this.root.selectionModel().select(0)
    this.labels("sel").text = s"Albums of ${dir.getName}:"
    println(dir.listFiles.toList.sorted)
    //show(dir.listFiles.toList.sorted, "alb", loadTracks, 40)
  }

  def loadArtists() = {
    val dirs =  Source.fromFile(sys.env("HOME") + "/.mhdirs").getLines.toList(0).split("\\s+")
    val files = dirs.map (d => (new File(d)).listFiles.toList)
    show(files.flatten, "art", loadAlbums, 25)
  }

  def show(files: Array[File], pane: String, fun: File => Unit, size: Int) = {
    this.panes(pane).children.clear
    files.map (f => {
      val btn = new Button(f.getName()) {
        onAction = handle { fun(f) }
      }
      this.panes(pane).children.add(btn)
    }
    )
  }



  def makeScroll(pane: FlowPane) : ScrollPane  = {
     new ScrollPane {
       pannable = true
       fitToWidth = true
       content = pane
     }
  }


}


    

