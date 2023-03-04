package com.panda.sport.rcs.trade.service;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.BeanUtils;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.panda.sport.data.rcs.api.ExceptionData;
import com.panda.sport.data.rcs.api.RcsMarketCategorySetApiService;
import com.panda.sport.data.rcs.api.Request;
import com.panda.sport.data.rcs.api.Response;
import com.panda.sport.data.rcs.dto.MarketCategoryCetBean;
import com.panda.sport.rcs.common.CategorySetMargin;
import com.panda.sport.rcs.constants.BaseConstants;
import com.panda.sport.rcs.constants.RedisKeys;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.log.annotion.NotWriteLog;
import com.panda.sport.rcs.log.annotion.monnitor.MonitorAnnotion;
import com.panda.sport.rcs.mapper.MarketCategorySetMapper;
import com.panda.sport.rcs.pojo.RcsLanguageInternation;
import com.panda.sport.rcs.pojo.RcsMarketCategorySet;
import com.panda.sport.rcs.pojo.RcsMarketCategorySetMargin;
import com.panda.sport.rcs.pojo.StandardSportMarketCategory;
import com.panda.sport.rcs.pojo.dto.CategoryCacheDataDTO;
import com.panda.sport.rcs.pojo.dto.TradeCacheDataDTO;
import com.panda.sport.rcs.trade.cache.CategoryCache;
import com.panda.sport.rcs.trade.cache.TradeDataCache;
import com.panda.sport.rcs.trade.wrapper.DataSyncService;
import com.panda.sport.rcs.trade.wrapper.MarketCategorySetService;
import com.panda.sport.rcs.trade.base.AbstractApiService;
import com.panda.sport.rcs.trade.wrapper.RcsLanguageInternationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author :  Felix
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.rpc.service.impl
 * @Description :  TODO
 * @Date: 2019-10-03 13:18
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Service
@org.springframework.stereotype.Service
@Slf4j
public class RcsMarketCategorySetApiImpl extends AbstractApiService<Long> implements RcsMarketCategorySetApiService {

    @Resource(name = "marketCategorySetService")
    DataSyncService dataSyncService;

    @Autowired
    MarketCategorySetService marketCategorySetService;

    @Autowired
    private RedisClient redisClient;

    @Autowired
    MarketCategorySetMapper marketCategorySetMapper;

    @Autowired
    private RcsLanguageInternationService rcsLanguageInternationService;

    @Override
    @MonitorAnnotion(code = "RPC_CATEGORY_SET")
    public Response putSportMarketCategorySet(Request<MarketCategoryCetBean> requestParam) {
        if (requestParam.getData() == null) {
            return Response.success(null);
        }
        if (requestParam.getData().getSportId() == null) {
            requestParam.getData().setSportId(1L);
        }
        log.info("::{}::putSportMarketCategorySet:{}",requestParam.getGlobalId(),JSONObject.toJSONString(requestParam.getData()));
        //玩法集缓存设置
        String cacheKey = String.format(RedisKeys.RCSCACHE_MARKET_CATEGORY_SET, requestParam.getData().getSportId());
        String obj = redisClient.get(cacheKey);
        List<RcsMarketCategorySet> playSet = JsonFormatUtils.fromJsonArray(obj, RcsMarketCategorySet.class);
        if (CollectionUtils.isEmpty(playSet)) {
            playSet = marketCategorySetService.findCategorySetSyncList(requestParam.getData());
            //写入缓存
            redisClient.setExpiry(cacheKey, JsonFormatUtils.toJson(playSet), BaseConstants.CACHE_EXPIRE_SECONDS);
        }

        //玩法集多语言缓存设置
        String cacheLanguageKey = String.format("rcs:market_category_set_language:%s", requestParam.getData().getSportId());
        String cacheMap = redisClient.get(cacheLanguageKey);
        Map languageMap = JSONObject.parseObject(cacheMap);
        if (CollectionUtils.isEmpty(languageMap)) {
            //根据编码，获取多语言
            List<String> nameCodes = playSet.stream().map(RcsMarketCategorySet::getNameCode).filter(nameCode -> nameCode != null).collect(Collectors.toList());
            List<RcsLanguageInternation> language = rcsLanguageInternationService.getLanguageInternationByCode(nameCodes);
            if (CollectionUtils.isNotEmpty(language)) {
                languageMap = language.stream().collect(Collectors.toMap(RcsLanguageInternation::getNameCode, o -> o.getText() == null ? "0" : o.getText()));
            }
            //写入缓存
            redisClient.setExpiry(cacheLanguageKey, JsonFormatUtils.toJson(languageMap), BaseConstants.CACHE_EXPIRE_SECONDS);
        }

        RcsMarketCategorySet rcsMarketCategorySet = new RcsMarketCategorySet();
        rcsMarketCategorySet.setId(new Long(0));
        Map<String, String> map = Maps.newHashMap();
        map.put("zs", "所有投注");
        map.put("en", "All");
        rcsMarketCategorySet.setMarketName(JSONObject.toJSONString(map));
        List<RcsMarketCategorySet> playSetList = Lists.newArrayList();
        playSetList.add(rcsMarketCategorySet);
        if (CollectionUtils.isNotEmpty(playSet)) {
            for (RcsMarketCategorySet se : playSet) {
                //设置多语言
                if (StringUtils.isNotBlank(se.getNameCode()) && ObjectUtil.isNotNull(languageMap)) {
                    String o = String.valueOf(languageMap.get(se.getNameCode()));
//                    JSONObject jsonObject = JSONObject.parseObject(o);
                    se.setMarketName(o);
                } else {
                    Map<String, String> a = Maps.newHashMap();
                    a.put("zs", se.getName());
                    se.setMarketName(JSONObject.toJSONString(a));
                }
                playSetList.add(se);
            }
        }
        return Response.success(playSetList);
    }

    @Override
    @MonitorAnnotion(code = "RPC_CATEGORY_SET_PLAY")
    public Response putSportMarketCategorySetPlay(Request<Long> requestParam) {
        Object obj = requestParam.getData();
        log.info("::{}::putSportMarketCategorySetPlay:{}",requestParam.getGlobalId(),JSONObject.toJSONString(requestParam.getData()));
        Integer setId = 0;
        if (obj != null) {
            setId = Integer.valueOf(obj.toString());
        }
        List<StandardSportMarketCategory> play;
        //新增玩法集缓存十分钟
        String category_key = String.format("rcs:category:set:play:%s",setId);
        CategoryCacheDataDTO category_cache_obj = CategoryCache.getCategoryCache(category_key);
        if(Objects.nonNull(category_cache_obj)){
            if(System.currentTimeMillis() - category_cache_obj.getCreateTime() > TimeUnit.MINUTES.toMillis(10)){
                CategoryCache.categoryMap.remove(category_key);
                category_cache_obj = null;
            }
        }
        if(Objects.nonNull(category_cache_obj)){
            play = JSONArray.parseArray(category_cache_obj.getCacheValue(),StandardSportMarketCategory.class);
        }else{
            play = marketCategorySetService.findMarketCategoryContent(setId);
            CategoryCache.categoryMap.put(category_key, new CategoryCacheDataDTO(JSONArray.toJSONString(play), System.currentTimeMillis()));
        }
        return Response.success(play);
    }
    @Override
    @NotWriteLog
    public Response getSportMarketCategorySetMargin(Request<MarketCategoryCetBean> requestParam) {
        MarketCategoryCetBean obj = requestParam.getData();
        Long categoryId = obj.getPlayId() != null ? Long.valueOf(obj.getPlayId().toString()) : 0L;

        String val = redisClient.hGet(RedisKeys.MARGAIN_CACHE_KEY, String.format("margain_%s_%s", obj.getSportId(), categoryId));
        LinkedHashMap<String, RcsMarketCategorySetMargin> findMargin = null;
        if (StringUtils.isBlank(val)) {
            findMargin = marketCategorySetService.findMargin(obj);
            redisClient.hSet(RedisKeys.MARGAIN_CACHE_KEY, String.format("margain_%s_%s", obj.getSportId(), categoryId), JSONObject.toJSONString(findMargin));
        } else {
            HashMap<String, RcsMarketCategorySetMargin> map = JSONObject.parseObject(val, new TypeReference<HashMap<String, RcsMarketCategorySetMargin>>() {
            });

            findMargin = new LinkedHashMap<>(6);
            List<RcsMarketCategorySetMargin> list = new ArrayList<RcsMarketCategorySetMargin>(map.values());
            Collections.sort(list, (a, b) -> b.getTimeFrame().compareTo(a.getTimeFrame()));
            for (RcsMarketCategorySetMargin margin : list) {
                findMargin.put(CategorySetMargin.getTimeFrame(margin.getTimeFrame()), margin);
            }
        }

        return Response.success(findMargin);
    }

    @Override
    public Response putSportMarketCategoryBySportId(Request<Long> requestParam) {
        if (requestParam.getData() == null) {
            throw new RcsServiceException("调用风控api操作异常：体育种类ID不能为空！");
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("sportId", requestParam.getData());
        params.put("type", 0);
        params.put("status", 2);
        List<Map<String, Object>> playList = marketCategorySetMapper.queryAllCategoryListBySportId(params);
        List<Long> playIds = playList.stream().map(map -> Long.parseLong(String.valueOf(map.get("playId")))).collect(Collectors.toList());
        return Response.success(playIds);
    }

    @Override
    protected List<ExceptionData> validation(Long requestData) {
        return null;
    }

    @Override
    protected Map<String, String> doRequest(Long requestData) {
        return dataSyncService.receive(requestData);
    }

    @Override
    protected void notifyDownstreamSystem(Long requestData) {

    }
}
