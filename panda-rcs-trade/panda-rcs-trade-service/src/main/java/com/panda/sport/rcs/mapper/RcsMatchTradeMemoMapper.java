package com.panda.sport.rcs.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsMatchTradeMemo;
import com.panda.sport.rcs.pojo.RcsMatchUserMemoRef;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface RcsMatchTradeMemoMapper extends BaseMapper<RcsMatchTradeMemo> {

    @Update(" UPDATE rcs_match_trade_memo SET browse_count = #{browseCount}, browse_history = #{browseHistory}, modify_time = #{modifyTime} WHERE id = #{id} ")
    void updateMatchTradeMemoWhenBrows(@Param("id") String id, @Param("browseCount") Integer browseCount, @Param("browseHistory") String browseHistory, @Param("modifyTime") Long modifyTime );

    @Select(" select * from rcs_sys_user where logic_delete = '0' and enabled = '1' and id != #{currUserId}  ")
    List<Long> getOtherUserIds(String currUserId);

    @Select(" select * from rcs_match_user_memo_ref where memo_id = #{memoId} and standard_match_id = #{standardMatchId} and trader_id = #{traderId} ")
    RcsMatchUserMemoRef getTraderMemoRef(@Param("memoId")String memoId, @Param("traderId") String traderId, @Param("standardMatchId") Long standardMatchId);

    @Select(" select memo_id from rcs_match_user_memo_ref where  standard_match_id = #{standardMatchId} and trader_id = #{traderId} ")
    List<String> getTraderReadMemos(@Param("traderId") String traderId, @Param("standardMatchId") Long standardMatchId);
}