package com.panda.sport.rcs.trade.vo.userprofile;

/**
 *  真实值对象
 *
 * @author lithan
 * @date 2021-10-20 18:00:13
 */
public class RuleResultDataVo {
    //规则对象
    public UserProfileTagsRuleRelationResVo rule;
    //结果
    public String result;

    public UserProfileTagsRuleRelationResVo getRule() {
        return rule;
    }

    public void setRule(UserProfileTagsRuleRelationResVo rule) {
        this.rule = rule;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}

