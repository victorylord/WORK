package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsTradingAssignment;
import com.panda.sport.rcs.pojo.vo.TradingAssignmentDataVo;
import com.panda.sport.rcs.vo.RcsMarketCategorySetVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @program: xindaima
 * @description:
 * @author: kimi
 * @create: 2020-11-07 11:42
 **/
@Component
public interface RcsTradingAssignmentMapper extends BaseMapper<RcsTradingAssignment> {
    /**
    * @Description: 查询指派数据
    * @Param: [matchId, matchType]
    * @return: java.util.List<com.panda.sport.rcs.pojo.RcsTradingAssignment>
    * @Author: KIMI
    * @Date: 2020/11/7
    */
    List<TradingAssignmentDataVo> selectRcsTradingAssignment(@Param("matchId") Long matchId, @Param("matchType")Integer matchType, @Param("playSetList") List<RcsMarketCategorySetVo> playSetList );

    /**
     * 根据用户删除数据
     * @param userIdList
     */
    void deleteTradingAssignmentByUserIdList(@Param("matchId") Long matchId, @Param("matchType")Integer matchType,@Param("userIdList") List<String> userIdList);
    /**
    * @Description: 查询人数
    * @Param: [matchId, matchType]
    * @return: java.lang.Integer
    * @Author: KIMI
    * @Date: 2020/11/10
    */
    Integer selectTradingAssignmentCount(@Param("matchId") Long matchId, @Param("matchType")Integer matchType,@Param("tradeId") Integer tradeId);

    /**
     * 
     * @param traderId
     * @param matchType
     * @return
     */
    List<Long> queryTradeMatchIds(@Param("traderId") String traderId, @Param("matchType") Integer matchType);
    
    /**
    * @Description: 查询id
    * @Param: [matchId, matchType]
    * @return: java.util.List<java.lang.Long>
    * @Author: KIMI
    * @Date: 2020/11/13
    */
    List<Integer> selectUserId(@Param("matchId") Long matchId, @Param("matchType")Integer matchType);

    /**
     *
     * @param matchId
     * @param matchType
     * @param traderId
     */
    void  deleteByIdAndMatchId(@Param("matchId")Integer matchId, @Param("matchType")Integer matchType, @Param("traderId")Integer traderId);
}
