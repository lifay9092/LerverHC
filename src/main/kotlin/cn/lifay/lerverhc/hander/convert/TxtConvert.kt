package cn.lifay.lerverhc.hander.convert

import cn.hutool.json.JSONArray
import cn.hutool.json.JSONUtil

/**
 *@ClassName TxtConvert
 *@Description TODO
 *@Author lifay
 *@Date 2022/8/15 11:03
 **/
class TxtConvert(sourceStr: String, ruleStr: String) : IConvert(sourceStr, ruleStr) {

    override fun convert(): String {
        val datas = JSONArray()
        val sb = StringBuffer()
        val isOutJson = JSONUtil.isJson(ruleStr)
        //获取变量
        val vars = getVars(ruleStr)
        val varsSize = vars.size
        //校验数量
        val split = sourceStr.split("\n")
        for (i in split.indices) {
            val line = split[i]
            if (line.isBlank()) {
                continue
            }
            //判断规则串是txt还是json
            if (isOutJson) {
                val strings = line.trim().replace("\t", " ").split(" ")
                if (varsSize != strings.size) {
                    throw Exception("第${i + 1}行数据,规则数量不一致:规则-${varsSize}条, 文本-${strings.size}条")
                }
                //合并
                var tempStr = ruleStr
                for (v in vars) {
                    tempStr = tempStr.replace("${'$'}{${v}}", strings[v.toInt() - 1])
                }
                datas.add(JSONUtil.parseObj(tempStr))
            } else {
                val strings = line.trim().replace("\t", " ").split(" ")
                if (varsSize != strings.size) {
                    throw Exception("第${i + 1}行数据[${line}],规则数量不一致:规则-${varsSize}条, 文本-${strings.size}条")
                }
                //合并
                var tempStr = ruleStr
                for (v in vars) {
                    tempStr = tempStr.replace("${'$'}{${v}}", strings[v.toInt() - 1])
                }
                sb.append(tempStr).append("\n")
            }
        }
        if (isOutJson) {
            return JSONUtil.formatJsonStr(JSONUtil.toJsonStr(datas))
        } else {
            return sb.toString()
        }
    }


}