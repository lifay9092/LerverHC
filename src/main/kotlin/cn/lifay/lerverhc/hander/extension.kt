package cn.lifay.lerverhc.hander

import javafx.scene.control.Button


fun Button.primary(): Button {
    styleClass.setAll("btn","btn-danger")
    return this
}
