package cn.lifay.lerverhc.hander

import java.io.FileOutputStream
import java.util.prefs.Preferences

object ConfigUtil {
    //配置项
    private const val PREFERENCES_KEY = "/HCAppConfig"

    //配置项
    val preferences = Preferences.userRoot().node(PREFERENCES_KEY)

    /**
     * 马上更新数据
     */
    fun flush() {
        preferences.flush()
    }

    /**
     * 导出配置
     */
    fun export(name: String) {
        val outputStream = FileOutputStream(name)
        preferences.exportNode(outputStream)
        outputStream.close()
    }

    fun setOutputFolderValue(str : String){
        preferences.put("OutputFolder",str)
    }
    fun getOutputFolderValue():String{
        return preferences.get("OutputFolder",System.getProperty("user.dir"))
    }
}