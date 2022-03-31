package cn.lifay.lerverhc.hander

import java.io.FileOutputStream
import java.util.prefs.Preferences

object ConfigUtil {
    //配置项
    private const val PREFERENCES_KEY = "/HCAppConfig"

    //配置项
    val preferences = Preferences.userRoot().node(PREFERENCES_KEY)
    //属性管理-输出文件夹
    const val PROPERTIES_OUTPUT_FOLDER = "PROPERTIES_OUTPUT_FOLDER"
    //Api管理-json文件夹
    const val API_JSON_FILE = "API_JSON_FILE"
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

}