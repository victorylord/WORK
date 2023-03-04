package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsFirstMarket;
import com.panda.sport.rcs.pojo.RcsOrderBasketballMatrix;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @program: xindaima
 * @description:
 * @author: kimi
 * @create: 2021-01-13 17:14
 **/
@Component
public interface RcsOrderBasketballMatrixMapper extends BaseMapper<RcsOrderBasketballMatrix> {
    List<RcsOrderBasketballMatrix> selectRcsOrderBasketballMatrix(@Param("matchId") Integer matchId,
                                                                   @Param("playIdList")List<Integer> playIdList,
                                                                   @Param("merchantIdList")List<Long> merchantIdList,
                                                                   @Param("matchType")List<Integer> matchType,
                                                                   @Param("settlement")List<Integer> settlement);

    List<RcsFirstMarket>  getMedian(@Param("matchId") Integer matchId);
}
