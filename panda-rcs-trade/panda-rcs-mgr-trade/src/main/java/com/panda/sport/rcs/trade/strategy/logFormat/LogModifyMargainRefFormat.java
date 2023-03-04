package com.panda.sport.rcs.trade.strategy.logFormat;

import com.panda.sport.rcs.enums.OperateLogEnum;
import com.panda.sport.rcs.log.format.RcsOperateLog;
import com.panda.sport.rcs.mapper.RcsLanguageInternationMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.LanguageInternation;
import com.panda.sport.rcs.trade.enums.MatchTypeEnum;
import com.panda.sport.rcs.trade.param.TournamentTemplatePlayMargainRefParam;
import com.panda.sport.rcs.trade.strategy.LogFormatStrategy;
import com.panda.sport.rcs.trade.util.NumberConventer;
import com.panda.sport.rcs.utils.CategoryParseUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 操盤日誌(modifyMargainRef)
 * 設置-分時節點設置 保存
 */
@Service
public class LogModifyMargainRefFormat extends LogFormatStrategy {
	
    @Autowired
    private ProducerSendMessageUtils sendMessage;
    
    @Autowired
    private RcsLanguageInternationMapper rcsLanguageInternationMapper;

    @Override
    public RcsOperateLog formatLogBean(RcsOperateLog rcsOperateLog, Object[] args) {
        TournamentTemplatePlayMargainRefParam param = (TournamentTemplatePlayMargainRefParam) args[0];

        //根據不同操作頁面組裝不同格式
        switch (param.getOperatePageCode()) {
            case 14:
                //早盘操盘-设置
                rcsOperateLog.setOperatePageCode(110);
                initalOperateFormat(rcsOperateLog, param);
                break;
            case 17:
                //滚球操盘-设置
                rcsOperateLog.setOperatePageCode(111);
                initalOperateFormat(rcsOperateLog, param);
                break;
            case 21:
                rcsOperateLog.setOperatePageCode(param.getOperatePageCode());
                initalTemplateFormat(rcsOperateLog, param);
                break;
        }

        configDiffFilter(rcsOperateLog, param);

        return null;
    }

    private void configDiffFilter(RcsOperateLog rcsOperateLog, TournamentTemplatePlayMargainRefParam newParam) {
        //取原始值
        TournamentTemplatePlayMargainRefParam oriVo = newParam.getBeforeParams();

        //查詢玩法名稱
        String playName = getPlayName(newParam.getPlayId().longValue(), newParam.getSportId());
        String timeValName = transTimeValName(newParam);

        //新增分時節點
        if (Objects.isNull(newParam.getId())) {
            RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, OperateLogEnum.NONE.getName(), "新增");
            templateLog.setParameterName(playName + "-" + timeValName);
            pushMessage(templateLog);
        }

        //Malay Spread
        if (Objects.nonNull(newParam.getMargain()) &&
                !newParam.getMargain().equals(oriVo.getMargain())) {
            RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, oriVo.getMargain(), newParam.getMargain());
            templateLog.setParameterName(playName + "-" + timeValName + "-" + OperateLogEnum.SPECIAL_ODDS_INTERVAL.getName());
            pushMessage(templateLog);
        }
        //累计差额计算方式
        if (Objects.nonNull(newParam.getBalanceOption()) &&
                !newParam.getBalanceOption().equals(oriVo.getBalanceOption())) {
            RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, getBalanceOptionName(oriVo.getBalanceOption()), getBalanceOptionName(newParam.getBalanceOption()));
            templateLog.setParameterName(playName + "-" + timeValName + "-" + OperateLogEnum.BALANCE_OPTION.getName());
            pushMessage(templateLog);
        }

        //最小马来赔
        if (Objects.nonNull(newParam.getMinOdds()) &&
                !newParam.getMinOdds().equals(oriVo.getMinOdds())) {
            RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, oriVo.getMinOdds(), newParam.getMinOdds());
            templateLog.setParameterName(playName + "-" + timeValName + "-" + OperateLogEnum.MIN_MALAY_ODDS.getName());
            pushMessage(templateLog);
        }

        //最大马来赔
        if (Objects.nonNull(newParam.getMaxOdds()) &&
                !newParam.getMaxOdds().equals(oriVo.getMaxOdds())) {
            RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, oriVo.getMaxOdds(), newParam.getMaxOdds());
            templateLog.setParameterName(playName + "-" + timeValName + "-" + OperateLogEnum.MAX_MALAY_ODDS.getName());
            pushMessage(templateLog);
        }

        //跳分机制
        if (Objects.nonNull(newParam.getOddChangeRule()) &&
                !newParam.getOddChangeRule().equals(oriVo.getOddChangeRule())) {
            RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, getOddChangeRuleName(oriVo.getOddChangeRule()), getOddChangeRuleName(newParam.getOddChangeRule()));
            templateLog.setParameterName(playName + "-" + timeValName + "-" + OperateLogEnum.ODD_CHANGE_RULE.getName());
            pushMessage(templateLog);
        }

        //累计/单枪跳分
        //累计 限额值
        if (Objects.nonNull(newParam.getHomeMultiMaxAmount()) &&
                !newParam.getHomeMultiMaxAmount().equals(oriVo.getHomeMultiMaxAmount())) {
            RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, oriVo.getHomeMultiMaxAmount(), newParam.getHomeMultiMaxAmount());
            templateLog.setParameterName(playName + "-" + timeValName + "-" + OperateLogEnum.HOME_MULTI_MAX_AMOUNT.getName());
            pushMessage(templateLog);
        }

        //累计 累计上盘变化
        if (Objects.nonNull(newParam.getHomeMultiOddsRate()) &&
                !newParam.getHomeMultiOddsRate().equals(oriVo.getHomeMultiOddsRate())) {
            RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, oriVo.getHomeMultiOddsRate(), newParam.getHomeMultiOddsRate());
            templateLog.setParameterName(playName + "-" + timeValName + "-" + OperateLogEnum.HOME_MULTI_ODDS_RATE.getName());
            pushMessage(templateLog);
        }

        //累计 累计下盘变化
        if (Objects.nonNull(newParam.getAwayMultiOddsRate()) &&
                !newParam.getHomeMultiOddsRate().equals(oriVo.getAwayMultiOddsRate())) {
            RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, oriVo.getAwayMultiOddsRate(), newParam.getAwayMultiOddsRate());
            templateLog.setParameterName(playName + "-" + timeValName + "-" + OperateLogEnum.AWAY_MULTI_ODDS_RATE.getName());
            pushMessage(templateLog);
        }

        //单枪 限额值
        if (Objects.nonNull(newParam.getHomeSingleMaxAmount()) &&
                !newParam.getHomeSingleMaxAmount().equals(oriVo.getHomeSingleMaxAmount())) {
            RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, oriVo.getHomeSingleMaxAmount(), newParam.getHomeSingleMaxAmount());
            templateLog.setParameterName(playName + "-" + timeValName + "-" + OperateLogEnum.HOME_SINGLE_MAX_AMOUNT.getName());
            pushMessage(templateLog);
        }

        //上盘单枪赔率变化率
        if (Objects.nonNull(newParam.getHomeSingleOddsRate()) &&
                !newParam.getHomeSingleOddsRate().equals(oriVo.getHomeSingleOddsRate())) {
            RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, oriVo.getHomeSingleOddsRate(), newParam.getHomeSingleOddsRate());
            templateLog.setParameterName(playName + "-" + timeValName + "-" + OperateLogEnum.HOME_SINGLE_ODDS_RATE.getName());
            pushMessage(templateLog);
        }

        //下盘单枪赔率变化率
        if (Objects.nonNull(newParam.getAwaySingleOddsRate()) &&
                !newParam.getAwaySingleOddsRate().equals(oriVo.getAwaySingleOddsRate())) {
            RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, oriVo.getAwaySingleOddsRate(), newParam.getAwaySingleOddsRate());
            templateLog.setParameterName(playName + "-" + timeValName + "-" + OperateLogEnum.AWAY_SINGLE_ODDS_RATE.getName());
            pushMessage(templateLog);
        }

        //一级累计跳分 限额值
        if (Objects.nonNull(newParam.getHomeLevelFirstMaxAmount()) &&
                !newParam.getHomeLevelFirstMaxAmount().equals(oriVo.getHomeLevelFirstMaxAmount())) {
            RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, oriVo.getHomeLevelFirstMaxAmount(), newParam.getHomeLevelFirstMaxAmount());
            templateLog.setParameterName(playName + "-" + timeValName + "-" + OperateLogEnum.HOME_LEVEL_FIRST_MAX_AMOUNT.getName());
            pushMessage(templateLog);
        }

        //一级累计跳分 累计上盘变化
        if (Objects.nonNull(newParam.getHomeLevelFirstOddsRate()) &&
                !newParam.getHomeLevelFirstOddsRate().equals(oriVo.getHomeLevelFirstOddsRate())) {
            RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, oriVo.getHomeLevelFirstOddsRate(), newParam.getHomeLevelFirstOddsRate());
            templateLog.setParameterName(playName + "-" + timeValName + "-" + OperateLogEnum.HOME_LEVEL_FIRST_ODDS_RATE.getName());
            pushMessage(templateLog);
        }

        //一级累计跳分 累计下盘变化
        if (Objects.nonNull(newParam.getAwayLevelFirstOddsRate()) &&
                !newParam.getAwayLevelFirstOddsRate().equals(oriVo.getAwayLevelFirstOddsRate())) {
            RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, oriVo.getAwayLevelFirstOddsRate(), newParam.getAwayLevelFirstOddsRate());
            templateLog.setParameterName(playName + "-" + timeValName + "-" + OperateLogEnum.AWAY_LEVEL_FIRST_ODDS_RATE.getName());
            pushMessage(templateLog);
        }

        //二级累计跳分 限额值
        if (Objects.nonNull(newParam.getHomeLevelSecondMaxAmount()) &&
                !newParam.getHomeLevelSecondMaxAmount().equals(oriVo.getHomeLevelSecondMaxAmount())) {
            RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, oriVo.getHomeLevelSecondMaxAmount(), newParam.getHomeLevelSecondMaxAmount());
            templateLog.setParameterName(playName + "-" + timeValName + "-" + OperateLogEnum.HOME_LEVEL_SECOND_MAX_AMOUNT.getName());
            pushMessage(templateLog);
        }

        //二级累计跳分 累计上盘变化
        if (Objects.nonNull(newParam.getHomeLevelSecondOddsRate()) &&
                !newParam.getHomeLevelSecondOddsRate().equals(oriVo.getHomeLevelSecondOddsRate())) {
            RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, oriVo.getHomeLevelSecondOddsRate(), newParam.getHomeLevelSecondOddsRate());
            templateLog.setParameterName(playName + "-" + timeValName + "-" + OperateLogEnum.HOME_LEVEL_SECOND_ODDS_RATE.getName());
            pushMessage(templateLog);
        }

        //二级累计跳分 累计下盘变化
        if (Objects.nonNull(newParam.getAwayLevelSecondOddsRate()) &&
                !newParam.getAwayLevelSecondOddsRate().equals(oriVo.getAwayLevelSecondOddsRate())) {
            RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, oriVo.getAwayLevelSecondOddsRate(), newParam.getAwayLevelSecondOddsRate());
            templateLog.setParameterName(playName + "-" + timeValName + "-" + OperateLogEnum.AWAY_LEVEL_SECOND_ODDS_RATE.getName());
            pushMessage(templateLog);
        }

        //限额参数
        //单注投注/赔付限额
        if (Objects.nonNull(newParam.getOrderSinglePayVal()) &&
                !newParam.getOrderSinglePayVal().equals(oriVo.getOrderSinglePayVal())) {
            RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, oriVo.getOrderSinglePayVal(), newParam.getOrderSinglePayVal());
            templateLog.setParameterName(playName + "-" + timeValName + "-" + OperateLogEnum.ORDER_SINGLE_PAY_VAL.getName());
            pushMessage(templateLog);
        }

        //用户累计赔付限额
        if (Objects.nonNull(newParam.getUserMultiPayVal()) &&
                !newParam.getUserMultiPayVal().equals(oriVo.getUserMultiPayVal())) {
            RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, oriVo.getUserMultiPayVal(), newParam.getUserMultiPayVal());
            templateLog.setParameterName(playName + "-" + timeValName + "-" + OperateLogEnum.USER_MULTI_PAY_VAL.getName());
            pushMessage(templateLog);
        }

        //提前结算
        //提前结算开关
        if (Objects.nonNull(newParam.getCategoryPreStatus()) &&
                !newParam.getCategoryPreStatus().equals(oriVo.getCategoryPreStatus())) {
            RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, getOpenCloseName(oriVo.getCategoryPreStatus()), getOpenCloseName(newParam.getCategoryPreStatus()));
            templateLog.setParameterName(playName + "-" + timeValName + "-" + OperateLogEnum.CATEGORY_PRE_STATUS.getName());
            pushMessage(templateLog);
        }
        //CashOut Margin
        if (Objects.nonNull(newParam.getCashOutMargin()) &&
                !newParam.getCashOutMargin().equals(oriVo.getCashOutMargin())) {
            RcsOperateLog templateLog = prepareLogBean(rcsOperateLog, oriVo.getCashOutMargin(), newParam.getCashOutMargin());
            templateLog.setParameterName(playName + "-" + timeValName + "-" + OperateLogEnum.CASH_OUT_MARGIN.getName());

            pushMessage(templateLog);
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
     * 初始化-來源為聯賽模板
     *
     * @param rcsOperateLog
     * @param param
     */
    private void initalTemplateFormat(RcsOperateLog rcsOperateLog, TournamentTemplatePlayMargainRefParam param) {
        rcsOperateLog.setObjectIdByObj(param.getTemplateId());//模板ID
        String tournamentLevelTemplateName = getTournamentLevelTemplateName(param);
        rcsOperateLog.setObjectNameByObj(StringUtils.isNoneBlank(param.getTemplateName()) ? param.getTemplateName() : tournamentLevelTemplateName);
        rcsOperateLog.setExtObjectIdByObj(OperateLogEnum.NONE.getName());
        rcsOperateLog.setExtObjectNameByObj(OperateLogEnum.NONE.getName());
        rcsOperateLog.setBehavior(OperateLogEnum.TEMPLATE_UPDATE.getName());
    }

    /**
     * 初始化-來源為操盤設置
     *
     * @param rcsOperateLog
     * @param param
     */
    private void initalOperateFormat(RcsOperateLog rcsOperateLog, TournamentTemplatePlayMargainRefParam param) {
        rcsOperateLog.setObjectIdByObj(param.getPlayId());
        rcsOperateLog.setMatchId(param.getMatchId());
        rcsOperateLog.setPlayId(param.getPlayId().longValue());
        String playName = getPlayName(param.getPlayId().longValue(), param.getSportId());
        rcsOperateLog.setObjectNameByObj(playName);
        rcsOperateLog.setExtObjectIdByObj(param.getMatchManageId());
        rcsOperateLog.setExtObjectNameByObj(getMatchName(param.getTeamList()));
        rcsOperateLog.setBehavior(OperateLogEnum.OPERATE_SETTING.getName());

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

    private void pushMessage(RcsOperateLog rcsOperateLog) {
        sendMessage.sendMessage("rcs_log_operate", "", "", rcsOperateLog);
    }

    /**
     * 獲取預設模板名稱
     *
     * @param param
     * @return
     */
    private String getTournamentLevelTemplateName(TournamentTemplatePlayMargainRefParam param) {
        if (Objects.nonNull(param.getTypeVal())) {
            String levelName = NumberConventer.GetCH(param.getTypeVal().intValue());
            return StringUtils.isNoneBlank(levelName) ? levelName + "级联赛" + MatchTypeEnum.getNameById(param.getMatchType()) + "模板" : "";
        } else {
            return "";
        }
    }

    /**
     * TimeVal轉換
     *
     * @param param
     * @return
     */
    private String transTimeValName(TournamentTemplatePlayMargainRefParam param) {
        if (Objects.nonNull(param.getMatchType())) {
            switch (param.getMatchType()) {
                case 0:
                    //滾球
                    return inRunningMarketTimeVal(param);
                case 1:
                    //早盤
                    return earlyMarketTimeVal(param);
                default:
                    return String.valueOf(param.getTimeVal());
            }
        }
        return String.valueOf(param.getTimeVal());
    }

    /**
     * 早盤文字轉換
     *
     * @param param
     * @return
     */
    private String earlyMarketTimeVal(TournamentTemplatePlayMargainRefParam param) {
        Long hour = param.getTimeVal() / 3600;
        if (24 >= hour) {
            return hour + "H";
        } else {
            if (hour / 24 == 30) {
                return "开售";
            } else {
                return hour / 24 + "D";
            }
        }
    }

    /**
     * 滾球文字轉換
     *
     * @param param
     * @return
     */
    private String inRunningMarketTimeVal(TournamentTemplatePlayMargainRefParam param) {
        Long minute = param.getTimeVal() / 60;
        if (minute == 0) {
            return "开售";
        } else {
            return minute + "分钟";
        }
    }

    /**
     * 跳分機制轉換
     *
     * @param code
     * @return
     */
    private String getOddChangeRuleName(Integer code) {
        switch (code) {
            case 0:
                return "累计/单枪跳分";
            case 1:
                return "累计差值跳分";
            default:
                return "";
        }
    }

    /**
     * 累計差額計算方式
     *
     * @param code
     * @return
     */
    private String getBalanceOptionName(Integer code) {
        switch (code) {
            case 0:
                return "投注额差值";
            case 1:
                return "投注额/赔付混合差值";
            default:
                return "";
        }
    }

    /**
     * 轉換开关名稱
     *
     * @param status
     * @return
     */
    private String getOpenCloseName(Integer status) {
        switch (status) {
            case 0:
                return "关";
            case 1:
                return "开";
            default:
                return String.valueOf(status);
        }
    }
}
