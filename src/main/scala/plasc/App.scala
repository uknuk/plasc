package plasc

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage

import scalafx.scene.Scene


object App extends JFXApp {
  stage = new PrimaryStage {
    title = "Plasc"
    scene = new Scene(Player.root, 1024, 572) 
  }
}
