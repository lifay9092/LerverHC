package cn.lifay.lerverhc.view

import cn.hutool.core.date.DatePattern
import cn.hutool.core.date.DateUtil
import cn.hutool.core.io.FileUtil
import cn.hutool.core.io.resource.ResourceUtil
import cn.hutool.json.JSONUtil
import cn.lifay.lerverhc.hander.ConfigUtil
import cn.lifay.lerverhc.hander.convert.IConvert
import cn.lifay.lerverhc.hander.convert.JsonConvert
import cn.lifay.lerverhc.hander.convert.TxtConvert
import cn.lifay.lerverhc.hander.quickly
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Alert
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.Tooltip
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import java.io.File
import java.net.URL
import java.util.*

class ConvertToJsonToolController : Initializable {

    @FXML
    lateinit var rootPane: AnchorPane

    @FXML
    var ruleLabel = Label()

    @FXML
    lateinit var exportImg: ImageView

    @FXML
    lateinit var importImg: ImageView

    @FXML
    lateinit var convertImg: ImageView

    @FXML
    lateinit var ruleText: TextArea

    @FXML
    lateinit var resultText: TextArea

    @FXML
    lateinit var dataSourceText: TextArea

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        importImg.apply {
            image = Image(ConfigUtil.IMPORT_IMG)
            setOnMouseClicked {
                val fileChooser = FileChooser().apply {
                    title = "选择加载文件"
                    initialDirectory = File(System.getProperty("user.dir"))
                }
                val file = fileChooser.showOpenDialog(rootPane.scene.window)
                file?.let { dataSourceText.text = FileUtil.readUtf8String(file.absolutePath) }
            }
        }
        Tooltip.install(importImg, Tooltip("导入数据").quickly())

        exportImg.apply {
            image = Image(ConfigUtil.EXPORT_IMG)
            setOnMouseClicked {
                val directoryChooser = DirectoryChooser().apply {
                    title = "选择文件导出目录"
                    initialDirectory = File(System.getProperty("user.dir"))

                }
                val file = directoryChooser.showDialog(rootPane.scene.window)
                file?.let {
                    FileUtil.writeUtf8String(
                        resultText.text, file.absolutePath + File.separator + "转换结果-${
                            DateUtil.format(
                                Date(),
                                DatePattern.PURE_DATETIME_FORMATTER
                            )
                        }.json"
                    )
                    Alert(Alert.AlertType.INFORMATION, "保存成功!").show()
                }
            }
        }
        Tooltip.install(exportImg, Tooltip("导出数据").quickly())

        convertImg.apply {
            image = Image(ConfigUtil.CONVERT_IMG)
            setOnMouseClicked {
                convertAction()
            }
        }
        Tooltip.install(convertImg, Tooltip("转换").quickly())

        ruleLabel.tooltip = Tooltip(
            """
            【txt格式】
            每行变量命名为:${'$'}{1},${'$'}{2},${'$'}{3}...
            【json格式】
            变量名为:${'$'}{key1},${'$'}{key2},${'$'}{key3}
        """.trimIndent()
        ).quickly()

    }

    private fun convertAction() {
        if (dataSourceText.text.isBlank()) {
            Alert(Alert.AlertType.ERROR, "数据源不能为空!").show()
            return
        }
        if (ruleText.text.isBlank()) {
            Alert(Alert.AlertType.ERROR, "转换规则不能为空!").show()
            return
        }
        if (!JSONUtil.isJson(ruleText.text)) {
            Alert(Alert.AlertType.ERROR, "转换规则必须遵循JSON格式!").show()
            return
        }
        val convert = getConvert()
        try {
            resultText.text = convert.convert()
        } catch (e: Exception) {
            e.printStackTrace()
            Alert(Alert.AlertType.ERROR, "转换失败:${e.message}").show()
        }
    }

    private fun getConvert(): IConvert {
        //判断数据源类型:txt json?
        val isTxtType = !JSONUtil.isJson(dataSourceText.text)
        return if (isTxtType) TxtConvert(dataSourceText.text, ruleText.text) else JsonConvert(
            dataSourceText.text,
            ruleText.text
        )
    }

    fun txtSample(actionEvent: ActionEvent) {
        dataSourceText.text = """
            11 12
            21 22
        """.trimIndent()
        ruleText.text = """
            {
            	"id":${'$'}{1},
            	"name":"${'$'}{2}"
            }
        """.trimIndent()
    }

    fun jsonSample(actionEvent: ActionEvent) {
        dataSourceText.text = """
            [
                {
                    "id":"1",
                    "name":"1"
                },
                {
                    "id":"2",
                    "name":"2"
                }
            ]
        """.trimIndent()
        ruleText.text = """
            {
            	"id":${'$'}{id},
            	"name":"${'$'}{name}"
            }
        """.trimIndent()
    }
}