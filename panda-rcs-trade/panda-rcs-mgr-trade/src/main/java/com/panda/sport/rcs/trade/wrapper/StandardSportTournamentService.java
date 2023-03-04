package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.StandardSportTournament;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * SR中对应tournament
 * BC中对应Competition
 * 服务类
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-09-03
 */
public interface StandardSportTournamentService extends IService<StandardSportTournament> {
    /**
     * @return java.util.List<com.panda.sport.rcs.pojo.StandardSportTournament>
     * @Description 查询
     * @Param [tournamentIds, beginDate, endDate, matchType, otherMorningMarke]
     * @Author toney
     * @Date 13:10 2020/3/19
     **/
    List<StandardSportTournament> queryByIdsAndBeginDateAndEndDateAndMatchType(@Param("tournamentIds") List<Long> tournamentIds, @Param("beginDate") Long beginDate, @Param("endDate") Long endDate, @Param("matchType") Integer matchType, @Param("otherMorningMarke") Integer otherMorningMarke);

    /**
     * @param tournamentName: 联赛名称
     * @Description: 根据名称，查询联赛
     * @Author carver
     * @Date 2020/10/20 20:47
     * @return: java.util.List<com.panda.sport.rcs.pojo.StandardSportTournament>
     **/
    List<StandardSportTournament> queryTournamentByName(Integer sportId,String tournamentName);
}
