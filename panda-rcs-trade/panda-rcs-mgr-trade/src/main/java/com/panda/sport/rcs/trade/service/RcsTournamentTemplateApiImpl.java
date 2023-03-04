package com.panda.sport.rcs.trade.service;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.panda.merge.dto.TradeMarketUiConfigDTO;
import com.panda.sport.data.rcs.api.Request;
import com.panda.sport.data.rcs.api.Response;
import com.panda.sport.data.rcs.api.tournament.TournamentTemplateService;
import com.panda.sport.data.rcs.dto.tournament.TournamentTemplateDTO;
import com.panda.sport.rcs.cache.redis.RedisUtils;
import com.panda.sport.rcs.constants.RedisKey;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.core.utils.BeanCopyUtils;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.mapper.RcsTournamentTemplateAcceptConfigAutoChangeMapper;
import com.panda.sport.rcs.mapper.RcsTournamentTemplateAcceptConfigMapper;
import com.panda.sport.rcs.mapper.StandardMatchInfoMapper;
import com.panda.sport.rcs.mapper.tourTemplate.*;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.RcsMarketCategorySet;
import com.panda.sport.rcs.pojo.RcsStandardSportMarketSell;
import com.panda.sport.rcs.pojo.dto.tourTemplate.TournamentTemplateRefDto;
import com.panda.sport.rcs.pojo.tourTemplate.*;
import com.panda.sport.rcs.trade.enums.DataSourceWeightEnum;
import com.panda.sport.rcs.trade.enums.ManagerCodeEnum;
import com.panda.sport.rcs.trade.enums.MatchTypeEnum;
import com.panda.sport.rcs.trade.enums.TempTypeEnum;
import com.panda.sport.rcs.trade.init.AoDataSourceInit;
import com.panda.sport.rcs.trade.param.RcsTournamentTemplateBaijiaConfigParam;
import com.panda.sport.rcs.trade.param.TournamentTemplatePlayMargainParam;
import com.panda.sport.rcs.trade.param.TournamentTemplateUpdateParam;
import com.panda.sport.rcs.trade.service.impl.OnSaleCommonServer;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.util.JavaBeanUtils;
import com.panda.sport.rcs.trade.util.NumberConventer;
import com.panda.sport.rcs.trade.vo.tourTemplate.otherSystem.TournamentLevelAndTourTemplateVo;
import com.panda.sport.rcs.trade.vo.tourTemplate.otherSystem.TournamentLevelTemplateVo;
import com.panda.sport.rcs.trade.wrapper.MarketCategorySetService;
import com.panda.sport.rcs.trade.wrapper.RcsStandardSportMarketSellService;
import com.panda.sport.rcs.trade.wrapper.tourTemplate.*;
import com.panda.sport.rcs.vo.StandardMarketSellQueryVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.dubbo.common.utils.Assert;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * 处理联赛设置模板，生成赛事模板
 *
 * @author carver
 * @date 2020-10-03
 */
@Service
@Slf4j
public class RcsTournamentTemplateApiImpl implements TournamentTemplateService {

    //该类玩法 多单注投注限额值 默认值为全场让分玩法不同比例的值
    public static final List<Integer> SPECIAL_PLAY_ID = Arrays.asList(7, 8, 9, 14, 20, 74, 103, 341, 342,361,362);

    @Autowired
    private RcsTournamentTemplateMapper rcsTournamentTemplateMapper;
    @Autowired
    private RcsTournamentTemplateRefMapper rcsTournamentTemplateRefMapper;
    @Autowired
    private RcsTournamentTemplateEventMapper templateEventMapper;
    @Autowired
    private RcsTournamentTemplatePlayMargainMapper playMargainMapper;
    @Autowired
    private RcsTournamentTemplatePlayMargainRefMapper playMargainRefMapper;
    @Autowired
    private RcsMatchEventTypeInfoServiceImpl rcsMatchEventTypeInfoService;
    @Autowired
    private RcsTournamentTemplateAcceptConfigMapper rcsTournamentTemplateAcceptConfigMapper;
    @Autowired
    private RcsTournamentTemplateAcceptEventMapper rcsTournamentTemplateAcceptEventMapper;
    @Autowired
    private TournamentTemplatePushService tournamentTemplatePushService;
    @Autowired
    private IRcsTournamentTemplateService tournamentTemplateService;
    @Autowired
    private ProducerSendMessageUtils producerSendMessageUtils;
    @Autowired
    private RcsStandardSportMarketSellService rcsStandardSportMarketSellService;
    @Autowired
    private RcsTournamentTemplateAcceptConfigSettleMapper configSettleMapper;
    @Autowired
    private RcsTournamentTemplateAcceptEventSettleMapper eventSettleMapper;
    @Autowired
    private MarketCategorySetService marketCategorySetService;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private RedisClient redisClient;
    //    @Reference(check = false, lazy = true, retries = 3, timeout = 5000)
//    private AoMatchConfigService aoMatchConfigService;
    @Autowired
    private IRcsMatchTemplateModifyService rcsMatchTemplateModifyService;
    @Autowired
    private StandardMatchInfoMapper standardMatchInfoMapper;
    @Autowired
    private ProducerSendMessageUtils sendMessage;
    @Autowired
    AoDataSourceInit aoDataSourceInit;
    @Autowired
    private OnSaleCommonServer onSaleCommonServer;
    @Autowired
    private RcsTournamentTemplateAcceptConfigAutoChangeMapper templateAcceptConfigAutoChangeMapper;

    /**
     * 根据赛种获取所有等级模板，且当前联赛等级的专用模板
     *
     * @author carver
     * @date 2020-10-03
     */
    @Override
    public Response queryTournamentLevelTemplate(Request<TournamentTemplateDTO> request) throws RcsServiceException {
        log.info("::{}::，根据赛种获取所有等级模板:{}", request.getGlobalId(), JsonFormatUtils.toJson(request.getData()));
        try {
            TournamentTemplateDTO dto = request.getData();
            Assert.notNull(dto, "请输入参数");
            Assert.notNull(dto.getSportId(), "赛种不能为空");
            Assert.notNull(dto.getTournamentId(), "联赛id不能为空");
            Assert.notNull(dto.getMatchType(), "盘口类型不能为空");

            Map<String, Object> map = JavaBeanUtils.objectToMap(dto);
            List<Map<String, Object>> list = rcsTournamentTemplateMapper.queryTournamentLevelTemplate(map);
            List<TournamentLevelTemplateVo> rtnList = Lists.newArrayList();
            if (!CollectionUtils.isEmpty(list)) {
                rtnList = JSONArray.parseArray(JSONArray.toJSONString(list), TournamentLevelTemplateVo.class);
                rtnList.forEach(obj -> {
                    if (dto.getMatchType().equals(MatchTypeEnum.EARLY.getId())) {
                        if (obj.getType().equals(TempTypeEnum.LEVEL.getId())) {
                            obj.setTournamentName(NumberConventer.GetCH(obj.getTypeVal().intValue()) + "级联赛早盘模板");
                        } else if (obj.getType().equals(TempTypeEnum.TOUR.getId())) {
                            obj.setTournamentName(obj.getTournamentName() + "专用早盘模板");
                        }
                    }
                    if (dto.getMatchType().equals(MatchTypeEnum.LIVE.getId())) {
                        if (obj.getType().equals(TempTypeEnum.LEVEL.getId())) {
                            obj.setTournamentName(NumberConventer.GetCH(obj.getTypeVal().intValue()) + "级联赛滚球模板");
                        } else if (obj.getType().equals(TempTypeEnum.TOUR.getId())) {
                            obj.setTournamentName(obj.getTournamentName() + "专用滚球模板");
                        }
                    }
                });
            }
            return Response.success(rtnList);
        } catch (IllegalArgumentException ex) {
            return Response.error(201, ex.getMessage());
        } catch (Exception e) {
            TournamentTemplateDTO dto = request.getData();
            log.error("::{}::{}", CommonUtil.getRequestId(dto.getStandardMatchId()),e.getMessage(), e);
            throw new RcsServiceException("风控服务器异常，请联系风控处理");
        }
    }

    /**
     * 根据设置操盘手，生成相应的赛事模板数据
     *
     * @author carver
     * @date 2020-10-03
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response putTemplateToMatchTemplate(Request<TournamentTemplateDTO> request) throws RcsServiceException {
        log.info("::{}::，-生成赛事模板数据:{}", request.getGlobalId(), JsonFormatUtils.toJson(request.getData()));
        try {
            TournamentTemplateDTO dto = request.getData();
            Assert.notNull(dto, "请输入参数");
            Assert.notNull(dto.getSportId(), "赛种不能为空");
            Assert.notNull(dto.getMatchType(), "盘口类型不能为空");
            Assert.notNull(dto.getStandardMatchId(), "标准赛事id不能为空");
            Assert.notEmptyString(dto.getRiskManagerCode(), "操盘平台不能为空");
            // 根据参数，判断当前赛事模板是否存在
            RcsTournamentTemplate template = isTournamentTemplate(dto);
            if (ObjectUtil.isNotEmpty(template)) {
                throw new IllegalArgumentException("当前赛事模板已经存在，请核对参数");
            }
            //赛种 1：足球 2：篮球 5：篮球 8：乒乓球 9:排球 7:斯诺克 3:棒球 4:冰球
            List sportType = Arrays.asList(1, 2, 5, 7, 8, 9, 3, 10, 4);
            //目前联赛设置只上了足球和篮球，其他赛种单独做处理
            if (sportType.contains(dto.getSportId())) {
                Long templateId = 0L;
                // 是否使用当前联赛等级模板(1:是 0:否)
                Assert.notNull(dto.getIsCurrentTemp(), "是否使用当前联赛等级模板不能为空");
                Integer isCurrentTemp = dto.getIsCurrentTemp();
                if (isCurrentTemp == NumberUtils.INTEGER_ZERO) {
                    // 使用当前联赛等级模板
                    Assert.notNull(dto.getTournamentId(), "联赛id不能为空");
                    Assert.notNull(dto.getTournamentLevel(), "联赛等级不能为空");
                    Map<String, Object> map = JSONObject.parseObject(JSONObject.toJSONString(dto), Map.class);
                    templateId = rcsTournamentTemplateMapper.queryTemplateId(map);
                } else if (isCurrentTemp == NumberUtils.INTEGER_ONE) {
                    // 使用选择的联赛等级模板
                    Assert.notNull(dto.getTemplateId(), "模板id不能为空");
                    templateId = dto.getTemplateId();
                }
                // 根据模板id，查询等级模板数据，生成赛事数据
                RcsTournamentTemplate tournamentTemplate = generateMatchTemplate(templateId, dto);
                TournamentTemplateUpdateParam tournamentTemplateParam = BeanCopyUtils.copyProperties(tournamentTemplate, TournamentTemplateUpdateParam.class);
                tournamentTemplateParam.setRiskManagerCode(dto.getRiskManagerCode());
                //足球默认显示角球
                RcsStandardSportMarketSell marketSell = rcsStandardSportMarketSellService.selectStandardMarketSellVo(dto.getStandardMatchId());
                Integer cornerShow = NumberUtils.INTEGER_ZERO;
                if (!ObjectUtils.isEmpty(marketSell)) {
                    cornerShow = marketSell.getCornerShow();
                }
                if (dto.getSportId() == NumberUtils.INTEGER_ONE && cornerShow == NumberUtils.INTEGER_ZERO) {
                    StandardMarketSellQueryVo standardMarketSellQueryVo = new StandardMarketSellQueryVo();
                    standardMarketSellQueryVo.setMatchInfoId(dto.getStandardMatchId());
                    standardMarketSellQueryVo.setCornerShow(NumberUtils.INTEGER_ONE);
                    standardMarketSellQueryVo.setCardShow(NumberUtils.INTEGER_ZERO);
                    rcsStandardSportMarketSellService.configPlayShow(standardMarketSellQueryVo);
                }
                //只有足蓝发送
                if (aoDataSourceInit.checkIfAoSport(dto.getSportId())) {
                    //设置操盘手发送AO数据到业务
                    aoDataSourceInit.sendAoDataSourceMessage(tournamentTemplate, dto.getStandardMatchId());
                }
                //mq发送赛事模板数据
                return sendMatchTemplateByMq(tournamentTemplateParam);
            } else {
                //其他赛种直接下发所有玩法
                TournamentTemplateUpdateParam tournamentTemplateParam = new TournamentTemplateUpdateParam();
                tournamentTemplateParam.setTypeVal(dto.getStandardMatchId());
                tournamentTemplateParam.setMatchType(dto.getMatchType());
                tournamentTemplateParam.setDataSourceCode(DataSourceWeightEnum.SR.getName());
                tournamentTemplateParam.setTemplateName(dto.getRiskManagerCode());
                List<RcsTournamentTemplatePlayMargain> list = playMargainMapper.listPlayIdBySportId(dto.getSportId());
                if (!CollectionUtils.isEmpty(list)) {
                    //普通赛事过滤冠军玩法下发
                    List<RcsTournamentTemplatePlayMargain> addList = Lists.newArrayList();
                    for (RcsTournamentTemplatePlayMargain margin : list) {
                        if (margin.getPlayId().intValue() < 10001) {
                            addList.add(margin);
                        }
                    }
                    List<TournamentTemplatePlayMargainParam> paramList = BeanCopyUtils.copyPropertiesList(addList, TournamentTemplatePlayMargainParam.class);
                    tournamentTemplateParam.setPlayMargainList(paramList);
                    //mq发送赛事模板数据
                    return sendMatchTemplateByMq(tournamentTemplateParam);
                } else {
                    return Response.error(Response.FAIL, "没有找到玩法，请联系风控核对数据");
                }
            }
        } catch (IllegalArgumentException ex) {
            return Response.error(201, ex.getMessage());
        } catch (Exception e) {
            TournamentTemplateDTO dto = request.getData();
            log.error("::{}::{}", CommonUtil.getRequestId(dto.getStandardMatchId()),e.getMessage(), e);
            throw new RcsServiceException("风控服务器异常，请联系风控处理");
        }
    }


    /**
     * @param templateId:赛事id
     * @param dto:            接收参数
     * @Description: 根据模板id，copy生成赛事模板
     * @Author carver
     * @Date 2020/10/27 11:36
     * @return: com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplate
     **/
    private RcsTournamentTemplate generateMatchTemplate(Long templateId, TournamentTemplateDTO dto) {
        log.info("::{}::设置操盘手操作-入参：{}，等级模板ID:{}",CommonUtil.getRequestId(dto.getStandardMatchId()), JsonFormatUtils.toJson(dto), templateId);
        // 根据模板id，查询等级模板数据，生成赛事数据
        RcsTournamentTemplate tournamentTemplate = rcsTournamentTemplateMapper.queryTemplateById(templateId);
        if (ObjectUtil.isNotNull(tournamentTemplate)) {
            log.info("::{}::设置操盘手操作-等级模板信息：{}",CommonUtil.getRequestId(dto.getStandardMatchId()), JsonFormatUtils.toJson(tournamentTemplate));
            if (ManagerCodeEnum.PA.getId().equals(dto.getRiskManagerCode())) {
                if (TempTypeEnum.LEVEL.getId().equals(tournamentTemplate.getType())) {
                    tournamentTemplate.setTemplateName(NumberConventer.GetCH(tournamentTemplate.getTypeVal().intValue()) + "级联赛" + MatchTypeEnum.getNameById(tournamentTemplate.getMatchType()) + "模板");
                } else if (TempTypeEnum.TOUR.getId().equals(tournamentTemplate.getType())) {
                    tournamentTemplate.setTemplateName(tournamentTemplate.getTemplateName() + "专用" + MatchTypeEnum.getNameById(tournamentTemplate.getMatchType()) + "模板");
                }
            } else if (ManagerCodeEnum.MTS.getId().equals(dto.getRiskManagerCode())) {
                tournamentTemplate.setTemplateName("MTS操盘赛事");
                tournamentTemplate.setDataSourceCode(DataSourceWeightEnum.SR.getName());
            }
            tournamentTemplate.setId(null);
            tournamentTemplate.setType(TempTypeEnum.MATCH.getId());
            tournamentTemplate.setTypeVal(dto.getStandardMatchId());
            tournamentTemplate.setCopyTemplateId(templateId);
            log.info("::{}::设置操盘手操作-赛事模板信息：{}", CommonUtil.getRequestId(dto.getStandardMatchId()),JsonFormatUtils.toJson(tournamentTemplate));
            rcsTournamentTemplateMapper.insertBatch(tournamentTemplate);

            //kir-1453-设置操盘手默认存百家赔配置
            log.info("::{}::kir-1453-设置操盘手-百家赔缓存修改入参:{}",CommonUtil.getRequestId(dto.getStandardMatchId()), JSONObject.toJSONString(tournamentTemplate));
            String key = "rcs:tournament:template:baijia:config:matchId:%s:matchType:%s";
            if (!ObjectUtils.isEmpty(tournamentTemplate.getCautionValue()) && !ObjectUtils.isEmpty(tournamentTemplate.getBaijiaConfigValue())) {
                redisUtils.hset(String.format(key, dto.getStandardMatchId(), tournamentTemplate.getMatchType()), "cautionValue", String.valueOf(tournamentTemplate.getCautionValue()));
                redisUtils.expire(String.format(key, dto.getStandardMatchId(), tournamentTemplate.getMatchType()), 60L, TimeUnit.DAYS);
                for (RcsTournamentTemplateBaijiaConfigParam.BaijiaConfig baijiaConfig : JsonFormatUtils.fromJsonArray(tournamentTemplate.getBaijiaConfigValue(), RcsTournamentTemplateBaijiaConfigParam.BaijiaConfig.class)) {
                    //状态为开则存缓存
                    if (baijiaConfig.getStatus().equals(1)) {
                        redisUtils.hset(String.format(key, dto.getStandardMatchId(), tournamentTemplate.getMatchType()), baijiaConfig.getName(), String.valueOf(baijiaConfig.getValue()));
                    }
                }
            }
            //koala-1682需求 只设置足球
            if (dto.getSportId() == 1) {
                String mtsConfigKey = "rcs:redis:mts:contact:config:matchId:%s:matchType:%s";
                redisUtils.set(String.format(mtsConfigKey, dto.getStandardMatchId(), tournamentTemplate.getMatchType()), tournamentTemplate.getMtsConfigValue());
                redisUtils.expire(String.format(mtsConfigKey, dto.getStandardMatchId(), tournamentTemplate.getMatchType()), 10, TimeUnit.HOURS);
            }


            //kir-1255需求 赔率变动接拒开关（0.关 1.开）
            String matchKey = "rcs:risk:order:oddsScope:match.%s.match_type.%s";
            log.info("::{}::kir-1255-设置操盘手-开始录入缓存:{},{}",CommonUtil.getRequestId(dto.getStandardMatchId()), dto.getStandardMatchId(), tournamentTemplate.getMatchType());
            redisUtils.set(String.format(matchKey, dto.getStandardMatchId(), tournamentTemplate.getMatchType()), String.valueOf(tournamentTemplate.getOddsChangeStatus()));
            log.info("::{}::kir-1255-设置操盘手-录入缓存结束:{},{}",CommonUtil.getRequestId(dto.getStandardMatchId()), dto.getStandardMatchId(), tournamentTemplate.getMatchType());

            //kir-1547需求 是否出涨自动封盘开关（0.关 1.开）
            String redisKeyForIfWarnSuspended = RedisKey.Config.getChuZhangSwitchKey(dto.getStandardMatchId(), tournamentTemplate.getMatchType());
            log.info("::{}::kir-1547-设置操盘手-开始录入缓存:{},{},{}",CommonUtil.getRequestId(dto.getStandardMatchId()), dto.getStandardMatchId(), tournamentTemplate.getMatchType(), tournamentTemplate.getIfWarnSuspended());
            redisUtils.hset(redisKeyForIfWarnSuspended, String.valueOf(dto.getStandardMatchId()), String.valueOf(tournamentTemplate.getIfWarnSuspended()));
            log.info("::{}::kir-1547-设置操盘手-录入缓存结束:{},{},{}",CommonUtil.getRequestId(dto.getStandardMatchId()), dto.getStandardMatchId(), tournamentTemplate.getMatchType(), tournamentTemplate.getIfWarnSuspended());

            //kir-1368
            //rcsMatchTemplateModifyService.updateMarketConfig(Long.valueOf(tournamentTemplate.getSportId()), dto.getStandardMatchId(), TradeLevelEnum.MATCH.getLevel(), tournamentTemplate, null);

            ////kir-1368-将赛事级别提前结算状态给enzo

            TradeMarketUiConfigDTO dto1 = onSaleCommonServer.getCommonClass(tournamentTemplate);

            producerSendMessageUtils.sendMessage("RCS_TOUR_TEMPLATE_CASHOUT_TOPIC", "matchs", dto1.getStandardMatchInfoId() + "_" + dto.getMatchType(), dto1.getConfigCashOutTradeItemDTO());

            Long newTemplateId = tournamentTemplate.getId();
            // 根据模板id，查询等级模板事件数据，生成赛事事件数据
            Map<String, Object> eventMap = Maps.newHashMap();
            eventMap.put("template_id", templateId);
            List<RcsTournamentTemplateEvent> eventList = templateEventMapper.selectByMap(eventMap);
            if (!CollectionUtils.isEmpty(eventList)) {
                log.info("::{}::设置操盘手操作-等级模板事件信息：{}",CommonUtil.getRequestId(dto.getStandardMatchId()), JsonFormatUtils.toJson(eventList));
                for (RcsTournamentTemplateEvent event : eventList) {
                    event.setId(null);
                    event.setTemplateId(newTemplateId);
                }
                log.info("::{}::设置操盘手操作-赛事模板事件信息：{}",CommonUtil.getRequestId(dto.getStandardMatchId()), JsonFormatUtils.toJson(eventList));
                templateEventMapper.insertBatch(eventList);
            }
            // 获取关闭的玩法
            List<Integer> closePlayIds = playMargainMapper.listClosePlayIdBySportId(dto.getSportId());
            log.info("::{}::设置操盘手操作-玩法管理关闭的玩法信息：{}",CommonUtil.getRequestId(dto.getStandardMatchId()), JsonFormatUtils.toJson(closePlayIds));

            // 根据模板id，查询等级模板玩法数据，生成赛事玩法数据
            Map<String, Object> playMarginMap = Maps.newHashMap();
            playMarginMap.put("template_id", templateId);
            if (tournamentTemplate.getMatchType() == MatchTypeEnum.LIVE.getId()) {
                //如果是滚球赛事，过滤联赛设置未启用得玩法，生成赛事模板玩法数据
                playMarginMap.put("is_sell", 1);
            }
            List<RcsTournamentTemplatePlayMargain> playMarginList = playMargainMapper.selectByMap(playMarginMap);
            if (!CollectionUtils.isEmpty(playMarginList)) {
                log.info("::{}::设置操盘手操作-等级模板玩法信息：{}",CommonUtil.getRequestId(dto.getStandardMatchId()), JsonFormatUtils.toJson(playMarginList));
                List<RcsTournamentTemplatePlayMargainRef> newPlayMarginRefList = new ArrayList<>();
                //获取玩法4全场让分模板的id;
                RcsTournamentTemplatePlayMargain has = playMarginList.stream().filter(data -> data.getPlayId().equals(4)).findFirst().orElse(null);
                Map<Long, Long> orderSingleBetValMap = new HashMap<>();
                if (has != null) {
                    Map<String, Object> playMarginRefMap = Maps.newHashMap();
                    playMarginRefMap.put("margain_id", has.getId());
                    List<RcsTournamentTemplatePlayMargainRef> playMarginRefList = playMargainRefMapper.selectByMap(playMarginRefMap);
                    //获取开售和开赛下单注投注/赔付限额的值
                    orderSingleBetValMap = playMarginRefList.stream().filter(data -> Arrays.asList(0L, 2592000L).contains(data.getTimeVal()))
                            .collect(Collectors.toMap(RcsTournamentTemplatePlayMargainRef::getTimeVal, RcsTournamentTemplatePlayMargainRef::getOrderSinglePayVal));
                }
                for (RcsTournamentTemplatePlayMargain playMargin : playMarginList) {
                    //插入需要耗时20多秒，所以这边使用异步多线程
                    // 玩法管理已关闭的玩法，过滤生成赛事模板玩法数据
                    if (!CollectionUtils.isEmpty(closePlayIds)) {
                        if (closePlayIds.contains(playMargin.getPlayId())) {
                            continue;
                        }
                    }
                    // 获取旧marginId
                    Long oldMarginId = playMargin.getId();
                    playMargin.setId(null);
                    playMargin.setTemplateId(newTemplateId);
                    log.info("::{}::设置操盘手操作-玩法id:{}，赛事模板玩法信息：{}",CommonUtil.getRequestId(dto.getStandardMatchId()), playMargin.getPlayId(), JsonFormatUtils.toJson(playMargin));
                    playMargainMapper.insertBatch(Arrays.asList(playMargin));

                    //kir-1467需求 如果特殊抽水打开则取各区间数据进行缓存保存
                    log.info("::{}::kir-1467-设置操盘手:{}",CommonUtil.getRequestId(dto.getStandardMatchId()), playMargin.getPlayId());
                    setRedisForSpecialOddsInterVal(playMargin, dto.getStandardMatchId(), tournamentTemplate.getMatchType());

                    //1547需求代码-设置redis值
                    log.info("::{}::kir-1547-设置操盘手:{}",CommonUtil.getRequestId(dto.getStandardMatchId()), playMargin.getPlayId());
                    setRedisForIfWarnSuspended(playMargin, dto.getStandardMatchId(), tournamentTemplate.getMatchType());

                    //kir-1255需求 赔率接拒变动范围
                    log.info("::{}::kir-1255-设置操盘手-对应联赛模板数据为:{}",CommonUtil.getRequestId(dto.getStandardMatchId()), JSONObject.toJSONString(playMargin));
                    //开关打开才需录入缓存
                    if (!ObjectUtils.isEmpty(playMargin)) {
                        if (playMargin.getOddsChangeStatus() != null && playMargin.getOddsChangeStatus().equals(1)) {
                            log.info("::{}::kir-1255-设置操盘手-开始录入缓存:{},{},{}",CommonUtil.getRequestId(dto.getStandardMatchId()), dto.getStandardMatchId(), playMargin.getPlayId(), playMargin.getMatchType());
                            String redisKey = "rcs:risk:order:oddsScope:match.%s.play_id.%s.match_type.%s";
                            redisUtils.set(String.format(redisKey, dto.getStandardMatchId(), playMargin.getPlayId(), playMargin.getMatchType()), String.valueOf(playMargin.getOddsChangeValue()));
                            log.info("::{}::kir-1255-设置操盘手-录入缓存结束:{},{},{}",CommonUtil.getRequestId(dto.getStandardMatchId()), dto.getStandardMatchId(), playMargin.getPlayId(), playMargin.getMatchType());
                        } else {
                            log.info("::{}::kir-1255-设置操盘手-开始删除缓存:{},{},{}",CommonUtil.getRequestId(dto.getStandardMatchId()), dto.getStandardMatchId(), playMargin.getPlayId(), playMargin.getMatchType());
                            String redisKey = "rcs:risk:order:oddsScope:match.%s.play_id.%s.match_type.%s";
                            redisUtils.del(String.format(redisKey, dto.getStandardMatchId(), playMargin.getPlayId(), playMargin.getMatchType()));
                            log.info("::{}::kir-1255-设置操盘手-删除缓存成功:{},{},{}",CommonUtil.getRequestId(dto.getStandardMatchId()), dto.getStandardMatchId(), playMargin.getPlayId(), playMargin.getMatchType());
                        }
                    }

                    Map<String, Object> playMarginRefMap = Maps.newHashMap();
                    playMarginRefMap.put("margain_id", oldMarginId);
                    List<RcsTournamentTemplatePlayMargainRef> playMarginRefList = playMargainRefMapper.selectByMap(playMarginRefMap);
                    log.info("::{}::设置操盘手操作-玩法id:{}，等级模板margin信息：{}",CommonUtil.getRequestId(dto.getStandardMatchId()), playMargin.getPlayId(), JsonFormatUtils.toJson(playMarginRefList));
                    if (SPECIAL_PLAY_ID.contains(playMargin.getPlayId())) {
                        specialPlaySetOrderSingleBetVal(playMargin.getPlayId(), orderSingleBetValMap, playMarginRefList);
                    }
                    for (RcsTournamentTemplatePlayMargainRef playMarginRef : playMarginRefList) {
                        playMarginRef.setId(null);
                        if (dto.getSportId() == 2) {//篮球
                            playMarginRef.setStatus(3);
                        }
                        log.info("::{}::设置操盘手操作-赛事模板marginRef信息：{}",CommonUtil.getRequestId(dto.getStandardMatchId()), JsonFormatUtils.toJson(playMarginRef));
                        playMarginRef.setMargainId(playMargin.getId());
                        newPlayMarginRefList.add(playMarginRef);
                    }

                }

                //kir-1368
                //rcsMatchTemplateModifyService.updateMarketConfig(Long.valueOf(tournamentTemplate.getSportId()), dto.getStandardMatchId(), TradeLevelEnum.CASH_OUT.getLevel(), null, playMarginList);

                if (!CollectionUtils.isEmpty(newPlayMarginRefList)) {
                    log.info("::{}::设置操盘手操作-赛事模板margin信息：{}",CommonUtil.getRequestId(dto.getStandardMatchId()), JsonFormatUtils.toJson(newPlayMarginRefList));
                    // 生成赛事分时margin数据
                    playMargainRefMapper.insertBatch(newPlayMarginRefList);
                }
            }

            // 获取风控型玩法集
            QueryWrapper<RcsMarketCategorySet> setWrapper = new QueryWrapper();
            setWrapper.lambda().eq(RcsMarketCategorySet::getSportId, dto.getSportId())
                    .eq(RcsMarketCategorySet::getType, 1);
            List<RcsMarketCategorySet> setList = marketCategorySetService.list(setWrapper);
            List<Integer> setIds = setList.stream().map(e -> e.getId().intValue()).collect(Collectors.toList());
            //添加其他玩法集
            setIds.add(-1);
            log.info("::{}::设置操盘手操作-玩法集信息：{}",CommonUtil.getRequestId(dto.getStandardMatchId()), JsonFormatUtils.toJson(setIds));

            // 根据模板id，生成赛事接拒单事件数据
            Map<String, Object> acceptConfigMap = Maps.newHashMap();
            acceptConfigMap.put("template_id", templateId);
            List<RcsTournamentTemplateAcceptConfig> acceptConfigList = rcsTournamentTemplateAcceptConfigMapper.selectByMap(acceptConfigMap);
            if (!CollectionUtils.isEmpty(acceptConfigList)) {
                log.info("::{}::设置操盘手操作-等级模板接拒单信息：{}",CommonUtil.getRequestId(dto.getStandardMatchId()), JsonFormatUtils.toJson(acceptConfigList));
                for (RcsTournamentTemplateAcceptConfig acceptConfig : acceptConfigList) {
                    // 过滤已删除的玩法集，不生成赛事模板
                    if (!CollectionUtils.isEmpty(setIds)) {
                        if (!setIds.contains(acceptConfig.getCategorySetId())) {
                            continue;
                        }
                    }
                    // 获取旧接拒单数据
                    Long oldAcceptConfigId = acceptConfig.getId();
                    Map<String, Object> acceptEventMap = Maps.newHashMap();
                    acceptEventMap.put("accept_config_id", oldAcceptConfigId);
                    List<RcsTournamentTemplateAcceptEvent> newAcceptEventList = Lists.newArrayList();
                    List<RcsTournamentTemplateAcceptEvent> acceptEventList = rcsTournamentTemplateAcceptEventMapper.selectByMap(acceptEventMap);
                    for (RcsTournamentTemplateAcceptEvent acceptEvent : acceptEventList) {
                        acceptEvent.setId(null);
                        acceptEvent.setAcceptConfigId(null);
                        acceptEvent.setCreateTime(new Date());
                        acceptEvent.setUpdateTime(new Date());
                        newAcceptEventList.add(acceptEvent);
                    }
                    // copy生成赛事接拒单事件数据
                    acceptConfig.setId(null);
                    acceptConfig.setTemplateId(newTemplateId);
                    acceptConfig.setEvents(newAcceptEventList);
                    log.info("::{}::设置操盘手操作-赛事模板接拒单信息：{}",CommonUtil.getRequestId(dto.getStandardMatchId()), JsonFormatUtils.toJson(acceptConfig));
                    rcsMatchEventTypeInfoService.insertEventList(acceptConfig);

                    //kir-1788-当前使用接拒数据源缓存
                    String rcsTournamentTemplateAcceptAutoChangeKey = RedisKey.getDataLiveMatchConfigDatasource(dto.getStandardMatchId(), Long.valueOf(acceptConfig.getCategorySetId()));
                    log.info("::{}::kir-1788-设置操盘手-开始录入接拒数据源缓存:{},{}",CommonUtil.getRequestId(dto.getStandardMatchId()), dto.getStandardMatchId(), Long.valueOf(acceptConfig.getCategorySetId()));
                    redisClient.setExpiry(rcsTournamentTemplateAcceptAutoChangeKey, acceptConfig.getDataSource(), 3600 * 24L);
                }
            }

            // 根据模板id，生成赛事接拒单 结算 事件数据
            Map<String, Object> configSettleMap = Maps.newHashMap();
            configSettleMap.put("template_id", templateId);
            List<RcsTournamentTemplateAcceptConfigSettle> configSettleList = configSettleMapper.selectByMap(configSettleMap);
            if (!CollectionUtils.isEmpty(configSettleList)) {
                for (RcsTournamentTemplateAcceptConfigSettle configSettle : configSettleList) {
                    // 过滤已删除的玩法集，不生成赛事模板
                    if (!CollectionUtils.isEmpty(setIds)) {
                        if (!setIds.contains(configSettle.getCategorySetId())) {
                            continue;
                        }
                    }
                    // 获取旧接拒单结算数据
                    Long oldConfigSettleId = configSettle.getId();
                    // copy生成赛事接拒单结算事件数据
                    configSettle.setId(null);
                    configSettle.setTemplateId(newTemplateId);
                    log.info("::{}::设置操盘手操作-赛事模板接拒单结算信息：{}", CommonUtil.getRequestId(dto.getStandardMatchId()),JsonFormatUtils.toJson(configSettle));
                    configSettleMapper.insert(configSettle);
                    Long newConfigSettleId = configSettle.getId();

                    Map<String, Object> eventSettleMap = Maps.newHashMap();
                    eventSettleMap.put("accept_config_settle_id", oldConfigSettleId);
                    List<RcsTournamentTemplateAcceptEventSettle> eventSettleList = eventSettleMapper.selectByMap(eventSettleMap);
                    for (RcsTournamentTemplateAcceptEventSettle eventSettle : eventSettleList) {
                        eventSettle.setId(null);
                        eventSettle.setAcceptConfigSettleId(newConfigSettleId);
                        eventSettleMapper.insert(eventSettle);
                    }


                    //kir-1788-当前使用结算数据源缓存
                    String rcsTournamentTemplateAcceptAutoChangeKey = RedisKey.getDataLiveMatchSettleDatasource(dto.getStandardMatchId(), Long.valueOf(configSettle.getCategorySetId()));
                    log.info("::{}::kir-1788-设置操盘手-开始录入结算数据源缓存:{},{}",CommonUtil.getRequestId(dto.getStandardMatchId()), dto.getStandardMatchId(), Long.valueOf(configSettle.getCategorySetId()));
                    redisClient.setExpiry(rcsTournamentTemplateAcceptAutoChangeKey, configSettle.getDataSource(), 3600 * 24L);
                }
            }

            //kir-1788-设置操盘手
            //根据模板id，生成赛事模板自动接拒开关信息
            Map<String, Object> configAutoChangeMap = Maps.newHashMap();
            configAutoChangeMap.put("template_id", templateId);
            List<RcsTournamentTemplateAcceptConfigAutoChange> rcsTournamentTemplateAcceptConfigAutoChanges = templateAcceptConfigAutoChangeMapper.selectByMap(configAutoChangeMap);
            log.info("::{}::kir-1788-自动接拒开关:{}",CommonUtil.getRequestId(dto.getStandardMatchId()), JSONObject.toJSONString(rcsTournamentTemplateAcceptConfigAutoChanges));
            log.info("::{}::kir-1788-玩法集id:{}",CommonUtil.getRequestId(dto.getStandardMatchId()), JSONObject.toJSONString(setIds));

            if (!CollectionUtils.isEmpty(rcsTournamentTemplateAcceptConfigAutoChanges)) {
                for (RcsTournamentTemplateAcceptConfigAutoChange configAutoChange : rcsTournamentTemplateAcceptConfigAutoChanges) {
                    //copy生成赛事模板自动接拒开关信息
                    configAutoChange.setId(null);
                    configAutoChange.setTemplateId(newTemplateId);
                    log.info("::{}::kir-1788-设置操盘手-赛事模板自动接拒开关信息：{}",CommonUtil.getRequestId(dto.getStandardMatchId()), JsonFormatUtils.toJson(configAutoChange));
                    templateAcceptConfigAutoChangeMapper.insert(configAutoChange);

                    //kir-1788-赛事模板自动接拒开关（0.关 1.开）
                    String rcsTournamentTemplateAcceptAutoChangeKey = RedisKey.getRcsTournamentTemplateAcceptAutoChangeKey(dto.getStandardMatchId(), configAutoChange.getCategorySetId());
                    log.info("::{}::kir-1788-设置操盘手-开始录入自动接拒开关缓存:{},{}",CommonUtil.getRequestId(dto.getStandardMatchId()), dto.getStandardMatchId(), configAutoChange.getCategorySetId());
                    redisClient.setExpiry(rcsTournamentTemplateAcceptAutoChangeKey, String.valueOf(configAutoChange.getIsOpen()), 3600 * 24L);
                }
            }

            //通知更新缓存
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("sportId", tournamentTemplate.getSportId());
            map.put("dataType", "3");
            map.put("matchId", tournamentTemplate.getTypeVal());
            log.info("::{}::RCS_LIMIT_CACHE_CLEAR_TOPIC缓存通知:{}",CommonUtil.getRequestId(dto.getStandardMatchId()), JSONObject.toJSONString(map));
            producerSendMessageUtils.sendMessage("RCS_LIMIT_CACHE_CLEAR_TOPIC", map);

            map.put("dataType", "1");
            producerSendMessageUtils.sendMessage("RCS_LIMIT_CACHE_CLEAR_TOPIC", map);
            log.info("::{}::RCS_LIMIT_CACHE_CLEAR_TOPIC缓存通知:{}",CommonUtil.getRequestId(dto.getStandardMatchId()), JSONObject.toJSONString(map));
            return tournamentTemplate;
        }
        return null;
    }


    //一些特殊玩法。单注投注限额赋默认值
    private void specialPlaySetOrderSingleBetVal(Integer playid, Map<Long, Long> orderSingleBetValMap, List<RcsTournamentTemplatePlayMargainRef> playMarginRefList) {
        for (RcsTournamentTemplatePlayMargainRef ref : playMarginRefList) {
            if (Arrays.asList(0L, 2592000L).contains(ref.getTimeVal()) && ref.getOrderSingleBetVal() == null) {
                switch (playid) {
                    case 7:
                    case 20:
                    case 74:
                    case 341:
                    case 342:
                        ref.setOrderSingleBetVal(orderSingleBetValMap.containsKey(ref.getTimeVal()) ? orderSingleBetValMap.get(ref.getTimeVal()) / 2L : null);
                        break;
                    case 8:
                    case 9:
                    case 14:
                    case 103:
                        ref.setOrderSingleBetVal(orderSingleBetValMap.containsKey(ref.getTimeVal()) ? orderSingleBetValMap.get(ref.getTimeVal()) / 4L : null);
                        break;
                }
            }

        }
    }

    /**
     * 1547需求代码-设置redis值
     *
     * @param playMargin
     * @param standardMatchId
     * @param matchType
     */
    void setRedisForIfWarnSuspended(RcsTournamentTemplatePlayMargain playMargin, Long standardMatchId, Integer matchType) {
        if (!ObjectUtils.isEmpty(playMargin.getIfWarnSuspended())) {
            String redisKey = RedisKey.Config.getChuZhangSwitchKey(standardMatchId, matchType);
            //log.info("::{}::kir-1547-玩法开关-开始录入缓存:{},{},{},{}",CommonUtil.getRequestId(), standardMatchId, playMargin.getPlayId(), matchType, playMargin.getIfWarnSuspended());
            redisUtils.hset(redisKey, String.valueOf(playMargin.getPlayId()), String.valueOf(playMargin.getIfWarnSuspended()));
            //log.info("::{}::kir-1547-玩法开关-录入缓存结束:{},{},{},{}",CommonUtil.getRequestId(), standardMatchId, playMargin.getPlayId(), matchType, playMargin.getIfWarnSuspended());
        }
    }

    /**
     * 1467需求代码-设置redis值
     *
     * @param playMargin
     * @param standardMatchId
     * @param matchType
     */
    void setRedisForSpecialOddsInterVal(RcsTournamentTemplatePlayMargain playMargin, Long standardMatchId, Integer matchType) {
        if (!ObjectUtils.isEmpty(playMargin.getIsSpecialPumping())) {
            log.info("::{}::kir-1467-设置赛事:{},玩法:{},的特殊抽水缓存 总开关:{},特殊抽水赔率区间:{},高赔:{},低赔:{},区间开关:{}",CommonUtil.getRequestId(standardMatchId), playMargin.getPlayId(), playMargin.getIsSpecialPumping(), playMargin.getSpecialOddsInterval(),
                    playMargin.getSpecialOddsIntervalHigh(), playMargin.getSpecialOddsIntervalLow(), playMargin.getSpecialOddsIntervalStatus());
            //总开关 1.开 0.关
            String pumpingKey = "rcs:risk:order:limit:scope:match:%s:matchType:%s:playId:%s:status";
            if (playMargin.getIsSpecialPumping().equals(1)) {
                //总开关:1为开
                redisUtils.set(String.format(pumpingKey, standardMatchId, matchType, playMargin.getPlayId()), "1");
                //log.info("::{}::kir-1467-设置总开关缓存设置完毕:{},{},{},值为:{}",CommonUtil.getRequestId(), standardMatchId, matchType, playMargin.getPlayId(), "1");

                //区间开关
                if (!ObjectUtils.isEmpty(playMargin.getSpecialOddsIntervalStatus())) {
                    Map map = JSON.parseObject(playMargin.getSpecialOddsIntervalStatus(), Map.class);
                    log.info("::{}::kir-1467-区间开关值为:{}",CommonUtil.getRequestId(), JSONObject.toJSONString(map));
                    map.forEach((k, v) -> {
                        //区间开关 1.开 0.关
                        String statusKey = "rcs:risk:order:limit:scope:match:%s:matchType:%s:playId:%s:between:%s:status";
                        redisUtils.set(String.format(statusKey, standardMatchId, matchType, playMargin.getPlayId(), k), String.valueOf(v));
                        //log.info("::{}::kir-1467-区间开关设置完毕:{},{}",CommonUtil.getRequestId(),String.format(statusKey, standardMatchId, matchType, playMargin.getPlayId(), k), v);
                    });
                }

                //高赔
                if (!ObjectUtils.isEmpty(playMargin.getSpecialOddsIntervalHigh())) {
                    Map map = JSON.parseObject(playMargin.getSpecialOddsIntervalHigh(), Map.class);
                    log.info("::{}::kir-1467-高赔区间值为:{}", CommonUtil.getRequestId(), JSONObject.toJSONString(map));
                    map.forEach((k, v) -> {
                        //高赔 type标识高低赔率 1高 0低
                        String highKey = "rcs:risk:order:limit:scope:match:%s:matchType:%s:playId:%s:type:%s:between:%s";
                        redisUtils.set(String.format(highKey, standardMatchId, matchType, playMargin.getPlayId(), 1, k), String.valueOf(v));
                        //log.info("::{}::kir-1467-高赔区间值设置完毕:{},{}",CommonUtil.getRequestId(), String.format(highKey, standardMatchId, matchType, playMargin.getPlayId(), 1, k), v);
                    });
                }

                //高赔：保底投注限额
                if (!ObjectUtils.isEmpty(playMargin.getSpecialBettingIntervalHigh())) {
                    Map map = JSON.parseObject(playMargin.getSpecialBettingIntervalHigh(), Map.class);
                    log.info("::{}::dev-1888-高赔保底投注区间值为:{}",CommonUtil.getRequestId(standardMatchId), JSONObject.toJSONString(map));
                    map.forEach((k, v) -> {
                        //高赔 type标识高低赔率 1高 0低
                        String highKey = "rcs:risk:order:limit:scope:match:%s:matchType:%s:playId:%s:type:%s:between:%s:betting";
                        redisUtils.set(String.format(highKey, standardMatchId, matchType, playMargin.getPlayId(), 1, k), String.valueOf(v));
                        //log.info("::{}::dev-1888-高赔投注区间值设置完毕:{},{}",CommonUtil.getRequestId(standardMatchId), String.format(highKey, standardMatchId, matchType, playMargin.getPlayId(), 1, k), v);
                    });
                }

                //低赔
                if (!ObjectUtils.isEmpty(playMargin.getSpecialOddsIntervalLow())) {
                    Map map = JSON.parseObject(playMargin.getSpecialOddsIntervalLow(), Map.class);
                    log.info("::{}::kir-1467-低赔区间值为:{}", CommonUtil.getRequestId(standardMatchId),JSONObject.toJSONString(map));
                    map.forEach((k, v) -> {
                        //低赔 type标识高低赔率 1高 0低
                        String lowKey = "rcs:risk:order:limit:scope:match:%s:matchType:%s:playId:%s:type:%s:between:%s";
                        redisUtils.set(String.format(lowKey, standardMatchId, matchType, playMargin.getPlayId(), 0, k), String.valueOf(v));
                        //log.info("::{}::kir-1467-低赔区间值设置完毕:{},{}",CommonUtil.getRequestId(), String.format(lowKey, standardMatchId, matchType, playMargin.getPlayId(), 0, k), v);
                    });
                }
            } else {
                //总开关:0为关
                redisUtils.set(String.format(pumpingKey, standardMatchId, matchType, playMargin.getPlayId()), "0");
                //log.info("::{}::kir-1467-设置总开关缓存设置完毕:{},{},{},值为:{}",CommonUtil.getRequestId(standardMatchId), standardMatchId, matchType, playMargin.getPlayId(), "0");
            }
        }
    }

    /**
     * 根据设置操盘手，取消开售操作，清除相应的赛事模板数据
     *
     * @return
     * @author carver
     * @date 2020-10-03
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response putMatchTemplateCancel(Request<TournamentTemplateDTO> request) throws RcsServiceException {
        log.info("::{}::，取消开售操作-清除相应的赛事模板数据:{}", request.getGlobalId(), JsonFormatUtils.toJson(request.getData()));
        try {
            TournamentTemplateDTO dto = request.getData();
            Assert.notNull(dto, "请输入参数");
            Assert.notNull(dto.getSportId(), "赛种不能为空");
            Assert.notNull(dto.getMatchType(), "盘口类型不能为空");
            Assert.notNull(dto.getStandardMatchId(), "标准赛事id不能为空");
            //删除赛事模板数据
            return deleteMatchTemplate(dto, request.getGlobalId());
        } catch (IllegalArgumentException ex) {
            return Response.error(201, ex.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(),e.getMessage(), e);
            throw new RcsServiceException("风控服务器异常，请联系风控处理");
        }
    }

    /**
     * Panda操盘切换MTS操盘，Panda赛事数据先删除，在生成MTS赛事数据
     *
     * @throws RcsServiceException
     * @author carver
     * @date 2020-10-03
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response putMatchReplaceRiskManagerCode(Request<TournamentTemplateDTO> request) throws RcsServiceException {
        log.info("::{}::，Panda操盘切换MTS操盘:{}", request.getGlobalId(), JsonFormatUtils.toJson(request.getData()));
        try {
            TournamentTemplateDTO dto = request.getData();
            Assert.notNull(dto, "请输入参数");
            Assert.notNull(dto.getSportId(), "赛种不能为空");
            Assert.notNull(dto.getMatchType(), "盘口类型不能为空");
            Assert.notNull(dto.getStandardMatchId(), "标准赛事id不能为空");
            //删除赛事模板数据
            Response res = deleteMatchTemplate(dto, request.getGlobalId());
            if (res.getCode() != Response.SUCCESS) {
                return res;
            }
            //构建MTS赛事模板数据，根据赛事所属联赛找专用模板，未找到专用模板，采用等级模板生成
            Map<String, Object> map = JSONObject.parseObject(JSONObject.toJSONString(dto), Map.class);
            Long templateId = rcsTournamentTemplateMapper.queryTemplateId(map);
            // 根据模板id，查询等级模板数据，生成赛事数据
            RcsTournamentTemplate tournamentTemplate = generateMatchTemplate(templateId, dto);
            TournamentTemplateUpdateParam tournamentTemplateParam = BeanCopyUtils.copyProperties(tournamentTemplate, TournamentTemplateUpdateParam.class);
            //mq发送赛事模板数据
            return sendMatchTemplateByMq(tournamentTemplateParam);
        } catch (IllegalArgumentException ex) {
            return Response.error(201, ex.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(),e.getMessage(), e);
            throw new RcsServiceException("风控服务器异常，请联系风控处理");
        }
    }

    /**
     * 删除赛事模板数据
     *
     * @author carver
     * @date 2020-10-03
     */
    private Response deleteMatchTemplate(TournamentTemplateDTO dto, String globalId) {
        RcsTournamentTemplate template = isTournamentTemplate(dto);
        if (ObjectUtil.isNotNull(template)) {
            Long templateId = template.getId();
            //删除模板基础表
            rcsTournamentTemplateMapper.deleteById(templateId);
            log.info("::{}::，取消开售操作-删除模板基础表:{}", globalId, JsonFormatUtils.toJson(template));
            //删除结算审核事件
            QueryWrapper<RcsTournamentTemplateEvent> event = new QueryWrapper<>();
            event.lambda().eq(RcsTournamentTemplateEvent::getTemplateId, templateId);
            List<RcsTournamentTemplateEvent> eventList = templateEventMapper.selectList(event);
            if (!CollectionUtils.isEmpty(eventList)) {
                List<Long> eventIdsList = Lists.newArrayList();
                eventList.forEach(obj -> {
                    eventIdsList.add(obj.getId());
                });
                templateEventMapper.deleteBatchIds(eventIdsList);
                log.info("::{}::，取消开售操作-删除结算审核事件:{}", globalId, JsonFormatUtils.toJson(eventList));
            }

            //删除玩法数据
            QueryWrapper<RcsTournamentTemplatePlayMargain> playMargain = new QueryWrapper<>();
            playMargain.lambda().eq(RcsTournamentTemplatePlayMargain::getTemplateId, templateId)
                    .eq(RcsTournamentTemplatePlayMargain::getMatchType, dto.getMatchType());
            List<RcsTournamentTemplatePlayMargain> playMargainList = playMargainMapper.selectList(playMargain);
            List<Long> playMargainId = Lists.newArrayList();
            if (!CollectionUtils.isEmpty(playMargainList)) {
                playMargainList.forEach(obj -> {
                    playMargainId.add(obj.getId());
                });
                playMargainMapper.deleteBatchIds(playMargainId);
                log.info("::{}::，取消开售操作-删除玩法数据:{}", globalId, JsonFormatUtils.toJson(playMargainList));
            }

            //删除margin数据
            if (!CollectionUtils.isEmpty(playMargainId)) {
                QueryWrapper<RcsTournamentTemplatePlayMargainRef> playMargainRef = new QueryWrapper<>();
                playMargainRef.lambda().in(RcsTournamentTemplatePlayMargainRef::getMargainId, playMargainId);
                List<RcsTournamentTemplatePlayMargainRef> playMargainRefList = playMargainRefMapper.selectList(playMargainRef);
                if (!CollectionUtils.isEmpty(playMargainRefList)) {
                    List<Long> playMargainRefIds = Lists.newArrayList();
                    playMargainRefList.forEach(obj -> {
                        playMargainRefIds.add(obj.getId());
                    });
                    playMargainRefMapper.deleteBatchIds(playMargainRefIds);
                    log.info("::{}::，取消开售操作-删除margin数据:{}", globalId, JsonFormatUtils.toJson(playMargainRefList));
                }
            }

            //删除滚球接拒单
            RcsTournamentTemplateAcceptConfig config = new RcsTournamentTemplateAcceptConfig();
            config.setTemplateId(templateId);
            config.setMatchId(Long.valueOf(dto.getStandardMatchId()));
            rcsMatchEventTypeInfoService.deleteEventList(config);
            log.info("::{}::，取消开售操作-删除滚球接拒单:{}", globalId, JsonFormatUtils.toJson(config));
        }
        return Response.success();
    }

    /**
     * MQ下发赛事模板数据，通知融合和业务
     *
     * @author carver
     * @date 2020-10-03
     */
    private Response sendMatchTemplateByMq(TournamentTemplateUpdateParam tournamentTemplateParam) {
        if (ObjectUtil.isNotNull(tournamentTemplateParam)) {
            // 发送赛事模板数据
            tournamentTemplatePushService.putTournamentTemplateMatchEventData(tournamentTemplateParam);
            // 发送赛事模玩法数据
            tournamentTemplatePushService.putTournamentTemplatePlayData(tournamentTemplateParam);
            // 发送赛事模板结算接拒单数据
            tournamentTemplatePushService.putTournamentTemplateSettleData(tournamentTemplateParam);
        } else {
            return Response.error(Response.FAIL, "模板数据未找到，请联系风控处理");
        }
        return Response.success();
    }

    /**
     * 判断赛事模板是否存在
     *
     * @author carver
     * @date 2020-10-03
     */
    private RcsTournamentTemplate isTournamentTemplate(TournamentTemplateDTO dto) {
        QueryWrapper<RcsTournamentTemplate> tournamentTemplateQueryWrapper = new QueryWrapper<>();
        tournamentTemplateQueryWrapper.lambda().eq(RcsTournamentTemplate::getType, TempTypeEnum.MATCH.getId())
                .eq(RcsTournamentTemplate::getTypeVal, dto.getStandardMatchId())
                .eq(RcsTournamentTemplate::getSportId, dto.getSportId())
                .eq(RcsTournamentTemplate::getMatchType, dto.getMatchType());
        RcsTournamentTemplate template = rcsTournamentTemplateMapper.selectOne(tournamentTemplateQueryWrapper);
        return template;
    }

    /**
     * 根据赛种和联赛id，获取所有模板
     *
     * @author carver
     * @date 2020-10-03
     */
    @Override
    public Response queryAllTournamentTemplateById(Request<TournamentTemplateDTO> request) throws RcsServiceException {
        log.info("::{}::，根据赛种获取所有等级模板:{}", request.getGlobalId(), JsonFormatUtils.toJson(request.getData()));
        try {
            TournamentTemplateDTO dto = request.getData();
            Assert.notNull(dto, "请输入参数");
            Assert.notNull(dto.getSportId(), "赛种不能为空");
            if (CollectionUtils.isEmpty(dto.getTournamentIds())) {
                throw new IllegalArgumentException("联赛id不能为空");
            }
            //返回给融合
            List<TournamentLevelAndTourTemplateVo> rtnList = Lists.newArrayList();
            //根据赛种获取早盘和滚球等级模板
            QueryWrapper<RcsTournamentTemplate> tournamentTemplateQueryWrapper = new QueryWrapper<>();
            tournamentTemplateQueryWrapper.lambda().eq(RcsTournamentTemplate::getType, TempTypeEnum.LEVEL.getId())
                    .eq(RcsTournamentTemplate::getSportId, dto.getSportId());
            List<RcsTournamentTemplate> list = rcsTournamentTemplateMapper.selectList(tournamentTemplateQueryWrapper);
            if (CollectionUtils.isEmpty(list)) {
                return Response.success(null);
            }
            //根据联赛id获取关联模板
            List<TournamentTemplateRefDto> refList = rcsTournamentTemplateRefMapper.selectTemplateByTournamentId(dto.getTournamentIds());
            Map<Long, List<TournamentTemplateRefDto>> refMap = refList.stream().collect(Collectors.groupingBy(TournamentTemplateRefDto::getTournamentId, LinkedHashMap::new, Collectors.toList()));
            //组装返回结构数据
            for (Long tournamentId : dto.getTournamentIds()) {
                //等级模板处理
                List<TournamentLevelTemplateVo> preTemplate = Lists.newArrayList();
                List<TournamentLevelTemplateVo> liveTemplate = Lists.newArrayList();
                for (RcsTournamentTemplate obj : list) {
                    TournamentLevelTemplateVo vo = new TournamentLevelTemplateVo();
                    vo.setTemplateId(obj.getId());
                    if (obj.getMatchType().equals(MatchTypeEnum.EARLY.getId())) {
                        vo.setTournamentName(NumberConventer.GetCH(obj.getTypeVal().intValue()) + "级联赛早盘模板");
                        vo.setTypeVal(obj.getTypeVal());
                        vo.setType(obj.getType());
                        vo.setTournamentEnglishName(NumberConventer.GetPreEN(obj.getTypeVal().intValue()));
                        preTemplate.add(vo);
                    }
                    if (obj.getMatchType().equals(MatchTypeEnum.LIVE.getId())) {
                        vo.setTournamentName(NumberConventer.GetCH(obj.getTypeVal().intValue()) + "级联赛滚球模板");
                        vo.setTypeVal(obj.getTypeVal());
                        vo.setType(obj.getType());
                        vo.setTournamentEnglishName(NumberConventer.GetLiveEN(obj.getTypeVal().intValue()));
                        liveTemplate.add(vo);
                    }
                }
                //专用模板处理
                List<Long> preTemplateIds = preTemplate.stream().map(TournamentLevelTemplateVo::getTemplateId).collect(Collectors.toList());
                List<Long> liveTemplateIds = liveTemplate.stream().map(TournamentLevelTemplateVo::getTemplateId).collect(Collectors.toList());
                if (refMap.containsKey(tournamentId)) {
                    TournamentTemplateRefDto tournamentTemplateRefDto = refMap.get(tournamentId).get(0);
                    Long preTemplateId = tournamentTemplateRefDto.getTemplateId();
                    Long liveTemplateId = tournamentTemplateRefDto.getLiveTemplateId();
                    if (!preTemplateIds.contains(preTemplateId) && preTemplateId.intValue() != 0) {
                        TournamentLevelTemplateVo tournamentLevelTemplateVo = new TournamentLevelTemplateVo();
                        tournamentLevelTemplateVo.setTemplateId(preTemplateId);
                        tournamentLevelTemplateVo.setTournamentName(tournamentTemplateRefDto.getPreTemplateName() + "专用早盘模板");
                        tournamentLevelTemplateVo.setTournamentEnglishName(tournamentTemplateRefDto.getPreTemplateName() + "专用早盘模板");
                        tournamentLevelTemplateVo.setTypeVal(tournamentId);
                        tournamentLevelTemplateVo.setType(TempTypeEnum.TOUR.getId());
                        preTemplate.add(0, tournamentLevelTemplateVo);
                    }
                    if (!liveTemplateIds.contains(liveTemplateId) && liveTemplateId.intValue() != 0) {
                        TournamentLevelTemplateVo tournamentLevelTemplateVo = new TournamentLevelTemplateVo();
                        tournamentLevelTemplateVo.setTemplateId(liveTemplateId);
                        tournamentLevelTemplateVo.setTournamentName(tournamentTemplateRefDto.getLiveTemplateName() + "专用滚球模板");
                        tournamentLevelTemplateVo.setTournamentEnglishName(tournamentTemplateRefDto.getLiveTemplateName() + "专用滚球模板");
                        tournamentLevelTemplateVo.setTypeVal(tournamentId);
                        tournamentLevelTemplateVo.setType(TempTypeEnum.TOUR.getId());
                        liveTemplate.add(0, tournamentLevelTemplateVo);
                    }
                }
                TournamentLevelAndTourTemplateVo vo = new TournamentLevelAndTourTemplateVo();
                vo.setTournamentId(tournamentId);
                vo.setPreTemplate(preTemplate);
                vo.setLiveTemplate(liveTemplate);
                rtnList.add(vo);
            }
            if (CollectionUtils.isEmpty(rtnList)) {
                return Response.success(null);
            }
            Map<Long, List<TournamentLevelAndTourTemplateVo>> ojbk = rtnList.stream().collect(Collectors.groupingBy(TournamentLevelAndTourTemplateVo::getTournamentId, LinkedHashMap::new, Collectors.toList()));
            return Response.success(ojbk);
        } catch (IllegalArgumentException ex) {
            return Response.error(201, ex.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(),e.getMessage(), e);
            throw new RcsServiceException("风控服务器异常，请联系风控处理");
        }
    }

    /**
     * 在线编辑功能，已开售赛事提供所属模板
     *
     * @author carver
     * @date 2020-10-03
     */
    @Override
    public Response queryMatchTemplateByMatchId(Request<List<Long>> request) throws RcsServiceException {
        log.info("::{}::，在线编辑功能，已开售赛事提供所属模板:{}", request.getGlobalId(), JsonFormatUtils.toJson(request.getData()));
        try {
            Assert.notNull(request.getData(), "请输入参数");
            List<Long> matchIds = request.getData();
            if (CollectionUtils.isEmpty(matchIds)) {
                throw new IllegalArgumentException("请输入赛事id");
            }
            QueryWrapper<RcsTournamentTemplate> tournamentTemplateQueryWrapper = new QueryWrapper<>();
            tournamentTemplateQueryWrapper.lambda().eq(RcsTournamentTemplate::getType, TempTypeEnum.MATCH.getId())
                    .in(RcsTournamentTemplate::getTypeVal, matchIds);
            List<RcsTournamentTemplate> list = tournamentTemplateService.list(tournamentTemplateQueryWrapper);
            if (CollectionUtils.isEmpty(list)) {
                return Response.success(null);
            }
            List<TournamentLevelTemplateVo> matchSoldTemplateVos = Lists.newArrayList();
            list.forEach(obj -> {
                TournamentLevelTemplateVo vo = new TournamentLevelTemplateVo();
                vo.setTemplateId(obj.getId());
                vo.setType(obj.getType());
                vo.setTypeVal(obj.getTypeVal());
                vo.setTournamentName(obj.getTemplateName());
                //根据当前赛事查询所属联赛模板等级
                RcsTournamentTemplate byId = tournamentTemplateService.getById(obj.getCopyTemplateId());
                if (Objects.nonNull(byId)) {
                    if (obj.getMatchType().equals(1)) {
                        vo.setTournamentEnglishName(NumberConventer.GetPreEN(byId.getTypeVal().intValue()));
                    } else {
                        vo.setTournamentEnglishName(NumberConventer.GetLiveEN(byId.getTypeVal().intValue()));
                    }
                }
                vo.setMatchType(obj.getMatchType());
                matchSoldTemplateVos.add(vo);
            });
            return Response.success(matchSoldTemplateVos);
        } catch (IllegalArgumentException ex) {
            return Response.error(201, ex.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(),e.getMessage(), e);
            throw new RcsServiceException("风控服务器异常，请联系风控处理");
        }
    }

    /**
     * 赛程取消赛事关联，根据赛事id和数据源编码，判断接拒是否设置当前数据源
     *
     * @param request
     * @return
     * @throws RcsServiceException
     */
    @Override
    public Response queryAcceptConfigByMatchId(Request<TournamentTemplateDTO> request) throws RcsServiceException {
        log.info("::{}::，赛程取消赛事关联-入参:{}", request.getGlobalId(), JsonFormatUtils.toJson(request.getData()));
        try {
            TournamentTemplateDTO dto = request.getData();
            Assert.notNull(dto, "请输入参数");
            Assert.notNull(dto.getStandardMatchId(), "赛事id不能为空");
            Assert.notNull(dto.getDataSourceCode(), "数据源编码不能为空");
            List<RcsTournamentTemplateAcceptConfig> list = rcsTournamentTemplateAcceptConfigMapper.queryAcceptConfigByMatchId(dto.getStandardMatchId(), dto.getDataSourceCode());
            log.info("::{}::，赛程取消赛事关联-记录:{}", request.getGlobalId(), JsonFormatUtils.toJson(list));
            if (list.size() > 0) {
                return Response.success(Boolean.TRUE);
            } else {
                return Response.success(Boolean.FALSE);
            }
        } catch (IllegalArgumentException ex) {
            return Response.error(201, ex.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(),e.getMessage(), e);
            throw new RcsServiceException("风控服务器异常，请联系风控处理");
        }
    }
}
