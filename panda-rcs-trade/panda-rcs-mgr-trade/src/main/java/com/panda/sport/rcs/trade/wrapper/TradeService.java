package com.panda.sport.rcs.trade.wrapper;

import com.panda.sport.rcs.pojo.StandardSportMarket;
import com.panda.sport.rcs.vo.MarketDisableVO;
import com.panda.sport.rcs.vo.trade.WaterDiffRelevanceReqVo;

import java.util.List;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 操盘服务类
 * @Author : Paca
 * @Date : 2020-11-06 11:57
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface TradeService {
    /**
     * 盘口弃用列表
     *
     * @param marketDisableVO
     * @return
     * @author Paca
     */
    List<StandardSportMarket> marketDisableList(MarketDisableVO marketDisableVO);

    /**
     * 盘口弃用
     *
     * @param marketDisableVO
     * @return
     * @author Paca
     */
    String marketDisable(MarketDisableVO marketDisableVO);

    /**
     * 水差关联
     *
     * @param reqVo
     * @return
     * @author Paca
     */
    String waterDiffRelevance(WaterDiffRelevanceReqVo reqVo);
}
