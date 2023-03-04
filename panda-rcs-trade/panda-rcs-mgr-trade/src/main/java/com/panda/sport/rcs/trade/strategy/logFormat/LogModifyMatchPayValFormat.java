package com.panda.sport.rcs.trade.strategy.logFormat;

import com.panda.sport.rcs.enums.OperateLogEnum;
import com.panda.sport.rcs.log.format.RcsOperateLog;
import com.panda.sport.rcs.trade.param.TournamentTemplateUpdateParam;
import com.panda.sport.rcs.trade.strategy.LogFormatStrategy;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 操盤日誌(modifyMatchPayVal)
 * 設置-商戶/用戶 單場賠付限額
 */
@Service
public class LogModifyMatchPayValFormat extends LogFormatStrategy {
    @Override
    public RcsOperateLog formatLogBean(RcsOperateLog rcsOperateLog, Object[] args) {
        TournamentTemplateUpdateParam param = (TournamentTemplateUpdateParam) args[0];

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
        rcsOperateLog.setObjectIdByObj(param.getMatchManageId());
        rcsOperateLog.setObjectNameByObj(getMatchName(param.getTeamList()));
        rcsOperateLog.setExtObjectIdByObj(OperateLogEnum.NONE.getName());
        rcsOperateLog.setExtObjectNameByObj(OperateLogEnum.NONE.getName());

        if (Objects.nonNull(param.getBusinesMatchPayVal())) {
            return updateBusinesMatchPayFormat(rcsOperateLog, param);
        } else if (Objects.nonNull(param.getUserMatchPayVal())) {
            return updateUserMatchPayFormat(rcsOperateLog, param);
        } else {
            return null;
        }
    }

    private RcsOperateLog updateBusinesMatchPayFormat(RcsOperateLog rcsOperateLog, TournamentTemplateUpdateParam param) {
        if (!param.getBusinesMatchPayVal().equals(param.getBeforeParams().getBusinesMatchPayVal())) {
            rcsOperateLog.setParameterName(OperateLogEnum.BUSINES_MATCH_PAY_VAL.getName());
            rcsOperateLog.setBeforeValByObj(param.getBeforeParams().getBusinesMatchPayVal());
            rcsOperateLog.setAfterValByObj(param.getBusinesMatchPayVal());
            return rcsOperateLog;
        }
        return null;
    }

    private RcsOperateLog updateUserMatchPayFormat(RcsOperateLog rcsOperateLog, TournamentTemplateUpdateParam param) {
        if (!param.getUserMatchPayVal().equals(param.getBeforeParams().getUserMatchPayVal())) {
            rcsOperateLog.setParameterName(OperateLogEnum.USER_MATCH_PAY_VAL.getName());
            rcsOperateLog.setBeforeValByObj(param.getBeforeParams().getUserMatchPayVal());
            rcsOperateLog.setAfterValByObj(param.getUserMatchPayVal());
            return rcsOperateLog;
        }
        return null;
    }
}
