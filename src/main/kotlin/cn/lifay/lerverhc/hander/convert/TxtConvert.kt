package cn.lifay.lerverhc.hander.convert

import cn.hutool.json.JSONArray
import cn.hutool.json.JSONUtil

/**
 *@ClassName TxtConvert
 *@Description TODO
 *@Author lifay
 *@Date 2022/8/15 11:03
 **/
class TxtConvert(sourceStr:String,ruleStr: String) : IConvert(sourceStr,ruleStr) {

    override fun convert(): String {
        val datas = JSONArray()
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
            val strings = line.trim().split(" ")
            if (varsSize != strings.size) {
                throw Exception("数量不一致:规则 [$varsSize] 条,第${i+1}行数据 [${strings.size}] 条")
            }
            //合并
            var tempStr = ruleStr
            for (v in vars) {
                tempStr = tempStr.replace("${'$'}{${v}}",strings[v.toInt() - 1])
            }
            datas.add(JSONUtil.parseObj(tempStr))
        }
        return JSONUtil.formatJsonStr(JSONUtil.toJsonStr(datas))
    }


}