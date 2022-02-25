package cn.lifay.lerverhc

import cn.hutool.core.io.resource.ResourceUtil
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.layout.Pane
import javafx.stage.Stage

class LerverHCApplication : Application() {
    override fun start(primaryStage: Stage) {
        //index
        val indexPane = FXMLLoader.load<Pane>(ResourceUtil.getResource("index.fxml"))
        var scene = Scene(indexPane, 1500.0, 800.0)
        primaryStage.apply {
            title = "图形工具"
            isResizable = false

            getIcons().add(Image(ResourceUtil.getStream("icon.png")))
            setScene(scene)
        }

        primaryStage.show()
    }
}
