package com.panda.sport.rcs.mongo;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * @author :  enzo
 * @Project Name :  panda-rcs-trade-group
 * @Package Name :  com.panda.sport.rcs.mongo
 * @Description :  前十五分钟盘口配置
 */
@Data
@Document(collection = "rcs_match_market_config")
public class MarketConfigMongo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Field(value = "id")
    private Long id;
    /**
     * 盘口新id
     */
    private String newId;

    /**
     * 联赛id
     */
    private Long tournamentId;

    /**
     * 赛事id
     */
    private Long matchId;

    /**
     * 玩法id
     */
    private Long playId;

    /**
     * 盘口id
     */
    private String marketId;

    private Long homeMarketId;
    /**
     * 更新赔率时的对应赔率值
     */
    private String updateOdds;

    /**
     * 主队盘口值
     * 主队盘口值
     */
    private BigDecimal homeMarketValue;

    /**
     * 客队盘口值
     */
    private BigDecimal awayMarketValue;

    /**
     * margin值
     */
    private BigDecimal margin;

    /**
     * 上盘单枪限额
     */
    private Long homeLevelFirstMaxAmount;

    /**
     * 上盘单枪赔率变化率
     */
    private BigDecimal homeLevelFirstOddsRate;

    /**
     * 上盘累计限额
     */
    private Long homeLevelSecondMaxAmount;

    /**
     * 上盘累计赔率变化率
     */
    private BigDecimal homeLevelSecondOddsRate;

    /**
     * 最大单注限额/派奖
     */
    private Long maxSingleBetAmount;
    /**
     * 最大可投金额，根据联赛配置获取
     */
    private BigDecimal maxBetAmount;

    /**
     * 最大赔率
     */
    private BigDecimal maxOdds;

    /**
     * 最小赔率
     */
    private BigDecimal minOdds;

    /**
     * 是否使用数据源0：手动；1：使用数据源
     */

    private Long dataSource;

    /**
     * 创建时间
     */
    private Timestamp createTime;

    /**
     * 创建人
     */
    private String createUser;

    /**
     * 修改时间
     */
    private Timestamp modifyTime;

    /**
     * 修改人
     */
    private String modifyUser;

    /**
     * 下盘单枪限额
     */
    private Long awayLevelFirstMaxAmount;

    /**
     * 下盘累计限额
     */
    private Long awayLevelSecondMaxAmount;

    /**
     * 下盘单枪赔率变化率
     */
    private BigDecimal awayLevelFirstOddsRate;

    /**
     * 下盘累计赔率变化率
     */
    private BigDecimal awayLevelSecondOddsRate;

    /**
     * 盘口状态
     */
    private Integer marketStatus;

    /**
     * 平衡值计算规则
     * 0 投注额 1 投注额/赔付值组合
     */
    private Integer balanceOption;
    /**
     * 跳赔规则
     * 0 累计/单枪 1 差额累计
     */
    private Integer oddChangeRule;
    /**
     * 盘口是否改变
     */
    private Integer isMarketChange;

    /**
     * 盘口是否存在
     */
    private Integer isExistsMarket;
    /**
     * 平衡值
     */
    private Long balance;
    /**
     * @Description //赔率数据
     * @Param
     * @return
     **/
    private List<Map<String, Object>> oddsList;

    /**
     * 盘口状态 前端展示用
     */
    private Boolean marketActive;

    /**
     * 操盘类型 EU 欧盘 MY 马来盘
     */
    private String marketType;

    /**
     * @Description 主队水差
     * @Param
     * @Author Sean
     * @Date 15:36 2020/1/11
     * @return
     **/
    private String homeAutoChangeRate;
    /**
     * @Description 客队水差
     * @Param
     * @Author Sean
     * @Date 15:36 2020/1/11
     * @return
     **/
    private String awayAutoChangeRate;
    /**
     * @Description 和局水差
     * @Param
     * @Author Sean
     * @Date 15:36 2020/1/11
     * @return
     **/
    private String tieAutoChangeRate;

    /**
     * 0 否 1 是 自动封盘
     */
    private String autoBetStop;

    /**
     * 欧赔赔率差值
     */
    private Integer diffOdds;

    /**
     * 赔率变化累计值
     */
    private BigDecimal oddsChange;
    /**
     * 主队投注额
     */
    private Long homeAmount;
    /**
     * 客队投注额
     */
    private Long awayAmount;
    /**
     * 和投注额
     */
    private Long tieAmount;
    /**
     * 盘口配置位置
     */
    private Integer marketIndex;
    /**
     * 主胜margin
     */
    private BigDecimal homeMargin;
    /**
     * 客胜margin
     */
    private BigDecimal awayMargin;
    /**
     * 平局margin
     */
    private BigDecimal tieMargin;
    /**
     * 类型：1 ：早盘 ，2： 滚球盘， 3： 冠军盘
     */
    private Integer matchType;

    private String updateTime;
}
