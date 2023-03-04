package com.panda.sport.rcs.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author :  enzo
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.vo
 * @Description :  TODO
 * @Date: 2020-01-15 11:56
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
@Accessors(chain = true)
public class MatchMarketTradeTypeVo implements Serializable {

    /**
     * 默认
     */
    public MatchMarketTradeTypeVo(){

    }

    /**
     * 用于赛事变更
     * @param sportId
     * @param matchId
     * @param level
     * @param cashoutStatus 1
     */
    public MatchMarketTradeTypeVo(Long sportId, Long matchId, Integer level, Integer cashoutStatus) {
        this.sportId = sportId;
        this.matchId = matchId;
        this.level = level;
        this.cashoutStatus = cashoutStatus;
    }

    /**
     * 用于批量玩法变更
     * @param sportId
     * @param matchId
     * @param level
     * @param playCashout 10
     */
    public MatchMarketTradeTypeVo(Long sportId, Long matchId, Integer level, List<MatchMarketTradeTypeVo> playCashout) {
        this.sportId = sportId;
        this.matchId = matchId;
        this.level = level;
        this.playCashout = playCashout;
    }

    /**
     * 单个玩法构造
     * @param categoryId
     * @param cashoutStatus
     * @param cashoutValue
     */
    public MatchMarketTradeTypeVo(Long categoryId, Integer cashoutStatus, BigDecimal cashoutValue) {
        this.categoryId = categoryId;
        this.cashoutStatus = cashoutStatus;
        this.cashoutValue = cashoutValue;
    }

    private static final long serialVersionUID = 1L;

    private String linkId;

    /**
     * 运动种类ID
     */
    private Long sportId;

    /**
     * 标准赛事的id. 对应 standard_match_info.id
     */
    private Long matchId;

    /**
     * 玩法集ID
     */
    private Long categorySetId;

    /**
     * 玩法ID
     */
    private Long categoryId;
    /**
     * 子玩法ID
     */
    @TableField(exist = false)
    private Long subPlayId;

    /**
     * 玩法ID集合
     */
    private List<Long> playIds;

    /**
     * 操盘类型，0-自动操盘，1-手动操盘
     *
     * @see com.panda.sport.rcs.enums.TradeEnum
     */
    private Integer tradeType;

    /**
     * 操盘级别，1-赛事级别，2-玩法级别，3-盘口级别，4-玩法集
     *
     * @see com.panda.sport.rcs.enums.TradeLevelEnum
     */
    private Integer level;

    /**
     * 状态
     *
     * @see com.panda.sport.rcs.enums.TradeStatusEnum
     */
    private Integer status;

    /**
     * 新增盘口标志，0-普通切换，1-新增盘口切换
     */
    private Integer newFlag;

    /**
     * 水差是否关联，0-不关联，1-关联
     */
    private Integer relevanceType;

    /**
     * 自动操盘玩法数量
     */
    private Integer autoCount;
    /**
     * 手动操盘玩法数量
     */
    private Integer manualCount;

    /**
     * 自动+ 操盘玩法数量
     */
    private Integer autoAddCount;

    /**
     * 占位符玩法总开关封锁
     * key=playId,value=playStatus
     */
    private Map<Long, Integer> mainPlayStatusMap;

    /**
     * 结算状态
     */
    private Integer cashoutStatus;

    /**
     * 结算状态对应的值
     */
    private BigDecimal cashoutValue;

    /**
     * 玩法集合状态
     */
    private List<MatchMarketTradeTypeVo> playCashout;
}
