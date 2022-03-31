package cn.lifay.lerverhc.view

import cn.hutool.core.io.resource.ResourceUtil
import cn.hutool.core.util.StrUtil
import cn.lifay.lerverhc.db.DbInfor
import javafx.event.ActionEvent
import javafx.event.EventType
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.DragEvent
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Pane
import javafx.stage.Stage
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
        var rootTreeItem = TreeItem(HttpTool("0", "-1", "", "根目录", HttpType.NODE.name, "", ""),ImageView(Image(ResourceUtil.getStream("folder.png"))))

        //HttpTool("1","0","11111",HttpType.NODE.name,"","")
        val httpTools = DbInfor.database.httpTools

        for (httpTool in httpTools.filter { it.parentId eq "0" }) {
            //child
            addChild(rootTreeItem, httpTools, httpTool)

        }
        httpTreeView.apply {
            root = rootTreeItem
            isShowRoot = true
            setOnMouseDragReleased {
                println("release")
            }
            setOnDragDone {
                println("done...")
            }
            //节点点击事件
            setOnMouseClicked {

                val selectItem = selectionModel.selectedItem?.value
                if (it.button == MouseButton.SECONDARY) {
                    //右键
                    contextMenu = ContextMenu().apply {
                        if (selectItem?.isNode() == true) {
                            //新增目录菜单
                            items.add(MenuItem("新增目录").apply {
                                setOnAction {
                                    //假如是NODE,判断是否有子节点
                                    if (selectItem.isHttp()) {
                                        Alert(Alert.AlertType.ERROR, "非节点类型，无法添加").show()
                                        return@setOnAction
                                    }
                                    val textInputDialog = TextInputDialog().apply {
                                        title = "请输入节点名称"
                                    }
                                    textInputDialog.showAndWait().let {
                                        if (it.isPresent) {
                                            val inputStr = it.get()
                                            if (StrUtil.isNotBlank(inputStr)) {
                                                val count = DbInfor.database.httpTools.count {
                                                    (it.parentId eq selectItem.id) and (it.name eq inputStr)
                                                }
                                                if (count > 0) {
                                                    Alert(Alert.AlertType.ERROR, "请勿重复添加同名节点").show()
                                                    return@setOnAction
                                                }
                                                DbInfor.database.insert(HttpTools) {
                                                    set(HttpTools.id, StrUtil.uuid())
                                                    set(HttpTools.parentId, selectItem.id)
                                                    set(HttpTools.name, inputStr)
                                                    set(HttpTools.type, HttpType.NODE.name)
                                                }
                                                reloadHttpTree(null)
                                            }
                                        }
                                    }

                                }
                            })
                        }
                        if (selectItem!!.id != "0"){
                            //删除菜单
                            items.add(MenuItem("删除节点").apply {
                                setOnAction {
                                    //假如是NODE,判断是否有子节点
                                    val count = DbInfor.database.httpTools.count() {
                                        it.parentId eq selectItem!!.id
                                    }
                                    if (count > 0) {
                                        val alert = Alert(Alert.AlertType.CONFIRMATION, "该节点下还有子节点,是否继续删除?")
                                        if (alert.showAndWait().get() == ButtonType.OK) {
                                            DbInfor.database.delete(HttpTools) { httpTool ->
                                                httpTool.id eq selectItem!!.id
                                            }
                                        }
                                    } else {
                                        //println(item!!.id)
                                        val alert = Alert(Alert.AlertType.CONFIRMATION, "是否删除?")
                                        if (alert.showAndWait().get() == ButtonType.OK) {
                                            DbInfor.database.delete(HttpTools) { httpTool ->
                                                httpTool.id eq selectItem!!.id
                                            }
                                        }
                                    }
                                }
                            })
                            //移动到 菜单
                            items.add(MenuItem("移动到").apply {
                                setOnAction {
                                    selectParentNode(selectItem.id)
                                }
                            })
                        }

                    }
                } else if (it.clickCount == 2) {
                    //双击
                    if (selectItem?.isHttp() == true) {
                        addTabHttpForm(selectItem)
                    }
                } else if (it.clickCount == 1) {
                    //单击
                    if (selectItem?.isNode() == true) {
                        currentHttpParentId = selectItem.id
                    }
                }
            }
        }
    }

    /**
     * 新增tab并加载http表单界面
     */
    private fun addTabHttpForm(httpTool: HttpTool) {
        //判断是否已经打开，已打开则选择
        for (t in tabList.tabs) {
            if (t.id == httpTool.id) {
                tabList.selectionModel.select(t)
                return
            }
        }
        //添加并打开新的
        val fxmlLoader = FXMLLoader(ResourceUtil.getResource("httpTool.fxml"))
        val load = fxmlLoader.load<Any>()
        val httpToolController = fxmlLoader.getController<HttpToolController>()
        httpToolController.initForm(this, httpTool.id)
        val tab = Tab((httpTool.name)).apply {
            id = httpTool.id
            content = load as Node?
        }
        tabList.tabs.add(tab)
        tabList.selectionModel.select(tab)

    }


    private fun addChild(
        rootTreeItem: TreeItem<HttpTool>, httpTools: EntitySequence<HttpTool, HttpTools>, httpTool: HttpTool
    ) {

        val treeItem = buildTreeItem(httpTool)

        rootTreeItem.children.add(treeItem)
        if (httpTool.isNode()) {
            val childHttpTools = httpTools.filter { it.parentId eq httpTool.id!! }
            for (childHttpTool in childHttpTools) {
                addChild(treeItem, httpTools, childHttpTool)
            }
        }
    }

    private fun buildTreeItem(httpTool: HttpTool): TreeItem<HttpTool> {
        val treeItem = TreeItem(
            httpTool, ImageView(Image(ResourceUtil.getStream(if (httpTool.isHttp()) "http.png" else "folder.png")))
        )
        return treeItem
    }

    /**
     * 刷新http树
     */
    fun reloadHttpTree(mouseEvent: MouseEvent?) {
        initTreeView()
    }

    /**
     * 属性管理菜单
     */
    fun propertiesManage(actionEvent: ActionEvent) {
        //propertiesManage
        var propertiesManageStage = Stage()
        val indexPane = FXMLLoader.load<Pane>(ResourceUtil.getResource("propertiesManage.fxml"))
        var scene = Scene(indexPane, 700.0, 500.0)
        propertiesManageStage.apply {
            title = "属性管理"
            isResizable = false
            setScene(scene)
        }
        propertiesManageStage.show()
    }

    /**
     * 地址管理菜单
     */
    fun addrManage(actionEvent: ActionEvent) {
        var addrManageStage = Stage()
        val indexPane = FXMLLoader.load<Pane>(ResourceUtil.getResource("addrManage.fxml"))
        var scene = Scene(indexPane)
        addrManageStage.apply {
            title = "地址管理"
            isResizable = false
            setScene(scene)
        }
        addrManageStage.show()

    }

    /**
     * api管理菜单
     */
    fun apiManage(actionEvent: ActionEvent) {
        var apiManageStage = Stage()
        val indexPane = FXMLLoader.load<Pane>(ResourceUtil.getResource("apiManage.fxml"))
        var scene = Scene(indexPane)
        apiManageStage.apply {
            title = "Api管理"
            isResizable = false
            setScene(scene)
        }
        apiManageStage.show()

    }

    /**
     * 移动到节点的界面
     */
    fun selectParentNode(id : String) {
        var selectParentStage = Stage()
        val fxmlLoader = FXMLLoader(ResourceUtil.getResource("selectParent.fxml"))
        val indexPane = fxmlLoader.load<Pane>()
        val selectParentController = fxmlLoader.getController<SelectParentController>()
        selectParentController.initForm(id) { initTreeView() }
        var scene = Scene(indexPane)
        selectParentStage.apply {
            title = "选择新节点"
            isResizable = false
            setScene(scene)
        }
        selectParentStage.show()
    }

}