package com.panda.sport.rcs.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author derre
 * @date 2022-03-28
 */
@Data
public class RiskMerchantManagerUpdateVo {

    @NotNull(message = "id不能为空")
    private Long id;


    /**
     * 商户处理人
     */
    @NotBlank(message = "商户处理人不能为空")
    private String merchantOperator;


    /**
     * 状态:0待处理,1同意,2拒绝,3强制执行
     */
    @NotNull(message = "状态不能为空")
    private Integer status;


    /**
     * 商户处理说明
     */
    private String merchantRemark;




}
