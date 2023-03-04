package com.panda.sport.rcs.trade.strategy.logFormat;

import com.panda.sport.rcs.enums.OperateLogEnum;
import com.panda.sport.rcs.log.format.RcsOperateLog;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateAcceptConfig;
import com.panda.sport.rcs.trade.strategy.LogFormatStrategy;
import org.springframework.stereotype.Service;

/**
 * 操盤日誌(copyEventAndTimeConfig)
 * 联赛模板日志-接拒单玩法集事件复制功能
 */
@Service
public class LogCopyEventAndTimeConfigFormat extends LogFormatStrategy {

    @Override
    public RcsOperateLog formatLogBean(RcsOperateLog rcsOperateLog, Object[] args) {
        RcsTournamentTemplateAcceptConfig config = (RcsTournamentTemplateAcceptConfig) args[0];

        switch (config.getOperatePageCode()) {
            case 21:
                return copyEventAndTimeConfigFormat(rcsOperateLog, config);
        }

        return null;
    }

    /**
     * 初始化-來源為聯賽模板設置
     *
     * @param rcsOperateLog
     * @param param
     */
    private RcsOperateLog copyEventAndTimeConfigFormat(RcsOperateLog rcsOperateLog, RcsTournamentTemplateAcceptConfig param) {
        rcsOperateLog.setOperatePageCode(param.getOperatePageCode());
        rcsOperateLog.setObjectIdByObj(param.getTemplateId());//模板ID
        rcsOperateLog.setObjectNameByObj(param.getTemplateName());//模板名稱
        rcsOperateLog.setExtObjectIdByObj(OperateLogEnum.NONE.getName());
        rcsOperateLog.setExtObjectNameByObj(OperateLogEnum.NONE.getName());
        rcsOperateLog.setBehavior(OperateLogEnum.TEMPLATE_UPDATE.getName());
        rcsOperateLog.setParameterName(param.getCategorySetName() + "-" + OperateLogEnum.COPY_EVENT_AND_TIME_CONFIG.getName());
        rcsOperateLog.setBeforeValByObj(OperateLogEnum.NONE.getName());
        rcsOperateLog.setAfterValByObj(param.getCopyCategorySetName());


        return rcsOperateLog;
    }

}
