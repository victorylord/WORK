package com.panda.sport.rcs.vo;

import com.panda.sport.rcs.enums.TradeEnum;
import com.panda.sport.rcs.enums.TradeLevelEnum;
import com.panda.sport.rcs.enums.TradeStatusEnum;
import com.panda.sport.rcs.enums.YesNoEnum;
import com.panda.sport.rcs.log.annotion.format.LogFormatAnnotion;
import com.panda.sport.rcs.pojo.MatchTeamInfo;
import com.panda.sport.rcs.trade.enums.LinkedTypeEnum;
import com.panda.sport.rcs.utils.CommonUtils;
import com.panda.sport.rcs.utils.RcsAssert;
import com.panda.sport.rcs.utils.StringUtils;
import com.panda.sport.rcs.utils.TradeUserUtils;
import com.panda.sport.rcs.vo.trade.TradePlayVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @Project Name: panda-rcs-trade-group
 * @Package Name: com.panda.sport.rcs.vo
 * @Description : 手动/自动切换，开/关/封/锁请求入参
 * @Author : Paca
 * @Date : 2020-07-16 11:35
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
@Accessors(chain = true)
@LogFormatAnnotion
public class MarketStatusUpdateVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 操盘级别，1-赛事级别，2-玩法级别，3-盘口级别，4-玩法集级别，5-批量玩法级别,6-投注项级别（冠军赛事）
     *
     * @see TradeLevelEnum
     */
    @ApiModelProperty(name = "tradeLevel", value = "操盘级别")
    @LogFormatAnnotion(name = "操盘级别")
    private Integer tradeLevel;

    /**
     * 运动种类ID
     */
    @ApiModelProperty(name = "sportId", value = "赛种")
    @LogFormatAnnotion(name = "赛种")
    private Long sportId;

    /**
     * 赛事ID
     */
    @ApiModelProperty(name = "matchId", value = "赛事ID")
    @LogFormatAnnotion(name = "赛事ID")
    private Long matchId;

    /**
     * 玩法ID
     */
    @ApiModelProperty(name = "categoryId", value = "玩法ID")
    @LogFormatAnnotion(name = "玩法ID")
    private Long categoryId;

    /**
     * 子玩法ID
     */
    @ApiModelProperty(name = "subPlayId", value = "子玩法ID")
    @LogFormatAnnotion(name = "子玩法ID")
    private Long subPlayId;

    /**
     * 位置ID
     */
    @ApiModelProperty(name = "placeNumId", value = "操盘级别")
    private String placeNumId;

    /**
     * 盘口ID
     */
    @ApiModelProperty(name = "marketId", value = "盘口ID")
    private String marketId;

    /**
     * 盘口值
     */
    @ApiModelProperty(name = "marketValue", value = "盘口值")
    private String marketValue;

    /**
     * 盘口位置
     */
    @ApiModelProperty(name = "marketPlaceNum", value = "盘口位置")
    @LogFormatAnnotion(name = "盘口位置")
    private Integer marketPlaceNum;

    /**
     * 玩法集ID
     */
    @ApiModelProperty(name = "categorySetId", value = "玩法集ID")
    @LogFormatAnnotion(name = "玩法集ID")
    private Long categorySetId;

    /**
     * 玩法集编码
     */
    @ApiModelProperty(name = "playSetCode", value = "玩法集编码")
    private String playSetCode;

    /**
     * 玩法ID集合
     */
    @ApiModelProperty(name = "categoryIdList", value = "玩法ID集合")
    @LogFormatAnnotion(name = "玩法ID集合")
    private List<Long> categoryIdList;

    /**
     * 玩法集合
     */
    @ApiModelProperty(name = "playList", value = "玩法集合")
    private List<TradePlayVo> playList;

    /**
     * 子玩法ID集合
     */
    @ApiModelProperty(name = "subPlayIds", value = "子玩法ID集合")
    private List<Long> subPlayIds;

    /**
     * 状态，0-开，1-封，2-关，11-锁
     *
     * @see TradeStatusEnum
     */
    @ApiModelProperty(name = "marketStatus", value = "操盘状态")
    @LogFormatAnnotion(name = "操盘状态")
    private Integer marketStatus;

    /**
     * 操盘类型，0-自动操盘，1-手动操盘，2-自动加强操盘
     *
     * @see TradeEnum
     */
    @ApiModelProperty(name = "tradeType", value = "操盘模式")
    @LogFormatAnnotion(name = "操盘模式")
    private Integer tradeType;

    /**
     * 切换操盘模式是否封盘标志
     */
    @ApiModelProperty(name = "isSeal", value = "切换操盘模式是否封盘标志")
    private Integer isSeal;

    /**
     * 更新人ID
     */
    @ApiModelProperty(name = "updateUserId", value = "操作账号")
    @LogFormatAnnotion(name = "操作账号")
    private Integer updateUserId;

    /**
     * 是否属于前十五分钟快照赛事
     * 1是 0否
     */
    @ApiModelProperty(name = "matchSnapshot", value = "是否赛前")
    @LogFormatAnnotion(name = "是否赛前")
    private Integer matchSnapshot;

    /**
     * 数据源关盘标志，0-否，1-是
     */
    @ApiModelProperty(name = "sourceCloseFlag", value = "数据源关盘标志")
    private Integer sourceCloseFlag;

    /**
     * 收盘标志，0-否，1-是
     */
    @ApiModelProperty(name = "endFlag", value = "收盘标志")
    private Integer endFlag;

    /**
     * 1-切滚球标识，2-前十五分钟，3-数据商挡板，4-A+模式（让球0|0.5封盘），5-M模式（让球0|0.5封盘），6-球头改变独赢封盘
     *
     * @see com.panda.sport.rcs.trade.enums.LinkedTypeEnum
     */
    private Integer linkedType;

    private String remark;

    /**
     * 新增盘口标志，0-普通切换，1-新增盘口切换
     */
    private Integer newFlag;

    /**
     * 操作来源，1-操盘手
     */
    private Integer operateSource;
    /**
     * 是否推送赔率
     */
    private Integer isPushOdds;

    /**
     * 切换失败的玩法封装
     * 改版后，玩法切换操盘模式不会失败，相关代码失效
     */
    @Deprecated
    private List<Long> switchErrorPlayList;

    /**
     * 比分类型  1：是当前比分， 2：角球 3：红牌
     */
    private String scoreType;

    /**
     * 比分
     */
    private String score;

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
     * 投注项ID
     */
    private String oddsId;
    /**
     * 投注项类型
     */
    private String oddsType;

    /**
     * 盘口类型，1-赛前盘，0-滚球盘
     */
    private Integer matchType;
    /**
     * 是否MTS
     */
    private Boolean isMts;
    /**
     * 玩法集展示开关 0关 1开
     */
    private Integer clientShow;
    /**
     * 1 滚球 0早盘
     */
    private Integer liveOddBusiness;
    /**
     * 普通玩法
     */
    private List<Long> normalPlayIds;
    /**
     * 占位符玩法
     */
    private List<Long> placeholderPlayIds;
    /**
     * 封盘普通让球玩法
     */
    private List<Long> sealNormalPlayIds;
    /**
     * 封盘占位符让球玩法
     */
    private List<Long> sealPlaceholderPlayIds;

    /**
     * 关盘子玩法
     */
    private Map<Long, Long> closeSubPlayMap;

    private String linkId;

    /**
     * 操作類別編碼
     */
    private Integer operatePageCode;

    /**
     * 玩法集名称
     */
    private String playSetName;

    /**
     * 球队
     */
    private List<MatchTeamInfo> teamList;

    /**
     * 标准赛事编码. 用于管理的赛事id
     */
    private String matchManageId;

    private MarketStatusUpdateVO beforeParams;

    public String getLinkId() {
        if (StringUtils.isNotBlank(linkId)) {
            return linkId;
        }
        return CommonUtils.getLinkId();
    }

    public String generateLinkId(String func) {
        linkId = CommonUtils.getLinkId(LinkedTypeEnum.getSuffix(func, linkedType));
        return linkId;
    }

    public Integer getUpdateUserId() {
        if (updateUserId == null) {
            return TradeUserUtils.getUserIdNoException();
        }
        return updateUserId;
    }

    public Integer getIsPushOdds() {
        if (isPushOdds == null) {
            return YesNoEnum.Y.getValue();
        }
        return isPushOdds;
    }

    public Integer getIsSeal() {
        if (isSeal == null) {
            return YesNoEnum.Y.getValue();
        }
        return isSeal;
    }

    /**
     * 更新 操盘模式 参数校验
     */
    public void updateTradeModeParamCheck() {
        marketStatus = null;
        subPlayId = null;
        RcsAssert.gtZero(matchId, "赛事ID[matchId]不能为空");
        RcsAssert.isTrue(TradeLevelEnum.updateTradeTypeCheck(tradeLevel), "操盘级别[tradeLevel]有误");
        RcsAssert.isTrue(TradeEnum.checkTradeType(tradeType), "操盘方式[tradeType]有误");

        if (TradeLevelEnum.isPlayLevel(tradeLevel)) {
            RcsAssert.gtZero(categoryId, "玩法ID[categoryId]不能为空");
        }

        if (TradeLevelEnum.isBatchPlayLevel(tradeLevel)) {
            RcsAssert.isNotEmpty(categoryIdList, "玩法ID集合[categoryIdList]不能为空");
        }

        if (TradeEnum.isAutoAdd(tradeType) || TradeEnum.isLinkage(tradeType)) {
            String mode = TradeEnum.getByTradeType(tradeType).getMode();
            RcsAssert.isTrue(TradeLevelEnum.isPlayLevel(tradeLevel), "只有玩法级别才支持" + mode + "模式");
        }
    }

    /**
     * 更新 操盘状态 参数校验
     */
    public void updateStatusParamCheck() {
        tradeType = null;
        RcsAssert.gtZero(matchId, "赛事ID[matchId]不能为空");
        RcsAssert.isTrue(TradeLevelEnum.updateStatusCheck(tradeLevel), "操盘级别[tradeLevel]有误");
        RcsAssert.isTrue(TradeStatusEnum.checkMarketStatus(marketStatus), "状态[marketStatus]有误");

        if (TradeLevelEnum.isPlayLevel(tradeLevel)) {
            RcsAssert.gtZero(categoryId, "玩法ID[categoryId]不能为空");
        }
        if (TradeLevelEnum.isMarketLevel(tradeLevel)) {
            RcsAssert.gtZero(categoryId, "玩法ID[categoryId]不能为空");
            RcsAssert.gtZero(marketPlaceNum, "盘口位置[marketPlaceNum]不能为空");
        }
        if (TradeLevelEnum.isPlaySetLevel(tradeLevel)) {
            RcsAssert.gtZero(categorySetId, "玩法集ID[categorySetId]不能为空");
        }
        if (TradeLevelEnum.isBatchPlayLevel(tradeLevel)) {
            RcsAssert.isNotEmpty(categoryIdList, "玩法ID集合[categoryIdList]不能为空");
        }
        if (TradeLevelEnum.isBatchSubPlayLevel(tradeLevel)) {
            RcsAssert.isNotEmpty(playList, "玩法集合[playList]不能为空");
            for (TradePlayVo vo : playList) {
                RcsAssert.gtZero(vo.getPlayId(), "玩法集合[playList]中玩法ID[playId]不能为空");
                RcsAssert.gtZero(vo.getSubPlayId(), "玩法集合[playList]中子玩法ID[subPlayId]不能为空");
            }
        }
        if (categoryId != null && categoryId.equals(subPlayId)) {
            subPlayId = null;
        }
        if (TradeLevelEnum.isPlaySetCodeLevel(tradeLevel)) {
            RcsAssert.isNotBlank(playSetCode, "玩法集编码[playSetCode]不能为空");
        }
    }

}
