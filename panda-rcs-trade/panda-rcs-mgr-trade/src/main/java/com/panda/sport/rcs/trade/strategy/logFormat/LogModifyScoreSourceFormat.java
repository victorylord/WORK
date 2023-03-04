package com.panda.sport.rcs.trade.strategy.logFormat;


import com.panda.sport.rcs.enums.OperateLogEnum;
import com.panda.sport.rcs.log.format.RcsOperateLog;
import com.panda.sport.rcs.mapper.StandardMatchTeamRelationMapper;
import com.panda.sport.rcs.trade.enums.ScoreSourceEnum;
import com.panda.sport.rcs.trade.param.TournamentTemplateUpdateParam;
import com.panda.sport.rcs.trade.strategy.LogFormatStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 操盤日誌(modifyTemplate)
 * 操盤設置-比分源
 */
@Service
public class LogModifyScoreSourceFormat extends LogFormatStrategy {

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
            case 17:
                //滚球操盘-设置
                rcsOperateLog.setOperatePageCode(111);
            default:
                rcsOperateLog.setOperatePageCode(param.getOperatePageCode());
        }
        return modifyScoreSourceFormat(rcsOperateLog, param);
    }

    private RcsOperateLog modifyScoreSourceFormat(RcsOperateLog rcsOperateLog, TournamentTemplateUpdateParam param) {
        if ((param.getScoreSource() == param.getBeforeParams().getScoreSource()) ||
                (Objects.nonNull(param.getScoreSource()) && param.getScoreSource().compareTo(param.getBeforeParams().getScoreSource()) != 0))
        {
            rcsOperateLog.setMatchId(param.getMatchId());
            rcsOperateLog.setObjectIdByObj(param.getMatchManageId());
            rcsOperateLog.setObjectIdByObj(param.getMatchManageId());
            rcsOperateLog.setObjectNameByObj(queryMatchName(Math.toIntExact(param.getMatchId())));
            rcsOperateLog.setExtObjectIdByObj(OperateLogEnum.NONE.getName());
            rcsOperateLog.setExtObjectNameByObj(OperateLogEnum.NONE.getName());
            rcsOperateLog.setParameterName(OperateLogEnum.SCORE_SOURCE.getName());
            rcsOperateLog.setBeforeValByObj(transScoreSource(param.getBeforeParams().getScoreSource()));
            rcsOperateLog.setAfterValByObj(transScoreSource(param.getScoreSource()));
            return rcsOperateLog;
        }
        return null;
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
}
