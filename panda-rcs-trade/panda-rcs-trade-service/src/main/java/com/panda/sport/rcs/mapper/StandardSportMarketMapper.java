package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.merge.dto.StandardMarketDTO;
import com.panda.merge.dto.StandardMarketOddsDTO;
import com.panda.sport.rcs.pojo.MatchStatisticsInfoDetail;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.StandardSportMarket;
import com.panda.sport.rcs.pojo.StandardSportMarketOdds;
import com.panda.sport.rcs.pojo.dto.StandardMarketPlaceDto;
import com.panda.sport.rcs.pojo.dto.odds.RcsStandardMarketDTO;
import com.panda.sport.rcs.vo.MarketLiveOddsQueryVo;
import com.panda.sport.rcs.vo.OddsSnapShotVo;
import com.panda.sport.rcs.vo.UpdateOddsValueVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 足球赛事盘口表. 使用盘口关联的功能存在以下假设：同一个盘口的显示值不可变更，如果变更需要删除2个盘口之间的关联关系。。 Mapper 接口
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-09-03
 */
@Repository
public interface StandardSportMarketMapper extends BaseMapper<StandardSportMarket> {

    Map<String, Object> queryMatchMarketInfo(Map<String, Object> map);

    String queryOddTemplateInfo(String templateCode);

    /**
     * @return java.util.List<com.panda.sport.rcs.pojo.StandardSportMarket>
     * @Description //查找盘口id
     * @Param [matchId, playIds]
     * @Author kimi
     * @Date 2020/1/15
     **/
    List<StandardSportMarket> selectStandardSportMarketByMatchIdAndPlayId(@Param("matchId") Long matchId, @Param("playId") Integer playId);

    /**
     * @return java.lang.Integer
     * @Description //根据盘口值查询id
     * @Param [matchId, marketValue]
     * @Author kimi
     * @Date 2020/1/25
     **/
    Long selectStandardSportMarketIdByMarketValue(@Param("matchId") Long matchId, @Param("playId") Long playId, @Param("marketValue") String marketValue);

    /**
     * @return java.util.List<java.lang.Long>
     * @Description //根据玩法阶段查找有哪些盘口
     * @Param [state, matchId]
     * @Author kimi
     * @Date 2020/2/10
     **/
    List<Long> selectMarketIdByState(@Param("state") Integer state, @Param("matchId") Long matchId);

    /**
     * @return java.util.List<java.lang.Long>
     * @Description //根据赛事id 查询所有盘口Id
     * @Param [matchId]
     * @Author kimi
     * @Date 2020/2/10
     **/
    List<Long> selectMarketIdByMatchId(@Param("matchId") Long matchId);

    /**
     * @return java.util.List<java.lang.Integer>
     * @Description //TODO
     * @Param [matchId]
     * @Author kimi
     * @Date 2020/2/18
     **/
    List<Long> selectPlayIdByMatchId(@Param("matchId") Long matchId);

    /**
     * @return java.util.List<com.panda.sport.rcs.pojo.StandardSportMarket>
     * @Description //TODO
     * @Param [matchId, playId]
     * @Author kimi
     * @Date 2020/2/20
     **/
    List<StandardSportMarketOdds> selectStandardSportMarketByGiveWay(@Param("matchId") Long matchId, @Param("playId") Long playId);

    Map<String, Object> getOtherMarketInfo(Map<String, Object> params);

    List<Map<String, Object>> queryRelatedDataList(Map<String, Object> queryParams);

    /**
     * @return java.util.List<com.panda.sport.rcs.pojo.MatchStatisticsInfoDetail>
     * @Description //TODO
     * @Param [matchId]
     * @Author kimi
     * @Date 2020/2/29
     **/
    List<MatchStatisticsInfoDetail> selectMatchStatisticsInfoDetail(@Param("matchId") Long matchId);

    /**
     * @return java.util.List<com.panda.sport.rcs.pojo.StandardSportMarket>
     * @Description //TODO
     * @Param [matchId, playId, addition2]
     * @Author kimi
     * @Date 2020/2/29
     **/
    List<StandardSportMarket> selectStandardSportMarket(@Param("matchId") Long matchId, @Param("playId") Long playId, @Param("addition2") String addition2);

    /**
     * @return java.util.List<com.panda.sport.rcs.pojo.StandardSportMarket>
     * @Description //TODO
     * @Param [matchId, playId1, playId2]
     * @Author kimi
     * @Date 2020/3/2
     **/
    List<StandardSportMarket> selectStandardSportMarketByMatchIdAndPlayIdAndPlayId(@Param("matchId") Long matchId);

    /**
     * @return java.util.List<com.panda.sport.rcs.vo.OddsSnapShotVo>
     * @Description //TODO
     * @Param [matchId]
     * @Author kimi
     * @Date 2020/7/8
     **/
    List<OddsSnapShotVo> selectMatchSnapShot(@Param("matchId") Long matchId, @Param("matchStatus") Integer matchStatus);

    StandardSportMarket selectTournamentAndMatchById(@Param("marketId") Long marketId);

    /**
     * 查询盘口位置信息
     *
     * @param matchId  赛事ID
     * @param playIds  玩法ID集合，可为空，为空查所有
     * @param placeNum 盘口位置，可为空，为空查所有
     * @return
     * @author Paca
     */
    @Deprecated
    List<StandardMarketPlaceDto> selectMarketPlaceInfo(@Param("matchId") Long matchId, @Param("playIds") Collection<Long> playIds, @Param("placeNum") Integer placeNum);

    /**
     * 查询盘口信息
     *
     * @param matchId 赛事ID
     * @param playIds 玩法ID集合
     * @return
     * @author Paca
     */
    List<StandardSportMarket> queryMarketInfo(@Param("matchId") Long matchId, @Param("playIds") Collection<Long> playIds);

    /**
     * 查询主盘口信息
     *
     * @param matchId 赛事ID
     * @param playId  玩法ID
     * @return
     * @author Paca
     */
    StandardSportMarket queryMainMarketInfo(@Param("matchId") Long matchId, @Param("playId") Long playId);

    /**
     * 查询主盘口信息
     *
     * @param matchId
     * @param playIds
     * @return
     */
    List<StandardSportMarket> listMainMarketInfo(@Param("matchId") Long matchId, @Param("playIds") Collection<Long> playIds);

    /**
     * 查询主盘口信息
     *
     * @param matchId    赛事ID
     * @param categoryId 玩法ID
     * @return
     * @author Paca
     */
    @Select("SELECT" +
            " m.*" +
            " FROM" +
            " (" +
            " SELECT" +
            " r.standard_match_info_id," +
            " r.market_category_id," +
            " r.version_id" +
            " FROM" +
            " rcs_standard_place_ref r" +
            " WHERE" +
            " r.standard_match_info_id = #{matchId}" +
            " AND r.market_category_id = #{categoryId}" +
            " AND r.child_market_category_id = #{subPlayId}" +
            " AND r.place_num = 1" +
            " ) t" +
            " LEFT JOIN rcs_standard_place_ref p ON ( p.standard_match_info_id = t.standard_match_info_id AND p.market_category_id = t.market_category_id AND p.version_id = t.version_id )" +
            " LEFT JOIN standard_sport_market m ON p.market_id = m.id" +
            " WHERE" +
            " p.place_num = 1" +
            " AND m.child_market_category_id = #{subPlayId}" +
            " AND p.market_id IS NOT NULL")
    StandardSportMarket selectMainMarketInfo(@Param("matchId") Long matchId, @Param("categoryId") Long categoryId,@Param("subPlayId")String subPlayId);

    /**
     * @return java.util.List<com.panda.sport.rcs.pojo.StandardSportMarket>
     * @Description //根据赛事id和玩法id查询所有有效的盘口
     * @Param [config]
     * @Author Sean
     * @Date 11:04 2020/10/6
     **/
    List<StandardSportMarket> selectAllMarketByPlayAndMatch(@Param("config") RcsMatchMarketConfig config);

    /**
     * @return java.util.List<com.panda.sport.rcs.pojo.StandardSportMarket>
     * @Description //根据赛事id和玩法id查询所有有效的盘口
     * @Param [config]
     * @Author Sean
     * @Date 11:04 2020/10/6
     **/
    List<RcsStandardMarketDTO> selectMarketOddsByMarketIds(@Param("config") RcsMatchMarketConfig config);

    /**
     * 盘口弃用
     *
     * @param marketId
     * @return
     * @author Paca
     */
    @Update("UPDATE standard_sport_market m" +
            " SET m.`status` = m.`status` + 100," +
            " m.third_market_source_status = m.third_market_source_status + 100" +
            " WHERE" +
            " m.id = #{marketId}")
    Integer marketDisable(@Param("marketId") Long marketId);
    /**
     * @Description   //清空盘口差
     * @Param [standardMatchInfo]
     * @Author  sean
     * @Date   2020/12/12
     * @return void
     **/
//    void clearMarketHeadGapByMatch(@Param("match") StandardSportMarket market);
    /**
     * @Description   //清除水差
     * @Param []
     * @Author  sean
     * @Date   2020/12/12
     * @return void
     **/
    void clearMarketDiffByMarketIds(@Param("ids") List<Long> ids);
    /**
     * @Description   //根据玩法获取模板id和投注项排序
     * @Param [marketCategoryId]
     * @Author  sean
     * @Date   2020/12/27
     * @return java.util.List<com.panda.sport.rcs.pojo.dto.odds.RcsStandardMarketDTO>
     **/
    List<StandardMarketOddsDTO> selectOddsFieldsTempletId(@Param("marketCategoryId") Long marketCategoryId);

    /**
     * 查询三方盘口球头
     * @return
     */
    List<Map<String,String>> queryThirdSourceMarket(@Param("vo") MarketLiveOddsQueryVo vo);
    /**
     * @Description   //查询冠军玩法赔率
     * @Param [updateOddsValueVo]
     * @Author  sean
     * @Date   2021/6/16
     * @return com.panda.merge.dto.StandardMarketDTO
     **/
    StandardMarketDTO selectChampionOddsByMarketIds(@Param("vo")UpdateOddsValueVo updateOddsValueVo);
    /**
     * @Description   //国际化信息
     * @Param [id]
     * @Author  sean
     * @Date   2021/6/25
     * @return java.util.Map<java.lang.String,java.lang.String>
     **/
    List<Map<String, String>> queryMarketI18nNames(@Param("id")String id);


    List<StandardSportMarket> queryPlaceByMarketIds(@Param("ids") List<Long> collect);

}


