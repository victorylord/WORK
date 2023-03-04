package com.panda.sport.rcs.trade.strategy.logFormat;

import com.panda.sport.rcs.enums.OperateLogEnum;
import com.panda.sport.rcs.log.format.RcsOperateLog;
import com.panda.sport.rcs.trade.strategy.LogFormatStrategy;
import com.panda.sport.rcs.vo.StandardMarketSellQueryV2Vo;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 操盤日誌(matchChangeStatusSource)
 * 切換數據源 賽事狀態源
 */
@Service
public class LogChangeStatusSourceFormat extends LogFormatStrategy {
    @Override
    public RcsOperateLog formatLogBean(RcsOperateLog rcsOperateLog, Object[] args) {
        if(args == null || args.length == 0){return null;}
        StandardMarketSellQueryV2Vo vo = (StandardMarketSellQueryV2Vo) args[0];
        if(vo == null || vo.getOperatePageCode() == null){return null;}

        //根據不同操作頁面組裝不同格式
        switch (vo.getOperatePageCode()) {
            case 13:
                //早盤賽事
            case 16:
                //滾球賽事
                return ChangeStatusFormat(rcsOperateLog, vo);
        }
        return null;
    }

    private RcsOperateLog ChangeStatusFormat(RcsOperateLog rcsOperateLog, StandardMarketSellQueryV2Vo vo) {
        StandardMarketSellQueryV2Vo oriVo = vo.getBeforeParams();

        if (Objects.nonNull(vo.getDataSouceCode()) &&
                vo.getDataSouceCode().compareTo(oriVo.getDataSouceCode()) != 0) {
            rcsOperateLog.setOperatePageCode(vo.getOperatePageCode());
            rcsOperateLog.setMatchId(vo.getMatchId());
            rcsOperateLog.setObjectIdByObj(vo.getMatchManageId());
            rcsOperateLog.setObjectNameByObj(getMatchName(vo.getTeamList()));
            rcsOperateLog.setExtObjectIdByObj(OperateLogEnum.NONE.getName());
            rcsOperateLog.setExtObjectNameByObj(OperateLogEnum.NONE.getName());
            rcsOperateLog.setBeforeValByObj(oriVo.getDataSouceCode());
            rcsOperateLog.setAfterValByObj(vo.getDataSouceCode());
            return rcsOperateLog;
        }
        return null;
    }

}
