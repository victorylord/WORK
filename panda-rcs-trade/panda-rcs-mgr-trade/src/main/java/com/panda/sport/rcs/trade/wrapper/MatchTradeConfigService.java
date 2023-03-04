package com.panda.sport.rcs.trade.wrapper;

import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.statistics.RcsProfitRectangle;
import com.panda.sport.rcs.trade.param.RcsMatchConfigParam;
import com.panda.sport.rcs.trade.vo.MarketBalanceVo;
import com.panda.sport.rcs.trade.vo.MarketProfitVo;

import java.util.List;
import java.util.Map;

/**
 * @ClassName MatchTradeConfigService
 * @Description: TODO
 * @Author Enzo
 * @Date 2020/8/08
 **/
public interface MatchTradeConfigService {

    Map<String, Object> updateRiskManagerCode(RcsMatchConfigParam config);

    Map<String, String> getRiskManagerCode(Long matchId);

    /**
     * @MethodName: carver
     * @Description: 让球/大小球玩法期望值
     **/
    List<MarketProfitVo> getProfitByMatchIdAndPlayId(RcsProfitRectangle rcsProfitRectangle);

    /**
     * 查询盘口对应下注详情
     *
     * @param
     * @return
     */
    Map<String, Object> getBalancesByMatchIdAndPlayId(RcsMatchMarketConfig config,Integer matchType);

    MarketBalanceVo queryBalance(Long matchId, Long marketId, String marketType, Integer balanceOption);

    /**
     * @return com.panda.sport.rcs.vo.HttpResponse<java.util.Map < java.lang.String, java.lang.Object>>
     * @Description 获取盘口对应配置信息
     * @Param [config]
     * @Author Sean
     * @Date 17:12 2020/6/26
     **/
    RcsMatchMarketConfig queryMatchMarketConfig(RcsMatchMarketConfig config);

}
