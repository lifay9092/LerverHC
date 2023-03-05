package cn.lifay.lerverhc.model

import cn.hutool.core.util.StrUtil
import org.ktorm.database.Database
import org.ktorm.dsl.QueryRowSet
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.BaseTable
import org.ktorm.schema.varchar

data class HttpAddr(
    var id: String,
    var name: String,
    var addr: String,
) {
    override fun toString(): String {
        return this.name
    }

    fun isCustom(): Boolean {
        return "custom" == id
    }
}

object HttpAddrs : BaseTable<HttpAddr>("HTTP_ADDR") {
    val id = varchar("id").primaryKey()
    val name = varchar("name")
    val addr = varchar("addr")


    override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean) = HttpAddr(
        id = row[id] ?: StrUtil.uuid(),
        name = row[name]!!,
        addr = row[addr] ?: "",
    )

    val Database.httpAddrs get() = this.sequenceOf(HttpAddrs)

}