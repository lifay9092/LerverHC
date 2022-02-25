package cn.lifay.lerverhc.view

import cn.hutool.core.io.resource.ResourceUtil
import cn.hutool.core.util.StrUtil
import cn.lifay.lerverhc.db.DbInfor
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.fxml.JavaFXBuilderFactory
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.scene.layout.BorderPane
import javafx.util.BuilderFactory
import javafx.util.Callback
import model.HttpTool
import model.HttpTools
import model.HttpTools.httpTools
import model.enum.HttpType
import org.ktorm.dsl.and
import org.ktorm.dsl.delete
import org.ktorm.dsl.eq
import org.ktorm.dsl.insert
import org.ktorm.entity.EntitySequence
import org.ktorm.entity.count
import org.ktorm.entity.filter
import view.BaseController
import java.net.URL
import java.util.*

/**
 *@ClassName IndexController
 *@Description TODO
 *@Author lifay
 *@Date 2022/2/25 15:21
 **/
class IndexController : BaseController(), Initializable {
    @FXML
    var tabList: TabPane = TabPane()

    @FXML
    var rootPane = BorderPane()

    @FXML
    var httpTreeView = TreeView<HttpTool>()

    @FXML
    var reloadHttpTreeImg = ImageView()

    /*临时http对象*/
    var currentHttpId: String = ""
    var currentHttpParentId: String = ""


    override fun initialize(location: URL?, resources: ResourceBundle?) {
        /*图标渲染*/
        reloadHttpTreeImg.image = Image(ResourceUtil.getStream("reload.png"))
        /*初始化http树*/
        initTreeView()

    }

    /**
     * 初始化树
     */
    private fun initTreeView() {
        var rootTreeItem = TreeItem(HttpTool("0", "-1", "根节点", HttpType.NODE.name, "", ""))

        //HttpTool("1","0","11111",HttpType.NODE.name,"","")
        val httpTools = DbInfor.database.httpTools

        for (httpTool in httpTools.filter { it.parentId eq "0" }) {
            //child
            addChild(rootTreeItem, httpTools, httpTool)

        }


        httpTreeView.apply {
            root = rootTreeItem
            isShowRoot = true
            contextMenu = ContextMenu().apply {
                //新增节点菜单
                items.add(MenuItem("新增节点").apply {
                    setOnAction {
                        val item = httpTreeView.selectionModel?.selectedItem?.value
                        //假如是NODE,判断是否有子节点
                        if (HttpType.NODE.name != item?.type) {
                            Alert(Alert.AlertType.ERROR, "非节点类型，无法添加").show()
                            return@setOnAction
                        }
                        val textInputDialog = TextInputDialog().apply {
                            title = "请输入节点名称"
                        }
                        val inputStr = textInputDialog.showAndWait().get()
                        if (StrUtil.isNotBlank(inputStr)) {
                            val count = DbInfor.database.httpTools.count {
                                (it.parentId eq item.id) and (it.name eq inputStr)
                            }
                            if (count>0){
                                Alert(Alert.AlertType.ERROR, "请勿重复添加同名词节点").show()
                                return@setOnAction
                            }
                            DbInfor.database.insert(HttpTools){
                                set(HttpTools.id, StrUtil.uuid())
                                set(HttpTools.parentId,item.id)
                                set(HttpTools.name,inputStr)
                                set(HttpTools.type, HttpType.NODE.name)
                            }
                            reloadHttpTree(null)
                        }
                    }
                }
                )
                //删除菜单
                items.add(MenuItem("删除节点").apply {
                    setOnAction {
                        val item = httpTreeView.selectionModel?.selectedItem?.value
                        //假如是NODE,判断是否有子节点
                        val count = DbInfor.database.httpTools.count() {
                            it.parentId eq item!!.id
                        }
                        if (count > 0) {
                            val alert = Alert(Alert.AlertType.CONFIRMATION, "该节点下还有子节点,是否继续删除?")
                            if (alert.showAndWait().get() == ButtonType.OK) {
                                DbInfor.database.delete(HttpTools) { httpTool ->
                                    httpTool.id eq item!!.id
                                }
                            }
                        } else {
                            //println(item!!.id)
                            val alert = Alert(Alert.AlertType.CONFIRMATION, "是否删除?")
                            if (alert.showAndWait().get() == ButtonType.OK) {
                                DbInfor.database.delete(HttpTools) { httpTool ->
                                    httpTool.id eq item!!.id
                                }
                            }
                        }
                    }
                }
                )
            }
            addEventHandler(MouseEvent.MOUSE_CLICKED) {
                val item = this.selectionModel?.selectedItem?.value
                if (it.clickCount == 2) {
                    //双击
                    if (HttpType.HTTP.name == item?.type) {
                        loadHttpForm(item)
                    }
                } else if (it.clickCount == 1) {
                    //单击
                    if (HttpType.NODE.name == item?.type) {
                        currentHttpParentId = item.id
                    }
                }

            }

        }
    }

    /**
     * 加载http表单界面
     */
    private fun loadHttpForm(httpTool: HttpTool,callback: Callback<IndexController,HttpTool>) {
        tabList.tabs.add(Tab((httpTool.name)).apply {
            content = FXMLLoader.load(ResourceUtil.getResource("tab.fxml"),null, JavaFXBuilderFactory()) { it.name }
        })
    }


    private fun addChild(
        rootTreeItem: TreeItem<HttpTool>,
        httpTools: EntitySequence<HttpTool, HttpTools>,
        httpTool: HttpTool
    ) {

        val treeItem = buildTreeItem(httpTool)
        rootTreeItem.children.add(treeItem)
        if (HttpType.NODE.name.equals(httpTool.type)) {
            val childHttpTools = httpTools.filter { it.parentId eq httpTool.id!! }
            for (childHttpTool in childHttpTools) {
                addChild(treeItem, httpTools, childHttpTool)
            }
        }

    }
    private fun buildTreeItem(httpTool: HttpTool): TreeItem<HttpTool> {
        val isHttp = httpTool.type.equals("HTTP")
        var treeItem = TreeItem(
            httpTool,
            ImageView(Image(ResourceUtil.getStream(if (isHttp) "http.png" else "folder.png")))
        )
        return treeItem
    }
    /**
     * 刷新http树
     */
    fun reloadHttpTree(mouseEvent: MouseEvent?) {
        initTreeView()
    }

}