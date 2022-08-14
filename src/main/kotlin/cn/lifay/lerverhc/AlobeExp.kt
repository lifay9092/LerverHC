package cn.lifay.lerverhc

import javafx.event.EventType
import javafx.scene.Scene
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.stage.Stage
import org.kordamp.bootstrapfx.BootstrapFX


fun Scene.bootstrap(): Scene {
    stylesheets.add(BootstrapFX.bootstrapFXStylesheet())
    return this
}


fun Stage.bindEscKey():Stage{
    addEventHandler(KeyEvent.KEY_PRESSED){
        if (it.code == KeyCode.ESCAPE) {
            close()
        }
    }
    return this
}