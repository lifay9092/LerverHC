package cn.lifay.lerverhc.view

import cn.hutool.core.io.resource.ResourceUtil
import cn.hutool.core.util.IdUtil
import cn.hutool.core.util.StrUtil
import cn.hutool.json.JSONUtil
import cn.lifay.lerverhc.db.DbInfor
import cn.lifay.lerverhc.hander.bindEscKey
import cn.lifay.lerverhc.hander.bootstrap
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseButton
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Pane
import javafx.stage.Modality
import javafx.stage.Stage
import cn.lifay.lerverhc.model.HttpTool
import cn.lifay.lerverhc.model.HttpTools
import cn.lifay.lerverhc.model.HttpTools.httpTools
import cn.lifay.lerverhc.model.enums.HttpType
import org.ktorm.dsl.and
import org.ktorm.dsl.delete
import org.ktorm.dsl.eq
import org.ktorm.dsl.insert
import org.ktorm.entity.count
import org.ktorm.entity.toList
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

    @FXML
    var keywordField = TextField()

    /*临时http对象*/
    var currentHttpId: String = ""
    var currentHttpParentId: String = ""


    override fun initialize(location: URL?, resources: ResourceBundle?) {
        /*图标渲染*/
        reloadHttpTreeImg.image = Image(ResourceUtil.getStream("reload.png"))
        /*初始化http树*/
        initTreeView("")
        keywordField.textProperty().addListener { obs, ov, nv ->
            if (nv != null) {
                initTreeView(nv)
            }
        }
    }

    /**
     * 初始化树
     */
    private fun initTreeView(keyword: String) {
        val rootTreeItem = TreeItem(
            HttpTool("0", "-1", "", "根目录", HttpType.NODE.name, "", ""),
            ImageView(Image(ResourceUtil.getStream("folder.png")))
        )
        //HttpTool("1","0","11111",HttpType.NODE.name,"","")

        var httpTools = DbInfor.database.httpTools.toList()
        if (keyword.isNotBlank()) {
            val nodeIds = getAllParentNodeIds(httpTools)
            httpTools = httpTools.filter { it.type == HttpType.HTTP.name || nodeIds.contains(it.id) }.toList()

            httpTools = httpTools.filter { it.name.contains(keyword) }.toList()
        }
        for (httpTool in httpTools.filter { it.parentId == "0" }) {
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
                                                reloadHttpTree()
                                            }
                                        }
                                    }

                                }
                            })
                            //新增节点菜单
                            items.add(MenuItem("新增节点").apply {
                                setOnAction {
                                    //新增一个临时节点
                                    var tempHttpModel = HttpTool(
                                        id = IdUtil.fastUUID(),
                                        parentId = selectItem.id,
                                        name = "新建http",
                                        type = HttpType.HTTP.name,
                                        addrId = "custom",
                                        body = """
                                        {
                                            
                                        }
                                    """.trimIndent(),
                                        datas = """
                                        {
                                            "method":"GET","isBatch":false,
                                            "isSync":false,
                                            "url":"http://localhost:80/temp",
                                            "authorization":"",
                                            "contentType":"FORM_URLENCODED"}
                                    """.trimIndent()
                                    )
                                    //入库
                                    DbInfor.database.insert(HttpTools) {
                                        set(HttpTools.id, tempHttpModel.id)
                                        set(HttpTools.parentId, tempHttpModel.parentId)
                                        set(HttpTools.name, tempHttpModel.name)
                                        set(HttpTools.type, tempHttpModel.type)
                                        set(HttpTools.addrId, tempHttpModel.addrId)
                                        set(HttpTools.body, tempHttpModel.body)
                                        set(HttpTools.datas, tempHttpModel.datas)
                                    }
                                    //加载
                                    addTreeItemById(httpTreeView.root.children, tempHttpModel.parentId, tempHttpModel)
                                    addTabHttpForm(tempHttpModel)
                                }
                            })
                            //导入API文档菜单
                            items.add(MenuItem("导入API文档").apply {
                                setOnAction {
                                    var apiManageStage = Stage()
                                    val fxmlLoader = FXMLLoader(ResourceUtil.getResource("apiManage.fxml"))
                                    val indexPane = fxmlLoader.load<Pane>()
                                    val apiController = fxmlLoader.getController<ApiController>()
                                    apiController.initForm(selectItem.id)
                                    var scene = Scene(indexPane).bootstrap()
                                    apiManageStage.apply {
                                        title = "Api管理"
                                        isResizable = false
                                        setScene(scene)
                                    }
                                    apiManageStage.show()

                                }
                            })
                        }
                        if (selectItem!!.id != "0") {
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
                                            //tab页
                                            tabList.tabs.removeIf { i ->
                                                i.id == selectItem.id
                                            }
                                            //树菜单
                                            httpTreeView.apply {
                                                removeTreeItemById(root.children, selectItem.id)
                                                refresh()
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

    private fun removeTreeItemById(children: ObservableList<TreeItem<HttpTool>>, id: String) {
        if (isExistTreeItemById(children, id)) {
            children.removeIf { i ->
                i.value.id == id
            }
            return
        }
        for (child in children) {
            if (child.children != null) {
                removeTreeItemById(child.children, id)
            }
        }
    }

    private fun addTreeItemById(children: ObservableList<TreeItem<HttpTool>>, parentId: String, httpTool: HttpTool) {

        for (child in children) {
            if (child.value.id == parentId && child.children != null) {
                val treeItem = buildTreeItem(httpTool)
                child.children.add(treeItem)
                return
            } else if (child.children != null) {
                addTreeItemById(child.children, parentId, httpTool)
            }
        }
    }

    private fun isExistTreeItemById(children: ObservableList<TreeItem<HttpTool>>, id: String): Boolean {
        return (children.find { it.value.id == id } != null)

    }

    private fun getAllParentNodeIds(httpTools: List<HttpTool>): List<String> {
        val list = ArrayList<String>()
        val httpList = httpTools.filter { it.type == HttpType.HTTP.name }.toList()
        loopHandle(httpList, list)
        return list
    }

    private fun loopHandle(httpLists: List<HttpTool>, list: ArrayList<String>) {
        for (httpTool in httpLists) {
            val id = getParentIdById(httpLists, httpTool.id)
            list.add(id)
        }
    }

    private fun getParentIdById(httpTools: List<HttpTool>, id: String): String {
        return httpTools.first() { it.id == id }.parentId
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
        rootTreeItem: TreeItem<HttpTool>, httpTools: List<HttpTool>, httpTool: HttpTool
    ) {
        val treeItem = buildTreeItem(httpTool)
        rootTreeItem.children.add(treeItem)
        if (httpTool.isNode()) {
            val childHttpTools = httpTools.filter { it.parentId == httpTool.id!! }
            for (childHttpTool in childHttpTools) {
                addChild(treeItem, httpTools, childHttpTool)
            }
        }
    }

    private fun buildTreeItem(httpTool: HttpTool): TreeItem<HttpTool> {
        val treeItem = TreeItem(
            httpTool, ImageView(Image(ResourceUtil.getStream(getTreeItemImg(httpTool))))
        )
        return treeItem
    }

    private fun getTreeItemImg(httpTool: HttpTool): String {
        if (!httpTool.isHttp()) {
            return "folder.png"
        } else {
            val jsonObject = JSONUtil.parseObj(httpTool.datas)
            val method = jsonObject.getStr("method")
            return when (method) {
                "GET" -> "get.png"
                "POST" -> "post.png"
                else -> "http.png"
            }
        }
    }

    /**
     * 刷新http树
     */
    fun reloadHttpTree() {
        initTreeView("")
    }

    /**
     * 属性管理菜单
     */
    fun propertiesManage(actionEvent: ActionEvent) {
        //propertiesManage
        val propertiesManageStage = Stage().bindEscKey()
        propertiesManageStage.initModality(Modality.WINDOW_MODAL)
        val indexPane = FXMLLoader.load<Pane>(ResourceUtil.getResource("propertiesManage.fxml"))
        val scene = Scene(indexPane, 700.0, 500.0).bootstrap()
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
        val addrManageStage = Stage().bindEscKey()
        addrManageStage.initModality(Modality.WINDOW_MODAL)
        val indexPane = FXMLLoader.load<Pane>(ResourceUtil.getResource("addrManage.fxml"))
        val scene = Scene(indexPane).bootstrap()
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


    }

    /**
     * 移动到节点的界面
     */
    fun selectParentNode(id: String) {
        var selectParentStage = Stage().bindEscKey()
        val fxmlLoader = FXMLLoader(ResourceUtil.getResource("selectParent.fxml"))
        val indexPane = fxmlLoader.load<Pane>()
        val selectParentController = fxmlLoader.getController<SelectParentController>()
        selectParentController.initForm(id) { initTreeView("") }
        val scene = Scene(indexPane).bootstrap()
        selectParentStage.apply {
            title = "选择新节点"
            isResizable = false
            setScene(scene)
        }
        selectParentStage.show()
    }

    /**
     * 转换json
     */
    fun convertToJsonTool(actionEvent: ActionEvent) {
        val convertToJsonToolStage = Stage().bindEscKey()
        convertToJsonToolStage.initModality(Modality.APPLICATION_MODAL)
        val indexPane = FXMLLoader.load<Pane>(ResourceUtil.getResource("convertToJsonTool.fxml"))
        val scene = Scene(indexPane).bootstrap()
        convertToJsonToolStage.apply {
            title = "转换JSON"
            isResizable = false
            setScene(scene)
        }
        convertToJsonToolStage.show()
    }

}