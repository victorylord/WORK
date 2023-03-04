package com.panda.sport.rcs.mapper;

import com.panda.sport.rcs.vo.RcsMatchProfitVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * @program: xindaima
 * @description:
 * @author: kimi
 * @create: 2020-12-05 14:21
 **/
@Component
public interface RcsMatchProfitMapper {
    List<RcsMatchProfitVo> selectRcsMatchProfitByPlayId(@Param("matchId")Integer matchId, @Param("playIds")List<Integer> playIds, @Param("matchType")Integer matchType);
    BigDecimal selectRcsMatchProfitByMatchId(@Param("matchId")Integer matchId);
}
