package com.panda.sport.rcs.trade.vo.userprofile;

import java.io.Serializable;
import java.util.List;

/**
 * 新增标签 vo对象
 */
public class AddUserTagVo implements Serializable {

    public AddUserTagVo(Long userId, UserProfileTagsExtVo tag, List<RuleResult<List<RuleResultDataVo>>> ruleResultList, String changeManner, String remark, Integer changeType) {
        this.userId = userId;
        this.tag = tag;
        this.ruleResultList = ruleResultList;
        this.changeManner = changeManner;
        this.remark = remark;
        this.changeType = changeType;
    }

    //用户id
    Long userId;
    //标签对象
    UserProfileTagsExtVo tag;
    //标签对应用户  对用规则结果
    List<RuleResult<List<RuleResultDataVo>>> ruleResultList;
    //操作人
    String changeManner;
    //备注
    String remark;
    //1自动  2非自动
    Integer changeType;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public UserProfileTagsExtVo getTag() {
        return tag;
    }

    public void setTag(UserProfileTagsExtVo tag) {
        this.tag = tag;
    }

    public List<RuleResult<List<RuleResultDataVo>>> getRuleResultList() {
        return ruleResultList;
    }

    public void setRuleResultList(List<RuleResult<List<RuleResultDataVo>>> ruleResultList) {
        this.ruleResultList = ruleResultList;
    }

    public String getChangeManner() {
        return changeManner;
    }

    public void setChangeManner(String changeManner) {
        this.changeManner = changeManner;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getChangeType() {
        return changeType;
    }

    public void setChangeType(Integer changeType) {
        this.changeType = changeType;
    }
}
