package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.StandardSportTournament;
import com.panda.sport.rcs.pojo.TournamentLevelTemporaryLog;
import com.panda.sport.rcs.vo.TournamentVagueVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * SR中对应tournament
 * BC中对应Competition
 * Mapper 接口
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-09-03
 */
@Repository
public interface StandardSportTournamentMapper extends BaseMapper<StandardSportTournament> {
    /**
     * @return java.util.List<com.panda.sport.rcs.pojo.StandardSportTournament>
     * @Description 查询
     * @Param [tournamentIds, beginDate, endDate, matchType, otherMorningMarke]
     * @Author toney
     * @Date 13:11 2020/3/19
     **/
    List<StandardSportTournament> queryByIdsAndBeginDateAndEndDateAndMatchType(@Param("tournamentIds") List<Long> tournamentIds, @Param("beginDate") Long beginDate, @Param("endDate") Long endDate, @Param("matchType") Integer matchType, @Param("otherMorningMarke") Integer otherMorningMarke);

    /**
     * @param tournamentName: 联赛名称
     * @param sportId:        赛种
     * @Description: 根据名称，查询联赛
     * @Author carver
     * @Date 2020/10/20 20:52
     * @return: java.util.List<com.panda.sport.rcs.pojo.StandardSportTournament>
     **/
    List<StandardSportTournament> queryTournamentByName(@Param("sportId") Integer sportId, @Param("tournamentName") String tournamentName);

    /**
     * 根据名字模糊查询
     * @param sportId
     * @param tournamentName
     * @return
     */
    List<TournamentVagueVo> selectTournamentByVagueName(@Param("sportId") Integer sportId, @Param("tournamentName") String tournamentName);

    /**
     * 修改联赛等级时存临时日志
     * @param log
     * @return
     */
    int insertTournamentLevelUpdateLog(TournamentLevelTemporaryLog log);
}
