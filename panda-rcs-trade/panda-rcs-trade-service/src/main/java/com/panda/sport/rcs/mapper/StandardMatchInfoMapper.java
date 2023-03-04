package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.panda.sport.rcs.matrix.ForecastInfoVo;
import com.panda.sport.rcs.matrix.ForecastReqVo;
import com.panda.sport.rcs.matrix.MatrixMatchInfoVo;
import com.panda.sport.rcs.matrix.MatrixReqVo;
import com.panda.sport.rcs.mongo.PredictForecastVo;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.vo.*;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-09-03
 */
@Repository
public interface StandardMatchInfoMapper extends BaseMapper<StandardMatchInfo> {


    //IPage<StandardMatchInfo> selectPageByCondition(IPage<StandardMatchInfo> page, @Param("obj") MarketLiveOdselectMarketIdByStatedsQueryVo marketLiveOddsQueryVo);

    TraderConditionVo selectConditionById(@Param("id") Long standardMatchId);

    StandardMatchInfo queryTimeOutMatch(@Param("id") Long standardMatchId);

    String selectMatchPositionByOptionId(Long playOptionId);

    List<TournamentMatchInfoVo> selectMacthInfo(@Param("tournamentId") long tournamentId,@Param("dateTime") String dateTime);

    List<StandardMatchInfo> selectPageByCondition(@Param("obj") MarketLiveOddsQueryVo marketLiveOddsQueryVo);

    List<StandardMatchInfo> selectTournamentsByCondition(@Param("obj") MarketLiveOddsQueryVo marketLiveOddsQueryVo);

    List<TournamentNameVo> selectMatchsByStandardTournamentId(@Param("sportId") Long sportId, @Param("standardTournamentId") Long standardTournamentId, @Param("beginTime") Long beginTime, @Param("endTime") Long endTime);

    List<TeamVo> selectTeamNameByStandardTournamentId(@Param("sportId") Long sportId, @Param("standardTournamentId") Long standardTournamentId, @Param("beginTime") Long beginTime, @Param("endTime") Long endTime);


    List<TeamVo> selectTeamNameByStandardTournamentIds(@Param("sportId") Long sportId, @Param("standardTournamentId")  List<Long> standardTournamentIds, @Param("beginTime") Long beginTime, @Param("endTime") Long endTime);


    List<TournamentNameVo> selectTournamentNameById(@Param("id") String id);

    List<TeamVo> selectTeamNameById(@Param("id") String id);

    List<TournamentVoBySport> getTournamentList(@Param("sportId") Long sportId, @Param("beginTime") Long beginTime, @Param("endTime") Long endTime, @Param("type") Integer type);

	List<StandardMatchInfo> selectPageByConditionV2(@Param("obj") MarketLiveOddsQueryVo marketLiveOddsQueryVo);

    List<StandardMatchInfo> selectMatchs(@Param("obj") MarketLiveOddsQueryVo marketLiveOddsQueryVo);

    //List<MacthTournamentNameVo> selectTournamentNameVo(@Param("beginTime") long beginTime, @Param("endTime") long endTime, @Param("isOtherEarly") Integer isOtherEarly, @Param("liveOddBusiness") Integer liveOddBusiness);

    List<StandardMatchInfo> selectTournamentCount(@Param("tournamentIds") List<Long> tournamentIds, @Param("matchIds") List<Long> matchIds, @Param("matchType")Integer matchType);

    List<TeamVo> selectTeamNameByMatchId(@Param("matchId") Long matchId);

    /**
     * @return java.lang.Integer
     * @Description //查询滚球数量
     * @Param []
     * @Author kimi
     * @Date 2020/1/13
     **/
    Integer getGrounderNumber();

    @Select("SELECT id,sport_id FROM standard_match_info WHERE  begin_time >  #{time}  and match_status not in (3) AND standard_tournament_id = #{id}")
    List<StandardMatchInfo> selectOpenStatusMatchByTournament(@Param("id") Long tournamentId,@Param("time") Long time);
    /**
     * @Description   查询联赛
     * @Param [map]
     * @Author  Sean
     * @Date  12:21 2020/2/5
     * @return java.util.List<com.panda.sport.rcs.vo.BaseMatchInfoVo>
     **/
    List<BaseMatchInfoVo> queryTournamentList(Map<String, Object> map);


    /**
     * @return java.util.List<java.lang.Long>
     * @Description //根据玩法阶段查找有哪些盘口
     * @Param [state, matchId]
     * @Author kimi
     * @Date 2020/2/10
     **/
    List<Long> selectMarketIdByState(@Param("state") Integer state, @Param("matchId") Long matchId);

    /**
     * @Description   查询30s内没有处理的订单
     * @Param [matchId, beginTime]
     * @Author  Sean
     * @Date  17:31 2020/2/13
     * @return java.util.List<java.lang.String>
     **/
    List<String> queryOrderNoByMatchId(@Param("matchId") Long matchId,@Param("beginTime") Long beginTime);

	List<String> queryMatchEndIds(Map<String, Object> params);

	IPage<StandardMatchAllSellVo> getAllSellMatchList(IPage<StandardMatchAllSellVo> iPage
			,@Param("marketLiveOddsQueryVo") MarketLiveOddsQueryVo marketLiveOddsQueryVo);


    List<Long> selectMatchIds(@Param("tournamentIds") List<Long> tournamentIds, @Param("matchIds") List<Long> matchIds, @Param("traderId")String traderId,@Param("time")Long time);
    
    /**
     * @Description  查询赛事基础数据
     * @Param [matchId]
     * @Author  kimi
     * @Date   2020/10/27
     * @return java.util.Map<java.lang.String,java.lang.Object>
     **/
    StandardMatchInfoVo  selectStandardMatchInfoBaseByMatchId(@Param("matchId") Long matchId);


    Integer getSportId(@Param("broadId") Integer broadId);

    /**
     * 根据赛事ID查询赛事状态
     * @Author Kir
     * @param id
     * @return
     * @Date 2021/1/3
     */
    Integer selectMatchStatusById(@Param("id") Long id);

    /**
     * 查询矩阵赛事信息
     *
     * @param page
     * @param matrixReqVo
     * @return
     * @author Paca
     */
    IPage<MatrixMatchInfoVo> getMatrixMatchInfo(IPage<MatrixMatchInfoVo> page, @Param("param") MatrixReqVo matrixReqVo);

    /**
     * 查询Forecast赛事信息
     *
     * @param page
     * @param forecastReqVo
     * @return
     * @author Kir
     */
    IPage<ForecastInfoVo> getForecastMatchInfo(IPage<ForecastInfoVo> page, @Param("param") ForecastReqVo forecastReqVo);

    /**
     * 查询forecast集合
     * @param matchId
     * @param matchType
     * @return
     */
    List<PredictForecastVo> selectForecastPlayList(@Param("matchId") Long matchId, @Param("matchType") Integer matchType, @Param("type") Integer type);

    /**
     *
     * @param matchId
     */
    List<MatchInfoLanguageVo> selectMatchInfoLanguage(@Param("matchId") Long matchId);

    List<Long> getAllPreMatchIds();

    List<Long> getSuportCategoryMatchIds(@Param("matchIds") List<Long> matchIds, @Param("categoryIds") List<Long> categoryIds, @Param("marketType") Integer marketType);
}
