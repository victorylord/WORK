package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.panda.sport.rcs.pojo.RcsMatchCollection;
import com.panda.sport.rcs.pojo.RcsMatchTradeMemo;
import com.panda.sport.rcs.pojo.RcsStandardSportMarketSell;
import com.panda.sport.rcs.vo.StandardMarketSellQueryVo;
import com.panda.sport.rcs.vo.StandardMarketSellVo;
import com.panda.sport.rcs.vo.StandardMatchInfoMatrixVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @ClassName RcsStandardSportMarketSellMapper
 * @Description: TODO
 * @Author Vector
 * @Date 2019/12/30
 **/
@Component
public interface RcsStandardSportMarketSellMapper extends BaseMapper<RcsStandardSportMarketSell> {
    int updateBatch(List<RcsStandardSportMarketSell> list);

    int batchInsert(@Param("list") List<RcsStandardSportMarketSell> list);

    int insertOrUpdate(RcsStandardSportMarketSell record);

    int updateBatchSelective(List<RcsStandardSportMarketSell> list);

    IPage<StandardMarketSellVo> selectRcsStandardSportMarketSell(IPage<StandardMarketSellVo> page, @Param("standardMarketSellQueryVo") StandardMarketSellQueryVo standardMarketSellQueryVo);

    RcsStandardSportMarketSell queryMarketSell(@Param("record") RcsStandardSportMarketSell record);

    List<Map> getMatchNumberByType(@Param("beginTimeMillis") long beginTimeMillis, @Param("endTimeMillis") long endTimeMillis);

    int updateMarketCount(@Param("record") RcsStandardSportMarketSell record);

    RcsStandardSportMarketSell selectRcsStandardSportMarketSellByMatchInfoId(@Param("matchId") Long matchId);

    List<Long> queryTraderMatchIds(@Param("co")RcsMatchCollection co);

    void updateRcsStandardSportMarketSellTradeId(@Param("matchId")Integer matchId,@Param("matchType")Integer matchType,@Param("tradeId")Integer tradeId,@Param("tradeName") String tradeName);

    IPage<StandardMatchInfoMatrixVo> selectMatchId(IPage<StandardMatchInfoMatrixVo> page, @Param("tournamentIdList")List<Long> tournamentIdList, @Param("matchType")Integer matchType, @Param("startTime")Long startTime,
                              @Param("endTime")Long endTime, @Param("traderIdList") Set<String> traderIdList);


    @Select("<script> SELECT  refInfo.memo_id FROM " +
            " rcs_match_user_memo_ref refInfo LEFT JOIN rcs_match_trade_memo memoInfo ON memoInfo.id = refInfo.memo_id " +
            " WHERE memoInfo.trader_id != #{traderId} AND refInfo.trader_id = #{traderId} AND refInfo.read_status = '1' " +
            " AND refInfo.standard_match_id IN <foreach item='item' index='index' collection='matchIds' open='(' separator=',' close=')'> #{item}</foreach>" +
            " GROUP BY refInfo.memo_id </script> ")
    List<String> getReadMemoIds(@Param("matchIds") List<Long> matchIds, @Param("traderId") String traderId);

    @Select("<script> SELECT id, standard_match_id FROM rcs_match_trade_memo WHERE trader_id != #{traderId} " +
            " AND standard_match_id IN <foreach item='item' index='index' collection='matchIds' open='(' separator=',' close=')'> #{item}</foreach> " +
            " <if test = 'readMatchMemos != null and readMatchMemos.size() > 0' > AND id NOT IN <foreach item='item' index='index' collection='readMatchMemos' open='(' separator=',' close=')'> #{item}</foreach> </if> </script> ")
    List<RcsMatchTradeMemo> getMatchMemoMatchIds(@Param("matchIds") List<Long> matchIds, @Param("traderId") String traderId, @Param("readMatchMemos") List<String> readMatchMemos);

}