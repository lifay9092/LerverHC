package cn.lifay.lerverhc.view

import cn.hutool.core.date.DatePattern
import cn.hutool.core.date.DateUtil
import cn.hutool.core.date.TimeInterval
import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.ObjectUtil
import cn.hutool.core.util.StrUtil
import cn.hutool.core.util.URLUtil
import cn.hutool.http.ContentType
import cn.hutool.http.Method
import cn.hutool.json.JSONObject
import cn.hutool.json.JSONUtil
import cn.lifay.extension.checkParam
import cn.lifay.lerverhc.db.DbInfor
import cn.lifay.lerverhc.hander.ConfigUtil
import cn.lifay.lerverhc.hander.DialogView
import cn.lifay.lerverhc.hander.HttpHander
import cn.lifay.lerverhc.hander.quickly
import cn.lifay.lerverhc.model.BatchVO
import cn.lifay.lerverhc.model.Header
import cn.lifay.lerverhc.model.HttpTool
import cn.lifay.lerverhc.model.HttpTools
import cn.lifay.lerverhc.model.HttpTools.httpTools
import cn.lifay.lerverhc.model.enums.HttpType
import cn.lifay.ui.BaseView
import cn.lifay.ui.LoadingUI
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.FlowPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.DirectoryChooser
import javafx.stage.Stage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import model.HttpAddr
import model.HttpAddrs.httpAddrs
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
import kotlin.reflect.KMutableProperty0


/**
 *@ClassName IndexController
 *@Description TODO
 *@Author lifay
 *@Date 2022/1/4 20:09
 **/
class HttpToolController : BaseView<VBox>() {

    //首页controller
    private lateinit var index: IndexController

    /*http对象*/
    private var httpTool: HttpTool? = null

    /*http根界面*/
    @FXML
    var httpPane = VBox()

    /*http信息*/
    @FXML
    lateinit var httpInfoPane: FlowPane

    @FXML
    lateinit var requestPane: HBox

    @FXML
    lateinit var headPane: VBox

    @FXML
    lateinit var bodyPane: VBox

    @FXML
    lateinit var statusPane: HBox

    @FXML
    lateinit var responsePane: VBox

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
    var selectContentType: ChoiceBox<ContentType> = ChoiceBox()

    @FXML
    lateinit var saveImg: ImageView

//
//    @FXML
//    var saveImg = ImageView(Image(ConfigUtil.SAVE_IMG))


    @FXML
    var headersTable = TableView<Header>()

    @FXML
    var bodyStr: TextArea = TextArea()

    @FXML
    var responseStr: TextArea = TextArea()

    @FXML
    var httpStatus: Label = Label()

    @FXML
    var nowTimeLabel: Label = Label()

    @FXML
    var useTimeLabel: Label = Label()
    /*

        @FXML
        var checkAsync: CheckBox = CheckBox()
    */

    //    @FXML
//    var saveBtn: Button = Button("保存",ImageView(Image(ResourceUtil.getStream("save.png"))))
    /*批量表单参数*/

    @FXML
    var checkBatch: CheckBox = CheckBox()

    @FXML
    var viewBatchBtn = Button()

    var batchVO = BatchVO()

    /*检查结果*/
    @FXML
    var jsonCheckText = Label()

    private var isDownFile: Boolean = false

    override fun rootPane(): KMutableProperty0<VBox> {
        return this::httpPane
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        //布局
//        httpPane.prefHeightProperty().addListener { observableValue, old, new ->
//            println("http:h=$new")
//        }
        httpInfoPane.prefWidthProperty().bind(httpPane.prefWidthProperty())
        httpInfoPane.prefHeightProperty().bind(httpPane.prefHeightProperty().multiply(0.06))

        requestPane.prefWidthProperty().bind(httpPane.prefWidthProperty())
        requestPane.prefHeightProperty().bind(httpPane.prefHeightProperty().multiply(0.30))
        headPane.prefWidthProperty().bind(requestPane.prefWidthProperty().multiply(0.45))
        headPane.prefHeightProperty().bind(requestPane.prefHeightProperty())
        headersTable.prefHeightProperty().bind(headPane.prefHeightProperty().subtract(32))
        bodyPane.prefWidthProperty().bind(requestPane.prefWidthProperty().multiply(0.45))
        bodyPane.prefHeightProperty().bind(requestPane.prefHeightProperty())
        bodyStr.prefHeightProperty().bind(headersTable.prefHeightProperty())


        statusPane.prefWidthProperty().bind(httpPane.prefWidthProperty())
        statusPane.prefHeightProperty().bind(httpPane.prefHeightProperty().multiply(0.05))

        responsePane.prefWidthProperty().bind(httpPane.prefWidthProperty())
        responsePane.prefHeightProperty().bind(httpPane.prefHeightProperty().multiply(0.59))

        responseStr.prefWidthProperty().bind(responsePane.prefWidthProperty())
        responseStr.prefHeightProperty().bind(responsePane.prefHeightProperty().multiply(0.95))


        //img
        saveImg.apply {
            image = Image(ConfigUtil.SAVE_IMG)
            setOnMouseClicked {
                if (it.clickCount == 1) {
                    saveHttp()
                }
            }
        }
//        sendBtn = Button("发送",ImageView(Image(ConfigUtil.SEND_IMG)))
        /*        sendImg.apply {
                    image = Image(ConfigUtil.SEND_IMG)
                    setOnMouseClicked {
                        if (it.clickCount == 1) {
                            sendHttp()
                        }
                    }
                }*/

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
        /*初始化一些默认配置*/
        url.text = "http://192.168.218.12:8083/gisCacheOrg/getCompanyCodeByBureauCode"
        bodyStr.text = "{\n" +
                "    \"bureauCode\": \"\${bureauCode}\"\n" +
                "}"
        bodyStr.textProperty().addListener { observable, oldValue, newValue ->
            try {
                JSONUtil.parseObj(newValue)
                jsonCheckText.textFill = Color.LIGHTSKYBLUE
                jsonCheckText.text = "json格式:true"
            } catch (e: Exception) {
                jsonCheckText.textFill = Color.RED
                jsonCheckText.text = "json格式:false 错误:${e.message}"
            }
        }
        viewBatchBtn.isVisible = checkBatch.isSelected
        //批量选项监听
        Tooltip.install(checkBatch, Tooltip("当前接口执行多个请求\n支持json数组格式数据").quickly())
        checkBatch.selectedProperty().addListener { observable, oldValue, newValue ->
            viewBatchBtn.isVisible = !oldValue && newValue
        }

        //批量文件名
//        batchFileNameLabel.isVisible = false
//        batchFileNameText.isVisible = false
//        batchFileExtLabel.isVisible = false

    }


    /**
     * 批量表单
     *
     * @author lifay
     * @return
     */
    private fun showBatchForm() {
        val view = DialogView.initForm<BatchFormController>("批量表单", "batchForm")
        view.controller.bodyStr.bind(bodyStr.textProperty())
        view.controller.batchDataFilePath.textProperty().bindBidirectional(batchVO.batchDataFilePath)
        view.controller.batchFileNameText.textProperty().bindBidirectional(batchVO.batchFileNameText)
        view.show()
    }

    fun initForm(indexController: IndexController, id: String) {
        this.index = indexController
        this.httpTool = DbInfor.database.httpTools.find { it.id eq id }
        loadHttpForm(httpTool)

    }

    fun viewBatch() {
        //显示 批量表单
        showBatchForm()
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
        /*
                checkAsync.isSelected = dataObj["isSync"] as Boolean
        */
        selectContentType.value = ContentType.valueOf(dataObj["contentType"] as String)
        batchVO.batchFileNameText.value = ObjectUtil.defaultIfBlank(dataObj.getStr("batchFileName"), "")
        batchVO.batchDataFilePath.value = ObjectUtil.defaultIfBlank(dataObj.getStr("batchDataFilePath"), "")

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
     * 发送
     */
    fun sendHttp() {
        checkParam("select服务地址", selectAddr.value)
        checkParam("url", url.text)
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
                asyncDo {
                    var fullUrl = getFullUrl() + "?"
                    val jsonObject = JSONUtil.parseObj(bodyStr.text)
                    for (key in jsonObject.keys) {
                        fullUrl += "${key}=${jsonObject[key]}&"
                    }
                    val timer = DateUtil.timer()
                    HttpHander.downFile(
                        if (fullUrl.endsWith("&")) fullUrl.substring(
                            0,
                            fullUrl.length - 1
                        ) else fullUrl, directory
                    )
                    uptUseTime(timer)
                    ConfigUtil.preferences.put(ConfigUtil.PROPERTIES_OUTPUT_FOLDER, directory.absolutePath)
                }

            }
        } else {
            if (checkBatch.isSelected) {
                //批量执行
                //检查是否有指定文件名变量格式
                if (StrUtil.isBlank(batchVO.batchFileNameText.value)) {
                    alert("【批量模板文件名】不能为空", Alert.AlertType.ERROR)
                    return
                }
                asyncDo {
                    try {
                        val timer = DateUtil.timer()

                        val result = parseJsonFmtStr(
                            HttpHander.batchSendHttp(
                                getFullUrl(),
                                selectMethod.value,
                                selectContentType.value,
                                headersTable.items.toList(),
                                bodyStr.text,
                                batchVO.batchDataFilePath.value,
                                batchVO.batchFileNameText.value
                            )
                        )
                        Platform.runLater {
                            uptUseTime(timer)
                            responseStr.text = result
                        }
                    } catch (e: Exception) {
                        alert(e.message!!, Alert.AlertType.ERROR)
                    }
                }
            } else {
                //单个
                asyncDo {
                    try {
                        val timer = DateUtil.timer()
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
                            uptUseTime(timer)
                            httpStatus.text = "200"
                            responseStr.text = str
                        }
                    } catch (e: Exception) {
                        alert(e.message!!, Alert.AlertType.ERROR)
                    }
                }
                // println("外部执行")
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
        checkParam("url", url.text)
        //批量
        if (checkBatch.isSelected) {
            //检查是否有指定文件名变量格式
            if (StrUtil.isBlank(batchVO.batchFileNameText.value)) {
                alert("【批量模板文件名】不能为空", Alert.AlertType.ERROR)
                return
            }
            if (StrUtil.isBlank(batchVO.batchDataFilePath.value)) {
                alert("【数据文件】不能为空", Alert.AlertType.ERROR)
                return
            }
            asyncDo {
                try {
                    val timer = DateUtil.timer()
                    val result = HttpHander.batchSendHttp(
                        getFullUrl(),
                        selectMethod.value,
                        selectContentType.value, headersTable.items.toList(),
                        bodyStr.text, batchVO.batchDataFilePath.value, batchVO.batchFileNameText.value
                    )
                    Platform.runLater {
                        responseStr.text = result
                        uptUseTime(timer)
                    }
                } catch (e: Exception) {
                    alert(e.message!!, Alert.AlertType.ERROR)
                }
            }
        } else {
            asyncDo {
                try {
                    val timer = DateUtil.timer()
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
                        uptUseTime(timer)
                    }
                    //输出目录
                    val outputDir =
                        ConfigUtil.preferences.get(
                            ConfigUtil.PROPERTIES_OUTPUT_FOLDER,
                            System.getProperty("user.dir")
                        ) + File.separator + "result"
                    val fileName =
                        selectAddr.selectionModel.selectedItem.name + "_" + httpNameText.text + File.separator + DateUtil.format(
                            Date(),
                            DatePattern.PURE_DATETIME_FORMATTER
                        ) + ".json"
                    val newFilePath = outputDir + File.separator + fileName
                    FileUtil.writeString(httpResponse?.body(), newFilePath, Charset.forName("utf-8"))
                } catch (e: Exception) {
                    alert(e.message!!, Alert.AlertType.ERROR)
                }
            }
        }
        uptNowTime()
    }

    private fun uptUseTime(timer: TimeInterval) {
        useTimeLabel.text = "${timer.interval()} 毫秒"
    }


    fun checkId(actionEvent: ActionEvent) {
    }

    /**
     * 保存http
     */
    fun saveHttp() {
        if (StrUtil.isBlank(httpNameText.text)) {
            Alert(Alert.AlertType.ERROR, "http名称不能为空", ButtonType.CLOSE).show()
            return
        }
        var dataObj = JSONObject()
        dataObj["method"] = selectMethod.value.name
        dataObj["url"] = url.text
        dataObj["isBatch"] = checkBatch.isSelected
        /*
                dataObj["isSync"] = checkAsync.isSelected
        */
        dataObj["contentType"] = selectContentType.value.name
        dataObj["headers"] = JSONUtil.toJsonStr(headersTable.items.toList())
        dataObj["batchFileName"] = batchVO.batchFileNameText.value
        dataObj["batchDataFilePath"] = batchVO.batchDataFilePath.value

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
                val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                // 封装文本内容
                val trans = StringSelection(fullUrl)
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

    fun asyncDo(f: () -> Unit) {
        val loadingUI = LoadingUI(httpPane.scene.window as Stage)
        GlobalScope.launch {
            loadingUI.show()
            try {
                f()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                loadingUI.closeStage()
            }
        }
    }
}