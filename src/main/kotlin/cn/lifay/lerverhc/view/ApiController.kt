package cn.lifay.lerverhc.view

import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.IdUtil
import cn.hutool.core.util.ObjectUtil
import cn.hutool.http.ContentType
import cn.hutool.http.HttpUtil
import cn.hutool.json.JSONObject
import cn.hutool.json.JSONUtil
import cn.lifay.lerverhc.db.DbInfor
import cn.lifay.lerverhc.hander.ConfigUtil
import cn.lifay.lerverhc.hander.HttpHander
import cn.lifay.lerverhc.model.ApiModel
import cn.lifay.lerverhc.model.HttpAddr
import cn.lifay.lerverhc.model.HttpAddrs.httpAddrs
import cn.lifay.lerverhc.model.HttpTool
import cn.lifay.lerverhc.model.HttpTools
import cn.lifay.lerverhc.model.HttpTools.httpTools
import cn.lifay.lerverhc.model.enums.HttpType
import cn.lifay.ui.LoadingUI
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.ChoiceBox
import javafx.scene.control.TextArea
import javafx.scene.layout.AnchorPane
import javafx.stage.FileChooser
import javafx.stage.Stage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.ktorm.dsl.*
import org.ktorm.entity.find
import org.ktorm.entity.toList
import java.io.File
import java.net.URL
import java.nio.charset.Charset
import java.util.*


/**
 *@ClassName AddrController
 *@Description TODO
 *@Author lifay
 *@Date 2022/1/4 20:09
 **/
class ApiController : BaseController(), Initializable {

    @FXML
    var apiRootPane = AnchorPane()

    @FXML
    var onLineAddr = TextArea()

    @FXML
    var impFromAddrBtn = Button()

    @FXML
    var selectFileBtn = Button()

    @FXML
    var impFromFileBtn = Button()

    //form
    @FXML
    var selectAddrs = ChoiceBox<HttpAddr>()

    @FXML
    var filePath = TextArea()

    lateinit var nodeId: String
    override fun initialize(location: URL?, resources: ResourceBundle?) {
        selectAddrs.apply {
            items.addAll(DbInfor.database.httpAddrs.toList())
            valueProperty().addListener { observable, oldValue, newValue ->
                onLineAddr.text = toApiAddr(newValue.addr, "")
            }
        }
        filePath.text = ConfigUtil.preferences.get(ConfigUtil.API_JSON_FILE, "")

    }

    private fun toSwaggerAddr(addr: String): String {
        return "${addr}/swagger-resources"
    }

    private fun toApiAddr(addr: String, url: String): String {
        return addr + url
    }

    fun initForm(nodeId: String) {
        this.nodeId = nodeId
    }

    /**
     * 从在线地址导入
     * @author 李方宇
     */
    fun impFromAddr(actionEvent: ActionEvent) {
        try {
            if (selectAddrs.selectionModel.isEmpty) {
                Alert(Alert.AlertType.ERROR, "文件不能为空").show()
                return
            }
            val loading = LoadingUI(apiRootPane.scene.window as Stage)
            GlobalScope.launch {
                try {
                    disableBtn()
                    loading.show()
                    val async = async {
                        try {
                            //获取分组
                            val resourceStr = HttpUtil.get(toSwaggerAddr(selectAddrs.selectionModel.selectedItem.addr))
                            if (!JSONUtil.isJson(resourceStr)) {
                                Platform.runLater { Alert(Alert.AlertType.ERROR, "api接口请求失败:${resourceStr}").show() }
                                return@async null
                            }
                            val resourceObj = JSONUtil.parseArray(resourceStr)[0] as JSONObject
                            val apiDocUrl = resourceObj.getStr("url")
                            val str = HttpUtil.get(toApiAddr(selectAddrs.selectionModel.selectedItem.addr, apiDocUrl))
                            if (!JSONUtil.isJson(str)) {
                                Platform.runLater { Alert(Alert.AlertType.ERROR, "api接口请求失败:${str}").show() }
                                return@async null
                            }
                            return@async JSONUtil.toBean(str, ApiModel::class.java)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Platform.runLater {
                                Alert(Alert.AlertType.ERROR, "api接口请求失败:${e}").show()
                            }
                            return@async null
                        }
                    }
                    impHandle(async.await()) {
                        Platform.runLater {
                            //结束
                            Alert(Alert.AlertType.INFORMATION, "导入成功:${it} 条").show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    throw RuntimeException("在线导入api失败:${e.message}")
                } finally {
                    loading.closeStage()
                    showBtn()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Alert(Alert.AlertType.ERROR, e.message).show()
        }
    }

    fun selectFile(actionEvent: ActionEvent) {
        val oldFilePath = ConfigUtil.preferences.get(ConfigUtil.API_JSON_FILE, "")
        val fileChooser = FileChooser().apply {
            title = "选择Api文件"
            initialDirectory =
                if (oldFilePath.isNotBlank()) File(oldFilePath).parentFile else File(System.getProperty("user.dir"))
            initialFileName = ConfigUtil.preferences.get(ConfigUtil.API_JSON_FILE, "")
            extensionFilters.add(FileChooser.ExtensionFilter("JSON", "*.json"))
        }
        val file = fileChooser.showOpenDialog(apiRootPane.scene.window)
        file?.let {
            filePath.text = file.absolutePath
            ConfigUtil.preferences.put(ConfigUtil.API_JSON_FILE, file.absolutePath)
        }

    }

    private fun disableBtn() {
        impFromAddrBtn.isDisable = true
        impFromFileBtn.isDisable = true
        selectFileBtn.isDisable = true
    }

    private fun showBtn() {
        impFromAddrBtn.isDisable = false
        impFromFileBtn.isDisable = false
        selectFileBtn.isDisable = false
    }

    /**
     * 从文件导入
     * @author 李方宇
     */
    fun impFromFile(actionEvent: ActionEvent) {
        try {
            if (filePath.text.isBlank()) {
                Alert(Alert.AlertType.ERROR, "文件不能为空").show()
                return
            }

            disableBtn()

            GlobalScope.launch {
                try {
                    val apiModel =
                        JSONUtil.toBean(
                            FileUtil.readString(filePath.text, Charset.forName("utf-8")),
                            ApiModel::class.java
                        )
                    impHandle(apiModel) {
                        Platform.runLater {
                            //结束
                            Alert(Alert.AlertType.INFORMATION, "导入成功:${it} 条").show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    throw RuntimeException("文件导入api失败:${e.message}")
                } finally {
                    showBtn()
                }
            }
        } catch (e: Exception) {
            Alert(Alert.AlertType.ERROR, e.message).show()
        }
    }

    fun impHandle(apiModel: ApiModel?, resultFunc: (Int) -> Unit) {
        /*判断父节点*/
        val panId = nodeId ?: "0"
        val title = apiModel!!.info!!.title
        //addr
        /*一级目录 应用*/
        val firstDir = DbInfor.database.httpTools.find { (it.parentId eq panId) and (it.name eq title!!) }
        //val firstDir = firstDirQ.first()
        var firstDirId: String
        if (firstDir == null) {
            firstDirId = IdUtil.fastSimpleUUID()
            DbInfor.database.insert(HttpTools) {
                set(HttpTools.id, firstDirId)
                set(HttpTools.parentId, panId)
                set(HttpTools.name, title)
                set(HttpTools.type, HttpType.NODE.name)
            }
        } else {
            //已存在
            firstDirId = firstDir.id
            //先删除久的
            DbInfor.database.delete(HttpTools) { it.parentId eq firstDirId }
        }
        /*先初始化http数据*/
        var httpList = ArrayList<HttpTool>()
        val httpAddrs = DbInfor.database.httpAddrs.toList()
        for (pathEntry in apiModel.paths!!.entries) {
            //k-url
            for (httpEntry in pathEntry.value.entries) {
                //k-method v-http
                val httpDTO = httpEntry.value

                httpList.add(httpDTO.let {
                    //addr
                    val httpAddr = httpAddrs.first { httpAddr -> httpAddr.addr.contains(apiModel.host!!) }
                    val addId = httpAddr.id ?: "c98574f3-6115-40a2-bf9d-8847c42e6d4d"
                    //datas
                    var dataObj = JSONObject()
                    dataObj["method"] = httpEntry.key.uppercase()
                    dataObj["url"] = pathEntry.key
                    dataObj["isBatch"] = false
                    dataObj["isSync"] = false
                    dataObj["contentType"] = HttpHander.getContentTypeByValue(httpDTO.consumes)
                    dataObj["downFile"] =
                        ObjectUtil.isNotNull(it.produces) && it.produces!!.contains("application/octet-stream")

                    //body
                    var bodyObj = JSONObject()
                    var b = false
                    httpDTO.parameters?.let {
                        //特殊处理 contentType
                        if (!b && httpDTO.parameters!!.isNotEmpty() && dataObj["contentType"] == ContentType.JSON.name) {
                            val p = httpDTO.parameters!![0]
                            if ("query" == p.`in`) {
                                dataObj["contentType"] = ContentType.FORM_URLENCODED.name
                            }
                            b = true
                        }
                        for (param in httpDTO.parameters!!) {
                            when (param.type) {
                                "string" -> {
                                    bodyObj.set(param.name, param.default ?: "")
                                }
                                "integer", "ref" -> {
                                    bodyObj.set(param.name, param.default?.toInt() ?: 0)
                                }
                                "boolean" -> {
                                    bodyObj.set(param.name, param.default?.toBoolean() ?: false)
                                }
                                "array" -> {
                                    bodyObj.set(param.name, param.default?.toList() ?: listOf(""))
                                }
                            }
                        }
                    }

                    HttpTool(
                        IdUtil.fastSimpleUUID(), parentId = httpDTO.tags?.get(0) ?: "-1", addId,
                        it.summary ?: "未知名称", HttpType.HTTP.name, bodyObj.toString(), dataObj.toString()
                    )
                })
            }
        }
        /*二级目录 模块*/
        apiModel.tags!!.forEach { tag ->
            val secondDirId = IdUtil.fastSimpleUUID()
            DbInfor.database.insert(HttpTools) {
                set(HttpTools.id, secondDirId)
                set(HttpTools.parentId, firstDirId)
                set(HttpTools.name, tag.name)
                set(HttpTools.type, HttpType.NODE.name)
            }
            /*http*/
            httpList.filter { it.parentId == tag.name }.forEach { it.parentId = secondDirId }
            //.forEach { it.parentId = secondDirId }
        }
        DbInfor.database.batchInsert(HttpTools) {
            httpList.forEach { http ->
                item {
                    set(HttpTools.id, http.id)
                    set(HttpTools.parentId, http.parentId)
                    set(HttpTools.addrId, http.addrId)
                    set(HttpTools.name, http.name)
                    set(HttpTools.type, http.type)
                    set(HttpTools.body, http.body)
                    set(HttpTools.datas, http.datas)
                }
            }

        }
        resultFunc(httpList.size)
    }


}