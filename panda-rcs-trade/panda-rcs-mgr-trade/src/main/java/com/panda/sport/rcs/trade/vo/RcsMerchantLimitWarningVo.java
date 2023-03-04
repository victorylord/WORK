package com.panda.sport.rcs.trade.vo;

import com.panda.sport.rcs.pojo.RcsMerchantLimitWarning;
import lombok.Data;

import java.util.List;

/**
 * @program: xindaima
 * @description:
 * @author: kimi
 * @create: 2021-03-06 17:50
 **/
@Data
public class RcsMerchantLimitWarningVo {
    private List<RcsMerchantLimitWarning> rcsMerchantLimitWarningList;
    private Long total;
    private Integer currentDayCount = 0;
    private Integer pageNum;
    private Integer pageSize;
    private Long pages;
}
