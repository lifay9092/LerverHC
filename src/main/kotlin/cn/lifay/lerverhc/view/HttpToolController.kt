package view

import cn.hutool.core.date.DateUtil
import cn.hutool.core.io.FileUtil
import cn.hutool.core.io.resource.ResourceUtil
import cn.hutool.core.lang.UUID
import cn.hutool.core.util.StrUtil
import cn.hutool.http.ContentType
import cn.hutool.http.HttpRequest
import cn.hutool.http.HttpUtil
import cn.hutool.http.Method
import cn.hutool.json.JSONObject
import cn.hutool.json.JSONUtil
import cn.lifay.lerverhc.AppStartup
import cn.lifay.lerverhc.db.DbInfor
import cn.lifay.lerverhc.hander.GlobeProps
import cn.lifay.lerverhc.hander.HttpHander
import cn.lifay.lerverhc.view.IndexController
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Pane
import javafx.stage.FileChooser
import javafx.stage.Stage
import model.HttpTool
import model.HttpTools
import model.HttpTools.httpTools
import model.enum.HttpType
import org.ktorm.dsl.*
import org.ktorm.entity.EntitySequence
import org.ktorm.entity.count
import org.ktorm.entity.filter
import java.awt.Desktop
import java.io.File
import java.io.FileInputStream
import java.net.URL
import java.nio.charset.Charset
import java.util.*


/**
 *@ClassName IndexController
 *@Description TODO
 *@Author lifay
 *@Date 2022/1/4 20:09
 **/
class HttpToolController : BaseController(), Initializable {

    var index : IndexController =
    @FXML
    var httpNameText = TextArea()

    @FXML
    var selectMethod: ChoiceBox<Method> = ChoiceBox()

    @FXML
    var url: TextArea = TextArea()

    @FXML
    var resultText: TextArea = TextArea()

    @FXML
    var selectContentType: ChoiceBox<ContentType> = ChoiceBox()

    @FXML
    var authorization: TextField = TextField()

    @FXML
    var bodyStr: TextArea = TextArea()

    @FXML
    var responseStr: TextArea = TextArea()

    @FXML
    var httpStatus: Label = Label()

    @FXML
    var nowTimeLabel: Label = Label()

    @FXML
    var checkBatch: CheckBox = CheckBox()

    @FXML
    var checkAsync: CheckBox = CheckBox()

    @FXML
    val btnDataFile: Button = Button()

    /*批量*/
    @FXML
    var batchDataFilePath: TextArea = TextArea()

    @FXML
    var batchFileNameLabel: Label = Label()

    @FXML
    var batchFileNameText: TextField = TextField()

    @FXML
    var batchFileExtLabel: Label = Label()

    /*检查结果*/
    @FXML
    var checkResult = TextField()

    /*临时http对象*/
    var httpId: String = ""
    var httpParentId: String = ""

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        //method
        selectMethod.items.addAll(Method.values().asList())
        selectMethod.value = Method.GET
        //contentType
        selectContentType.items.addAll(ContentType.values().asList())
        selectContentType.value = ContentType.JSON
        //批量选项监听
        checkBatch.selectedProperty().addListener { observable, oldValue, newValue ->
            if (!oldValue && newValue) {
                //批量文件名
                batchFileNameLabel.isVisible = true
                batchFileNameText.isVisible = true
                batchFileExtLabel.isVisible = true
            } else {
                //批量文件名
                batchFileNameLabel.isVisible = false
                batchFileNameText.isVisible = false
                batchFileExtLabel.isVisible = false
            }
        }
        /*初始化一些默认配置*/
        url.text = "http://192.168.218.12:8083/gisCacheOrg/getCompanyCodeByBureauCode"
        bodyStr.text = "{\n" +
                "    \"bureauCode\": \"\${bureauCode}\"\n" +
                "}"
        batchDataFilePath.text = "C:\\Users\\lifay9092\\Desktop\\temp\\测试批量数据.json"

        //批量文件名
        batchFileNameLabel.isVisible = false
        batchFileNameText.isVisible = false
        batchFileExtLabel.isVisible = false

    }

    /**
     * 加载http表单
     */
    private fun loadHttpForm(item: HttpTool?) {
        if (!JSONUtil.isJsonObj(item!!.datas)) {
            Alert(Alert.AlertType.ERROR, "datas信息缺失或格式不正确", ButtonType.CLOSE).show()
            return
        }
        val dataObj = JSONUtil.parseObj(item.datas)
        selectMethod.value = Method.valueOf(dataObj["method"] as String)
        url.text = dataObj["url"] as String
        checkBatch.isSelected = dataObj["isBatch"] as Boolean
        checkAsync.isSelected = dataObj["isSync"] as Boolean
        selectContentType.value = ContentType.valueOf(dataObj["contentType"] as String)
        authorization.text = dataObj["authorization"] as String?
        batchFileNameText.text = dataObj["batchFileName"] as String?
        batchDataFilePath.text = dataObj["batchDataFilePath"] as String?

        httpParentId = item.parentId
        httpNameText.text = item.name
        bodyStr.text = item.body

        httpId = item.id
    }




    /**
     * 更新时间
     */
    fun uptNowTime() {
        nowTimeLabel.text = DateUtil.now()
    }

    /**
     * 定义基础请求类
     */
    fun buildHttpRequest() : HttpRequest{
        var httpRequest = HttpUtil.createRequest(selectMethod.value, url.text)
        httpRequest.contentType(selectContentType.value.value)
        if (authorization.text.isNotEmpty()) {
            httpRequest.header("authorization",authorization.text)
        }
        return httpRequest
    }
    /**
     * 发送
     */
    fun sendHttp(actionEvent: ActionEvent) {
        checkParam(url.text, "url")
        //定义基础请求类
        var httpRequest = buildHttpRequest()

        if (checkBatch.isSelected) {
            //批量执行
            //检查是否有指定文件名变量格式
            if (StrUtil.isBlank(batchFileNameText.text)) {
                Alert(Alert.AlertType.ERROR, "【批量模板文件名】不能为空", ButtonType.CLOSE).show()
                return
            }
            responseStr.text = HttpHander.batchSendHttp(
                httpRequest,
                bodyStr.text,
                batchDataFilePath.text,
                batchFileNameText.text
            )
        } else {
            //单个
            val httpResponse = HttpHander.singleSendHttp(httpRequest, bodyStr.text)
            httpStatus.text = httpResponse!!.status.toString()
            responseStr.text = httpResponse.body()
        }
        uptNowTime()
    }

    /**
     * 发送并保存为文件
     */
    fun sendHttpAndSave(actionEvent: ActionEvent) {
        checkParam(url.text, "url")
        //定义基础请求类
        var httpRequest = buildHttpRequest()

        //批量
        if (checkBatch.isSelected) {
            //检查是否有指定文件名变量格式
            if (StrUtil.isBlank(batchFileNameText.text)) {
                Alert(Alert.AlertType.ERROR, "【批量模板文件名】不能为空", ButtonType.CLOSE).show()
                return
            }
            Platform.runLater {
                responseStr.text =
                    HttpHander.batchSendHttp(httpRequest, bodyStr.text, batchDataFilePath.text, batchFileNameText.text)
            }
        } else {
            val httpResponse = HttpHander.singleSendHttp(httpRequest, bodyStr.text)
            httpStatus.text = httpResponse!!.status.toString()
            responseStr.text = httpResponse.body()
            val newFilePath = GlobeProps.getOutputFolderValue() + File.separator + UUID.fastUUID().toString() + ".json"
            FileUtil.writeString(httpResponse.body(), newFilePath, Charset.forName("utf-8"))
        }
        uptNowTime()
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
        batchDataFilePath.text = file.absolutePath
    }

    fun checkId(actionEvent: ActionEvent) {

    }

    /**
     * 检查批量【参数模板】和【数据文件】
     */
    fun checkDataFile(actionEvent: ActionEvent) {
        checkParam(bodyStr.text, "参数模板")
        checkParam(batchDataFilePath.text, "数据文件路径")
        val result: String = HttpHander.checkDataFile(bodyStr.text, batchDataFilePath.text)
        checkResult.text = result
    }

    /**
     * 保存http
     */
    fun saveHttp(actionEvent: ActionEvent) {
        if (StrUtil.isBlank(httpNameText.text)) {
            Alert(Alert.AlertType.ERROR, "http名称不能为空", ButtonType.CLOSE).show()
            return
        }
        if (StrUtil.isBlank(httpParentId)) {
            Alert(Alert.AlertType.ERROR, "请选择http目录", ButtonType.CLOSE).show()
            return
        }
        var dataObj = JSONObject()
        dataObj["method"] = selectMethod.value.name
        dataObj["url"] = url.text
        dataObj["isBatch"] = checkBatch.isSelected
        dataObj["isSync"] = checkAsync.isSelected
        dataObj["contentType"] = selectContentType.value.name
        dataObj["authorization"] = authorization.text
        dataObj["batchFileName"] = batchFileNameText.text
        dataObj["batchDataFilePath"] = batchDataFilePath.text

//        val newItem = HttpTool(
//            id = httpId,
//            name = httpNameText.text,
//            parentId = httpParentId,
//            type = HttpType.HTTP.name,
//            body = bodyStr.text,
//            datas = dataObj.toString(),
//        )
        if (StrUtil.isBlank(httpId)) {
            val uuid = StrUtil.uuid()
            DbInfor.database.insert(HttpTools) {
                set(it.id, uuid)
                set(it.name, httpNameText.text)
                set(it.parentId, httpParentId)
                set(it.type, HttpType.HTTP.name)
                set(it.body, bodyStr.text)
                set(it.datas, dataObj.toString())
            }
            httpId = uuid
            reloadHttpTree(null)
        } else {
            DbInfor.database.update(HttpTools) {
                set(it.name, httpNameText.text)
                //set(it.parentId,httpParentId)
                set(it.type, HttpType.HTTP.name)
                set(it.body, bodyStr.text)
                set(it.datas, dataObj.toString())
                where {
                    it.id eq httpId
                }
            }
            httpTreeView.selectionModel?.selectedItem?.value = HttpTool(
                id = httpId,
                name = httpNameText.text,
                parentId = httpParentId,
                type = HttpType.HTTP.name,
                body = bodyStr.text,
                datas = dataObj.toString(),
            )
        }
        //显示

    }

    /**
     * 打开输出目录
     */
    fun openOutputFolder(actionEvent: ActionEvent) {
        Desktop.getDesktop().open(File(GlobeProps.getOutputFolderValue()));

    }

    /**
     * 属性管理菜单
     */
    fun propertiesManage(actionEvent: ActionEvent) {
        //propertiesManage
        var propertiesManageStage = Stage()
        val indexPane = FXMLLoader.load<Pane>(AppStartup::class.java.getResource("propertiesMange.fxml"))
        var scene = Scene(indexPane, 700.0, 500.0)
        propertiesManageStage.apply {
            title = "属性管理"
            isResizable = false
            setScene(scene)
        }
        propertiesManageStage.show()
    }


}