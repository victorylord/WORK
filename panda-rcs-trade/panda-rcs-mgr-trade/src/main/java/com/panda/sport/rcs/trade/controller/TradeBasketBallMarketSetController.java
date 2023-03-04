package com.panda.sport.rcs.trade.controller;

import com.alibaba.fastjson.JSONObject;
import com.panda.sport.rcs.enums.SportIdEnum;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.log.annotion.LogAnnotion;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.trade.service.TradeBasketBallMarketServiceImpl;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.RcsTradeConfigService;
import com.panda.sport.rcs.trade.wrapper.RcsTradingAssignmentService;
import com.panda.sport.rcs.utils.CommonUtils;
import com.panda.sport.rcs.utils.TradeUserUtils;
import com.panda.sport.rcs.vo.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description   //操盘接口
 * @Param
 * @Author  Sean
 * @Date  14:12 2020/10/2
 * @return
 **/
@Component
@RestController
@RequestMapping(value = "trade/basketBall")
@Slf4j
public class TradeBasketBallMarketSetController {

    @Autowired
    TradeBasketBallMarketServiceImpl tradeBasketBallMarketService;
    @Autowired
    RcsTradeConfigService rcsTradeConfigService;
    @Autowired
    private RcsTradingAssignmentService rcsTradingAssignmentService;

    @RequestMapping(value = "/updateMarketOddsValue", method = RequestMethod.POST)
    @LogAnnotion(name = "篮球独赢更新水差或赔率", keys = {"matchId",  "playId", "marketIndex","oddsChange","oddsType","marketType","dataSource","matchType","active"},
            title = {"赛事id","玩法id", "位置","赔率变化","投注项名称","盘口类型","数据源类型","赛事阶段早盘或者滚球","是否继续"},urlType = "trade",urlTypeVal = "matchId")
    public HttpResponse updateMarketOddsValue(@RequestBody RcsMatchMarketConfig config) {
        log.info("::{}::篮球独赢更新水差或赔率:{}，操盘手:{}",CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), JSONObject.toJSONString(config), TradeUserUtils.getUserIdNoException());
        try {
            CommonUtils.mdcPut();
            // MTS操盘不处理
            rcsTradeConfigService.isMtsTrade(config, SportIdEnum.BASKETBALL.getId().intValue());
//            boolean b = rcsTradingAssignmentService.tradeJurisdictionByPlayId(null, config.getMatchId(), config.getPlayId(), null);
            boolean b = rcsTradingAssignmentService.hasTraderJurisdiction(config);
            if (!b){
                return HttpResponse.failToMsg("您没有该操作权限！");
            }
            String msg = tradeBasketBallMarketService.updateEUMarketOddsOrWater(config);
            return HttpResponse.success(msg);
        } catch (RcsServiceException e) {
            log.error("::{}::篮球独赢更新水差或赔率:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL,e.getMessage());
        } catch (Exception e) {
            log.error("::{}::篮球独赢更新水差或赔率:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL,"风控服务器出问题");
        } finally {
            CommonUtils.mdcRemove();
        }
    }
}
