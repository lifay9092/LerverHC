package cn.lifay.lerverhc.model

import javafx.beans.property.SimpleStringProperty

data class BatchVO(
    var batchDataFilePath : SimpleStringProperty = SimpleStringProperty(""),
    var batchFileNameText : SimpleStringProperty = SimpleStringProperty(""),
)
