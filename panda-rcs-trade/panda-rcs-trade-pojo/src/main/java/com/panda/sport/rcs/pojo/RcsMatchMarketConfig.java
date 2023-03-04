package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.panda.sport.rcs.log.annotion.format.LogFormatAnnotion;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargain;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 盘口设置表
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-10-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@LogFormatAnnotion
public class RcsMatchMarketConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 赛事id
     */
    @LogFormatAnnotion(name = "赛事ID" )
    private Long matchId;
    /**
     * 玩法id
     */
    @LogFormatAnnotion(name = "玩法ID" )
    private Long playId;
    /**
     * 接单等待时间
     */
    private Integer waitSeconds;
    /**
     * 暂停接单等待时间
     */
    private Integer timeOutWaitSeconds;

    /**
     * 盘口id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @TableField(exist = false)
    @LogFormatAnnotion(name = "盘口M" )
    private Long marketId;
    /**
     * 主队盘口值
     */
    @TableField(exist = false)
    private BigDecimal homeMarketValue;
    /**
     * 客队盘口值
     */
    @TableField(exist = false)
    private BigDecimal awayMarketValue;
    /**
     * margin值
     */
    @LogFormatAnnotion(name = "抽水" )
    private BigDecimal margin;
    /**
     * 暂停margin值
     */
    private BigDecimal timeOutMargin;

    /**
     * 上盘一级限额
     */
    @LogFormatAnnotion(name = "一级限额" )
    private Long homeLevelFirstMaxAmount;
    /**
     * 上盘二级限额
     */
    @LogFormatAnnotion(name = "二级限额" )
    private Long homeLevelSecondMaxAmount;

    /**
     * 上盘一级赔率变化率
     */
    @LogFormatAnnotion(name = "累计上盘变化（一级）" )
    private BigDecimal homeLevelFirstOddsRate;

    /**
     * 上盘二级赔率变化率
     */
    @LogFormatAnnotion(name = "累计上盘变化（二级）" )
    private BigDecimal homeLevelSecondOddsRate;

    /**
     * 下盘一级赔率变化率
     */
    @LogFormatAnnotion(name = "累计下盘变化（一级）" )
    private BigDecimal awayLevelFirstOddsRate;

    /**
     * 下盘二级赔率变化率
     */
    @LogFormatAnnotion(name = "累计下盘变化（二级）" )
    private BigDecimal awayLevelSecondOddsRate;

    /**
     * 上盘单枪限额
     */
    @LogFormatAnnotion(name = "单枪限额" )
    private Long homeSingleMaxAmount;
    /**
     * 上盘累计限额
     */
    @LogFormatAnnotion(name = "累计限额" )
    private Long homeMultiMaxAmount;

    /**
     * 上盘单枪赔率变化率
     */
    @LogFormatAnnotion(name = "累计上盘变化（单枪跳分）" )
    private BigDecimal homeSingleOddsRate;

    /**
     * 上盘累计赔率变化率
     */
    @LogFormatAnnotion(name = "累计上盘变化（累计跳分）" )
    private BigDecimal homeMultiOddsRate;

    /**
     * 下盘单枪赔率变化率
     */
    @LogFormatAnnotion(name = "累计下盘变化（单枪跳分）" )
    private BigDecimal awaySingleOddsRate;

    /**
     * 下盘累计赔率变化率
     */
    @LogFormatAnnotion(name = "累计下盘变化（累计跳分）" )
    private BigDecimal awayMultiOddsRate;

    /**
     * 最大单注限额/派奖
     */
    @LogFormatAnnotion(name = "最大投注最大赔付" )
    private Long maxSingleBetAmount;
    /**
     * 最大可投金额，根据联赛配置获取
     */
    @TableField(exist = false)
    private BigDecimal maxBetAmount;

    /**
     * 最大赔率
     */
    @LogFormatAnnotion(name = "最大欧赔/最大马来赔" )
    private BigDecimal maxOdds;

    /**
     * 最小赔率
     */
    @LogFormatAnnotion(name = "最小欧赔/最小马来赔" )
    private BigDecimal minOdds;

    /**
     * 是否使用数据源0：手动；1：使用数据源,2:A+
     */
    @TableField(exist = false)
    private Long dataSource;

    /**
     * 创建时间
     */
    @TableField(exist = false)
    private Timestamp createTime;

    /**
     * 创建人
     */
    @LogFormatAnnotion(name = "操作者账号" )
    private String createUser;

    /**
     * 修改时间
     */
    @TableField(exist = false)
    private Timestamp modifyTime;
    /**
     * 修改时间
     */
    @TableField(exist = false)
    @LogFormatAnnotion(name = "操作时间" )
    private Long updateTime;

    /**
     * 修改人
     */
    private String modifyUser;



    /**
     * 状态，0-开，1-封，2-关，11-锁
     *
     * @see com.panda.sport.rcs.enums.MarketStatusEnum
     */
    @TableField(exist = false)
    private Integer marketStatus;

    @TableField(exist = false)
    private Integer thirdMarketSourceStatus;

    @TableField(exist = false)
    private Integer operateMatchStatus;
    @TableField(exist = false)
    private Integer playSetCodeStatus;

    /**
     * 平衡值计算规则
     * 0 投注额 1 投注额/赔付值组合
     */
    @LogFormatAnnotion(name = "平衡值设置" )
    private Integer balanceOption;
    /**
     * 跳赔规则
     * 0 累计/单枪 1 差额累计
     */
    @LogFormatAnnotion(name = "自动跳分机制设置" )
    private Integer oddChangeRule;
    /**
     * @Description //赔率数据
     * @Param
     * @Author kimi
     * @Date 2019/12/11
     * @return
     **/
    @TableField(exist = false)
    @LogFormatAnnotion(name = "赔率" )
    private List<Map<String, Object>> oddsList;

    /**
     * 盘口状态 前端展示用
     */
    @TableField(exist = false)
    private Boolean marketActive;

    /**
     * 操盘类型 EU 欧盘 MY 马来盘
     */
    private String marketType;
    /**
     * @Description   客队水差
     * @Param
     * @Author  Sean
     * @Date  15:36 2020/1/11
     * @return
     **/
//    @LogFormatAnnotion(name = "自动水差" )
    private  String awayAutoChangeRate;

    /**
     * @Description //盘口是否关联
     * @Param 1是关联
     * @Author kimi
     * @Date 2020/2/20
     * @return
     **/
    @TableField(exist = false)
    private Integer relevanceType;
    /**
     * 盘口差
     */
    @TableField(exist = false)
    private BigDecimal marketHeadGap;

    /**
     * 赔率变化累计值
     */
    @TableField(exist = false)
    private BigDecimal oddsChange;
    /**
     * 盘口配置位置
     */
    @LogFormatAnnotion(name = "坑位N" )
    private Integer marketIndex;
    /**
     * 主胜margin
     */
    @TableField(exist = false)
    private BigDecimal homeMargin;
    /**
     * 客胜margin
     */
    @TableField(exist = false)
    private BigDecimal awayMargin;
    /**
     * 平局margin
     */
    @TableField(exist = false)
    private BigDecimal tieMargin;
    /**
     * 类型：1 ：早盘 ，2： 滚球盘， 3： 冠军盘
     */
    @TableField(exist = false)
    @LogFormatAnnotion(name = "赛事阶段" )
    private Integer matchType;
    /**
     * 投注项类型
     */
    @TableField(exist = false)
    private String oddsType;
    /**
     * 1-没有联赛信息需要再次同步，0-完整数据不需处理
     */
    @TableField(exist = false)
    private Integer active;
    /**
     * 玩法类型
     */
    @TableField(exist = false)
    private String playType;
    /**
     * 当前事件类型
     */
    @TableField(exist = false)
    private String eventCode;
    /**
     * 当前比分
     */
    @TableField(exist = false)
    private String score;
    /**
     * 一级累计盘口跳水金额
     **/
    private BigDecimal levelFirstMarketAmount;
    /**
     * 二级累计盘口跳水金额
     **/
    private BigDecimal levelSecondMarketAmount;
    /**
     * 累计上盘一级盘口变化值
     **/
    private BigDecimal homeLevelFirstMarketRate;
    /**
     * 累计上盘二级盘口变化值
     **/
    private BigDecimal homeLevelSecondMarketRate;
    /**
     * 累计下盘一级盘口变化值
     **/
    private BigDecimal awayLevelFirstMarketRate;
    /**
     * 累计下盘二级盘口变化值
     **/
    private BigDecimal awayLevelSecondMarketRate;
    /**
     * 单枪盘口跳水金额
     **/
    private BigDecimal marketSingleMaxAmount;
    /**
     * 累计盘口跳水金额
     **/
    private BigDecimal marketCumulativeMaxAmount;
    /**
     * 上盘单枪盘口变化值
     **/
    private BigDecimal homeSingleMarketRate;
    /**
     * 上盘累计盘口变化值
     **/
    private BigDecimal homeCumulativeMarketRate;
    /**
     * 下盘单枪盘口变化值
     **/
    private BigDecimal awaySingleMarketRate;
    /**
     * 下盘累计盘口变化值
     **/
    private BigDecimal awayCumulativeMarketRate;

    /**
     * 是否开启跳赔，0-否，1-是
     */
    private Integer isOpenJumpOdds;
    /**
     * 是否倍数跳赔，0-否，1-是
     */
    private Integer isMultipleJumpOdds;
    /**
     * 是否开启跳盘，0-否，1-是
     */
    private Integer isOpenJumpMarket;
    /**
     * 是否倍数跳盘，0-否，1-是
     */
    private Integer isMultipleJumpMarket;

    /***********************************************
     * 赛事账务日
     */
    @TableField(exist = false)
    private String dateExpect;
    /**
     * 投注阶段，live-滚球，pre-早盘
     */
    @TableField(exist = false)
    private String betStage;
    /**
     * 联动模式：0(否),1(是)
     */
    private Integer linkageMode;

    /**
     * 1.来自融合的自动清理标识  2.手动清理标识   3，比分清理标识  4.赛前切滚球清识标识   5.清理标识  6.数据源切换清理标识 7 margin 优化清理标识，不清概率差
     */
    @TableField(exist = false)
    private Integer clearType;
    /**
     *  盘口构建标志
     */
    @TableField(exist = false)
    private Boolean marketBuildFlag = false;
    /**
     *  子玩法id
     */
    @TableField(exist = false)
    private String subPlayId;
    /**
     * 是否特殊抽水1:是 0:否
     */
    @TableField(exist = false)
    private Integer isSpecialPumping;
    /**
     * 特殊抽水赔率区间
     */
    @TableField(exist = false)
    private String specialOddsInterval;
    /**
     * 盘口构建参数
     */
    @TableField(exist = false)
    private RcsTournamentTemplatePlayMargain buildConfig;

    @TableField(exist = false)
    private Integer sportId;

    /**
     * 操作頁面代碼
     */
    @TableField(exist = false)
    private Integer operatePageCode;

    @TableField(exist = false)
    private RcsMatchMarketConfig beforeParams;

    /**
     * 主隊名稱
     */
    @TableField(exist = false)
    private String home;

    /**
     * 客隊名稱
     */
    @TableField(exist = false)
    private String away;

    /**
     * 水差
     */
    @TableField(exist = false)
    private BigDecimal marketDiffValue;

    /**
     * 操盘类型，0-自动操盘，1-手动操盘
     *
     */
    @TableField(exist = false)
    private Integer tradeType;

    /**
     *  赛事管理id
     */
    @TableField(exist = false)
    private String matchManageId;

    /**
     * 球队
     */
    @TableField(exist = false)
    private List<MatchTeamInfo> teamList;

    public RcsMatchMarketConfig() {
    }

    public RcsMatchMarketConfig(Long matchId,  Long playId) {
    	this.matchId = matchId;
    	this.playId = playId;
    }

    public BigDecimal getHomeMarketValue(){
        return ObjectUtils.isEmpty(this.homeMarketValue) ? new BigDecimal(NumberUtils.INTEGER_ZERO) : this.homeMarketValue;
    }

    /**
     * 取得原始HomeMarketValue
     * @return
     */
    public BigDecimal getHomeMarketValueOri() {
        return this.homeMarketValue;
    }

    /**
     * 客队盘口值
     */
    public BigDecimal getAwayMarketValue(){
        return ObjectUtils.isEmpty(this.awayMarketValue) ? new BigDecimal(NumberUtils.INTEGER_ZERO) : this.awayMarketValue;
    }

    public BigDecimal getPlaceWaterDiff() {
        if (com.panda.sport.rcs.common.NumberUtils.isNumber(awayAutoChangeRate)) {
            return new BigDecimal(awayAutoChangeRate);
        }
        return BigDecimal.ZERO;
    }
    public Integer getIsOpenJumpOdds() {
        return ObjectUtils.isEmpty(isOpenJumpOdds) ? NumberUtils.INTEGER_ONE : isOpenJumpOdds;
    }
    public Integer getIsMultipleJumpOdds() {
        return ObjectUtils.isEmpty(isMultipleJumpOdds) ? NumberUtils.INTEGER_ONE : isMultipleJumpOdds;
    }
    public Integer getIsOpenJumpMarket() {
        return ObjectUtils.isEmpty(isOpenJumpMarket) ? NumberUtils.INTEGER_ONE : isOpenJumpMarket;
    }
    public Integer getIsMultipleJumpMarket() {
        return ObjectUtils.isEmpty(isMultipleJumpMarket) ? NumberUtils.INTEGER_ONE : isMultipleJumpMarket;
    }
}
