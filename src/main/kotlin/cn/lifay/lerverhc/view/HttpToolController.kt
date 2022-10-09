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
import cn.lifay.lerverhc.hander.HttpHander
import cn.lifay.lerverhc.model.Header
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.paint.Color
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import model.HttpAddr
import model.HttpAddrs.httpAddrs
import cn.lifay.lerverhc.model.HttpTool
import cn.lifay.lerverhc.model.HttpTools
import cn.lifay.lerverhc.model.HttpTools.httpTools
import cn.lifay.lerverhc.model.enums.HttpType
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
/*

    @FXML
    var authorization: TextField = TextField("")
*/

    @FXML
    lateinit var headersTable: TableView<Header>

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

    @FXML
    var jsonCheckText = Label()

    private var isDownFile: Boolean = false

    override fun initialize(location: URL?, resources: ResourceBundle?) {

        //method
        selectMethod.items.addAll(Method.values().asList())
        selectMethod.value = Method.GET
        //addr
        selectAddr.apply {
            //items.add(HttpAddr(id = "custom", name = "自定义地址",""))
            items.addAll(DbInfor.database.httpAddrs.toList())
            this.valueProperty().addListener { observable, oldValue, newValue ->
                addrValue.text = newValue.addr
            }
        }
        //contentType
        selectContentType.items.addAll(listOf(ContentType.FORM_URLENCODED, ContentType.JSON, ContentType.MULTIPART))
        selectContentType.value = ContentType.JSON

        //headers
        headersTable.isEditable = true
        headersTable.columns.addAll(
            listOf(
                buildTableColumn("KEY", "key", 100.0),
                buildTableColumn("VALUE", "value", 386.0),
            )
        )
        //批量选项监听
        checkBatch.selectedProperty().addListener { observable, oldValue, newValue ->
            if (!oldValue && newValue) {
                //显示 批量文件
                batchFileNameLabel.isVisible = true
                batchFileNameText.isVisible = true
                batchFileExtLabel.isVisible = true
            } else {
                //隐藏 批量文件
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
        bodyStr.textProperty().addListener { observable, oldValue, newValue ->
            try {
                JSONUtil.parseObj(newValue)
                jsonCheckText.textFill = Color.BLACK
                jsonCheckText.text = "json格式:true"
            } catch (e: Exception) {
                jsonCheckText.textFill = Color.RED
                jsonCheckText.text = "json格式:false 错误:${e.message}"
            }
        }

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

    private fun buildTableColumn(colName: String, valName: String, width: Double): TableColumn<Header, String> {
        val col = TableColumn<Header, String>(colName)
        col.cellValueFactory = PropertyValueFactory(valName)
        col.cellFactory = TextFieldTableCell.forTableColumn();//给需要编辑的列设置属性
        col.prefWidth = width
        return col
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
        addrValue.text = if (selectAddr.value != null) selectAddr.value.addr else ""
        selectMethod.value = Method.valueOf(dataObj["method"] as String)
        url.text = dataObj["url"] as String
        checkBatch.isSelected = dataObj["isBatch"] as Boolean
        checkAsync.isSelected = dataObj["isSync"] as Boolean
        selectContentType.value = ContentType.valueOf(dataObj["contentType"] as String)
        batchFileNameText.text = dataObj["batchFileName"] as String?
        batchDataFilePath.text = dataObj["batchDataFilePath"] as String?

        httpNameText.text = item.name
        item.body.let {
            bodyStr.text = JSONUtil.formatJsonStr(it)
        }

        isDownFile = (dataObj["downFile"] ?: false) as Boolean

        val headerStr = dataObj["headers"] as String?
        headerStr?.let {
            val headers = JSONUtil.toList(headerStr, Header::class.java)
            headersTable.items.addAll(headers)
        }

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
        val httpRequest = HttpUtil.createRequest(selectMethod.value, getFullUrl())
        httpRequest.contentType(selectContentType.value.value)

        if (headersTable.items.size > 0) {
            for (header in headersTable.items) {
                httpRequest.header(header.key, header.value)
            }
        }
        return httpRequest
    }

    /**
     * 发送
     */
    fun sendHttp(actionEvent: ActionEvent) {
        checkParam(selectAddr.value, "select服务地址")
        checkParam(url.text, "url")
        //是否下载文件
        if (isDownFile) {
            //下载文件
            val directoryChooser = DirectoryChooser().apply {
                title = "选择输出目录"
                initialDirectory =
                    File(System.getProperty("user.dir"))
            }
            val directory = directoryChooser.showDialog(index.rootPane.scene.window)
            directory?.let {
                GlobalScope.launch {
                    var fullUrl = getFullUrl() + "?"
                    val jsonObject = JSONUtil.parseObj(bodyStr.text)
                    for (key in jsonObject.keys) {
                        fullUrl += "${key}=${jsonObject[key]}&"
                    }
                    HttpHander.downFile(
                        if (fullUrl.endsWith("&")) fullUrl.substring(
                            0,
                            fullUrl.length - 1
                        ) else fullUrl, directory
                    )
                    ConfigUtil.preferences.put(ConfigUtil.PROPERTIES_OUTPUT_FOLDER, directory.absolutePath)
                }

            }
        } else {
            if (checkBatch.isSelected) {
                //批量执行
                //检查是否有指定文件名变量格式
                if (StrUtil.isBlank(batchFileNameText.text)) {
                    Alert(Alert.AlertType.ERROR, "【批量模板文件名】不能为空", ButtonType.CLOSE).show()
                    return
                }
                GlobalScope.launch {
                    val result = parseJsonFmtStr(
                        HttpHander.batchSendHttp(
                            getFullUrl(),
                            selectMethod.value,
                            selectContentType.value,
                            headersTable.items.toList(),
                            bodyStr.text,
                            batchDataFilePath.text,
                            batchFileNameText.text
                        )
                    )
                    Platform.runLater {
                        responseStr.text = result
                    }
                }
            } else {
                //单个
                GlobalScope.launch {
                    val httpResponse =
                        HttpHander.singleSendHttp(
                            getFullUrl(),
                            selectMethod.value,
                            selectContentType.value,
                            headersTable.items.toList(),
                            bodyStr.text
                        )
                    val str = parseJsonFmtStr(httpResponse?.body())
//                    delay(10000)
//                    val str = "dddddd"
                    Platform.runLater {
                        httpStatus.text = "200"
                        responseStr.text = str
                    }
                }
                println("外部执行")
            }
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
        //批量
        if (checkBatch.isSelected) {
            //检查是否有指定文件名变量格式
            if (StrUtil.isBlank(batchFileNameText.text)) {
                Alert(Alert.AlertType.ERROR, "【批量模板文件名】不能为空", ButtonType.CLOSE).show()
                return
            }
            if (StrUtil.isBlank(batchDataFilePath.text)) {
                Alert(Alert.AlertType.ERROR, "【数据文件】不能为空", ButtonType.CLOSE).show()
                return
            }
            GlobalScope.launch {
                val result = HttpHander.batchSendHttp(
                    getFullUrl(),
                    selectMethod.value,
                    selectContentType.value, headersTable.items.toList(),
                    bodyStr.text, batchDataFilePath.text, batchFileNameText.text
                )
                Platform.runLater {
                    responseStr.text = result
                }
            }
        } else {
            GlobalScope.launch {
                val httpResponse =
                    HttpHander.singleSendHttp(
                        getFullUrl(),
                        selectMethod.value,
                        selectContentType.value,
                        headersTable.items.toList(),
                        bodyStr.text
                    )
                Platform.runLater {
                    httpStatus.text = "200"
                    responseStr.text = httpResponse?.body()
                }
                //输出目录
                val outputDir =
                    ConfigUtil.preferences.get(ConfigUtil.PROPERTIES_OUTPUT_FOLDER, System.getProperty("user.dir"))
                val newFilePath = outputDir + File.separator + UUID.fastUUID().toString() + ".json"
                FileUtil.writeString(httpResponse?.body(), newFilePath, Charset.forName("utf-8"))
            }
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
        dataObj["headers"] = JSONUtil.toJsonStr(headersTable.items.toList())
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
        Desktop.getDesktop().open(
            File(
                ConfigUtil.preferences.get(
                    ConfigUtil.PROPERTIES_OUTPUT_FOLDER,
                    System.getProperty("user.dir")
                )
            )
        );
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
        val httpAddr = selectAddr.value
        if (httpAddr.isCustom()) {
            return URLUtil.normalize(url.text)
        } else {
            var host = URLUtil.normalize(httpAddr.addr)
            var uri = if (url.text.startsWith("/")) url.text else "/${url.text}"
            return host + uri
        }

    }

    fun addHeader(actionEvent: ActionEvent) {
        headersTable.items.add(Header("KEY${headersTable.items.size + 1}", ""))
    }

    fun delHeader(actionEvent: ActionEvent) {
        headersTable.selectionModel?.selectedIndex?.let { index ->
            println(index)
            headersTable.items.removeAt(index)
        }
    }

}