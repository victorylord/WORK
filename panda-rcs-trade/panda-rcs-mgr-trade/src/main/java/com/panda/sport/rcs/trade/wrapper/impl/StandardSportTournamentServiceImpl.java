package com.panda.sport.rcs.trade.wrapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.mapper.StandardSportTournamentMapper;
import com.panda.sport.rcs.pojo.StandardSportTournament;
import com.panda.sport.rcs.trade.wrapper.StandardSportTournamentService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * SR中对应tournament
 * BC中对应Competition
 * 服务实现类
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-09-03
 */
@Service
public class StandardSportTournamentServiceImpl extends ServiceImpl<StandardSportTournamentMapper, StandardSportTournament> implements StandardSportTournamentService {
    @Autowired
    private StandardSportTournamentMapper standardSportTournamentMapper;

    /**
     * @return java.util.List<com.panda.sport.rcs.pojo.StandardSportTournament>
     * @Description 查询
     * @Param [tournamentIds, beginDate, endDate, matchType, otherMorningMarke]
     * @Author toney
     * @Date 13:10 2020/3/19
     **/
    @Override
    public List<StandardSportTournament> queryByIdsAndBeginDateAndEndDateAndMatchType(@Param("tournamentIds") List<Long> tournamentIds, @Param("beginDate") Long beginDate, @Param("endDate") Long endDate, @Param("matchType") Integer matchType, @Param("otherMorningMarke") Integer otherMorningMarke) {
        return standardSportTournamentMapper.queryByIdsAndBeginDateAndEndDateAndMatchType(tournamentIds, beginDate, endDate, matchType, otherMorningMarke);
    }

    /**
     * @param tournamentName: 联赛名称
     * @param sportId: 赛种
     * @Description: 根据名称，查询联赛
     * @Author carver
     * @Date 2020/10/20 20:47
     * @return: java.util.List<com.panda.sport.rcs.pojo.StandardSportTournament>
     **/
    @Override
    public List<StandardSportTournament> queryTournamentByName(Integer sportId,String tournamentName) {
        return standardSportTournamentMapper.queryTournamentByName(sportId,tournamentName);
    }
}
