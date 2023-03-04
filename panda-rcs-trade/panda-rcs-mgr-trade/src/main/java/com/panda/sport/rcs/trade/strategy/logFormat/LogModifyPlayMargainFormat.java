package com.panda.sport.rcs.trade.strategy.logFormat;

import com.panda.sport.rcs.enums.OperateLogEnum;
import com.panda.sport.rcs.log.format.RcsOperateLog;
import com.panda.sport.rcs.mapper.RcsLanguageInternationMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.LanguageInternation;
import com.panda.sport.rcs.trade.enums.AutoCloseMarketEnum;
import com.panda.sport.rcs.trade.param.TournamentTemplatePlayMargainParam;
import com.panda.sport.rcs.trade.strategy.LogFormatStrategy;
import com.panda.sport.rcs.utils.CategoryParseUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 操盤日誌(modifyPlayMargain)
 * 設置-盤口參數調整-最大盤口數/盤口賠付預警/支持串關/賠率(水差)變動幅度/自動關盤時間設置
 */
@Service
public class LogModifyPlayMargainFormat extends LogFormatStrategy {

	@Autowired
    private RcsLanguageInternationMapper rcsLanguageInternationMapper;
    @Autowired
    private ProducerSendMessageUtils sendMessage;

    @Override
    public RcsOperateLog formatLogBean(RcsOperateLog rcsOperateLog, Object[] args) {
        TournamentTemplatePlayMargainParam param = (TournamentTemplatePlayMargainParam) args[0];

        //根據不同操作頁面組裝不同格式
        switch (param.getOperatePageCode()) {
            case 14:
                //早盘操盘-设置
                rcsOperateLog.setOperatePageCode(110);
                break;
            case 17:
                //滚球操盘-设置
                rcsOperateLog.setOperatePageCode(111);
                break;
            default:
                rcsOperateLog.setOperatePageCode(param.getOperatePageCode());
        }

        rcsOperateLog.setMatchId(param.getMatchId());
        rcsOperateLog.setObjectIdByObj(param.getPlayId());

        return getRcsOperateLog(rcsOperateLog, param);
    }

    private RcsOperateLog getRcsOperateLog(RcsOperateLog rcsOperateLog, TournamentTemplatePlayMargainParam param) {
        String playName = getPlayName(param.getPlayId().longValue(), param.getSportId());
        rcsOperateLog.setObjectNameByObj(playName);
        rcsOperateLog.setExtObjectIdByObj(param.getMatchManageId());
        rcsOperateLog.setExtObjectNameByObj(getMatchName(param.getTeamList()));

        //最大盤口數/盤口賠付預警
        if (Objects.nonNull(param.getMarketCount())) {
            marketSettingFormat(rcsOperateLog, param, playName);

            //支持串關
        } else if (Objects.nonNull(param.getIsSeries())) {
            return isSeriesFormat(rcsOperateLog, param, playName);

            //賠率(水差)變動幅度
        } else if (Objects.nonNull(param.getOddsAdjustRange())) {
            return oddsAdjustRangeFormat(rcsOperateLog, param, playName);

            //自動關盤時間設置
        } else if (Objects.nonNull(param.getAutoCloseMarket())) {
            autoCloseMarketFormat(rcsOperateLog, param, playName);

            //手动操盘相邻盘口差
        } else if (Objects.nonNull(param.getManualMarketNearDiff())) {
            manualMarketNearDiffFormat(rcsOperateLog, param, playName);
        }
        return null;
    }

    /**
     * 最大盤口數/盤口賠付預警 格式
     *
     * @param rcsOperateLog
     * @param param
     */
    private void marketSettingFormat(RcsOperateLog rcsOperateLog, TournamentTemplatePlayMargainParam param, String playName) {

        if (param.getMarketCount().compareTo(param.getBeforeParams().getMarketCount()) != 0) {
            RcsOperateLog marketCountLog = new RcsOperateLog();
            BeanUtils.copyProperties(rcsOperateLog, marketCountLog);
            marketCountLog.setParameterName(playName + "-" + OperateLogEnum.MARKET_COUNT.getName());
            marketCountLog.setBeforeValByObj(param.getBeforeParams().getMarketCount());
            marketCountLog.setAfterValByObj(param.getMarketCount());
            pushMessage(marketCountLog);
        }

        if (Objects.nonNull(param.getMarketWarn()) &&
                param.getMarketWarn().compareTo(param.getBeforeParams().getMarketWarn()) != 0) {
            RcsOperateLog marketWarnLog = new RcsOperateLog();
            BeanUtils.copyProperties(rcsOperateLog, marketWarnLog);
            marketWarnLog.setParameterName(playName + "-" + OperateLogEnum.MARKET_WARN.getName());
            marketWarnLog.setBeforeValByObj(param.getBeforeParams().getMarketWarn());
            marketWarnLog.setAfterValByObj(param.getMarketWarn());
            pushMessage(marketWarnLog);
        }

        if (Objects.nonNull(param.getViceMarketRatio()) &&
                !param.getViceMarketRatio().equals(param.getBeforeParams().getViceMarketRatio())) {
            RcsOperateLog viceMarketRatioLog = new RcsOperateLog();
            BeanUtils.copyProperties(rcsOperateLog, viceMarketRatioLog);
            viceMarketRatioLog.setParameterName(playName + "-" + OperateLogEnum.VICE_MARKET_RATIO.getName());
            viceMarketRatioLog.setBeforeValByObj(param.getBeforeParams().getViceMarketRatio());
            viceMarketRatioLog.setAfterValByObj(param.getViceMarketRatio());
            pushMessage(viceMarketRatioLog);
        }
    }

    /**
     * 支持串關 格式
     *
     * @param rcsOperateLog
     * @param param
     * @return
     */
    private RcsOperateLog isSeriesFormat(RcsOperateLog rcsOperateLog, TournamentTemplatePlayMargainParam param, String playName) {
        if (param.getIsSeries().compareTo(param.getBeforeParams().getIsSeries()) != 0) {
            rcsOperateLog.setParameterName(playName + "-" + OperateLogEnum.IS_SERIES.getName());
            rcsOperateLog.setBeforeValByObj(getIsSeriesName(param.getBeforeParams().getIsSeries()));
            rcsOperateLog.setAfterValByObj(getIsSeriesName(param.getIsSeries()));
            return rcsOperateLog;
        }
        return null;
    }

    /**
     * 賠率(水差)變動幅度 格式
     *
     * @param rcsOperateLog
     * @param param
     * @return
     */
    private RcsOperateLog oddsAdjustRangeFormat(RcsOperateLog rcsOperateLog, TournamentTemplatePlayMargainParam param, String playName) {
        if (param.getOddsAdjustRange().compareTo(param.getBeforeParams().getOddsAdjustRange()) != 0) {
            rcsOperateLog.setParameterName(playName + "-" + OperateLogEnum.ODDS_ADJUST_RANGE.getName());
            rcsOperateLog.setBeforeValByObj(param.getBeforeParams().getOddsAdjustRange());
            rcsOperateLog.setAfterValByObj(param.getOddsAdjustRange());
            return rcsOperateLog;
        }
        return null;
    }

    /**
     * 自动关盘时间设置 格式
     *
     * @param rcsOperateLog
     * @param param
     * @return
     */
    private void autoCloseMarketFormat(RcsOperateLog rcsOperateLog, TournamentTemplatePlayMargainParam param, String playName) {

        //自动关盘时间设置
        if (param.getAutoCloseMarket().compareTo(param.getBeforeParams().getAutoCloseMarket()) != 0) {
            RcsOperateLog logBean = new RcsOperateLog();
            BeanUtils.copyProperties(rcsOperateLog, logBean);
            logBean.setParameterName(playName + "-" + OperateLogEnum.AUTO_CLOSE_MARKET.getName());
            logBean.setBeforeValByObj(AutoCloseMarketEnum.getValue(param.getBeforeParams().getAutoCloseMarket()));
            logBean.setAfterValByObj(AutoCloseMarketEnum.getValue(param.getAutoCloseMarket()));
            pushMessage(logBean);
        }
        //比赛进程时间
        if (Objects.nonNull(param.getMatchProgressTime()) &&
                param.getMatchProgressTime().compareTo(param.getBeforeParams().getMatchProgressTime()) != 0) {
            RcsOperateLog logBean = new RcsOperateLog();
            BeanUtils.copyProperties(rcsOperateLog, logBean);
            logBean.setParameterName(playName + "-" + AutoCloseMarketEnum.getValue(param.getAutoCloseMarket()) + OperateLogEnum.MATCH_PROGRESS_TIME.getName());
            logBean.setBeforeValByObj(secondToTime(param.getBeforeParams().getMatchProgressTime()));
            logBean.setAfterValByObj(secondToTime(param.getMatchProgressTime()));
            pushMessage(logBean);
        }
        //补时时间
        if (Objects.nonNull(param.getInjuryTime()) &&
                param.getInjuryTime().compareTo(param.getBeforeParams().getInjuryTime()) != 0) {
            RcsOperateLog logBean = new RcsOperateLog();
            BeanUtils.copyProperties(rcsOperateLog, logBean);
            logBean.setParameterName(playName + "-" + AutoCloseMarketEnum.getValue(param.getAutoCloseMarket()) + OperateLogEnum.INJURY_TIME.getName());
            logBean.setBeforeValByObj(secondToTime(param.getBeforeParams().getInjuryTime()));
            logBean.setAfterValByObj(secondToTime(param.getInjuryTime()));
            pushMessage(logBean);
        }
    }

    /**
     * 手动操盘相邻盘口差 格式
     *
     * @param rcsOperateLog
     * @param param
     * @param playName
     */
    private void manualMarketNearDiffFormat(RcsOperateLog rcsOperateLog, TournamentTemplatePlayMargainParam param, String playName) {

        //手动操盘相邻盘口差
        if (param.getManualMarketNearDiff().compareTo(param.getBeforeParams().getManualMarketNearDiff()) != 0) {
            RcsOperateLog logBean = new RcsOperateLog();
            BeanUtils.copyProperties(rcsOperateLog, logBean);
            logBean.setParameterName(playName + "-" + OperateLogEnum.MANUAL_MARKET_NEAR_DIFF.getName());
            logBean.setBeforeValByObj(param.getBeforeParams().getManualMarketNearDiff());
            logBean.setAfterValByObj(param.getManualMarketNearDiff());
            pushMessage(logBean);
        }
        //手动操盘相邻盘口赔率差
        if (Objects.nonNull(param.getManualMarketNearOddsDiff()) &&
                param.getManualMarketNearOddsDiff().compareTo(param.getBeforeParams().getManualMarketNearOddsDiff()) != 0) {
            RcsOperateLog logBean = new RcsOperateLog();
            BeanUtils.copyProperties(rcsOperateLog, logBean);
            logBean.setParameterName(playName + "-" + OperateLogEnum.MANUAL_MARKET_NEAR_ODDS_DIFF.getName());
            logBean.setBeforeValByObj(param.getBeforeParams().getManualMarketNearOddsDiff());
            logBean.setAfterValByObj(param.getManualMarketNearOddsDiff());
            pushMessage(logBean);
        }
    }

    /**
     * 查詢玩法名稱
     *
     * @param playId
     * @return
     */
    private String getPlayName(Long playId, Integer sportId) {
        LanguageInternation playName = rcsLanguageInternationMapper.getPlayNameByCategoryIdAndSportId(playId, sportId);
        return CategoryParseUtils.parseName(Objects.nonNull(playName) ? playName.getText() : "");
    }

    /**
     * 轉換狀態碼
     *
     * @param isSeries
     * @return
     */
    private String getIsSeriesName(Integer isSeries) {
        switch (isSeries) {
            case 0:
                return "否";
            case 1:
                return "是";
            default:
                return "";
        }
    }

    private void pushMessage(RcsOperateLog rcsOperateLog) {
        sendMessage.sendMessage("rcs_log_operate", "", "", rcsOperateLog);
    }

}
