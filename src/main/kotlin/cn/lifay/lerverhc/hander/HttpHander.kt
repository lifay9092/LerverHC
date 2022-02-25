package cn.lifay.lerverhc.hander

import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.ReUtil
import cn.hutool.core.util.StrUtil
import cn.hutool.http.HttpRequest
import cn.hutool.http.HttpResponse
import cn.hutool.json.JSONObject
import cn.hutool.json.JSONUtil
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import java.io.File
import java.nio.charset.Charset
import java.util.regex.Pattern
import java.util.stream.Collectors

/**
 *@ClassName HttpHander
 *@Description TODO
 *@Author lifay
 *@Date 2022/1/5 17:45
 **/
object HttpHander {

    fun checkDataFile(bodyStr: String?, batchDataFilePath: String): String {
        //提取变量key ${}
        var keys = getVars(bodyStr)
        //循环批量数据文件
        val readStr = FileUtil.readString(batchDataFilePath, Charset.forName("utf-8"))
        if (!JSONUtil.isJsonArray(readStr)) {
            return "检查完毕:${batchDataFilePath} 非json数组格式"
        }
        val jsonArray = JSONUtil.parseArray(readStr)
        for (index in jsonArray.indices) {
            val jsonObject: JSONObject = jsonArray[index] as JSONObject
            for (key in keys!!) {
                val v = getStrValueByKey(jsonObject, key)
                if (v.isEmpty()) {
                    return "检查完毕:请检查index:[${index}]的参数$key"
                }
            }
        }
        return "检查完毕:成功"
    }

    /**
     * 获取模板变量
     *
     * @param str
     * @return
     */
    fun getVars(str: String?): List<String>? {
        return ReUtil.findAllGroup1(Pattern.compile("\\$\\{(.*)\\}"), str).stream().distinct()
            .collect(Collectors.toList())
    }

    /**
     * 获取模板变量
     *
     * @param str
     * @return
     */
    fun getVarsNew(str: String?): List<String>? {
        return ReUtil.findAllGroup1(Pattern.compile("\\$\\[([A-Za-z0-9]+)\\]"), str).stream().distinct()
            .collect(Collectors.toList())
    }

    /**
     * 发送http请求
     */
    fun singleSendHttp(httpRequest: HttpRequest?, bodyStr: String): HttpResponse? {
        httpRequest!!.body(bodyStr)
        return httpRequest.execute()
    }

    /**
     * 批量发送http请求
     */
    fun batchSendHttp(
        httpRequest: HttpRequest?,
        bodyStr: String?,
        batchDataFilePath: String,
        batchFileNameStr: String
    ): String? {
        var count = 0
        try {/*解析模板和数据文件*/
            if (!FileUtil.exist(batchDataFilePath)) {
                Alert(Alert.AlertType.ERROR, "$batchDataFilePath 不存在", ButtonType.CLOSE).show()
                return ""
            }
            val readStr = FileUtil.readString(batchDataFilePath, Charset.forName("utf-8"))
            if (!JSONUtil.isJsonArray(readStr)) {
                Alert(Alert.AlertType.ERROR, "$batchDataFilePath 非json数组格式", ButtonType.CLOSE).show()
                return ""
            }
            //输出目录
            val outputDir = GlobeProps.getOutputFolderValue() + File.separator
            //提取变量key ${}
            var bodyKeys = getVars(bodyStr)
            var fileNameKeys = getVars(batchFileNameStr)
            //遍历
            val jsonArray = JSONUtil.parseArray(readStr)
            for (index in jsonArray.indices) {
                val jsonObject: JSONObject = jsonArray[index] as JSONObject
                //替换模板变量
                val realBodyStr = getRealReplaceStr(bodyStr, bodyKeys, jsonObject)
                try {

                    var httpResponsebody = httpRequest!!.body(realBodyStr).execute().body()
                    //println(httpRequest!!.body(realBodyStr).execute().body())
                    //输出文件名
                    val realFileNameStr = getRealReplaceStr(batchFileNameStr, fileNameKeys, jsonObject)
                    var outputFilePath = "$outputDir$realFileNameStr.json"
                    FileUtil.writeString(httpResponsebody, outputFilePath, Charset.forName("utf-8"))
                } catch (e: Exception) {
                    return "执行失败,index[${index}],msg:${e.message}"
                }
                count++
            }
            return "批量执行成功,总共 ${count} 条,输出目录:$outputDir"
        } catch (e: Exception) {
            Alert(Alert.AlertType.ERROR, "运行出错:${e.message}", ButtonType.CLOSE).show()
        }
        return ""
    }

    /**
     * 将bodyStr里的变量转换成真实值
     */
    fun getRealReplaceStr(bodyStr: String?, keys: List<String>?, jsonObject: JSONObject): String? {
        var str = bodyStr
        for (key in keys!!) {
            val v: String = getStrValueByKey(jsonObject, key)
            if (v.isBlank()) {
                throw Exception("key不存在:${key}")
            }
            str = str!!.replace("\${${key}}", v)
        }
        return str
    }

    private fun getStrValueByKey(jsonObject: JSONObject, key: String): String {
        if (!key.contains(".")) {
            return StrUtil.toString(jsonObject[key])
        } else {
            return StrUtil.toString(jsonObject.getByPath(key))
        }
    }
}