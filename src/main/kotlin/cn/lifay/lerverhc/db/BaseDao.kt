package cn.lifay.lerverhc.db

import cn.lifay.lerverhc.model.HttpTools
import cn.lifay.lerverhc.model.HttpTools.httpTools
import org.ktorm.dsl.delete
import org.ktorm.dsl.eq
import org.ktorm.entity.filter
import org.ktorm.entity.toList

object BaseDao {

    fun deleteHttpToolById(id: String) {
        DbInfor.database.delete(HttpTools) { httpTool ->
            httpTool.id eq id
        }
    }

    fun deleteDeepHttpToolById(id: String) {
        val c = DbInfor.database.httpTools.filter { it.parentId eq id }.toList()
        if (c.isNotEmpty()) {
            c.forEach { deleteDeepHttpToolById(it.id) }
        }
        DbInfor.database.delete(HttpTools) { httpTool ->
            httpTool.id eq id
        }
    }

}