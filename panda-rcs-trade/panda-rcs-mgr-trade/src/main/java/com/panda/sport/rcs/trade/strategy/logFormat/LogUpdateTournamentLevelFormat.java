package com.panda.sport.rcs.trade.strategy.logFormat;

import com.panda.sport.rcs.enums.OperateLogEnum;
import com.panda.sport.rcs.log.format.RcsOperateLog;
import com.panda.sport.rcs.mapper.RcsLanguageInternationMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.LanguageInternation;
import com.panda.sport.rcs.trade.param.UpdateTournamentLevelParam;
import com.panda.sport.rcs.trade.strategy.LogFormatStrategy;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 操盤日誌(updateTournamentLevel)
 * 聯賽模板-联赛属性
 */
@Service
public class LogUpdateTournamentLevelFormat extends LogFormatStrategy {
    @Autowired
    private ProducerSendMessageUtils sendMessage;
    @Autowired
    private RcsLanguageInternationMapper rcsLanguageInternationMapper;

    @Override
    public RcsOperateLog formatLogBean(RcsOperateLog rcsOperateLog, Object[] args) {
        UpdateTournamentLevelParam param = (UpdateTournamentLevelParam) args[0];

        //根據不同操作頁面組裝不同格式
        switch (param.getOperatePageCode()) {
            case 21:
                //联赛参数设置
                updateTournamentConfigFormat(rcsOperateLog, param);
                break;
        }
        return null;
    }

    private void updateTournamentConfigFormat(RcsOperateLog rcsOperateLog, UpdateTournamentLevelParam param) {
        //設置共用LogBean內容
        rcsOperateLog.setOperatePageCode(param.getOperatePageCode());
        rcsOperateLog.setObjectIdByObj(param.getId());
        rcsOperateLog.setObjectNameByObj(getTournamentName(param.getId(), Math.toIntExact(param.getSportId())));
        rcsOperateLog.setExtObjectIdByObj(OperateLogEnum.NONE.getName());
        rcsOperateLog.setExtObjectNameByObj(OperateLogEnum.NONE.getName());

        //等级选择
        if (Objects.nonNull(param.getLevel()) &&
                param.getLevel().compareTo(param.getBeforeParams().getLevel()) != 0) {
            RcsOperateLog logBean = initialLogBean(rcsOperateLog, param.getBeforeParams().getLevel(), param.getLevel());
            logBean.setParameterName(OperateLogEnum.TOURNAMENT_LEVEL.getName());
            pushMessage(logBean);
        }

        //是否热门
        if (Objects.nonNull(param.getIsPopular()) &&
                param.getIsPopular().compareTo(param.getBeforeParams().getIsPopular()) != 0) {
            RcsOperateLog logBean = initialLogBean(rcsOperateLog, getTrueFalse(param.getBeforeParams().getIsPopular()), getTrueFalse(param.getIsPopular()));
            logBean.setParameterName(OperateLogEnum.IS_POPULAR.getName());
            pushMessage(logBean);
        }
        //目标咬度
        if (Objects.nonNull(param.getTargetProfitRate()) &&
                param.getTargetProfitRate().compareTo(param.getBeforeParams().getTargetProfitRate()) != 0) {
            RcsOperateLog logBean = initialLogBean(rcsOperateLog, param.getBeforeParams().getTargetProfitRate().stripTrailingZeros(), param.getTargetProfitRate().stripTrailingZeros());
            logBean.setParameterName(OperateLogEnum.TARGET_PROFIT_RATE.getName());
            pushMessage(logBean);
        }
    }

    private RcsOperateLog initialLogBean(RcsOperateLog sample, Object before, Object after) {
        RcsOperateLog rcsOperateLog = new RcsOperateLog();
        BeanUtils.copyProperties(sample, rcsOperateLog);
        rcsOperateLog.setBeforeValByObj(before);
        rcsOperateLog.setAfterValByObj(after);
        return rcsOperateLog;
    }

    /**
     * 查詢聯賽名稱
     *
     * @param tournamentId
     * @param sportId
     * @return
     */
    private String getTournamentName(Long tournamentId, Integer sportId) {
        LanguageInternation tournamentName = rcsLanguageInternationMapper.getTournamentNameByIdAndSprotId(tournamentId, sportId);
        return Objects.nonNull(tournamentName) ? tournamentName.getText() : "";
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
