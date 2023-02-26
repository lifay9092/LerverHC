package cn.lifay.lerverhc

import cn.hutool.core.io.resource.ResourceUtil
import cn.lifay.lerverhc.hander.ConfigUtil
import cn.lifay.ui.GlobeTheme
import cn.lifay.util.StaticUtil
import javafx.application.Application
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.layout.Pane
import javafx.stage.Stage

class LerverHCApplication : Application() {
    override fun start(primaryStage: Stage) {
        //index
//        FXApplication.setElementStyleEnable(true)
//        val indexView = IndexController()
//        val indexPane = indexView.rootPane()
        GlobeTheme.enableElement(true)

        val indexPane = FXMLLoader.load<Pane>(ResourceUtil.getResource("index.fxml"))
        indexPane.prefWidth = StaticUtil.SCREEN_WIDTH * 0.998
        indexPane.prefHeight = StaticUtil.SCREEN_HEIGHT * 0.98

        var scene = Scene(indexPane)

        primaryStage.apply {
            title = "LerverHC工具"
//            isResizable = false
            this.isMaximized = true
            icons.add(Image(ConfigUtil.ICON_IMG))
            setScene(scene)
            setOnCloseRequest { Platform.exit() }
            minWidth = 944.0
            minHeight = 664.0
            show()
            widthProperty().addListener { ov, old, new ->
                println("new:$new")
                indexPane.prefWidth = new.toDouble() * 0.998
            }
            heightProperty().addListener { ov, old, new ->
                println("window h=$new")
                indexPane.prefHeight = new.toDouble() * 0.98
            }
        }
    }
}
