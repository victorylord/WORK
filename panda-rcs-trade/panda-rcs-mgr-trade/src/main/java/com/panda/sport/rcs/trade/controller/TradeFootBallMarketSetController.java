package com.panda.sport.rcs.trade.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.google.common.collect.Lists;
import com.panda.sport.rcs.constants.BaseConstants;
import com.panda.sport.rcs.enums.*;
import com.panda.sport.rcs.trade.log.LogContext;
import com.panda.sport.rcs.log.annotion.OperateLog;
import com.panda.sport.rcs.trade.log.format.LogFormatBean;
import com.panda.sport.rcs.trade.log.format.LogFormatPublicBean;
import com.panda.sport.rcs.mapper.RcsStandardOutrightMatchInfoMapper;
import com.panda.sport.rcs.mapper.RcsSysUserMapper;
import com.panda.sport.rcs.mapper.RcsTradeConfigMapper;
import com.panda.sport.rcs.mapper.StandardSportMarketOddsMapper;
import com.panda.sport.rcs.pojo.*;
import com.panda.sport.rcs.trade.enums.LogTypeEnum;
import com.panda.sport.rcs.trade.enums.MatchTradeStatu;
import com.panda.sport.rcs.trade.enums.MatchTradeType;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.IAuthPermissionService;
import com.panda.sport.rcs.trade.wrapper.MatchTradeConfigService;
import com.panda.sport.rcs.trade.wrapper.RcsLogFomatService;
import com.panda.sport.rcs.utils.CommonUtils;
import com.panda.sport.rcs.utils.TradeUserUtils;
import com.panda.sport.rcs.vo.*;
import com.panda.sports.auth.permission.AuthRequiredPermission;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.dubbo.common.utils.Assert;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.rpc.RpcException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.log.annotion.LogAnnotion;
import com.panda.sport.rcs.log.annotion.format.LogFormatAnnotion;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.trade.service.TradeFootBallMarketServiceImpl;
import com.panda.sport.rcs.trade.wrapper.RcsTradeConfigService;
import com.panda.sport.rcs.trade.wrapper.RcsTradingAssignmentService;

import lombok.extern.slf4j.Slf4j;

/**
 * @Description //操盘接口
 * @Param
 * @Author Sean
 * @Date 14:12 2020/10/2
 * @return
 **/
@Component
@RestController
@RequestMapping(value = "trade/footBall")
@Slf4j
public class TradeFootBallMarketSetController {

    @Autowired
    TradeFootBallMarketServiceImpl tradefootBallMarketService;
    @Autowired
    RcsTradeConfigService rcsTradeConfigService;
    @Autowired
    private RcsTradingAssignmentService rcsTradingAssignmentService;
    @Autowired
    private MatchTradeConfigService matchTradeConfigService;
    @Autowired
    IAuthPermissionService iAuthPermissionService;
    @RequestMapping(value = "/updateMarketOddsValue", method = RequestMethod.POST)
    @LogAnnotion(name = "足球更新水差或赔率", keys = {"matchId", "playId", "marketIndex", "oddsChange", "oddsType", "marketType", "dataSource", "matchType", "active"},
            title = {"赛事id", "玩法id", "位置", "赔率变化", "投注项名称", "盘口类型", "数据源类型", "赛事阶段早盘或者滚球", "是否继续"}, urlType = "trade", urlTypeVal = "matchId")
    @LogFormatAnnotion
    @OperateLog(operateType = OperateLogEnum.ODDS_UPDATE)
    public HttpResponse updateMarketOddsValue(@RequestBody RcsMatchMarketConfig config) {
        try {
            CommonUtils.mdcPut();
            // MTS操盘不处理
            rcsTradeConfigService.isMtsTrade(config, SportIdEnum.FOOTBALL.getId().intValue());
//            boolean b = rcsTradingAssignmentService.tradeJurisdictionByPlayId(null, config.getMatchId(), config.getPlayId(), null);
            boolean b = rcsTradingAssignmentService.hasTraderJurisdiction(config);
            if (!b) {
                return HttpResponse.failToMsg("您没有该操作权限！");
            }
            log.info("::{}::足球更新水差或赔率:{}，操盘手:{}",CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), JSONObject.toJSONString(config), TradeUserUtils.getUserIdNoException());
            String msg = tradefootBallMarketService.updateMarketOddsOrWater(config);
            return HttpResponse.success(msg);
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

    /**
     * @return com.panda.sport.rcs.vo.HttpResponse<java.util.Map < java.lang.String, java.lang.Object>>
     * @Description //足球新增盘口
     * @Param [config]
     * @Author sean
     * @Date 2021/2/7
     **/
    @RequestMapping(value = "/updateMatchMarketConfig", method = RequestMethod.POST)
    @LogAnnotion(name = "更新盘口对应配置", keys = {"marketId", "matchId", "playId", "homeLevelFirstMaxAmount", "homeLevelSecondMaxAmount", "homeSingleMaxAmount", "homeMultiMaxAmount",
            "maxSingleBetAmount", "maxOdds", "minOdds", "marketStatus", "awayAutoChangeRate", "homeMargin", "awayMargin", "tieMargin"},
            title = {"盘口Id", "赛事id", "玩法id", "主一级限额", "主二级限额", "上盘单枪限额", "上盘累计限额",
                    "最大单注限额/派奖", "最大赔率", "最小赔率", "状态", "客队水差", "主胜margin", "客胜margin", "平局margin"})
//    @LogFormatAnnotion
    @OperateLog
    public HttpResponse<Map<String, Object>> updateMatchMarketConfig(@RequestBody RcsMatchMarketConfig config) {
        try {
            CommonUtils.mdcPut();
            // MTS操盘不处理
            rcsTradeConfigService.isMtsTrade(config, SportIdEnum.FOOTBALL.getId().intValue());
            boolean b = rcsTradingAssignmentService.hasTraderJurisdiction(config);
            if (!b) {
                return HttpResponse.failToMsg("您没有该操作权限！");
            }
            log.info("::{}::足球更新盘口对应配置:{}，操盘手:{}",CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), JSONObject.toJSONString(config), TradeUserUtils.getUserIdNoException());
            RcsMatchMarketConfig info = tradefootBallMarketService.updateMatchMarketConfig(config);
            //给前端展示用。进行数据刷新
            if (info.getMarketStatus() == MarketStatusEnum.CLOSE.getState()) {
                info.setMarketActive(false);
            } else {
                info.setMarketActive(true);
            }
            return HttpResponse.success(null);
        } catch (RpcException e) {
            log.error("::{}::更新盘口对应配置，融合Rpc异常:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL, "融合Rpc请检查");
        } catch (RcsServiceException e) {
            log.error("::{}::更新盘口对应配置:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL, e.getMessage());
        } catch (Exception e) {
            log.error("::{}::更新盘口对应配置:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL, "服务异常稍后重试");
        } finally {
            CommonUtils.mdcRemove();
        }
    }

    /**
     * @return com.panda.sport.rcs.vo.HttpResponse<java.util.Map < java.lang.String, java.lang.Object>>
     * @Description //查询盘口配置
     * @Param [config]
     * @Author sean
     * @Date 2021/4/30
     **/
    @RequestMapping(value = "/queryMatchMarketConfig", method = RequestMethod.POST)
    public HttpResponse<Map<String, Object>> queryMatchMarketConfig(@RequestBody RcsMatchMarketConfig config) {
        try {
            CommonUtils.mdcPut();
            // MTS操盘不处理
            rcsTradeConfigService.isMtsTrade(config, SportIdEnum.FOOTBALL.getId().intValue());
            RcsMatchMarketConfig rcsMatchMarketConfig = matchTradeConfigService.queryMatchMarketConfig(config);
            return HttpResponse.success(rcsMatchMarketConfig);
        } catch (RcsServiceException e) {
            log.error("::{}::查询盘口配置:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return HttpResponse.error(HttpResponse.FAIL, e.getMessage());
        } catch (Exception e) {
            log.error("::{}::查询盘口配置:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return HttpResponse.error(HttpResponse.FAIL, "风控服务器出问题");
        } finally {
            CommonUtils.mdcRemove();
        }
    }

    @RequestMapping(value = "/updateMarketWater", method = RequestMethod.POST)
    @LogAnnotion(name = "足球更新水差或赔率", keys = {"matchId", "playId", "marketIndex", "marketType", "dataSource", "matchType", "oddsList"},
            title = {"赛事id", "玩法id", "位置", "盘口类型", "数据源类型", "赛事阶段早盘或者滚球", "赔率列表"}, urlType = "trade", urlTypeVal = "matchId")
    @LogFormatAnnotion
    @OperateLog(operateType = OperateLogEnum.CONFIG_UPDATE)
    public HttpResponse updateMarketWater(@RequestBody RcsMatchMarketConfig config) {
        try {
            CommonUtils.mdcPut();
            // MTS操盘不处理
            rcsTradeConfigService.isMtsTrade(config, SportIdEnum.FOOTBALL.getId().intValue());
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

    /**
     * @return com.panda.sport.rcs.vo.HttpResponse<java.util.Map < java.lang.String, java.lang.Object>>
     * @Description //足球新增盘口
     * @Param [config]
     * @Author sean
     * @Date 2021/2/7
     **/
    @RequestMapping(value = "/updateMatchMarketValue", method = RequestMethod.POST)
    @LogAnnotion(name = "更新盘口值", keys = {"marketId", "matchId", "playId", "marketIndex", "homeMarketValue", "matchType", "score"},
            title = {"盘口Id", "赛事id", "玩法id", "位置", "盘口值", "赛事阶段", "比分"})
    @LogFormatAnnotion
    @OperateLog(operateType = OperateLogEnum.MARKET_UPDATE)
    public HttpResponse<Map<String, Object>> updateMatchMarketValue(@RequestBody RcsMatchMarketConfig config) {
        try {
            CommonUtils.mdcPut();
            // MTS操盘不处理
            rcsTradeConfigService.isMtsTrade(config, SportIdEnum.FOOTBALL.getId().intValue());
            boolean b = rcsTradingAssignmentService.hasTraderJurisdiction(config);
            if (!b) {
                return HttpResponse.failToMsg("您没有该操作权限！");
            }
            log.info("::{}::足球更新盘口值:{}，操盘手:{}",CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), JSONObject.toJSONString(config), TradeUserUtils.getUserIdNoException());
            tradefootBallMarketService.updateMatchMarketValue(config);
            return HttpResponse.success();
        } catch (RpcException e) {
            log.error("::{}::足球更新盘口值，融合rpc异常：{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL, "融合Rpc请检查");
        } catch (RcsServiceException e) {
            log.error("::{}::足球更新盘口值:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL, e.getMessage());
        } catch (Exception e) {
            log.error("::{}::足球更新盘口值:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL, "参数输入不合法");
        } finally {
            CommonUtils.mdcRemove();
        }
    }
    /**
     * @Description   //快捷新增盘口
     * @Param [config]
     * @Author  sean
     * @Date   2021/7/25
     * @return com.panda.sport.rcs.vo.HttpResponse<java.util.Map<java.lang.String,java.lang.Object>>
     **/
    @RequestMapping(value = "/convenientCreateMarket", method = RequestMethod.POST)
    @LogAnnotion(name = "快捷新增盘口", keys = {"marketId", "matchId", "playId", "marketIndex", "subPlayId"},
            title = {"盘口Id", "赛事id", "玩法id", "位置", "新盘口的子玩法id"})
    @LogFormatAnnotion
    public HttpResponse<Map<String, Object>> convenientCreateMarket(@RequestBody RcsMatchMarketConfig config) {
        try {
            CommonUtils.mdcPut();
            // MTS操盘不处理
            rcsTradeConfigService.isMtsTrade(config, SportIdEnum.FOOTBALL.getId().intValue());
            boolean b = rcsTradingAssignmentService.hasTraderJurisdiction(config);
            if (!b) {
                return HttpResponse.failToMsg("您没有该操作权限！");
            }
            tradefootBallMarketService.convenientCreateMarket(config);
            return HttpResponse.success();
        } catch (RpcException e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL, "融合Rpc请检查");
        } catch (RcsServiceException e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL, e.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL, "参数输入不合法");
        } finally {
            CommonUtils.mdcRemove();
        }
    }

    @Autowired
    private RcsStandardOutrightMatchInfoMapper standardOutrightMatchInfoMapper;
    @Autowired
    private StandardSportMarketOddsMapper standardSportMarketOddsMapper;
    @Autowired
    private RcsTradeConfigMapper rcsTradeConfigMapper;

    /**
     * @param config
     * @return
     * @Description: 冠军赛事，开关封锁
     * @Author carver
     * @Date 2021/6/14 14:51
     **/
    @PostMapping("/updateChampionMatchTradeStatus")
    @LogAnnotion(name = "更新冠军赛事，开关封锁", keys = {"matchId", "marketId", "marketStatus","tradeLevel"}, title = {"赛事id", "盘口Id", "状态", "操盘级别"})
    @LogFormatAnnotion
    public HttpResponse<Map<String, Object>> championMatchTradeStatus(@RequestBody MarketStatusUpdateVO config) {
        try {
            CommonUtils.mdcPut();
            boolean b = iAuthPermissionService.checkAuthOpearate( "rcs:trade:operate");
            if (!b) {
                return HttpResponse.failToMsg("您没有该操作权限！");
            }
            Assert.notNull(config.getMatchId(), "赛事id不能为空");
            Assert.notNull(config.getMarketId(), "盘口Id不能为空");
            Assert.notNull(config.getMarketStatus(), "状态不能为空");
            Assert.notNull(config.getTradeLevel(), "操盘级别不能为空");
            log.info("::{}::更新冠军赛事，开关封锁:{}，操盘手:{}",CommonUtil.getRequestId(config.getMatchId(),config.getMarketId()), JSONObject.toJSONString(config), TradeUserUtils.getUserIdNoException());

            //获取原有数值并记录日志
            Map<String, Object> dynamicBean = new HashMap<String, Object>();
            Integer oldValue;
            String operateStr;
            dynamicBean.put("obj_id", config.getMatchId() + "-" + config.getMarketId());
            StringBuilder changDesc = new StringBuilder();
            if (TradeLevelEnum.isBetItemLevel(config.getTradeLevel()) && ObjectUtils.isNotEmpty(config.getOddsId())) {
                operateStr = "投注项操盘";
                dynamicBean.put("click_case", "投注项操盘");
                StandardSportMarketOdds oldOdds = standardSportMarketOddsMapper.selectById(config.getOddsId());
                oldValue = oldOdds.getActive() == null ? 0 : oldOdds.getActive();
                changDesc.append(oldOdds.getName());
            } else {
                //盘口开关封锁，记录保存
                operateStr = "玩法操盘";
                dynamicBean.put("click_case", "玩法操盘");
                RcsTradeConfig config1=new RcsTradeConfig();
                config1.setMatchId(config.getMatchId().toString());
                config1.setTraderLevel(config.getTradeLevel());
                config1.setTargerData(config.getMarketId());
                RcsTradeConfig lastConfig = rcsTradeConfigMapper.getRcsTradeConfig(config1);
                oldValue = lastConfig == null ? 0 : lastConfig.getStatus() == null ? 0 : lastConfig.getStatus();
            }
            if(Integer.compare(oldValue, config.getMarketStatus()) != 0){
                LogFormatPublicBean publicBean = new LogFormatPublicBean(LogTypeEnum.CHAMPION_TYPE.getCode()+"", operateStr, String.valueOf(config.getMatchId()));
                LogFormatBean logFormatBean = new LogFormatBean(changDesc.toString(), MatchTradeStatu.getCodeDesc(oldValue),  MatchTradeStatu.getCodeDesc(config.getMarketStatus()));
                LogContext.getContext().addFormatBean(publicBean, dynamicBean, logFormatBean);
            }
            rcsTradeConfigService.championMatchTradeStatus(config);
            return HttpResponse.success();
        } catch (IllegalArgumentException e) {
            log.error("::{}::更新冠军赛事，开关封锁:{}", CommonUtil.getRequestId(config.getMatchId(),config.getMarketId()), e.getMessage(), e);
            return HttpResponse.error(201, e.getMessage());
        } catch (RpcException e) {
            log.error("::{}::更新冠军赛事，开关封锁,融合rpc异常:{}", CommonUtil.getRequestId(config.getMatchId(),config.getMarketId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL, e.getMessage());
        } catch (Exception e) {
            log.error("::{}::更新冠军赛事，开关封锁:{}", CommonUtil.getRequestId(config.getMatchId(),config.getMarketId()), e.getMessage(), e);
            return HttpResponse.error(HttpResponse.FAIL, e.getMessage());
        } finally {
            CommonUtils.mdcRemove();
        }
    }

    /**
     * @param config
     * @return
     * @Description: 冠军赛事，操盘方式切换
     * @Author carver
     * @Date 2021/6/13 14:51
     **/
    @PostMapping("/updateChampionMatchTradeType")
    @LogAnnotion(name = "更新冠军赛事操盘方式", keys = {"matchId", "marketId", "tradeType","tradeLevel"}, title = {"赛事id", "盘口Id", "操盘方式", "操盘级别"})
    @LogFormatAnnotion
    public HttpResponse championMatchTradeType(@RequestBody MarketStatusUpdateVO config) {
        try {
            CommonUtils.mdcPut();
            boolean b = iAuthPermissionService.checkAuthOpearate( "rcs:trade:operate");
            if (!b) {
                return HttpResponse.failToMsg("您没有该操作权限！");
            }
            Assert.notNull(config.getMatchId(), "赛事id不能为空");
            Assert.notNull(config.getMarketId(), "盘口Id不能为空");
            Assert.notNull(config.getTradeType(), "操盘方式不能为空");
            Assert.notNull(config.getTradeLevel(), "操盘级别不能为空");
            log.info("::{}::更新冠军赛事操盘方式:{}，操盘手:{}",CommonUtil.getRequestId(config.getMatchId(),config.getMarketId()), JSONObject.toJSONString(config), TradeUserUtils.getUserIdNoException());

            //获取原有数值并记录日志
            LogFormatPublicBean publicBean = new LogFormatPublicBean(LogTypeEnum.CHAMPION_TYPE.getCode()+"", "操盘方式", String.valueOf(config.getMatchId()));
            Map<String, Object> dynamicBean = new HashMap<String, Object>();
            dynamicBean.put("obj_id", config.getMatchId() + "-" + config.getMarketId());
            dynamicBean.put("click_case", "操盘方式");

            RcsTradeConfig config1=new RcsTradeConfig();
            config1.setMatchId(config.getMatchId().toString());
            config1.setTraderLevel(config.getTradeLevel());
            config1.setTargerData(config.getMarketId());
            RcsTradeConfig lastConfig = rcsTradeConfigMapper.getRcsTradeConfig(config1);
            Integer oldValue;
            if(ObjectUtils.isNotEmpty(lastConfig) && ObjectUtils.isNotEmpty(lastConfig.getDataSource())){
                oldValue = lastConfig.getDataSource();
            }else{
                oldValue = 0;
            }
            if(Integer.compare(oldValue, config.getTradeType()) != 0){
                LogFormatBean logFormatBean = new LogFormatBean("操盘方式",  MatchTradeType.getCodeDesc(oldValue), MatchTradeType.getCodeDesc(config.getTradeType()));
                LogContext.getContext().addFormatBean(publicBean, dynamicBean, logFormatBean);
            }
            rcsTradeConfigService.championMatchTradeType(config);
            return HttpResponse.success();
        } catch (IllegalArgumentException e) {
            log.error("::{}::更新冠军赛事操盘方式:{}", CommonUtil.getRequestId(config.getMatchId(),config.getMarketId()), e.getMessage(), e);
            return HttpResponse.error(201, e.getMessage());
        } catch (RpcException e) {
            log.error("::{}::更新冠军赛事操盘方式,融合rpc异常:{}", CommonUtil.getRequestId(config.getMatchId(),config.getMarketId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL, e.getMessage());
        } catch (Exception e) {
            log.error("::{}::更新冠军赛事操盘方式:{}", CommonUtil.getRequestId(config.getMatchId(),config.getMarketId()), e.getMessage(), e);
            return HttpResponse.error(HttpResponse.FAIL, e.getMessage());
        } finally {
            CommonUtils.mdcRemove();
        }
    }
    /**
     * @return
     * @Description: 冠军赛事，操盘方式切换
     * @Author carver
     * @Date 2021/6/13 14:51
     **/
    @PostMapping("/updateChampionMarketOdds")
    @LogAnnotion(name = "修改冠军玩法赔率", keys = {"matchId", "playId", "marketId", "oddsValueList"}, title = {"赛事id", "玩法id", "盘口id", "投注项数据"})
    @LogFormatAnnotion
    public HttpResponse updateChampionMarketOdds(@RequestBody UpdateOddsValueVo updateOddsValueVo) {
        try {
            CommonUtils.mdcPut();
            boolean b = iAuthPermissionService.checkAuthOpearate( "rcs:trade:operate");
            if (!b) {
                return HttpResponse.failToMsg("您没有该操作权限！");
            }
            Assert.notNull(updateOddsValueVo.getMatchId(), "赛事id不能为空");
            Assert.notNull(updateOddsValueVo.getMarketId(), "盘口Id不能为空");
            Assert.notNull(updateOddsValueVo.getPlayId(), "玩法Id不能为空");
            Assert.notNull(updateOddsValueVo.getOddsValueList(), "赔率不能为空");
            log.info("::{}::修改冠军玩法赔率:{}，操盘手:{}",CommonUtil.getRequestId(updateOddsValueVo.getMatchId(),updateOddsValueVo.getMarketId()), JSONObject.toJSONString(updateOddsValueVo), TradeUserUtils.getUserIdNoException());

            //获取原有数值并记录日志
            LogFormatPublicBean publicBean = new LogFormatPublicBean(LogTypeEnum.CHAMPION_TYPE.getCode()+"", "赔率变更", String.valueOf(updateOddsValueVo.getMatchId()));
            Map<String, Object> dynamicBean = new HashMap<String, Object>();
            dynamicBean.put("obj_id", updateOddsValueVo.getMatchId() + "-" + updateOddsValueVo.getMarketId());
            List<LogFormatBean> logs = Lists.newArrayList();
            for(OddsValueVo oddsVo : updateOddsValueVo.getOddsValueList()){
                dynamicBean.put("click_case", "赔率变更");
                StandardSportMarketOdds oldOdds = standardSportMarketOddsMapper.selectById(oddsVo.getId());
                BigDecimal oldValue = new BigDecimal(oldOdds.getOddsValue()).divide(new BigDecimal(String.valueOf(BaseConstants.MULTIPLE_VALUE)));
                if(oldValue == null) oldValue = new BigDecimal(0.0);
                if(Double.compare(oldValue.doubleValue(), oddsVo.getValue()) != 0){
                    LogFormatBean logFormatBean = new LogFormatBean(oldOdds.getName(), String.valueOf(oldValue), String.valueOf(oddsVo.getValue()));
                    logs.add(logFormatBean);
                }
            }
            LogContext.getContext().addFormatBean(publicBean, dynamicBean, logs);
            tradefootBallMarketService.updateChampionMarketOdds(updateOddsValueVo);
            return HttpResponse.success();
        } catch (IllegalArgumentException e) {
            log.error("::{}::修改冠军玩法赔率：{}", CommonUtil.getRequestId(updateOddsValueVo.getMatchId(),updateOddsValueVo.getMarketId()), e.getMessage(), e);
            return HttpResponse.error(201, e.getMessage());
        } catch (RpcException e) {
            log.error("::{}::修改冠军玩法赔率，融合rpc异常：{}", CommonUtil.getRequestId(updateOddsValueVo.getMatchId(),updateOddsValueVo.getMarketId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL, e.getMessage());
        } catch (Exception e) {
            log.error("::{}::修改冠军玩法赔率：{}", CommonUtil.getRequestId(updateOddsValueVo.getMatchId(),updateOddsValueVo.getMarketId()), e.getMessage(), e);
            return HttpResponse.error(HttpResponse.FAIL, e.getMessage());
        } finally {
            CommonUtils.mdcRemove();
        }
    }

    @Autowired
    RcsSysUserMapper rcsSysUserMapper;

    @Autowired
    private  RcsLogFomatService logFomatService;
    @PostMapping("/championMatchOperateLog/list")
    public HttpResponse getChampionMatchOperateLogs(@RequestBody ChampionMatchOperateLogQueryVo params) {
        try{
            if(StringUtils.isBlank(params.getMatchId()) || StringUtils.isBlank(params.getMarketId())){
                return HttpResponse.error(201, "赛事Id或玩法Id不能为空！");
            }
            String value = params.getMatchId() + "-" + params.getMarketId();
            log.info("::{}:::::getChampionMatchOperateLogs:::获取赛事{}日志",CommonUtil.getRequestId(), value);
            List<RcsLogFomat> rcsLogs = logFomatService.getChampionMatchOperateLogs(value);
            if(CollectionUtils.isNotEmpty(rcsLogs)){
                List<String> userIds = rcsLogs.stream().map(RcsLogFomat :: getUid).collect(Collectors.toList());
                List<RcsSysUser> users = rcsSysUserMapper.selectBatchIds(userIds);
                Map<String, String> userNameMap = users.stream().collect(Collectors.toMap(e -> String.valueOf(e.getId()), RcsSysUser:: getUserCode, (o1,o2) -> o1));
                for(RcsLogFomat log : rcsLogs){
                    log.setUid(userNameMap.get(log.getUid()));
                }
            }
            return HttpResponse.success(rcsLogs);
        }catch (Exception e) {
            log.error("::{}::获取赛事日志：{}", CommonUtil.getRequestId(params.getMatchId(),params.getMarketId()), e.getMessage(), e);
            return HttpResponse.fail("获取日志异常");
        }
    }
}