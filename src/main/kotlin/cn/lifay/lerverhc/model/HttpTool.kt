package model

import cn.hutool.core.util.StrUtil
import model.enum.HttpType
import org.ktorm.database.Database
import org.ktorm.dsl.QueryRowSet
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.BaseTable
import org.ktorm.schema.varchar

data class HttpTool(
    var id: String,
    var parentId: String,
    var name: String,
    var type: String,
    var body: String,
    var datas: String,
){
    override fun toString(): String {
        return this.name
    }
}

object HttpTools : BaseTable<HttpTool>("HTTP_TOOL") {
    val id = varchar("id").primaryKey()
    var parentId = varchar("parent_id")
    val name = varchar("name")
    val type = varchar("type")
    val body = varchar("body")
    val datas = varchar("datas")

    override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean) = HttpTool(
        id = row[id] ?: StrUtil.uuid(),
        parentId = row[parentId] ?: "-1",
        name = row[name]!!,
        type = row[type] ?: HttpType.NODE.name,
        body = row[body] ?: "",
        datas = row[datas] ?: "",

    )
    val Database.httpTools get() = this.sequenceOf(HttpTools)

}