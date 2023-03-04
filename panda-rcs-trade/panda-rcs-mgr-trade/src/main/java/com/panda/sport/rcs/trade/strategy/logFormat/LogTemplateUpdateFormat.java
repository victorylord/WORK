package com.panda.sport.rcs.trade.strategy.logFormat;

import com.panda.sport.rcs.enums.OperateLogEnum;
import com.panda.sport.rcs.log.format.RcsOperateLog;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateEvent;
import com.panda.sport.rcs.trade.enums.AutoCloseMarketEnum;
import com.panda.sport.rcs.trade.enums.MatchTypeEnum;
import com.panda.sport.rcs.trade.enums.ScoreSourceEnum;
import com.panda.sport.rcs.trade.param.TournamentTemplatePlayMargainParam;
import com.panda.sport.rcs.trade.param.TournamentTemplateUpdateParam;
import com.panda.sport.rcs.trade.strategy.LogFormatStrategy;
import com.panda.sport.rcs.trade.util.NumberConventer;
import com.panda.sport.rcs.trade.vo.tourTemplate.TournamentTemplateCategorySetVo;
import com.panda.sport.rcs.trade.vo.tourTemplate.TournamentTemplateEventVo;
import com.panda.sport.rcs.trade.vo.tourTemplate.TournamentTemplatePlayMargainVo;
import com.panda.sport.rcs.trade.vo.tourTemplate.TournamentTemplateVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 操盤日誌(update)
 * 联赛模板日志-模板修改
 */
@Service
public class LogTemplateUpdateFormat extends LogFormatStrategy {

    @Autowired
    private ProducerSendMessageUtils sendMessage;

    @Override
    public RcsOperateLog formatLogBean(RcsOperateLog rcsOperateLog, Object[] args) {
        TournamentTemplateUpdateParam param = (TournamentTemplateUpdateParam) args[0];
        //LogBean基礎參數設置
        initalLogBean(rcsOperateLog, param);

        switch (param.getMatchType()) {
            case 0:
                //滾球模板
                inRunningTemplateFilter(rcsOperateLog, param);
                break;
            case 1:
                //早盤模板判斷
                earlyTemplateFilter(rcsOperateLog, param);
                break;
        }
        return null; //清空，避免原先邏輯多發送一次MQ
    }

    /**
     * 滾球模板判斷是否要寫Log
     *
     * @param rcsOperateLog
     * @param param
     */
    private void inRunningTemplateFilter(RcsOperateLog rcsOperateLog, TournamentTemplateUpdateParam param) {
        earlyTemplateFilter(rcsOperateLog, param);

        TournamentTemplateVo beforeParams = param.getBeforeParams();
        List<TournamentTemplateEventVo> beforeTemplateEventList = beforeParams.getTemplateEventList();
        for (int i = 0; i < beforeTemplateEventList.size(); i++) {
            RcsTournamentTemplateEvent newEventVo = param.getTemplateEventList().get(i);
            TournamentTemplateEventVo oriEventVo = beforeTemplateEventList.get(i);

            //事件审核时间
            if (Objects.nonNull(newEventVo.getEventHandleTime()) &&
                    !newEventVo.getEventHandleTime().equals(oriEventVo.getEventHandleTime())) {
                RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, secondToTime(oriEventVo.getEventHandleTime()), secondToTime(newEventVo.getEventHandleTime()));
                templateLog.setBehavior(OperateLogEnum.TEMPLATE_UPDATE.getName());
                templateLog.setParameterName(newEventVo.getEventDesc() + "-" + OperateLogEnum.EVENT_HANDLE_TIME.getName());
                pushMessage(templateLog);
            }
            //结算审核时间
            if (Objects.nonNull(newEventVo.getSettleHandleTime()) &&
                    !newEventVo.getSettleHandleTime().equals(oriEventVo.getSettleHandleTime())) {
                RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, secondToTime(oriEventVo.getSettleHandleTime()), secondToTime(newEventVo.getSettleHandleTime()));
                templateLog.setBehavior(OperateLogEnum.TEMPLATE_UPDATE.getName());
                templateLog.setParameterName(newEventVo.getEventDesc() + "-" + OperateLogEnum.SETTLE_HANDLE_TIME.getName());
                pushMessage(templateLog);
            }

        }

        //比分源

        String oriScoreSource = String.valueOf(Optional.ofNullable(beforeParams.getScoreSource()).orElse(null));
        String newScoreSource = String.valueOf(Optional.ofNullable(param.getScoreSource()).orElse(null));
        if (!newScoreSource.equals(oriScoreSource)) {
            RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, transScoreSource(beforeParams.getScoreSource()), transScoreSource(param.getScoreSource()));
            templateLog.setBehavior(OperateLogEnum.TEMPLATE_UPDATE.getName());
            templateLog.setParameterName(OperateLogEnum.SCORE_SOURCE.getName());
            pushMessage(templateLog);
        }

    }

    /**
     * 早盤模板判斷是否要寫Log
     *
     * @param rcsOperateLog
     * @param param
     */
    private void earlyTemplateFilter(RcsOperateLog rcsOperateLog, TournamentTemplateUpdateParam param) {
        TournamentTemplateVo beforeParams = param.getBeforeParams();

        //模板名稱
        if (Objects.nonNull(param.getTemplateName()) &&
                !param.getTemplateName().equals(param.getBeforeParams().getTemplateName())) {
            RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, beforeParams.getTemplateName(), param.getTemplateName());
            templateLog.setBehavior(OperateLogEnum.TEMPLATE_UPDATE.getName());
            templateLog.setParameterName(OperateLogEnum.TEMPLATE_NAME.getName());
            pushMessage(templateLog);
        }

        //赔率源设置
        if (Objects.nonNull(param.getDataSourceCode()) &&
                !param.getDataSourceCode().equals(beforeParams.getDataSourceCode())) {
            RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, beforeParams.getDataSourceCode(), param.getDataSourceCode());
            templateLog.setBehavior(OperateLogEnum.TEMPLATE_UPDATE.getName());
            templateLog.setParameterName(OperateLogEnum.DATA_SOURCE_CODE.getName());
            pushMessage(templateLog);
        }

        //商户单场赔付限额
        if (Objects.nonNull(param.getBusinesMatchPayVal()) &&
                !param.getBusinesMatchPayVal().equals(beforeParams.getBusinesMatchPayVal())) {
            RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, beforeParams.getBusinesMatchPayVal(), param.getBusinesMatchPayVal());
            templateLog.setBehavior(OperateLogEnum.TEMPLATE_UPDATE.getName());
            templateLog.setParameterName(OperateLogEnum.BUSINES_MATCH_PAY_VAL.getName());
            pushMessage(templateLog);
        }

        //用户单场赔付限额
        if (Objects.nonNull(param.getUserMatchPayVal()) &&
                !param.getUserMatchPayVal().equals(beforeParams.getUserMatchPayVal())) {
            RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, beforeParams.getUserMatchPayVal(), param.getUserMatchPayVal());
            templateLog.setBehavior(OperateLogEnum.TEMPLATE_UPDATE.getName());
            templateLog.setParameterName(OperateLogEnum.USER_MATCH_PAY_VAL.getName());
            pushMessage(templateLog);
        }

        //提前结算开关
        if (Objects.nonNull(param.getMatchPreStatus()) &&
                !param.getMatchPreStatus().equals(beforeParams.getMatchPreStatus())) {
            RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, getStatusName(beforeParams.getMatchPreStatus()), getStatusName(param.getMatchPreStatus()));
            templateLog.setBehavior(OperateLogEnum.TEMPLATE_UPDATE.getName());
            templateLog.setParameterName(OperateLogEnum.MATCH_PRE_STATUS.getName());
            pushMessage(templateLog);
        }

        //玩法集margain配置
        playMargainDiffFilter(rcsOperateLog, param, beforeParams);
    }

    /**
     * 玩法集margain配置
     *
     * @param rcsOperateLog
     * @param param
     * @param beforeParams
     */
    private void playMargainDiffFilter(RcsOperateLog rcsOperateLog, TournamentTemplateUpdateParam param, TournamentTemplateVo beforeParams) {
        //組織原始玩法集Map<模板Id,玩法Vo>
        Map<Integer, TournamentTemplatePlayMargainVo> beforePlayMap = new HashMap<>();
        for (TournamentTemplateCategorySetVo categorySetVo : beforeParams.getCategorySetList()) {
            for (TournamentTemplatePlayMargainVo playMargainVo : categorySetVo.getCategoryList()) {
                beforePlayMap.put(playMargainVo.getPlayId(), playMargainVo);
            }
        }

        for (TournamentTemplatePlayMargainParam newParam : param.getPlayMargainList()) {
            TournamentTemplatePlayMargainVo beforeVo = beforePlayMap.get(newParam.getPlayId());

            //是否开售
            if (Objects.nonNull(newParam.getIsSell()) &&
                    !newParam.getIsSell().equals(beforeVo.getIsSell())) {
                RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, getStatusName(beforeVo.getIsSell()), getStatusName(newParam.getIsSell()));
                templateLog.setBehavior(OperateLogEnum.TEMPLATE_UPDATE.getName());
                templateLog.setParameterName(newParam.getPlayName());
                pushMessage(templateLog);
            }

            //盘口参数
            //最大盘口数
            if (Objects.nonNull(newParam.getMarketCount()) &&
                    !newParam.getMarketCount().equals(beforeVo.getMarketCount())) {
                RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, beforeVo.getMarketCount(), newParam.getMarketCount());
                templateLog.setBehavior(OperateLogEnum.TEMPLATE_UPDATE.getName());
                templateLog.setParameterName(newParam.getPlayName() + "-" + OperateLogEnum.MARKET_COUNT.getName());
                pushMessage(templateLog);
            }
            //副盘限额比例
            String oriViceMarketRatio = StringUtils.isNotBlank(beforeVo.getViceMarketRatio())? beforeVo.getViceMarketRatio() : OperateLogEnum.NONE.getName();
            String newViceMarketRatio = StringUtils.isNotBlank(newParam.getViceMarketRatio())? newParam.getViceMarketRatio() : OperateLogEnum.NONE.getName();
            if (!newViceMarketRatio.equals(oriViceMarketRatio)) {
                RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, oriViceMarketRatio, newViceMarketRatio);
                templateLog.setBehavior(OperateLogEnum.TEMPLATE_UPDATE.getName());
                templateLog.setParameterName(newParam.getPlayName() + "-" + OperateLogEnum.VICE_MARKET_RATIO.getName());
                pushMessage(templateLog);
            }
            //盘口赔付预警
            if (Objects.nonNull(newParam.getMarketWarn()) &&
                    !newParam.getMarketWarn().equals(beforeVo.getMarketWarn())) {
                RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, beforeVo.getMarketWarn(), newParam.getMarketWarn());
                templateLog.setBehavior(OperateLogEnum.TEMPLATE_UPDATE.getName());
                templateLog.setParameterName(newParam.getPlayName() + "-" + OperateLogEnum.MARKET_WARN.getName());
                pushMessage(templateLog);
            }
            //支持串关
            if (Objects.nonNull(newParam.getIsSeries()) &&
                    !newParam.getIsSeries().equals(beforeVo.getIsSeries())) {
                RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, getTrueFalse(beforeVo.getIsSeries()), getTrueFalse(newParam.getIsSeries()));
                templateLog.setBehavior(OperateLogEnum.TEMPLATE_UPDATE.getName());
                templateLog.setParameterName(newParam.getPlayName() + "-" + OperateLogEnum.IS_SERIES.getName());
                pushMessage(templateLog);
            }

            //赔率(水差)变动幅度
            if (Objects.nonNull(newParam.getOddsAdjustRange()) &&
                    newParam.getOddsAdjustRange().compareTo(beforeVo.getOddsAdjustRange()) != 0) {
                RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, beforeVo.getOddsAdjustRange(), newParam.getOddsAdjustRange());
                templateLog.setBehavior(OperateLogEnum.TEMPLATE_UPDATE.getName());
                templateLog.setParameterName(newParam.getPlayName() + "-" + OperateLogEnum.ODDS_ADJUST_RANGE.getName());
                pushMessage(templateLog);
            }
            //手动操盘相邻盘口差
            if (Objects.nonNull(newParam.getManualMarketNearDiff()) &&
                    newParam.getManualMarketNearDiff().compareTo(beforeVo.getManualMarketNearDiff()) != 0) {
                RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, beforeVo.getManualMarketNearDiff(), newParam.getManualMarketNearDiff());
                templateLog.setBehavior(OperateLogEnum.TEMPLATE_UPDATE.getName());
                templateLog.setParameterName(newParam.getPlayName() + "-" + OperateLogEnum.MANUAL_MARKET_NEAR_DIFF.getName());
                pushMessage(templateLog);
            }
            //手动操盘相邻盘口赔率差
            if (Objects.nonNull(newParam.getManualMarketNearOddsDiff()) &&
                    newParam.getManualMarketNearOddsDiff().compareTo(beforeVo.getManualMarketNearOddsDiff()) != 0) {
                RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, beforeVo.getManualMarketNearOddsDiff(), newParam.getManualMarketNearOddsDiff());
                templateLog.setBehavior(OperateLogEnum.TEMPLATE_UPDATE.getName());
                templateLog.setParameterName(newParam.getPlayName() + "-" + OperateLogEnum.MANUAL_MARKET_NEAR_ODDS_DIFF.getName());
                pushMessage(templateLog);
            }

            //是否特殊抽水
            if (Objects.nonNull(newParam.getIsSpecialPumping()) &&
                    !newParam.getIsSpecialPumping().equals(beforeVo.getIsSpecialPumping())) {
                RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, getTrueFalse(beforeVo.getIsSpecialPumping()), getTrueFalse(newParam.getIsSpecialPumping()));
                templateLog.setBehavior(OperateLogEnum.TEMPLATE_UPDATE.getName());
                templateLog.setParameterName(newParam.getPlayName() + "-" + OperateLogEnum.SPECIAL_PUMPING.getName() + "-" + OperateLogEnum.IS_SPECIAL_PUMPING.getName());
                pushMessage(templateLog);
            }

            //特殊抽水赔率区间(Malay Spread)
            if (Objects.nonNull(newParam.getSpecialOddsInterval()) &&
                    !newParam.getSpecialOddsInterval().equals(beforeVo.getSpecialOddsInterval())) {
                RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, beforeVo.getSpecialOddsInterval(), newParam.getSpecialOddsInterval());
                templateLog.setBehavior(OperateLogEnum.TEMPLATE_UPDATE.getName());
                templateLog.setParameterName(newParam.getPlayName() + "-" + OperateLogEnum.SPECIAL_PUMPING.getName() + "-" + OperateLogEnum.SPECIAL_ODDS_INTERVAL.getName());
                pushMessage(templateLog);
            }

            //特殊抽水赔率区间状态(限额生效)
            if (Objects.nonNull(newParam.getSpecialOddsIntervalStatus()) &&
                    !newParam.getSpecialOddsIntervalStatus().equals(beforeVo.getSpecialOddsIntervalStatus())) {
                RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, beforeVo.getSpecialOddsIntervalStatus(), newParam.getSpecialOddsIntervalStatus());
                templateLog.setBehavior(OperateLogEnum.TEMPLATE_UPDATE.getName());
                templateLog.setParameterName(newParam.getPlayName() + "-" + OperateLogEnum.SPECIAL_PUMPING.getName() + "-" + OperateLogEnum.SPECIAL_ODDS_INTERVAL_STATUS.getName());
                pushMessage(templateLog);
            }

            //低赔特殊抽水赔率区间(低赔:单注赔付限额)
            if (Objects.nonNull(newParam.getSpecialOddsIntervalLow()) &&
                    !newParam.getSpecialOddsIntervalLow().equals(beforeVo.getSpecialOddsIntervalLow())) {
                RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, beforeVo.getSpecialOddsIntervalLow(), newParam.getSpecialOddsIntervalLow());
                templateLog.setBehavior(OperateLogEnum.TEMPLATE_UPDATE.getName());
                templateLog.setParameterName(newParam.getPlayName() + "-" + OperateLogEnum.SPECIAL_PUMPING.getName() + "-" + OperateLogEnum.SPECIAL_ODDS_INTERVAL_LOW.getName());
                pushMessage(templateLog);
            }

            //高赔特殊抽水赔率区间(高赔:单注投注赔付限额)
            if (Objects.nonNull(newParam.getSpecialOddsIntervalHigh()) &&
                    !newParam.getSpecialOddsIntervalHigh().equals(beforeVo.getSpecialOddsIntervalHigh())) {
                RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, beforeVo.getSpecialOddsIntervalHigh(), newParam.getSpecialOddsIntervalHigh());
                templateLog.setBehavior(OperateLogEnum.TEMPLATE_UPDATE.getName());
                templateLog.setParameterName(newParam.getPlayName() + "-" + OperateLogEnum.SPECIAL_PUMPING.getName() + "-" + OperateLogEnum.SPECIAL_ODDS_INTERVAL_HIGH.getName());
                pushMessage(templateLog);
            }

            //自动关盘时间设置
            if (Objects.nonNull(newParam.getAutoCloseMarket()) &&
                    !newParam.getAutoCloseMarket().equals(beforeVo.getAutoCloseMarket())) {
                RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, AutoCloseMarketEnum.getValue(beforeVo.getAutoCloseMarket()), AutoCloseMarketEnum.getValue(newParam.getAutoCloseMarket()));
                templateLog.setBehavior(OperateLogEnum.TEMPLATE_UPDATE.getName());
                templateLog.setParameterName(newParam.getPlayName() + "-" + OperateLogEnum.AUTO_CLOSE_MARKET.getName());
                pushMessage(templateLog);
            }

            //比赛进程时间
            if (Objects.nonNull(newParam.getMatchProgressTime()) &&
                    !newParam.getMatchProgressTime().equals(beforeVo.getMatchProgressTime())) {
                RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, secondToTime(beforeVo.getMatchProgressTime()), secondToTime(newParam.getMatchProgressTime()));
                templateLog.setBehavior(OperateLogEnum.TEMPLATE_UPDATE.getName());
                templateLog.setParameterName(newParam.getPlayName() + "-" + AutoCloseMarketEnum.getValue(newParam.getAutoCloseMarket()) + OperateLogEnum.MATCH_PROGRESS_TIME.getName());
                pushMessage(templateLog);
            }

            //补时时间
            if (Objects.nonNull(newParam.getInjuryTime()) &&
                    !newParam.getInjuryTime().equals(beforeVo.getInjuryTime())) {
                RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, secondToTime(beforeVo.getInjuryTime()), secondToTime(newParam.getInjuryTime()));
                templateLog.setBehavior(OperateLogEnum.TEMPLATE_UPDATE.getName());
                templateLog.setParameterName(newParam.getPlayName() + "-" + AutoCloseMarketEnum.getValue(newParam.getAutoCloseMarket()) + OperateLogEnum.INJURY_TIME.getName());
                pushMessage(templateLog);
            }
        }
    }

    /**
     * 準備LogBean 塞入beafore after
     *
     * @param sample
     * @param before
     * @param after
     * @return
     */
    private RcsOperateLog prepareLogBean(RcsOperateLog sample, Object before, Object after) {
        RcsOperateLog rcsOperateLog = new RcsOperateLog();
        BeanUtils.copyProperties(sample, rcsOperateLog);
        rcsOperateLog.setBeforeValByObj(before);
        rcsOperateLog.setAfterValByObj(after);
        return rcsOperateLog;
    }

    /**
     * 初始化LogBean
     *
     * @param rcsOperateLog
     * @param param
     */
    private void initalLogBean(RcsOperateLog rcsOperateLog, TournamentTemplateUpdateParam param) {
        rcsOperateLog.setOperatePageCode(param.getOperatePageCode());
        rcsOperateLog.setObjectIdByObj(param.getId());
        String tournamentLevelTemplateName = getTournamentLevelTemplateName(param);
        rcsOperateLog.setObjectNameByObj(StringUtils.isNoneBlank(param.getTemplateName()) ? param.getTemplateName() : tournamentLevelTemplateName);
        rcsOperateLog.setExtObjectIdByObj(OperateLogEnum.NONE.getName());
        rcsOperateLog.setExtObjectNameByObj(OperateLogEnum.NONE.getName());
    }

    private String getTournamentLevelTemplateName(TournamentTemplateUpdateParam param) {
        if(Objects.nonNull(param.getBeforeParams().getTypeVal())) {
            String levelName = NumberConventer.GetCH(param.getBeforeParams().getTypeVal().intValue());
            return StringUtils.isNoneBlank(levelName) ? levelName + "级联赛" + MatchTypeEnum.getNameById(param.getBeforeParams().getMatchType()) + "模板" : "";
        }else{
            return "";
        }
    }



    private void pushMessage(RcsOperateLog rcsOperateLog) {
        sendMessage.sendMessage("rcs_log_operate", "", "", rcsOperateLog);
    }

    /**
     * 轉換狀態碼
     *
     * @param status
     * @return
     */
    private String getStatusName(Integer status) {
        switch (status) {
            case 0:
                return "关";
            case 1:
                return "开";
            default:
                return "";
        }
    }

    /**
     * 比分源轉換
     *
     * @param scoreSource
     * @return
     */
    private String transScoreSource(Integer scoreSource) {
        if (Objects.nonNull(scoreSource)) {

            switch (scoreSource) {
                case 1:
                    return ScoreSourceEnum.SR.getName();
                case 2:
                    return ScoreSourceEnum.UOF.getName();
            }
        } else {
            return "自動匹配";
        }
        return String.valueOf(scoreSource);
    }

    /**
     * 轉換true false 1:是 0:否
     *
     * @param status
     * @return
     */
    private String getTrueFalse(Integer status) {
        switch (status) {
            case 0:
                return "否";
            case 1:
                return "是";
            default:
                return "";
        }
    }
}
