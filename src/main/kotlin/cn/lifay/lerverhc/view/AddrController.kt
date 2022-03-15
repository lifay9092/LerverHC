package cn.lifay.lerverhc.view

import cn.lifay.lerverhc.db.DbInfor
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.ChoiceBox
import javafx.scene.control.TextField
import javafx.scene.layout.AnchorPane
import model.HttpAddr
import model.HttpAddrs
import model.HttpAddrs.httpAddrs
import org.ktorm.dsl.eq
import org.ktorm.dsl.insert
import org.ktorm.dsl.update
import org.ktorm.entity.count
import org.ktorm.entity.removeIf
import org.ktorm.entity.toList
import java.net.URL
import java.util.*


/**
 *@ClassName AddrController
 *@Description TODO
 *@Author lifay
 *@Date 2022/1/4 20:09
 **/
class AddrController : BaseController(), Initializable {

    @FXML
    var addrRootPane = AnchorPane()
    //form
    @FXML
    var selectAddrs = ChoiceBox<HttpAddr>()
    @FXML
    var addrId = TextField()
    @FXML
    var addrName = TextField()
    @FXML
    var addrValue = TextField()

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        addrId.isVisible = false
        selectAddrs.apply {
            items.addAll(DbInfor.database.httpAddrs.toList())
            this.valueProperty().addListener { observable, oldValue, newValue ->
                addrId.text = newValue?.id
                addrName.text = newValue?.name
                addrValue.text = newValue?.addr

            }
        }

    }

    fun delAdder(actionEvent: ActionEvent) {
        checkParam(addrId.text,"请选择旧数据")
        val showAndWait = Alert(Alert.AlertType.CONFIRMATION, "确认删除吗？", ButtonType.YES).showAndWait()
        showAndWait.ifPresent {
            DbInfor.database.httpAddrs.removeIf { it.id eq addrId.text }
            refresh()
        }

    }

    fun saveForm(actionEvent: ActionEvent) {
        checkParam(addrName.text,"名称")
        checkParam(addrValue.text,"地址")


        if (addrId.text.isBlank()) {
            //新增数据 判断名称重复
            if (DbInfor.database.httpAddrs.count() { it.name eq addrName.text } > 0) {
                Alert(Alert.AlertType.ERROR, "名称[${addrName.text}]已存在!").show()
                return
            }
            DbInfor.database.insert(HttpAddrs) {
                set(HttpAddrs.id, UUID.randomUUID().toString())
                set(HttpAddrs.name, addrName.text)
                set(HttpAddrs.addr, addrValue.text)
            }
        }else{
            DbInfor.database.update(HttpAddrs) {
                set(it.id, addrId.text)
                set(it.name, addrName.text)
                set(it.addr, addrValue.text)
                where { it.id eq addrId.text }
            }
        }
        refresh()
    }
    fun refresh(){
        selectAddrs.items.setAll(DbInfor.database.httpAddrs.toList())
        selectAddrs.selectionModel.clearSelection()
        clearForm(null)
    }
    fun clearForm(actionEvent: ActionEvent?) {
        addrId.text = ""
        addrName.text = ""
        addrValue.text = ""
    }


}