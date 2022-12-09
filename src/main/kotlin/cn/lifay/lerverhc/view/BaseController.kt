package cn.lifay.lerverhc.view

import cn.hutool.core.util.ObjectUtil
import javafx.application.Platform
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.TextArea

/**
 *@ClassName BaseController
 *@Description TODO
 *@Author lifay
 *@Date 2022/1/5 10:07
 **/
open class BaseController {

    fun errorAlert(msg: String) {
        Platform.runLater {
            Alert(Alert.AlertType.ERROR, "错误", ButtonType.CLOSE).apply {
                dialogPane.expandableContent = TextArea(msg)
                show()
            }
        }
        error(msg)
    }

    fun checkParam(value: Any, name: String?): Unit {
        if (ObjectUtil.isEmpty(value)) {
            Platform.runLater { Alert(Alert.AlertType.ERROR, "${name}不能为空", ButtonType.CLOSE).show()}
            error("${name}不能为空")
        }
    }
}