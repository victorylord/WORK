package com.panda.sport.rcs.trade.strategy.logFormat;

import com.panda.sport.rcs.enums.OperateLogEnum;
import com.panda.sport.rcs.log.format.RcsOperateLog;
import com.panda.sport.rcs.mapper.RcsLanguageInternationMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.LanguageInternation;
import com.panda.sport.rcs.trade.param.UpdateTournamentTemplateParam;
import com.panda.sport.rcs.trade.strategy.LogFormatStrategy;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 操盤日誌(updateTournamentTemplate)
 * 联赛模板日志-模板选择
 */
@Service
public class LogTemplateSelectionFormat extends LogFormatStrategy {

    @Autowired
    private ProducerSendMessageUtils sendMessage;
    @Autowired
    private RcsLanguageInternationMapper rcsLanguageInternationMapper;

    @Override
    public RcsOperateLog formatLogBean(RcsOperateLog rcsOperateLog, Object[] args) {
        UpdateTournamentTemplateParam param = (UpdateTournamentTemplateParam) args[0];

        //根據不同操作頁面組裝不同格式
        switch (param.getOperatePageCode()) {
            case 21:
                //联赛参数设置
                templateSelectionFormat(rcsOperateLog, param);
                break;
        }
        return null;
    }

    private void templateSelectionFormat(RcsOperateLog rcsOperateLog, UpdateTournamentTemplateParam param) {
        //設置共用LogBean內容
        rcsOperateLog.setOperatePageCode(param.getOperatePageCode());
        rcsOperateLog.setObjectIdByObj(param.getId());
        rcsOperateLog.setObjectNameByObj(getTournamentName(param.getId(), param.getSportId()));
        rcsOperateLog.setExtObjectIdByObj(OperateLogEnum.NONE.getName());
        rcsOperateLog.setExtObjectNameByObj(OperateLogEnum.NONE.getName());

        //早盘操盘参数模板
        if (Objects.nonNull(param.getPreLemplateName()) &&
                !param.getPreLemplateName().equals(param.getBeforeParams().getPreLemplateName())) {
            RcsOperateLog logBean = initialLogBean(rcsOperateLog, param.getBeforeParams().getPreLemplateName(), param.getPreLemplateName());
            logBean.setParameterName(OperateLogEnum.PRE_TEMPLATE.getName());
            pushMessage(logBean);
        }

        //滚球操盘参数模板
        if (Objects.nonNull(param.getLiveLemplateName()) &&
                !param.getLiveLemplateName().equals(param.getBeforeParams().getLiveLemplateName())) {
            RcsOperateLog logBean = initialLogBean(rcsOperateLog, param.getBeforeParams().getLiveLemplateName(), param.getLiveLemplateName());
            logBean.setParameterName(OperateLogEnum.LIVE_TEMPLATE.getName());
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
}
