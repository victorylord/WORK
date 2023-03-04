package com.panda.sport.rcs.trade.wrapper.statistics;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.statistics.RcsProfitMarket;
import com.panda.sport.rcs.pojo.statistics.RcsProfitRectangle;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author :  toney
 * @Project Name :  panda-rcs
 * @Package Name :  com.panda.sport.rcs.trade.wrapper.statistics
 * @Description :  TODO
 * @Date: 2019-12-11 16:35
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface RcsProfitRectangleService extends IService<RcsProfitRectangle> {
    /**
     * @Description   搜索
     * @Param [tournamentIds, beginDate, endDate, matchType]
     * @Author  toney
     * @Date  16:59 2020/3/5
     * @return java.util.List<com.panda.sport.rcs.pojo.statistics.RcsProfitRectangle>
     **/
    List<RcsProfitRectangle> queryByIdsAndBeginDateAndEndDateAndMatchType(List<Long> tournamentIds, Long beginDate, Long endDate,String matchType,Integer otherMorningMarket);
}
