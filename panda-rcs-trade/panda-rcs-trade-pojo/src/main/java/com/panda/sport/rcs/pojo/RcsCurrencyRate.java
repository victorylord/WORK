package com.panda.sport.rcs.pojo;

import java.math.BigDecimal;
import lombok.Data;

/**
 * @ClassName RcsCurrencyRate
 * @Description: TODO 
 * @Author Vector
 * @Date 2019/12/20 
**/
@Data
public class RcsCurrencyRate {
    private Long id;

    /**
    * 货币编码
    */
    private String currencyCode;

    /**
    * 汇率
    */
    private BigDecimal rate;

    private Long createTime;

    private Long modifyTime;
}