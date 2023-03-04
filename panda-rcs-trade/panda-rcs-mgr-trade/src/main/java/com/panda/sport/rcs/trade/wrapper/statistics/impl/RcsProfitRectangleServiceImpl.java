package com.panda.sport.rcs.trade.wrapper.statistics.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.mapper.statistics.RcsProfitRectangleMapper;
import com.panda.sport.rcs.pojo.statistics.RcsProfitRectangle;
import com.panda.sport.rcs.trade.wrapper.statistics.RcsProfitRectangleService;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author :  toney
 * @Project Name :  panda-rcs
 * @Package Name :  com.panda.sport.rcs.trade.wrapper.statistics.impl
 * @Description :  TODO
 * @Date: 2019-12-11 16:40
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Service
@Slf4j
public class RcsProfitRectangleServiceImpl extends ServiceImpl<RcsProfitRectangleMapper, RcsProfitRectangle> implements RcsProfitRectangleService {
    @Autowired
    private RcsProfitRectangleMapper mapper;


    /**
     * @Description   搜索
     * @Param [ids, beginDate, endDate, matchType]
     * @Author  toney
     * @Date  16:59 2020/3/5
     * @return java.util.List<com.panda.sport.rcs.pojo.statistics.RcsProfitRectangle>
     **/
    @Override
    public List<RcsProfitRectangle> queryByIdsAndBeginDateAndEndDateAndMatchType(List<Long> tournamentIds, Long beginDate, Long endDate,String matchType,Integer otherMorningMarke){
        return mapper.queryByIdsAndBeginDateAndEndDateAndMatchType(tournamentIds,beginDate,endDate,matchType,otherMorningMarke);
    }
}