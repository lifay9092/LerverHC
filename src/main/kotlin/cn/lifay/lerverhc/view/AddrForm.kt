package cn.lifay.lerverhc.view

import cn.lifay.lerverhc.db.DbInfor
import cn.lifay.ui.DelegateProp
import cn.lifay.ui.form.FormElement
import cn.lifay.ui.form.FormUI
import cn.lifay.ui.form.text.TextElement
import model.HttpAddr
import model.HttpAddrs.httpAddrs
import org.ktorm.entity.toList
import java.net.URL
import java.util.*


/**
 * AddrForm TODO
 * @author lifay
 * @date 2023/3/3 18:39
 **/
class AddrForm(t: HttpAddr?) : FormUI<HttpAddr>("地址管理", t) {
    override fun buildElements(): List<FormElement<HttpAddr, *>> {
        val id2 = TextElement("ID:", HttpAddr::id, true)
        val name = TextElement<HttpAddr, String>("名称:", HttpAddr::name, false, true)
        val addr = TextElement<HttpAddr, String>("地址:", HttpAddr::addr, false, true)
        return listOf(id,name,addr)
    }

    override fun datas(): List<HttpAddr> {
        return DbInfor.database.httpAddrs.toList()
    }

    override fun delData(primaryValue: Any?) {
        TODO("Not yet implemented")
    }

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
    }

    override fun saveData(t: HttpAddr?) {
        TODO("Not yet implemented")
    }

    override fun editData(t: HttpAddr?) {
        TODO("Not yet implemented")
    }
}