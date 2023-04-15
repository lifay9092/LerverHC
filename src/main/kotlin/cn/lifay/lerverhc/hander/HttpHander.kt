package cn.lifay.lerverhc.hander

import cn.hutool.core.io.FileUtil
import cn.hutool.core.lang.Validator
import cn.hutool.core.util.ReUtil
import cn.hutool.core.util.StrUtil
import cn.hutool.core.util.URLUtil
import cn.hutool.http.*
import cn.hutool.json.JSONObject
import cn.hutool.json.JSONUtil
import cn.lifay.lerverhc.hander.HttpHander.getVars
import cn.lifay.lerverhc.model.Header
import javafx.scene.control.Alert
import org.apache.commons.logging.LogFactory
import java.io.File
import java.io.Serializable
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
    val logger = LogFactory.getLog(HttpHander::class.java)

    fun checkDataFile(bodyStr: String?, batchDataFilePath: String): String {
        if (!FileUtil.exist(batchDataFilePath)) {
            Alert(Alert.AlertType.ERROR, "文件不存在:$batchDataFilePath").show()
            return ""
        }
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
    fun singleSendHttp(
        url: String?,
        method: Method,
        contentType: ContentType,
        headers: List<Header>,
        bodyStr: String
    ): HttpResponse? {
        //组装参数信息
        try {
            if (!checkUrl(url!!)) {
                throw RuntimeException(" url格式不正确")
            }
            val httpRequest = buildHttpRequest(url, method, contentType, headers)
            if (Method.POST == method && ContentType.JSON == contentType && bodyStr.isNotBlank()) {
                httpRequest.body(bodyStr)
            } else if (bodyStr.isNotBlank()) {
                httpRequest.form(parseToFile(bodyStr))
            }
            return httpRequest.execute()
        } catch (e: Exception) {
            throw RuntimeException("HTTP请求失败 [$url]:${e.message}")
//            Alert(Alert.AlertType.ERROR, "HTTP请求失败:${e.message}").show()
//            return null
        }
    }

    /**
     * 定义基础请求类
     */
    fun buildHttpRequest(url: String?, method: Method, contentType: ContentType, headers: List<Header>): HttpRequest {
        val httpRequest = HttpUtil.createRequest(method, url)
        httpRequest.contentType(contentType.value)
        for (header in headers) {
            httpRequest.header(header.key, header.value)
        }
        return httpRequest
    }

    private fun post(url: String?, bodyObjString: String?): String? {
        //是否有文件
        val bodyObj = JSONUtil.parseObj(bodyObjString)
        for (key in bodyObj!!.keys) {
            bodyObj[key]?.let {
                if (it is String && it.startsWith("@")) {
                    val newFilePath = it.substring(1)
                    if (!FileUtil.exist(newFilePath)) {
                        throw Exception("文件不存在:${newFilePath}")
                    }
                    bodyObj[key] = File(newFilePath)
                }
            }
        }
        //return HttpUtil.post(url, bodyObj)
//        val map = HashMap<String,Any>()
//        map["file"] = File("E:\\\\TEST\\\\swserver\\\\swserver.bat")
        return HttpUtil.post(url, bodyObj)
    }
    private fun parseToFile(bodyObjString: String?): Map<String,Any>? {
        if (bodyObjString == null) {
            return null
        }
        val tempBodyStr =  if (bodyObjString.contains("\\"))  bodyObjString.replace("\\","\\\\") else bodyObjString
        //是否有文件
        val bodyObj = JSONUtil.parseObj(tempBodyStr).toMutableMap()
        for (key in bodyObj!!.keys) {
            bodyObj[key]?.let {
                if (it is String && it.startsWith("@")) {
                    val newFilePath = it.substring(1)
                    if (!FileUtil.exist(newFilePath)) {
                        throw Exception("文件不存在:${newFilePath}")
                    }
                    bodyObj[key] = File(newFilePath)
                }
            }
        }
        return bodyObj
    }
    private fun checkUrl(url: String) :Boolean{
        return ReUtil.isMatch(Validator.URL_HTTP,url)
    }
    /**
     * 批量发送http请求并保存
     */
    fun batchSendHttp(
        url: String,
        method: Method,
        contentType: ContentType,
        headers: List<Header>,
        bodyStr: String?,
        batchDataFilePath: String,
        batchFileNameStr: String?
    ): String? {
        var count = 0
        /*解析模板和数据文件*/
        if (!FileUtil.exist(batchDataFilePath)) {
//                Alert(Alert.AlertType.ERROR, "$batchDataFilePath 不存在", ButtonType.CLOSE).show()
            throw RuntimeException("$batchDataFilePath 不存在")
        }
        val readStr = FileUtil.readString(batchDataFilePath, Charset.forName("utf-8"))
        if (!JSONUtil.isJsonArray(readStr)) {
//                Alert(Alert.AlertType.ERROR, "$batchDataFilePath 非json数组格式", ButtonType.CLOSE).show()
            throw RuntimeException("$batchDataFilePath 非json数组格式")
        }
        if (!checkUrl(url)) {
            throw RuntimeException("$url url格式不正确")
        }
        //输出目录
        val outputDir = ConfigUtil.preferences.get(
            ConfigUtil.PROPERTIES_OUTPUT_FOLDER,
            System.getProperty("user.dir")
        ) + File.separator
        //提取变量key ${}
        val bodyKeys = getVars(bodyStr)
        val fileNameKeys = getVars(batchFileNameStr)

        //遍历
        val jsonArray = JSONUtil.parseArray(readStr)
        for (index in jsonArray.indices) {
            val jsonObject: JSONObject = jsonArray[index] as JSONObject
            //替换模板变量
            val realBodyStr = getRealReplaceStr(bodyStr, bodyKeys, jsonObject)
            try {
                val httpResponsebody = singleSendHttp(url, method, contentType, headers, realBodyStr ?: "")
                //println(httpRequest!!.body(realBodyStr).execute().body())
                //输出文件名
                val realFileNameStr = getRealReplaceStr(batchFileNameStr, fileNameKeys, jsonObject)
                val outputFilePath = "$outputDir$realFileNameStr.json"
                FileUtil.writeString(httpResponsebody?.body(), outputFilePath, Charset.forName("utf-8"))
            } catch (e: Exception) {
                e.printStackTrace()
                throw RuntimeException("执行失败,index[${index}],msg:${e.message}")
            }
            count++
        }
        return "批量执行成功,总共 ${count} 条,输出目录:$outputDir"

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


    fun getContentTypeByValue(consumes: List<String>?): String {
        if (consumes == null) {
            return ContentType.FORM_URLENCODED.name
        }
        return ContentType.values().first { it.value == consumes[0] }.name
    }

    fun downFile(url: String, directory: File) {
        val response = HttpUtil.createGet(url, true)
            .timeout(-1)
            .executeAsync()
        if (response.isOk) {
            response.writeBodyForFile(directory, null)
        } else {
            throw HttpException("Server response error with status code: [${response.status}]")
        }
    }

}