package cn.lifay.lerverhc.view

import cn.hutool.core.io.resource.ResourceUtil
import cn.lifay.lerverhc.db.DbInfor
import cn.lifay.lerverhc.hander.ConfigUtil
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import cn.lifay.lerverhc.model.HttpTool
import cn.lifay.lerverhc.model.HttpTools
import cn.lifay.lerverhc.model.HttpTools.httpTools
import cn.lifay.lerverhc.model.enums.HttpType
import cn.lifay.ui.tree.Register
import javafx.event.ActionEvent
import org.ktorm.dsl.eq
import org.ktorm.dsl.update
import org.ktorm.entity.EntitySequence
import org.ktorm.entity.filter
import org.ktorm.entity.first
import org.ktorm.entity.toList
import java.net.URL
import java.util.*


/**
 *@ClassName AddrController
 *@Description TODO
 *@Author lifay
 *@Date 2022/1/4 20:09
 **/
class SelectParentController : BaseController(), Initializable {

    @FXML
    var selectParentPane = AnchorPane()

    @FXML
    var parentTreeView = TreeView<HttpTool>()

    lateinit var sourceId: String
    lateinit var refreshFunc: (HttpTool) -> Unit

    fun initForm(id: String, refresh: (HttpTool) -> Unit) {
        this.sourceId = id
        this.refreshFunc = refresh
    }


    override fun initialize(location: URL?, resources: ResourceBundle?) {
        var rootTreeItem = TreeItem(
            HttpTool("0", "-1", "", "根目录", HttpType.NODE.name, "", ""),
            ImageView(ConfigUtil.FOLDER_IMG)
        )
//        val httpTools = DbInfor.database.httpTools
//        for (httpTool in httpTools.filter { it.parentId eq "0" }) {
//            //child
//            addChild(rootTreeItem, httpTools, httpTool)
//        }
        parentTreeView.apply {
            root = rootTreeItem
            isShowRoot = true
            Register(HttpTool::id,HttpTool::parentId,true){
                DbInfor.database.httpTools.filter { it.type eq HttpType.NODE.name }.toList()
            }
        }

    }

    private fun buildTreeItem(httpTool: HttpTool): TreeItem<HttpTool> {
        val treeItem = TreeItem(
            httpTool, ImageView(Image(ResourceUtil.getResource(if (httpTool.isHttp()) "http.png" else "folder.png").toExternalForm()))
        )
        return treeItem
    }

    fun ok(actionEvent: ActionEvent) {
        parentTreeView.selectionModel?.let {
            it.selectedItem?.let {
                val selectId = it.value.id
                if (selectId != "0") {
                    println("${sourceId}选中了${selectId}")
                    DbInfor.database.update(HttpTools) {
                        set(HttpTools.parentId, selectId)
                        where { HttpTools.id eq sourceId }
                    }
                    refreshFunc(DbInfor.database.httpTools.first { it.id eq sourceId })
                    it.isExpanded = true
                }
            }

        }
    }

}