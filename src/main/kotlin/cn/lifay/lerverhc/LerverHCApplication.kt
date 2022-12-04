package cn.lifay.lerverhc

import cn.hutool.core.io.resource.ResourceUtil
import cn.lifay.lerverhc.hander.bootstrap
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
        val indexPane = FXMLLoader.load<Pane>(ResourceUtil.getResource("index.fxml"))
        //indexPane.styleClass.add("panel-primary")
        var scene = Scene(indexPane, 1500.0, 800.0).bootstrap()
//        scene.stylesheets.add(BootstrapFX.bootstrapFXStylesheet())
//        scene.stylesheets.add(
//            ResourceUtil.getResource("custom.css")
//                .toExternalForm()
//        )
        primaryStage.apply {
            title = "LerverHC工具"
            isResizable = false
            getIcons().add(Image(ResourceUtil.getStream("icon.png")))
            setScene(scene)
            setOnCloseRequest { Platform.exit() }
        }
        primaryStage.show()
    }
}
