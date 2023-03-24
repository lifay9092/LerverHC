package cn.lifay.lerverhc.model

import cn.hutool.core.util.StrUtil
import cn.lifay.lerverhc.model.enums.HttpType
import org.ktorm.database.Database
import org.ktorm.dsl.QueryRowSet
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.BaseTable
import org.ktorm.schema.varchar

data class HttpTool(
    var id: String,
    var parentId: String,
    var addrId: String?,
    var name: String,
    var type: String,
    var body: String?,//请求参数串
    var datas: String?,//
) {

    fun isNode(): Boolean {
        return HttpType.NODE.name == type
    }

    fun isHttp(): Boolean {
        return HttpType.HTTP.name == type
    }

    override fun toString(): String {
        return this.name
    }
}

object HttpTools : BaseTable<HttpTool>("HTTP_TOOL") {

    val id = varchar("id").primaryKey()
    var parentId = varchar("parent_id")
    var addrId = varchar("addr_id")
    val name = varchar("name")
    val type = varchar("type")
    val body = varchar("body")
    val datas = varchar("datas")

    override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean) = HttpTool(
        id = row[id] ?: StrUtil.uuid(),
        parentId = row[parentId] ?: "-1",
        addrId = row[addrId] ?: "",
        name = row[name]!!,
        type = row[type] ?: HttpType.NODE.name,
        body = row[body] ?: "",
        datas = row[datas] ?: "",

        )

    val Database.httpTools get() = this.sequenceOf(HttpTools)

    val DEFAULT_ADDR_ID = "custom"
    val DEFAULT_BODY_STR = """
        {
        
        }
    """.trimIndent()
    val DEFAULT_DATAS = """
                        {
                            "method":"GET","isBatch":false,
                            "isSync":false,
                            "url":"http://localhost:80/temp",
                            "authorization":"",
                            "contentType":"FORM_URLENCODED"}
                    """.trimIndent()
}