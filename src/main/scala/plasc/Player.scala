package plasc

import java.io.{File, FileNotFoundException, PrintWriter}
import scalafx.Includes._
import scala.io.Source
import scalafx.scene.control.{Label, TabPane, Tab, ScrollPane, Button}
import scalafx.scene.layout.{FlowPane, HBox, VBox}
import scalafx.scene.input.{KeyCode, KeyEvent, InputEvent}
import scalafx.geometry.Orientation
import scalafx.event.ActionEvent
import scalafx.scene.media.{MediaPlayer, Media}

object Player {
  var art = " "
  var selected = " "
  var alb = " "
  var tracks = scala.collection.mutable.ArrayBuffer[File]()
  var pos = 0
  var mp = None : Option[MediaPlayer]

  val re = "\\.mp3$|\\.mp4a$".r

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
      println(e.eventType)
      if (e.code == KeyCode.F1) {
        val sm = selectionModel()
        sm.select(1 - sm.selectedIndex())
      }
    }
    }
  }

  loadLast
  loadArtists

  def loadLast = {
    try {
      val data = Source.fromFile(sys.env("HOME") + "/.mhlast").getLines.toList
      val art = new File(data(0))
      this.selected = art.getPath
      this.pos = data(2).toInt
      loadAlbums(art)
      loadTracks(new File(data(1)), this.pos)
    }
    catch {
      case ex: FileNotFoundException =>
      case ex: java.lang.IndexOutOfBoundsException =>
    }
  }

  def loadAlbums(dir :File) = {
    this.root.selectionModel().select(0)
    this.labels("sel").text = s"Albums of ${dir.getName}:"
    show(dir.listFiles.toList.sorted, "alb", playTracks, 40)
    this.selected = dir.getPath
  }

  def loadArtists = {
    val dirs =  Source.fromFile(sys.env("HOME") + "/.mhdirs").getLines.toList(0).split("\\s+")
    val files = dirs.map (d => (new File(d)).listFiles.toList)
    show(files.flatten.toList.sorted, "art", loadAlbums, 25)
  }

  def playTracks(alb: File) = {
    loadTracks(alb, 0)
  }

  def loadTracks(alb: File, idx: Int) = {
    this.tracks.clear
    this.panes("tracks").children.clear

    if (alb.isFile && this.re.findFirstIn(alb.getName).nonEmpty) {
      this.tracks += alb
      this.labels("alb").text = this.re.replaceFirstIn(alb.getName, "")
    }
    else {
      loadDir(alb);
      this.labels("alb").text = alb.getName
    }

    this.alb = alb.getPath
    this.art = this.selected
    this.labels("art").text = this.selected.split("/").last
    play(idx)
  }

  def loadDir(dir: File): Unit = {
    dir.listFiles.toList.sorted.map (file => {
      if (file.isDirectory) {
        loadDir(file)
      }
      else if (this.re.findFirstIn(file.getName).nonEmpty) {
        val btn = makeButton(file, 30)
        val n = this.tracks.length
        btn.onAction = handle { play(n) }
        this.panes("tracks").children.add(btn)
        this.tracks += file
      }
    }
    )
  }

  def show(files: List[File], pane: String, fun: File => Unit, size: Int) = {
    this.panes(pane).children.clear
    files.map (file => {
      val btn = makeButton(file, size)
      btn.onAction = handle { fun(file) }
      this.panes(pane).children.add(btn)
    }
    )
  }

  def play(i: Int): Unit = {
    this.mp match {
      case Some(mp) => mp.stop
      case None =>
    }

    if (i >= 0)
      this.pos = i
    else
      this.pos += 1

    val track = this.tracks(this.pos)
    val m = new Media(track.toURI.toString)
    val mp = new MediaPlayer(m) {
      onEndOfMedia = play(-1)
    }
    mp.play
    this.mp = Some(mp)
    this.labels("track").text = track.getName
    store
  }

  def store = {
    val out = new PrintWriter(sys.env("HOME") + "/.mhlast")
    Array(this.art, this.alb).map(s => out.println(s))
    out.println(this.pos)
    out.close
  }

  def makeButton(file: File, size: Int) = {
    new Button(this.re.replaceFirstIn(file.getName, "").slice(0, size)) {
      mnemonicParsing = false
      style = "-fx-text-fill: blue"
    }
  }

  def makeScroll(pane: FlowPane) : ScrollPane  = {
     new ScrollPane {
       pannable = true
       fitToWidth = true
       content = pane
     }
  }

}
