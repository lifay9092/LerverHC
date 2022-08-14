package cn.lifay.lerverhc.view

import cn.hutool.core.date.DatePattern
import cn.hutool.core.date.DateUtil
import cn.hutool.core.io.FileUtil
import cn.hutool.core.io.resource.ResourceUtil
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.Tooltip
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import javafx.stage.FileChooser
import java.io.File
import java.net.URL
import java.time.format.DateTimeFormatter
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
            image = Image(ResourceUtil.getStream("import.png"))
            setOnMouseClicked {
                val fileChooser = FileChooser().apply {
                    title = "选择加载文件"
                    initialDirectory = File(System.getProperty("user.dir"))
                }
                val file = fileChooser.showOpenDialog(rootPane.scene.window)
                file?.let { dataSourceText.text = FileUtil.readUtf8String(file.absolutePath) }
            }
        }

        exportImg.apply {
            image = Image(ResourceUtil.getStream("export.png"))
            setOnMouseClicked {
                val fileChooser = FileChooser().apply {
                    title = "选择文件导出目录"
                    initialDirectory = File(System.getProperty("user.dir"))
                }
                val file = fileChooser.showOpenDialog(rootPane.scene.window)

                file?.let { FileUtil.writeUtf8String(resultText.text,file.absolutePath + File.separator + "转换结果-${DateUtil.format(Date(),
                    DatePattern.PURE_DATETIME_FORMATTER)}.json") }
            }
        }

        convertImg.apply {
            image = Image(ResourceUtil.getStream("convert.png"))
            setOnMouseClicked {

            }
        }
        ruleLabel.tooltip = Tooltip("""
            【txt格式】
            每行变量命名为:${'$'}{1},${'$'}{2},${'$'}{3}...
            【json格式】
            变量名为:${'$'}{key1},${'$'}{key2},${'$'}{key3}
        """.trimIndent())

    }

}