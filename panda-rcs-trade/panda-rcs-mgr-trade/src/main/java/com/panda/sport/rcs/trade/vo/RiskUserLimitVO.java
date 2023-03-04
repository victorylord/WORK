package com.panda.sport.rcs.trade.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;


/**
 * @author javier
 * 用户特别额度限制实体
 *  2020-02-03 18:34
 **/
@Data
public class RiskUserLimitVO {
    /**
     * 用户Id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    /**
     * 最新时间
     */
    private String lastUpdateTime;
}
