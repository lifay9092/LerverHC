package cn.lifay.lerverhc.hander

import cn.hutool.core.io.resource.ResourceUtil
import cn.lifay.lerverhc.view.BaseController
import cn.lifay.lerverhc.view.DialogBaseView
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.stage.Modality
import javafx.stage.Stage

object DialogView {


    fun<V : BaseController> initForm(title: String, fxml: String): DialogBaseView<V> {

        return initForm(title, fxml,false)
    }

    fun <V : BaseController> initForm(title: String, fxml: String, isResizable: Boolean,modality: Modality = Modality.WINDOW_MODAL): DialogBaseView<V> {
        val view = DialogBaseView<V>()
        val loader = FXMLLoader(ResourceUtil.getResource("${fxml}.fxml"))
        val pane = loader.load<Pane>()
        val controller = loader.getController<V>()

        val scene = Scene(pane).bootstrap()
        val s = Stage().bindEscKey().apply {
            initModality(modality)
            this.title = title
            this.isResizable = isResizable
            setScene(scene)
        }
        view.stage = s
        view.controller = controller
        return view
    }

    fun<V : BaseController> initForm(
        title: String,
        fxml: String,
        isResizable: Boolean,
        width: Double,
        height: Double,
        modality: Modality
    ): DialogBaseView<V> {
        val view = DialogBaseView<V>()
        val s = Stage().bindEscKey()
        s.initModality(modality)
        val loader = FXMLLoader(ResourceUtil.getResource("${fxml}.fxml"))
        val controller = loader.getController<V>()

        val pane = loader.load<Pane>()
        val scene = Scene(pane, width, height).bootstrap()
        s.apply {
            this.title = title
            this.isResizable = isResizable
            setScene(scene)
        }
        view.stage = s
        view.controller = controller
        return view
    }
}