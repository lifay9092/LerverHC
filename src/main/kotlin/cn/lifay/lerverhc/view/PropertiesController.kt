package cn.lifay.lerverhc.view

import cn.hutool.core.util.StrUtil
import cn.lifay.lerverhc.hander.ConfigUtil
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Scene
import javafx.scene.control.TextField
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Pane
import javafx.stage.DirectoryChooser
import javafx.stage.Stage
import java.io.File
import java.net.URL
import java.util.*


/**
 *@ClassName IndexController
 *@Description TODO
 *@Author lifay
 *@Date 2022/1/4 20:09
 **/
class PropertiesController : BaseController(), Initializable {

    @FXML
    var propertiesRootPane = BorderPane()

    @FXML
    var outputFolderText = TextField()

    override fun initialize(location: URL?, resources: ResourceBundle?) {

        outputFolderText.text = ConfigUtil.preferences.get(ConfigUtil.PROPERTIES_OUTPUT_FOLDER,System.getProperty("user.dir"))
    }

    /**
     * 选择输出目录
     */
    fun selectOutputFolder(actionEvent: ActionEvent) {
        val directoryChooser = DirectoryChooser().apply {
            title = "选择输出目录"
            initialDirectory =
                File(if (StrUtil.isNotBlank(outputFolderText.text)) outputFolderText.text else System.getProperty("user.dir"))
        }
        val file = directoryChooser.showDialog(propertiesRootPane.scene.window)
        outputFolderText.text = file?.absolutePath
        file?.absolutePath?.let { ConfigUtil.preferences.put(ConfigUtil.PROPERTIES_OUTPUT_FOLDER,it) }
    }


}