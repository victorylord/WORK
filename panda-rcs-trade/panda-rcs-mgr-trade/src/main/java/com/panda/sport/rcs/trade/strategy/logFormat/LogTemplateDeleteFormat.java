package com.panda.sport.rcs.trade.strategy.logFormat;

import com.panda.sport.rcs.enums.OperateLogEnum;
import com.panda.sport.rcs.log.format.RcsOperateLog;
import com.panda.sport.rcs.trade.param.TournamentTemplateParam;
import com.panda.sport.rcs.trade.strategy.LogFormatStrategy;
import org.springframework.stereotype.Service;

/**
 * 操盤日誌
 * 联赛模板日志需求-模板删除
 */
@Service
public class LogTemplateDeleteFormat extends LogFormatStrategy {
    @Override
    public RcsOperateLog formatLogBean(RcsOperateLog rcsOperateLog, Object[] args) {
        TournamentTemplateParam param = (TournamentTemplateParam) args[0];

        //根據不同操作頁面組裝不同格式
        switch (param.getOperatePageCode()) {
            case 21:
                //联赛参数设置
                return templateDeleteFormat(rcsOperateLog, param);
        }
        return null;
    }

    private RcsOperateLog templateDeleteFormat(RcsOperateLog rcsOperateLog, TournamentTemplateParam param) {
        rcsOperateLog.setOperatePageCode(param.getOperatePageCode());
        rcsOperateLog.setObjectIdByObj(param.getId());
        rcsOperateLog.setObjectNameByObj(param.getTemplateName());
        rcsOperateLog.setExtObjectIdByObj(OperateLogEnum.NONE.getName());
        rcsOperateLog.setExtObjectNameByObj(OperateLogEnum.NONE.getName());
        rcsOperateLog.setBeforeValByObj(OperateLogEnum.NONE.getName());
        rcsOperateLog.setAfterValByObj("刪除");

        return rcsOperateLog;
    }

}
