package com.panda.sport.rcs.trade.controller;

import com.alibaba.fastjson.JSONObject;
import com.panda.sport.rcs.constants.BaseConstants;
import com.panda.sport.rcs.enums.SportIdEnum;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.log.annotion.LogAnnotion;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.trade.service.TradeMarketSetServiceImpl;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.RcsTradeConfigService;
import com.panda.sport.rcs.trade.wrapper.RcsTradingAssignmentService;
import com.panda.sport.rcs.utils.CommonUtils;
import com.panda.sport.rcs.utils.TradeUserUtils;
import com.panda.sport.rcs.vo.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @Description   //操盘接口
 * @Param
 * @Author  Sean
 * @Date  14:12 2020/10/2
 * @return
 **/
@Component
@RestController
@RequestMapping(value = "trade")
@Slf4j
public class TradeMarketSetController {

    @Autowired
    TradeMarketSetServiceImpl tradeMarketSetService;
    @Autowired
    RcsTradeConfigService rcsTradeConfigService;
    @Autowired
    private RcsTradingAssignmentService rcsTradingAssignmentService;
    @Autowired
    private ProducerSendMessageUtils producerSendMessageUtils;

    @RequestMapping(value = "/queryMatchMarketConfig", method = RequestMethod.POST)
    @LogAnnotion(name = "查询盘口对应配置", keys = {"marketIndex","marketId", "matchId","playId", "waitSeconds", "homeMarketValue", "awayMarketValue", "margin","homeLevelFirstMaxAmount",
            "homeLevelFirstOddsRate", "homeLevelSecondMaxAmount", "homeLevelSecondOddsRate","awayLevelFirstOddsRate", "awayLevelSecondOddsRate", "homeSingleMaxAmount", "homeMultiMaxAmount", "homeSingleOddsRate","homeMultiOddsRate", "awaySingleOddsRate","awayMultiOddsRate","maxSingleBetAmount", "maxOdds", "minOdds", "dataSource", "homeMargin",
            "awayMargin", "tieMargin", "matchType", "marketType", "awayAutoChangeRate"},
            title = {"位置Id","盘口Id", "赛事id", "玩法id", "等待时间", "主队盘口值", "客队盘口值", "margin值", "主一级限额", "主一级赔率变化率",
                    "主二级限额", "主二级赔率变化率", "客一级赔率变化率","客二级赔率变化率","主单枪限额", "主单枪赔率变化率",
                    "主累计限额", "主累计赔率变化率", "客单枪赔率变化率","客累计赔率变化率","最大单注限额", "最大赔率", "最小赔率", "是否使用数据源0", "操盘类型", "客队水差"},urlType = "trade",urlTypeVal = "matchId")
    public HttpResponse<RcsMatchMarketConfig> queryMatchMarketConfig(@RequestBody RcsMatchMarketConfig config) {
        try {
            CommonUtils.mdcPut();
            // MTS操盘不处理
            rcsTradeConfigService.isMtsTrade(config, SportIdEnum.BASKETBALL.getId().intValue());
            RcsMatchMarketConfig rcsMatchMarketConfig = tradeMarketSetService.queryMatchMarketConfig(config);
            log.info("::{}::queryMatchMarketConfig查询数据：{}",CommonUtil.getRequestId(), JSONObject.toJSONString(rcsMatchMarketConfig));
            return HttpResponse.success(rcsMatchMarketConfig);
        } catch (RcsServiceException e) {
            log.error("::{}::查询盘口对应配置:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL,e.getMessage());
        } catch (Exception e) {
            log.error("::{}::查询盘口对应配置:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL,"风控服务器出问题");
        } finally {
            CommonUtils.mdcRemove();
        }
    }
    @RequestMapping(value = "/updateMatchMarketConfig", method = RequestMethod.POST)
    @LogAnnotion(name = "更新盘口对应配置", keys = {"marketIndex","marketId", "matchId","playId", "waitSeconds", "homeMarketValue", "awayMarketValue", "margin","homeLevelFirstMaxAmount",
            "homeLevelFirstOddsRate", "homeLevelSecondMaxAmount", "homeLevelSecondOddsRate","awayLevelFirstOddsRate", "awayLevelSecondOddsRate", "homeSingleMaxAmount", "homeMultiMaxAmount", "homeSingleOddsRate","homeMultiOddsRate", "awaySingleOddsRate","awayMultiOddsRate","maxSingleBetAmount", "maxOdds", "minOdds", "dataSource", "homeMargin",
            "awayMargin", "tieMargin", "matchType", "marketType", "awayAutoChangeRate"},
            title = {"位置Id","盘口Id", "赛事id", "玩法id", "等待时间", "主队盘口值", "客队盘口值", "margin值", "主一级限额", "主一级赔率变化率",
                    "主二级限额", "主二级赔率变化率", "客一级赔率变化率","客二级赔率变化率","主单枪限额", "主单枪赔率变化率",
                    "主累计限额", "主累计赔率变化率", "客单枪赔率变化率","客累计赔率变化率","最大单注限额", "最大赔率", "最小赔率", "是否使用数据源0", "操盘类型", "客队水差"},urlType = "trade",urlTypeVal = "matchId")
    public HttpResponse<RcsMatchMarketConfig> updateMatchMarketConfig(@RequestBody RcsMatchMarketConfig config) {
        try {
            CommonUtils.mdcPut();
            // MTS操盘不处理
            rcsTradeConfigService.isMtsTrade(config, SportIdEnum.BASKETBALL.getId().intValue());
            boolean b = rcsTradingAssignmentService.hasTraderJurisdiction(config);
            if (!b){
                return HttpResponse.failToMsg("您没有该操作权限！");
            }
            log.info("::{}::更新盘口对应配置:{}，操盘手:{}",CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), JSONObject.toJSONString(config), TradeUserUtils.getUserIdNoException());
            RcsMatchMarketConfig rcsMatchMarketConfig = tradeMarketSetService.updateMatchMarketConfig(config);
            return HttpResponse.success(rcsMatchMarketConfig);
        } catch (RcsServiceException e) {
            log.error("::{}::更新盘口对应配置：{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL,e.getMessage());
        } catch (Exception e) {
            log.error("::{}::更新盘口对应配置：{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL,"风控服务器出问题");
        } finally {
            CommonUtils.mdcRemove();
        }
    }
    @RequestMapping(value = "/updateMarketOddsValue", method = RequestMethod.POST)
    @LogAnnotion(name = "更新赔率值", keys = {"matchId",  "playId", "marketIndex","oddsChange","oddsType","marketType","dataSource","matchType"},
            title = {"赛事id","玩法id", "位置","赔率变化","投注项名称","盘口类型","数据源类型","赛事阶段早盘或者滚球"},urlType = "trade",urlTypeVal = "matchId")
    public HttpResponse updateMarketOddsValue(@RequestBody RcsMatchMarketConfig config) {
        try {
            CommonUtils.mdcPut();
            // MTS操盘不处理
            rcsTradeConfigService.isMtsTrade(config, SportIdEnum.BASKETBALL.getId().intValue());
            boolean b = rcsTradingAssignmentService.hasTraderJurisdiction(config);
            if (!b){
                return HttpResponse.failToMsg("您没有该操作权限！");
            }
            log.info("::{}::更新赔率值:{}，操盘手:{}",CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), JSONObject.toJSONString(config), TradeUserUtils.getUserIdNoException());
            tradeMarketSetService.updateMarketOddsValue(config);
            return HttpResponse.success();
        } catch (RcsServiceException e) {
            log.error("::{}::updateMarketOddsValue:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL,e.getMessage());
        } catch (Exception e) {
            log.error("::{}::updateMarketOddsValue:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL,"风控服务器出问题");
        } finally {
            CommonUtils.mdcRemove();
        }
    }
    @RequestMapping(value = "/updateMarketAutoRatio", method = RequestMethod.POST)
    @LogAnnotion(name = "更新水差", keys = {"matchId",  "playId", "marketId","oddsChange","oddsType","marketType","dataSource","matchType"},
            title = {"赛事id","玩法id", "盘口id","赔率变化","投注项名称","盘口类型","数据源类型","赛事阶段早盘或者滚球"},urlType = "trade",urlTypeVal = "matchId")
    public HttpResponse updateMarketAutoRatio(@RequestBody RcsMatchMarketConfig config) {
        try {
            CommonUtils.mdcPut();
            Assert.notNull(config.getOddsChange(),"水差不能为空");
            // MTS操盘不处理
            rcsTradeConfigService.isMtsTrade(config, SportIdEnum.BASKETBALL.getId().intValue());
            boolean b = rcsTradingAssignmentService.hasTraderJurisdiction(config);
            if (!b){
                return HttpResponse.failToMsg("您没有该操作权限！");
            }
            log.info("::{}::更新水差:{}，操盘手:{}",CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), JSONObject.toJSONString(config), TradeUserUtils.getUserIdNoException());
            String msg = tradeMarketSetService.updateMarketAutoRatio(config);
            return HttpResponse.success(msg);
        } catch (RcsServiceException e) {
            log.error("::{}::updateMarketAutoRatio:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL,e.getMessage());
        } catch (IllegalArgumentException e){
            log.error("::{}::updateMarketAutoRatio:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL,e.getMessage());
        }catch (Exception e) {
            log.error("::{}::updateMarketAutoRatio:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL,"风控服务器出问题");
        } finally {
            CommonUtils.mdcRemove();
        }
    }
    @RequestMapping(value = "/updateMarketHeadGap", method = RequestMethod.POST)
    @LogAnnotion(name = "更新盘口差", keys = {"matchId",  "playId", "marketHeadGap","dataSource","matchType","relevanceType"},
            title = {"赛事id","玩法id", "盘口差","数据源类型","赛事阶段早盘或者滚球","是否关联"},urlType = "trade",urlTypeVal = "matchId")
    public HttpResponse updateMarketHeadGap(@RequestBody RcsMatchMarketConfig config, @RequestHeader(value = "lang",required = false)String lang) {
        try {
            CommonUtils.mdcPut();
            // MTS操盘不处理
            rcsTradeConfigService.isMtsTrade(config, SportIdEnum.BASKETBALL.getId().intValue());
            boolean b = rcsTradingAssignmentService.hasTraderJurisdiction(config);
            if (!b){
                return HttpResponse.failToMsg("您没有该操作权限！");
            }
            log.info("::{}::更新盘口差:{}，操盘手:{}",CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), JSONObject.toJSONString(config), TradeUserUtils.getUserIdNoException());
            String msg = tradeMarketSetService.updateMarketHeadGap(config,lang);
            return HttpResponse.success(msg);
        } catch (RcsServiceException e) {
            log.error("::{}::更新盘口差:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL,e.getMessage());
        } catch (Exception e) {
            log.error("::{}::更新盘口差:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL,"风控服务器出问题");
        } finally {
            CommonUtils.mdcRemove();
        }
    }

    @RequestMapping(value = "/reductionWater", method = RequestMethod.POST)
    @LogAnnotion(name = "还原水差", keys = {"matchId",  "playId", "marketIndex","marketType","relevanceType"},
            title = {"赛事id","玩法id", "位置","盘口类型","是否关联"},urlType = "trade",urlTypeVal = "matchId")
    public HttpResponse reductionWater(@RequestBody RcsMatchMarketConfig config) {
        try {
            CommonUtils.mdcPut();
            // MTS操盘不处理
            rcsTradeConfigService.isMtsTrade(config, SportIdEnum.BASKETBALL.getId().intValue());
            boolean b = rcsTradingAssignmentService.hasTraderJurisdiction(config);
            if (!b){
                return HttpResponse.failToMsg("您没有该操作权限！");
            }
            log.info("::{}::还原水差:{}，操盘手:{}",CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), JSONObject.toJSONString(config), TradeUserUtils.getUserIdNoException());
            String msg = tradeMarketSetService.reductionWater(config);
            return HttpResponse.success(msg);
        } catch (RcsServiceException e) {
            log.error("::{}::还原水差:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL,e.getMessage());
        } catch (Exception e) {
            log.error("::{}::还原水差:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL,"风控服务器出问题");
        } finally {
            CommonUtils.mdcRemove();
        }
    }

    @RequestMapping(value = "/queryOriginalOdds", method = RequestMethod.POST)
    @LogAnnotion(name = "查询原始赔率", keys = {"matchId",  "playId", "marketIndex","marketType","margin","matchType"},
            title = {"赛事id","玩法id", "位置","盘口类型","margin","赛事阶段"})
    public HttpResponse queryOriginalOdds(@RequestBody RcsMatchMarketConfig config) {
        try {
            CommonUtils.mdcPut();
//            if (ObjectUtils.isEmpty(config.getSportId())){
//                config.setSportId(SportIdEnum.BASKETBALL.getId().intValue());
//            }
//            config.setMatchType(!ObjectUtils.isEmpty(config.getMatchType()) && config.getMatchType() ==2 ? NumberUtils.INTEGER_ZERO:config.getMatchType());
            rcsTradeConfigService.isMtsTrade(config, SportIdEnum.BASKETBALL.getId().intValue());
            Map<String,Object> map = tradeMarketSetService.queryOriginalOdds(config);
            return HttpResponse.success(map);
        } catch (RcsServiceException e) {
            log.error("::{}::查询原始赔率:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL,e.getMessage());
        } catch (Exception e) {
            log.error("::{}::查询原始赔率:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL,"风控服务器出问题");
        } finally {
            CommonUtils.mdcRemove();
        }
    }

}
