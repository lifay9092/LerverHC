package cn.lifay.lerverhc.view

import cn.hutool.core.io.resource.ResourceUtil
import cn.lifay.lerverhc.hander.bootstrap
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.stage.Modality
import javafx.stage.Stage

/**
 *@ClassName DialogBaseView
 *@Description TODO
 *@Author 李方宇
 *@Date 2022/12/5 20:10
 **/
open class DialogBaseView<V : BaseController> {
    lateinit var stage: Stage
    lateinit var controller: V

    fun show() {
        stage.show()
    }

    fun showAndWait() {
        stage.showAndWait()
    }

    fun close() {
        stage.close()
    }

}