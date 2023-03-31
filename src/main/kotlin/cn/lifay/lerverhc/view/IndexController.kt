package cn.lifay.lerverhc.view

import cn.hutool.core.io.resource.ResourceUtil
import cn.hutool.core.util.IdUtil
import cn.hutool.core.util.StrUtil
import cn.hutool.json.JSONUtil
import cn.lifay.extension.bindEscKey
import cn.lifay.lerverhc.BusiService
import cn.lifay.lerverhc.db.DbInfor
import cn.lifay.lerverhc.hander.ConfigUtil
import cn.lifay.lerverhc.hander.bootstrap
import cn.lifay.lerverhc.model.HttpTool
import cn.lifay.lerverhc.model.HttpTools
import cn.lifay.lerverhc.model.HttpTools.httpTools
import cn.lifay.lerverhc.model.enums.HttpType
import cn.lifay.ui.BaseView
import cn.lifay.ui.GlobeTheme
import cn.lifay.ui.LoadingUI
import cn.lifay.ui.tree.*
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseButton
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.insert
import org.ktorm.entity.count
import org.ktorm.entity.toList
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.KMutableProperty0


/**
 *@ClassName IndexController
 *@Description TODO
 *@Author lifay
 *@Date 2022/2/25 15:21
 **/
class IndexController : BaseView<BorderPane>() {

    @FXML
    var rootPane = BorderPane()

    @FXML
    var leftPane = VBox()

    @FXML
    var tabPane: TabPane = TabPane()

    @FXML
    var httpTreeView = TreeView<HttpTool>()

    @FXML
    var reloadHttpTreeImg = ImageView()

    @FXML
    var keywordField = TextField()

    /*临时http对象*/
    var currentHttpId: String = ""
    var currentHttpParentId: String = ""
    override fun rootPane(): KMutableProperty0<BorderPane> {
        return this::rootPane
    }

    fun fxml(): URL? {
        return ResourceUtil.getResource("index.fxml")
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        super.initialize(location, resources)
        /*布局大小*/
        leftPane.prefWidthProperty().bind(rootPane.prefWidthProperty().multiply(0.2))
        leftPane.prefHeightProperty().bind(rootPane.prefHeightProperty().multiply(0.9))

        tabPane.prefWidthProperty().bind(rootPane.prefWidthProperty().multiply(0.8))
        tabPane.prefHeightProperty().bind(rootPane.prefHeightProperty())

        /*图标渲染*/
        reloadHttpTreeImg.image = Image(ConfigUtil.RELOAD_IMG)
        /*初始化http树*/
        initTreeView()
        keywordField.textProperty().addListener { obs, ov, nv ->
            if (ov.isNullOrBlank() && nv.isNullOrBlank()) {
                return@addListener
            }
            if (!ov.isNullOrBlank() && nv.isNullOrBlank()) {
                httpTreeView.RefreshTree<HttpTool,String>()
            }else if (!nv.isNullOrBlank()){
                httpTreeView.RefreshTree<HttpTool,String>(keyword = nv)
            }
        }
    }


    //    private var HTTP_TOOL_DATA_LIST = FXCollections.observableArrayList<>()
    private var ROOT_TREE_ITEM = TreeItem(
        HttpTool("0", "-1", "", "根目录", HttpType.NODE.name, "", ""),
        ImageView(ConfigUtil.FOLDER_IMG)
    )
    /*    private val ROOT_TREE_ITEM_PROPERTIY = SimpleObjectProperty<TreeItem<HttpTool>>().apply {
            value = ROOT_TREE_ITEM
        }*/


    /**
     * 初始化树
     */
    private fun initTreeView() {

        httpTreeView.apply {
            root = ROOT_TREE_ITEM
            isShowRoot = true
            Register(HttpTool::id, HttpTool::parentId,true, imgCall = {
                it.graphic = ImageView(getTreeItemImg(it.value))
            }){DbInfor.database.httpTools.toList()}
            setOnMouseDragReleased {
                println("release")
            }
            setOnDragDone {
                println("done...")
            }
            //节点点击事件
            setOnMouseClicked {
                val selectItem = selectionModel.selectedItem ?: return@setOnMouseClicked
                val selectItemValue = selectItem.value
                if (it.button == MouseButton.SECONDARY) {
                    //右键
                    contextMenu = ContextMenu().apply {
                        if (selectItemValue?.isNode() == true) {
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
                                                    (it.parentId eq selectItemValue.id) and (it.name eq inputStr)
                                                }
                                                if (count > 0) {
                                                    alertError("请勿重复添加同名节点")
                                                    return@setOnAction
                                                }
                                                val httpTool = HttpTool(StrUtil.uuid(),selectItemValue.id,null,inputStr,HttpType.NODE.name,null,null)
                                                DbInfor.database.insert(HttpTools) {
                                                    set(HttpTools.id, httpTool.id)
                                                    set(HttpTools.parentId, httpTool.parentId)
                                                    set(HttpTools.name, httpTool.name)
                                                    set(HttpTools.type, httpTool.type)
                                                }
                                                //更新选中的父目录
                                                selectItem.AddChildren(httpTool)
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
                                        parentId = selectItemValue.id,
                                        name = "新建http",
                                        type = HttpType.HTTP.name,
                                        addrId = HttpTools.DEFAULT_ADDR_ID,
                                        body = HttpTools.DEFAULT_BODY_STR,
                                        datas = HttpTools.DEFAULT_DATAS
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
                                    //更新UI
                                    selectItem.AddChildren(tempHttpModel)
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
                                    apiController.initForm(selectItemValue.id)
                                    var scene = Scene(indexPane).bootstrap()
                                    apiManageStage.apply {
                                        title = "Api管理"
                                        isResizable = false
                                        setScene(scene)
                                    }
                                    apiManageStage.show()

                                }
                            })
                        } else {
                            //复制节点菜单
                            items.add(MenuItem("复制").apply {
                                setOnAction {
                                    val newHttpName = getNewHttpName(selectItemValue!!.parentId, selectItemValue.name)
                                    val httpTool = HttpTool(StrUtil.uuid(),selectItemValue.parentId,selectItemValue.addrId,newHttpName,HttpType.HTTP.name,selectItemValue.body,selectItemValue.datas)
                                    DbInfor.database.insert(HttpTools) {
                                        set(HttpTools.id, httpTool.id)
                                        set(HttpTools.parentId, httpTool.parentId)
                                        set(HttpTools.name, httpTool.name)
                                        set(HttpTools.type, httpTool.type)
                                        set(HttpTools.addrId, httpTool.addrId)
                                        set(HttpTools.body, httpTool.body)
                                        set(HttpTools.datas, httpTool.datas)
                                    }
                                    //更新选中的父目录
                                    selectItem.AddChildren(httpTool)
                                }
                            })
                        }
                        if (selectItemValue!!.id != "0") {
                            //删除菜单
                            items.add(MenuItem("删除节点").apply {
                                setOnAction {
                                    //假如是NODE,判断是否有子节点
                                    val count = DbInfor.database.httpTools.count() {
                                        it.parentId eq selectItemValue!!.id
                                    }
                                    if (count > 0) {
                                        if (alertConfirmation("该节点下还有子节点,是否继续删除?")) {
                                            //递归删除
                                            val loadingUI = LoadingUI(rootPane.scene.window as Stage)
                                            GlobalScope.launch {
                                                try {
                                                    loadingUI.show()
                                                    BusiService.deleteHttpTool(
                                                        selectionModel.selectedItem.parent.children,
                                                        selectionModel.selectedItem
                                                    )
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                } finally {
                                                    loadingUI.closeStage()
                                                }

                                            }
                                        }

                                    } else {
                                        //println(item!!.id)
                                        if (alertConfirmation("是否删除?")) {
                                            BusiService.deleteHttpTool(
                                                selectionModel.selectedItem.parent.children,
                                                selectionModel.selectedItem
                                            )
                                            //tab页
                                            tabPane.tabs.removeIf { i ->
                                                i.id == selectItemValue.id
                                            }
                                            //树菜单
                                            selectItem.DeleteThis()
                                        }
                                    }
                                }
                            })

                            //移动到 菜单
                            items.add(MenuItem("移动到").apply {
                                setOnAction {
                                    selectParentNode(selectItem)
                                }
                            })
                        }

                    }
                } else if (it.clickCount == 2) {
                    //双击
                    if (selectItemValue?.isHttp() == true) {
                        addTabHttpForm(selectItemValue)
                    } else {
                        selectionModel.selectedItem?.let { si -> si.isExpanded = true }
                    }
                } else if (it.clickCount == 1) {
                    //单击
                    if (selectItemValue?.isNode() == true) {
                        currentHttpParentId = selectItemValue.id
                    }
                }
            }
        }
    }

    private fun getNewHttpName(parentId: String, name: String): String {
        val newName = "$name-new"
        val count = DbInfor.database.httpTools.count {
            (it.parentId eq parentId) and (it.name eq newName)
        }
        if (count > 0) {
            return getNewHttpName(parentId, newName)
        }
        return newName
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
        for (t in tabPane.tabs) {
            if (t.id == httpTool.id) {
                tabPane.selectionModel.select(t)
                return
            }
        }
        //添加并打开新的
        val indexController = this
        val httpToolView = createView<HttpToolController, VBox>(ResourceUtil.getResource("httpTool.fxml")) {
            initForm(indexController, httpTool.id)
        }
        val httpToolPane = httpToolView.getRoot() as Pane

        httpToolPane.prefWidthProperty().bind(tabPane.prefWidthProperty())
        httpToolPane.prefHeightProperty().bind(tabPane.prefHeightProperty())
        val tab = Tab((httpTool.name)).apply {
            id = httpTool.id
            content = httpToolPane
        }
        tabPane.tabs.add(tab)
        tabPane.selectionModel.select(tab)
    }



    private fun getTreeItemImg(httpTool: HttpTool): Image {
        if (!httpTool.isHttp()) {
            return ConfigUtil.FOLDER_IMG
        } else {
            val jsonObject = JSONUtil.parseObj(httpTool.datas)
            val method = jsonObject.getStr("method")
            return when (method) {
                "GET" -> ConfigUtil.GET_IMG
                "POST" -> ConfigUtil.POST_IMG
                else -> ConfigUtil.HTTP_IMG
            }
        }
    }

    /**
     * 刷新http树
     */
 /*   fun reloadHttpTree() {
        initTreeView("")
    }*/

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
        val addrForm = AddrForm()
        addrForm.show()
    }

    /**
     * api管理菜单
     */
    fun apiManage(actionEvent: ActionEvent) {


    }

    /**
     * 移动到 新目录节点下
     */
    fun selectParentNode(selectItem: TreeItem<HttpTool>) {
        var selectParentStage = Stage().bindEscKey()
        val fxmlLoader = FXMLLoader(ResourceUtil.getResource("selectParent.fxml"))
        val indexPane = fxmlLoader.load<Pane>()
        val selectParentController = fxmlLoader.getController<SelectParentController>()
        selectParentController.initForm(selectItem.value.id) {
            selectItem.DeleteThis()
            httpTreeView.RefreshTree<HttpTool,String>()
        }
        val scene = Scene(indexPane).bootstrap()
        selectParentStage.apply {
            title = "选择新节点"
            isResizable = false
            setScene(scene)
            setOnCloseRequest {
                selectParentController.parentTreeView.ClearCache()
            }
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

    fun whiteTheme(actionEvent: ActionEvent) {
        GlobeTheme.setWhite()
        //  Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA)
    }

    fun darkTheme(actionEvent: ActionEvent) {
        GlobeTheme.setDark()
        // Application.setUserAgentStylesheet(Application.STYLESHEET_CASPIAN)
    }

}