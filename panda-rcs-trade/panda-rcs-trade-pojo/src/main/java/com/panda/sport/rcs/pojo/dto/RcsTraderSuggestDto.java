package com.panda.sport.rcs.pojo.dto;

import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 		操盘手贴标提醒风控
 * </p>
 *
 * @author ${author}
 * @since 2022-04-08
 */
@Data
@ApiModel(value = "操盘手贴标提醒风控传输类")
public class RcsTraderSuggestDto implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * 主键
     */
    @ApiModelProperty("主键")
    private Integer id;

    /**
     * 用户id
     */
    @ApiModelProperty("用户id")
    private String userId;

    /**
     * 用户名
     */
    @ApiModelProperty("用户名")
    private String userName;
    
    /**
     * 商户id
     */
    @ApiModelProperty("商户id")
    private Long businessId;

    /**
     * 商户名称
     */
    @ApiModelProperty("商户名称")
    private String businessName;

    /**
     * 操盘手建议
     */
    @ApiModelProperty("操盘手建议")
    private String traderSuggest;

    /**
     * 操盘手建议补充说明
     */
    @ApiModelProperty("操盘手建议补充说明")
    private String traderSuggestReplenish;

    /**
     * 建议时间,新建时间
     */
    @ApiModelProperty("建议时间,新建时间")
    private Date createTime;

    
    /**
     * 查询时间开始时间戳
     */
    @ApiModelProperty("查询时间开始时间戳")
    private String startTime;
    
    /**
     * 查询时间结束时间戳
     */
    @ApiModelProperty("查询时间结束时间戳")
    private String endTime;
    
    /**
     * 操盘建议人账户名
     */
    @ApiModelProperty("操盘建议人账户名")
    private String proposer;
    
    @ApiModelProperty("风控处理类型：0：其他、1:殊限额、2:赔率分组、3：提前结算、4：投注延时、5：投注特征标签")
    private Integer riskProcesType;

    /**
     * 风控处理状态：0:未处理、1:已设置、2:忽略
     */
    @ApiModelProperty("风控处理状态：0:未处理、1:已设置、2:忽略")
    private Integer riskProcesStatus;

    /**
     * 风控处理人
     */
    @ApiModelProperty("风控处理人")
    private String riskHandler;

    /**
     * 风控处理时间
     */
    @ApiModelProperty("风控处理时间")
    private Date riskProcesTime;

    /**
     * 风控处理说明
     */
    @ApiModelProperty("风控处理说明")
    private String riskRemark;
    
    @ApiModelProperty("當前頁數")
    private Integer pageNum = 1;

    @ApiModelProperty("条数")
    private Integer pageSize = 10;


}
