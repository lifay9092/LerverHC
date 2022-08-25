package cn.lifay.lerverhc.view

import cn.hutool.core.io.resource.ResourceUtil
import cn.lifay.lerverhc.db.DbInfor
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
import cn.lifay.lerverhc.model.enum.HttpType
import org.ktorm.dsl.eq
import org.ktorm.dsl.update
import org.ktorm.entity.EntitySequence
import org.ktorm.entity.filter
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
    lateinit var refreshFunc: () -> Unit

    fun initForm(id: String, refresh: () -> Unit) {
        this.sourceId = id
        this.refreshFunc = refresh
    }


    override fun initialize(location: URL?, resources: ResourceBundle?) {
        var rootTreeItem = TreeItem(
            HttpTool("0", "-1", "", "根目录", HttpType.NODE.name, "", ""),
            ImageView(Image(ResourceUtil.getStream("folder.png")))
        )
        val httpTools = DbInfor.database.httpTools
        for (httpTool in httpTools.filter { it.parentId eq "0" }) {
            //child
            addChild(rootTreeItem, httpTools, httpTool)
        }
        parentTreeView.apply {
            root = rootTreeItem
            isShowRoot = true
            setOnMouseClicked { event ->
                val selectId = this.selectionModel?.selectedItem?.value?.id
                if (selectId != "0" && event.clickCount == 2) {
                    println("${sourceId}选中了${selectId}")
                    DbInfor.database.update(HttpTools) {
                        set(HttpTools.parentId, selectId)
                        where { HttpTools.id eq sourceId }
                    }
                    refreshFunc()
                }
            }
        }

    }

    private fun addChild(
        rootTreeItem: TreeItem<HttpTool>, httpTools: EntitySequence<HttpTool, HttpTools>, httpTool: HttpTool
    ) {
        if (httpTool.isNode()) {
            val treeItem = buildTreeItem(httpTool)
            rootTreeItem.children.add(treeItem)
        }
    }

    private fun buildTreeItem(httpTool: HttpTool): TreeItem<HttpTool> {
        val treeItem = TreeItem(
            httpTool, ImageView(Image(ResourceUtil.getStream(if (httpTool.isHttp()) "http.png" else "folder.png")))
        )
        return treeItem
    }

}