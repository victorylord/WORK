package com.panda.sport.rcs.trade.vo.userprofile;

/**
 * 规则返回值
 *
 * @author lithan
 * @date 2020-06-28 09:58:26
 */
public class RuleResult<T> {

    //用户ID
    Long userId;

    //规则标识ID 非主键ID
    String ruleCode;

    //是否满足规则
    Boolean flag;

    //返回实际值
    T data;

    public static <T> RuleResult<T> init(Long userId, String ruleCode, Boolean flag, T data) {
        RuleResult<T> ruleResult = new RuleResult<>();
        ruleResult.setUserId(userId);
        ruleResult.setRuleCode(ruleCode);
        ruleResult.setFlag(flag);
        ruleResult.setData(data);
        return ruleResult;
    }

    public static <T> RuleResult<T> init(Boolean flag, T data) {
        RuleResult<T> ruleResult = new RuleResult<>();
        ruleResult.setFlag(flag);
        ruleResult.setData(data);
        return ruleResult;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public Boolean getFlag() {
        return flag;
    }

    public void setFlag(Boolean flag) {
        this.flag = flag;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
