package com.panda.sport.rcs.trade.service.impl;

import com.panda.sport.data.rcs.api.Request;
import com.panda.sport.data.rcs.api.Response;
import com.panda.sport.data.rcs.api.tournament.TournamentOnSaleApi;
import com.panda.sport.data.rcs.dto.tournament.StandardMarketSellQueryDto;
import com.panda.sport.rcs.trade.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.utils.Assert;
import org.apache.dubbo.config.annotation.Service;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author :  myname
 * @Project Name :  panda-rcs-trade-group
 * @Package Name :  com.panda.sport.rcs.trade.service.impl
 * @Description :  TODO
 * @Date: 2022-07-09 10:25
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Service
@Slf4j
public class TournamentOnSaleApiImpl implements TournamentOnSaleApi {
    @Autowired
    private OnSaleCommonServer onSaleCommonServer;

    @Override
    public Response confirmMarketCategorySell(Request<StandardMarketSellQueryDto> request) {
        StandardMarketSellQueryDto standardMarketSellQueryVo = request.getData();
        Assert.notNull(standardMarketSellQueryVo, "开售参数不能为null");
        Assert.notNull(standardMarketSellQueryVo.getMatchId(), "赛事id不能为null");
        Assert.notNull(standardMarketSellQueryVo.getSportId(), "赛种id不能为null");
        MDC.put("X-B3-TraceId", request.getGlobalId());
        try {
            onSaleCommonServer.confirmMarketCategorySell(standardMarketSellQueryVo);
        } catch (IllegalArgumentException e) {
            log.error("::{}::{}", CommonUtil.getRequestId(),e.getMessage(), e);
            return Response.error(201, e.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(),e.getMessage(), e);
            return Response.error(500, e.getMessage());
        }
        return Response.success();
    }
}
