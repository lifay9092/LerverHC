package cn.lifay.lerverhc.view

import cn.hutool.core.util.IdUtil
import cn.hutool.core.util.StrUtil
import cn.lifay.lerverhc.db.DbInfor
import cn.lifay.lerverhc.model.HttpAddr
import cn.lifay.lerverhc.model.HttpAddrs
import cn.lifay.lerverhc.model.HttpAddrs.httpAddrs
import cn.lifay.ui.form.FormUI
import cn.lifay.ui.form.text.TextElement
import org.ktorm.dsl.eq
import org.ktorm.entity.count
import org.ktorm.entity.removeIf
import org.ktorm.entity.toList
import org.ktorm.support.sqlite.insertOrUpdate


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
        val id = if (StrUtil.isBlank(entity!!.id)) IdUtil.simpleUUID() else entity.id
        DbInfor.database.insertOrUpdate(HttpAddrs) {
            set(HttpAddrs.id, id)
            set(HttpAddrs.name, entity.name)
            set(HttpAddrs.addr, entity.addr)
            onConflict(it.id) {
                set(HttpAddrs.name, entity.name)
                set(HttpAddrs.addr, entity.addr)
            }
        }
    }

}