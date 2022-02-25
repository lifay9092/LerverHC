package cn.lifay.lerverhc.hander

import cn.lifay.lerverhc.db.DbInfor
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.select
import org.ktorm.dsl.update
import org.ktorm.entity.find

object GlobeProps {
    lateinit var OutputFolder : String


//    init {
//        val list = DbInfor.database.from(PropInfos).select(PropInfos.name, PropInfos.value)
//        for (rowSet in list) {
//            if (rowSet[PropInfos.name] == "OutputFolder") {
//                OutputFolder = rowSet[PropInfos.value].toString()
//            }
//        }
//    }
    fun getOutputFolderValue(): String {
        return OutputFolder
    }
    fun setOutputFolderValue(str : String?){
//        if (str != null) {
//            OutputFolder = str
//
//            var outputFolderObj = DbInfor.database.propInfos.find { it.name eq "OutputFolder" } ?: return
//            outputFolderObj.value = str
//            DbInfor.database.update(PropInfos){
//                set(it.value,str)
//                where { it.name eq "OutputFolder" }
//            }
//        }

    }
}