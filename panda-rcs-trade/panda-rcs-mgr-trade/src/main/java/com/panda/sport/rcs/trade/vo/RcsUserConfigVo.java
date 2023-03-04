package com.panda.sport.rcs.trade.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @program: xindaima
 * @description:
 * @author: kimi
 * @create: 2020-12-08 18:34
 **/
@Data
public class RcsUserConfigVo implements Serializable {
    /**
     * 用户Id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    /**
     * 体育种类Id
     */
    private List<Long> sportIdList;
    /**
     * 投注额外延时
     */
    private Integer betExtraDelay;
    /**
     *   0 无  1标签限额  2特殊百分比限额 3特殊单注单场限额 4特殊vip限额
     */
    private Integer specialBettingLimit;
    /**
     * 备注
     */
    private String remarks;
    /**
     * 时间
     */
    private String updateTime;
    /**
     * 特殊货量
     */
    private Integer  specialVolume;
    /**
     * 是否提前结算  1是  其他否
     */
    private Integer settlementInAdvance;
    /**
     * 标签行情等级ID（赔率分组）
     */
    private String tagMarketLevelId;

    /**
     * 冠军玩法限额比例
     */
    private BigDecimal championLimitRate;
}
