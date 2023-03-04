package com.panda.sport.rcs.trade.wrapper.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.util.UuidUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.panda.merge.dto.Response;
import com.panda.merge.dto.StandardMarketOddsDTO;
import com.panda.sport.data.rcs.dto.tournament.StandardMarketSellQueryDto;
import com.panda.sport.rcs.common.DateUtils;
import com.panda.sport.rcs.constants.BaseConstants;
import com.panda.sport.rcs.constants.RcsConstant;
import com.panda.sport.rcs.constants.RedisKeys;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.core.utils.BeanCopyUtils;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.enums.MarketKindEnum;
import com.panda.sport.rcs.enums.MarketStatusEnum;
import com.panda.sport.rcs.enums.SellStatusEnum;
import com.panda.sport.rcs.enums.SportIdEnum;
import com.panda.sport.rcs.enums.TradeEnum;
import com.panda.sport.rcs.enums.TradeLevelEnum;
import com.panda.sport.rcs.enums.TradeStatusEnum;
import com.panda.sport.rcs.enums.TraderLevelEnum;
import com.panda.sport.rcs.enums.YesNoEnum;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.mapper.RcsMatchMarketMarginConfigMapper;
import com.panda.sport.rcs.mapper.StandardMatchInfoMapper;
import com.panda.sport.rcs.mapper.StandardSportMarketMapper;
import com.panda.sport.rcs.mapper.StandardSportMarketOddsMapper;
import com.panda.sport.rcs.mapper.tourTemplate.RcsTournamentTemplateMapper;
import com.panda.sport.rcs.mapper.tourTemplate.RcsTournamentTemplatePlayMargainMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.RcsMatchConfig;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.RcsMatchMarketMarginConfig;
import com.panda.sport.rcs.pojo.RcsStandardSportMarketSell;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.pojo.StandardSportMarket;
import com.panda.sport.rcs.pojo.StandardSportMarketOdds;
import com.panda.sport.rcs.pojo.constants.TradeConstant;
import com.panda.sport.rcs.pojo.dto.ClearDTO;
import com.panda.sport.rcs.pojo.dto.ClearSubDTO;
import com.panda.sport.rcs.pojo.dto.utils.SubPlayUtil;
import com.panda.sport.rcs.pojo.statistics.RcsProfitRectangle;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplate;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargain;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargainRef;
import com.panda.sport.rcs.trade.enums.LinkedTypeEnum;
import com.panda.sport.rcs.trade.enums.ManagerCodeEnum;
import com.panda.sport.rcs.trade.enums.TempTypeEnum;
import com.panda.sport.rcs.trade.param.RcsMatchConfigParam;
import com.panda.sport.rcs.trade.service.TradeOddsCommonService;
import com.panda.sport.rcs.trade.service.TradeStatusService;
import com.panda.sport.rcs.trade.service.TradeSubPlayCommonService;
import com.panda.sport.rcs.trade.service.TradeVerificationService;
import com.panda.sport.rcs.trade.service.impl.TradeModeServiceImpl;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.util.MarginUtils;
import com.panda.sport.rcs.trade.util.NameExpressionValueUtils;
import com.panda.sport.rcs.trade.vo.MarketBalanceVo;
import com.panda.sport.rcs.trade.vo.MarketProfitVo;
import com.panda.sport.rcs.trade.wrapper.IRcsMatchMarketConfigService;
import com.panda.sport.rcs.trade.wrapper.ITOrderDetailService;
import com.panda.sport.rcs.trade.wrapper.MarketStatusService;
import com.panda.sport.rcs.trade.wrapper.MarketViewService;
import com.panda.sport.rcs.trade.wrapper.MatchStatisticsInfoService;
import com.panda.sport.rcs.trade.wrapper.MatchTradeConfigService;
import com.panda.sport.rcs.trade.wrapper.RcsMatchConfigService;
import com.panda.sport.rcs.trade.wrapper.RcsOddsConvertMappingService;
import com.panda.sport.rcs.trade.wrapper.RcsStandardSportMarketSellService;
import com.panda.sport.rcs.trade.wrapper.RcsTradeConfigService;
import com.panda.sport.rcs.trade.wrapper.StandardSportMarketOddsService;
import com.panda.sport.rcs.trade.wrapper.StandardSportMarketService;
import com.panda.sport.rcs.trade.wrapper.statistics.RcsProfitRectangleService;
import com.panda.sport.rcs.trade.wrapper.tourTemplate.IRcsTournamentTemplatePlayMargainService;
import com.panda.sport.rcs.trade.wrapper.tourTemplate.IRcsTournamentTemplateService;
import com.panda.sport.rcs.vo.MarketStatusUpdateVO;
import com.panda.sport.rcs.vo.OrderDetailStatReportVo;
import com.panda.sport.rcs.vo.SportTeam;
import com.panda.sport.rcs.vo.StandardMarketSellQueryV2Vo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.ws.rs.HEAD;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.panda.sport.rcs.constants.RedisKey.MATCH_LIVE_RISK_MANAGERCODE;


/**
 * @ClassName MatchTradeConfigServiceImpl
 * @Description: TODO
 * @Author Enzo
 * @Date
 **/
@Service
@Slf4j
public class MatchTradeConfigServiceImpl implements MatchTradeConfigService {
    @Autowired
    private RedisClient redisClient;
    @Autowired
    private MarketViewService marketViewService;
    @Autowired
    private ITOrderDetailService orderDetailService;
    @Autowired
    private RcsMatchConfigService rcsMatchConfigService;
    @Autowired
    private StandardMatchInfoMapper standardMatchInfoMapper;
    @Autowired
    private RcsTradeConfigService rcsTradeConfigService;
    @Autowired
    private RcsProfitRectangleService rcsProfitRectangleService;
    @Autowired
    private StandardSportMarketOddsMapper standardSportMarketOddsMapper;
    @Autowired
    private RcsTournamentTemplatePlayMargainMapper rcsTournamentTemplatePlayMargainMapper;
    @Autowired
    private IRcsMatchMarketConfigService iRcsMatchMarketConfigService;
    @Autowired
    private RcsOddsConvertMappingService rcsOddsConvertMappingService;
    @Autowired
    private StandardSportMarketService standardSportMarketService;
    @Autowired
    private StandardSportMarketMapper standardSportMarketMapper;
    @Autowired
    private TradeVerificationService tradeVerificationService;
    @Autowired
    RcsMatchMarketMarginConfigMapper rcsMatchMarketMarginConfigMapper;
    @Autowired
    private TradeSubPlayCommonService tradeSubPlayCommonService;
    @Autowired
    private RcsStandardSportMarketSellService rcsStandardSportMarketSellService;
    @Autowired
    private MarketStatusService marketStatusService;
    @Autowired
    private StandardSportMarketOddsService standardSportMarketOddsService;
    @Autowired
    private ProducerSendMessageUtils producerSendMessageUtils;
    @Autowired
    private IRcsMatchMarketConfigService rcsMatchMarketConfigService;
    @Autowired
    private RcsTournamentTemplateMapper rcsTournamentTemplateMapper;
    @Autowired
    private IRcsTournamentTemplateService rcsTournamentTemplateService;
    @Autowired
    private IRcsTournamentTemplatePlayMargainService rcsTournamentTemplatePlayMargainService;
    @Autowired
    private TradeStatusService tradeStatusService;
    @Autowired
    private TradeOddsCommonService tradeOddsCommonService;
    @Autowired
    MatchStatisticsInfoService matchStatisticsInfoService;
    @Autowired
    private TradeModeServiceImpl tradeModeService;

    private String BALANCE_KEY = "rcs:balance:query:%s:%s";

    private static String MY_DEFAULT_MARGIN = "0.1";
    private static String EU_DEFAULT_MARGIN = "110";
    private static String DEFAULT_BET_MAx = "1000000";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateRiskManagerCode(RcsMatchConfigParam config) {
        log.info("::{}::操盘平台切换-入参：{}",config.getMatchId(), JsonFormatUtils.toJson(config));
        StandardMatchInfo matchInfo = standardMatchInfoMapper.selectById(config.getMatchId());
        if (ObjectUtils.isEmpty(matchInfo)) {
            throw new RcsServiceException("未找到赛事数据");
        }
        //校验滚球阶段PA是否允许切换MTS（SR必须支持滚球）
        if (config.getMatchType().intValue() == 2 && ManagerCodeEnum.PA.getId().equals(matchInfo.getLiveRiskManagerCode())) {
            marketViewService.checkChangeMTS(config);
        }

        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("matchId", config.getMatchId());
        if (config.getMatchType() == 1) {//早盘
            updateMap.put("preRiskManagerCode", config.getPreRiskManagerCode());
            updateMap.put("set_risk_manager_code", config.getPreRiskManagerCode());
        } else if (config.getMatchType() == 2) {
            updateMap.put("liveRiskManagerCode", config.getLiveRiskManagerCode());
            updateMap.put("set_risk_manager_code", config.getLiveRiskManagerCode());
            if (matchInfo.getSportId().equals(NumberUtils.LONG_ONE) && matchInfo.getEventTime() != null && matchInfo.getSecondsMatchStart() != null && matchInfo.getSecondsMatchStart() > 0 && matchInfo.getMatchStatus() != 0) {
                Long time = System.currentTimeMillis() - matchInfo.getEventTime().longValue() + matchInfo.getSecondsMatchStart().longValue() * 1000;
                if (time > 80 * 60 * 1000) {
                    //80分钟
                    throw new RcsServiceException("比赛进行到滚球80分钟之后，不能设置");
                }
            }
        }

        boolean isPre = config.getMatchType() == 1 && !NumberUtils.INTEGER_ONE.equals(matchInfo.getOddsLive());
        boolean isLive = config.getMatchType() == 2 && NumberUtils.INTEGER_ONE.equals(matchInfo.getOddsLive());
        // PA切换MTS操盘
        if ((isPre && ManagerCodeEnum.MTS.isYes(config.getPreRiskManagerCode())) ||
                (isLive && ManagerCodeEnum.MTS.isYes(config.getLiveRiskManagerCode()))) {
            // 早盘切换MTS 或 滚球切换MTS，所有玩法切自动
            allPlaySwitchAuto(matchInfo.getSportId(), config.getMatchId());
        }

        //更新开售表的操盘方式
        RcsStandardSportMarketSell marketSell = rcsStandardSportMarketSellService.selectStandardMarketSellVo(config.getMatchId());
        // MTS切换PA操盘
        if ((SellStatusEnum.SOLD.isYes(marketSell.getPreMatchSellStatus()) && isPre && ManagerCodeEnum.PA.isYes(config.getPreRiskManagerCode())) ||
                (SellStatusEnum.SOLD.isYes(marketSell.getLiveMatchSellStatus()) && isLive && ManagerCodeEnum.PA.isYes(config.getLiveRiskManagerCode()))) {
            // 早盘切换PA 或 滚球切换PA，赛事关盘、清理水差和盘口差等
            matchCloseAndClear(matchInfo.getSportId(), config.getMatchId(), matchInfo.getBeginTime());
        }
        if ((marketSell.getLiveMatchSellStatus().equals("Sold") && ManagerCodeEnum.MTS.getId().equals(matchInfo.getLiveRiskManagerCode()))
                || (marketSell.getPreMatchSellStatus().equals("Sold") && ManagerCodeEnum.MTS.getId().equals(matchInfo.getPreRiskManagerCode()))) {
            if (config.getMatchType() == 1 && matchInfo.getOddsLive() == 1) {
                //早盘切换操盘方式，odds_live=1（接收到滚球赔率）时，不允许切换
                throw new RcsServiceException("赛事进入滚球，早盘不允许切换！");
            } else if (config.getMatchType() == 2 && matchInfo.getOddsLive() == 0) {
                //切换滚球操盘方式，如果odds_live=0（未接收到滚球赔率），则赛事不做关盘处理，水差不清空；
            }

            //kir-1529-MTS切PA
            if(!TradeConstant.OTHER_BALL.contains(matchInfo.getSportId().intValue())){
                //1.关闭所有已开售玩法
                Integer matchType = config.getMatchType();
                if(matchType.equals(2)){
                    matchType = 0;
                }
                RcsTournamentTemplate template = new RcsTournamentTemplate();
                template.setSportId(matchInfo.getSportId().intValue());
                template.setTypeVal(config.getMatchId());
                template.setMatchType(matchType);
                rcsTournamentTemplatePlayMargainMapper.closeAllPlaysSell(template);

                //2.查询出当前赛事模板所属的联赛模板，根据copyTemplate
                LambdaQueryWrapper<RcsTournamentTemplate> queryTemplateWrapper = new LambdaQueryWrapper<>();
                queryTemplateWrapper.eq(RcsTournamentTemplate::getSportId, matchInfo.getSportId().intValue());
                queryTemplateWrapper.eq(RcsTournamentTemplate::getTypeVal, config.getMatchId());
                queryTemplateWrapper.eq(RcsTournamentTemplate::getMatchType, matchType);
                RcsTournamentTemplate matchTemplate = rcsTournamentTemplateMapper.selectOne(queryTemplateWrapper);
                RcsTournamentTemplate tournamentTemplate = rcsTournamentTemplateMapper.selectById(matchTemplate.getCopyTemplateId());

                //3.查询出所属的联赛模板所有已开售的玩法
                LambdaQueryWrapper<RcsTournamentTemplatePlayMargain> queryTemplatePlayMargainWrapper = new LambdaQueryWrapper<>();
                queryTemplatePlayMargainWrapper.eq(RcsTournamentTemplatePlayMargain::getTemplateId, tournamentTemplate.getId());
                queryTemplatePlayMargainWrapper.eq(RcsTournamentTemplatePlayMargain::getIsSell, 1);
                List<RcsTournamentTemplatePlayMargain> margains = rcsTournamentTemplatePlayMargainMapper.selectList(queryTemplatePlayMargainWrapper);

                //4.将联赛模板中已开售的玩法，为对应的赛事模板设置为开售
                List<Long> playIds = margains.stream().map(e -> e.getPlayId().longValue()).collect(Collectors.toList());
                rcsTournamentTemplatePlayMargainMapper.updatePlayMargainIsSellByPlayId(template, playIds);
                config.setCategoryIds(playIds);
            }else{
                //其他赛种则把所有需要操盘的玩法发给融合
                List<Long> allPlaysBySportId = SportIdEnum.getAllPlaysBySportId(matchInfo.getSportId());
                List<Long> playIds = allPlaysBySportId.stream().map(e -> e.longValue()).collect(Collectors.toList());
                config.setCategoryIds(playIds);
            }
        }

        //调用融合接口，更改操盘平台
        Response response = marketViewService.updateRiskManagerCodeByDataManager(config);
        if (response.isSuccess()) {
            log.info("::{}::操盘平台切换-赛事操盘配置：{}",config.getMatchId(), JsonFormatUtils.toJson(updateMap));
            rcsMatchConfigService.updateRiskManagerCode(updateMap);
            //更新赛事表的操盘方式
            StandardMatchInfo standardMatchInfo = standardMatchInfoMapper.selectById(config.getMatchId());
            if (config.getMatchType() == 1) {
                //早盘
                standardMatchInfo.setPreRiskManagerCode(config.getPreRiskManagerCode());
                marketSell.setPreRiskManagerCode(config.getPreRiskManagerCode());
                marketSell.setPreMatchDataProviderCode("SR");
            } else if (config.getMatchType() == 2) {
                //滚球
                standardMatchInfo.setLiveRiskManagerCode(config.getLiveRiskManagerCode());
                marketSell.setLiveRiskManagerCode(config.getLiveRiskManagerCode());
                marketSell.setLiveMatchDataProviderCode("SR");
                marketSell.setBusinessEvent("SR");
                //更新滚球接拒单事件源
                StandardMarketSellQueryDto sell = new StandardMarketSellQueryDto();
                sell.setSportId(matchInfo.getSportId());
                sell.setMatchId(config.getMatchId());
                sell.setDataSouceCode("SR");
                log.info("::{}::操盘平台切换-接拒单事件配置表：{}",config.getMatchId(), JsonFormatUtils.toJson(sell));
                rcsStandardSportMarketSellService.updateTemplateEventSourceConfig(sell);
            }
            log.info("::{}::操盘平台切换-开售表：{}", config.getMatchId(), JsonFormatUtils.toJson(marketSell));
            rcsStandardSportMarketSellService.updateById(marketSell);
            log.info("::{}::操盘平台切换-赛事表：{}", config.getMatchId(), JsonFormatUtils.toJson(standardMatchInfo));
            standardMatchInfoMapper.updateById(standardMatchInfo);
        } else {
            throw new RcsServiceException(response.getMsg());
        }
        Map<String, Object> resultMap = new HashMap<>();
        QueryWrapper<RcsMatchConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(RcsMatchConfig::getMatchId, config.getMatchId());
        RcsMatchConfig newConfig = rcsMatchConfigService.getOne(queryWrapper);
        if (newConfig != null) {
            resultMap.put("matchId", newConfig.getMatchId());
            resultMap.put("preRiskManagerCode", newConfig.getPreRiskManagerCode());
            resultMap.put("liveRiskManagerCode", newConfig.getLiveRiskManagerCode());
        }

        //事件接拒MTS不触发封盘 TODO Enzo bug 24643
//        redisClient.delete(String.format(MATCH_LIVE_RISK_MANAGERCODE,config.getMatchId()));
        if (StringUtils.isNotBlank(config.getLiveRiskManagerCode())) {
            String key = String.format(MATCH_LIVE_RISK_MANAGERCODE, config.getMatchId());
            redisClient.set(key, config.getLiveRiskManagerCode());
            redisClient.expireKey(key, (int) TimeUnit.HOURS.toSeconds(2));
        }
        return resultMap;
    }

    private void allPlaySwitchAuto(Long sportId, Long matchId) {
        List<Long> playIds = rcsTradeConfigService.getNotAutoPlayIds(matchId);
        if (!CollectionUtils.isEmpty(playIds)) {
            MarketStatusUpdateVO tradeTypeVo = new MarketStatusUpdateVO()
                    .setTradeLevel(TradeLevelEnum.BATCH_PLAY.getLevel())
                    .setSportId(sportId)
                    .setMatchId(matchId)
                    .setCategoryIdList(playIds)
                    .setTradeType(TradeEnum.AUTO.getCode())
                    .setIsSeal(YesNoEnum.N.getValue())
                    .setLinkedType(LinkedTypeEnum.PA_2_MTS.getCode());
            log.info("::{}::操盘平台切换-玩法切换成自动:{}",matchId, JsonFormatUtils.toJson(tradeTypeVo));
            tradeModeService.updateTradeMode(tradeTypeVo);
        }
    }

    private void matchCloseAndClear(Long sportId, Long matchId, Long beginTime) {
        // 赛事关盘
        MarketStatusUpdateVO statusVo = new MarketStatusUpdateVO().
                setTradeLevel(TradeLevelEnum.MATCH.getLevel())
                .setSportId(sportId)
                .setMatchId(matchId)
                .setMarketStatus(TradeStatusEnum.CLOSE.getStatus())
                .setLinkedType(LinkedTypeEnum.MTS_2_PA.getCode());
        log.info("::{}::操盘平台切换-赛事关盘:{}",matchId, JsonFormatUtils.toJson(statusVo));
        tradeStatusService.updateTradeStatus(statusVo);
        // 清理水差和盘口差
        ClearDTO clearDTO = new ClearDTO();
        clearDTO.setType(0);
        clearDTO.setClearType(8);
        clearDTO.setMatchId(matchId);
        clearDTO.setBeginTime(beginTime);
        ArrayList<ClearSubDTO> objects = new ArrayList<>();
        ClearSubDTO clearSubDTO = new ClearSubDTO();
        clearSubDTO.setMatchId(matchId);
        objects.add(clearSubDTO);
        clearDTO.setList(objects);

        producerSendMessageUtils.sendMessage("RCS_CLEAR_MATCH_MARKET_TAG", null, UuidUtils.generateUuid(), clearDTO);
    }

    @Override
    public Map<String, String> getRiskManagerCode(Long matchId) {
        Map<String, String> map = new HashMap<>();
        QueryWrapper<RcsMatchConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(RcsMatchConfig::getMatchId, matchId);
        RcsMatchConfig config = rcsMatchConfigService.getOne(queryWrapper);
        if (config != null) {
            map.put("preRiskManagerCode", config.getPreRiskManagerCode());
            map.put("liveRiskManagerCode", config.getLiveRiskManagerCode());
        }
        StandardMatchInfo matchInfo = standardMatchInfoMapper.selectById(matchId);
        if (config == null) {
            if (matchInfo != null) {
                map.put("preRiskManagerCode", matchInfo.getPreRiskManagerCode());
                map.put("liveRiskManagerCode", matchInfo.getLiveRiskManagerCode());
            } else {
                map.put("preRiskManagerCode", "PA");
                map.put("liveRiskManagerCode", "PA");
            }
        }
        map.put("data_source_code", matchInfo.getDataSourceCode());
        return map;
    }

    @Override
    public List<MarketProfitVo> getProfitByMatchIdAndPlayId(RcsProfitRectangle rcsProfitRectangle) {

        if (rcsProfitRectangle.getMatchType() == null) {
            rcsProfitRectangle.setMatchType(1);
        }
        if (rcsProfitRectangle.getScore() == null) {
            rcsProfitRectangle.setScore(0);
        }
        QueryWrapper<RcsProfitRectangle> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(RcsProfitRectangle::getMatchId, rcsProfitRectangle.getMatchId());
        wrapper.lambda().eq(RcsProfitRectangle::getPlayId, rcsProfitRectangle.getPlayId());
        wrapper.lambda().eq(RcsProfitRectangle::getMatchType, rcsProfitRectangle.getMatchType());
        //根据不同玩法组装查询条件，期望值固定显示7列
        switch (rcsProfitRectangle.getPlayId()) {
            //全场大小，上半场大小
            case 2:
            case 122:
            case 114:
            case 18:
                if (rcsProfitRectangle.getScore() < 0 || rcsProfitRectangle.getScore() > 24) {
                    rcsProfitRectangle.setScore(0);
                }
                wrapper.lambda().between(RcsProfitRectangle::getScore, rcsProfitRectangle.getScore(), rcsProfitRectangle.getScore() + 6);
                break;
            //全场让球，上半场让球
            case 4:
            case 113:
            case 121:
            case 19:
                wrapper.lambda().between(RcsProfitRectangle::getScore, rcsProfitRectangle.getScore() - 3, rcsProfitRectangle.getScore() + 3);
                break;
            default:
                break;
        }
        wrapper.lambda().orderByDesc(RcsProfitRectangle::getScore);
        List<RcsProfitRectangle> rcsProfitRectangleList = rcsProfitRectangleService.list(wrapper);
        List<MarketProfitVo> list = BeanCopyUtils.copyPropertiesList(rcsProfitRectangleList, MarketProfitVo.class);

        if (rcsProfitRectangleList == null || rcsProfitRectangleList.size() == 0
                || list == null || list.size() == 0) {
            list = new ArrayList<>();
            if (rcsProfitRectangle.getPlayId() == 2 || rcsProfitRectangle.getPlayId() == 18) {
                for (int i = 0 + rcsProfitRectangle.getScore(); i <= 6 + rcsProfitRectangle.getScore(); i++) {
                    MarketProfitVo profiRectangleVo = new MarketProfitVo();
                    profiRectangleVo.setProfitValue(BigDecimal.ZERO);
                    profiRectangleVo.setScore(i);
                    list.add(profiRectangleVo);
                }
            } else if (rcsProfitRectangle.getPlayId() == 4 || rcsProfitRectangle.getPlayId() == 19) {
                for (int i = -3 + rcsProfitRectangle.getScore(); i <= 3 + rcsProfitRectangle.getScore(); i++) {
                    MarketProfitVo profiRectangleVo = new MarketProfitVo();
                    profiRectangleVo.setProfitValue(BigDecimal.ZERO);
                    profiRectangleVo.setScore(i);
                    list.add(profiRectangleVo);
                }
            }
        }
        return list;
    }

    @Override
    public Map<String, Object> getBalancesByMatchIdAndPlayId(RcsMatchMarketConfig config, Integer matchType) {
        List<OrderDetailStatReportVo> list = orderDetailService.getMarketStatByMatchIdAndPlayIdAndMatchStatus(config.getMatchId(), config.getPlayId(), matchType);

        List<MarketBalanceVo> oddList = Lists.newArrayList();
        Map<Long, MarketBalanceVo> markets = Maps.newHashMap();
        for (OrderDetailStatReportVo rvo : list) {
            MarketBalanceVo vo = markets.get(rvo.getMarketId());
            if (vo == null) {
                vo = new MarketBalanceVo();
                vo.setMarketId(rvo.getMarketId());
                vo.setMarketValue(rvo.getMarketValue());
                markets.put(rvo.getMarketId(), vo);
            }
            if ("home".equals(rvo.getOddsType())) {
                vo.setHomeAmount(rvo.getBetAmount() / 100);
            } else {
                vo.setAwayAmount(rvo.getBetAmount() / 100 * -1);
            }
            vo.setBalanceValue(vo.getHomeAmount() + vo.getAwayAmount());
        }
        for (Map.Entry<Long, MarketBalanceVo> entry : markets.entrySet()) {
            oddList.add(entry.getValue());
        }
        Collections.sort(oddList, new Comparator<MarketBalanceVo>() {
            @Override
            public int compare(MarketBalanceVo o1, MarketBalanceVo o2) {
                return (int) ((Float.parseFloat(o1.getMarketValue()) - Float.parseFloat(o2.getMarketValue())) * 100);
            }
        });
        //标准化输出
        Map<String, Object> info = Maps.newHashMap();
        info.put("values", oddList);
        return info;
    }

    @Override
    public MarketBalanceVo queryBalance(Long matchId, Long marketId, String marketType, Integer balanceOption) {
//        Integer userId = null;
//        try {
//            userId = TradeUserUtils.getUserId();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        String key = String.format("query_Balance_%s_%s_%s_%s", userId, matchId, marketId, marketType);
//        String time = redisClient.get(key);
//        if (StringUtils.isNotBlank(time)) {
//            throw new RcsServiceException("刷新太频繁，刷新间隔2s");
//        }
//        String redisString = redisClient.get(String.format(BALANCE_KEY, matchId, marketId));
        MarketBalanceVo vo = new MarketBalanceVo();
        vo.setMatchId(matchId);
        vo.setMarketId(marketId);
        // 设置水差和margin
//        QueryWrapper<RcsMatchMarketConfig> queryWrapper = new QueryWrapper<>();
//        queryWrapper.lambda().eq(RcsMatchMarketConfig ::getMarketId,marketId);
//        RcsMatchMarketConfig rcsMatchMarketConfig = rcsMatchMarketConfigMapper.selectOne(queryWrapper);
        QueryWrapper<RcsMatchMarketMarginConfig> queryWrapper = new QueryWrapper();
        queryWrapper.lambda().eq(RcsMatchMarketMarginConfig::getMarketId, marketId);
        RcsMatchMarketMarginConfig rcsMatchMarketConfig = rcsMatchMarketMarginConfigMapper.selectOne(queryWrapper);
//        if (StringUtils.isNotBlank(redisString)) {
//            vo = JSONObject.parseObject(redisString, MarketBalanceVo.class);
//        }else {
        // redis 没有从redis重新计算
        QueryWrapper<StandardMatchInfo> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(StandardMatchInfo::getId, matchId);
        wrapper.lambda().select(StandardMatchInfo::getBeginTime);
        StandardMatchInfo standardMatchInfo = standardMatchInfoMapper.selectOne(wrapper);
        if (ObjectUtils.isEmpty(standardMatchInfo)) {
            throw new RcsServiceException("赛事不存在" + matchId);
        }
        if (ObjectUtils.isEmpty(standardMatchInfo.getBeginTime())) {
            standardMatchInfo.setBeginTime(System.currentTimeMillis());
        }
        String dateExpect = DateUtils.getDateExpect(standardMatchInfo.getBeginTime());
        balanceOption = balanceOption == null ? 1 : balanceOption;
        String keyBalance = "";
        // 根据跳赔设置获取不同的缓存
        if (org.apache.commons.lang3.math.NumberUtils.INTEGER_ZERO.intValue() == balanceOption.intValue()) {
            keyBalance = String.format(RedisKeys.CALC_AMOUNT_ODDS_CHANGE, dateExpect, marketId);
        }
        if (org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE.intValue() == balanceOption.intValue()) {
            keyBalance = String.format(RedisKeys.CALC_AMOUNT_ODDS_CHANGE_PLUS, dateExpect, marketId);
        }
        keyBalance = keyBalance + "{" + marketId + "}";

        Object obj = redisClient.hGetAllToObj(keyBalance);
        log.info("::{}::查询返回平衡值,keyBalance：{}, result :{}",matchId, keyBalance, JSONObject.toJSONString(obj));
        if (!ObjectUtils.isEmpty(obj)) {
            // 先得到最大金额和总金额
            HashMap<String, String> map = Maps.newHashMap();
            BigDecimal maxAmount = new BigDecimal("0");
            BigDecimal totalAmount = new BigDecimal("0");
            map = (HashMap) obj;
            String oddsType = "";

            for (Map.Entry<String, String> m : map.entrySet()) {
                String amount = m.getValue();
                if (StringUtils.isNotBlank(amount)) {
                    totalAmount = totalAmount.add(new BigDecimal(amount));
                    if (maxAmount.compareTo(new BigDecimal(amount)) == -1) {
                        maxAmount = new BigDecimal(m.getValue());
                        oddsType = m.getKey();
                    }
                }
            }
            QueryWrapper<StandardSportMarketOdds> wrapperOdds = new QueryWrapper<>();
            wrapperOdds.lambda().eq(StandardSportMarketOdds::getMarketId, marketId);
            List<StandardSportMarketOdds> list = standardSportMarketOddsMapper.selectList(wrapperOdds);
            if (org.apache.commons.collections.CollectionUtils.isNotEmpty(list)) {
                for (StandardSportMarketOdds odds : list) {
                    if (odds.getOddsType().equalsIgnoreCase(oddsType)) {
                        vo.setBalanceValue(maxAmount.subtract(totalAmount.subtract(maxAmount)).longValue());
                        vo.setCurrentSide(odds.getOddsType());
                        redisClient.setExpiry(String.format(BALANCE_KEY, matchId, marketId), JSONObject.toJSONString(vo), 2 * 60 * 60L);
                        break;
                    }
                }
            }
        }
//        }
        if (!ObjectUtils.isEmpty(rcsMatchMarketConfig)) {
            vo.setHomeMargin(rcsMatchMarketConfig.getHomeMargin());
            vo.setAwayMargin(rcsMatchMarketConfig.getAwayMargin());
            vo.setTieMargin(rcsMatchMarketConfig.getTieMargin());
            vo.setAwayAutoChangeRate(rcsMatchMarketConfig.getAwayAutoChangeRate());
        }
        log.info("::{}::查询返回平衡值,keyBalance：{}, vo :{}",matchId, keyBalance, JSONObject.toJSONString(vo));
//        redisClient.setExpiry(key, vo.toString(), 2L);
        return vo;
    }

    @Override
    public RcsMatchMarketConfig queryMatchMarketConfig(RcsMatchMarketConfig config) {
        tradeSubPlayCommonService.setSubPlayId(config);
        if (config.getSubPlayId().indexOf("-") >= 0){
            config.setSubPlayId(SubPlayUtil.getWebSubPlayId(config));
        }

        RcsMatchMarketConfig rcsMatchMarketConfig = iRcsMatchMarketConfigService.queryMatchMarketConfigNew(config);
        log.info("::{}::,数据库盘口原有配置{}", CommonUtil.getRequestId(config.getMatchId(), config.getPlayId()),JSONObject.toJSONString(rcsMatchMarketConfig));
        RcsTournamentTemplatePlayMargain matchConfig = rcsTournamentTemplatePlayMargainMapper.queryTournamentAdjustRangeByPlayId(config);
        if (ObjectUtils.isEmpty(matchConfig)) {
            throw new RcsServiceException("玩法未开售，不能新增盘口");
        }
        if (ObjectUtils.isEmpty(rcsMatchMarketConfig)) {
            // 从联赛配置获取
            rcsMatchMarketConfig = rcsMatchMarketConfigService.getRcsMatchMarketConfigByConfig(config, NumberUtils.INTEGER_ONE);
            log.info("::{}::,数据库盘口为空config={}", CommonUtil.getRequestId(config.getMatchId(), config.getPlayId()),JSONObject.toJSONString(config));
        }

        rcsMatchMarketConfig.setMarketType(rcsMatchMarketConfig.getMarketType() == null ? config.getMarketType() : rcsMatchMarketConfig.getMarketType());
        rcsMatchMarketConfig.setMarketId(config.getMarketId());
        rcsMatchMarketConfig.setIsSpecialPumping(matchConfig.getIsSpecialPumping());
        rcsMatchMarketConfig.setSpecialOddsInterval(matchConfig.getSpecialOddsInterval());
        if (!ObjectUtils.isEmpty(config.getMarketId())) {
            RcsMatchMarketMarginConfig rcsMatchMarketMarginConfig = tradeOddsCommonService.getFootballWaterDiff(config);
            if (!ObjectUtils.isEmpty(rcsMatchMarketMarginConfig)) {
                rcsMatchMarketConfig.setAwayAutoChangeRate(rcsMatchMarketMarginConfig.getAwayAutoChangeRate());
            }
        }

        //状态取盘口的
        rcsMatchMarketConfig.setMarketStatus(tradeVerificationService.getMarketIndexStatus(config, SportIdEnum.FOOTBALL.getId()));
        rcsMatchMarketConfig.setMatchType(config.getMatchType());
        //状态玩法集状态
        rcsMatchMarketConfig.setPlaySetCodeStatus(tradeStatusService.getPlaySetCodeStatus(config.getSportId().longValue(),config.getMatchId(),config.getPlayId()));
        // 设置margin和最大可投
        if (ObjectUtils.isEmpty(rcsMatchMarketConfig.getMaxBetAmount()) || ObjectUtils.isEmpty(rcsMatchMarketConfig.getMargin())) {
            setMarginAndMaxAmount(rcsMatchMarketConfig);
        }

        if (!ObjectUtils.isEmpty(rcsMatchMarketConfig.getMarketId())) {
            //自动手动放进去
            Integer dataSource = rcsTradeConfigService.getDataSource(config.getMatchId(), config.getPlayId());
            //是否使用数据源0：手动；1：使用数据源。 没有配置即使用数据源
            rcsMatchMarketConfig.setDataSource(dataSource.longValue());
            //如果有盘口值 需要设置盘口值  取实时值
            StandardSportMarket standardSportMarket = standardSportMarketService.getStandardSportMarketById(config.getMarketId());
            if (ObjectUtils.isEmpty(standardSportMarket)) {
                log.info("::{}::该盘口没数据:{}",config.getMatchId(), config.getMarketId());
                throw new RcsServiceException("该盘口没数据");
            }

            rcsMatchMarketConfig.setThirdMarketSourceStatus(standardSportMarket.getThirdMarketSourceStatus());
            if (MarketStatusEnum.OPEN.getState() == rcsMatchMarketConfig.getThirdMarketSourceStatus()) {
                rcsMatchMarketConfig.setThirdMarketSourceStatus(standardSportMarket.getPaStatus());
            }

            Integer lastMatchStatus = rcsTradeConfigService.getLatestStatusByLevel(config.getMatchId(), TraderLevelEnum.MATCH, config.getMatchId());
            rcsMatchMarketConfig.setOperateMatchStatus(lastMatchStatus);
            //设置赔率列表和margin
            setOddsList(config, rcsMatchMarketConfig);
            String addition1 = standardSportMarket.getAddition1();
            if (StringUtils.isNotBlank(addition1)) {
                try {
                    if (Double.parseDouble(addition1) >= 0) {
                        rcsMatchMarketConfig.setAwayMarketValue(new BigDecimal(addition1));
                        rcsMatchMarketConfig.setHomeMarketValue(new BigDecimal("0"));
                    } else if (Double.parseDouble(addition1) < 0) {
                        rcsMatchMarketConfig.setHomeMarketValue(new BigDecimal(addition1).multiply(new BigDecimal(-1)));
                        rcsMatchMarketConfig.setAwayMarketValue(new BigDecimal("0"));
                    }
                } catch (NumberFormatException e) {
                    rcsMatchMarketConfig.setHomeMarketValue(new BigDecimal("0"));
                    rcsMatchMarketConfig.setAwayMarketValue(new BigDecimal("0"));
                }
            }
        } else {
            rcsMatchMarketConfig.setDataSource(NumberUtils.LONG_ONE);
        }
        // 给基准分比分
        if (TradeConstant.FOOTBALL_BENCHMARK_SCORE_PLAYS.contains(config.getPlayId().intValue()) ||
                TradeConstant.FOOTBALL_X_A3_PLAYS.contains(config.getPlayId().intValue())){
            String benchmarks = matchStatisticsInfoService.queryCurrentScoreByPlayId(config);
            rcsMatchMarketConfig.setScore(benchmarks);
        }
        rcsMatchMarketConfig.setSubPlayId(config.getSubPlayId());
        return rcsMatchMarketConfig;
    }

    /**
     * @return java.math.BigDecimal
     * @Description //margin 自动从赔率表获取，手动从盘口配置获取
     * @Param [config, rcsMatchMarketConfig]
     * @Author Sean
     * @Date 17:08 2020/8/22
     **/
    private void setMarginAndMaxAmount(RcsMatchMarketConfig rcsMatchMarketConfig) {
        BigDecimal margin = null;
        BigDecimal amount = null;
        RcsTournamentTemplatePlayMargainRef templatePlayMargainRef = rcsTournamentTemplatePlayMargainMapper.queryMarginByPlayId(rcsMatchMarketConfig);
        if (!ObjectUtils.isEmpty(templatePlayMargainRef)) {
            if (!ObjectUtils.isEmpty(templatePlayMargainRef.getMargain())) {
                margin = new BigDecimal(templatePlayMargainRef.getMargain());
            }
            if (!ObjectUtils.isEmpty(templatePlayMargainRef.getOrderSinglePayVal())) {
                amount = BigDecimal.valueOf(templatePlayMargainRef.getOrderSinglePayVal());
                if (StringUtils.isNotBlank(templatePlayMargainRef.getViceMarketRatio()) &&
                        NumberUtils.INTEGER_ONE.intValue() != rcsMatchMarketConfig.getMarketIndex()) {
                    JSONArray array = JSONArray.parseArray(templatePlayMargainRef.getViceMarketRatio());
                    if (!ObjectUtils.isEmpty(array) &&
                            (array.size() >= (rcsMatchMarketConfig.getMarketIndex() - NumberUtils.INTEGER_ONE))) {
                        Object betAmount = array.get(rcsMatchMarketConfig.getMarketIndex() - NumberUtils.INTEGER_TWO);
                        amount = amount.multiply(new BigDecimal(betAmount.toString())).divide(new BigDecimal(BaseConstants.MULTIPLE_BET_AMOUNT_VALUE), NumberUtils.INTEGER_ZERO, BigDecimal.ROUND_DOWN);
                    }
                }
            }
        }
        if (ObjectUtils.isEmpty(margin)) {
            log.info("::{}::联赛配置的margin没有{}",rcsMatchMarketConfig.getMatchId(), JSONObject.toJSONString(rcsMatchMarketConfig));
            margin = new BigDecimal(MY_DEFAULT_MARGIN);
            if (MarketKindEnum.Europe.getValue().equalsIgnoreCase(rcsMatchMarketConfig.getMarketType())) {
                margin = new BigDecimal(EU_DEFAULT_MARGIN);
            }
        }
        if (ObjectUtils.isEmpty(rcsMatchMarketConfig.getMargin())) {
            rcsMatchMarketConfig.setMargin(margin);
        }
        if (ObjectUtils.isEmpty(amount)) {
            log.info("::{}::联赛配置没有默认100w，config={}",rcsMatchMarketConfig.getMatchId(), JSONObject.toJSONString(rcsMatchMarketConfig));
            amount = new BigDecimal(DEFAULT_BET_MAx);
        }
        rcsMatchMarketConfig.setMaxBetAmount(amount);
    }

//    private BigDecimal getProbability(Map<String, Object> map) {
//        Integer odds = Integer.valueOf(map.get("fieldOddsValue").toString());
//        BigDecimal margin = BigDecimal.ZERO;
//        if (org.apache.commons.lang3.math.NumberUtils.INTEGER_ZERO.intValue() != odds.intValue()){
//            margin = BigDecimal.valueOf(100).multiply(BigDecimal.valueOf(BaseConstants.MULTIPLE_VALUE)).divide(BigDecimal.valueOf(odds), 2, BigDecimal.ROUND_DOWN);
//        }
//        return margin;
//    }

    private void setDefaultValue(RcsMatchMarketConfig config, RcsMatchMarketConfig rcsMatchMarketConfig) {
        rcsMatchMarketConfig.setMarketType(rcsMatchMarketConfig.getMarketType() == null ? config.getMarketType() : rcsMatchMarketConfig.getMarketType());
    }

    private void setOddsList(RcsMatchMarketConfig config, RcsMatchMarketConfig rcsMatchMarketConfig) {
        //如果有盘口值 需要设置盘口值  取实时值
//        List<StandardSportMarketOdds> oddsList = standardSportMarketOddsMapper.queryMarketInfoAndOdds(config);
        List<StandardSportMarketOdds> oddsList = tradeOddsCommonService.getMatchMarketOdds(config);
        if (org.apache.commons.collections.CollectionUtils.isEmpty(oddsList)) {
            log.error("该盘口没数据:{}", config.getMarketId());
            throw new RcsServiceException("该盘口没数据");
        }
        List<Map<String, Object>> maps = getOddsList(config, oddsList);
        if (TradeConstant.FOOTBALL_ODDS_TYPE_ORDER_NUMBER.contains(config.getPlayId().intValue())) {
            Map<String, Object>[] ms = new HashMap[3];
            for (Map<String, Object> map : maps) {
                if (TradeConstant.ODD_TYPE_0_8.equalsIgnoreCase(map.get("oddsType").toString()) ||
                        TradeConstant.ODD_TYPE_0_4.equalsIgnoreCase(map.get("oddsType").toString())) {
                    ms[0] = map;
                } else if (TradeConstant.ODD_TYPE_9_11.equalsIgnoreCase(map.get("oddsType").toString()) ||
                        TradeConstant.ODD_TYPE_5_6.equalsIgnoreCase(map.get("oddsType").toString())) {
                    ms[1] = map;
                }
                if (TradeConstant.ODD_TYPE_7.equalsIgnoreCase(map.get("oddsType").toString()) ||
                        TradeConstant.ODD_TYPE_12_OVER.equalsIgnoreCase(map.get("oddsType").toString())) {
                    ms[2] = map;
                }
            }
            maps = Arrays.asList(ms);
        }

//        if (!CollectionUtils.isEmpty(maps) &&
//                MarketKindEnum.Europe.getValue().equalsIgnoreCase(config.getMarketType())) {
//            for (Map<String, Object> map : maps) {
//                if (BaseConstants.ODD_TYPE_1.equalsIgnoreCase(map.get("oddsType").toString())) {
//                    map.put("margin", setMargin(rcsMatchMarketConfig.getHomeMargin(), rcsMatchMarketConfig.getMargin()));
//                    continue;
//                }
//                if (BaseConstants.ODD_TYPE_2.equalsIgnoreCase(map.get("oddsType").toString())) {
//                    map.put("margin", setMargin(rcsMatchMarketConfig.getAwayMargin(), rcsMatchMarketConfig.getMargin()));
//                    continue;
//                }
//                if (BaseConstants.ODD_TYPE_X.equalsIgnoreCase(map.get("oddsType").toString())) {
//                    map.put("margin", setMargin(rcsMatchMarketConfig.getTieMargin(), rcsMatchMarketConfig.getMargin()));
//                    continue;
//                }
//            }
//        }
        rcsMatchMarketConfig.setOddsList(maps);
    }

    /**
     * @return java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     * @Description //设置赔率列表
     * @Param [config, oddsList]
     * @Author Sean
     * @Date 10:03 2020/10/4
     **/
    public List<Map<String, Object>> getOddsList(RcsMatchMarketConfig config, List<StandardSportMarketOdds> oddsList) {
        List<Map<String, Object>> maps = Lists.newArrayList();
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(oddsList)) {
            for (StandardSportMarketOdds odds : oddsList) {
                Map<String, Object> map = Maps.newHashMap();
                String fieldOddsValue = "";
                String originalOddsValue = "";
                String originalMYOddsValue = "";
                // 马来盘需要转成2位小数
                if (MarketKindEnum.Malaysia.getValue().equalsIgnoreCase(config.getMarketType())) {
                    // 原始马来赔是原始赔率，原始赔率是原始香港赔
                    fieldOddsValue = rcsOddsConvertMappingService.getMyOdds(odds.getOddsValue());
                    originalOddsValue = rcsOddsConvertMappingService.getMyOdds(odds.getOriginalOddsValue());
                    originalMYOddsValue = new BigDecimal(odds.getOriginalOddsValue()).subtract(new BigDecimal(BaseConstants.MULTIPLE_VALUE)).divide(new BigDecimal(BaseConstants.MULTIPLE_VALUE), NumberUtils.INTEGER_TWO, BigDecimal.ROUND_DOWN).toPlainString();
                    map.put("originalOddsValue", originalMYOddsValue);
                    map.put("originalMYOddsValue", originalOddsValue);
                    map.put("fieldOddsValue", fieldOddsValue);
                    if (!ObjectUtils.isEmpty(odds.getMarketDiffValue()) &&
                            odds.getOddsType().equalsIgnoreCase(tradeVerificationService.getBasketBallUnderOddsType(odds.getOddsType()))) {
                        config.setAwayAutoChangeRate(odds.getMarketDiffValue().toString());
                    }
                } else if (MarketKindEnum.Europe.getValue().equalsIgnoreCase(config.getMarketType())) {
                    fieldOddsValue = new BigDecimal(odds.getOddsValue())
                            .divide(new BigDecimal(BaseConstants.MULTIPLE_VALUE), 2, BigDecimal.ROUND_DOWN).toString();
                    originalOddsValue = new BigDecimal(odds.getOriginalOddsValue())
                            .divide(new BigDecimal(BaseConstants.MULTIPLE_VALUE), 2, BigDecimal.ROUND_DOWN).toString();
                    map.put("originalOddsValue", originalOddsValue);
                    map.put("fieldOddsValue", fieldOddsValue);
//                    map.put("margin", setMargin(config, odds));
                }
//                if (!ObjectUtils.isEmpty(odds.getMarketDiffValue()) &&
//                        odds.getOddsType().equalsIgnoreCase(tradeVerificationService.getBasketBallUnderOddsType(odds.getOddsType()))) {
//                    config.setAwayAutoChangeRate(odds.getMarketDiffValue().toString());
//                }
                map.put("margin", odds.getMargin());
                if (!ObjectUtils.isEmpty(config.getMargin())) {
                    map.put("margin", config.getMargin());
                }
                map.put("probability", odds.getProbability());
                map.put("probabilityOdds", ObjectUtils.isEmpty(odds.getProbabilityOdds()) ? 0 : odds.getProbabilityOdds().divide(new BigDecimal(BaseConstants.MULTIPLE_VALUE), NumberUtils.INTEGER_TWO, BigDecimal.ROUND_DOWN));
                map.put("anchor", odds.getAnchor());
                map.put("marketDiffValue", odds.getMarketDiffValue());
                map.put("marginProbabilityOdds", ObjectUtils.isEmpty(odds.getMarginProbabilityOdds()) ? 0 : odds.getMarginProbabilityOdds().divide(new BigDecimal(BaseConstants.MULTIPLE_VALUE), NumberUtils.INTEGER_TWO, BigDecimal.ROUND_DOWN));
                map.put("active", odds.getActive());
                map.put("dataSourceCode", odds.getDataSourceCode());
                map.put("nameExpressionValue", ObjectUtils.isEmpty(odds.getNameExpressionValue()) ? NumberUtils.INTEGER_ZERO.toString() : odds.getNameExpressionValue());
                map.put("oddsType", odds.getOddsType());
                maps.add(map);
            }
        }
        return maps;
    }

    /**
     * @return java.math.BigDecimal
     * @Description //设置margin
     * @Param [config, odds]
     * @Author Sean
     * @Date 10:02 2020/10/4
     **/
    private BigDecimal setMargin(RcsMatchMarketConfig config, StandardSportMarketOdds odds) {
        BigDecimal margin = null;
        if (BaseConstants.ODD_TYPE_1.equalsIgnoreCase(odds.getOddsType())) {
            margin = config.getHomeMargin();
        } else if (BaseConstants.ODD_TYPE_2.equalsIgnoreCase(odds.getOddsType())) {
            margin = config.getAwayMargin();
        } else if (BaseConstants.ODD_TYPE_X.equalsIgnoreCase(odds.getOddsType())) {
            margin = config.getTieMargin();
        }
        if (ObjectUtils.isEmpty(margin)) {
            margin = config.getMargin();
        }
        return margin;
    }

    private void reSetNameExpressionValue(Map<String, Object> map) {
        if (map.containsKey("nameExpressionValue")) {
            map.put("nameExpressionValue", NameExpressionValueUtils.getNumberParseToText(String.valueOf(map.get("nameExpressionValue"))));
        }
    }

    private BigDecimal setMargin(BigDecimal margin, BigDecimal defaultMargin) {
        if (ObjectUtils.isEmpty(defaultMargin)) {
            defaultMargin = new BigDecimal(EU_DEFAULT_MARGIN);
        }
        return ObjectUtils.isEmpty(margin) ? defaultMargin : margin;
    }

    /**
     * @return java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     * @Description //获取盘口赔率
     * @Param [config, odds]
     * @Author sean
     * @Date 2021/7/28
     **/
    public List<Map<String, Object>> getOddsList(List<StandardMarketOddsDTO> odds, RcsMatchMarketConfig config) {
        List<StandardSportMarketOdds> oddsList = JSONArray.parseArray(JSONArray.toJSONString(odds), StandardSportMarketOdds.class);

        List<Map<String, Object>> maps = getOddsList(config, oddsList);
        config.setMargin(ObjectUtils.isEmpty(config.getMargin()) ? MarginUtils.convert(maps, config.getMarketType(), null) : config.getMargin());
        if (MarketKindEnum.Europe.getValue().equalsIgnoreCase(config.getMarketType())) {
            config.setMargin(config.getMargin().setScale(0, BigDecimal.ROUND_DOWN));
        }
        return maps;
    }
}
