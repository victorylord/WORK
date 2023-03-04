package com.panda.sport.rcs.trade.strategy.logFormat;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.panda.sport.rcs.enums.SportIdEnum;
import com.panda.sport.rcs.log.format.RcsOperateLog;
import com.panda.sport.rcs.mapper.StandardSportTeamMapper;
import com.panda.sport.rcs.mapper.tourTemplate.RcsTournamentTemplateMapper;
import com.panda.sport.rcs.pojo.MatchTeamInfo;
import com.panda.sport.rcs.pojo.tourTemplate.AoParameterTemplateReq;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplate;
import com.panda.sport.rcs.trade.strategy.LogFormatStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 操盤日誌(updateMarketOddsValue)
 * 調整賠率格式化類別
 */
@Service
@Slf4j
public class LogAoCsChangeFormat extends LogFormatStrategy {

    @Resource
    private RcsTournamentTemplateMapper rcsTournamentTemplateMapper;
    @Resource
    private StandardSportTeamMapper standardSportTeamMapper;

    @Override
    public RcsOperateLog formatLogBean(RcsOperateLog rcsOperateLog, Object[] args) {
        AoParameterTemplateReq aoParameterTemplateReq = (AoParameterTemplateReq) args[0];
        Long matchId = aoParameterTemplateReq.getMatchId();
        Integer matchType = aoParameterTemplateReq.getMatchType();
        Integer sportId = aoParameterTemplateReq.getSportId();
        if (!SportIdEnum.isBasketball(Long.valueOf(sportId))) {
            return null;
        }
        Long templateId = aoParameterTemplateReq.getTemplateId();
        QueryWrapper<RcsTournamentTemplate> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(RcsTournamentTemplate::getId, templateId);
        RcsTournamentTemplate tournamentTemplate = rcsTournamentTemplateMapper.selectOne(queryWrapper);
        List<MatchTeamInfo> teamList = standardSportTeamMapper.queryTeamListByMatchId(matchId);
        String matchName = getMatchName(teamList);
        rcsOperateLog.setOperatePageCode(15);
        rcsOperateLog.setMatchId(matchId);
        rcsOperateLog.setObjectIdByObj(matchId);
        rcsOperateLog.setObjectNameByObj(matchName);
        rcsOperateLog.setExtObjectIdByObj(templateId);
        rcsOperateLog.setExtObjectNameByObj(convertMatchType(matchType, sportId));
        rcsOperateLog.setBeforeValByObj(tournamentTemplate.getAoConfigValue());
        rcsOperateLog.setAfterValByObj(aoParameterTemplateReq.getAoConfigValue());
        rcsOperateLog.setParameterName("AO参数修改");
        return rcsOperateLog;
    }

    /**
     * 获取赛事类型 1 ：早盘 ，2： 滚球盘， 3： 冠军盘
     */
    private String convertMatchType(Integer matchType, Integer sportId) {
        String objectName = null;
        if (1 == matchType) {
            objectName = "早盘";
        } else if (2 == matchType) {
            objectName = "滚球盘";
        } else if (3 == matchType) {
            objectName = "冠军盘";
        }
        return SportIdEnum.getBySportId(Long.valueOf(sportId)).getName() + "-" + objectName;
    }
}



