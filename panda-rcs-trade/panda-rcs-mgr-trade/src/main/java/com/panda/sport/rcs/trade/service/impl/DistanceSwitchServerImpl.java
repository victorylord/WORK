package com.panda.sport.rcs.trade.service.impl;

import com.alibaba.fastjson.JSON;
import com.panda.merge.api.ITradeMarketConfigApi;
import com.panda.merge.dto.MarketTwoStatusConfigDTO;
import com.panda.merge.dto.Request;
import com.panda.merge.dto.Response;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplate;
import com.panda.sport.rcs.trade.service.DistanceSwitchServer;
import com.panda.sport.rcs.utils.DataRealtimeApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Service;

/**
 * @author :  koala
 * @Project Name :  panda-rcs-trade-group
 * @Package Name :  com.panda.sport.rcs.trade.service.impl
 * @Description :  发送接距开关给融合服务类
 * @Date: 2022-05-20 16:21
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Slf4j
@Service
public class DistanceSwitchServerImpl implements DistanceSwitchServer {
    @Reference(check = false, lazy = true, retries = 3, timeout = 5000)
    private ITradeMarketConfigApi tradeMarketConfigApi;
    @Override
    public void sendDistanceSwitch(RcsTournamentTemplate temp) {
        //1852发送开关状态给融合
        String linkId = "distanceSwitch_"+temp.getTypeVal()+"_"+temp.getMatchType();
        MarketTwoStatusConfigDTO marketTwoStatusConfigDTO = new MarketTwoStatusConfigDTO();
        marketTwoStatusConfigDTO.setStandardMatchId(temp.getTypeVal());
        marketTwoStatusConfigDTO.setStatus(temp.getDistanceSwitch());
        marketTwoStatusConfigDTO.setMarketType(temp.getMatchType());
        log.info("::{}::::RPC调用[接距开关状态同步]请求参数::{}", linkId, JSON.toJSONString(marketTwoStatusConfigDTO));
        Response<String> response = DataRealtimeApiUtils.handleApi(marketTwoStatusConfigDTO, new DataRealtimeApiUtils.ApiCall() {
            @Override
            public <R> Response<R> callApi(Request request) {
                return tradeMarketConfigApi.putMarketTwoStatusConfig(request);
            }
        });
        log.info("::{}::::RPC调用[接距开关状态同步]响应参数::{}", linkId, JSON.toJSONString(response));
    }
}
