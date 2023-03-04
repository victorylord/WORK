package com.panda.sport.rcs.trade.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.panda.merge.api.ITradeMarketConfigApi;
import com.panda.merge.dto.ConfigCashOutTradeItemDTO;
import com.panda.merge.dto.Request;
import com.panda.merge.dto.Response;
import com.panda.merge.dto.TradeMarketUiConfigDTO;
import com.panda.sport.data.rcs.dto.tournament.StandardMarketSellQueryDto;
import com.panda.sport.manager.api.IMarketCategorySellApi;
import com.panda.sport.manager.api.dto.ConfirmMarketCategorySellDTO;
import com.panda.sport.rcs.core.utils.BeanCopyUtils;
import com.panda.sport.rcs.enums.SportIdEnum;
import com.panda.sport.rcs.mapper.tourTemplate.RcsTournamentTemplateMapper;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplate;
import com.panda.sport.rcs.trade.enums.LinkedTypeEnum;
import com.panda.sport.rcs.trade.service.DistanceSwitchServer;
import com.panda.sport.rcs.trade.service.TradeModeService;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.RcsStandardSportMarketSellService;
import com.panda.sport.rcs.utils.DataRealtimeApiUtils;
import com.panda.sport.rcs.utils.TradeUserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.springframework.stereotype.Component;

/**
 * @author :  myname
 * @Project Name :  panda-rcs-trade-group
 * @Package Name :  com.panda.sport.rcs.trade.service.impl
 * @Description :  TODO
 * @Date: 2022-07-09 10:32
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OnSaleCommonServer {
    @Reference(check = false, lazy = true, retries = 1, timeout = 100000)
    private IMarketCategorySellApi marketCategorySellApi;
    @Reference(check = false, lazy = true, retries = 3, timeout = 5000)
    private ITradeMarketConfigApi tradeMarketConfigApi;

    private final DistanceSwitchServer distanceSwitchServerImpl;
    private final RcsTournamentTemplateMapper templateMapper;
    private final TradeModeService tradeModeService;
    private final RcsStandardSportMarketSellService rcsStandardSportMarketSellService;

    public void confirmMarketCategorySell(StandardMarketSellQueryDto standardMarketSellQueryVo) {
        //调用融合开售接口
        ConfirmMarketCategorySellDTO dto = BeanCopyUtils.copyProperties(standardMarketSellQueryVo, ConfirmMarketCategorySellDTO.class);
        String linkId = CommonUtil.getRequestId(standardMarketSellQueryVo.getMatchId(),standardMarketSellQueryVo.getPlayId());
        log.info("::{}::RPC调用[开售]请求参数::{}", linkId, JSON.toJSONString(dto));
        dto.setOperateTime(System.currentTimeMillis());
        Response<String> response = DataRealtimeApiUtils.handleApi(dto, new DataRealtimeApiUtils.ApiCall() {
            @Override
            @Trace
            public <R> Response<R> callApi(Request request) {
                request.setLinkId(request.getLinkId() + "_sold");
                Response<String> rs = marketCategorySellApi.confirmMarketCategorySell(request);
                return (Response<R>) rs;
            }
        });
        log.info("::{}::RPC调用[开售]响应参数::{}", linkId, JSON.toJSONString(response));
        if (response.isSuccess()) {
            //更新赛事模板赔率源权重优先级和玩法状态
            rcsStandardSportMarketSellService.updatePlayMarginIsSellByPlayId(standardMarketSellQueryVo);

            //kir-开售赛事时也需要同步最新的（赛事级别的提前结算开关）状态给融合
            if (SportIdEnum.isFootball(standardMarketSellQueryVo.getSportId())) {
                QueryWrapper<RcsTournamentTemplate> wrapper = new QueryWrapper<>();
                wrapper.eq("type_val", standardMarketSellQueryVo.getMatchId());
                wrapper.eq("type", 3);
                wrapper.eq("sport_id", standardMarketSellQueryVo.getSportId());
                wrapper.eq("match_type", standardMarketSellQueryVo.getMarketType().equals("PRE") ? 1 : 0);
                RcsTournamentTemplate temp = templateMapper.selectOne(wrapper);
                TradeMarketUiConfigDTO tradeMarketUiConfigDTO = this.getCommonClass(temp);
                log.info("::{}::RPC调用[提前结算开关]请求参数::{}", linkId, JSON.toJSONString(tradeMarketUiConfigDTO));
                Response<String> responseConfig = DataRealtimeApiUtils.handleApi(tradeMarketUiConfigDTO, new DataRealtimeApiUtils.ApiCall() {
                    @Override
                    public <R> Response<R> callApi(Request request) {
                        return tradeMarketConfigApi.putTradeMarketUiConfig(request);
                    }
                });
                log.info("::{}::RPC调用[提前结算开关]响应参数::{}", linkId, JSON.toJSONString(responseConfig));
                //1852发送开关状态给融合
                distanceSwitchServerImpl.sendDistanceSwitch(temp);
            }
            tradeModeService.basketballPlaySaleSwitchLinkage(standardMarketSellQueryVo.getSportId(), standardMarketSellQueryVo.getMatchId(), standardMarketSellQueryVo.getMarketCategoryIds(), LinkedTypeEnum.PLAY_SALE);
        }
    }

    public TradeMarketUiConfigDTO getCommonClass(RcsTournamentTemplate temp) {
        ConfigCashOutTradeItemDTO cashOutTradeItemDTO = new ConfigCashOutTradeItemDTO();
        cashOutTradeItemDTO.setMatchId(temp.getTypeVal());
        cashOutTradeItemDTO.setMatchPreStatus(temp.getMatchPreStatus());
        cashOutTradeItemDTO.setPendingOrderStatus(temp.getPendingOrderStatus());
        cashOutTradeItemDTO.setMarketType(temp.getMatchType());
        cashOutTradeItemDTO.setDataSourceCode(CommonUtil.getDataSourceCode(temp.getEarlySettStr()));
        TradeMarketUiConfigDTO dto = new TradeMarketUiConfigDTO();
        dto.setConfigCashOutTradeItemDTO(cashOutTradeItemDTO);
        dto.setStandardMatchInfoId(temp.getTypeVal());
        return dto;
    }
}
