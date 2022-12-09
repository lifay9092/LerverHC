package cn.lifay.lerverhc.view

import cn.lifay.lerverhc.db.DbInfor
import cn.lifay.lerverhc.hander.HttpHander
import cn.lifay.lerverhc.hander.quickly
import cn.lifay.lerverhc.model.BatchVO
import cn.lifay.lerverhc.model.HttpTools.addrId
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.layout.AnchorPane
import javafx.stage.FileChooser
import model.HttpAddr
import model.HttpAddrs
import model.HttpAddrs.httpAddrs
import org.ktorm.dsl.eq
import org.ktorm.dsl.insert
import org.ktorm.dsl.update
import org.ktorm.entity.count
import org.ktorm.entity.removeIf
import org.ktorm.entity.toList
import java.io.File
import java.net.URL
import java.util.*


/**
 *@ClassName AddrController
 *@Description TODO
 *@Author lifay
 *@Date 2022/1/4 20:09
 **/
class BatchFormController : BaseController(), Initializable {

//    lateinit var batchVO : BatchVO

    var bodyStr = SimpleStringProperty()

    @FXML
    var rootPane = AnchorPane()

    @FXML
    val btnDataFile: Button = Button()

    /*批量*/
    @FXML
    var batchDataFilePath: TextArea = TextArea()

    @FXML
    var help: Label = Label()

    @FXML
    var batchFileNameLabel: Label = Label()

    @FXML
    var batchFileNameText: TextField = TextField()

    @FXML
    var batchFileExtLabel: Label = Label()
    @FXML
    var checkResult = TextField()


    override fun initialize(location: URL?, resources: ResourceBundle?) {
        if (batchDataFilePath.text.isBlank()) {
            batchDataFilePath.text = "C:\\Users\\lifay9092\\Desktop\\temp\\测试批量数据.json"
        }
        Tooltip.install(help, Tooltip("""
            [自定义输出文件名]
            规则1:json数据文件里的变量名
            规则2:需要保证唯一
            
            [基础数据文件]
            持json数组格式数据
            
            示例数据:
                [
                    {
                        "id": "id1",
                        "info": {
                            "name": "u1",
                            "addr": "a1"
                        }
                    }
                ]
                自定义输出文件名1: ${'$'}{id}.json
                自定义输出文件名2: ${'$'}{info.name}.json
               
        """.trimIndent()).quickly())

        Tooltip.install(batchFileNameText, Tooltip("""
            自定义输出文件名
            规则1:json数据文件里的变量名
            规则2:需要保证唯一
            示例1:\\$\\{name}
        """.trimIndent()).quickly())
        Tooltip.install(batchDataFilePath, Tooltip("基础数据文件\n支持json数组格式数据").quickly())

    }

    /**
     * 选择数据文件
     */
    fun selectDataFile(actionEvent: ActionEvent) {
        val fileChooser = FileChooser().apply {
            title = "选择数据文件"
            initialDirectory = File(System.getProperty("user.dir"))
            extensionFilters.add(FileChooser.ExtensionFilter("JSON", "*.json"))
        }

        val file = fileChooser.showOpenDialog(rootPane.scene.window)
        file?.let { batchDataFilePath.text = file.absolutePath }
        println("选择了:${batchDataFilePath.text}")
    }

    /**
     * 检查批量【参数模板】和【数据文件】
     */
    fun checkDataFile(actionEvent: ActionEvent) {
        checkParam(bodyStr, "参数模板")
        checkParam(batchDataFilePath.text, "数据文件路径")
        val result: String = HttpHander.checkDataFile(bodyStr.value, batchDataFilePath.text)
        checkResult.text = result
    }


}