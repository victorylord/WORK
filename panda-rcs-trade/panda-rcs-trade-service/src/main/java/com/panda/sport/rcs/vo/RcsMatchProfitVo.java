package com.panda.sport.rcs.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @program: xindaima
 * @description:
 * @author: kimi
 * @create: 2020-12-05 14:23
 **/
@Data
public class RcsMatchProfitVo {
    private Long playId;
    private Long marketId;
    private BigDecimal profitAmount;
    private Integer matchType;
}
