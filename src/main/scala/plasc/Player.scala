package plasc

import java.io.{File, FileNotFoundException, PrintWriter}
import scala.io.Source
import scalafx.Includes._
import scalafx.scene.control.{Label, TabPane, Tab, ScrollPane, Button}
import scalafx.scene.layout.{FlowPane, HBox, VBox}
import scalafx.scene.input.{KeyCode, KeyEvent}
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

  val txtLength = Map("art" -> 25, "alb" -> 40, "track" -> 30)
  val fontSize = Map("art" -> 8, "alb" -> 10, "track" -> 9)

  val btn = new Button {
    style = "-fx-text-fill: red"
    onAction = handle {ctlPlay}
  }

  val labels = Array("art", "alb","track", "sel")
    .map (k => (k, new Label { style = "-fx-text-fill: green" }))
    .toMap

  val control = new HBox(10);

  val panes =  Array("tracks", "alb", "art")
    .map(k => (k, new FlowPane(Orientation.HORIZONTAL)))
    .toMap

  labels.values.map(lbl => control.children += lbl)
  control.children += btn

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

    onKeyReleased =  (e: KeyEvent) => {
      println(e.code)
      e.code match {
        case KeyCode.LESS | KeyCode.BACK_QUOTE => {
          val sm = selectionModel()
          sm.select(1 - sm.selectedIndex())
        }
        case KeyCode.QUOTE | KeyCode.BACK_SLASH => ctlPlay
        case _ =>
      }
    }
  }

  loadLast
  loadArtists

  def loadLast = {
    try {
      val data = Source.fromFile(sys.env("HOME") + "/.mhlast").getLines.toList
      val artist = new File(data(0))
      selected = artist.getPath
      pos = data(2).toFloat.toInt
      loadAlbums(artist)
      loadTracks(new File(data(1)), pos)
    }
    catch {
      case ex: FileNotFoundException =>
      case ex: java.lang.IndexOutOfBoundsException =>
    }
  }

  def loadAlbums(dir :File) = {
    root.selectionModel().select(0)
    labels("sel").text = s"Albums of ${dir.getName}:"
    show(dir.listFiles.toList.sorted, "alb", playTracks)
    selected = dir.getPath
  }

  def loadArtists = {
    val dirs = Source.fromFile(sys.env("HOME") + "/.mhdirs").getLines.toList(0).split("\\s+")
    val files = dirs.map (d => (new File(d)).listFiles.toList)
    show(files.flatten.toList.sorted, "art", loadAlbums)
  }

  def playTracks(alb: File) = {
    loadTracks(alb, 0)
  }

  def loadTracks(alb: File, idx: Int) = {
    tracks.clear
    panes("tracks").children.clear

    if (alb.isFile && this.re.findFirstIn(alb.getName).nonEmpty) {
      tracks += alb
      labels("alb").text = this.re.replaceFirstIn(alb.getName, "")
    }
    else {
      loadDir(alb);
      labels("alb").text = alb.getName
    }

    this.alb = alb.getPath
    art = selected
    labels("art").text = selected.split("/").last
    play(idx)
  }

  def loadDir(dir: File): Unit = {
    dir.listFiles.toList.sorted.map {file =>
      if (file.isDirectory) {
        loadDir(file)
      }
      else if (re.findFirstIn(file.getName).nonEmpty) {
        val btn = makeButton(file, "track")
        val n = tracks.length
        btn.onAction = handle { play(n) }
        panes("tracks").children.add(btn)
        tracks += file
      }
    }
  }

  def show(files: List[File], pane: String, fun: File => Unit) = {
    panes(pane).children.clear
    files.map {file =>
      val btn = makeButton(file, pane)
      btn.onAction = handle { fun(file) }
      this.panes(pane).children.add(btn)
    }
  }

  def play(i: Int): Unit = {
    this.mp match {
      case Some(mp) => mp.stop
      case None =>
    }

    if (i >= 0)
      pos = i
    else
      pos += 1

    val track = this.tracks(pos)
    val m = new Media(track.toURI.toString)
    val mp = new MediaPlayer(m) {
      onEndOfMedia = play(-1)
    }
    mp.play
    btn.text = "Pause"
    this.mp = Some(mp)
    labels("track").text = track.getName
    store
  }

  def ctlPlay: Unit = {
    this.mp match {
      case Some(mp) => {
        mp.status() match {
          case MediaPlayer.Status.Playing.delegate => {
            this.btn.text = "Play"
            mp.pause
          }
          case _ => {
            this.btn.text = "Pause"
            mp.play
          }
        }
      }
      case None =>
    }
  }


  def store = {
    val out = new PrintWriter(sys.env("HOME") + "/.mhlast")
    Array(this.art, this.alb).map(s => out.println(s))
    out.println(this.pos)
    out.close
  }

  def makeButton(file: File, kind: String) = {
    val size = fontSize(kind)
    new Button(this.re.replaceFirstIn(file.getName, "").slice(0, txtLength(kind))) {
      mnemonicParsing = false
      style = s"-fx-text-fill: blue; -fx-font-size: ${size}pt" 
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
