package com.panda.sport.rcs.trade.wrapper;

import com.panda.sport.rcs.vo.MarketLiveOddsQueryVo;
import com.panda.sport.rcs.vo.MarketStatusUpdateVO;

/**
 * <p>
 * 赛事设置表 服务类
 * </p>
 *
 * @author CodeGenerator
 * @since 2020-07-28
 */
public interface MatchSetService {

    void updateCategorySetShow(MarketLiveOddsQueryVo vo);

    void updateMarketTradeType(MarketStatusUpdateVO marketStatusUpdateVO);

    void updateMarketStatus(MarketStatusUpdateVO marketStatusUpdateVO);
}
