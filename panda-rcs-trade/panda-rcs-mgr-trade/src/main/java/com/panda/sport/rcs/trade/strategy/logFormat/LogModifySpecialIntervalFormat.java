package com.panda.sport.rcs.trade.strategy.logFormat;

import com.panda.sport.rcs.enums.OperateLogEnum;
import com.panda.sport.rcs.log.format.RcsOperateLog;
import com.panda.sport.rcs.mapper.RcsLanguageInternationMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.LanguageInternation;
import com.panda.sport.rcs.trade.param.TournamentTemplatePlayMargainParam;
import com.panda.sport.rcs.trade.strategy.LogFormatStrategy;
import com.panda.sport.rcs.utils.CategoryParseUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 操盤日誌(modifySpecialInterval)
 * 設置-盤口參數調整-特殊抽水
 */
@Service
public class LogModifySpecialIntervalFormat extends LogFormatStrategy {
    
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

        modifySpecialIntervalFormat(rcsOperateLog, param);

        return null;
    }

    private void modifySpecialIntervalFormat(RcsOperateLog rcsOperateLog, TournamentTemplatePlayMargainParam param) {
        String playName = getPlayName(param.getPlayId().longValue(), param.getSportId());
        rcsOperateLog.setMatchId(param.getMatchId());
        rcsOperateLog.setPlayId(param.getPlayId().longValue());
        rcsOperateLog.setObjectIdByObj(param.getPlayId());
        rcsOperateLog.setObjectNameByObj(playName);
        rcsOperateLog.setExtObjectIdByObj(param.getMatchManageId());
        rcsOperateLog.setExtObjectNameByObj(getMatchName(param.getTeamList()));

        //是否特殊抽水
        if (!param.getIsSpecialPumping().equals(param.getBeforeParams().getIsSpecialPumping())) {
            RcsOperateLog logBean = new RcsOperateLog();
            BeanUtils.copyProperties(rcsOperateLog, logBean);
            logBean.setParameterName(playName + "-" + OperateLogEnum.SPECIAL_PUMPING.getName() + "-" + OperateLogEnum.IS_SPECIAL_PUMPING.getName());
            logBean.setBeforeValByObj(getTrueFalse(param.getBeforeParams().getIsSpecialPumping()));
            logBean.setAfterValByObj(getTrueFalse(param.getIsSpecialPumping()));
            pushMessage(logBean);
        }

        //特殊抽水赔率区间(Malay Spread)
        if (!param.getSpecialOddsInterval().equals(param.getBeforeParams().getSpecialOddsInterval())) {
            RcsOperateLog logBean = new RcsOperateLog();
            BeanUtils.copyProperties(rcsOperateLog, logBean);
            logBean.setParameterName(playName + "-" + OperateLogEnum.SPECIAL_PUMPING.getName() + "-" + OperateLogEnum.SPECIAL_ODDS_INTERVAL.getName());
            logBean.setBeforeValByObj(param.getBeforeParams().getSpecialOddsInterval());
            logBean.setAfterValByObj(param.getSpecialOddsInterval());
            pushMessage(logBean);
        }
        //特殊抽水赔率区间状态
        if (!param.getSpecialOddsIntervalStatus().equals(param.getBeforeParams().getSpecialOddsIntervalStatus())) {
            RcsOperateLog logBean = new RcsOperateLog();
            BeanUtils.copyProperties(rcsOperateLog, logBean);
            logBean.setParameterName(playName + "-" + OperateLogEnum.SPECIAL_PUMPING.getName() + "-" + OperateLogEnum.SPECIAL_ODDS_INTERVAL_STATUS.getName());
            logBean.setBeforeValByObj(param.getBeforeParams().getSpecialOddsIntervalStatus());
            logBean.setAfterValByObj(param.getSpecialOddsIntervalStatus());
            pushMessage(logBean);

        }
        //低赔特殊抽水赔率区间
        if (!param.getSpecialOddsIntervalLow().equals(param.getBeforeParams().getSpecialOddsIntervalLow())) {
            RcsOperateLog logBean = new RcsOperateLog();
            BeanUtils.copyProperties(rcsOperateLog, logBean);
            logBean.setParameterName(playName + "-" + OperateLogEnum.SPECIAL_PUMPING.getName() + "-" + OperateLogEnum.SPECIAL_ODDS_INTERVAL_LOW.getName());
            logBean.setBeforeValByObj(param.getBeforeParams().getSpecialOddsIntervalStatus());
            logBean.setAfterValByObj(param.getSpecialOddsIntervalStatus());
            pushMessage(logBean);
        }
        //高赔特殊抽水赔率区间
        if (!param.getSpecialOddsIntervalHigh().equals(param.getBeforeParams().getSpecialOddsIntervalHigh())) {
            RcsOperateLog logBean = new RcsOperateLog();
            BeanUtils.copyProperties(rcsOperateLog, logBean);
            logBean.setParameterName(playName + "-" + OperateLogEnum.SPECIAL_PUMPING.getName() + "-" + OperateLogEnum.SPECIAL_ODDS_INTERVAL_HIGH.getName());
            logBean.setBeforeValByObj(param.getBeforeParams().getSpecialOddsIntervalStatus());
            logBean.setAfterValByObj(param.getSpecialOddsIntervalStatus());
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

    private void pushMessage(RcsOperateLog rcsOperateLog) {
        sendMessage.sendMessage("rcs_log_operate", "", "", rcsOperateLog);
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
