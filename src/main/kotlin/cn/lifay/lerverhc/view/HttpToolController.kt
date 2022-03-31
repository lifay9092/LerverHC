package cn.lifay.lerverhc.view

import cn.hutool.core.date.DateUtil
import cn.hutool.core.io.FileUtil
import cn.hutool.core.lang.UUID
import cn.hutool.core.util.StrUtil
import cn.hutool.core.util.URLUtil
import cn.hutool.http.ContentType
import cn.hutool.http.HttpRequest
import cn.hutool.http.HttpUtil
import cn.hutool.http.Method
import cn.hutool.json.JSONObject
import cn.hutool.json.JSONUtil
import cn.lifay.lerverhc.db.DbInfor
import cn.lifay.lerverhc.hander.ConfigUtil
import cn.lifay.lerverhc.hander.GlobeProps
import cn.lifay.lerverhc.hander.HttpHander
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.stage.FileChooser
import model.HttpAddr
import model.HttpAddrs.httpAddrs
import model.HttpTool
import model.HttpTools
import model.HttpTools.httpTools
import model.enum.HttpType
import org.ktorm.dsl.eq
import org.ktorm.dsl.update
import org.ktorm.entity.find
import org.ktorm.entity.toList
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File
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

    //首页controller
    private lateinit var index: IndexController

    /*http对象*/
    private var httpTool: HttpTool? = null

    @FXML
    var httpNameText = TextArea()

    @FXML
    var selectAddr: ChoiceBox<HttpAddr> = ChoiceBox()

    @FXML
    var addrValue = Label()

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

    override fun initialize(location: URL?, resources: ResourceBundle?) {

        //method
        selectMethod.items.addAll(Method.values().asList())
        selectMethod.value = Method.GET
        //addr
        selectAddr.apply {
            items.addAll(DbInfor.database.httpAddrs.toList())
            this.valueProperty().addListener { observable, oldValue, newValue ->
                addrValue.text = newValue.addr
            }
        }
        //contentType
        selectContentType.items.addAll(listOf(ContentType.FORM_URLENCODED,ContentType.JSON,ContentType.MULTIPART))
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

    fun initForm(indexController: IndexController, id: String) {
        this.index = indexController
        this.httpTool = DbInfor.database.httpTools.find { it.id eq id }
        loadHttpForm(httpTool)

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
        selectAddr.value = DbInfor.database.httpAddrs.find { it.id eq item.addrId }
        addrValue.text = selectAddr.value.addr
        selectMethod.value = Method.valueOf(dataObj["method"] as String)
        url.text = dataObj["url"] as String
        checkBatch.isSelected = dataObj["isBatch"] as Boolean
        checkAsync.isSelected = dataObj["isSync"] as Boolean
        selectContentType.value = ContentType.valueOf(dataObj["contentType"] as String)
        authorization.text = dataObj["authorization"] as String?
        batchFileNameText.text = dataObj["batchFileName"] as String?
        batchDataFilePath.text = dataObj["batchDataFilePath"] as String?

        httpNameText.text = item.name
        bodyStr.text = item.body

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
    fun buildHttpRequest(): HttpRequest {
        var httpRequest = HttpUtil.createRequest(selectMethod.value, getFullUrl())
        httpRequest.contentType(selectContentType.value.value)
        if (authorization.text.isNotEmpty()) {
            httpRequest.header("authorization", authorization.text)
        }
        return httpRequest
    }

    /**
     * 发送
     */
    fun sendHttp(actionEvent: ActionEvent) {
        checkParam(selectAddr.value,"select服务地址")
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
            responseStr.text = parseJsonFmtStr(HttpHander.batchSendHttp(
                httpRequest,
                bodyStr.text,
                batchDataFilePath.text,
                batchFileNameText.text
            ))
        } else {
            //单个
            val httpResponse = HttpHander.singleSendHttp(getFullUrl(),selectMethod.value,selectContentType.value, bodyStr.text)
            httpStatus.text = "200"
            responseStr.text = parseJsonFmtStr(httpResponse)
        }
        uptNowTime()
    }

    /**
     * 转换成json预览格式
     */
    private fun parseJsonFmtStr(jsonStr: String?): String? {
        if (JSONUtil.isJson(jsonStr)) {
            return JSONUtil.formatJsonStr(jsonStr)
        }
        return jsonStr
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
            val httpResponse = HttpHander.singleSendHttp(getFullUrl(),selectMethod.value,selectContentType.value, bodyStr.text)
            httpStatus.text = "200"
            responseStr.text = httpResponse
            val newFilePath = GlobeProps.getOutputFolderValue() + File.separator + UUID.fastUUID().toString() + ".json"
            FileUtil.writeString(httpResponse, newFilePath, Charset.forName("utf-8"))
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

        val file = fileChooser.showOpenDialog(index.rootPane.scene.window)
        file?.let { batchDataFilePath.text = file?.absolutePath }

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
        var dataObj = JSONObject()
        dataObj["method"] = selectMethod.value.name
        dataObj["url"] = url.text
        dataObj["isBatch"] = checkBatch.isSelected
        dataObj["isSync"] = checkAsync.isSelected
        dataObj["contentType"] = selectContentType.value.name
        dataObj["authorization"] = authorization.text
        dataObj["batchFileName"] = batchFileNameText.text
        dataObj["batchDataFilePath"] = batchDataFilePath.text

        DbInfor.database.update(HttpTools) {
            set(it.name, httpNameText.text)
            //set(it.parentId,httpParentId)
            set(it.addrId, selectAddr.value.id)
            set(it.type, HttpType.HTTP.name)
            set(it.body, bodyStr.text)
            set(it.datas, dataObj.toString())
            where {
                it.id eq httpTool!!.id
            }
        }
        //显示

    }

    /**
     * 打开输出目录
     */
    fun openOutputFolder(actionEvent: ActionEvent) {
        Desktop.getDesktop().open(File(ConfigUtil.preferences.get(ConfigUtil.PROPERTIES_OUTPUT_FOLDER,System.getProperty("user.dir"))));
    }

    fun viewUrl(actionEvent: ActionEvent) {
        val fullUrl = getFullUrl()
        val alert = Alert(
            Alert.AlertType.INFORMATION,
            fullUrl,
            ButtonType("复制", ButtonBar.ButtonData.OK_DONE),
            ButtonType.CLOSE
        )
        alert.showAndWait().apply {
            if (this.get().text == "复制") {
                // 获取系统剪贴板
                var clipboard = Toolkit.getDefaultToolkit().systemClipboard
                // 封装文本内容
                var trans = StringSelection(fullUrl)
                // 把文本内容设置到系统剪贴板
                clipboard.setContents(trans, null)
            }
        }
    }

    fun getFullUrl(): String {
        var host = URLUtil.normalize(selectAddr.value.addr)
        var uri = if (url.text.startsWith("/")) url.text else "/${url.text}"
        return host + uri
    }

}