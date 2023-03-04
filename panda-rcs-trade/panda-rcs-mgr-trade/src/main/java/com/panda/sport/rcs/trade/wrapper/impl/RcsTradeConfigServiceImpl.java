package com.panda.sport.rcs.trade.wrapper.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.panda.merge.dto.StandardMarketDTO;
import com.panda.merge.dto.StandardMarketOddsDTO;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.enums.MarketStatusEnum;
import com.panda.sport.rcs.enums.TradeEnum;
import com.panda.sport.rcs.enums.TradeLevelEnum;
import com.panda.sport.rcs.enums.TraderLevelEnum;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.factory.BeanFactory;
import com.panda.sport.rcs.mapper.RcsStandardOutrightMatchInfoMapper;
import com.panda.sport.rcs.mapper.RcsTradeConfigMapper;
import com.panda.sport.rcs.mapper.StandardMatchInfoMapper;
import com.panda.sport.rcs.mapper.StandardSportMarketMapper;
import com.panda.sport.rcs.mongo.MatchCategorySetVo;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.RcsStandardOutrightMatchInfo;
import com.panda.sport.rcs.pojo.RcsTradeConfig;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.pojo.dto.ClearDTO;
import com.panda.sport.rcs.pojo.dto.ClearSubDTO;
import com.panda.sport.rcs.trade.enums.CategoryShowEnum;
import com.panda.sport.rcs.trade.service.TradeCommonService;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.MatchSetMongoService;
import com.panda.sport.rcs.trade.wrapper.RcsTradeConfigService;
import com.panda.sport.rcs.vo.MarketStatusUpdateVO;
import com.panda.sport.rcs.vo.UpdateOddsValueVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.panda.sport.rcs.constants.MqConstant.TRADE_CATEGORYSET_SHOW;
import static com.panda.sport.rcs.utils.CommonUtils.getLinkId;

/**
 * @author :  myname
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.wrapper.impl
 * @Description :  TODO
 * @Date: 2020-03-05 16:56
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Service
@Slf4j
public class RcsTradeConfigServiceImpl extends ServiceImpl<RcsTradeConfigMapper, RcsTradeConfig> implements RcsTradeConfigService {

    @Autowired
    private MatchSetMongoService matchSetMongoServicex;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private StandardMatchInfoMapper standardMatchInfoMapper;
    @Autowired
    private RcsStandardOutrightMatchInfoMapper standardOutrightMatchInfoMapper;
    @Autowired
    private TradeCommonService tradeCommonService;
    @Autowired
    private StandardSportMarketMapper standardSportMarketMapper;

    private static final String MTS = "MTS";

    private static List<Integer> STATUS_LIST = Lists.newArrayList(1, 2, 10);
    @Autowired
    private ProducerSendMessageUtils producerSendMessageUtils;

    @Override
    public Integer getLatestStatusByLevel(Long matchId, TraderLevelEnum tradeLevelEnum, Long targetId) {
        RcsTradeConfig config = getLatestStatusConfig(matchId, tradeLevelEnum, targetId);
        return config != null ? config.getStatus() : MarketStatusEnum.OPEN.getState();
    }

    private RcsTradeConfig getLatestStatusConfig(Long matchId, TraderLevelEnum tradeLevelEnum, Long targetId) {
        LambdaQueryWrapper<RcsTradeConfig> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(RcsTradeConfig::getMatchId, matchId.toString())
                .eq(RcsTradeConfig::getTraderLevel, tradeLevelEnum.getLevel())
                .eq(RcsTradeConfig::getTargerData, targetId.toString())
                .isNotNull(RcsTradeConfig::getStatus)
                .orderByDesc(RcsTradeConfig::getId)
                .last("LIMIT 1");
        return this.getOne(wrapper);
    }

    @Override
    public Integer getDataSource(Long matchId, Long playId) {
        LambdaQueryWrapper<RcsTradeConfig> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(RcsTradeConfig::getMatchId, matchId.toString())
                .eq(RcsTradeConfig::getTraderLevel, TradeLevelEnum.PLAY.getLevel())
                .eq(RcsTradeConfig::getTargerData, playId.toString())
                .isNotNull(RcsTradeConfig::getDataSource)
                .orderByDesc(RcsTradeConfig::getId)
                .last("LIMIT 1");
        RcsTradeConfig config = this.getOne(wrapper);
        return config != null ? config.getDataSource() : TradeEnum.AUTO.getCode();
    }

    @Override
    public Map<Long, Integer> getTradeMode(Long matchId, Collection<Long> playIds) {
        List<String> playIdList = null;
        if (CollectionUtils.isNotEmpty(playIds)) {
            playIdList = playIds.stream().map(String::valueOf).collect(Collectors.toList());
        }
        List<RcsTradeConfig> list = this.baseMapper.getTradeMode(matchId.toString(), playIdList);
        if (CollectionUtils.isEmpty(list)) {
            return Maps.newHashMap();
        }
        return list.stream().collect(Collectors.toMap(config -> NumberUtils.toLong(config.getTargerData()), RcsTradeConfig::getDataSource));
    }

    @Override
    public List<Long> getNotAutoPlayIds(Long matchId) {
        List<RcsTradeConfig> list = this.baseMapper.getTradeMode(matchId.toString(), null);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        List<Long> playIds = new ArrayList<>(list.size());
        list.forEach(config -> {
            if (!TradeEnum.isAuto(config.getDataSource())) {
                playIds.add(NumberUtils.toLong(config.getTargerData()));
            }
        });
        return playIds;
    }

    @Override
    public RcsTradeConfig getMatchStatusConfig(Long matchId) {
        LambdaQueryWrapper<RcsTradeConfig> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(RcsTradeConfig::getMatchId, matchId.toString())
                .eq(RcsTradeConfig::getTraderLevel, TradeLevelEnum.MATCH.getLevel())
                .eq(RcsTradeConfig::getTargerData, matchId.toString())
                .isNotNull(RcsTradeConfig::getStatus)
                .orderByDesc(RcsTradeConfig::getId)
                .last("LIMIT 1");
        RcsTradeConfig config = this.getOne(wrapper);
        return config != null ? config : BeanFactory.defaultMatchStatus();
    }

    @Override
    public Integer getMatchStatus(Long matchId) {
        return getMatchStatusConfig(matchId).getStatus();
    }

    @Override
    public Map<Long, RcsTradeConfig> getPlayStatus(Long matchId, Collection<Long> playIds) {
        Set<String> playIdSet = playIds.stream().map(String::valueOf).collect(Collectors.toSet());
        List<RcsTradeConfig> configList = this.baseMapper.getPlayStatus(String.valueOf(matchId), playIdSet);
        if (CollectionUtils.isEmpty(configList)) {
            return Maps.newHashMap();
        }
        return configList.stream().collect(Collectors.toMap(config -> NumberUtils.toLong(config.getTargerData()), Function.identity()));
    }

    @Override
    public RcsTradeConfig getPlaySetStatusByPlayId(Long matchId, Long playId) {
        RcsTradeConfig config = this.baseMapper.getPlaySetStatusByPlayId(String.valueOf(matchId), playId);
        return config != null ? config : BeanFactory.defaultCategorySetStatus();
    }

    @Override
    public Map<Integer, RcsTradeConfig> getMarketPlaceStatus(Long matchId, Long playId) {
        List<RcsTradeConfig> configList = this.baseMapper.getMarketPlaceStatus(String.valueOf(matchId), Lists.newArrayList(String.valueOf(playId)));
        if (CollectionUtils.isEmpty(configList)) {
            return Maps.newHashMap();
        }
        return configList.stream().collect(Collectors.toMap(config -> Integer.valueOf(config.getTargerData()), Function.identity()));
    }

    @Override
    public Map<Long, Map<Integer, RcsTradeConfig>> getMarketPlaceStatus(Long matchId, Collection<Long> playIds) {
        Set<String> playIdSet = playIds.stream().map(String::valueOf).collect(Collectors.toSet());
        List<RcsTradeConfig> configList = this.baseMapper.getMarketPlaceStatus(String.valueOf(matchId), playIdSet);
        if (CollectionUtils.isEmpty(configList)) {
            return Maps.newHashMap();
        }
        Map<Long, Map<Integer, RcsTradeConfig>> resultMap = Maps.newHashMapWithExpectedSize(playIds.size());
        configList.forEach(config -> {
            Long playId = NumberUtils.toLong(config.getAddition1());
            Integer placeNum = NumberUtils.toInt(config.getTargerData());
            if (resultMap.containsKey(playId)) {
                resultMap.get(playId).put(placeNum, config);
            } else {
                Map<Integer, RcsTradeConfig> placeStatusMap = Maps.newHashMap();
                placeStatusMap.put(placeNum, config);
                resultMap.put(playId, placeStatusMap);
            }
        });
        return resultMap;
    }

    @Override
    public Map<Integer, RcsTradeConfig> getSubPlayPlaceStatus(Long matchId, Long playId, Long subPlayId) {
        List<RcsTradeConfig> configList = this.baseMapper.getSubPlayPlaceStatus(String.valueOf(matchId), Lists.newArrayList(String.valueOf(playId)), Lists.newArrayList(subPlayId));
        if (CollectionUtils.isEmpty(configList)) {
            return Maps.newHashMap();
        }
        return configList.stream().collect(Collectors.toMap(config -> NumberUtils.toInt(config.getTargerData()), Function.identity()));
    }

    @Override
    public Map<Long, Map<Long, Map<Integer, RcsTradeConfig>>> getSubPlayPlaceStatus(Long matchId, Collection<Long> playIds, Collection<Long> subPlayIds) {
        Set<String> playIdSet = playIds.stream().map(String::valueOf).collect(Collectors.toSet());
        List<RcsTradeConfig> configList = this.baseMapper.getSubPlayPlaceStatus(String.valueOf(matchId), playIdSet, subPlayIds);
        if (CollectionUtils.isEmpty(configList)) {
            return Maps.newHashMap();
        }
        Map<Long, Map<Long, Map<Integer, RcsTradeConfig>>> resultMap = Maps.newHashMap();
        Map<String, List<RcsTradeConfig>> groupByPlayId = configList.stream().collect(Collectors.groupingBy(RcsTradeConfig::getAddition1));
        groupByPlayId.forEach((playId, subPlayList) -> {
            Map<Long, List<RcsTradeConfig>> groupSubPlayId = subPlayList.stream().collect(Collectors.groupingBy(RcsTradeConfig::getSubPlayId));
            Map<Long, Map<Integer, RcsTradeConfig>> subPlayMap = Maps.newHashMap();
            groupSubPlayId.forEach((subPlayId, list) -> {
                Map<Integer, RcsTradeConfig> placeMap = list.stream().collect(Collectors.toMap(config -> NumberUtils.toInt(config.getTargerData()), Function.identity()));
                subPlayMap.put(subPlayId, placeMap);
            });
            resultMap.put(NumberUtils.toLong(playId), subPlayMap);
        });
        return resultMap;
    }

    @Override
    public List<RcsTradeConfig> getNotOpenStatusByMatchId(Long matchId) {
        return this.baseMapper.getNotOpenStatusByMatchId(String.valueOf(matchId));
    }

    @Override
    public void saveTradeStatusConfig(MarketStatusUpdateVO updateVO) {
        Integer tradeLevel = updateVO.getTradeLevel();
        Long matchId = updateVO.getMatchId();
        Integer status = updateVO.getMarketStatus();
        Integer linkedType = updateVO.getLinkedType();
        Integer updateUserId = updateVO.getUpdateUserId();

        if (TradeLevelEnum.isBatchPlayLevel(tradeLevel) || TradeLevelEnum.isPlaySetLevel(tradeLevel) || TradeLevelEnum.isPlaySetCodeLevel(tradeLevel)) {
            saveMultipleStatusConfig(updateVO);
            return;
        }

        Long playId = updateVO.getCategoryId();
        Long subPlayId = updateVO.getSubPlayId();
        RcsTradeConfig config = new RcsTradeConfig(matchId, tradeLevel, status, linkedType, updateUserId);
        if (TradeLevelEnum.isMatchLevel(tradeLevel)) {
            config.setTargerData(matchId.toString());
        } else if (TradeLevelEnum.isMarketLevel(tradeLevel)) {
            config.setTargerData(String.valueOf(updateVO.getMarketPlaceNum()));
            config.setAddition1(String.valueOf(playId));
            config.setSubPlayId(subPlayId);
        } else if (TradeLevelEnum.isPlayLevel(tradeLevel)) {
            config.setTargerData(String.valueOf(playId));
            config.setSubPlayId(subPlayId);
        } else {
            log.warn("::{}::操盘级别有误：tradeLevel" + tradeLevel,matchId);
            return;
        }
        this.save(config);
    }

    private void saveMultipleStatusConfig(MarketStatusUpdateVO updateVO) {
        Integer tradeLevel = updateVO.getTradeLevel();
        if (!TradeLevelEnum.isBatchPlayLevel(tradeLevel) && !TradeLevelEnum.isPlaySetLevel(tradeLevel) && !TradeLevelEnum.isPlaySetCodeLevel(tradeLevel)) {
            return;
        }
        Long matchId = updateVO.getMatchId();
        Integer status = updateVO.getMarketStatus();
        Integer linkedType = updateVO.getLinkedType();
        Integer updateUserId = updateVO.getUpdateUserId();
        List<Long> playIds = null;
        if (TradeLevelEnum.isBatchPlayLevel(tradeLevel)) {
            playIds = updateVO.getCategoryIdList();
        } else if (TradeLevelEnum.isPlaySetLevel(tradeLevel)) {
            playIds = updateVO.getPlaceholderPlayIds();
        } else if (TradeLevelEnum.isPlaySetCodeLevel(tradeLevel)) {
            playIds = Lists.newArrayList();
            List<Long> sealNormalPlayIds = updateVO.getSealNormalPlayIds();
            List<Long> sealPlaceholderPlayIds = updateVO.getSealPlaceholderPlayIds();
            if (CollectionUtils.isNotEmpty(sealNormalPlayIds)) {
                playIds.addAll(sealNormalPlayIds);
            }
            if (CollectionUtils.isNotEmpty(sealPlaceholderPlayIds)) {
                playIds.addAll(sealPlaceholderPlayIds);
            }
        }
        List<RcsTradeConfig> configList = null;
        if (CollectionUtils.isNotEmpty(playIds)) {
            configList = playIds.stream().map(playId -> {
                RcsTradeConfig config = new RcsTradeConfig(matchId, TradeLevelEnum.PLAY.getLevel(), status, linkedType, updateUserId);
                config.setTargerData(String.valueOf(playId));
                return config;
            }).collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(configList)) {
            configList = Lists.newArrayList();
        }
        if (TradeLevelEnum.isPlaySetLevel(tradeLevel)) {
            RcsTradeConfig config = new RcsTradeConfig(matchId, tradeLevel, status, linkedType, updateUserId);
            config.setTargerData(String.valueOf(updateVO.getCategorySetId()));
            configList.add(config);
        }
        if (TradeLevelEnum.isPlaySetCodeLevel(tradeLevel)) {
            RcsTradeConfig config = new RcsTradeConfig(matchId, tradeLevel, status, linkedType, updateUserId);
            config.setTargerData(updateVO.getPlaySetCode());
            configList.add(config);
        }
        if (CollectionUtils.isNotEmpty(configList)) {
            this.saveBatch(configList);
        }
    }

    @Override
    public void isMtsTrade(RcsMatchMarketConfig config, Integer sportId) {
        StandardMatchInfo info = standardMatchInfoMapper.selectById(config.getMatchId());
        if (ObjectUtils.isEmpty(info)) {
            throw new RcsServiceException("赛事不存在=" + config.getMatchId());
        }
        if (STATUS_LIST.contains(info.getMatchStatus()) && MTS.equalsIgnoreCase(info.getLiveRiskManagerCode())) {
            throw new RcsServiceException("MTS滚球操盘不支持该操作");
        }
        if (!STATUS_LIST.contains(info.getMatchStatus()) && MTS.equalsIgnoreCase(info.getPreRiskManagerCode())) {
            throw new RcsServiceException("MTS早盘操盘不支持该操作");
        }
        if (ObjectUtils.isNotEmpty(info.getSportId())) {
            config.setSportId(info.getSportId().intValue());
        } else {
            config.setSportId(sportId);
        }
        if (ObjectUtils.isEmpty(config.getMatchType())) {
            config.setMatchType(!ObjectUtils.isEmpty(info.getOddsLive()) && info.getOddsLive() == 1 ? NumberUtils.INTEGER_ZERO : NumberUtils.INTEGER_ONE);
        }
        config.setMatchType(config.getMatchType() == 2 ? NumberUtils.INTEGER_ZERO : config.getMatchType());
    }

    @Override
    public HashMap<Integer, RcsTradeConfig> getRcsTradeConfigStatusByMatchId(List<Integer> matchId) {
        HashMap<Integer, RcsTradeConfig> rcsTradeConfigHashMap = new HashMap<>();
        List<RcsTradeConfig> rcsTradeConfigStatusByMatchId = this.baseMapper.getRcsTradeConfigStatusByMatchId(matchId);
        if (!CollectionUtils.isEmpty(rcsTradeConfigStatusByMatchId)) {
            for (RcsTradeConfig rcsTradeConfig : rcsTradeConfigStatusByMatchId) {
                Integer targerData = Integer.parseInt(rcsTradeConfig.getTargerData());
                RcsTradeConfig rcsTradeConfig1 = rcsTradeConfigHashMap.get(targerData);
                if (rcsTradeConfig1 == null) {
                    rcsTradeConfigHashMap.put(targerData, rcsTradeConfig);
                }
            }
        }
        return rcsTradeConfigHashMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void championMatchTradeType(MarketStatusUpdateVO config) {
        log.info("::{}::冠军赛事，操盘方式切换:{}", CommonUtil.getRequestId(config.getMatchId(),config.getCategoryId()), JsonFormatUtils.toJson(config));

        //操盘方式切换，记录保存
        this.championMarketRecord(config);
        //调用融合RPC服务
        tradeCommonService.championMatchTradeType(config);

        ClearSubDTO marketConfig = JSONObject.parseObject(JSONObject.toJSONString(config), ClearSubDTO.class);
        marketConfig.setPlayId(config.getCategoryId());
        //清理概率差
        ClearDTO clearDTO = new ClearDTO();
        clearDTO.setMatchId(config.getMatchId());
        clearDTO.setType(NumberUtils.INTEGER_ONE);
        clearDTO.setList(Arrays.asList(marketConfig));
        clearDTO.setClearType(NumberUtils.INTEGER_TWO);
        producerSendMessageUtils.sendMessage("RCS_CLEAR_CHAMPION_MARKET", clearDTO);
        //手动操盘处理,给融合下发赔率
        this.putTradeMarketOdds(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void championMatchTradeStatus(MarketStatusUpdateVO config) {
        log.info("::{}::冠军赛事，开关封锁:{}", CommonUtil.getRequestId(config.getMatchId(),config.getCategoryId()), JsonFormatUtils.toJson(config));
        if (TradeLevelEnum.isBetItemLevel(config.getTradeLevel()) && ObjectUtils.isNotEmpty(config.getOddsId())) {
            //投注项开关封锁,调用融合RPC服务  marketStatus   投注项状态  0-关，1-开
            tradeCommonService.championMatchTradeBetItemStatus(config);
        } else {
            //盘口开关封锁，记录保存   盘口状态  0-开，2-关
            this.championMarketRecord(config);
            //调用融合RPC服务
            tradeCommonService.championMatchTradeStatus(config);
        }
        //手动操盘处理,给融合下发赔率
        this.putTradeMarketOdds(config);
    }

    private void championMarketRecord(MarketStatusUpdateVO config) {
        RcsStandardOutrightMatchInfo outrightMatchInfo = standardOutrightMatchInfoMapper.selectById(config.getMatchId());
        if (ObjectUtils.isEmpty(outrightMatchInfo)) {
            throw new IllegalArgumentException("冠军赛事不存在:" + config.getMatchId());
        }
        //操作记录保存
        RcsTradeConfig tradeConfig = new RcsTradeConfig()
                .setMatchId(config.getMatchId().toString())
                .setTraderLevel(config.getTradeLevel())
                .setTargerData(config.getMarketId())
                .setDataSource(config.getTradeType())
                .setStatus(config.getMarketStatus())
                .setUpdateUser(String.valueOf(config.getUpdateUserId()));
        this.save(tradeConfig);
    }

    private void putTradeMarketOdds(MarketStatusUpdateVO config) {
        //手动操盘处理,给融合下发赔率
        if (config.getTradeType().intValue() == NumberUtils.INTEGER_ONE.intValue()) {
            UpdateOddsValueVo updateOddsValueVo = new UpdateOddsValueVo();
            updateOddsValueVo.setMarketId(Long.valueOf(config.getMarketId()));
            StandardMarketDTO market = standardSportMarketMapper.selectChampionOddsByMarketIds(updateOddsValueVo);
            if (ObjectUtils.isEmpty(market)) {
                throw new IllegalArgumentException("盘口不存在");
            }
            RcsMatchMarketConfig matchMarketConfig = new RcsMatchMarketConfig();
            matchMarketConfig.setMatchId(config.getMatchId());
            market.setId(config.getMarketId());
            market = JSONObject.parseObject(JSONObject.toJSONString(market), StandardMarketDTO.class);
            if (market.getThirdMarketSourceStatus().intValue() == NumberUtils.INTEGER_TWO.intValue()) {
                market.setThirdMarketSourceStatus(NumberUtils.INTEGER_ZERO);
            }
            if (!TradeLevelEnum.isBetItemLevel(config.getTradeLevel()) && null != config.getMarketStatus()) {
                market.setStatus(config.getMarketStatus());
            }
            tradeCommonService.setI18nName(market);
            List<StandardMarketOddsDTO> standardMarketOddsList = market.getMarketOddsList();
            if (TradeLevelEnum.isBetItemLevel(config.getTradeLevel()) && ObjectUtils.isNotEmpty(config.getOddsId()) && StringUtils.isNotEmpty(config.getOddsType())) {
                standardMarketOddsList.stream().forEach(item -> {
                    if (config.getOddsType().equals(item.getOddsType())) {
                        item.setActive(config.getMarketStatus());
                    }
                });
            }
            market.setDataSourceCode("PA");   //冠军手动操盘更改为PA，ws推送区分操盘模式（手动PA和自动SR）
            tradeCommonService.putTradeMarketOdds(matchMarketConfig, Arrays.asList(market), NumberUtils.INTEGER_TWO);
        }
    }

    @Override
    public void updateShow(MarketStatusUpdateVO vo) {
        Long sportId = vo.getSportId();
        String userId = String.valueOf(vo.getUpdateUserId());
        Long matchId = vo.getMatchId();
        Integer oddBusiness = vo.getLiveOddBusiness();
        if (oddBusiness == null) oddBusiness = 0;
        Long categorySetId = CategoryShowEnum.querySendId(vo.getCategorySetId());
        if (categorySetId.intValue() <= 0) {
            throw new RcsServiceException("不支持该玩法集");
        }
        Integer categorySetShow = vo.getClientShow();
        
        // 10105: 晋级/冠军，只有一个开
        Long subPlayId = vo.getSubPlayId();
        if (categorySetId.equals(10105L)) {
        	if (subPlayId == null) {
        		throw new RcsServiceException("晋级玩法开关需要指定冠军/晋级");
        	}
        	if (categorySetShow == 1) {
        		categorySetShow = subPlayId.intValue();
        	}
        }
//        Integer tradeLevel = TradeLevelEnum.CATEGORYSET_SHOW.getLevel();

        MatchCategorySetVo matchCategorySetVo = new MatchCategorySetVo()
                .setCategorySetId(categorySetId)
                .setMatchId(matchId)
                .setSportId(sportId)
                .setLiveOdds(oddBusiness)
                .setTraderId(userId)
                .setClientShow(categorySetShow);
        matchSetMongoServicex.upsertMatchCategorySetMongo(matchCategorySetVo);
        /*RcsTradeConfig config = new RcsTradeConfig();
        config.setMatchId(matchId.toString());
        config.setTraderLevel(tradeLevel);
        config.setUpdateUser(userId);
        config.setTargerData(String.valueOf(categorySetId));
        config.setCategorySetShow(categorySetShow);
        config.setAddition1(String.valueOf(oddBusiness));
        RcsTradeConfig tradeConfig = this.getBaseMapper().getRcsTradeConfig(config);
        if(tradeConfig==null){
            this.save(config);
        }else {
            this.baseMapper.updateClientShow(config);
        }*/

        //向业务发送MQ
        JSONObject mqSend = new JSONObject();
        mqSend.put("sportId", sportId);
        mqSend.put("matchId", matchId);
        mqSend.put("categorySetId", categorySetId);
        mqSend.put("clientShow", vo.getClientShow());
        mqSend.put("liveOdds", oddBusiness);
        mqSend.put("playId", subPlayId);
        JSONObject data = new JSONObject();
        data.put("linkId", getLinkId("show"));
        data.put("data", mqSend);
        producerSendMessageUtils.sendMessage(TRADE_CATEGORYSET_SHOW, String.valueOf(vo.getClientShow()), String.valueOf(matchId), data);
    }

    @Override
    public Map<String, Integer> queryCategoryShow(List<Long> matchIds, Integer liveOdds) {
        Map<String, Integer> res = new HashMap<>();
        /*if (CollectionUtils.isNotEmpty(matchIds)) {
            List<RcsTradeConfig> configs = this.baseMapper.queryCategoryShow(matchIds,liveOdds);
            res = configs.stream().filter(fi -> fi.getCategorySetShow() != null).collect(Collectors.toMap(e -> e.getMatchId() + "_" + e.getTargerData(), e -> e.getCategorySetShow()));
        }*/
        Criteria criteria = Criteria.where("matchId").in(matchIds).and("liveOdds").is(liveOdds);
        List<MatchCategorySetVo> setVos = mongoTemplate.find(new Query().addCriteria(criteria), MatchCategorySetVo.class);
        if(CollectionUtils.isNotEmpty(setVos)){
            res =setVos.stream().collect(Collectors.toMap(e->e.getMatchId()+"_"+e.getCategorySetId(),e->e.getClientShow(), (entity1, entity2) -> entity1));
        }
        return res;
    }

}
