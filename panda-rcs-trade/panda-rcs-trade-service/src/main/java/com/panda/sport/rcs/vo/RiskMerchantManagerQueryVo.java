package com.panda.sport.rcs.vo;

import lombok.Data;

/**
 * @author derre
 * @date 2022-03-28
 */
@Data
public class RiskMerchantManagerQueryVo extends PageQuery {

    /**
     * 商户编号
     */
    private String merchantCode;

    /**
     * 用户名/用户id
     */
    private String userName;

    /**
     * 风控类型,1.投注特征标签,2特殊限额,3特殊延时,4提前结算,5赔率分组
     */
    private Integer type;


    /**
     * 商户处理人
     */
    private String merchantOperator;

    /**
     * 建议人
     */
    private String riskOperator;

    /**
     * 风控建议时间--开始时间
     */
    private String recommendStartTime;

    /**
     * 风控建议时间--结束时间
     */
    private String recommendEndTime;


    /**
     * 商户处理时间--开始时间
     */
    private String processStartTime;

    /**
     * 商户处理时间--结束时间
     */
    private String processEndTime;

    /**
     * 状态:0待处理,1同意,2拒绝,3强制执行
     */
    private Integer status;

    /**
     * 排序列：1-平台建议时间 2-处理时间
     */
    private Integer orderKey;

    /**
     * 排序方式：desc-降序 asc-升序
     */
    private String orderType;

}
