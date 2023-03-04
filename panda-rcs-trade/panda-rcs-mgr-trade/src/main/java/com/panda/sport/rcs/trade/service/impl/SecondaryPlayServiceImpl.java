package com.panda.sport.rcs.trade.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.panda.sport.rcs.cache.redis.RedisUtils;
import com.panda.sport.rcs.common.DateUtils;
import com.panda.sport.rcs.constants.Placeholder;
import com.panda.sport.rcs.constants.RcsConstant;
import com.panda.sport.rcs.constants.RedisKey;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.core.utils.BeanCopyUtils;
import com.panda.sport.rcs.enums.*;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.factory.BeanFactory;
import com.panda.sport.rcs.mapper.StandardSportMarketCategoryMapper;
import com.panda.sport.rcs.mongo.*;
import com.panda.sport.rcs.pojo.StandardSportMarketCategory;
import com.panda.sport.rcs.pojo.statistics.RcsPredictBetOdds;
import com.panda.sport.rcs.trade.enums.CategoryShowEnum;
import com.panda.sport.rcs.trade.service.MongoDbService;
import com.panda.sport.rcs.trade.service.SecondaryPlayService;
import com.panda.sport.rcs.trade.service.TradeStatusService;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.*;
import com.panda.sport.rcs.trade.wrapper.tourTemplate.IRcsTournamentTemplatePlayMargainService;
import com.panda.sport.rcs.utils.ListUtils;
import com.panda.sport.rcs.utils.OddsConvertUtils;
import com.panda.sport.rcs.utils.PlayTemplateUtils;
import com.panda.sport.rcs.vo.CategoryTemplateVo;
import com.panda.sport.rcs.vo.secondary.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.panda.sport.rcs.constants.RedisKey.EXPRIY_TIME_2_HOURS;
import static com.panda.sport.rcs.constants.RedisKey.RCS_BASKETBALL_TIME;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 次要玩法
 * @Author : Paca
 * @Date : 2021-02-19 10:42
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Slf4j
@Service
public class SecondaryPlayServiceImpl implements SecondaryPlayService {

    @Autowired
    private RedisClient redisClient;
    @Autowired
    private MongoDbService mongoDbService;
    @Autowired
    private MarketCategorySetService marketCategorySetService;
    @Autowired
    private RcsPredictBetOddsService predictBetOddsService;
    @Autowired
    private RcsOddsConvertMappingService mappingService;
    @Autowired
    private RcsTradeConfigService tradeConfigService;
    @Autowired
    private RcsLanguageInternationService rcsLanguageInternationService;
    @Autowired
    private MatchStatisticsInfoDetailService detailService;
    @Autowired
    private IRcsTournamentTemplatePlayMargainService templatePlayMargainService;
    @Autowired
    private StandardSportMarketCategoryService standardSportMarketCategoryService;
    @Autowired
    private TradeStatusService tradeStatusService;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private StandardSportMarketCategoryMapper standardSportMarketCategoryMapper;

    @Override
    public List<BasketballTwoPlaySet> basketballTwoPlaySet(String lang) {
        List<BasketballTwoPlaySet> result = new ArrayList<>(20);
        int i = 1;
        List<Basketball.TwoItemPlaySet> queryList = Basketball.TwoItemPlaySet.queryList();
        for (Basketball.TwoItemPlaySet twoItemPlaySet : queryList) {
            BasketballTwoPlaySet playSet = new BasketballTwoPlaySet(twoItemPlaySet);
            playSet.setQueryFlag(YesNoEnum.Y.getValue());
            playSet.setSortNo(i++);
            if(lang.equals("en")){
                playSet.setPlaySetName(playSet.getEnName());
            }
            result.add(playSet);
        }
        for (Basketball.TwoItemPlaySet twoItemPlaySet : Basketball.TwoItemPlaySet.values()) {
            if (queryList.contains(twoItemPlaySet)) {
                continue;
            }
            BasketballTwoPlaySet playSet = new BasketballTwoPlaySet(twoItemPlaySet);
            playSet.setQueryFlag(YesNoEnum.N.getValue());
            playSet.setSortNo(i++);
            if(lang.equals("en")){
                playSet.setPlaySetName(playSet.getEnName());
            }
            result.add(playSet);
        }
        return result;
    }

    @Override
    public List<List<BasketballTwoPlaySetInfo>> basketballTwoList(BasketballTwoReqVo reqVo) {
        Long matchId = reqVo.getMatchId();
        Collection<Long> playIds = reqVo.getPlayIds();
        Collection<Long> subPlaySetIds = reqVo.getSubPlaySetIds();
        // 筛选status = 1 的玩法
        playIds = getOpenIds(new ArrayList<>(playIds), reqVo.getSportId(), 1);
        Map<Long, BasketballTwoPlaySetInfo> playSetInfoMap = getPlaySetInfoMap(matchId, playIds);
        List<List<BasketballTwoPlaySetInfo>> result = Lists.newArrayList();
        for (List<Basketball.TwoItemPlaySet> twoItemPlaySetList : Basketball.TwoItemPlaySet.group()) {
            List<BasketballTwoPlaySetInfo> playSetInfoList = new ArrayList<>(twoItemPlaySetList.size());
            for (Basketball.TwoItemPlaySet twoItemPlaySet : twoItemPlaySetList) {
                if (!subPlaySetIds.contains(twoItemPlaySet.getId())) {
                    continue;
                }
                if (playSetInfoMap.containsKey(twoItemPlaySet.getId())) {
                    BasketballTwoPlaySetInfo playSetInfo = playSetInfoMap.get(twoItemPlaySet.getId());
                    List<Long> playIdList = twoItemPlaySet.getPlayIds();
                    // 排序
                    playSetInfo.getPlayInfoList().forEach(playInfo -> {
                        for (int i = 0; i < playIdList.size(); i++) {
                            if (playInfo.getId().equals(playIdList.get(i))) {
                                playInfo.setOrderNo(i + 1);
                            }
                        }
                    });
                    List<MarketCategory> playInfoList = playSetInfo.getPlayInfoList().stream().sorted(Comparator.comparingInt(MarketCategory::getOrderNo)).collect(Collectors.toList());
                    playSetInfo.setPlayInfoList(playInfoList);
                    playSetInfoList.add(playSetInfo);
                } else {
                    playSetInfoList.add(new BasketballTwoPlaySetInfo(twoItemPlaySet));
                }
            }
            if (CollectionUtils.isNotEmpty(playSetInfoList)) {
                result.add(playSetInfoList);
            }
        }
        return result;
    }


    private Map<Long, BasketballTwoPlaySetInfo> getPlaySetInfoMap(Long matchId, Collection<Long> playIds) {
        List<MarketCategory> playInfoList = mongoDbService.getPlayInfoList(matchId, playIds, false);
        List<Long> existPlayIds = null;
        if (CollectionUtils.isNotEmpty(playInfoList)) {
            existPlayIds = playInfoList.stream().map(MarketCategory::getId).collect(Collectors.toList());
        }
        if (CollectionUtils.isNotEmpty(existPlayIds)) {
            playIds.removeAll(existPlayIds);
        }
        playIds.forEach(playId -> {
            if (Lists.newArrayList(41L, 201L, 214L, 215L, 147L, 2L, 10L, 11L, 15L).contains(playId)) {
                return;
            }
            MarketCategory playInfo = new MarketCategory();
            playInfo.setId(playId);
            playInfo.setSportId(SportIdEnum.BASKETBALL.getId());
            playInfo.setMatchId(matchId.toString());
            playInfo.setTradeType(TradeEnum.AUTO.getCode());
            playInfo.setNames(rcsLanguageInternationService.getPlayLanguage(SportIdEnum.BASKETBALL.getId(), playId));
            if (Basketball.Main.getWinAlonePlayIds().contains(playId)) {
                playInfo.setMarketType(MarketKindEnum.Europe.getValue());
            } else {
                playInfo.setMarketType(MarketKindEnum.Malaysia.getValue());
            }
            playInfoList.add(playInfo);
        });
        Map<Long, BasketballTwoPlaySetInfo> map = Maps.newHashMap();
        MatchMarketLiveBean matchInfo = mongoDbService.getMatchInfo(matchId,null);
        Map<String, I18nBean> teamMap = mongoDbService.getTeamMap(matchInfo);
        playInfoList.forEach(playInfo -> {
            Long playId = playInfo.getId();
            if (Lists.newArrayList(145L, 146L).contains(playId)) {
                playInfo.setRelevanceTypeMap(getRelevanceTypeMap(matchId, playId));
            }
            playInfo.setShowFlag(YesNoEnum.convert(PlayTemplateUtils.isShowMarketName(playId)));
            playInfo.setIsNewMarket(YesNoEnum.convert(Basketball.isNewMarket(playId)));
            Basketball.TwoItemPlaySet twoItemPlaySet = Basketball.TwoItemPlaySet.getGroupByPlayId(playId);
            if (twoItemPlaySet == null) {
                return;
            }
            // 根据玩法id 赛种id 玩法国际化
            playInfo.setNames(rcsLanguageInternationService.getCategoryLanguage(playInfo.getId(), playInfo.getSportId()));
            PlayTemplateUtils.handlePlayName(playInfo, teamMap);
            if (map.containsKey(twoItemPlaySet.getId())) {
                map.get(twoItemPlaySet.getId()).getPlayInfoList().add(playInfo);
            } else {
                BasketballTwoPlaySetInfo playSetInfo = new BasketballTwoPlaySetInfo(twoItemPlaySet);
                List<MarketCategory> playInfos = Lists.newArrayList();
                playInfos.add(playInfo);
                playSetInfo.setPlayInfoList(playInfos);
                map.put(twoItemPlaySet.getId(), playSetInfo);
            }

            List<MatchMarketVo> marketList = playInfo.getMatchMarketVoList();
            if (CollectionUtils.isEmpty(marketList)) {
                return;
            }
            marketList.forEach(market -> {
                PlayTemplateUtils.handleMarketName(market, teamMap);
                // 篮球220L, 221L, 271L, 272L 四个玩法球员名做特殊处理
                if (Basketball.Secondary.PLAYER.getPlayIds().contains(market.getMarketCategoryId())) {
                	String playerLanguageStr = rcsLanguageInternationService.getPlayerLanguageStr(market.getAddition3());
                	market.setAddition3(playerLanguageStr);
                	market.setOddsName(playerLanguageStr);
                }
                
                List<MatchMarketOddsVo> marketOddsList = market.getOddsFieldsList();
                if (CollectionUtils.isEmpty(marketOddsList)) {
                    return;
                }
                marketOddsList.forEach(marketOdds -> {
                    // 赔率原始值
                    marketOdds.setFieldOddsOriginValue(Double.valueOf(marketOdds.getFieldOddsValue()).intValue());
                    // 赔率显示值
                    marketOdds.setFieldOddsValue(OddsConvertUtils.convertAndDefaultDisplay(MarketKindEnum.Europe, marketOdds.getFieldOddsOriginValue()));
                });
                // 投注项排序
                List<MatchMarketOddsVo> sortedMarketOddsList = marketOddsList.stream().sorted(Comparator.comparingInt(MatchMarketOddsVo::getOrderOdds)).collect(Collectors.toList());
                market.setOddsFieldsList(sortedMarketOddsList);
            });

            //判断次玩法
            List<MatchMarketVo> childMarketVoList = marketList.stream().filter(fi -> StringUtils.isNotBlank(fi.getChildMarketCategoryId())
                    && (!String.valueOf(fi.getMarketCategoryId()).equals(fi.getChildMarketCategoryId()))).collect(Collectors.toList());
            playInfo.setIsChildCategory(CollectionUtils.isNotEmpty(childMarketVoList));
        });
        return map;
    }

    private Map<Long, Integer> getRelevanceTypeMap(Long matchId, Long playId) {
        String key = RedisKey.getRelevanceTypeKey(matchId, playId);
        Map<String, String> map = redisUtils.hgetAll(key);
        if (CollectionUtils.isEmpty(map)) {
            return Maps.newHashMap();
        }
        Map<Long, Integer> resultMap = Maps.newHashMap();
        map.forEach((k, v) -> resultMap.put(NumberUtils.toLong(k), NumberUtils.toInt(v)));
        return resultMap;
    }

    @Override
    public List<FootballTwoPlaySetInfo> footballTwoPlaySet(FootballTwoReqVo reqVo) {
        List<FootballTwoPlaySetInfo> infos = new ArrayList<>();
        Long matchId = reqVo.getMatchId();
        Integer oddBusiness = reqVo.getLiveOddBusiness();
        Long sportId = reqVo.getSportId();
        List<Long> categorySetIds = reqVo.getCategorySetIds();
        MatchMarketLiveBean matchInfo = mongoDbService.getMatchInfo(matchId,null);
        Integer dataType = 1;
        if (SportIdEnum.isFootball(sportId) || SportIdEnum.isBasketball(sportId) || SportIdEnum.isTennis(sportId) || SportIdEnum.isSnooker(sportId) || SportIdEnum.isBaseBall(sportId) || SportIdEnum.isIceHockey(sportId))
            dataType = 2;
        Map<Long, Map<String, List<RcsPredictBetOdds>>> matchBetMap = predictBetOddsService.queryBetOdds(Arrays.asList(matchId), dataType, reqVo.getSeriesType());
        //查询数据源
        Map<String, String> dataSourceMap = templatePlayMargainService.queryDataSource(Arrays.asList(matchId));
        // 球队信息 国际化
        Map<String, I18nBean> teamMap = Maps.newHashMap();
        if (matchInfo != null && CollectionUtils.isNotEmpty(matchInfo.getTeamList())) {
            for (MatchTeamVo team : matchInfo.getTeamList()) {
                String position = team.getMatchPosition();
                Map<String, String> names = team.getNames();
                if (StringUtils.isNotBlank(position) && CollectionUtils.isNotEmpty(names)) {
                    teamMap.put(position.toLowerCase(), new I18nBean(names));
                }
            }
        }
        Map<String, Integer> clientShowMap = tradeConfigService.queryCategoryShow(Arrays.asList(matchId),oddBusiness==null?0:oddBusiness);
        if (CollectionUtils.isNotEmpty(categorySetIds)) {
            for (Long categorySetId : categorySetIds) {
                FootballTwoPlaySetInfo info = transferCategories(categorySetId, reqVo, matchInfo, teamMap, dataSourceMap);

                if(!CollectionUtils.isEmpty(clientShowMap)) {
                    Integer clientShow = clientShowMap.get(matchId + "_" + CategoryShowEnum.querySendId(categorySetId));
                    info.setClientShow(clientShow == null ? 1 : clientShow);
                }
                // 晋级/冠军，原有数据/如果为空 默认关
                Integer clientShow = info.getClientShow();
                if (categorySetId.equals(10021L) && (clientShow == null || clientShow == 1)) {
                	info.setClientShow(0);
                }
                if (!CollectionUtils.isEmpty(matchBetMap)&&!CollectionUtils.isEmpty(matchBetMap.get(matchId))) {
                    if (SportIdEnum.isFootball(sportId)) {
                        List<Long> categoryIds = Football.CategorySet.getCategoryIdsBySetId(Arrays.asList(categorySetId));
                        info.setBetMap(Maps.filterKeys(matchBetMap.get(matchId), m -> categoryIds.contains(Long.parseLong(m.split("_")[0]))));
                    } else if (SportIdEnum.isTennis(sportId)) {
                        List<Long> categoryIds = Tennis.CategorySet.getCategoryIdsBySetId(categorySetId);
                        info.setBetMap(Maps.filterKeys(matchBetMap.get(matchId), m -> categoryIds.contains(Long.parseLong(m.split("_")[0]))));
                    }else if(SportIdEnum.isSnooker(sportId)){
                        List<Long> categoryIds = Snooker.CategorySet.getCategoryIdsBySetId(categorySetId);
                        info.setBetMap(Maps.filterKeys(matchBetMap.get(matchId), m -> categoryIds.contains(Long.parseLong(m.split("_")[0]))));
                    }else if (SportIdEnum.isBaseBall(sportId)) {
                        List<Long> categoryIds = Baseball.CategorySet.getCategoryIdsBySetId(categorySetId);
                        info.setBetMap(Maps.filterKeys(matchBetMap.get(matchId), m -> categoryIds.contains(Long.parseLong(m.split("_")[0]))));
                    } else if (SportIdEnum.isIceHockey(sportId)) {
                        List<Long> categoryIds = IceHockey.CategorySet.getCategoryIdsBySetId(categorySetId);
                        info.setBetMap(Maps.filterKeys(matchBetMap.get(matchId), m -> categoryIds.contains(Long.parseLong(m.split("_")[0]))));
                    }
                }
                infos.add(info);
            }
        }

        ListUtils.sort(infos, true, "playSetId");
        return infos;
    }
    

    /**
     * 	查询status = 1的玩法 
     *  
     * @param ids 玩法id
     * @param sportId 赛种id
     * @return
     */
    private List<Long> getOpenIds(List<Long> ids, Long sportId, Integer status) {
    	List<StandardSportMarketCategory> queryCategoryInfoByIds = standardSportMarketCategoryMapper.queryCategoryInfoByIds(ids, sportId, status);
		return queryCategoryInfoByIds.stream().map(StandardSportMarketCategory :: getId).collect(Collectors.toList());
	}

    FootballTwoPlaySetInfo transferCategories(Long categorySetId, FootballTwoReqVo reqVo, MatchMarketLiveBean matchInfo, Map<String, I18nBean> teamMap, Map<String, String> dataSourceMap) {
        Long matchId = reqVo.getMatchId();
        Long sportId = reqVo.getSportId();
        List<Long> ids = new ArrayList<>();
        FootballTwoPlaySetInfo info = new FootballTwoPlaySetInfo();
        if(SportIdEnum.isFootball(sportId)){
            ids= Football.CategorySet.getCategoryIdsBySetId(Arrays.asList(categorySetId));
            log.info("::{}::[玩法集配置]玩法集={},配置的玩法ID={}", CommonUtil.getRequestId(matchInfo.getMatchId(), categorySetId),categorySetId,ids);
            if(Football.CategorySet.PENALTY_CARD.getCategorySetId().equals(categorySetId)){
                info.setScore(matchInfo.getCardScore());
                info.setScore1(matchInfo.getRedCardScore());
                info.setScore2(matchInfo.getYellowCardScore());
            }
        }else if(SportIdEnum.isTennis(sportId)){
            ids =Tennis.CategorySet.getCategoryIdsBySetId(categorySetId);
        }else if(SportIdEnum.isIceHockey(sportId)){
            ids = IceHockey.CategorySet.getCategoryIdsBySetId(categorySetId);
        }else if(SportIdEnum.isSnooker(sportId)){
            ids =Snooker.CategorySet.getCategoryIdsBySetId(categorySetId);
        }else if(SportIdEnum.isBaseBall(sportId)){
            ids=Baseball.CategorySet.getCategoryIdsBySetId(categorySetId);
        }
        // 38623优化 不筛选玩法状态 只校验是否存在
        ids = getOpenIds(ids, sportId, null);
        if(CollectionUtils.isEmpty(ids)){
            throw new RcsServiceException("玩法集" + categorySetId + "下玩法未同步");
        }
        if (SportIdEnum.isFootball(sportId)) {
        	info.setPlaySetCodeStatusMap(tradeStatusService.getPlaySetCodeStatus(sportId, matchId, ids));
        }

        info.setPlaySetId(categorySetId);
        info.setPlayIds(ids);

        info.setScore(getScore(matchInfo, categorySetId));
        //数据源
        if (!org.springframework.util.CollectionUtils.isEmpty(dataSourceMap)) {
            List<Long> categoryIs =ids;
            Map<Long, String> categoryDataSourceMap = dataSourceMap.entrySet().stream().filter(map -> map.getKey().split("_")[0].equals(String.valueOf(matchId))
                    && categoryIs.contains(Long.parseLong(map.getKey().split("_")[1]))
            ).collect(Collectors.toMap(p -> Long.parseLong(p.getKey().split("_")[1]), p -> p.getValue()));
            info.setDataSourceMap(categoryDataSourceMap);
        }
        List<MarketCategory> categoryList = mongoDbService.getPlayInfoList(matchId, ids, true);

        if (CollectionUtils.isNotEmpty(categoryList)) {
            categoryList.stream().forEach(ca -> {
                Long caId = ca.getId();
                if(SportIdEnum.isFootball(sportId)){
                    Football.CategorySet categorySet = Football.CategorySet.getCategorySetId(caId);
                    ca.setColNo(Football.CategorySet.getColNo(caId));
                    ca.setCategorySetId(categorySet.getCategorySetId());
                }else if(SportIdEnum.isTennis(sportId)){
                    Tennis.CategorySet tennisSetId = Tennis.CategorySet.getCategorySetId(caId);
                    ca.setColNo(Tennis.CategorySet.getColNo(caId));
                    ca.setCategorySetId(tennisSetId.getCategorySetId());
                }else if(SportIdEnum.isIceHockey(sportId)){
                    ca.setColNo(IceHockey.CategorySet.getColNo(caId));
                }else if(SportIdEnum.isSnooker(sportId)){
                    ca.setColNo(Snooker.CategorySet.getColNo(caId));
                }else if(SportIdEnum.isBaseBall(sportId)){
                    ca.setColNo(Baseball.CategorySet.getColNo(caId));
                }

            });

            // 玩法模板信息，key=categoryId
            Map<Long, CategoryTemplateVo> categoryTemplateMap = standardSportMarketCategoryService.getCategoryTemplateByCache(sportId, ids);
            List<MarketCategory> tradeCategories =new ArrayList<>();
            categoryList.stream().forEach(category -> {
                MarketCategory marketCategory = BeanCopyUtils.copyProperties(category, MarketCategory.class);
                // 获取模板ID
                Long categoryId = marketCategory.getId();
                Integer templateId = getTemplateId(matchId, categoryId, categoryTemplateMap);
                marketCategory.setTemplateId(templateId);
                marketCategory.setIsIrregular(CategoryTemplateEnum.isIrregular(categoryId, templateId) ? 1 : 0);
                marketCategory.setShowFlag(PlayTemplateUtils.isShowMarketName(categoryId) ? 1 : 0);
                List<MatchMarketVo> marketVos = marketCategory.getMatchMarketVoList();
                MarketKindEnum marketKind = MarketKindEnum.getMarketKindByValue(marketCategory.getMarketType());
                if (CollectionUtils.isNotEmpty(marketVos)) {
                    for (MatchMarketVo market : marketVos) {
                        //盘口名称占位符替换
                        I18nBean i18nBean = rcsLanguageInternationService.getCategoryLanguage(marketCategory.getId(), marketCategory.getSportId());
                        if(null != i18nBean){
                            market.setNames(i18nBean);
                        }
                        PlayTemplateUtils.handleMarketName(market, teamMap);
                        
                        // 篮球220L, 221L, 271L, 272L 四个玩法球员名做特殊处理
                        if (Basketball.Secondary.PLAYER.getPlayIds().contains(market.getMarketCategoryId())) {
                        	String playerLanguageStr = rcsLanguageInternationService.getPlayerLanguageStr(market.getAddition3());
                        	market.setAddition3(playerLanguageStr);
                        	market.setOddsName(playerLanguageStr);
                        }
                        
                        //根据玩法集 确定第X局
                        if (SportIdEnum.isTennis(sportId) || SportIdEnum.isSnooker(sportId) ||SportIdEnum.isBaseBall(sportId) || SportIdEnum.isIceHockey(sportId)) {
                             otherSetId(sportId, market,category);
                        }
                        if (SportIdEnum.isTennis(sportId) && Arrays.asList(208L, 166L, 167L).contains(marketCategory.getId()) && !categorySetId.equals(market.getSetId())
                        || SportIdEnum.isSnooker(sportId) && Snooker.CategorySet.getCategoryIdsBySetId(70102L).contains(marketCategory.getId()) && !categorySetId.equals(market.getSetId()))
                            continue;
                        List<MatchMarketOddsVo> marketOddsList = market.getOddsFieldsList();
                        if (CollectionUtils.isNotEmpty(marketOddsList)) {

                            for (MatchMarketOddsVo marketOdds : marketOddsList) {
                                // 赔率原始值
                                marketOdds.setFieldOddsOriginValue(Double.valueOf(marketOdds.getFieldOddsValue()).intValue());
                                // 赔率显示值
                                marketOdds.setFieldOddsValue(OddsConvertUtils.convertAndDefaultDisplay(MarketKindEnum.Europe, marketOdds.getFieldOddsOriginValue()));
                                if (MarketKindEnum.Malaysia.getValue().equals(marketCategory.getMarketType())) {
                                    String myOdds = mappingService.getMyOdds(marketOdds.getFieldOddsValue());
                                    if ("0".equals(myOdds))
                                        myOdds = mappingService.getOddsValue(marketOdds.getFieldOddsValue(), marketKind);
                                    marketOdds.setFieldOddsValue(myOdds);
                                }
                                //当特殊玩法时，查询球员库保持一致性。
                                if(RcsConstant.GOALSCORER.contains(categoryId)){
                                    handleSpecialMarketOdds(marketOdds);
                                }else {
                                    PlayTemplateUtils.handleMarketOdds(categoryId, templateId, marketOdds, teamMap);
                                }
                            }
                            // 投注项排序
                            List<MatchMarketOddsVo> sortedMarketOddsList = marketOddsList.stream().sorted(Comparator.comparingInt(MatchMarketOddsVo::getOrderOdds)).collect(Collectors.toList());
                            market.setOddsFieldsList(sortedMarketOddsList);
                        }
                        // 计算margin值
                        market.setMarginValue(OddsConvertUtils.calMarginByOddsList(market.getOddsFieldsList(), marketKind));

                    }
                    //判断次玩法
                    List<MatchMarketVo> childMarketVoList = marketVos.stream().filter(fi -> StringUtils.isNotBlank(fi.getChildMarketCategoryId())
                            && (!String.valueOf(fi.getMarketCategoryId()).equals(fi.getChildMarketCategoryId()))).collect(Collectors.toList());
                    marketCategory.setIsChildCategory(CollectionUtils.isNotEmpty(childMarketVoList));
                    //第X局过滤，不对应的局数忽略掉
                    if ((SportIdEnum.isTennis(sportId) && !Tennis.CategorySet.FULL_TIME.getCategorySetId().equals(categorySetId)
                            && !Tennis.CategorySet.SPECIAL_TYEP.getCategorySetId().equals(categorySetId))
                            || (SportIdEnum.isSnooker(sportId) && !Snooker.CategorySet.FULL_TIME.getCategorySetId().equals(categorySetId))
                            || (SportIdEnum.isIceHockey(sportId) && !IceHockey.CategorySet.FULL_TIME.getCategorySetId().equals(categorySetId))
                            ||(SportIdEnum.isBaseBall(sportId) && Baseball.CategorySet.SET_ANYTIME.getCategorySetId()<=(categorySetId))) {
                        marketVos = marketVos.stream().filter(fi -> fi.getSetId() != null && fi.getSetId().equals(categorySetId)).collect(Collectors.toList());
                        marketCategory.setMatchMarketVoList(marketVos);
                    }
                    if(Arrays.asList(33L,232L).contains(categoryId)){
                        Map<String, String> soreMap = detailService.fifteenSoreMap(matchId, categoryId);
                        marketCategory.setScoreMap(soreMap);
                    }
                }

                if (CategoryTemplateEnum.isGroupByColumn(templateId)) {
                    // 单盘口按列分组 BG新球员玩法分组
                    marketCategorySetService.groupByColumn(marketCategory, teamMap);
                } else if (CategoryTemplateEnum.isSingleGroupByColAndRow(categoryId, templateId)) {
                    // 单盘口按列和行分组
                    marketCategorySetService.singleGroupByColAndRow(marketCategory, teamMap);
                } else if (CategoryTemplateEnum.isMultiGroupByColAndRow(categoryId, templateId)) {
                    // 多盘口按列和行分组
                    marketCategorySetService.multiGroupByColAndRow(marketCategory, teamMap);
                }

                // 根据玩法id 赛种id 玩法国际化
            	marketCategory.setNames(rcsLanguageInternationService.getCategoryLanguage(marketCategory.getId(), marketCategory.getSportId()));
                // 占位符替换
                PlayTemplateUtils.handlePlayName(marketCategory, teamMap);

                tradeCategories.add(marketCategory);
            });
            ListUtils.sort(tradeCategories, true, "rowNo", "colNo");
            info.setPlayInfoList(tradeCategories);
        }

        return info;
    }


    private static final Pattern pattern=Pattern.compile("[0-9]*");

    private void handleSpecialMarketOdds(MatchMarketOddsVo marketOdds){
        String oddsType = marketOdds.getOddsType();
        if (OddsTypeEnum.OTHER.equalsIgnoreCase(oddsType)) {
            marketOdds.setNames(BeanFactory.getOtherI18n());
        } else if (OddsTypeEnum.NONE.equalsIgnoreCase(oddsType)) {
            marketOdds.setNames(BeanFactory.getNoneI18n());
        } else if (OddsTypeEnum.OWN_GOAL.equalsIgnoreCase(oddsType)) {
            marketOdds.setNames(BeanFactory.getOwnGoalI18n());
        } else {
            String namecode=marketOdds.getOddsType();
            if(pattern.matcher(namecode).matches()){
                marketOdds.setNames(rcsLanguageInternationService.getPlayerLanguage(namecode));
            }else {
                marketOdds.setNames(new I18nBean(marketOdds.getName()));
            };
        }
        String addition2 = marketOdds.getAddition2();
        if (OddsTypeEnum.HOME.equalsIgnoreCase(addition2)) {
            marketOddsSetValue(marketOdds, 1, Placeholder.HOME);
        } else if (OddsTypeEnum.AWAY.equalsIgnoreCase(addition2)) {
            marketOddsSetValue(marketOdds, 2, Placeholder.AWAY);
        } else {
            marketOddsSetValue(marketOdds, 3, Placeholder.OTHER);
        }
    }

    void marketOddsSetValue(MatchMarketOddsVo marketOdds, int groupId, String titleName) {
        marketOdds.setGroupId(groupId);
        marketOdds.setSortNo(marketOdds.getOrderOdds());
        marketOdds.setTitleName(titleName);
    }

    private Integer getTemplateId(final Long matchId, final Long categoryId, final Map<Long, CategoryTemplateVo> categoryTemplateMap) {
        Integer templateId = 0;
        try {
            if (Basketball.Secondary.PLAYER.getPlayIds().contains(categoryId)) {
                return 5;
            }
            // 查询模板ID，默认为0
            CategoryTemplateVo template = categoryTemplateMap.get(categoryId);

            if (templateId == null) {
                log.warn("::{}::玩法未配置模板：categoryId={}", matchId, categoryId);
                templateId = 0;
            }else {
                templateId = template.getTemplateId();
            }
        }catch (Exception e){
            log.warn("::{}::玩法未配置模板：categoryId={}", matchId, categoryId);
            log.error("::{}::模板数据错误{}",CommonUtil.getRequestId(),e.getMessage(),e);
        }

        return templateId;
    }

    Integer matchTime(MatchMarketLiveBean match, Integer oddBusiness) {
        Integer result = 0;
        //赛事时间过后，则显示即将开赛
        Long beginTime = DateUtils.tranferStringToDate(match.getMatchStartTime()).getTime() - System.currentTimeMillis();

        //判断赛前十五分钟
        if (beginTime > 0 && null != oddBusiness && 1 == oddBusiness && match.getMatchStatus() == 0) {
            result = (int) (beginTime / 1000);
        } else if (oddBusiness == 0) {
            result = 0;
        } else {
            Integer secondsMatchStart = match.getSecondsMatchStart();
            Long eventTime = match.getEventTime() == null ? 0 : match.getEventTime();
            Long time = eventTime > 0 ? (System.currentTimeMillis() - eventTime) / 1000 : 0;

            Integer secondsTime = match.getSecondsMatchStart() + time.intValue();
            log.info("::{}::赛事时间" + match.getMatchId() + "当前时间:" + DateUtils.transferLongToDateStrings(System.currentTimeMillis())
                    + "事件编码:" + match.getEventCode() + "事件时间:" + DateUtils.transferLongToDateStrings(eventTime) +
                    "比赛进行时间:" + match.getSecondsMatchStart() + "结果:" + secondsTime,CommonUtil.getRequestId());

            match.setSecondsMatchStart(secondsTime > 0 ? secondsTime : 0);

            if (StringUtils.isNotBlank(match.getEventCode()) && (match.getEventCode().equals("timeout"))) {
                match.setSecondsMatchStart(secondsMatchStart);
            }

            if (Arrays.asList(1, 2, 10).contains(match.getMatchStatus()) && StringUtils.isNotBlank(match.getEventCode())) {
                String key = String.format(RCS_BASKETBALL_TIME, match.getMatchId(), match.getPeriod());
                if (StringUtils.isNotBlank(redisClient.get(key))) {
                    int redisTime = Integer.parseInt(redisClient.get(key));
                    if (match.getSecondsMatchStart() < redisTime) {
                        match.setSecondsMatchStart(redisTime);
                    }
                }
                redisClient.setExpiry(key, match.getSecondsMatchStart(), EXPRIY_TIME_2_HOURS);
                log.info("::{}::赛事时间" + match.getMatchId() + "secondsMatchStart:" + match.getSecondsMatchStart(),CommonUtil.getRequestId());
            }

        }
        return result;
    }

    String getScore(MatchMarketLiveBean match, Long categorySetId) {
        String score = "";
        if(SportIdEnum.isIceHockey(match.getSportId())){
            score = getIceHocSetScore(match,categorySetId);
        }else{
            Long matchId = match.getMatchId();
            if(Arrays.asList(10014L,10015L,10016L,10021L,10023L).contains(categorySetId))categorySetId= Football.CategorySet.FULL_TIME.getCategorySetId();
            String key = RedisKey.getFootballScore(matchId, categorySetId);
            score = redisClient.get(key);
        }

        return score;
    }

    /**
     * 冰球比分
     * @param matchInfo
     * @param categorySetId
     * @return
     */
    private String getIceHocSetScore( MatchMarketLiveBean matchInfo,Long categorySetId) {
        try {
            Map<String, List<ScoreVo>> collect = matchInfo.getScoreVos().stream().collect(Collectors.groupingBy(e -> String.valueOf(e.getPeriod())));
            if(IceHockey.CategorySet.FULL_TIME.getCategorySetId().equals(categorySetId)){
                return matchInfo.getScore();
            }
            if(IceHockey.CategorySet.SET_ONE.getCategorySetId().equals(categorySetId)){
                if(null!=collect.get("1")){
                    return collect.get("1").get(0).getSetScore();
                }
            }
            if(IceHockey.CategorySet.SET_TWO.getCategorySetId().equals(categorySetId)){
                if(null!=collect.get("2")){
                    return collect.get("2").get(0).getSetScore();
                }            }
            if(IceHockey.CategorySet.SET_THREE.getCategorySetId().equals(categorySetId)){
                if(null!=collect.get("3")){
                    return collect.get("3").get(0).getSetScore();
                }
            }
        }catch (Exception e){
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
        return "";
    }

    void otherSetId(Long sportId,MatchMarketVo marketVo,MarketCategory category){
        Long set = 0L,setNum = 0L,categoryId = marketVo.getMarketCategoryId();
        //list类里玩法局数是在addition1
        String addition2=Arrays.asList(167L,208L,266L,267L,275L,283L).contains(categoryId)?marketVo.getAddition1():marketVo.getAddition2();
        setNum =StringUtils.isNotBlank(addition2)?Long.parseLong(addition2):setNum;
        if(SportIdEnum.isTennis(sportId)){
            set = sportId*10000+11+setNum;
        }else if(SportIdEnum.isSnooker(sportId)){
            set = sportId*10000+101+setNum;
        }else if(SportIdEnum.isBaseBall(sportId)){
            set=sportId*10000+13+setNum;
        }else if(SportIdEnum.isIceHockey(sportId)){
            set=sportId*10000+5+setNum;
        }
        marketVo.setSetId(set);
        category.setCategorySetId(set);
    }
}
