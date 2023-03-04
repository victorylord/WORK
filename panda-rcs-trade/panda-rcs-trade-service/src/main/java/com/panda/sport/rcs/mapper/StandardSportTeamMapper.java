package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.MatchTeamInfo;
import com.panda.sport.rcs.pojo.StandardSportTeam;
import com.panda.sport.rcs.vo.MatchTeamLinkVo;
import com.panda.sport.rcs.vo.SportMarketOddsQueryVo;
import com.panda.sport.rcs.vo.SportTeam;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 标准球队信息表.
球队id 与比赛id 作为唯一性约束 Mapper 接口
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-09-03
 */
@Repository
public interface StandardSportTeamMapper extends BaseMapper<StandardSportTeam> {

    List<SportTeam> selectTeamsByMatchId(Long matchId);

    /**
     * 根据赛事id查询球队信息
     * @param matchIds
     * @return
     */
    List<MatchTeamLinkVo> queryTeamByMatchIds(@Param("matchIds") List<Long> matchIds);

    List<MatchTeamInfo> queryTeamListByMatchId(@Param("matchId") Long matchId);
}
