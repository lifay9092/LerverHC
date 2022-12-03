package cn.lifay.lerverhc

import cn.lifay.lerverhc.db.BaseDao
import cn.lifay.lerverhc.model.HttpTool
import javafx.collections.ObservableList
import javafx.scene.control.TreeItem

object BusiService {


    fun deleteHttpTool(children: ObservableList<TreeItem<HttpTool>>, treeItem: TreeItem<HttpTool>) {
        treeItem.children?.let {
            val iterator = it.iterator()
            while (iterator.hasNext()) {
                val item = iterator.next()
                if (item.children.isNotEmpty()) {
                    BaseDao.deleteDeepHttpToolById(item.value.id)
                }
            }
        }
        BaseDao.deleteHttpToolById(treeItem.value.id)
        deleteChildTreeItem(children, treeItem)
    }


    fun deleteChildTreeItem(children: ObservableList<TreeItem<HttpTool>>, treeItem: TreeItem<HttpTool>) {
        val iterator = children.listIterator()
        while (iterator.hasNext()) {
            if (iterator.next().value.id == treeItem.value.id)
                iterator.remove()
        }
    }

}