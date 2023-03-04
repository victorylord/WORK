package com.panda.sport.rcs.trade.controller;

import com.alibaba.fastjson.JSONObject;
import com.panda.sport.rcs.enums.OperateLogEnum;
import com.panda.sport.rcs.enums.SportIdEnum;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.log.annotion.LogAnnotion;
import com.panda.sport.rcs.log.annotion.OperateLog;
import com.panda.sport.rcs.log.annotion.format.LogFormatAnnotion;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.trade.service.TradeFootBallMarketServiceImpl;
import com.panda.sport.rcs.trade.service.TradeTennisMarketSetService;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.MatchTradeConfigService;
import com.panda.sport.rcs.trade.wrapper.RcsTradeConfigService;
import com.panda.sport.rcs.trade.wrapper.RcsTradingAssignmentService;
import com.panda.sport.rcs.utils.CommonUtils;
import com.panda.sport.rcs.utils.TradeUserUtils;
import com.panda.sport.rcs.vo.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.dubbo.rpc.RpcException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @Description //操盘接口
 * @Param
 * @Author Sean
 * @Date 14:12 2020/10/2
 * @return
 **/
@Component
@RestController
@RequestMapping(value = "trade/tennis")
@Slf4j
public class TradeTennisMarketSetController {

    @Autowired
    TradeTennisMarketSetService tradeTennisMarketService;
    @Autowired
    RcsTradeConfigService rcsTradeConfigService;
    @Autowired
    private RcsTradingAssignmentService rcsTradingAssignmentService;
    
    @Autowired
    TradeFootBallMarketServiceImpl tradefootBallMarketService;

    @RequestMapping(value = "/updateMarketOddsValue", method = RequestMethod.POST)
    @LogAnnotion(name = "综合独赢更新水差或赔率", keys = {"matchId",  "playId", "marketIndex","oddsChange","oddsType","marketType","dataSource","matchType","active"},
            title = {"赛事id","玩法id", "位置","赔率变化","投注项名称","盘口类型","数据源类型","赛事阶段早盘或者滚球","是否继续"},urlType = "trade",urlTypeVal = "matchId")
    public HttpResponse updateMarketOddsValue(@RequestBody RcsMatchMarketConfig config) {
        try {
            CommonUtils.mdcPut();
            // MTS操盘不处理
            rcsTradeConfigService.isMtsTrade(config, SportIdEnum.TENNIS.getId().intValue());
            boolean b = rcsTradingAssignmentService.hasTraderJurisdiction(config);
            if (!b){
                return HttpResponse.failToMsg("您没有该操作权限！");
            }
            log.info("::{}::综合独赢更新水差或赔率:{}，操盘手:{}",CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), JSONObject.toJSONString(config), TradeUserUtils.getUserIdNoException());
            String msg = tradeTennisMarketService.updateEUMarketOddsOrWater(config);
            return HttpResponse.success(msg);
        } catch (RcsServiceException e) {
            log.error("::{}::综合独赢更新水差或赔率:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL,e.getMessage());
        } catch (Exception e) {
            log.error("::{}::综合独赢更新水差或赔率:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL,"风控服务器出问题");
        } finally {
            CommonUtils.mdcRemove();
        }
    }
    
    @RequestMapping(value = "/updateMarketWater", method = RequestMethod.POST)
    @LogAnnotion(name = "冰球独赢三项盘更新水差或赔率", keys = {"matchId", "playId", "marketIndex", "marketType", "dataSource", "matchType", "oddsList"},
            title = {"赛事id", "玩法id", "位置", "盘口类型", "数据源类型", "赛事阶段早盘或者滚球", "赔率列表"}, urlType = "trade", urlTypeVal = "matchId")
    @LogFormatAnnotion
    @OperateLog(operateType = OperateLogEnum.CONFIG_UPDATE)
    public HttpResponse updateMarketWater(@RequestBody RcsMatchMarketConfig config) {
        try {
            CommonUtils.mdcPut();
            // MTS操盘不处理
            rcsTradeConfigService.isMtsTrade(config, SportIdEnum.ICE_HOCKEY.getId().intValue());
            boolean b = rcsTradingAssignmentService.hasTraderJurisdiction(config);
            if (!b) {
                return HttpResponse.failToMsg("您没有该操作权限！");
            }
            log.info("::{}::足球更新水差或赔率:{}，操盘手:{}",CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), JSONObject.toJSONString(config), TradeUserUtils.getUserIdNoException());
            String msg = tradefootBallMarketService.updateMarketWater(config, NumberUtils.INTEGER_ONE);
            return HttpResponse.success(msg);
        }catch (RpcException e) {
            log.error("::{}::足球更新水差或赔率,融合RPC异常:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL,e.getMessage());
        } catch (RcsServiceException e) {
            log.error("::{}::足球更新水差或赔率:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL, e.getMessage());
        } catch (Exception e) {
            log.error("::{}::足球更新水差或赔率:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL, "风控服务器出问题");
        } finally {
            CommonUtils.mdcRemove();
        }
    }
    
    
    

    @RequestMapping(value = "/updateMatchMarketConfig", method = RequestMethod.POST)
    @LogAnnotion(name = "综合更新盘口对应配置", keys = {"marketIndex","marketId", "matchId","playId", "waitSeconds", "homeMarketValue", "awayMarketValue", "margin","homeLevelFirstMaxAmount",
            "homeLevelFirstOddsRate", "homeLevelSecondMaxAmount", "homeLevelSecondOddsRate","awayLevelFirstOddsRate", "awayLevelSecondOddsRate", "homeSingleMaxAmount", "homeMultiMaxAmount", "homeSingleOddsRate","homeMultiOddsRate", "awaySingleOddsRate","awayMultiOddsRate","maxSingleBetAmount", "maxOdds", "minOdds", "dataSource", "homeMargin",
            "awayMargin", "tieMargin", "matchType", "marketType", "awayAutoChangeRate"},
            title = {"位置Id","盘口Id", "赛事id", "玩法id", "等待时间", "主队盘口值", "客队盘口值", "margin值", "主一级限额", "主一级赔率变化率",
                    "主二级限额", "主二级赔率变化率", "客一级赔率变化率","客二级赔率变化率","主单枪限额", "主单枪赔率变化率",
                    "主累计限额", "主累计赔率变化率", "客单枪赔率变化率","客累计赔率变化率","最大单注限额", "最大赔率", "最小赔率", "是否使用数据源0", "操盘类型", "客队水差"},urlType = "trade",urlTypeVal = "matchId")
    public HttpResponse<RcsMatchMarketConfig> updateMatchMarketConfig(@RequestBody RcsMatchMarketConfig config) {
        try {
            CommonUtils.mdcPut();
            // MTS操盘不处理
            rcsTradeConfigService.isMtsTrade(config,SportIdEnum.TENNIS.getId().intValue());
            boolean b = rcsTradingAssignmentService.hasTraderJurisdiction(config);
            if (!b){
                return HttpResponse.failToMsg("您没有该操作权限！");
            }
            log.info("::{}::综合更新盘口对应配置:{}，操盘手:{}",CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), JSONObject.toJSONString(config), TradeUserUtils.getUserIdNoException());
            tradeTennisMarketService.updateMatchMarketConfig(config);
            return HttpResponse.success();
        }catch (RcsServiceException e) {
            log.error("::{}::更新盘口对应配置:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL, e.getMessage());
        }catch (RpcException e) {
            log.error("::{}::更新盘口对应配置,融合RPC异常:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL,e.getMessage());
        } catch (Exception e) {
            log.error("::{}::更新盘口对应配置:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL,"风控服务器出问题");
        } finally {
            CommonUtils.mdcRemove();
        }
    }

/**
 * @Description   //查询网球盘口配置
 * @Param [config]
 * @Author  sean
 * @Date   2021/9/5
 * @return com.panda.sport.rcs.vo.HttpResponse<java.util.Map<java.lang.String,java.lang.Object>>
 **/
    @RequestMapping(value = "/queryMatchMarketConfig", method = RequestMethod.POST)
    @LogAnnotion(name = "查询盘口对应配置", keys = {"marketIndex","marketId", "matchId","playId", "waitSeconds", "homeMarketValue", "awayMarketValue", "margin","homeLevelFirstMaxAmount",
            "homeLevelFirstOddsRate", "homeLevelSecondMaxAmount", "homeLevelSecondOddsRate","awayLevelFirstOddsRate", "awayLevelSecondOddsRate", "homeSingleMaxAmount", "homeMultiMaxAmount", "homeSingleOddsRate","homeMultiOddsRate", "awaySingleOddsRate","awayMultiOddsRate","maxSingleBetAmount", "maxOdds", "minOdds", "dataSource", "homeMargin",
            "awayMargin", "tieMargin", "matchType", "marketType", "awayAutoChangeRate"},
            title = {"位置Id","盘口Id", "赛事id", "玩法id", "等待时间", "主队盘口值", "客队盘口值", "margin值", "主一级限额", "主一级赔率变化率",
                    "主二级限额", "主二级赔率变化率", "客一级赔率变化率","客二级赔率变化率","主单枪限额", "主单枪赔率变化率",
                    "主累计限额", "主累计赔率变化率", "客单枪赔率变化率","客累计赔率变化率","最大单注限额", "最大赔率", "最小赔率", "是否使用数据源0", "操盘类型", "客队水差"},urlType = "trade",urlTypeVal = "matchId")
    public HttpResponse<Map<String, Object>> queryMatchMarketConfig(@RequestBody RcsMatchMarketConfig config) {
        try {
            CommonUtils.mdcPut();
            // MTS操盘不处理
            rcsTradeConfigService.isMtsTrade(config,config.getSportId());
            RcsMatchMarketConfig rcsMatchMarketConfig = tradeTennisMarketService.queryMatchMarketConfig(config);
            return HttpResponse.success(rcsMatchMarketConfig);
        } catch (RcsServiceException e) {
            log.error("::{}::查询网球盘口配置:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return HttpResponse.error(HttpResponse.FAIL, e.getMessage());
        } catch (Exception e) {
            log.error("::{}::查询网球盘口配置:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return HttpResponse.error(HttpResponse.FAIL, "风控服务器出问题");
        } finally {
            CommonUtils.mdcRemove();
        }
    }

    @RequestMapping(value = "/updateMarketAutoRatio", method = RequestMethod.POST)
    @LogAnnotion(name = "综合更新水差", keys = {"matchId",  "playId", "marketId","oddsChange","oddsType","marketType","dataSource","matchType"},
            title = {"赛事id","玩法id", "盘口id","赔率变化","投注项名称","盘口类型","数据源类型","赛事阶段早盘或者滚球"},urlType = "trade",urlTypeVal = "matchId")
    public HttpResponse updateMarketAutoRatio(@RequestBody RcsMatchMarketConfig config) {
        try {
            CommonUtils.mdcPut();
            Assert.notNull(config.getOddsChange(),"水差不能为空");
            // MTS操盘不处理
            rcsTradeConfigService.isMtsTrade(config, SportIdEnum.TENNIS.getId().intValue());
            boolean b = rcsTradingAssignmentService.hasTraderJurisdiction( config);
            if (!b){
                return HttpResponse.failToMsg("您没有该操作权限！");
            }
            log.info("::{}::综合更新水差:{}，操盘手:{}",CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), JSONObject.toJSONString(config), TradeUserUtils.getUserIdNoException());
            String msg = tradeTennisMarketService.updateMarketAutoRatio(config);
            return HttpResponse.success(msg);
        } catch (RcsServiceException e) {
            log.error("::{}::综合更新水差:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL,e.getMessage());
        } catch (IllegalArgumentException e){
            log.error("::{}::综合更新水差:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL,e.getMessage());
        }catch (Exception e) {
            log.error("::{}::综合更新水差:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL,"风控服务器出问题");
        } finally {
            CommonUtils.mdcRemove();
        }
    }
    @RequestMapping(value = "/updateMarketHeadGap", method = RequestMethod.POST)
    @LogAnnotion(name = "综合更新盘口差", keys = {"matchId",  "playId", "marketHeadGap","dataSource","matchType","relevanceType"},
            title = {"赛事id","玩法id", "盘口差","数据源类型","赛事阶段早盘或者滚球","是否关联"},urlType = "trade",urlTypeVal = "matchId")
    public HttpResponse updateMarketHeadGap(@RequestBody RcsMatchMarketConfig config, @RequestHeader(value = "lang",required = false)String lang) {
        try {
            CommonUtils.mdcPut();
            // MTS操盘不处理
            rcsTradeConfigService.isMtsTrade(config, SportIdEnum.TENNIS.getId().intValue());
            boolean b = rcsTradingAssignmentService.hasTraderJurisdiction(config);
            if (!b){
                return HttpResponse.failToMsg("您没有该操作权限！");
            }
            log.info("::{}::综合更新盘口差:{}，操盘手:{}",CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), JSONObject.toJSONString(config), TradeUserUtils.getUserIdNoException());
            String msg = tradeTennisMarketService.updateMarketHeadGap(config,lang);
            return HttpResponse.success(msg);
        } catch (RcsServiceException e) {
            log.error("::{}::updateMarketHeadGap:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL,e.getMessage());
        } catch (Exception e) {
            log.error("::{}::updateMarketHeadGap:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL,"风控服务器出问题");
        } finally {
            CommonUtils.mdcRemove();
        }
    }

    @RequestMapping(value = "/reductionWater", method = RequestMethod.POST)
    @LogAnnotion(name = "综合还原水差", keys = {"matchId",  "playId", "marketIndex","marketType","relevanceType"},
            title = {"赛事id","玩法id", "位置","盘口类型","是否关联"},urlType = "trade",urlTypeVal = "matchId")
    public HttpResponse reductionWater(@RequestBody RcsMatchMarketConfig config) {
        try {
            CommonUtils.mdcPut();
            // MTS操盘不处理
            rcsTradeConfigService.isMtsTrade(config, SportIdEnum.TENNIS.getId().intValue());
            boolean b = rcsTradingAssignmentService.hasTraderJurisdiction(config);
            if (!b){
                return HttpResponse.failToMsg("您没有该操作权限！");
            }
            log.info("::{}::综合还原水差:{}，操盘手:{}",CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), JSONObject.toJSONString(config), TradeUserUtils.getUserIdNoException());
            String msg = tradeTennisMarketService.reductionWater(config);
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
            rcsTradeConfigService.isMtsTrade(config, SportIdEnum.TENNIS.getId().intValue());
//            if (org.springframework.util.ObjectUtils.isEmpty(config.getSportId())){
//                config.setSportId(SportIdEnum.TENNIS.getId().intValue());
//            }
//            config.setMatchType(!ObjectUtils.isEmpty(config.getMatchType()) && config.getMatchType() ==2 ? NumberUtils.INTEGER_ZERO:config.getMatchType());
            Map<String,Object> map = tradeTennisMarketService.queryOriginalOdds(config);
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