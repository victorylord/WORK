package com.panda.sport.rcs.trade.vo.userprofile;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 用户画像标签管理表 扩展
 * </p>
 *
 * @author lithan auto
 * @since 2020-07-02 09:27:54
 */
public class UserProfileTagsExtVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "标签类型  1基本属性类 2投注特征类 3访问特征类 4财务特征类")
    private Integer tagType;

    @ApiModelProperty(value = "标签名称")
    private String tagName;

    @ApiModelProperty(value = "标签说明")
    private String tagDetail;

    @ApiModelProperty(value = "标签复核天数")
    private Integer tagRecheckDays;

    @ApiModelProperty(value = "是否循环复核（0.否 1.是）默认为0否")
    private Integer isRecheck;

    @ApiModelProperty(value = "是否停止计算（0.否 1.是）默认为0否")
    private Integer isCalculate;

    @ApiModelProperty(value = "是否自动化（0.否 1.是）默认为0否")
    private Integer isAuto;

    @ApiModelProperty(value = "是否默认标签（0.否 1.是）默认为0否")
    private Integer isDefault;

    @ApiModelProperty(value = "是否不达标回退至上级标签（0.否 1.是）默认为0否")
    private Integer isRollback;

    @ApiModelProperty(value = "上级标签")
    private Long fatherId;

    @ApiModelProperty(value = "允许风控措施 0否 1是")
    private Integer riskStatus;

    //标签包含的单一规则对象
    private List<UserProfileTagsRuleRelationResVo> relationVoList;

    //标签包含的组合规则对象
    private List<List<UserProfileTagsGroupRuleRelationResVo>> groupRuleRelationResVos;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getTagType() {
        return tagType;
    }

    public void setTagType(Integer tagType) {
        this.tagType = tagType;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getTagDetail() {
        return tagDetail;
    }

    public void setTagDetail(String tagDetail) {
        this.tagDetail = tagDetail;
    }

    public List<UserProfileTagsRuleRelationResVo> getRelationVoList() {
        return relationVoList;
    }

    public void setRelationVoList(List<UserProfileTagsRuleRelationResVo> relationVoList) {
        this.relationVoList = relationVoList;
    }
    public List<List<UserProfileTagsGroupRuleRelationResVo>> getGroupRuleRelationResVos() {
        return groupRuleRelationResVos;
    }

    public void setGroupRuleRelationResVos(List<List<UserProfileTagsGroupRuleRelationResVo>> groupRuleRelationResVos) {
        this.groupRuleRelationResVos = groupRuleRelationResVos;
    }

    public Integer getTagRecheckDays() {
        return tagRecheckDays;
    }

    public void setTagRecheckDays(Integer tagRecheckDays) {
        this.tagRecheckDays = tagRecheckDays;
    }

    public Integer getIsRecheck() {
        return isRecheck;
    }

    public void setIsRecheck(Integer isRecheck) {
        this.isRecheck = isRecheck;
    }

    public Integer getIsCalculate() {
        return isCalculate;
    }

    public void setIsCalculate(Integer isCalculate) {
        this.isCalculate = isCalculate;
    }

    public Integer getIsAuto() {
        return isAuto;
    }

    public void setIsAuto(Integer isAuto) {
        this.isAuto = isAuto;
    }

    public Integer getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Integer isDefault) {
        this.isDefault = isDefault;
    }

    public Integer getIsRollback() {
        return isRollback;
    }

    public void setIsRollback(Integer isRollback) {
        this.isRollback = isRollback;
    }

    public Long getFatherId() {
        return fatherId;
    }

    public void setFatherId(Long fatherId) {
        this.fatherId = fatherId;
    }

    public Integer getRiskStatus() {
        return riskStatus;
    }

    public void setRiskStatus(Integer riskStatus) {
        this.riskStatus = riskStatus;
    }
}
