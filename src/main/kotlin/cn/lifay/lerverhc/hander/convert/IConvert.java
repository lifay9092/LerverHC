package cn.lifay.lerverhc.hander.convert;

import cn.hutool.core.util.ReUtil;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *@ClassName IConvert
 *@Description TODO
 *@Author lifay
 *@Date 2022/8/15 10:56
 **/
public abstract class IConvert {

    protected String sourceStr;

    protected String ruleStr;

    public IConvert(String sourceStr, String ruleStr) {
        this.sourceStr = sourceStr;
        this.ruleStr = ruleStr;
    }

    abstract public String convert();

    public String getRuleStr() {
        return ruleStr;
    }

    public void setRuleStr(String ruleStr) {
        this.ruleStr = ruleStr;
    }

    public String getSourceStr() {
        return sourceStr;
    }

    public void setSourceStr(String sourceStr) {
        this.sourceStr = sourceStr;
    }


    /**
     * 获取模板变量
     *
     * @param str
     * @return
     */
    public List<String> getVars(String str) {
        return ReUtil.findAllGroup1(Pattern.compile("\\$\\{([A-Za-z0-9]+)\\}"), str).stream().distinct()
                .collect(Collectors.toList());
    }

}