package com.panda.sport.rcs.trade.wrapper.tourTemplate.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.panda.merge.api.ITradeMarketConfigApi;
import com.panda.merge.dto.ConfigTournamentTradeItemDTO;
import com.panda.merge.dto.Request;
import com.panda.merge.dto.Response;
import com.panda.merge.dto.TradeClearDiffValueDTO;
import com.panda.sport.rcs.cache.redis.RedisUtils;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.core.utils.BeanCopyUtils;
import com.panda.sport.rcs.enums.SportIdEnum;
import com.panda.sport.rcs.mapper.MarketCategorySetMapper;
import com.panda.sport.rcs.mapper.RcsMatchMarketConfigMapper;
import com.panda.sport.rcs.mapper.RcsMatchMarketProbabilityConfigMapper;
import com.panda.sport.rcs.mapper.StandardMatchInfoMapper;
import com.panda.sport.rcs.mapper.tourTemplate.RcsTournamentTemplateJumpConfigMapper;
import com.panda.sport.rcs.mapper.tourTemplate.RcsTournamentTemplateMapper;
import com.panda.sport.rcs.pojo.RcsMarketCategorySet;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.pojo.StandardSportTournament;
import com.panda.sport.rcs.pojo.constants.TradeConstant;
import com.panda.sport.rcs.pojo.dto.ClearSubDTO;
import com.panda.sport.rcs.pojo.tourTemplate.*;
import com.panda.sport.rcs.trade.param.TournamentTemplateJumpConfigParam;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.RcsOddsConvertMappingService;
import com.panda.sport.rcs.trade.wrapper.tourTemplate.IRcsTournamentTemplateJumpConfigService;
import com.panda.sport.rcs.trade.wrapper.tourTemplate.IRcsTournamentTemplatePlayMargainService;
import com.panda.sport.rcs.trade.wrapper.tourTemplate.RcsTournamentTemplateAcceptConfigAutoChangeService;
import com.panda.sport.rcs.utils.DataRealtimeApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;

import static com.panda.sport.rcs.constants.RedisKey.TEMPLATE_TOURNAMENT_AMOUNT;

/**
 * @author :  carver
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.pojo.tourTemplate
 * @Description :  综合操盘跳分设置表
 * @Date: 2021-09-29 16:41
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Service
@Slf4j
public class RcsTournamentTemplateJumpConfigServiceImpl extends ServiceImpl<RcsTournamentTemplateJumpConfigMapper, RcsTournamentTemplateJumpConfig> implements IRcsTournamentTemplateJumpConfigService {
    @Autowired
    private RcsTournamentTemplateJumpConfigMapper tournamentTemplateJumpConfigMapper;
    @Reference(check = false, lazy = true, retries = 3, timeout = 5000)
    private ITradeMarketConfigApi tradeMarketConfigApi;
    @Autowired
    private RedisClient redisClient;
    @Autowired
    private RcsOddsConvertMappingService rcsOddsConvertMappingService;
    @Autowired
    private RcsMatchMarketConfigMapper rcsMatchMarketConfigMapper;
    @Autowired
    private RcsMatchMarketProbabilityConfigMapper rcsMatchMarketProbabilityConfigMapper;
    @Autowired
    private StandardMatchInfoMapper standardMatchInfoMapper;
    @Autowired
    private IRcsTournamentTemplatePlayMargainService playMargainService;
    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private RcsTournamentTemplateMapper rcsTournamentTemplateMapper;
    @Autowired
    private RcsTournamentTemplateAcceptConfigAutoChangeService templateAcceptConfigAutoChangeService;
    @Autowired
    private MarketCategorySetMapper marketCategorySetMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initMTSOddsChangeValue() {
        //kir-1309-所有没有自己操盘的赛种默认读取该配置
        List<StandardSportTournament> standardSportTournaments = tournamentTemplateJumpConfigMapper.selectMTSOddsChangeValue();
        for (StandardSportTournament param : standardSportTournaments) {
            redisUtils.hset(String.format("rcs:tournament:property:%s", String.valueOf(param.getId())), "MTSOddsChangeValue", String.valueOf(4));
            redisUtils.hset(String.format("rcs:tournament:property:%s", String.valueOf(param.getId())), "orderDelayTime", String.valueOf(5));
            redisUtils.hset(String.format("rcs:tournament:property:%s", String.valueOf(param.getId())), "oddsChangeStatus", String.valueOf(1));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initTournamentSpecialOddsInterval() {
        List<TournamentTemplatePlayMargainResVo> list = tournamentTemplateJumpConfigMapper.selectTournamentSpecialOddsIntervalInitData();
        List<RcsTournamentTemplatePlayMargain> newList = new ArrayList<>();
        for (TournamentTemplatePlayMargainResVo margain : list) {
            String preStr = "{\"1.01-1.19\":%s,\"1.20-1.39\":%s,\"1.40-1.59\":%s,\"1.60-1.79\":%s,\"1.80-1.85\":%s,\"1.86-2.00\":%s}";
            String liveStr = "{\"1.01-1.05\":%s,\"1.06-1.19\":%s,\"1.20-1.39\":%s,\"1.40-1.60\":%s,\"1.61-1.85\":%s,\"1.86-1.88\":%s,\"1.89-2.00\":%s}";
            if (margain.getMatchType().equals(1)) {
                margain.setSpecialOddsIntervalHigh(String.format(preStr, margain.getOrderSinglePayVal().multiply(new BigDecimal(1)), margain.getOrderSinglePayVal().multiply(new BigDecimal(1)), margain.getOrderSinglePayVal().multiply(new BigDecimal(1)), margain.getOrderSinglePayVal().multiply(new BigDecimal(1)), margain.getOrderSinglePayVal().multiply(new BigDecimal(1)), margain.getOrderSinglePayVal().multiply(new BigDecimal(1))));
                margain.setSpecialOddsIntervalLow(String.format(preStr, margain.getOrderSinglePayVal().multiply(new BigDecimal("0.5")), margain.getOrderSinglePayVal().multiply(new BigDecimal("0.6")), margain.getOrderSinglePayVal().multiply(new BigDecimal("0.7")), margain.getOrderSinglePayVal().multiply(new BigDecimal("0.8")), margain.getOrderSinglePayVal().multiply(new BigDecimal("0.9")), margain.getOrderSinglePayVal().multiply(new BigDecimal(1))));
                margain.setSpecialOddsIntervalStatus("{\"1.01-1.19\":0,\"1.20-1.39\":0,\"1.40-1.59\":0,\"1.60-1.79\":0,\"1.80-1.85\":0,\"1.86-2.00\":0}");
            } else {
                margain.setSpecialOddsIntervalHigh(String.format(liveStr, margain.getOrderSinglePayVal().multiply(new BigDecimal(1)), margain.getOrderSinglePayVal().multiply(new BigDecimal(1)), margain.getOrderSinglePayVal().multiply(new BigDecimal(1)), margain.getOrderSinglePayVal().multiply(new BigDecimal(1)), margain.getOrderSinglePayVal().multiply(new BigDecimal(1)), margain.getOrderSinglePayVal().multiply(new BigDecimal(1)), margain.getOrderSinglePayVal().multiply(new BigDecimal(1))));
                margain.setSpecialOddsIntervalLow(String.format(liveStr, margain.getOrderSinglePayVal().multiply(new BigDecimal("0.5")), margain.getOrderSinglePayVal().multiply(new BigDecimal("0.5")), margain.getOrderSinglePayVal().multiply(new BigDecimal("0.6")), margain.getOrderSinglePayVal().multiply(new BigDecimal("0.7")), margain.getOrderSinglePayVal().multiply(new BigDecimal("0.8")), margain.getOrderSinglePayVal().multiply(new BigDecimal("0.9")), margain.getOrderSinglePayVal().multiply(new BigDecimal(1))));
                margain.setSpecialOddsIntervalStatus("{\"1.01-1.05\":0,\"1.06-1.19\":0,\"1.20-1.39\":0,\"1.40-1.60\":0,\"1.61-1.85\":0,\"1.86-1.88\":0,\"1.89-2.00\":0}");
            }
            newList.add(margain);
        }
        playMargainService.updateBatchById(newList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initTournamentSpecialBettingIntervalHigh() {
        List<TournamentTemplatePlayMargainResVo> list = tournamentTemplateJumpConfigMapper.selectTournamentSpecialOddsIntervalInitData();
        List<RcsTournamentTemplatePlayMargain> newList = new ArrayList<>();
        for (TournamentTemplatePlayMargainResVo margain : list) {
            if (margain.getSpecialOddsIntervalHigh() != null) {
                JSONObject specialOddsIntervalHighJSON = JSON.parseObject(margain.getSpecialOddsIntervalHigh(), Feature.OrderedField);
                for (String key : specialOddsIntervalHighJSON.keySet()) {
                    specialOddsIntervalHighJSON.put(key, specialOddsIntervalHighJSON.getBigDecimal(key).multiply(new BigDecimal("0.25")));
                }
                margain.setSpecialBettingIntervalHigh(specialOddsIntervalHighJSON.toJSONString());
            }
            newList.add(margain);
        }
        playMargainService.updateBatchById(newList);
    }

    @Override
    public void initOddsChangeValue() {
        List<TournamentTemplatePlayMargainOddsResVo> list = tournamentTemplateJumpConfigMapper.selectTournamentOddsChangeValue();
        List<RcsTournamentTemplatePlayMargain> newList = new ArrayList<>();
        for (TournamentTemplatePlayMargainOddsResVo margain : list) {
            if (margain.getOddsChangeStatus() != null && margain.getOddsChangeStatus().equals(1)) {
                //log.info("::{}::kir-1546-初始化-开始录入缓存:{},{},{}",CommonUtil.getRequestId(), margain.getTypeVal(), margain.getPlayId(), margain.getMatchType());
                String redisKey = "rcs:risk:order:oddsScope:match.%s.play_id.%s.match_type.%s";
                redisUtils.set(String.format(redisKey, margain.getTypeVal(), margain.getPlayId(), margain.getMatchType()), String.valueOf(margain.getOddsChangeValue()));
                //log.info("::{}::kir-1546-初始化-录入缓存结束:{},{},{}",CommonUtil.getRequestId(), margain.getTypeVal(), margain.getPlayId(), margain.getMatchType());
            } else {
                //log.info("::{}::kir-1546-初始化-开始删除缓存:{},{},{}",CommonUtil.getRequestId(), margain.getTypeVal(), margain.getPlayId(), margain.getMatchType());
                String redisKey = "rcs:risk:order:oddsScope:match.%s.play_id.%s.match_type.%s";
                redisUtils.del(String.format(redisKey, margain.getTypeVal(), margain.getPlayId(), margain.getMatchType()));
                //log.info("::{}::kir-1546-初始化-删除缓存成功:{},{},{}",CommonUtil.getRequestId(), margain.getTypeVal(), margain.getPlayId(), margain.getMatchType());
            }
            margain.setOddsChangeValue(new BigDecimal(4));
            newList.add(margain);
        }
        playMargainService.updateBatchById(newList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTournamentTemplateJumpConfig(TournamentTemplateJumpConfigParam param) {
        tournamentTemplateJumpConfigMapper.insertBatch(param);
        //推送综合操盘跳分机制至融合和缓存
        sendJumpConfig(param);
    }

    @Override
    public void initTournamentJump(Long sportId, Long tournamentId) {
        Map<String, Object> map = Maps.newHashMap();
        if (!ObjectUtils.isEmpty(sportId)) {
            map.put("sport_id", sportId);
        }
        if (!ObjectUtils.isEmpty(tournamentId)) {
            map.put("tournament_id", tournamentId);
        }
        List<RcsTournamentTemplateJumpConfig> list = tournamentTemplateJumpConfigMapper.selectByMap(map);
        log.info("::{}::联赛跳分机制数据：{}",CommonUtil.getRequestId(), JSONObject.toJSONString(list));
        if (!CollectionUtils.isEmpty(list)) {
            for (RcsTournamentTemplateJumpConfig obj : list) {
                sendJumpConfig(obj);
            }
        }
    }

    @Override
    public void clearMatchWater(RcsMatchMarketConfig config) {
        QueryWrapper<StandardMatchInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(StandardMatchInfo::getMatchManageId, config.getMatchId().toString());
        List<StandardMatchInfo> infos = standardMatchInfoMapper.selectList(queryWrapper);
        if (!CollectionUtils.isEmpty(infos)) {
            redisClient.delete(String.format(TradeConstant.RCS_MARKET_TIMES, infos.get(0).getId()));
            rcsMatchMarketConfigMapper.updateMatchWaterConfigByMatch(infos.get(0).getId().toString());
            List<ClearSubDTO> list = Lists.newArrayList();
            ClearSubDTO dto = new ClearSubDTO();
            dto.setMatchId(infos.get(0).getId());
            list.add(dto);
            rcsMatchMarketProbabilityConfigMapper.updateProbabilityBySelectivetToZero(list);
            TradeClearDiffValueDTO diffValueDTO = new TradeClearDiffValueDTO();
            diffValueDTO.setSportId(infos.get(0).getSportId().intValue());
            diffValueDTO.setStandardMatchId(infos.get(0).getId());
//            List<Long> categoryList = list.stream().map(e -> e.getPlayId()).collect(Collectors.toList());
            diffValueDTO.setCategoryList(SportIdEnum.getAllPlaysBySportId(infos.get(0).getSportId()));
            DataRealtimeApiUtils.handleApi(diffValueDTO, new DataRealtimeApiUtils.ApiCall() {
                @Override
                @Trace
                public <R> Response<R> callApi(Request request) {
                    return tradeMarketConfigApi.clearDiffValue(request);
                }
            });
        }
    }

    /**
     * 增加百家陪配置
     *
     * @param jsonObject 配置名称
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void addBaiJiaPaiWeight(JSONObject jsonObject) {
        //先查询所有的数据
        QueryWrapper<RcsTournamentTemplate> templateQueryWrapper = new QueryWrapper();

        templateQueryWrapper.lambda().eq(RcsTournamentTemplate::getSportId, jsonObject.getString("sportId"))
                .in(RcsTournamentTemplate::getType, 1, 2);
        List<RcsTournamentTemplate> rcsTournamentTemplateList = rcsTournamentTemplateMapper.selectList(templateQueryWrapper);
        if (rcsTournamentTemplateList.isEmpty()) {
            log.info("::{}::增加百家配置没有找到模板信息",CommonUtil.getRequestId());
            return;
        }
        rcsTournamentTemplateList.forEach(rcsTournamentTemplate -> {
            String configValue = rcsTournamentTemplate.getBaijiaConfigValue();
            JSONArray jsonArray = JSONObject.parseArray(configValue);
            if (!isContains(jsonArray)) {
                JSONObject tempJson = new JSONObject();
                tempJson.put("name", jsonObject.getString("name"));
                tempJson.put("value", 0);
                tempJson.put("status", 0);
                jsonArray.add(tempJson);
                rcsTournamentTemplate.setBaijiaConfigValue(JSONObject.toJSONString(jsonArray));
                //修改数据库
                rcsTournamentTemplateMapper.updateById(rcsTournamentTemplate);
            }
        });
    }

    @Override
    public void addDataSourceCode() {
        //先查询所有的数据
        QueryWrapper<RcsTournamentTemplate> templateQueryWrapper = new QueryWrapper();
        //查询赛事和联赛模板
        templateQueryWrapper.lambda().in(RcsTournamentTemplate::getType, 1, 2);
        List<RcsTournamentTemplate> rcsTournamentTemplateList = rcsTournamentTemplateMapper.selectList(templateQueryWrapper);
        if (rcsTournamentTemplateList.isEmpty()) {
            log.info("::{}::增加数据源配置没有找到模板信息",CommonUtil.getRequestId());
            return;
        }
        rcsTournamentTemplateList.forEach(rcsTournamentTemplate -> {
            Map<String, Integer> map = JSONObject.parseObject(rcsTournamentTemplate.getDataSourceCode(), LinkedHashMap.class, Feature.OrderedField);
            if (map.containsKey("PD") && map.get("PD") == 6) {
                map.remove("PD");
                map.put("AO", 6);
                map.put("PI", 7);
                map.put("PD", 8);
            }
            rcsTournamentTemplate.setDataSourceCode(JSONObject.toJSONString(map));
            rcsTournamentTemplateMapper.updateById(rcsTournamentTemplate);
        });
    }

    @Override
    public void addTemplateAcceptConfigAutoChange() {
        List<RcsMarketCategorySet> rcsMarketCategorySets = marketCategorySetMapper.selectList(new QueryWrapper<RcsMarketCategorySet>().lambda().eq(RcsMarketCategorySet::getSportId, 1).eq(RcsMarketCategorySet::getType, 1));
        if (CollectionUtils.isEmpty(rcsMarketCategorySets)) {
            log.info("::{}::addTemplateAcceptConfigAutoChange==玩法集为空！",CommonUtil.getRequestId());
            return;
        }
        QueryWrapper<RcsTournamentTemplate> templateQueryWrapper = new QueryWrapper();
        //查询赛事和联赛模板
        templateQueryWrapper.lambda().in(RcsTournamentTemplate::getType, 1, 2)
                .eq(RcsTournamentTemplate::getSportId, 1)
                .eq(RcsTournamentTemplate::getMatchType, 0);
        List<RcsTournamentTemplate> rcsTournamentTemplateList = rcsTournamentTemplateMapper.selectList(templateQueryWrapper);
        if (rcsTournamentTemplateList.isEmpty()) {
            log.info("::{}::增加数据源配置没有找到模板信息",CommonUtil.getRequestId());
            return;
        }

        List<RcsTournamentTemplateAcceptConfigAutoChange> list = new ArrayList<>();
        log.info("::{}::addTemplateAcceptConfigAutoChange rcsMarketCategorySets:{},rcsTournamentTemplateList:{}",CommonUtil.getRequestId(), rcsMarketCategorySets.size(), rcsTournamentTemplateList.size());

        for (RcsMarketCategorySet rcsMarketCategorySet : rcsMarketCategorySets) {
            for (RcsTournamentTemplate rcsTournamentTemplate : rcsTournamentTemplateList) {
                long count = templateAcceptConfigAutoChangeService.count(new QueryWrapper<RcsTournamentTemplateAcceptConfigAutoChange>().lambda()
                        .eq(RcsTournamentTemplateAcceptConfigAutoChange::getTemplateId, rcsTournamentTemplate.getId())
                        .eq(RcsTournamentTemplateAcceptConfigAutoChange::getCategorySetId, rcsMarketCategorySet.getId()));
                if (count <= 0) {
                    RcsTournamentTemplateAcceptConfigAutoChange change = new RcsTournamentTemplateAcceptConfigAutoChange();
                    change.setTemplateId(rcsTournamentTemplate.getId());
                    change.setCategorySetId(rcsMarketCategorySet.getId());
                    change.setIsOpen(1);
                    list.add(change);
                }
            }
        }
        if (!CollectionUtils.isEmpty(list) && list.size() > 0) {
            boolean result = templateAcceptConfigAutoChangeService.saveBatch(list);
            log.info("::{}::addTemplateAcceptConfigAutoChange save:{},list:{}",CommonUtil.getRequestId(), list.size(), result);
        }
    }

    @Override
    public void addDataSourceCodeCommon(String key, Integer val) {
        //先查询所有的数据
        QueryWrapper<RcsTournamentTemplate> templateQueryWrapperData = new QueryWrapper();
        //查询赛事和联赛模板
        templateQueryWrapperData.lambda().in(RcsTournamentTemplate::getType, 1, 2);
        List<RcsTournamentTemplate> rcsTournamentTemplateListData = rcsTournamentTemplateMapper.selectList(templateQueryWrapperData);
        if (rcsTournamentTemplateListData.isEmpty()) {
            log.info("::{}::增加数据源配置没有找到模板信息", CommonUtil.getRequestId());
            return;
        }
        rcsTournamentTemplateListData.forEach(rcsTournamentTemplate -> {
            Map<String, Integer> map = JSON.parseObject(rcsTournamentTemplate.getDataSourceCode(), LinkedHashMap.class, Feature.OrderedField);
            map.put(key, val);
            rcsTournamentTemplate.setDataSourceCode(JSONObject.toJSONString(map));
            rcsTournamentTemplateMapper.updateById(rcsTournamentTemplate);
        });
    }

    @Override
    public void updateTemplateAoConfigData() {
        //先查询所有的数据
        QueryWrapper<RcsTournamentTemplate> templateQueryWrapperData = new QueryWrapper();
        //查询赛事和联赛模板
        templateQueryWrapperData.lambda().eq(RcsTournamentTemplate::getSportId, 1).in(RcsTournamentTemplate::getType, 1, 2);
        List<RcsTournamentTemplate> rcsTournamentTemplateListData = rcsTournamentTemplateMapper.selectList(templateQueryWrapperData);
        if (rcsTournamentTemplateListData.isEmpty()) {
            //log.info("修改模板AO配置没有找到模板信息");
            return;
        }
        rcsTournamentTemplateListData.forEach(s -> {
            List<Map<String, Object>> jsonArray = JSON.parseObject(s.getAoConfigValue(), new TypeReference<List<Map<String, Object>>>() {
            });
            for (Map<String, Object> map : jsonArray) {
                if (StringUtils.equals(String.valueOf(map.get("tempType")), "ex_goal") || StringUtils.equals(String.valueOf(map.get("tempType")), "ex_corner")) {
                    map.put("perId", 15);
                    map.put("oneInjTime", 1);
                    map.put("twoInjTime", 1);
                }
            }
            s.setAoConfigValue(JSON.toJSONString(jsonArray));
            rcsTournamentTemplateMapper.updateById(s);
        });
    }

    private boolean isContains(JSONArray jsonArray) {
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            if (jsonObject.getString("name").equals("AO")) {
                return true;
            }
        }
        return false;
    }

    private void sendJumpConfig(RcsTournamentTemplateJumpConfig obj) {
        //综合操盘跳分设置最大投注最大赔付
        String key = String.format("%s_%s", obj.getTournamentId(), obj.getMatchType());
        redisClient.set(TEMPLATE_TOURNAMENT_AMOUNT + key, String.valueOf(obj.getMaxSingleBetAmount()));
        //综合操盘下发赔率给融合
        try {
            ConfigTournamentTradeItemDTO dto = new ConfigTournamentTradeItemDTO();
            BeanCopyUtils.copyProperties(obj, dto);
            //马来转成欧赔推送至融合
            dto.setSpreadMinOdds(new BigDecimal(rcsOddsConvertMappingService.getEUOdds(obj.getSpreadMinOdds().toPlainString())));
            dto.setSpreadMaxOdds(new BigDecimal(rcsOddsConvertMappingService.getEUOdds(obj.getSpreadMaxOdds().toPlainString())));
            DataRealtimeApiUtils.handleApi(dto, new DataRealtimeApiUtils.ApiCall() {
                @Override
                @Trace
                public <R> Response<R> callApi(Request request) {
                    return tradeMarketConfigApi.putConfigTournamentTradeItem(request);
                }
            });
        } catch (IllegalArgumentException ex) {
            log.error("融合服务异常:{} *************** 联赛模板跳分数据：{}", ex.getMessage(), JSONObject.toJSONString(obj));
        }
    }
}