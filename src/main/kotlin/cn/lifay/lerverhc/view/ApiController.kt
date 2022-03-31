package cn.lifay.lerverhc.view

import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.EnumUtil
import cn.hutool.core.util.IdUtil
import cn.hutool.http.ContentType
import cn.hutool.json.JSONObject
import cn.hutool.json.JSONUtil
import cn.lifay.lerverhc.db.DbInfor
import cn.lifay.lerverhc.hander.ConfigUtil
import cn.lifay.lerverhc.hander.HttpHander
import cn.lifay.lerverhc.model.ApiAddrModel
import cn.lifay.lerverhc.model.ApiModel
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Alert
import javafx.scene.control.ChoiceBox
import javafx.scene.control.TextArea
import javafx.scene.layout.AnchorPane
import javafx.stage.FileChooser
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import model.HttpAddrs.httpAddrs
import model.HttpTool
import model.HttpTools
import model.HttpTools.httpTools
import model.enum.HttpType
import org.ktorm.dsl.and
import org.ktorm.dsl.delete
import org.ktorm.dsl.eq
import org.ktorm.dsl.insert
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.first
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

    //form
    @FXML
    var selectAddrs = ChoiceBox<ApiAddrModel>()

    @FXML
    var filePath = TextArea()

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        selectAddrs.items.addAll(listOf(ApiAddrModel("20", ""), ApiAddrModel("12", "")))
        filePath.text = ConfigUtil.preferences.get(ConfigUtil.API_JSON_FILE, "")

    }

    fun impFromAddr(actionEvent: ActionEvent) {

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

    fun impFromFile(actionEvent: ActionEvent) {
        if (filePath.text.isBlank()) {
            Alert(Alert.AlertType.ERROR, "文件不能为空").show()
            return
        }
        var apiModel = JSONUtil.toBean(FileUtil.readString(filePath.text, Charset.forName("utf-8")), ApiModel::class.java)

        GlobalScope.launch { impHandle(apiModel) }

    }

    fun impHandle(apiModel: ApiModel?) {
        val title = apiModel!!.info!!.title
        //addr
        /*一级目录 应用*/
        val firstDir = DbInfor.database.httpTools.find { (it.parentId eq "0") and (it.name eq title!!) }
        //val firstDir = firstDirQ.first()
        var firstDirId: String
        if (firstDir == null) {
            firstDirId = IdUtil.fastSimpleUUID()
            DbInfor.database.insert(HttpTools) {
                set(HttpTools.id, firstDirId)
                set(HttpTools.parentId, "0")
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
                    val httpAddr = httpAddrs.filter { it.addr == apiModel.host }.first()
                    val addId = httpAddr.id ?: "c98574f3-6115-40a2-bf9d-8847c42e6d4d"
                    //datas
                    var dataObj = JSONObject()
                    dataObj["method"] = httpEntry.key.uppercase()
                    dataObj["url"] = pathEntry.key
                    dataObj["isBatch"] = false
                    dataObj["isSync"] = false
                    dataObj["contentType"] = HttpHander.getContentTypeByValue(httpDTO.consumes)
                    //body
                    var bodyObj = JSONObject()
                    httpDTO.parameters?.let {
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
            httpList.filter { it.parentId == tag.name }.forEach { http ->
                DbInfor.database.insert(HttpTools) {
                    set(HttpTools.id, http.id)
                    set(HttpTools.parentId, secondDirId)
                    set(HttpTools.addrId, http.addrId)
                    set(HttpTools.name, http.name)
                    set(HttpTools.type, http.type)
                    set(HttpTools.body, http.body)
                    set(HttpTools.datas, http.datas)
                }
            }
        }
        //结束
        Alert(Alert.AlertType.INFORMATION,"导入成功:${httpList.size} 条").show()
    }


}