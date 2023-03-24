package cn.lifay.lerverhc.view

import cn.lifay.lerverhc.db.DbInfor
import cn.lifay.lerverhc.model.HttpAddr
import cn.lifay.lerverhc.model.HttpAddrs
import cn.lifay.lerverhc.model.HttpAddrs.httpAddrs
import cn.lifay.ui.form.FormUI
import cn.lifay.ui.form.text.TextElement
import org.ktorm.dsl.eq
import org.ktorm.dsl.insert
import org.ktorm.dsl.update
import org.ktorm.entity.count
import org.ktorm.entity.removeIf
import org.ktorm.entity.toList
import java.util.*


/**
 * AddrForm 地址管理
 * @author lifay
 * @date 2023/3/3 18:39
 **/
class AddrForm(t: HttpAddr? = null) : FormUI<HttpAddr>("地址管理", t, buildElements = {
    val id = TextElement("ID:", HttpAddr::id, true)
    val name = TextElement("名称:", HttpAddr::name)
    val addr = TextElement("地址:", HttpAddr::addr)
    listOf(id, name, addr)
}) {


    override fun datas(): List<HttpAddr> {
        return DbInfor.database.httpAddrs.toList()
    }

    override fun delData(primaryValue: Any?) {
        DbInfor.database.httpAddrs.removeIf { it.id eq primaryValue as String }
    }

    override fun saveData(entity: HttpAddr?) {
        //新增数据 判断名称重复
        if (DbInfor.database.httpAddrs.count() { it.name eq entity!!.name } > 0) {
            alertError("名称[${entity!!.name}]已存在!")
            return
        }
        DbInfor.database.insert(HttpAddrs) {
            set(HttpAddrs.id, entity!!.id ?: UUID.randomUUID().toString())
            set(HttpAddrs.name, entity.name)
            set(HttpAddrs.addr, entity.addr)
        }
    }

    override fun editData(entity: HttpAddr?) {
        DbInfor.database.update(HttpAddrs) {
            set(it.id, entity!!.id)
            set(it.name, entity.name)
            set(it.addr, entity.addr)
            where { it.id eq entity.id }
        }
    }
}