package cn.lifay.lerverhc.view

import cn.hutool.core.util.ObjectUtil
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType

/**
 *@ClassName BaseController
 *@Description TODO
 *@Author lifay
 *@Date 2022/1/5 10:07
 **/
open class BaseController {

    fun errorAlert(msg: String) {
        Alert(Alert.AlertType.ERROR, msg, ButtonType.CLOSE).show()
        error(msg)
    }

    fun checkParam(value: Any, name: String?): Unit {
        if (ObjectUtil.isEmpty(value)) {
            Alert(Alert.AlertType.ERROR, "${name}不能为空", ButtonType.CLOSE).show()
            error("${name}不能为空")
        }
    }
}