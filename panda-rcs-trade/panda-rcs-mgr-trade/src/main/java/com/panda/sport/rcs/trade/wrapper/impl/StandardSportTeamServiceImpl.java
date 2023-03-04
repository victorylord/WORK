package com.panda.sport.rcs.trade.wrapper.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.panda.sport.rcs.mapper.StandardSportTeamMapper;
import com.panda.sport.rcs.pojo.MatchStatisticsInfoDetail;
import com.panda.sport.rcs.pojo.StandardSportTeam;
import com.panda.sport.rcs.trade.wrapper.RcsLanguageInternationService;
import com.panda.sport.rcs.trade.wrapper.StandardSportTeamService;
import com.panda.sport.rcs.vo.MatchMarketLiveOddsVo;
import com.panda.sport.rcs.vo.SportTeam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * <p>
 * 标准球队信息表.
 * 球队id 与比赛id 作为唯一性约束 服务实现类
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-09-03
 */
@Service
public class StandardSportTeamServiceImpl extends ServiceImpl<StandardSportTeamMapper, StandardSportTeam> implements StandardSportTeamService {

    @Autowired
    private StandardSportTeamMapper sportTeamMapper;

	@Autowired
    private RcsLanguageInternationService rcsLanguageInternationService;

    @Autowired
    private MatchStatisticsInfoDetailServiceImpl matchStatisticsInfoDetailService;

    @Override
    public List<SportTeam> queryTeamsByMatchId(Long matchId) {
        return sportTeamMapper.selectTeamsByMatchId(matchId);
    }

    @Override
    public List<MatchMarketLiveOddsVo.MatchMarketTeamVo> queryTeamList(Long matchId) {
        List<SportTeam> sportTeams = this.queryTeamsByMatchId(matchId);
        List<MatchMarketLiveOddsVo.MatchMarketTeamVo> teamList = Lists.newArrayListWithCapacity(sportTeams.size());
        if (!CollectionUtils.isEmpty(sportTeams)) {
            MatchStatisticsInfoDetail matchStatisticsInfoDetail = matchStatisticsInfoDetailService.slectRedScore(matchId);
            for (SportTeam sourceTeam : sportTeams) {
                MatchMarketLiveOddsVo.MatchMarketTeamVo team = new MatchMarketLiveOddsVo.MatchMarketTeamVo();
                team.setNameCode(sourceTeam.getNameCode());
                // 国际化
                team.setNames(rcsLanguageInternationService.getCachedNamesMapByCode(sourceTeam.getNameCode()));
                team.setMatchPosition(sourceTeam.getMatchPosition());
                if (matchStatisticsInfoDetail != null) {
                    if ("home".equals(team.getMatchPosition())) {
                        team.setRedCardNum(matchStatisticsInfoDetail.getT1());
                    } else {
                        team.setRedCardNum(matchStatisticsInfoDetail.getT2());
                    }
                }
                teamList.add(team);
            }
        }
        return teamList;
    }
}
