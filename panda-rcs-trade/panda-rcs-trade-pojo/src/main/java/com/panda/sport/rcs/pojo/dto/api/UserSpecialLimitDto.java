package com.panda.sport.rcs.pojo.dto.api;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 用户特殊限额
 * @Author : Paca
 * @Date : 2021-08-18 13:00
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
@ApiModel(value = "修改用户特殊限额传输类")
public class UserSpecialLimitDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("用户ID")
    private Long userId;
    
    @ApiModelProperty("体育种类Id")
    private List<Long> sportIdList;

    @ApiModelProperty("特殊限额类型，1-无，2-特殊百分比限额")
    private Integer specialLimitType;
    
    @ApiModelProperty("标签行情等级ID（赔率分组）")
    private String tagMarketLevelId;

    @ApiModelProperty("特殊限额百分比")
    private BigDecimal percentage;
    
    @ApiModelProperty("投注额外延时")
    private Integer betExtraDelay;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("操作人ID")
    private String operatorId;

    @ApiModelProperty("操作人名称")
    private String operatorName;
}