package com.panda.sport.rcs.trade.vo.userprofile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * <p>
 * 标签-规则关系表
 * </p>
 *
 * @author lithan auto
 * @since 2020-06-25
 */
@ApiModel(value="UserProfileTagsGroupRuleRelationResVo Vo对象", description="标签-规则关系表VO")
public class UserProfileTagsGroupRuleRelationResVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "标签id")
    private Integer tagId;

    @ApiModelProperty(value = "规则id")
    private Integer ruleId;

    @ApiModelProperty(value = "规则名称")
    private String ruleName;

    @ApiModelProperty(value = "规则说明")
    private String ruleDetail;

    @ApiModelProperty(value = "规则code")
    @JsonIgnore
    private String ruleCode;

    @ApiModelProperty(value = "规则参数1")
    private String parameter1;

    @ApiModelProperty(value = "规则参数2")
    private String parameter2;

    @ApiModelProperty(value = "规则参数3")
    private String parameter3;

    @ApiModelProperty(value = "规则参数4")
    private String parameter4;

    @ApiModelProperty(value = "规则参数5")
    private String parameter5;

    @ApiModelProperty(value = "规则参数6")
    private String parameter6;

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public String getParameter5() {
        return parameter5;
    }

    public void setParameter5(String parameter5) {
        this.parameter5 = parameter5;
    }

    public String getParameter6() {
        return parameter6;
    }

    public void setParameter6(String parameter6) {
        this.parameter6 = parameter6;
    }

    public Integer getTagId() {
        return tagId;
    }

    public void setTagId(Integer tagId) {
        this.tagId = tagId;
    }

    public Integer getRuleId() {
        return ruleId;
    }

    public void setRuleId(Integer ruleId) {
        this.ruleId = ruleId;
    }

    public String getParameter1() {
        return parameter1;
    }

    public void setParameter1(String parameter1) {
        this.parameter1 = parameter1;
    }

    public String getParameter2() {
        return parameter2;
    }

    public void setParameter2(String parameter2) {
        this.parameter2 = parameter2;
    }

    public String getParameter3() {
        return parameter3;
    }

    public void setParameter3(String parameter3) {
        this.parameter3 = parameter3;
    }

    public String getParameter4() {
        return parameter4;
    }

    public void setParameter4(String parameter4) {
        this.parameter4 = parameter4;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRuleDetail() {
        return ruleDetail;
    }

    public void setRuleDetail(String ruleDetail) {
        this.ruleDetail = ruleDetail;
    }
}
