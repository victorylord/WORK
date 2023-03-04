package com.panda.sport.rcs.trade.service;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.github.pagehelper.util.StringUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.panda.sport.rcs.constants.RedisKey;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.core.utils.BeanCopyUtils;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.mapper.RcsMatchEventTypeInfoMapper;
import com.panda.sport.rcs.mapper.RcsTournamentTemplateAcceptConfigAutoChangeMapper;
import com.panda.sport.rcs.mapper.RcsTournamentTemplateAcceptConfigMapper;
import com.panda.sport.rcs.mapper.StandardMatchInfoMapper;
import com.panda.sport.rcs.mapper.tourTemplate.RcsTournamentTemplateAcceptConfigSettleMapper;
import com.panda.sport.rcs.mapper.tourTemplate.RcsTournamentTemplateAcceptEventMapper;
import com.panda.sport.rcs.mapper.tourTemplate.RcsTournamentTemplateAcceptEventSettleMapper;
import com.panda.sport.rcs.mapper.tourTemplate.RcsTournamentTemplateMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.RcsMatchEventTypeInfo;
import com.panda.sport.rcs.pojo.RcsStandardSportMarketSell;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.pojo.tourTemplate.*;
import com.panda.sport.rcs.trade.enums.MatchEventEnum;
import com.panda.sport.rcs.pojo.vo.ThirdDataSourceCodeVo;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.RcsStandardSportMarketSellService;
import com.panda.sport.rcs.trade.wrapper.tourTemplate.IRcsMatchTemplateModifyService;
import com.panda.sport.rcs.trade.wrapper.tourTemplate.TournamentTemplatePushService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description //事件信息处理服务类
 * @Param
 * @Author Sean
 * @Date 20:34 2020/9/4
 * @return
 **/
@Service
@Slf4j
public class RcsMatchEventTypeInfoServiceImpl {

    @Autowired
    RcsMatchEventTypeInfoMapper rcsMatchEventTypeInfoMapper;
    @Autowired
    RcsTournamentTemplateAcceptConfigMapper rcsTournamentTemplateAcceptConfigMapper;
    @Autowired
    RcsTournamentTemplateAcceptEventMapper rcsTournamentTemplateAcceptEventMapper;
    @Autowired
    StandardMatchInfoMapper standardMatchInfoMapper;
    @Autowired
    IRcsMatchTemplateModifyService rcsMatchTemplateModifyService;
    @Autowired
    RcsTournamentTemplateMapper rcsTournamentTemplateMapper;
    @Autowired
    private RedisClient redisClient;
    @Autowired
    private RcsStandardSportMarketSellService rcsStandardSportMarketSellService;
    @Autowired
    RcsTournamentTemplateAcceptConfigSettleMapper configSettleMapper;
    @Autowired
    RcsTournamentTemplateAcceptEventSettleMapper eventSettleMapper;
    @Autowired
    TournamentTemplatePushService templatePushService;
    @Autowired
    private RcsTournamentTemplateAcceptConfigAutoChangeMapper templateAcceptConfigAutoChangeMapper;
    @Autowired
    private ProducerSendMessageUtils producerSendMessageUtils;
    private static String MATCH_EVENT_REDIS_KEY = "rcs:match:event:play:collection:%s";
    //自动接距redis_key 赛事id和玩法id拼接
    private static String MATCH_PLAY_SECOND_REDIS_KEY = "rcs:match:play:second:redis:key:%s:%s";

    private static Long MATCH_EVENT_WAIT_TIME = 2 * 60 * 60L;
    private static Integer DEFAULT_EVENT_PAGE_SIZE = 1000;

    /**
     * @return com.baomidou.mybatisplus.core.metadata.IPage<java.util.Map < java.lang.String, java.lang.Object>>
     * @Description //分页查询事件
     * @Param [info]
     * @Author Sean
     * @Date 16:13 2020/9/6
     **/
    public Map<String, Object> list(RcsMatchEventTypeInfo info,String lang) {
//        QueryWrapper<RcsMatchEventTypeInfo> queryWrapper = new QueryWrapper();
//        queryWrapper.lambda().eq(RcsMatchEventTypeInfo ::getSportId,info.getSportId());
//        queryWrapper.lambda().orderBy(true,true,RcsMatchEventTypeInfo ::getDataSourceCode,RcsMatchEventTypeInfo ::getEventCode);
//        Page<RcsMatchEventTypeInfo> page = new Page<>(NumberUtils.INTEGER_,Integer.MAX_VALUE);

        setEventPage(info);

        Map<String, Object> data = Maps.newHashMap();

        Integer count = rcsMatchEventTypeInfoMapper.selectEventPagesCount(info);
        List<RcsMatchEventTypeInfo> list = Lists.newArrayList();
        if (NumberUtils.INTEGER_ZERO.intValue() < count) {
            list = rcsMatchEventTypeInfoMapper.selectEventPages(info);
            if(lang.equals("en")){
                list.forEach(o->{
                    if(StringUtil.isNotEmpty(o.getEventEnName())){
                        o.setEventName(o.getEventEnName());
                    }
                });
            }
        }
        data.put("records", list);
        data.put("total", count);
        data.put("current", info.getPageNumber());
        return data;
    }

    /**
     * @return void
     * @Description //设置分页信息
     * @Param [info]
     * @Author sean
     * @Date 2020/11/10
     **/
    private void setEventPage(RcsMatchEventTypeInfo info) {
        if (ObjectUtils.isEmpty(info.getPageNumber()) || ObjectUtils.isEmpty(info.getPageSize())) {
            info.setPageNumber(NumberUtils.INTEGER_ONE);
            info.setPageSize(DEFAULT_EVENT_PAGE_SIZE);
        }
    }

    /**
     * @return void
     * @Description //更新事件备注
     * @Param [info]
     * @Author Sean
     * @Date 16:14 2020/9/6
     **/
    @Transactional(rollbackFor = Exception.class)
    public void updateById(RcsMatchEventTypeInfo info) {
        try {
            rcsMatchEventTypeInfoMapper.updateMatchEventById(info);
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(),e.getMessage(), e);
            throw new RcsServiceException("更新失败");
        }
    }

    /**
     * @return com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateAcceptConfig
     * @Description //查询事件源配置
     * @Param [config]
     * @Author Sean
     * @Date 16:14 2020/9/6
     **/
    public RcsTournamentTemplateAcceptConfig queryDataSourceConfig(RcsTournamentTemplateAcceptConfig config) {
        QueryWrapper<RcsTournamentTemplateAcceptConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(RcsTournamentTemplateAcceptConfig::getCategorySetId, config.getCategorySetId());
        queryWrapper.lambda().eq(RcsTournamentTemplateAcceptConfig::getTemplateId, config.getTemplateId());
        RcsTournamentTemplateAcceptConfig acceptConfig = rcsTournamentTemplateAcceptConfigMapper.selectOne(queryWrapper);
        if (ObjectUtil.isNotNull(config.getMatchId())) {
            //接拒单未初始化数据，特殊处理，指定赛事主数据源
            if (ObjectUtil.isNull(acceptConfig)) {
                acceptConfig = new RcsTournamentTemplateAcceptConfig();
                RcsStandardSportMarketSell marketSell = rcsStandardSportMarketSellService.selectStandardMarketSellVo(config.getMatchId());
                acceptConfig.setDataSource(marketSell.getBusinessEvent());
            } else {
                //只在第一次的时候做初始化，更新的时候在重新设置值
                if (acceptConfig.getNormal() == 0) {
                    //查询数据库
                    List<String> playList = rcsTournamentTemplateMapper.queryGamePlay(config.getSportId(), config.getCategorySetId());
                    RcsTournamentTemplateTempConfig rcsTournamentTemplateTempConfig=new RcsTournamentTemplateTempConfig();
                    rcsTournamentTemplateTempConfig.setNormal(config.getNormal());
                    log.info("::{}::查询事件源配置数据内容:{}",JSONObject.toJSONString(playList), CommonUtil.getRequestId(config.getMatchId()));
                    playList.forEach(s -> {
                        String key = String.format(MATCH_PLAY_SECOND_REDIS_KEY, config.getMatchId(), s);
                        if(!redisClient.exist(key)){
                            redisClient.setExpiry(key, JSONObject.toJSONString(rcsTournamentTemplateTempConfig), 1000 * 60 * 60 * 24 * 3L);
                        }
                    });
                }
            }
            //获取赛事所关联的商业数据源
            StandardMatchInfo info = standardMatchInfoMapper.selectById(config.getMatchId());
            if (StringUtils.isNotBlank(info.getThirdMatchListStr())) {
                List<ThirdDataSourceCodeVo> list = JSONArray.parseArray(info.getThirdMatchListStr(), ThirdDataSourceCodeVo.class);
                //新版本代码
                List<ThirdDataSourceCodeVo> filterList = list.stream().filter(filter -> (filter.getCommerce().equals(String.valueOf(NumberUtils.INTEGER_ONE)) && filter.getEventSupport().equals(String.valueOf(NumberUtils.INTEGER_ONE))) || filter.getDataSourceCode().equals("PD")).collect(Collectors.toList());
                acceptConfig.setThirdDataSourceCode(filterList);
            }
        }
        return acceptConfig;
    }

    /**
     * @return java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     * @Description //查询事件类型配置
     * @Param [config]
     * @Author Sean
     * @Date 16:14 2020/9/6
     **/
    public Map<String, Object> queryEventConfig(RcsTournamentTemplateAcceptEvent config) {
        Map<String, Object> events = Maps.newHashMap();
        List<RcsTournamentTemplateAcceptEvent> list = rcsTournamentTemplateAcceptEventMapper.queryMatchEventConfig(config);
        if (CollectionUtils.isNotEmpty(list)) {
            Map<String, List<RcsTournamentTemplateAcceptEvent>> maps = list.stream().collect(Collectors.groupingBy(RcsTournamentTemplateAcceptEvent::getEventType));
            for (Map.Entry<String, List<RcsTournamentTemplateAcceptEvent>> m : maps.entrySet()) {
                Map<String, Object> map = Maps.newHashMap();
                map.put("data", m.getValue());
                map.put("count", m.getValue().size());
                events.put(m.getKey(), map);
            }
        }
        return events;
    }

    /**
     * @return java.util.List<com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateAcceptEvent>
     * @Description //根据事件类型查询事件
     * @Param [config]
     * @Author Sean
     * @Date 16:15 2020/9/6
     **/
    public Map<String, Object> queryEventConfigByType(RcsTournamentTemplateAcceptEvent config) {
        Map<String, Object> map = Maps.newHashMap();
        List<RcsTournamentTemplateAcceptEvent> list = rcsTournamentTemplateAcceptEventMapper.queryMatchEventConfig(config);
        if (CollectionUtils.isNotEmpty(list)) {
            map.put("data", list);
            map.put("count", list.size());
        }
        return map;
    }

    /**
     * @return void
     * @Description //更新事件等待时间和事件类型配置
     * @Param [config]
     * @Author Sean
     * @Date 16:15 2020/9/6
     **/
    @Transactional(rollbackFor = Exception.class)
    public void updateEventAndTimeConfig(RcsTournamentTemplateAcceptConfig config) {
        log.info("::{}::修改事件配置入参{}", JSONObject.toJSONString(config),CommonUtil.getRequestId(config.getMatchId()));
        VerificationData(config);
        boolean isOpeUpdaten = true;
        if (ObjectUtils.isEmpty(config.getId())) {
            rcsTournamentTemplateAcceptConfigMapper.insert(config);
        } else {
        	RcsTournamentTemplateAcceptConfig selectById = rcsTournamentTemplateAcceptConfigMapper.selectById(config.getId());
			// 如果传入的数据源与原有的一致，不必进行更新
			if (config.getDataSource().equalsIgnoreCase(selectById.getDataSource())) {
				isOpeUpdaten = false;
			}
            rcsTournamentTemplateAcceptConfigMapper.updateMatchDataSourceAndTimeConfig(config);
            //修改数据源，结算数据源联动修改
            QueryWrapper<RcsTournamentTemplateAcceptConfigSettle> copyAcceptQueryWrapper = new QueryWrapper<>();
            copyAcceptQueryWrapper.lambda().eq(RcsTournamentTemplateAcceptConfigSettle::getTemplateId, config.getTemplateId())
                    .eq(RcsTournamentTemplateAcceptConfigSettle::getCategorySetId, config.getCategorySetId());
            RcsTournamentTemplateAcceptConfigSettle configSettle = configSettleMapper.selectOne(copyAcceptQueryWrapper);
            if (ObjectUtils.isNotEmpty(configSettle) && !config.getDataSource().equals(configSettle.getDataSource())) {
                configSettle.setDataSource(config.getDataSource());
                configSettleMapper.updateMatchDataSourceAndTimeConfigSettle(configSettle);

                //kir-1788-当前使用结算数据源缓存
                RcsTournamentTemplate rcsTournamentTemplate = rcsTournamentTemplateMapper.selectById(config.getTemplateId());
                if(ObjectUtils.isNotEmpty(rcsTournamentTemplate)){
                    String rcsTournamentTemplateAcceptAutoChangeKey = RedisKey.getDataLiveMatchSettleDatasource(rcsTournamentTemplate.getTypeVal(), Long.valueOf(config.getCategorySetId()));
                    log.info("::{}::kir-1788-保存接口-开始录入结算数据源缓存:{},{}",CommonUtil.getRequestId(config.getMatchId()), rcsTournamentTemplate.getTypeVal(), Long.valueOf(config.getCategorySetId()));
                    redisClient.setExpiry(rcsTournamentTemplateAcceptAutoChangeKey, config.getDataSource(), 3600 * 24L);
                }
            }
        }

        if (isOpeUpdaten) {
        	//kir-1788-当前使用接拒数据源缓存
        	RcsTournamentTemplate rcsTournamentTemplate = rcsTournamentTemplateMapper.selectById(config.getTemplateId());
        	if(ObjectUtils.isNotEmpty(rcsTournamentTemplate)){
        		String rcsTournamentTemplateAcceptAutoChangeKey = RedisKey.getDataLiveMatchConfigDatasource(rcsTournamentTemplate.getTypeVal(), Long.valueOf(config.getCategorySetId()));
        		log.info("::{}::kir-1788-保存接口-开始录入接拒数据源缓存:{},{}",CommonUtil.getRequestId(config.getMatchId()), rcsTournamentTemplate.getTypeVal(), Long.valueOf(config.getCategorySetId()));
        		redisClient.setExpiry(rcsTournamentTemplateAcceptAutoChangeKey, config.getDataSource(), 3600 * 24L);
        	}

        	//kir-1788-赛事模板自动接拒开关（0.关 1.开）
        	String rcsTournamentTemplateAcceptAutoChangeKey = RedisKey.getRcsTournamentTemplateAcceptAutoChangeKey(rcsTournamentTemplate.getTypeVal(), Long.valueOf(config.getCategorySetId()));
        	log.info("::{}::kir-1788-保存接口-关闭自动接拒开关缓存:{},{}",CommonUtil.getRequestId(config.getMatchId()), rcsTournamentTemplate.getTypeVal(), config.getCategorySetId());
        	redisClient.setExpiry(rcsTournamentTemplateAcceptAutoChangeKey, 0, 3600 * 24L);
        	//手动操作则关闭开关
        	QueryWrapper<RcsTournamentTemplateAcceptConfigAutoChange> autoChangeQueryWrapper = new QueryWrapper<>();
        	autoChangeQueryWrapper.eq("template_id", rcsTournamentTemplate.getId());
        	autoChangeQueryWrapper.eq("category_set_id", config.getCategorySetId());
        	RcsTournamentTemplateAcceptConfigAutoChange rcsTournamentTemplateAcceptConfigAutoChange = templateAcceptConfigAutoChangeMapper.selectOne(autoChangeQueryWrapper);
        	if(ObjectUtils.isNotEmpty(rcsTournamentTemplateAcceptConfigAutoChange)){
        		rcsTournamentTemplateAcceptConfigAutoChange.setIsOpen(0);
        		templateAcceptConfigAutoChangeMapper.updateById(rcsTournamentTemplateAcceptConfigAutoChange);
        	}else{
        		rcsTournamentTemplateAcceptConfigAutoChange= new RcsTournamentTemplateAcceptConfigAutoChange();
        		rcsTournamentTemplateAcceptConfigAutoChange.setIsOpen(0);
        		rcsTournamentTemplateAcceptConfigAutoChange.setTemplateId(rcsTournamentTemplate.getTypeVal());
        		rcsTournamentTemplateAcceptConfigAutoChange.setCategorySetId(Long.valueOf(config.getCategorySetId()));
        		templateAcceptConfigAutoChangeMapper.insert(rcsTournamentTemplateAcceptConfigAutoChange);
        	}
        	
        }

        if (ObjectUtils.isNotEmpty(config.getEvents())) {
            for (RcsTournamentTemplateAcceptEvent event : config.getEvents()) {
                event.setAcceptConfigId(config.getId());
            }
            rcsTournamentTemplateAcceptEventMapper.updateMatchEventConfig(config.getEvents());
            this.updateEventListToRedis(config);
        }
        //查询数据库
        List<String> playList = rcsTournamentTemplateMapper.queryGamePlay(config.getSportId(), config.getCategorySetId());
        RcsTournamentTemplateTempConfig rcsTournamentTemplateTempConfig=new RcsTournamentTemplateTempConfig();
        rcsTournamentTemplateTempConfig.setNormal(config.getNormal());
        log.info("::{}::修改事件源配置数据内容:{}",CommonUtil.getRequestId(config.getMatchId()),JSONObject.toJSONString(playList));
        playList.forEach(s -> {
            //直接从缓存先获取
            String key = String.format(MATCH_PLAY_SECOND_REDIS_KEY, config.getMatchId(), s);
            redisClient.setExpiry(key, JSONObject.toJSONString(rcsTournamentTemplateTempConfig), 1000 * 60 * 60 * 24 * 3L);
        });
    }

    /**
     * @return
     * @Description //校验数据源时间设置
     * @Param []
     * @Author Sean
     * @Date 9:36 2020/9/8
     **/
    private void VerificationData(RcsTournamentTemplateAcceptConfig config) {
        /**校验一个事件被分到不同的事件类型*/
        List<String> msgList = Lists.newArrayList();
        if (ObjectUtils.isNotEmpty(config.getEvents())) {
            for (RcsTournamentTemplateAcceptEvent event : config.getEvents()) {
                for (RcsTournamentTemplateAcceptEvent e : config.getEvents()) {
                    if (event.getEventCode().equalsIgnoreCase(e.getEventCode()) &&
                            !event.getEventType().equalsIgnoreCase(e.getEventType()) &&
                            e.getStatus().intValue() == NumberUtils.INTEGER_ONE.intValue() &&
                            event.getStatus().intValue() == NumberUtils.INTEGER_ONE.intValue()) {
                        msgList.add(event.getEventName());
                        break;
                    }
                }
            }
        }
        eventValidate(msgList, config.getNormal(), config.getMinWait(), config.getMaxWait());
    }

    /**
     * @return void
     * @Description //初始化赛事事件
     * @Param [config]
     * @Author Sean
     * @Date 16:16 2020/9/6
     **/
    @Transactional(rollbackFor = Exception.class)
    public void insertEventList(RcsTournamentTemplateAcceptConfig config) {
        rcsTournamentTemplateAcceptConfigMapper.insert(config);
        if (CollectionUtils.isNotEmpty(config.getEvents())) {
            for (RcsTournamentTemplateAcceptEvent event : config.getEvents()) {
                event.setAcceptConfigId(config.getId());
            }
            rcsTournamentTemplateAcceptEventMapper.updateMatchEventConfig(config.getEvents());
            this.updateEventListToRedis(config);
        }
    }

    /**
     * @return void
     * @Description //删除事件配置
     * @Param [config]
     * @Author Sean
     * @Date 17:46 2020/9/16
     **/
    public void deleteEventList(RcsTournamentTemplateAcceptConfig config) {
        log.info("::{}::删除redis配置和数据库配置",CommonUtil.getRequestId(config.getTemplateId()));
        QueryWrapper<RcsTournamentTemplateAcceptConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(RcsTournamentTemplateAcceptConfig::getTemplateId, config.getTemplateId());
        List<RcsTournamentTemplateAcceptConfig> list = rcsTournamentTemplateAcceptConfigMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(list)) {
            List<Long> ids = Lists.newArrayList();
            list.forEach(e -> ids.add(e.getId()));
            rcsTournamentTemplateAcceptConfigMapper.delete(queryWrapper);
            QueryWrapper<RcsTournamentTemplateAcceptEvent> wrapper = new QueryWrapper<>();
            wrapper.lambda().in(RcsTournamentTemplateAcceptEvent::getAcceptConfigId, ids);
            rcsTournamentTemplateAcceptEventMapper.delete(wrapper);
            this.deleteEventListToRedis(config);
        }
    }

    /**
     * @return void
     * @Description //事件配置保存到redis
     * @Param [config]
     * @Author Sean
     * @Date 16:16 2020/9/6
     **/
    private void updateEventListToRedis(RcsTournamentTemplateAcceptConfig config) {
        this.deleteEventListToRedis(config);
        /*if (ObjectUtils.isNotEmpty(config) && CollectionUtils.isNotEmpty(config.getEvents())){
            String key = String.format(MATCH_EVENT_REDIS_KEY,config.getMatchId());
            log.info("清除缓存key={}",key);
            redisClient.delete(key);
        }*/
    }

    /**
     * @return void
     * @Description //删除redis缓存
     * @Param [config]
     * @Author Sean
     * @Date 19:27 2020/9/16
     **/
    public void deleteEventListToRedis(RcsTournamentTemplateAcceptConfig config) {
        log.info("::{}::更新缓存数据：{}",CommonUtil.getRequestId(config.getMatchId()), JSONObject.toJSONString(config));
        //去查询一遍数据库
        List<RcsTournamentTemplateAcceptConfigNew> configNewList=rcsTournamentTemplateAcceptEventMapper.selectOrderAcceptConfig(config.getMatchId());
        configNewList.forEach(s -> {
            String redisKey = String.format(RedisKey.MATCH_EVENT_REDIS_KEY, s.getMatchId(),s.getCategorySetId());
            this.sendCacheMsg(redisKey,"1");
        });
    }
    /**
     * 发送到接距服务做及时更新配置
     * @param key key
     * @param value val
     */
    private void sendCacheMsg(String key,Object value){
        JSONObject json = new JSONObject();
        json.put("key", key);
        json.put("value", value);
        producerSendMessageUtils.sendMessage("rcs_order_reject_cache_update", "", key, json);
    }

    /**
     * @Description 接拒单玩法集事件复制功能
     * @Author carver
     * @Date 15:11 2021/1/8
     **/
    @Transactional(rollbackFor = Exception.class)
    public void copyEventAndTimeConfig(RcsTournamentTemplateAcceptConfig config) {
        log.info("::{}::复制玩法集事件配置-入参：{}",CommonUtil.getRequestId(), JSONObject.toJSONString(config));
        QueryWrapper<RcsTournamentTemplateAcceptConfig> copyAcceptQueryWrapper = new QueryWrapper<>();
        copyAcceptQueryWrapper.lambda().eq(RcsTournamentTemplateAcceptConfig::getTemplateId, config.getTemplateId())
                .eq(RcsTournamentTemplateAcceptConfig::getCategorySetId, config.getCopyCategorySetId());
        RcsTournamentTemplateAcceptConfig copyConfig = rcsTournamentTemplateAcceptConfigMapper.selectOne(copyAcceptQueryWrapper);
        log.info("::{}::复制玩法集事件配置-时间：{}",CommonUtil.getRequestId(), JSONObject.toJSONString(copyConfig));
        if (ObjectUtils.isEmpty(copyConfig)) {
            throw new RcsServiceException("未找到【" + config.getCopyCategorySetName() + "】玩法集配置！");
        }
        Long copyConfigId = copyConfig.getId();
        //更新接拒时间配置
        QueryWrapper<RcsTournamentTemplateAcceptConfig> acceptQueryWrapper = new QueryWrapper<>();
        acceptQueryWrapper.lambda().eq(RcsTournamentTemplateAcceptConfig::getTemplateId, config.getTemplateId())
                .eq(RcsTournamentTemplateAcceptConfig::getCategorySetId, config.getCategorySetId());
        RcsTournamentTemplateAcceptConfig acceptConfig = rcsTournamentTemplateAcceptConfigMapper.selectOne(acceptQueryWrapper);
        Long acceptConfigId;
        if (ObjectUtils.isNotEmpty(acceptConfig)) {
            acceptConfigId = acceptConfig.getId();
            BeanCopyUtils.copyProperties(copyConfig, acceptConfig);
            acceptConfig.setId(acceptConfigId);
            rcsTournamentTemplateAcceptConfigMapper.updateMatchDataSourceAndTimeConfig(acceptConfig);
        } else {
            copyConfig.setId(null);
            copyConfig.setCategorySetId(config.getCategorySetId());
            rcsTournamentTemplateAcceptConfigMapper.insert(copyConfig);
            acceptConfigId = copyConfig.getId();
        }

        //更新接拒事件配置
        QueryWrapper<RcsTournamentTemplateAcceptEvent> eventQueryWrapper = new QueryWrapper<>();
        eventQueryWrapper.lambda().eq(RcsTournamentTemplateAcceptEvent::getAcceptConfigId, copyConfigId);
        List<RcsTournamentTemplateAcceptEvent> copyEventList = rcsTournamentTemplateAcceptEventMapper.selectList(eventQueryWrapper);
        log.info("::{}::复制玩法集事件配置-事件：{}",CommonUtil.getRequestId(), JSONObject.toJSONString(copyEventList));
        if (CollectionUtils.isNotEmpty(copyEventList)) {
            //删除现有玩法集事件配置
            if (ObjectUtils.isNotEmpty(acceptConfig)) {
                QueryWrapper<RcsTournamentTemplateAcceptEvent> deleteWrapper = new QueryWrapper<>();
                deleteWrapper.lambda().eq(RcsTournamentTemplateAcceptEvent::getAcceptConfigId, acceptConfigId);
                rcsTournamentTemplateAcceptEventMapper.delete(deleteWrapper);
            }
            //复制玩法集的接拒事件配置
            copyEventList.forEach(e -> {
                e.setAcceptConfigId(acceptConfigId);
            });
            rcsTournamentTemplateAcceptEventMapper.updateMatchEventConfig(copyEventList);
        }
        //接拒单结算玩法集事件复制功能
        copyEventAndTimeConfigSettle(config);
    }

    /**
     * 接拒单结算玩法集事件复制功能
     *
     * @param config
     */
    public void copyEventAndTimeConfigSettle(RcsTournamentTemplateAcceptConfig config) {
        QueryWrapper<RcsTournamentTemplateAcceptConfigSettle> copyAcceptQueryWrapper = new QueryWrapper<>();
        copyAcceptQueryWrapper.lambda().eq(RcsTournamentTemplateAcceptConfigSettle::getTemplateId, config.getTemplateId())
                .eq(RcsTournamentTemplateAcceptConfigSettle::getCategorySetId, config.getCopyCategorySetId());
        RcsTournamentTemplateAcceptConfigSettle copyConfig = configSettleMapper.selectOne(copyAcceptQueryWrapper);
        if (ObjectUtils.isEmpty(copyConfig)) {
            return;
        }
        Long copyConfigId = copyConfig.getId();
        //更新接拒时间配置
        QueryWrapper<RcsTournamentTemplateAcceptConfigSettle> acceptQueryWrapper = new QueryWrapper<>();
        acceptQueryWrapper.lambda().eq(RcsTournamentTemplateAcceptConfigSettle::getTemplateId, config.getTemplateId())
                .eq(RcsTournamentTemplateAcceptConfigSettle::getCategorySetId, config.getCategorySetId());
        RcsTournamentTemplateAcceptConfigSettle acceptConfig = configSettleMapper.selectOne(acceptQueryWrapper);
        Long acceptConfigId;
        if (ObjectUtils.isNotEmpty(acceptConfig)) {
            acceptConfigId = acceptConfig.getId();
            BeanCopyUtils.copyProperties(copyConfig, acceptConfig);
            acceptConfig.setId(acceptConfigId);
            configSettleMapper.updateMatchDataSourceAndTimeConfigSettle(acceptConfig);
        } else {
            copyConfig.setId(null);
            copyConfig.setCategorySetId(config.getCategorySetId());
            configSettleMapper.insert(copyConfig);
            acceptConfigId = copyConfig.getId();
        }

        //更新接拒事件配置
        QueryWrapper<RcsTournamentTemplateAcceptEventSettle> eventQueryWrapper = new QueryWrapper<>();
        eventQueryWrapper.lambda().eq(RcsTournamentTemplateAcceptEventSettle::getAcceptConfigSettleId, copyConfigId);
        List<RcsTournamentTemplateAcceptEventSettle> copyEventList = eventSettleMapper.selectList(eventQueryWrapper);
        if (CollectionUtils.isNotEmpty(copyEventList)) {
            //删除现有玩法集事件配置
            if (ObjectUtils.isNotEmpty(acceptConfig)) {
                QueryWrapper<RcsTournamentTemplateAcceptEventSettle> deleteWrapper = new QueryWrapper<>();
                deleteWrapper.lambda().eq(RcsTournamentTemplateAcceptEventSettle::getAcceptConfigSettleId, acceptConfigId);
                eventSettleMapper.delete(deleteWrapper);
            }
            //复制玩法集的接拒事件配置
            copyEventList.forEach(e -> {
                e.setAcceptConfigSettleId(acceptConfigId);
            });
            eventSettleMapper.updateMatchEventConfigSettle(copyEventList);
        }
    }

    /**
     * @Description 开售判断足球赛事是否配置接拒单事件
     * 玩法集全部为空的情况下，同步"一级联赛"的联赛模板中对应玩法集下的事件配置
     * @Author carver
     * @Date 11:11 2021/1/15
     **/
    public void queryEventByMatchId(Long matchId) {
        List<RcsTournamentTemplateAcceptEvent> list = rcsTournamentTemplateAcceptEventMapper.queryEventByMatchId(matchId);
        log.info("::{}::开售判断足球赛事是否配置接拒单事件-入参：{}---{}",CommonUtil.getRequestId() ,matchId, JSONObject.toJSONString(list));
        if (CollectionUtils.isEmpty(list)) {
            QueryWrapper<RcsTournamentTemplate> oneTemplateQuery = new QueryWrapper<>();
            oneTemplateQuery.lambda().eq(RcsTournamentTemplate::getSportId, 1)
                    .eq(RcsTournamentTemplate::getType, 1)
                    .eq(RcsTournamentTemplate::getTypeVal, 1)
                    .eq(RcsTournamentTemplate::getMatchType, 0);
            RcsTournamentTemplate oneTemplate = rcsTournamentTemplateMapper.selectOne(oneTemplateQuery);

            QueryWrapper<RcsTournamentTemplate> matchTemplateQuery = new QueryWrapper<>();
            matchTemplateQuery.lambda().eq(RcsTournamentTemplate::getSportId, 1)
                    .eq(RcsTournamentTemplate::getType, 3)
                    .eq(RcsTournamentTemplate::getTypeVal, matchId)
                    .eq(RcsTournamentTemplate::getMatchType, 0);
            RcsTournamentTemplate matchTemplate = rcsTournamentTemplateMapper.selectOne(matchTemplateQuery);
            if (ObjectUtil.isNotNull(oneTemplate) && ObjectUtil.isNotNull(matchTemplate)) {
                rcsMatchTemplateModifyService.liveTemplateAccept(matchId, oneTemplate.getId(), matchTemplate.getId());
            }
        }
    }

    /**
     * 查询结算事件源配置
     *
     * @Author carver
     * @Date 15:11 2021/10/16
     */
    public RcsTournamentTemplateAcceptConfigSettle queryDataSourceConfigSettle(RcsTournamentTemplateAcceptConfigSettle config) {
        QueryWrapper<RcsTournamentTemplateAcceptConfigSettle> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(RcsTournamentTemplateAcceptConfigSettle::getCategorySetId, config.getCategorySetId());
        queryWrapper.lambda().eq(RcsTournamentTemplateAcceptConfigSettle::getTemplateId, config.getTemplateId());
        RcsTournamentTemplateAcceptConfigSettle acceptConfig = configSettleMapper.selectOne(queryWrapper);
        if (ObjectUtil.isNotNull(config.getMatchId())) {
            //接拒单未初始化数据，特殊处理，指定赛事主数据源
            if (ObjectUtil.isNull(acceptConfig)) {
                acceptConfig = new RcsTournamentTemplateAcceptConfigSettle();
                RcsStandardSportMarketSell marketSell = rcsStandardSportMarketSellService.selectStandardMarketSellVo(config.getMatchId());
                acceptConfig.setDataSource(marketSell.getBusinessEvent());
            }
            //获取赛事所关联的商业数据源
            StandardMatchInfo info = standardMatchInfoMapper.selectById(config.getMatchId());
            if (StringUtils.isNotBlank(info.getThirdMatchListStr())) {
                List<ThirdDataSourceCodeVo> list = JSONArray.parseArray(info.getThirdMatchListStr(), ThirdDataSourceCodeVo.class);
                //新版本代码
                List<ThirdDataSourceCodeVo> filterList = list.stream().filter(filter -> (filter.getCommerce().equals(String.valueOf(NumberUtils.INTEGER_ONE)) && filter.getEventSupport().equals(String.valueOf(NumberUtils.INTEGER_ONE))) || filter.getDataSourceCode().equals("PD")).collect(Collectors.toList());
                acceptConfig.setThirdDataSourceCode(filterList);
            }
        }
        return acceptConfig;
    }

    /**
     * 查询结算事件类型配置
     *
     * @Author carver
     * @Date 15:11 2021/10/16
     */
    public Map<String, Object> queryEventConfigSettle(RcsTournamentTemplateAcceptEventSettle config) {
        Map<String, Object> events = Maps.newHashMap();
        List<RcsTournamentTemplateAcceptEventSettle> list = rcsTournamentTemplateAcceptEventMapper.queryMatchEventConfigSettle(config);
        if (CollectionUtils.isNotEmpty(list)) {
            Map<String, List<RcsTournamentTemplateAcceptEventSettle>> maps = list.stream().collect(Collectors.groupingBy(RcsTournamentTemplateAcceptEventSettle::getEventType));
            for (Map.Entry<String, List<RcsTournamentTemplateAcceptEventSettle>> m : maps.entrySet()) {
                Map<String, Object> map = Maps.newHashMap();
                map.put("data", m.getValue());
                map.put("count", m.getValue().size());
                events.put(m.getKey(), map);
            }
        }
        return events;
    }

    /**
     * @return void
     * @Description //更新结算事件等待时间和事件类型配置
     * @Param [config]
     * @Author Carver
     * @Date 16:15 2021/10/17
     **/
    @Transactional(rollbackFor = Exception.class)
    public void updateEventAndTimeConfigSettle(RcsTournamentTemplateAcceptConfigSettle config) {
        log.info("::{}::修改事件配置入参{}",CommonUtil.getRequestId(), JSONObject.toJSONString(config));
        VerificationDataSettle(config);
        boolean isOpeUpdaten = true;
        if (ObjectUtils.isEmpty(config.getId())) {
            configSettleMapper.insert(config);
        } else {
			RcsTournamentTemplateAcceptConfigSettle selectById = configSettleMapper.selectById(config.getId());
			// 如果传入的数据源与原有的一致，不必进行更新
			if (config.getDataSource().equalsIgnoreCase(selectById.getDataSource())) {
				isOpeUpdaten = false;
			}
            configSettleMapper.updateMatchDataSourceAndTimeConfigSettle(config);
        }
        
        if (isOpeUpdaten) {
        	//kir-1788-当前使用结算数据源缓存,更新缓存
        	RcsTournamentTemplate rcsTournamentTemplate = rcsTournamentTemplateMapper.selectById(config.getTemplateId());
        	if(ObjectUtils.isNotEmpty(rcsTournamentTemplate)){
        		String rcsTournamentTemplateAcceptAutoChangeKey = RedisKey.getDataLiveMatchSettleDatasource(rcsTournamentTemplate.getTypeVal(), Long.valueOf(config.getCategorySetId()));
        		log.info("::{}::kir-1788-保存接口-开始录入结算数据源缓存:{},{}",CommonUtil.getRequestId(), rcsTournamentTemplate.getTypeVal(), Long.valueOf(config.getCategorySetId()));
        		redisClient.setExpiry(rcsTournamentTemplateAcceptAutoChangeKey, config.getDataSource(), 3600 * 24L);
        	}

        	//kir-1788-赛事模板自动接拒开关（0.关 1.开）
        	String rcsTournamentTemplateAcceptAutoChangeKey = RedisKey.getRcsTournamentTemplateAcceptAutoChangeKey(rcsTournamentTemplate.getTypeVal(), Long.valueOf(config.getCategorySetId()));
        	log.info("::{}::kir-1788-保存接口-关闭自动接拒开关缓存:{},{}",CommonUtil.getRequestId(), rcsTournamentTemplate.getTypeVal(), config.getCategorySetId());
        	redisClient.setExpiry(rcsTournamentTemplateAcceptAutoChangeKey, 0, 3600 * 24L);

        	//手动操作则关闭开关
        	QueryWrapper<RcsTournamentTemplateAcceptConfigAutoChange> autoChangeQueryWrapper = new QueryWrapper<>();
        	autoChangeQueryWrapper.eq("template_id", rcsTournamentTemplate.getId());
        	autoChangeQueryWrapper.eq("category_set_id", config.getCategorySetId());
        	RcsTournamentTemplateAcceptConfigAutoChange rcsTournamentTemplateAcceptConfigAutoChange = templateAcceptConfigAutoChangeMapper.selectOne(autoChangeQueryWrapper);
        	if(ObjectUtils.isNotEmpty(rcsTournamentTemplateAcceptConfigAutoChange)){
        		rcsTournamentTemplateAcceptConfigAutoChange.setIsOpen(0);
        		templateAcceptConfigAutoChangeMapper.updateById(rcsTournamentTemplateAcceptConfigAutoChange);
        	}else{
        		rcsTournamentTemplateAcceptConfigAutoChange.setIsOpen(0);
        		rcsTournamentTemplateAcceptConfigAutoChange.setTemplateId(rcsTournamentTemplate.getTypeVal());
        		rcsTournamentTemplateAcceptConfigAutoChange.setCategorySetId(Long.valueOf(config.getCategorySetId()));
        		templateAcceptConfigAutoChangeMapper.insert(rcsTournamentTemplateAcceptConfigAutoChange);
        	}
        }

        if (CollectionUtils.isNotEmpty(config.getEvents())) {
            for (RcsTournamentTemplateAcceptEventSettle event : config.getEvents()) {
                event.setAcceptConfigSettleId(config.getId());
            }
            eventSettleMapper.updateMatchEventConfigSettle(config.getEvents());
        }
        //如果是赛事模板，发送结算事件数据至业务
        if (ObjectUtils.isNotEmpty(config.getMatchId())) {
            templatePushService.putTournamentTemplateSettleDataByIncrement(config);
        }
    }

    /**
     * @return
     * @Description //校验数据源时间设置
     * @Param []
     * @Author Sean
     * @Date 9:36 2020/9/8
     **/
    private void VerificationDataSettle(RcsTournamentTemplateAcceptConfigSettle config) {
        /**校验一个事件被分到不同的事件类型*/
        List<String> msgList = Lists.newArrayList();
        if (ObjectUtils.isNotEmpty(config.getEvents())) {
            for (RcsTournamentTemplateAcceptEventSettle event : config.getEvents()) {
                for (RcsTournamentTemplateAcceptEventSettle e : config.getEvents()) {
                    if (event.getEventCode().equalsIgnoreCase(e.getEventCode()) &&
                            !event.getEventType().equalsIgnoreCase(e.getEventType()) &&
                            e.getStatus().intValue() == NumberUtils.INTEGER_ONE.intValue() &&
                            event.getStatus().intValue() == NumberUtils.INTEGER_ONE.intValue()) {
                        msgList.add(event.getEventName());
                        break;
                    }
                }
            }
        }
        eventValidate(msgList, config.getNormal(), config.getMinWait(), config.getMaxWait());
    }

    private void eventValidate(List<String> msgList, Integer normal, Integer minWait, Integer maxWait) {
        if (CollectionUtils.isNotEmpty(msgList)) {
            String msg = JSONObject.toJSONString(msgList);
            throw new RcsServiceException("事件：" + msg + "处于多个事件类型中");
        }
        if (normal > MatchEventEnum.EVENT_SAFETY.getMaxTime() ||
                normal < MatchEventEnum.EVENT_SAFETY.getMinTime()) {
            throw new RcsServiceException("T常规取值范围为0~30秒！");
        }
        if (minWait > MatchEventEnum.EVENT_DANGER.getMaxTime() ||
                minWait < MatchEventEnum.EVENT_DANGER.getMinTime()) {
            throw new RcsServiceException("T延时取值范围0~60秒！");
        }
        if (maxWait > MatchEventEnum.EVENT_CLOSING.getMaxTime() ||
                maxWait < MatchEventEnum.EVENT_CLOSING.getMinTime()) {
            throw new RcsServiceException("Tmax取值范围60~120秒！");
        }
    }
}
