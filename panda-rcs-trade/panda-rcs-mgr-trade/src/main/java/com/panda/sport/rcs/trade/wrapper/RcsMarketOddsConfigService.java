package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.RcsMarketOddsConfig;
import com.panda.sport.rcs.vo.OrderDetailStatReportVo;

import java.util.List;
import java.util.Map;

/**

/**
 * @author :  Administrator
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.wrapper
 * @Description :  TODO
 * @Date: 2019-11-01 16:18
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */

public interface RcsMarketOddsConfigService extends IService<RcsMarketOddsConfig>{

    RcsMarketOddsConfig getMarketOdds(RcsMarketOddsConfig rcsMarketOddsConfig);

    List<OrderDetailStatReportVo> queryMarketStatByMarketId(Long marketId);

    RcsMarketOddsConfig getMarketOdds(Long matchId, Long marketOddsId);

    /**
     * 查询盘口实货量
     * @param matchId
     * @return
     */
    Map<Long, RcsMarketOddsConfig> queryMathBetNums(Long matchId);

    /**
     * 查询盘口货量
     * @param matchIds
     * @return
     */
    Map<Long, Map<String, RcsMarketOddsConfig>> queryMathBetNums(List<Long> matchIds);

}
