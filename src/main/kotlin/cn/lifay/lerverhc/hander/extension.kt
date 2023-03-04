package cn.lifay.lerverhc.hander

import javafx.scene.Scene
import javafx.scene.control.Tooltip
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.stage.Stage
import javafx.util.Duration


fun Scene.bootstrap(): Scene {
    //stylesheets.add(BootstrapFX.bootstrapFXStylesheet())
    return this
}


/*fun Stage.bindEscKey(): Stage {
    addEventHandler(KeyEvent.KEY_PRESSED) {
        if (it.code == KeyCode.ESCAPE) {
            close()
        }
    }
    return this
}*/

fun Tooltip.quickly(): Tooltip {
    showDelay = Duration(50.0)
    showDuration = Duration(4000.0)
    return this
}