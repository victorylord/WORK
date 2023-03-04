package com.panda.sport.rcs.trade.strategy.logFormat;

import com.panda.sport.rcs.enums.OperateLogEnum;
import com.panda.sport.rcs.log.format.RcsOperateLog;
import com.panda.sport.rcs.mapper.StandardMatchTeamRelationMapper;
import com.panda.sport.rcs.trade.param.TournamentTemplateUpdateParam;
import com.panda.sport.rcs.trade.strategy.LogFormatStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 操盤日誌(modifySettleSwitch)
 * 設置-提前結算開關
 */
@Service
public class LogModifySettleSwitchFormat extends LogFormatStrategy {

    @Autowired
    private StandardMatchTeamRelationMapper matchTeamRelationMapper;

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
        }
        rcsOperateLog.setMatchId(param.getMatchId());

        return modifySettleFormat(rcsOperateLog, param);
    }

    private RcsOperateLog modifySettleFormat(RcsOperateLog rcsOperateLog, TournamentTemplateUpdateParam param) {
        if (Objects.nonNull(param.getMatchPreStatus()) &&
                param.getMatchPreStatus().compareTo(param.getBeforeParams().getMatchPreStatus()) != 0) {
            rcsOperateLog.setObjectIdByObj(param.getMatchManageId());
            rcsOperateLog.setObjectNameByObj(queryMatchName(Math.toIntExact(param.getMatchId())));
            rcsOperateLog.setExtObjectIdByObj(OperateLogEnum.NONE.getName());
            rcsOperateLog.setExtObjectNameByObj(OperateLogEnum.NONE.getName());
            rcsOperateLog.setBeforeValByObj(getStatusName(param.getBeforeParams().getMatchPreStatus()));
            rcsOperateLog.setAfterValByObj(getStatusName(param.getMatchPreStatus()));
            return rcsOperateLog;
        }
        return null;
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
     * 用賽事ID查詢比賽名稱
     *
     * @param matchId
     * @return
     */
    private String queryMatchName(Integer matchId) {
        List<Map<String, Object>> teamList = matchTeamRelationMapper.selectByMatchId(matchId);
        String home = "", away = "";
        for (Map<String, Object> map : teamList) {
            String position = Optional.ofNullable(map.get("match_position")).orElse("").toString();
            String text = Optional.ofNullable(map.get("text")).orElse("").toString();
            if ("home".equals(position)) {
                home = text;
            } else if ("away".equals(position)) {
                away = text;
            }
        }
        return home + " VS " + away;
    }
}
