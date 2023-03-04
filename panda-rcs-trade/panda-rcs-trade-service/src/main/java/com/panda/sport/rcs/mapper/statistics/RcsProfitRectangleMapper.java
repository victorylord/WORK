package com.panda.sport.rcs.mapper.statistics;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.statistics.RcsProfitMarket;
import com.panda.sport.rcs.pojo.statistics.RcsProfitRectangle;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author :  toney
 * @Project Name :  panda-rcs
 * @Package Name :  com.panda.sport.rcs.mapper.statistics
 * @Description :  期望值 矩阵
 * @Date: 2019-12-11 16:39
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Repository
public interface RcsProfitRectangleMapper extends BaseMapper<RcsProfitRectangle> {
    /**
     * @Description  删除记录
     * @Param [matchId, playId]
     * @Author  toney
     * @Date  11:07 2019/12/21
     * @return java.lang.Integer
     **/
    Integer deleteByMatchIdAndPlayId(@Param("matchId") Long matchId,@Param("playId") Integer playId);

    /**
     * 批量插入
     * @param list
     */
   Integer batchInsert(@Param("list")List<RcsProfitRectangle> list);

    /**
     * @Description   搜索
     * @Param [tournamentIds, beginDate, endDate, matchType]
     * @Author  toney
     * @Date  16:59 2020/3/5
     * @return java.util.List<com.panda.sport.rcs.pojo.statistics.RcsProfitRectangle>
     **/
    List<RcsProfitRectangle> queryByIdsAndBeginDateAndEndDateAndMatchType(@Param("tournamentIds") List<Long> tournamentIds,@Param("beginDate") Long beginDate,@Param("endDate") Long endDate,@Param("matchType")String matchType,@Param("otherMorningMarke")Integer otherMorningMarke);
}