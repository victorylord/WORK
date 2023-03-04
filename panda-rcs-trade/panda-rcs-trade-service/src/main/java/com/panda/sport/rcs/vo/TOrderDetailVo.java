package com.panda.sport.rcs.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @program: xindaima
 * @description:
 * @author: kimi
 * @create: 2020-11-26 17:02
 **/
@Data
public class TOrderDetailVo {
    private String orderNo;
    private String orderStatus;
    private Integer infoStatus;
    private BigDecimal oddsValue;
    private String betNo;
    private Long modifyTime;
    private String reason;
}
