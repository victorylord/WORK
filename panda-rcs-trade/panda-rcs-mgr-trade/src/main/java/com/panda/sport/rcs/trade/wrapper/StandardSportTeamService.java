package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.StandardSportTeam;
import com.panda.sport.rcs.vo.MatchMarketLiveOddsVo;
import com.panda.sport.rcs.vo.SportTeam;

import java.util.List;

/**
 * <p>
 * 标准球队信息表.
 * 球队id 与比赛id 作为唯一性约束 服务类
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-09-03
 */
public interface StandardSportTeamService extends IService<StandardSportTeam> {
    List<SportTeam> queryTeamsByMatchId(Long matchId);

    /**
     * 根据match 获取队伍名称多语言
     * @param matchId
     * @return
     */
    List<MatchMarketLiveOddsVo.MatchMarketTeamVo> queryTeamList(Long matchId);
}
