package cn.lifay.lerverhc.hander.convert

import cn.hutool.json.JSONArray
import cn.hutool.json.JSONObject
import cn.hutool.json.JSONUtil

/**
 *@ClassName JsonConvert
 *@Description TODO
 *@Author lifay
 *@Date 2022/8/15 11:03
 **/
class JsonConvert(sourceStr:String,ruleStr: String) : IConvert(sourceStr,ruleStr) {

    override fun convert(): String {
        val datas = JSONArray()
        //获取变量
        val vars = getVars(ruleStr)
        val varsSize = vars.size
        //校验数量
        val array = JSONUtil.parseArray(sourceStr)
        for (i in array.indices) {
            val obj = array[i] as JSONObject
            if (varsSize != obj.size) {
                throw Exception("数量不一致:规则 [$varsSize] 条,第${i+1}行数据 [${obj.size}] 条")
            }
            //合并
            var tempStr = ruleStr
            for (v in vars) {
                if (!obj.containsKey(v)) {
                    throw Exception("缺失属性值:第${i+1}行数据 属性名:[$v]")
                }
                tempStr = tempStr.replace("${'$'}{${v}}",obj.getStr(v))
            }
            datas.add(JSONUtil.parseObj(tempStr))
        }
        return JSONUtil.formatJsonStr(JSONUtil.toJsonStr(datas))
    }

}