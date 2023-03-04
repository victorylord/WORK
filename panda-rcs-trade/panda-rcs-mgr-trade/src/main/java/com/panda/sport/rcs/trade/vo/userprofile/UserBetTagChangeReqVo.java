package com.panda.sport.rcs.trade.vo.userprofile;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * <p>
 * 标签变更记录 reqVO
 * </p>
 *
 * @author Kir
 * @since 2021-03-11
 */
@ApiModel(value="UserBetTagChangeReqVo reqVO", description="标签变更记录reqVO")
public class UserBetTagChangeReqVo implements Serializable {


    @ApiModelProperty(value = "主键Id")
    private Long id;

    @ApiModelProperty(value = "用户Id")
    private String userId;

    @ApiModelProperty(value = "操作人")
    private String changeManner;

    @ApiModelProperty(value = "状态（0.未处理 1.接收 2.忽略）")
    private Integer status;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "手动修改后的标签id-针对于投注特征标签预警")
    private Long changeTag;

    @ApiModelProperty(value = "手动修改后的标签名称-针对于投注特征标签预警")
    private String changeTagName;

    @ApiModelProperty(value = "风控补充说明")
    private String supplementExplain;

    @ApiModelProperty(value = "提交类型  1 提交商户决策 2 强制执行 3审核通过")
    private Integer submitType;

    public String getSupplementExplain() {
        return supplementExplain;
    }

    public void setSupplementExplain(String supplementExplain) {
        this.supplementExplain = supplementExplain;
    }

    public Integer getSubmitType() {
        return submitType;
    }

    public void setSubmitType(Integer submitType) {
        this.submitType = submitType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getChangeManner() {
        return changeManner;
    }

    public void setChangeManner(String changeManner) {
        this.changeManner = changeManner;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Long getChangeTag() {
        return changeTag;
    }

    public void setChangeTag(Long changeTag) {
        this.changeTag = changeTag;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getChangeTagName() {
        return changeTagName;
    }

    public void setChangeTagName(String changeTagName) {
        this.changeTagName = changeTagName;
    }
}
