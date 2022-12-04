package cn.lifay.lerverhc.db

import cn.lifay.lerverhc.model.HttpTool
import cn.lifay.lerverhc.model.HttpTools
import cn.lifay.lerverhc.model.HttpTools.httpTools
import org.ktorm.dsl.delete
import org.ktorm.dsl.eq
import org.ktorm.dsl.inList
import org.ktorm.entity.filter
import org.ktorm.entity.toList

object BaseDao {

    fun deleteHttpToolById(id: String) {
        DbInfor.database.delete(HttpTools) { httpTool ->
            httpTool.id eq id
        }
    }

    fun deleteDeepHttpToolById(id: String) {
        val c = getChildrenById(id)
        if (c.isNotEmpty()) {
            //缓存所有父ID
            val temps = ArrayList<String>()
            temps.add(id)
            for (i in c) {
                addDeepPanId(temps, i)
            }
            DbInfor.database.delete(HttpTools) { httpTool ->
                httpTool.parentId inList temps
            }
        }
        DbInfor.database.delete(HttpTools) { httpTool ->
            httpTool.id eq id
        }
    }

    private fun addDeepPanId(temps: ArrayList<String>, httpTool: HttpTool) {
        val c = getChildrenById(httpTool.id)
        if (c.isNotEmpty()) {
            temps.add(httpTool.id)
            for (i in c) {
                addDeepPanId(temps, i)
            }
        }
    }

    fun getChildrenById(id: String): List<HttpTool> {
        val c = DbInfor.database.httpTools.filter { it.parentId eq id }.toList()
        if (c.isNotEmpty()) {
            return c
        }
        return ArrayList()
    }

}