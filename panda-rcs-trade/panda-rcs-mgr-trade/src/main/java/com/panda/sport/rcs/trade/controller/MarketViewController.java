package com.panda.sport.rcs.trade.controller;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.panda.aoodds.sports.api.entity.Response;
import com.panda.aoodds.sports.api.service.ApplyService;
import com.panda.sport.data.rcs.api.BalanceValueService;
import com.panda.sport.rcs.common.DateUtils;
import com.panda.sport.rcs.common.NumberUtils;
import com.panda.sport.rcs.constants.RcsConstant;
import com.panda.sport.rcs.constants.RedisKey;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.enums.*;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.log.annotion.LogAnnotion;
import com.panda.sport.rcs.log.annotion.OperateLog;
import com.panda.sport.rcs.log.annotion.format.LogFormatAnnotion;
import com.panda.sport.rcs.pojo.*;
import com.panda.sport.rcs.trade.enums.MatchTradeStatu;
import com.panda.sport.rcs.trade.log.format.LogFormatBean;
import com.panda.sport.rcs.trade.log.format.LogFormatPublicBean;
import com.panda.sport.rcs.mapper.*;
import com.panda.sport.rcs.mongo.*;
import com.panda.sport.rcs.pojo.danger.RcsDangerTeam;
import com.panda.sport.rcs.pojo.danger.RcsDangerTournament;
import com.panda.sport.rcs.pojo.dto.TradeCacheDataDTO;
import com.panda.sport.rcs.pojo.statistics.RcsProfitRectangle;
import com.panda.sport.rcs.pojo.vo.ThirdDataSourceCodeVo;
import com.panda.sport.rcs.trade.cache.TradeDataCache;
import com.panda.sport.rcs.trade.enums.LogTypeEnum;
import com.panda.sport.rcs.trade.enums.MatchTypeEnum;
import com.panda.sport.rcs.trade.log.LogContext;
import com.panda.sport.rcs.trade.param.RcsMatchConfigParam;
import com.panda.sport.rcs.trade.service.TradeModeService;
import com.panda.sport.rcs.trade.service.TradeStatusService;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.utils.mongopage.MongoPageHelper;
import com.panda.sport.rcs.trade.utils.mongopage.PageResult;
import com.panda.sport.rcs.trade.vo.MarketBalanceVo;
import com.panda.sport.rcs.trade.vo.MarketProfitVo;
import com.panda.sport.rcs.trade.wrapper.*;
import com.panda.sport.rcs.utils.CommonUtils;
import com.panda.sport.rcs.utils.TradeUserUtils;
import com.panda.sport.rcs.vo.*;
import com.panda.sport.rcs.vo.trade.TradePlayVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.utils.Assert;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.rpc.RpcException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.panda.sport.rcs.constants.RedisKey.EXPRIY_TIME_7_DAYS;
import static com.panda.sport.rcs.constants.RedisKeys.RCS_MATCH_MONGO_SET;


@Component
@RestController
@RequestMapping(value = "marketView")
@Slf4j
@Api(value = "marketView", tags = "操盘相关接口")
public class MarketViewController {

    private final static String DANGER_TOURNAMENT_KEY = "rcs:danger:tournament:";

    private final static String DANGER_TEAM_KEY = "rcs:danger:team:";

    static final String MATCH_TEAM_KEY = "rcs:key:match:team:";

    private static boolean openPreAllMatch_Switch = false;//开启早盘所有赛事状态 预防重复点击
    private static boolean openLiveAllMatch_Switch = false;//开启滚球所有赛事状态 预防重复点击

    @Autowired
    RcsBusinessSingleBetConfigService businessSingleBetConfigService;

    @Autowired
    RcsMarketOddsConfigService rcsMarketOddsConfigService;

    @Autowired
    StandardSportTournamentService standardSportTournamentService;

    @Autowired
    private RcsTournamentOperateMarketMapper rcsTournamentOperateMarketMapper;

    @Autowired
    private StandardSportMarketService standardSportMarketService;

    @Autowired
    private MarketViewService marketViewService;

    @Autowired
    private IRcsMatchMarketConfigService iRcsMatchMarketConfigService;

    @Autowired
    private RcsMatchCollectionService matchCollectionService;

    @Autowired
    private StandardSportMarketMapper marketMapper;

    @Autowired
    private RcsTournamentOperateMarketService rcsTournamentOperateMarketService;

    @Reference(retries = 3, lazy = true, check = false)
    private BalanceValueService balanceValueService;

    @Autowired
    private RcsMatchConfigService rcsMatchConfigService;

    @Autowired
    private RedisClient redisClient;

    @Autowired
    MongoPageHelper mongoPageHelper;

    @Autowired
    private StandardMatchInfoMapper standardMatchInfoMapper;

    @Autowired
    private RcsTradeConfigService rcsTradeConfigService;

    @Autowired
    private MatchSetService matchSetService;

    @Autowired
    private MarketStatusService marketStatusService;

    @Autowired
    private TradeModeService tradeModeService;

    @Autowired
    private TradeStatusService tradeStatusService;

    @Autowired
    private MatchTradeConfigService matchTradeConfigService;

    @Autowired
    private RcsOddsConvertMappingMyMapper rcsOddsConvertMappingMyMapper;

    @Autowired
    private RcsTradingAssignmentService rcsTradingAssignmentService;

    @Autowired
    private SportMatchViewService sportMatchViewService;

    @Reference(check = false, lazy = true, retries = 3, timeout = 5000)
    ApplyService applyService;

    @Autowired
    private IStandardSportTypeService standardSportTypeService;

    /**
     * @MethodName: collectNum
     * @Description: 收藏数量
     * @Date: 2019/10/24
     **/
    @RequestMapping(value = "collectNum")
    public HttpResponse<Integer> collectNum(@RequestBody RcsMatchCollection matchCollection) throws Exception {
        try{
            Integer userId = TradeUserUtils.getUserId();
            matchCollection.setStatus(1);
            matchCollection.setUserId(Long.valueOf(userId));
            if (matchCollection.getMatchType() == null) {
                matchCollection.setMatchType(1);
            }
            Integer integer = matchCollectionService.selectMatchCollectionCount(matchCollection);
            return HttpResponse.success(integer);

        }catch (Exception e){
            log.error("::{}::收藏数量:{}", CommonUtil.getRequestId(matchCollection.getMatchId()), e.getMessage(), e);
            return HttpResponse.fail(0);
        }
    }


    /**
     * 查询盘口对应下注详情
     *
     * @param config
     * @return
     */
    @RequestMapping(value = "/getBalancesByMatchIdAndPlayId", method = RequestMethod.GET)
    public HttpResponse<Map<String, Object>> getBalancesByMatchIdAndPlayId(RcsMatchMarketConfig config, @RequestParam(value = "matchType", defaultValue = "1", required = true) Integer matchType) {
        try {
            Assert.notNull(config.getMatchId(), "赛事ID不能为空！");
            Assert.notNull(config.getPlayId(), "玩法ID不能为空！");
            Map<String, Object> info = matchTradeConfigService.getBalancesByMatchIdAndPlayId(config, matchType);

            return HttpResponse.success(info);
        } catch (Exception e) {
            log.error("::{}::查询盘口对应下注详情:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return HttpResponse.fail("风控服务器出问题");
        }
    }

    /**
     * 单双/双方均进球玩法期望值
     *
     * @param marketId
     * @return
     */
    @RequestMapping(value = "/getSimpleExpectValue", method = RequestMethod.GET)
    public HttpResponse<Map<String, Object>> getExpectValueByMarketId(Long marketId) {
        try {
            Assert.notNull(marketId, "盘口ID不能为空！");
            StandardSportMarket market = standardSportMarketService.getById(marketId);
            List<OrderDetailStatReportVo> list = rcsMarketOddsConfigService.queryMarketStatByMarketId(marketId);
            //标准化输出
            Map<String, Object> info = Maps.newHashMap();
            info.put("values", list);
            return HttpResponse.success(info);
        } catch (Exception e) {
            log.error("::{}::单双/双方均进球玩法期望值:{}", CommonUtil.getRequestId(marketId), e.getMessage(), e);
            return HttpResponse.fail("风控服务器出问题" + ":" + e.getMessage());
        }
    }

    /**
     * @MethodName: carver
     * @Description: 让球/大小球玩法期望值
     **/
    @GetMapping("/getProfitByMatchIdAndPlayId")
    public HttpResponse<List<MarketProfitVo>> getProfitByMatchIdAndPlayId(RcsProfitRectangle rcsProfitRectangle) {
        try {
            Assert.notNull(rcsProfitRectangle.getMatchId(), "赛事ID不能为空！");
            Assert.notNull(rcsProfitRectangle.getPlayId(), "玩法ID不能为空！");

            List<MarketProfitVo> list = matchTradeConfigService.getProfitByMatchIdAndPlayId(rcsProfitRectangle);
            return HttpResponse.success(list);
        } catch (Exception e) {
            log.error("::{}::让球/大小球玩法期望值,查询异常：{}", CommonUtil.getRequestId(rcsProfitRectangle.getMatchId(),rcsProfitRectangle.getPlayId()), e.getMessage(), e);
            return HttpResponse.fail(e.getMessage());
        }
    }

    /**
     * @MethodName: tournamentConfig
     * @Description: 获取联赛对应配置信息
     **/
    @RequestMapping(value = "/tournamentConfig", method = RequestMethod.GET)
    public HttpResponse<List<RcsTournamentMarketConfig>> getTournamentConfig(RcsTournamentMarketConfig config) {
        try {
            Map<String, Object> columnMap = new HashMap<>(2);
            columnMap.put("tournament_id", config.getTournamentId());
            columnMap.put("play_id", config.getPlayId());
            List<RcsTournamentMarketConfig> rcsTournamentMarketConfigList = rcsTournamentOperateMarketMapper.selectByMap(columnMap);
            return HttpResponse.success(rcsTournamentMarketConfigList);
        } catch (RpcException e) {
            log.error("::{}::获取联赛对应配置信息,融合Rpc出问题，{}", CommonUtil.getRequestId(config.getTournamentId()), e.getMessage(), e);
            return HttpResponse.fail("融合Rpc请检查" + ":" + e.getMessage());
        } catch (RcsServiceException e) {
            log.error("::{}::获取联赛对应配置信息:{}", CommonUtil.getRequestId(config.getTournamentId()), e.getMessage(), e);
            return HttpResponse.fail("风控服务器出问题" + ":" + e.getMessage());
        } catch (Exception e) {
            log.error("::{}::获取联赛对应配置信息:{}", CommonUtil.getRequestId(config.getTournamentId()), e.getMessage(), e);
            return HttpResponse.fail("风控服务器出问题");
        }
    }

    @RequestMapping(value = "/getTournaments", method = RequestMethod.POST)
    public HttpResponse<Map<Long, List<MacthTournamentNameVo>>> getTournaments(@RequestBody MarketLiveOddsQueryVo vo) {
        try {
            // 操盘赛事ID输入非数字直接返回
            if (!Strings.isNullOrEmpty(vo.getMatchManageId()) && !NumberUtils.isNumber(vo.getMatchManageId())) {
                log.warn("Invalid input parameter, matchManageId:{}", vo.getMatchManageId());
                return HttpResponse.success(Collections.emptyList());
            }
            Integer userId = TradeUserUtils.getUserId();
            vo.setTradeId(Long.valueOf(userId));
            List rtnList = Lists.newArrayList();
            List<MatchMarketLiveBean> sportMatchInfos = marketViewService.queryTournaments(vo);
            if (!CollectionUtils.isEmpty(sportMatchInfos)) {
                //根据联赛id去重
                List<MatchMarketLiveBean> distractList = sportMatchInfos.stream().collect(Collectors.collectingAndThen(Collectors.toCollection
                        (() -> new TreeSet<>(Comparator.comparing(MatchMarketLiveBean::getStandardTournamentId))), ArrayList::new));
                //根据联赛等级分组
                Map<Integer, List<MatchMarketLiveBean>> list = distractList.stream().filter(bean -> bean.getTournamentLevel() != null).collect(Collectors.groupingBy(MatchMarketLiveBean::getTournamentLevel));


                for (Map.Entry<Integer, List<MatchMarketLiveBean>> m : list.entrySet()) {
                    Map map = Maps.newHashMap();
                    map.put("tournamentLevel", m.getKey());
                    map.put("trees", m.getValue());
                    rtnList.add(map);
                }
                //根据联赛等级升序排序
                Collections.sort(rtnList, new Comparator<Map<String, Object>>() {
                    @Override
                    public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                        Integer name1 = Integer.valueOf(o1.get("tournamentLevel").toString());
                        Integer name2 = Integer.valueOf(o2.get("tournamentLevel").toString());
                        return name1.compareTo(name2);
                    }
                });
            }
            return HttpResponse.success(rtnList);
        } catch (Exception e) {
            log.error("::{}::getTournaments:{}", CommonUtil.getRequestId(vo.getMatchId()), e.getMessage(), e);
            return HttpResponse.fail("风控服务器出问题");
        }
    }

    @RequestMapping(value = "/getSelectMatchs")
    public HttpResponse<Map<Long, List<MacthTournamentNameVo>>> getSelectMatchs(@RequestBody MarketLiveOddsQueryVo marketLiveOddsQueryVo) {
        try {
            List<Map<String, Object>> result = marketViewService.getSelectMatchs(marketLiveOddsQueryVo);
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("list", result);
            return HttpResponse.success(map);
        } catch (Exception e) {
            log.error("::{}::getSelectMatchs:{}", CommonUtil.getRequestId(marketLiveOddsQueryVo.getMatchId()), e.getMessage(), e);
            return HttpResponse.fail("风控服务器出问题");
        }
    }

    @RequestMapping(value = "/mongo", method = RequestMethod.POST)
    public HttpResponse<Boolean> mongo(Integer mongo) {
        try {
            redisClient.setExpiry(RCS_MATCH_MONGO_SET, mongo, EXPRIY_TIME_7_DAYS);
        } catch (Exception e) {
            log.error("::{}::mongo:{}", CommonUtil.getRequestId(mongo), e.getMessage(), e);
            return HttpResponse.fail("风控服务器出问题" + ":" + e.getMessage());
        }
        return HttpResponse.success("更新成功");
    }

    /**
     * @return com.panda.sport.rcs.vo.HttpResponse<java.lang.Boolean>
     * @Description //平衡值清0
     * @Param [matchId, marketId]
     * @Author kimi
     * @Date 2019/11/30
     **/
    @RequestMapping(value = "/balanceToZero", method = RequestMethod.GET)
    @LogAnnotion(name = "平衡值清0", keys = {"matchId", "marketId"}, title = {"赛事id", "盘口id"})
    @LogFormatAnnotion
    public HttpResponse<Boolean> balanceToZero(Long matchId, Long playId, Long marketId, String marketType, Integer balanceOption, Integer matchType) {
        try {

            rcsTradingAssignmentService.tradeJurisdictionByMarketId(marketId, null);

            if (MarketKindEnum.Malaysia.getValue().equalsIgnoreCase(marketType)) {
                StandardSportMarket market = standardSportMarketService.selectById(marketId);
                LogFormatPublicBean publicBean = new LogFormatPublicBean(LogTypeEnum.TRADE_TYPE.getCode() + "", "平衡值清零", String.valueOf(matchId));
                Map<String, Object> dynamicBean = new HashMap<String, Object>();
                dynamicBean.put("click_case", "调价弹窗手动调整");
                dynamicBean.put("play_id", playId);
                dynamicBean.put("obj_id", "盘口：" + market.getAddition1());
                dynamicBean.put("match_type", matchType);

                MarketBalanceVo vo = matchTradeConfigService.queryBalance(matchId, marketId, marketType, balanceOption);
                vo.setMatchId(matchId);
                vo.setMarketId(marketId);
                vo.setPlayId(playId);
                vo.setMatchType(matchType);
                vo.setCreateUser(TradeUserUtils.getUserIdNoException().toString());
                LogContext.getContext().addFormatBean(publicBean, dynamicBean, new LogFormatBean("平衡值清零",
                        vo.getBalanceValue() == null ? "0" : String.valueOf(vo.getBalanceValue()), "0"));
                log.info("::{}::平衡值清0:{}，操盘手:{}",CommonUtil.getRequestId(matchId,playId),JSONObject.toJSONString(vo),TradeUserUtils.getUserIdNoException());
            }

            balanceValueService.zeroBalanceValue(matchId, marketId);
            return HttpResponse.success(true);
        } catch (Exception e) {
            log.error("::{}::平衡值清0:{}", CommonUtil.getRequestId(matchId,playId), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL, "风控服务器出问题");
        }
    }
    @RequestMapping(value = "getTraderMatchCount")
    public HttpResponse<Integer> getTraderMatchCount(MarketLiveOddsQueryVo marketLiveOddsQueryVo) {
        if (StringUtils.isBlank(marketLiveOddsQueryVo.getMatchDate())) {
            marketLiveOddsQueryVo.setMatchDate(DateUtils.DateToString(new Date()));
        }
        Date matchDate = DateUtils.dateStrToDate(marketLiveOddsQueryVo.getMatchDate() + " 12:00:00");
        marketLiveOddsQueryVo.setBeginTime(matchDate);

        // 操盘赛事ID输入非数字直接返回
        if (!Strings.isNullOrEmpty(marketLiveOddsQueryVo.getMatchManageId()) && !NumberUtils.isNumber(marketLiveOddsQueryVo.getMatchManageId())) {
            log.warn("Invalid input parameter, matchManageId:{}", marketLiveOddsQueryVo.getMatchManageId());
            return HttpResponse.success(Collections.emptyList());
        }
        if (marketLiveOddsQueryVo.getTradeId() == null) {
            marketLiveOddsQueryVo.setTradeId(1L);
        }

        Long count = null;
        try {
            Integer userId = TradeUserUtils.getUserId();
            marketLiveOddsQueryVo.setTradeId(Long.valueOf(userId));
            count = marketViewService.getTraderMatchCount(marketLiveOddsQueryVo);
        } catch (Exception e) {
			log.error("::{}::getTraderMatchCount:{}", CommonUtil.getRequestId(marketLiveOddsQueryVo.getMatchId()), e.getMessage(), e);
        }
        return HttpResponse.success(count);
    }

    /**
     * 获取操盘界面滚球数
     *
     * @param queryVo
     * @return
     */
    @RequestMapping(value = "getLiveNum", method = RequestMethod.POST)
    public HttpResponse getLiveNum(@RequestBody MarketLiveOddsQueryVo queryVo) {
        if (queryVo.getSportId() == null) {
            return HttpResponse.fail("赛事种类不能为空");
        }
        if (queryVo.getCategorySetId() == null) {
            return HttpResponse.fail("categorySetId不能为空");
        }
        if (queryVo.getChooseType() == null) {
            return HttpResponse.fail("chooseType不能为空");
        }
        queryVo.setLiveOddBusiness(1);
        try {
            Integer userId = TradeUserUtils.getUserId();
            queryVo.setTradeId(Long.valueOf(userId));
            Long liveNum = marketViewService.getLiveNum(queryVo);
            return HttpResponse.success(liveNum);
        } catch (Exception e) {
            log.error("::{}::获取操盘界面滚球数:{}", CommonUtil.getRequestId(queryVo.getMatchId()), e.getMessage(), e);
            return HttpResponse.fail("接口异常");
        }

    }

    /**
     * @return com.panda.sport.rcs.vo.HttpResponse<com.panda.sport.rcs.pojo.RcsMatchConfig>
     * @Description //获取自动调价参数
     * @Param []
     * @Author kimi
     * @Date 2020/2/14
     **/
    @RequestMapping(value = "getRcsMatchConfig", method = RequestMethod.GET)
    public HttpResponse<RcsMatchConfig> getRcsMatchConfig(Long matchId) {
        try {
            RcsMatchConfig rcsMatchConfig = rcsMatchConfigService.selectMatchConfig(matchId);
            if (rcsMatchConfig == null) {
                rcsMatchConfig = new RcsMatchConfig();
                rcsMatchConfig.setMatchId(matchId);
                rcsMatchConfig.setPriceAdjustmentParameters(new BigDecimal(0.70));
                rcsMatchConfigService.insert(rcsMatchConfig);
            }
            //需要把名字放进去
            List<TeamVo> teamVos = standardMatchInfoMapper.selectTeamNameByMatchId(matchId);
            rcsMatchConfig.setTeamVos(teamVos);
            return HttpResponse.success(rcsMatchConfig);
        } catch (Exception e) {
            log.error("::{}::获取自动调价参数:{}", CommonUtil.getRequestId(matchId), e.getMessage(), e);
            return HttpResponse.fail("风控服务器出问题");
        }
    }

    /**
     * @return com.panda.sport.rcs.vo.HttpResponse<com.panda.sport.rcs.pojo.RcsMatchConfig>
     * @Description //更新自动调价参数
     * @Param [rcsMatchConfig]
     * @Author kimi
     * @Date 2020/2/18
     **/
    @RequestMapping(value = "/updateRcsMatchConfig", method = RequestMethod.POST)
    @LogAnnotion(name = "更新自动调价参数", keys = {"matchId", "operateMatchStatus", "tradeType", "priceAdjustmentParameters", "preRiskManagerCode", "liveRiskManagerCode"},
            title = {"赛事ID", "赛事状态", "操盘类型", "自动调价参数", "赛前操盘平台", "滚球操盘平台"})
    public HttpResponse<RcsMatchConfig> updateRcsMatchConfig(@RequestBody RcsMatchConfig rcsMatchConfig) {
        try {
            log.info("::{}::更新自动调价参数:{}，操盘手:{}",CommonUtil.getRequestId(rcsMatchConfig.getMatchId()),JSONObject.toJSONString(rcsMatchConfig),TradeUserUtils.getUserIdNoException());
            rcsMatchConfigService.updateRcsMatchConfig(rcsMatchConfig);
            return HttpResponse.success(rcsMatchConfig);
        } catch (Exception e) {
            log.error("::{}::更新自动调价参数:{}", CommonUtil.getRequestId(rcsMatchConfig.getMatchId()), e.getMessage(), e);
            return HttpResponse.fail("风控服务器出问题");
        }
    }

    /**
     * 获取系统时间
     *
     * @return
     * @Author enzo
     */
    @RequestMapping(value = "getSystemTime", method = RequestMethod.GET)
    public HttpResponse<Long> getSystemTime() {
        try {
            return HttpResponse.success(System.currentTimeMillis());
        } catch (Exception e) {
			log.error("::{}::获取服务器时间失败{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail("获取服务器时间失败");
        }
    }

    @RequestMapping(value = "relatedMarket", method = RequestMethod.POST)
    @LogAnnotion(name = "关联盘口", keys = {"matchId", "playId", "marketId", "marketIndex", "relevanceType"},
            title = {"赛事id", "玩法id", "盘口id", "盘口位置", "盘口关联"})
    public HttpResponse relevanceMarket(@RequestBody List<RcsMatchMarketConfig> matchMarketConfigs) {
        try {
            // marketViewService.relevanceMarket(matchMarketConfigs);
            return HttpResponse.success("盘口关联成功");
        } catch (RcsServiceException e) {
			log.error("::{}::盘口关联失败：{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail(e.getErrorMassage());
        } catch (Exception e) {
			log.error("::{}::盘口关联失败：{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail("盘口关联失败");
        }
    }

    /**
     * @return com.panda.sport.rcs.vo.HttpResponse<java.lang.Boolean>
     * @Description //次要玩法更新赔率
     * @Param []
     * @Author enzo
     **/
    @RequestMapping(value = "/updateOddsValue", method = RequestMethod.POST)
    @LogAnnotion(name = "次要玩法更新赔率", keys = {"matchId", "playId", "marketId", "oddsValueList"}, title = {"赛事id", "玩法id", "盘口id", "投注项数据"})
    @OperateLog(operateType = OperateLogEnum.ODDS_UPDATE)
    public HttpResponse<Boolean> updateOddsValue(@RequestBody UpdateOddsValueVo updateOddsValueVo) {
        try {
            log.info("::{}::修改操盘方式:{}，操盘手:{}",CommonUtil.getRequestId(updateOddsValueVo.getMatchId(),updateOddsValueVo.getPlayId()),JSONObject.toJSONString(updateOddsValueVo), TradeUserUtils.getUserIdNoException());
            marketViewService.updateOddsValue(updateOddsValueVo);
            return HttpResponse.success(true);
        } catch (RpcException e) {
			log.error("::{}::次要玩法更新赔率,融合RPC出问题:{}", CommonUtil.getRequestId(updateOddsValueVo.getMatchId(),updateOddsValueVo.getPlayId()), e.getMessage(), e);
            return HttpResponse.error(HttpResponse.FAIL, "融合RPC出问题" + e.getMessage());
        } catch (RcsServiceException e) {
			log.error("::{}::次要玩法更新赔率，风控服务出问题:{}", CommonUtil.getRequestId(updateOddsValueVo.getMatchId(),updateOddsValueVo.getPlayId()), e.getMessage(), e);
            return HttpResponse.error(HttpResponse.FAIL, e.getMessage());
        } catch (Exception e) {
			log.error("::{}::次要玩法更新赔率,风控服务出问题:{}", CommonUtil.getRequestId(updateOddsValueVo.getMatchId(),updateOddsValueVo.getPlayId()), e.getMessage(), e);
            return HttpResponse.error(HttpResponse.FAIL, "风控服务出问题");
        }
    }

    /**
     * 获取操盘方式
     *
     * @param matchId
     * @return
     * @Author enzo
     */
    @GetMapping(value = "/getRiskManagerCode")
    public HttpResponse<Map<String, String>> getRiskManagerCode(@RequestParam("matchId") Long matchId) {
        try {
            Map<String, String> map = matchTradeConfigService.getRiskManagerCode(matchId);
            return HttpResponse.success(map);
        } catch (RpcException e) {
			log.error("::{}::获取操盘方式:{}", CommonUtil.getRequestId(matchId), e.getMessage(), e);
            return HttpResponse.fail("获取失败" + e.getMessage());
        } catch (Exception e) {
			log.error("::{}::获取操盘方式:{}", CommonUtil.getRequestId(matchId), e.getMessage(), e);
            return HttpResponse.fail("获取失败");
        }
    }

    /***
     * 修改操盘方式
     * @param config
     * @return
     * @Author enzo
     */
    @PostMapping(value = "/updateRiskManagerCode")
    public HttpResponse<Map<String, String>> updateRiskManagerCode(@RequestBody RcsMatchConfigParam config) {
        HttpResponse response;
        String linkId = CommonUtils.mdcPut();
        try {
            log.info("::{}::修改操盘方式:{}，操盘手:{}",CommonUtil.getRequestId(config.getMatchId()),JSONObject.toJSONString(config), TradeUserUtils.getUserIdNoException());
            Map<String, Object> map = matchTradeConfigService.updateRiskManagerCode(config);
            response = HttpResponse.success(map);
        } catch (Exception e) {
			log.error("::{}::修改操盘方式:{}", CommonUtil.getRequestId(config.getMatchId()), e.getMessage(), e);
            response = HttpResponse.error(HttpResponse.FAIL, e.getMessage());
        } finally {
            CommonUtils.mdcRemove();
        }
        response.setLinkId(linkId);
        return response;
    }


    @RequestMapping(value = "test", method = RequestMethod.POST)
    public HttpResponse<PageResult> test(@RequestBody MarketLiveOddsQueryVo marketLiveOddsQueryVo) throws Exception {

        if (0 == marketLiveOddsQueryVo.getCurrentPage()) {
            marketLiveOddsQueryVo.setPageSize(1);
            marketLiveOddsQueryVo.setCurrentPage(1);
        }

        PageResult<MatchMarketLiveBean> matchMarketLiveBeanPageResult = mongoPageHelper.pageQuery(new Query().addCriteria(new Criteria().and("categoryCollectionList.id").in(Arrays.asList(1L, 2L, 3L))), MatchMarketLiveBean.class, 1, 1);
        List<MatchMarketLiveBean> list = matchMarketLiveBeanPageResult.getList();

        matchMarketLiveBeanPageResult.setList(list);
        return HttpResponse.success(matchMarketLiveBeanPageResult);
    }


    /**
     * 查询平衡值
     *
     * @param matchId
     * @param marketId
     * @param marketType
     * @param balanceOption
     * @return
     * @Author enzo
     */
    @RequestMapping(value = "/queryBalance", method = RequestMethod.GET)
    public HttpResponse<MarketBalanceVo> queryBalance(Long matchId, Long marketId, String marketType, Integer balanceOption) {
        try {
            MarketBalanceVo vo = matchTradeConfigService.queryBalance(matchId, marketId, marketType, balanceOption);

            return HttpResponse.success(vo);
        } catch (RcsServiceException e) {
            log.error("::{}::查询平衡值:{}", CommonUtil.getRequestId(matchId), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL, e.getMessage());
        } catch (Exception e) {
            log.error("::{}::查询平衡值:{}", CommonUtil.getRequestId(matchId), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL, "风控服务器出问题");
        }
    }

    /**
     * 修改盘口配置----赛前十五分钟
     *
     * @param config
     * @return
     * @Author enzo
     */
    @RequestMapping(value = "/updateMarketConfigSnap", method = RequestMethod.POST)
    public HttpResponse<Map<String, Object>> updateMarketConfig(@RequestBody RcsMatchMarketConfig config) {
        try {
            if (!NumberUtils.isInteger(String.valueOf(config.getHomeLevelFirstMaxAmount()))) {
                return HttpResponse.fail("只能设置为整型数字，例如：123");
            }
            log.info("::{}::修改盘口配置----赛前十五分钟:{}，操盘手:{}",CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()),JSONObject.toJSONString(config), TradeUserUtils.getUserIdNoException());
            RcsMatchMarketConfig info = marketViewService.updateMatchMarketMongo(config);
            //给前端展示用。进行数据刷新
            if (info.getMarketStatus() == MarketStatusEnum.CLOSE.getState()) {
                info.setMarketActive(false);
            } else {
                info.setMarketActive(true);
            }

            return HttpResponse.success(info);
        } catch (RpcException e) {
            log.error("::{}::修改盘口配置,融合Rpc请检查:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return HttpResponse.fail("融合Rpc请检查");
        } catch (RcsServiceException e) {
            log.error("::{}::修改盘口配置:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return HttpResponse.fail(e.getMessage());
        } catch (Exception e) {
            log.error("::{}::修改盘口配置:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return HttpResponse.fail("问题详情：" + e.getMessage());
        }
    }

    /**
     * @MethodName: config
     * @Description: 获取盘口对应配置信息
     * @Author enzo
     **/
    @RequestMapping(value = "/getMarketConfigSnapshot", method = RequestMethod.POST)
    public HttpResponse<MarketConfigMongo> getMarketConfigSnapshot(@RequestBody RcsMatchMarketConfig config) {
        try {
            MarketConfigMongo marketConfig = marketViewService.getMarketConfigMongo(config);
            return HttpResponse.success(marketConfig);
        } catch (Exception e) {
            log.error("::{}::获取盘口对应配置信息:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return HttpResponse.fail("风控服务器出问题");
        }
    }

    /***
     * 次要玩法快速修改赔率-----赛前十五分钟
     * @param updateOddsValueVo
     * @return
     * @Author enzo
     */
    @RequestMapping(value = "/updateSnapOddsValue", method = RequestMethod.POST)
    public HttpResponse<Boolean> updateSnapOddsValue(@RequestBody UpdateOddsValueVo updateOddsValueVo) {
        try {
            log.info("::{}::次要玩法快速修改赔率-----赛前十五分钟:{}，操盘手:{}",CommonUtil.getRequestId(updateOddsValueVo.getMatchId(),updateOddsValueVo.getPlayId()),JSONObject.toJSONString(updateOddsValueVo), TradeUserUtils.getUserIdNoException());
            marketViewService.updateSnapOddsValue(updateOddsValueVo);
            return HttpResponse.success(true);
        } catch (RpcException e) {
			log.error("::{}::次要玩法快速修改赔率:{}", CommonUtil.getRequestId(updateOddsValueVo.getMatchId(),updateOddsValueVo.getPlayId()), e.getMessage(), e);
            return HttpResponse.fail("融合RPC出问题" + e.getMessage());
        } catch (RcsServiceException e) {
			log.error("::{}::次要玩法快速修改赔率:{}", CommonUtil.getRequestId(updateOddsValueVo.getMatchId(),updateOddsValueVo.getPlayId()), e.getMessage(), e);
            return HttpResponse.fail("风控服务出问题" + e.getMessage());
        } catch (Exception e) {
			log.error("::{}::次要玩法快速修改赔率:{}", CommonUtil.getRequestId(updateOddsValueVo.getMatchId(),updateOddsValueVo.getPlayId()), e.getMessage(), e);
            return HttpResponse.fail("风控服务出问题");
        }
    }

    /***
     * 快速修改赔率----赛前十五分钟
     * @param config
     * @return
     * @throws Exception
     * @Author enzo
     */
    @RequestMapping(value = "updateMarketOddsSnap", method = RequestMethod.POST)
    public HttpResponse<Integer> updateMarketOddsSnap(@RequestBody RcsMatchMarketConfig config) throws Exception {
        try {
            org.springframework.util.Assert.notNull(config.getPlayId(), "玩法不能为空");
            org.springframework.util.Assert.notNull(config.getMatchId(), "赛事不能为空");
            org.springframework.util.Assert.notNull(config.getMarketId(), "盘口不能为空");
            org.springframework.util.Assert.notNull(config.getMarketIndex(), "盘口位置不能为空");
            org.springframework.util.Assert.notNull(config.getMarketType(), "盘口类型不能为空");
            org.springframework.util.Assert.notNull(config.getOddsChange(), "赔率变化不能为空");
            org.springframework.util.Assert.notNull(config.getOddsType(), "变化投注项不能为空");
            log.info("::{}::快速修改赔率----赛前十五分钟:{}，操盘手:{}",CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()),JSONObject.toJSONString(config), TradeUserUtils.getUserIdNoException());
            marketViewService.updateMarketOddsSnap(config);
        } catch (IllegalArgumentException e) {
            log.error("::{}::快速修改赔率----赛前十五分钟:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL, e.getMessage());
        } catch (RcsServiceException e) {
            log.error("::{}::快速修改赔率----赛前十五分钟:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL, e.getErrorMassage());
        } catch (Exception e) {
            log.error("::{}::快速修改赔率----赛前十五分钟:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL, "系统异常请稍后重试");
        }
        return HttpResponse.success();
    }

    @RequestMapping(value = "/queryOddsMapping", method = RequestMethod.GET)
    public HttpResponse<Map<String, RcsOddsConvertMappingMy>> queryOddsMapping() {
        try {
            List<RcsOddsConvertMappingMy> list = rcsOddsConvertMappingMyMapper.selectList(new QueryWrapper<>());
            Map<String, RcsOddsConvertMappingMy> map = Maps.newHashMap();
            if (!CollectionUtils.isEmpty(list)) {
                map = list.stream().collect(Collectors.toMap(RcsOddsConvertMappingMy::getMalaysia, a -> a));
            }
            return HttpResponse.success(map);
        } catch (Exception e) {
            log.error("::{}::queryOddsMapping:{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL, "风控服务器出问题");
        }
    }

    /**
     * 修改操盘方式----赛前十五分钟
     *
     * @param config
     * @return
     * @Author enzo
     */
    @PostMapping(value = "/updateSnapRiskManagerCode")
    public HttpResponse<Map<String, String>> updateSnapRiskManagerCode(@RequestBody RcsMatchConfigParam config) {
        try {
            log.info("::{}::修改操盘方式----赛前十五分钟:{}，操盘手:{}",CommonUtil.getRequestId(config.getMatchId()),JSONObject.toJSONString(config), TradeUserUtils.getUserIdNoException());
            marketViewService.updateRiskManagerCodeSnapshot(config);
            return HttpResponse.success("预设置成功");
        } catch (Exception e) {
			log.error("::{}::修改操盘方式,赛前十五分钟:{}", CommonUtil.getRequestId(config.getMatchId()), e.getMessage(), e);
            return HttpResponse.fail("风控服务出问题");
        }
    }

    /***
     * 主界面玩法ID列表
     * @param vo
     * @return
     * @Author voot
     */
    @RequestMapping(value = "/getMainCategorySetIds", method = RequestMethod.POST)
    public HttpResponse<List<Long>> getMainCategorySetIds(@RequestBody CategoryConVo vo) {

        if (vo.getSportId() == null) {
            return HttpResponse.fail("体育类型sportId不能为空");
        }
        if (vo.getId() == null) {
            return HttpResponse.fail("玩法集id不能为空");
        }

        List<Long> mainCategorySetIds = null;
        try {
            //查询赛种主要玩法
            mainCategorySetIds = sportMatchViewService.queryMainCategorySetIds(vo);

        } catch (Exception e) {
			log.error("::{}::获取主界面玩法ID列表失败:{}", CommonUtil.getRequestId(vo.getId()), e.getMessage(), e);
            return HttpResponse.fail("获取主界面玩法ID列表异常");
        }
        return HttpResponse.success(mainCategorySetIds);
    }

    /***
     * 赛事ID列表
     * @param vo
     * @return
     * @Author voot
     */
    @RequestMapping(value = "/getMatchIdList", method = RequestMethod.POST)
    public HttpResponse<List<Long>> getMatchIdList(@RequestBody MarketLiveOddsQueryVo vo) {

        if (vo.getSportId() == null) {
            return HttpResponse.fail("体育类型sportId不能为空");
        }
        if (vo.getChooseType() == null) {
            return HttpResponse.fail("赛事类型chooseType不能为空");
        }

        List<Long> matchIdList = null;
        try {
            Integer userId = TradeUserUtils.getUserId();
            vo.setTradeId(Long.valueOf(userId));
            matchIdList = sportMatchViewService.queryMyselfMatchs(vo);
        } catch (Exception e) {
			log.error("::{}::获取赛事Id列表失败:{}", CommonUtil.getRequestId(vo.getMatchId()), e.getMessage(), e);
            return HttpResponse.fail("获取赛事Id列表异常");
        }
        return HttpResponse.success(matchIdList);
    }

    /***
     * 赛事列表信息
     * 	赛事表，玩法表，组装数据返回
     * @param vo
     * @return
     * @Author enzo
     */
    @RequestMapping(value = "getMatchList", method = RequestMethod.POST)
    public HttpResponse<PageResult<MatchMarketLiveBean>> getMatchList(@RequestBody MarketLiveOddsQueryVo vo) {

        if (vo.getCategorySetId() == null) {
            return HttpResponse.fail("categorySetId不能为空");
        }
        if (vo.getChooseType() == null) {
            return HttpResponse.fail("chooseType不能为空");
        }
        if (vo.getSortType() == null) {
            vo.setSortType(1);
        }
        if (0 == vo.getCurrentPage()) {
            vo.setPageSize(8);
            vo.setCurrentPage(1);
        }
        PageResult<MatchMarketLiveBean> traderMatchList = null;
        try {
            Integer userId = TradeUserUtils.getUserId();
            vo.setTradeId(Long.valueOf(userId));
            // 从mogodb查询赛事玩法，盘口赔率
            traderMatchList = sportMatchViewService.queryMatchList(vo);
            List<MatchMarketLiveBean> matchInfos = traderMatchList.getList();
            if (matchInfos.size() > 0) {

                /***** 1802需求 ***/
                List<MatchMarketLiveBean> list = sportMatchViewService.tansferMatchInfo(matchInfos, vo);

                /***** 35323需求 AO参数介面入口，新增颜色提示***/
                Boolean flagArp = true;
                Map<String, Boolean> aoMatchApply = null;
                try {
	                Map<String, Boolean> result = new HashMap<>();
	                if (!CollectionUtils.isEmpty(list)) {
	                    Set<Long> aoMatchIds = new HashSet<>();
	                    for (MatchMarketLiveBean matchMarketLive : list) {
	                        if (StringUtils.isNotBlank(matchMarketLive.getAoId())) {
	                            aoMatchIds.add(Long.valueOf(matchMarketLive.getAoId()));
	                        }
	                    }
	                    Response response = applyService.applyIconInfo(new ArrayList<>(aoMatchIds));
	                    if (response != null && response.getCode() == 200)
	                        result = (Map<String, Boolean>) response.getData();
	                }
	                aoMatchApply = result.entrySet().stream()
	                        .collect(Collectors.toMap(e -> String.valueOf(e.getKey()), Map.Entry::getValue));
                } catch (Exception e) {
                	flagArp = false;
                	log.error("::" + CommonUtil.getRequestId() + "::applyService.applyIconInfo请求异常", e);
				}

                for (MatchMarketLiveBean m : list) {
                    m.getTeamList().forEach(t -> {
                        String dangerTeamKey = DANGER_TEAM_KEY + t.getId();
                        //先查本地缓存本地缓存为空在查redis
                        String teamJsonString = TradeDataCache.getDangerTeamMap(dangerTeamKey);
                        if (StringUtils.isNotEmpty(teamJsonString)) {
                            t.setDangerTeam(JSONObject.parseObject(teamJsonString, RcsDangerTeam.class));
                        }else {
                            teamJsonString = redisClient.get(dangerTeamKey);
                            //本地缓存为空设置本地缓存3小时自动清理
                            if (StringUtils.isNotEmpty(teamJsonString)) {
                                t.setDangerTeam(JSONObject.parseObject(teamJsonString, RcsDangerTeam.class));
                                TradeDataCache.dangerTournamentMap.put(dangerTeamKey, new TradeCacheDataDTO(dangerTeamKey, teamJsonString, System.currentTimeMillis()));
                            }
                        }
                    });

                    String dangerTournamentKey = DANGER_TOURNAMENT_KEY + m.getStandardTournamentId();
                    //先查本地缓存本地缓存为空在查redis
                    String tournamentJsonString = TradeDataCache.getDangerTournamentMap(dangerTournamentKey);
                    if (StringUtils.isNotEmpty(tournamentJsonString)) {
                        m.setDangerTournament(JSONObject.parseObject(tournamentJsonString, RcsDangerTournament.class));
                    }else{
                        tournamentJsonString = redisClient.get(dangerTournamentKey);
                        //本地缓存为空设置本地缓存3小时自动清理
                        if (StringUtils.isNotEmpty(tournamentJsonString)) {
                            m.setDangerTournament(JSONObject.parseObject(tournamentJsonString, RcsDangerTournament.class));
                            TradeDataCache.dangerTournamentMap.put(dangerTournamentKey, new TradeCacheDataDTO(dangerTournamentKey, tournamentJsonString, System.currentTimeMillis()));
                        }
                    }
                    if (!CollectionUtils.isEmpty(aoMatchApply) && flagArp) {
                        aoMatchApply.forEach((k, v) -> {
                            if (k.equals(m.getAoId())) m.setApply(v);
                        });
                    }
                }
                traderMatchList.setList(list);
            }
        } catch (Exception e) {
            log.error("::{}::赛事列表信息异常:{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail("获取赛事列表异常");
        }
        return HttpResponse.success(traderMatchList);
    }

    /**
     * 获取进球时间
     *
     * @param vo
     * @return
     */
    @PostMapping(value = "/getGoalTime")
    public HttpResponse getGoalTime(@RequestBody MarketLiveOddsQueryVo vo) {
        String key = "rcs:goal:time:redis:key:matchId:%s:sportId:%s";
        return HttpResponse.success(redisClient.get(String.format(key, vo.getMatchId(), vo.getSportId())));
    }


    /**
     * 修改操盘类型
     *
     * @param vo
     * @return
     * @author Paca
     */
    @PostMapping("/updateTradeType")
    @LogFormatAnnotion
    @LogAnnotion(name = "修改操盘类型",
            keys = {"tradeLevel", "sportId", "matchId", "categoryId", "placeNumId", "marketId", "marketPlaceNum", "categorySetId", "categoryIdList", "marketStatus", "tradeType"},
            title = {"操盘级别", "运动种类ID", "赛事ID", "玩法ID", "位置ID", "盘口ID", "盘口位置", "玩法集ID", "玩法ID集合", "状态", "操盘类型"})
    @OperateLog(operateType = OperateLogEnum.MARKET_TRADE_TYPE_UPDATE)
    public HttpResponse updateMarketTradeType(@RequestBody MarketStatusUpdateVO vo) {
        String link = CommonUtils.mdcPut();
        HttpResponse httpResponse;
        try {
            vo.updateTradeModeParamCheck();
            Long matchId = vo.getMatchId();
            StandardMatchInfo matchInfo = standardMatchInfoMapper.selectById(matchId);
            if (matchInfo == null) {
                return HttpResponse.fail("赛事[" + matchId + "]不存在");
            }
            boolean isMts = RcsConstant.isMts(matchInfo);
            vo.setMatchType(RcsConstant.getMatchType(matchInfo));
            vo.setIsMts(isMts);
            if (isMts && !TradeEnum.isAuto(vo.getTradeType())) {
                return HttpResponse.failToMsg("MTS赛事只能是A模式！");
            }
            vo.setSportId(matchInfo.getSportId());
            boolean pass = true;
            Integer tradeLevel = vo.getTradeLevel();
            if (TradeLevelEnum.isPlayLevel(tradeLevel)) {
                RcsMatchMarketConfig config = new RcsMatchMarketConfig();
                config.setSportId(matchInfo.getSportId().intValue());
                config.setMatchId(matchId);
                config.setPlayId(vo.getCategoryId());
                config.setMatchType(RcsConstant.getMatchType(matchInfo));
                pass = rcsTradingAssignmentService.hasTraderJurisdiction(config);
            }
            if (!pass) {
                return HttpResponse.failToMsg("您没有该操作权限！");
            }
            vo.setUpdateUserId(TradeUserUtils.getUserIdNoException());
            String linkId = tradeModeService.updateTradeMode(vo);
            // 修改操盘模式日志
            LogFormatPublicBean publicBean = new LogFormatPublicBean(LogTypeEnum.TRADE_TYPE.getCode().toString(), "操盘列表手动调整", String.valueOf(matchId));
            Map<String, Object> dynamicBean = Maps.newHashMap();
            dynamicBean.put("click_case", "修改操盘模式");
            dynamicBean.put("match_type", MatchTypeEnum.getByMatchStatus(matchInfo.getMatchStatus()).getId());
//            dynamicBean.put("trade_level", TradeLevelEnum.getRemarkByTradeLevel(tradeLevel));
//            dynamicBean.put("sport_id", SportIdEnum.getRemarkBySportId(matchInfo.getSportId()));
            if (TradeLevelEnum.isPlayLevel(tradeLevel)) {
                dynamicBean.put("play_id", String.valueOf(vo.getCategoryId()));
            } else if (TradeLevelEnum.isBatchPlayLevel(tradeLevel)) {
                dynamicBean.put("obj_id", "玩法集合：" + vo.getCategoryIdList());
            }
            LogContext.getContext().addFormatBean(publicBean, dynamicBean, new LogFormatBean("操盘模式", "", TradeEnum.getByTradeType(vo.getTradeType()).getValue()));
            log.info("::{}::修改操盘类型:{}，操盘手:{}",CommonUtil.getRequestId(vo.getMatchId(),vo.getCategoryId()),JSONObject.toJSONString(vo),TradeUserUtils.getUserIdNoException());
            httpResponse = HttpResponse.success(linkId);
        } catch (RpcException e) {
			log.error("::{}::修改操盘类型,融合RPC异常:{}", CommonUtil.getRequestId(vo.getMatchId(),vo.getCategoryId()), e.getMessage(), e);
            httpResponse = HttpResponse.failure("融合RPC异常：" + e.getMessage());
        } catch (RcsServiceException e) {
			log.error("::{}::修改操盘类型:{}", CommonUtil.getRequestId(vo.getMatchId(),vo.getCategoryId()), e.getMessage(), e);
            httpResponse = HttpResponse.failure(e.getMessage());
        } catch (Exception e) {
			log.error("::{}::修改操盘类型异常:{}", CommonUtil.getRequestId(vo.getMatchId(),vo.getCategoryId()), e.getMessage(), e);
            httpResponse = HttpResponse.failure("修改操盘类型异常：" + e.getMessage());
        } finally {
            CommonUtils.mdcRemove();
        }
        httpResponse.setLinkId(link);
        return httpResponse;
    }

    /***
     * 自动手动切换---赛事十五分钟
     * @param marketStatusUpdateVO
     * @return
     * @Author enzo
     */
    @PostMapping("/updateMarketSnapTradeType")
    public HttpResponse updateMarketSnapTradeType(@RequestBody MarketStatusUpdateVO marketStatusUpdateVO) {
        return HttpResponse.fail("赛事还未进入滚球");
//        Long matchId = marketStatusUpdateVO.getMatchId();
//        if (matchId == null || matchId <= 0) {
//            return HttpResponse.fail("参数matchId传值有误！");
//        }
//        try {
//            marketStatusUpdateVO.setUpdateUserId(TradeUserUtils.getUserId());
//            marketStatusService.updateSnapshotTradeType(marketStatusUpdateVO);
//
//            return HttpResponse.success("修改操盘类型成功");
//        } catch (RpcException e) {
//            log.error("融合RPC异常", e);
//            return HttpResponse.fail("融合RPC异常：" + e.getMessage());
//        } catch (RcsServiceException e) {
//            log.error("修改操盘类型：" + e.getMessage());
//            return HttpResponse.fail(e.getMessage());
//        } catch (Exception e) {
//            log.error("修改操盘类型异常", e);
//            return HttpResponse.fail("修改操盘类型异常：" + e.getMessage());
//        }
    }

    /**
     * 修改操盘状态
     *
     * @param vo
     * @return
     * @author Paca
     */
    @ApiOperation(value = "修改操盘状态", notes = "修改操盘状态")
    @PostMapping("/updateMarketStatus")
    @LogFormatAnnotion
    @LogAnnotion(name = "修改操盘状态",
            keys = {"tradeLevel", "sportId", "matchId", "categoryId", "placeNumId", "marketId", "marketPlaceNum", "categorySetId", "categoryIdList", "marketStatus", "tradeType"},
            title = {"操盘级别", "运动种类ID", "赛事ID", "玩法ID", "位置ID", "盘口ID", "盘口位置", "玩法集ID", "玩法ID集合", "状态", "操盘类型"})
    @OperateLog(operateType = OperateLogEnum.MARKET_STATUS)
    public HttpResponse updateMarketStatus(@RequestBody MarketStatusUpdateVO vo) {
        String link = CommonUtils.mdcPut();
        HttpResponse httpResponse;
        try {
            vo.updateStatusParamCheck();
            Integer tradeLevel = vo.getTradeLevel();
            Long matchId = vo.getMatchId();
            StandardMatchInfo matchInfo = standardMatchInfoMapper.selectById(matchId);
            if (matchInfo == null) {
                return HttpResponse.fail("赛事[" + matchId + "]不存在");
            }
            boolean isLive = RcsConstant.isLive(matchInfo);
            if ((TradeLevelEnum.isPlayLevel(tradeLevel) || TradeLevelEnum.isMarketLevel(tradeLevel))
                    && SportIdEnum.FOOTBALL.isYes(vo.getSportId())) {
                RcsMatchMarketConfig config = new RcsMatchMarketConfig();
                config.setSportId(matchInfo.getSportId().intValue());
                config.setMatchId(matchId);
                config.setPlayId(vo.getCategoryId());
                config.setMatchType(isLive ? 0 : 1);
                boolean pass = rcsTradingAssignmentService.hasTraderJurisdiction(config);
                if (!pass) {
                    return HttpResponse.failToMsg("您没有该操作权限！");
                }
            }
            vo.setMatchType(isLive ? 0 : 1);
            vo.setSportId(matchInfo.getSportId());
            vo.setUpdateUserId(TradeUserUtils.getUserIdNoException());
            vo.setOperateSource(1);
            StringBuilder sb = new StringBuilder();
            if (TradeLevelEnum.isBatchSubPlayLevel(tradeLevel)) {
                List<TradePlayVo> playList = Lists.newArrayList();
                for (TradePlayVo play : vo.getPlayList()) {
                    if (RcsConstant.isTwoPlaceholderPlay(matchInfo.getSportId(), play.getPlayId())) {
                        playList.add(play);
                    } else {
                        MarketStatusUpdateVO updateVO = JSON.parseObject(JSON.toJSONString(vo), MarketStatusUpdateVO.class);
                        updateVO.setTradeLevel(TradeLevelEnum.PLAY.getLevel());
                        updateVO.setCategoryId(play.getPlayId());
                        updateVO.setSubPlayId(play.getSubPlayId());
                        String linkId = tradeStatusService.updateTradeStatus(updateVO);
                        sb.append(linkId).append(",");
                    }
                }
                if (!CollectionUtils.isEmpty(playList)) {
                    Map<Long, List<TradePlayVo>> groupMap = playList.stream().collect(Collectors.groupingBy(TradePlayVo::getPlayId));
                    groupMap.forEach((playId, subPlayList) -> {
                        List<Long> subPlayIds = subPlayList.stream().map(TradePlayVo::getSubPlayId).collect(Collectors.toList());
                        MarketStatusUpdateVO updateVO = JSON.parseObject(JSON.toJSONString(vo), MarketStatusUpdateVO.class);
                        updateVO.setTradeLevel(TradeLevelEnum.PLAY.getLevel());
                        updateVO.setCategoryId(playId);
                        updateVO.setSubPlayIds(subPlayIds);
                        String linkId = tradeStatusService.updateBatchSubPlayTradeStatus(updateVO);
                        sb.append(linkId).append(",");
                    });
                }
            } else {
                String linkId = tradeStatusService.updateTradeStatus(vo);
                sb.append(linkId);
            }
            // 修改操盘状态日志
            LogFormatPublicBean publicBean = new LogFormatPublicBean(LogTypeEnum.TRADE_TYPE.getCode().toString(), "操盘列表手动调整", String.valueOf(matchId));
            Map<String, Object> dynamicBean = Maps.newHashMap();
            dynamicBean.put("click_case", "修改操盘状态");
            dynamicBean.put("match_type", MatchTypeEnum.getByMatchStatus(matchInfo.getMatchStatus()).getId());
            if (TradeLevelEnum.isMatchLevel(tradeLevel)) {
                dynamicBean.put("obj_id", "赛事：" + matchId);
            } else if (TradeLevelEnum.isPlayLevel(tradeLevel)) {
                dynamicBean.put("play_id", String.valueOf(vo.getCategoryId()));
            } else if (TradeLevelEnum.isBatchPlayLevel(tradeLevel)) {
                dynamicBean.put("obj_id", "玩法集合：" + vo.getCategoryIdList());
            } else if (TradeLevelEnum.isMarketLevel(tradeLevel)) {
                dynamicBean.put("play_id", String.valueOf(vo.getCategoryId()));
                dynamicBean.put("obj_id", "坑位：" + vo.getMarketPlaceNum());
            } else if (TradeLevelEnum.isPlaySetLevel(tradeLevel)) {
                dynamicBean.put("obj_id", "玩法集：" + vo.getCategorySetId());
            } else if (TradeLevelEnum.isBatchSubPlayLevel(tradeLevel)) {
                dynamicBean.put("obj_id", "子玩法集合：" + vo.getPlayList());
            }
            LogContext.getContext().addFormatBean(publicBean, dynamicBean, new LogFormatBean("操盘状态", "", TradeStatusEnum.getByStatus(vo.getMarketStatus()).getName()));
            log.info("::{}::修改操盘状态:{}，操盘手:{}",CommonUtil.getRequestId(vo.getMatchId(),vo.getCategoryId()),JSONObject.toJSONString(vo),TradeUserUtils.getUserIdNoException());
            httpResponse = HttpResponse.success(sb.toString());
        } catch (RpcException e) {
			log.error("::{}::修改操盘状态,融合RPC异常:{}", CommonUtil.getRequestId(vo.getMatchId(),vo.getCategoryId()), e.getMessage(), e);
            httpResponse = HttpResponse.failure("融合RPC异常：" + e.getMessage());
        } catch (RcsServiceException e) {
            log.error("::{}::修改操盘状态:{}", CommonUtil.getRequestId(vo.getMatchId(),vo.getCategoryId()), e.getMessage(), e);
            httpResponse = HttpResponse.failure(e.getMessage());
        } catch (Exception e) {
			log.error("::{}::修改操盘状态:{}", CommonUtil.getRequestId(vo.getMatchId(),vo.getCategoryId()), e.getMessage(), e);
            httpResponse = HttpResponse.failure("修改盘口状态异常：" + e.getMessage());
        } finally {
            CommonUtils.mdcRemove();
        }
        httpResponse.setLinkId(link);
        return httpResponse;
    }

    /**
     * 修改盘口状态----赛前十五分钟
     *
     * @param marketStatusUpdateVO
     * @return
     * @Author enzo
     */
    @PostMapping("/updateMarketSnapStatus")
    public HttpResponse updateMarketSnapStatus(@RequestBody MarketStatusUpdateVO marketStatusUpdateVO) {
        return HttpResponse.fail("赛事还未进入滚球");
//        Long matchId = marketStatusUpdateVO.getMatchId();
//        if (matchId == null || matchId <= 0) {
//            return HttpResponse.fail("参数matchId传值有误！");
//        }
//        try {
//            marketStatusUpdateVO.setUpdateUserId(TradeUserUtils.getUserId());
//            marketStatusService.updatSnapshotStatus(marketStatusUpdateVO);
//            return HttpResponse.success("修改盘口状态成功");
//        } catch (RpcException e) {
//            log.error("融合RPC异常", e);
//            return HttpResponse.fail("融合RPC异常：" + e.getMessage());
//        } catch (RcsServiceException e) {
//            log.error("修改盘口状态：" + e.getMessage());
//            return HttpResponse.fail(e.getMessage());
//        } catch (Exception e) {
//            log.error("修改盘口状态异常", e);
//            return HttpResponse.fail("修改盘口状态异常：" + e.getMessage());
//        }
    }

    @PostMapping("/updateCategorySetShow")
    public HttpResponse updateCategorySetShow(@RequestBody MarketLiveOddsQueryVo vo) {

        if (vo.getMatchId() == null) {
            return HttpResponse.fail("参数matchId传值有误！");
        }
        if (vo.getCategorySetId() == null) {
            return HttpResponse.fail("参数categorySetId传值有误！");
        }
        try {
            log.info("::{}::修改玩法集展示:{}",CommonUtil.getRequestId(vo.getMatchId(),vo.getCategorySetId()),JSONObject.toJSONString(vo));
            matchSetService.updateCategorySetShow(vo);
        } catch (Exception e) {
			log.error("::{}::修改玩法集展示失败:{}", CommonUtil.getRequestId(vo.getMatchId(),vo.getCategorySetId()), e.getMessage(), e);
            return HttpResponse.fail("修改玩法集展示：" + e.getMessage());
        }
        return HttpResponse.success("修改玩法集展示");
    }


    @PostMapping("/updateMatchTop")
    @LogAnnotion(name = "赛事置顶", keys = {"matchId", "topTime", "traderId", "topStatus"}, title = {"赛事Id", "置顶时间", "操盘手Id", "1 置顶 2 取消置顶"})
    public HttpResponse updateMatchTop(@RequestBody MatchTop top) {

        if (top.getMatchId() == null) {
            return HttpResponse.fail("参数matchId传值有误！");
        }
        try {
            log.info("::{}::赛事置顶:{}",CommonUtil.getRequestId(top.getMatchId()),JSONObject.toJSONString(top));
            top.setTraderId(String.valueOf(TradeUserUtils.getUserId()));
            sportMatchViewService.updateMatchTop(top);
        } catch (Exception e) {
			log.error("::{}::赛事置顶失败:{}", CommonUtil.getRequestId(top.getMatchId()), e.getMessage(), e);
            return HttpResponse.fail("赛事置顶失败：" + e.getMessage());
        }
        return HttpResponse.success("成功");
    }

    /**
     * 查询三方数据源球头
     *
     * @param vo
     * @return
     */
    @PostMapping("/queryMarketValue")
    public HttpResponse<List<Map<String, String>>> queryMarketValue(@RequestBody MarketLiveOddsQueryVo vo) {

        if (vo.getMatchId() == null) {
            return HttpResponse.fail("参数matchId传值有误！");
        }
        if (vo.getMarketCategoryId() == null) {
            return HttpResponse.fail("参数marketCategoryId传值有误！");
        }
        if (vo.getTradeType() == null || vo.getTradeType() == 1) {
            return HttpResponse.fail("参数tradeType传值有误！");
        }
        List<Map<String, String>> strings = null;
        try {
            strings = marketMapper.queryThirdSourceMarket(vo);
        } catch (Exception e) {
			log.error("::{}::查询球头失败:{}", CommonUtil.getRequestId(vo.getMatchId()), e.getMessage(), e);
            return HttpResponse.fail("查询球头失败：" + e.getMessage());
        }
        return HttpResponse.success(strings);
    }

    /**
     * @Description: 更新赛事玩法赔率源数据
     * @Author carver
     * @Date 2021/02/13 17:51
     * @return: com.panda.sport.rcs.vo.HttpResponse
     **/
    @GetMapping("/getMatchBusinessOdds")
    public HttpResponse modifyPlayOddsConfig(Long matchId) {
        try {
            Assert.notNull(matchId, "赛事id不能为空");
            List<ThirdDataSourceCodeVo> list = null;
            StandardMatchInfo info = standardMatchInfoMapper.selectById(matchId);
            if (ObjectUtil.isNotNull(info) && StringUtils.isNotBlank(info.getThirdMatchListStr())) {
                list = JSONArray.parseArray(info.getThirdMatchListStr(), ThirdDataSourceCodeVo.class);
                list = list.stream().filter(filter -> null != filter.getCommerce() && !"RB".equals(filter.getDataSourceCode()) && !"BT".equals(filter.getDataSourceCode())
                        && filter.getCommerce().equals(String.valueOf(org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE))).collect(Collectors.toList());
            }
            return HttpResponse.success(list);
        } catch (IllegalArgumentException e) {
            log.error("::{}::getMatchBusinessOdds:{}", CommonUtil.getRequestId(matchId), e.getMessage(), e);
            return HttpResponse.error(201, e.getMessage());
        } catch (Exception e) {
            log.error("::{}::getMatchBusinessOdds:{}", CommonUtil.getRequestId(matchId), e.getMessage(), e);
            return HttpResponse.error(500, e.getMessage());
        }
    }

    /**
     * 更新赔率未下发报警
     *
     * @param vo
     * @return
     */
    @PostMapping("/updateWarningSign")
    public HttpResponse updateWarningSign(@RequestBody MarketStatusUpdateVO vo) {
        Assert.notNull(vo.getMatchId(), "赛事id不能为空");
        Assert.notNull(vo.getCategoryId(), "玩法id不能为空");
        try {
            log.info("::{}::赛事置顶:{}，操盘手:{}",CommonUtil.getRequestId(vo.getMatchId(),vo.getCategoryId()),JSONObject.toJSONString(vo),TradeUserUtils.getUserIdNoException());
            sportMatchViewService.updateWarningSign(vo.getMatchId(), vo.getCategoryId());
        } catch (Exception e) {
            log.error("::{}::更新赔率未下发报警:{}", CommonUtil.getRequestId(vo.getMatchId(),vo.getCategoryId()), e.getMessage(), e);
            return HttpResponse.fail("更新失败");
        }
        return HttpResponse.success();
    }


    /**
     * 货量统计切换
     *
     * @param vo
     * @return
     */
    @PostMapping("/upsertBetAmountType")
    public HttpResponse upsertBetAmountType(@RequestBody MarketLiveOddsQueryVo vo) {

        Integer result = 1;
        try {
            Assert.notNull(vo.getUrlType(), "urlType不能为空");
            Integer betAmountType = vo.getBetAmountType();
            Integer userId = TradeUserUtils.getUserId();
            String key = String.format(RedisKey.RCS_TRADER_CHOOSE, userId);
            if (betAmountType == null) betAmountType = 1;

            if (vo.getUrlType() == 1) {
                String value = redisClient.get(key);
                if (StringUtils.isNotBlank(value)) {
                    result = Integer.parseInt(value);
                }
            } else if (vo.getUrlType() == 2) {
                result = betAmountType;
                redisClient.setExpiry(key, betAmountType, EXPRIY_TIME_7_DAYS);
            }
        } catch (Exception e) {
            log.error("::{}::货量统计切换:{}", CommonUtil.getRequestId(vo.getMatchId()), e.getMessage(), e);
            return HttpResponse.fail("更新失败");
        }
        return HttpResponse.success(result);
    }


    /**
     * 更新业务端玩法集展示
     *
     * @param vo
     * @return
     */
    @PostMapping("/updateShow")
    public HttpResponse updateShow(@RequestBody MarketStatusUpdateVO vo) {

        try {
            Assert.notNull(vo.getSportId(), "赛种id不能为空");
            Assert.notNull(vo.getMatchId(), "赛事id不能为空");
            Assert.notNull(vo.getCategorySetId(), "玩法集id不能为空");
            log.info("::{}::更新业务端玩法集展示:{}，操盘手:{}",CommonUtil.getRequestId(vo.getMatchId(),vo.getCategorySetId()),JSONObject.toJSONString(vo),TradeUserUtils.getUserIdNoException());
            rcsTradeConfigService.updateShow(vo);
        } catch (RcsServiceException e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail(e.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail("更新失败");
        }

        return HttpResponse.success();
    }


    @PostMapping("/queryMultiOdds")
    public HttpResponse queryMultiOdds(@RequestBody MarketLiveOddsQueryVo queryVo) {

        List<StandardTxThirdMarketPlayDTO> marketPlayDTOS = null;
        try {
            Assert.notNull(queryVo.getMatchId(), "赛事matchId不能为空");
            Assert.notNull(queryVo.getMarketCategoryIds(), "玩法marketCategoryIds不能为空");
            Assert.notNull(queryVo.getLiveOddBusiness(), "liveOddBusiness不能为空");
            marketPlayDTOS = sportMatchViewService.queryMultiOdds(queryVo);
        } catch (RcsServiceException e) {
            log.error("::{}::queryMultiOdds:{}", CommonUtil.getRequestId(queryVo.getMatchId()), e.getMessage(), e);
            return HttpResponse.fail(e.getMessage());
        } catch (Exception e) {
            log.error("::{}::queryMultiOdds:{}", CommonUtil.getRequestId(queryVo.getMatchId()), e.getMessage(), e);
            return HttpResponse.fail("查询异常:" + e.getMessage());
        }

        return HttpResponse.success(marketPlayDTOS);
    }


    @PostMapping("/getMultiOdds")
    public HttpResponse getMultiOdds(@RequestBody MarketLiveOddsQueryVo queryVo) {

        Map<String, List<ThirdMarketVo>> marketPlayDTOS = null;
        try {
            Assert.notNull(queryVo.getMatchId(), "赛事matchId不能为空");
            Assert.notNull(queryVo.getMarketCategoryIds(), "玩法marketCategoryIds不能为空");
            Assert.notNull(queryVo.getLiveOddBusiness(), "liveOddBusiness不能为空");
            marketPlayDTOS = sportMatchViewService.getMultiOdds(queryVo);
        } catch (RcsServiceException e) {
            log.error("::{}::getMultiOdds:{}", CommonUtil.getRequestId(queryVo.getMatchId()), e.getMessage(), e);
            return HttpResponse.fail(e.getMessage());
        } catch (Exception e) {
            log.error("::{}::getMultiOdds:{}", CommonUtil.getRequestId(queryVo.getMatchId()), e.getMessage(), e);
            return HttpResponse.fail("查询异常:" + e.getMessage());
        }

        return HttpResponse.success(marketPlayDTOS);
    }

    /**
     *
     * 所有状态为已开盘的赛事 重新下发开盘状态
     * @return
     */
    @RequestMapping(value = "/updatePreAllMarketStatus", method = RequestMethod.POST)
    public HttpResponse updatePreAllMarketStatus() {
        try {
            if(openPreAllMatch_Switch){
                return HttpResponse.fail("正在运行中");
            }
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            openPreAllMatch_Switch =  true;

            log.info("::{}::修改早盘所有开盘的操盘状态，操盘手:{}", CommonUtil.getRequestId(),TradeUserUtils.getUserIdNoException());
            MarketLiveOddsQueryVo matchQueryVo = new MarketLiveOddsQueryVo();
            matchQueryVo.setTradeId(Long.valueOf(TradeUserUtils.getUserIdNoException()));
            matchQueryVo.setSortType(1);

            List<MatchMarketLiveBean> matchInfoList = new ArrayList<>();//早盘赛事集合

            //获取早盘赛事
            Criteria criteriaPre = sportMatchViewService.buildMongoCriteria(matchQueryVo, 2);
            PageResult<MatchMarketLiveBean> matchIdPagePre = sportMatchViewService.queryMatchList(matchQueryVo, criteriaPre);
            if(Objects.nonNull(matchIdPagePre)){
                matchInfoList.addAll(matchIdPagePre.getList());
            }
            int totalCount = matchInfoList.size();//搜出总数量
            matchInfoList = matchInfoList.stream().filter(matchMarketLiveBean -> (matchMarketLiveBean.getOperateMatchStatus() == null || matchMarketLiveBean.getOperateMatchStatus() == MarketStatusEnum.OPEN.getState())).collect(Collectors.toList());
            ExecutorService executorService = Executors.newFixedThreadPool(1);
            Integer delTime = 2 * 60;//限制2分钟处理1000赛事
            for (MatchMarketLiveBean matchMarketLiveBean : matchInfoList) {
                executorService.execute(() -> {
                    Long startTime = System.currentTimeMillis();
                    log.info("早盘满足条件的赛事，是否滚球:{}，赛种类型：{}，赛事ID:{}",matchMarketLiveBean.getOddsLive(), matchMarketLiveBean.getSportId(), matchMarketLiveBean.getMatchManageId());
                    MarketStatusUpdateVO updateVO = new MarketStatusUpdateVO();
                    updateVO.setMatchId(matchMarketLiveBean.getMatchId());
                    updateVO.setMarketStatus(MatchTradeStatu.Trade_state_0.getCode());
                    updateVO.setTradeLevel(TradeLevelEnum.MATCH.getLevel());
                    updateVO.setSportId(matchMarketLiveBean.getSportId());
                    updateVO.setLinkId(updateVO.generateLinkId("batch_status"));
                    tradeStatusService.updateTradeStatus(updateVO);
                    Long time_cons = System.currentTimeMillis() - startTime;
                    //控制2分钟处理1000场赛事，控制120毫秒处理一场赛事
                    if(time_cons < delTime){
                        try {
                            Thread.sleep(delTime-time_cons);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
            executorService.shutdown();
            /*while(!executorService.isTerminated()){
                Thread.sleep(100);
            }*/
            stopWatch.stop();
            log.info("::{}::修改早盘所有开盘的操盘状态:{}",CommonUtil.getRequestId(),String.format("早盘共:%s个赛事，满足条件：%s个赛事，处理完成！耗时:%s秒",totalCount,matchInfoList.size(),stopWatch.getTotalTimeSeconds()));
            return HttpResponse.success(String.format("早盘共:%s个赛事，满足条件的赛事：%s个，正在后台陆续处理中。。",totalCount,matchInfoList.size()));
        }catch (Exception e) {
            log.error("::{}::修改早盘所有开盘的操盘状态:{}",CommonUtil.getRequestId(), e.getMessage(), e);
        }finally {
            openPreAllMatch_Switch = false;
        }
        return HttpResponse.fail("早盘批量处理发生异常");
    }

    @RequestMapping(value = "/updateLiveAllMarketStatus", method = RequestMethod.POST)
    public HttpResponse updateLiveAllMarketStatus() {
        try {
            if(openLiveAllMatch_Switch){
                return HttpResponse.fail("正在运行中");
            }
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            openLiveAllMatch_Switch =  true;

            log.info("::{}::修改滚球所有开盘的操盘状态，操盘手:{}", CommonUtil.getRequestId(),TradeUserUtils.getUserIdNoException());

            List<MatchMarketLiveBean> matchInfoList = new ArrayList<>();//滚球赛事总集合

            List<StandardSportType> sportTypeList = standardSportTypeService.getStandardSportTypeList();
            MarketLiveOddsQueryVo matchQueryVo = new MarketLiveOddsQueryVo();
            matchQueryVo.setTradeId(Long.valueOf(TradeUserUtils.getUserIdNoException()));
            matchQueryVo.setSortType(1);
            //根据球种查 为了满足内部搜索条件
            for (StandardSportType standardSportType : sportTypeList) {
                if(Objects.nonNull(standardSportType)){
                    matchQueryVo.setSportId(standardSportType.getNameCode());
                }
                Criteria criteria = sportMatchViewService.buildMongoCriteria(matchQueryVo, 1);
                PageResult<MatchMarketLiveBean> matchIdPageLive = sportMatchViewService.queryMatchList(matchQueryVo, criteria);
                if(Objects.nonNull(matchIdPageLive)){
                    matchInfoList.addAll(matchIdPageLive.getList());
                }
            }
            int totalCount = matchInfoList.size();//搜出总数量
            matchInfoList = matchInfoList.stream().filter(matchMarketLiveBean -> (matchMarketLiveBean.getOperateMatchStatus() == null || matchMarketLiveBean.getOperateMatchStatus() == MarketStatusEnum.OPEN.getState())).collect(Collectors.toList());
            ExecutorService executorService = Executors.newFixedThreadPool(1);
            Integer delTime = 2 * 60;//限制2分钟处理1000赛事
            for (MatchMarketLiveBean matchMarketLiveBean : matchInfoList) {
                executorService.execute(() -> {
                    Long startTime = System.currentTimeMillis();
                    log.info("滚球满足条件的赛事，是否滚球:{}，赛种类型：{}，赛事ID:{}",matchMarketLiveBean.getOddsLive(), matchMarketLiveBean.getSportId(), matchMarketLiveBean.getMatchManageId());
                    MarketStatusUpdateVO updateVO = new MarketStatusUpdateVO();
                    updateVO.setMatchId(matchMarketLiveBean.getMatchId());
                    updateVO.setMarketStatus(MatchTradeStatu.Trade_state_0.getCode());
                    updateVO.setTradeLevel(TradeLevelEnum.MATCH.getLevel());
                    updateVO.setSportId(matchMarketLiveBean.getSportId());
                    updateVO.setLinkId(updateVO.generateLinkId("batch_status"));
                    tradeStatusService.updateTradeStatus(updateVO);
                    Long time_cons = System.currentTimeMillis() - startTime;
                    //控制2分钟处理1000场赛事，控制120毫秒处理一场赛事
                    if(time_cons < delTime){
                        try {
                            Thread.sleep(delTime-time_cons);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
            executorService.shutdown();
            /*while(!executorService.isTerminated()){
                Thread.sleep(100);
            }*/
            stopWatch.stop();
            log.info("::{}::修改滚球所有开盘的操盘状态:{}",CommonUtil.getRequestId(),String.format("滚球共:%s个赛事，满足条件：%s个赛事，处理完成！耗时:%s秒",totalCount,matchInfoList.size(),stopWatch.getTotalTimeSeconds()));
            return HttpResponse.success(String.format("滚球共:%s个赛事，满足条件：%s个，正在后台陆续处理中。。。",totalCount,matchInfoList.size()));
        }catch (Exception e) {
            log.error("::{}::修改滚球所有开盘的操盘状态:{}",CommonUtil.getRequestId(), e.getMessage(), e);
        }finally {
            openLiveAllMatch_Switch = false;
        }
        return HttpResponse.fail("滚球批量处理发生异常");
    }
}
