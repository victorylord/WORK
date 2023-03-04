package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * 商户动态风控全局开关配置
 *
 * @description:
 * @author: magic
 * @create: 2022-05-15 11:15
 **/
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class RcsMerchantCommonConfig extends RcsBaseEntity<RcsMerchantCommonConfig> {
    /**
     * 表ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 赔率分组动态风控开关 0关 1开
     */
    private Integer tagMarketLevelStatus;

    /**
     * 提前结算动态风控开关 0关 1开
     */
    private Integer preSettlementStatus;

    /**
     * 投注延时动态风控开关 0关 1开
     */
    private Integer betDelayStatus;

    /**
     * 投注限额动态风控开关 0关 1开
     */
    private Integer betLimitStatus;

    /**
     * 投注货量动态风控开关 0关 1开
     */
    private Integer betVolumeStatus;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 赛事种类
     */
    private String sportIds;

    @TableField(exist = false)
    private List<String> sportIdList;
}
