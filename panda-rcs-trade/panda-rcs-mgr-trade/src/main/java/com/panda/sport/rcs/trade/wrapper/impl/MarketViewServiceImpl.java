package com.panda.sport.rcs.trade.wrapper.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.panda.merge.api.ITradeMarketConfigApi;
import com.panda.merge.api.ITradeMarketOddsApi;
import com.panda.merge.dto.*;
import com.panda.sport.rcs.common.DateUtils;
import com.panda.sport.rcs.common.MqConstants;
import com.panda.sport.rcs.common.NumberUtils;
import com.panda.sport.rcs.common.OddsValueConvertUtils;
import com.panda.sport.rcs.constants.BaseConstants;
import com.panda.sport.rcs.constants.MatchConstants;
import com.panda.sport.rcs.core.utils.BeanCopyUtils;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.enums.DataSourceTypeEnum;
import com.panda.sport.rcs.enums.MarketKindEnum;
import com.panda.sport.rcs.enums.MatchSetEnum;
import com.panda.sport.rcs.enums.TraderLevelEnum;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.mapper.*;
import com.panda.sport.rcs.mongo.*;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.*;
import com.panda.sport.rcs.pojo.dto.odds.RcsStandardMarketDTO;
import com.panda.sport.rcs.pojo.statistics.RcsMatchDimensionStatistics;
import com.panda.sport.rcs.trade.param.RcsMatchConfigParam;
import com.panda.sport.rcs.trade.service.TradeCommonService;
import com.panda.sport.rcs.trade.service.TradeMarketSetServiceImpl;
import com.panda.sport.rcs.trade.service.TradeOddsCommonService;
import com.panda.sport.rcs.trade.service.TradeVerificationService;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.utils.mongopage.MongoPageHelper;
import com.panda.sport.rcs.trade.vo.LiveStandardMarketMessageVO;
import com.panda.sport.rcs.trade.vo.LiveStandardMarketOddsMessageVO;
import com.panda.sport.rcs.trade.vo.LiveStandardMatchMarketMessageVO;
import com.panda.sport.rcs.trade.wrapper.*;
import com.panda.sport.rcs.trade.wrapper.statistics.RcsMatchDimensionStatisticsService;
import com.panda.sport.rcs.utils.DataRealtimeApiUtils;
import com.panda.sport.rcs.utils.DataRealtimeApiUtils.ApiCall;
import com.panda.sport.rcs.utils.TradeUserUtils;
import com.panda.sport.rcs.vo.*;
import com.panda.sport.rcs.vo.statistics.RcsMatchDimensionStatisticsVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.panda.sport.rcs.constants.MqConstant.MARKET_ODDS_UPDTAE_SNAPSHOT_TOPIC;
import static com.panda.sport.rcs.constants.MqConstant.MATCH_SNAPSHOT_MARKET_UPDATE_TOPIC;

/**
 * 盘口视图展示服务类
 */

@Service("marketViewServiceImp")
@Slf4j
public class MarketViewServiceImpl implements MarketViewService {
    @Autowired
    MarketStatusService marketStatusService;
    @Autowired
    MatchStatisticsInfoService matchStatisticsInfoService;
    @Autowired
    RcsMatchCollectionService rcsMatchCollectionService;
    @Autowired
    private TradeVerificationService tradeVerificationService;
    @Autowired
    RcsOddsConvertMappingMyService mappingMyService;
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    MongoService mongoService;
    @Autowired
    CategoryService categoryService;
    @Autowired
    private MatchSetMongoService matchSetMongoService;
    @Autowired
    private StandardMatchInfoMapper standardMatchInfoMapper;
    @Autowired
    RcsMatchConfigService rcsMatchConfigService;
    @Autowired
    IRcsMatchMarketConfigService matchMarketConfigService;
    @Autowired
    RcsMatchMarketConfigMapper rcsMatchMarketConfigMapper;
    @Autowired
    RcsMatchMarketMarginConfigMapper rcsMatchMarketMarginConfigMapper;
    @Autowired
    MongoPageHelper mongoPageHelper;
    @Autowired
    private RcsOddsConvertMappingService rcsOddsConvertMappingService;
    @Autowired
    private StandardSportMarketCategoryMapper standardSportMarketCategoryMapper;
    @Autowired
    private TradeCommonService tradeCommonService;
    @Autowired
    private StandardSportMarketMapper standardSportMarketMapper;
    @Autowired
    private RcsMatchDimensionStatisticsService matchDimensionStatisticsService;
    @Autowired
    private MatchPeriodService matchPeriodService;
    @Autowired
    private IRcsMatchMarketConfigService rcsMatchMarketConfigService;
    @Reference(check = false, lazy = true, retries = 1, timeout = 5000)
    private ITradeMarketConfigApi tradeMarketConfigApi;
    @Reference(check = false, lazy = true, retries = 1, timeout = 5000)
    private ITradeMarketOddsApi tradeMarketOddsApi;
    @Reference(lazy = true, check = false, retries = 3)
    private ITradeMarketConfigApi iTradeMarketConfigApi;

    @Autowired
    private ProducerSendMessageUtils producerSendMessageUtils;

    @Autowired
    RcsTournamentOperateMarketService rcsTournamentOperateMarketService;

    @Autowired
    private RcsTradeConfigService rcsTradeConfigService;

    @Autowired
    private MatchTradeConfigService matchTradeConfigService;

    @Autowired
    TradeMarketSetServiceImpl tradeMarketSetService;

    @Autowired
    private SportMatchViewService sportMatchViewService;
    @Autowired
    private TradeOddsCommonService tradeOddsCommonService;
    @Autowired
    RcsSysUserMapper rcsSysUserMapper;


    @Override
    public boolean updateMatchBetChange(RcsMatchDimensionStatistics matchDimensionStatistics) {
        log.info("::{}::更新同联赛赛事实货量updateMatchBetChange{}",matchDimensionStatistics.getGlobalId(), JsonFormatUtils.toJson(matchDimensionStatistics));
        try {
            if (matchDimensionStatistics != null) {
                MatchBetChange matchBetChange = new MatchBetChange();
                BeanUtils.copyProperties(matchDimensionStatistics, matchBetChange);
                MatchStatisticsInfo info = matchStatisticsInfoService.getMatchInfoByMatchId(matchBetChange.getMatchId());
                if (info != null) {
                    matchBetChange.setSet1Score(info.getSet1Score());
                    matchBetChange.setScore(info.getScore());
                    matchBetChange.setPeriod(info.getPeriod());
                    matchBetChange.setSecondsMatchStart(info.getSecondsMatchStart());
                    matchBetChange.setMatchPeriodId(info.getPeriod());
                    MatchPeriod one = matchPeriodService.getOne(matchBetChange.getMatchId(), info.getPeriod());

                    if (one != null) matchBetChange.setPeriodScore(one.getScore());
                    Long[] matchIds = {matchBetChange.getMatchId()};
                    Long startTime = System.currentTimeMillis();
                    //近一小时货量
                    List<RcsMatchDimensionStatisticsVo> rcsMatchDimensionStatisticsVos = matchDimensionStatisticsService.searchNearlyOneHourRealTimeValue(matchIds, startTime);

                    if (!CollectionUtils.isEmpty(rcsMatchDimensionStatisticsVos)) {
                        matchBetChange.setTotalValueOneHour(rcsMatchDimensionStatisticsVos.get(0).getRealTimeValue());
                    }
                    //liveMarketOddsService.macthBetChanged(matchBetChange);
                    producerSendMessageUtils.sendMessage(MqConstants.WS_MATCH_BET_CHANGED_TOPIC, MqConstants.WS_MATCH_BET_CHANGED_TAG, "", matchBetChange);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("::{}::推送同联赛赛事实货量失败:" + e.getMessage(), matchDimensionStatistics.getGlobalId(),e);
            return false;
        }
        return true;
    }




    @Override
    public Long getTraderMatchCount(MarketLiveOddsQueryVo marketLiveOddsQueryVo) {
        //查询指派赛事
        Query query = sportMatchViewService.buildMongoQuery(marketLiveOddsQueryVo);
        long count = mongoTemplate.count(query, MatchMarketLiveOddsVo.class);
        return count;
    }

    @Override
    public List<MatchMarketLiveBean> queryTournaments(MarketLiveOddsQueryVo queryVo) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        if(queryVo.getTournamentType()!=null&&queryVo.getTournamentType()==2){
            criteria = sportMatchViewService.timelyCriteria(queryVo);
        }else {
            criteria = sportMatchViewService.buildMongoCriteria(queryVo);
        }
        query.addCriteria(criteria);
        List<MatchMarketLiveBean> entityList = mongoTemplate.find(query, MatchMarketLiveBean.class);
        return entityList;
    }


    @Override
    public Long getRollNum(Long standardTournamentId) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        Long beginTime = System.currentTimeMillis() - 1000 * 60 * 60 * 4L;
        criteria.andOperator(Criteria.where("matchStartTime").gte(DateUtils.transferLongToDateStrings(beginTime - 1000 * 60 * 60 * 4L)),
                Criteria.where("matchStartTime").lt(DateUtils.transferLongToDateStrings(beginTime + 1000 * 60 * 10)));

        criteria.and("standardTournamentId").is(standardTournamentId);
        criteria.and("liveOddBusiness").is(1).and("matchStatus").in(1, 2, 6, 10);
        query.addCriteria(criteria);
        long count = mongoTemplate.count(query, MatchMarketLiveOddsVo.class);
        return count;
    }

    @Override
    public Long getLiveNum(MarketLiveOddsQueryVo queryVo) {
        Query query = new Query();
        query.addCriteria(sportMatchViewService.buildMongoCriteria(queryVo));
        long count = mongoTemplate.count(query, MatchMarketLiveOddsVo.class);
        return count;
    }

    /**
     * 发送状态变更，自动手动切换
     *
     * @param type         修改类型   1：玩法   2：联赛 3：赛事 4：盘口
     * @param configId     配置表id
     * @param marketStatus 盘口状态 0:active 开, 1:suspended 封, 2:deactivated 关, 11:锁 null 表示不修改当前盘口状态
     * @param targetId     目标id
     * @param tradeType    0:自动操盘 1:手动操盘 null 表示不修改当前操盘类型
     * @author paca
     */
    @Trace
    public void putTradeMarketConfig(Integer type, String configId, Integer marketStatus, String targetId, Integer tradeType, Map<String, String> additionsMap) {
        if (additionsMap == null) {
            additionsMap = new HashMap<>();
        }
        TradeMarketConfigDTO bean = JSONObject.parseObject(JSONObject.toJSONString(additionsMap), TradeMarketConfigDTO.class);
        if (marketStatus != null && marketStatus != 0 && marketStatus != 1 && marketStatus != 2 && marketStatus != 11) {
            throw new RcsServiceException("状态参数marketStatus错误，不是指定的几个参数");
        }
        if (tradeType != null && tradeType != 0 && tradeType != 1) {
            throw new RcsServiceException("操盘类型tradeType 参数错误，不是指定的几个参数");
        }

        bean.setActive(1);
        if (type == 1) {
            configId = MatchConstants.PLAY_ID + configId;
        } else if (type == 2) {
            configId = MatchConstants.TOURNAMENT_ID + configId;
        } else if (type == 3) {
            configId = MatchConstants.MATCH_ID + configId;
        } else if (type == 4) {
            configId = MatchConstants.MATCH_MARKET_ID + configId;
        }
        bean.setConfigId(configId);
        bean.setLevel(type);
        bean.setMarketStatus(marketStatus);
        bean.setModifyTime(System.currentTimeMillis());
        bean.setOperaterId(1L);
        bean.setSourceSystem(2);
        bean.setTargetId(targetId);
        bean.setTradeType(tradeType);

        Response response = DataRealtimeApiUtils.handleApi(bean, new ApiCall() {
            @Override
            @Trace
            public <R> Response<R> callApi(Request request) {
                return tradeMarketConfigApi.putTradeMarketConfig(request);
            }
        });
    }

    private static String parseName(String zs, String ext) {
        JSONObject jsonExt = JSONObject.parseObject("{\"from\":\"16\",\"to\":\"30\"}");
        int index = zs.indexOf("{");
        String srcStr = "";
        while (index >= 0) {
            srcStr = srcStr + zs.substring(0, index);
            String currentStr = zs.substring(index + 1);
            int end = currentStr.indexOf("}");
            String key = currentStr.substring(0, end);
            srcStr = srcStr + jsonExt.getString(key);

            zs = currentStr.substring(end + 1);
            index = zs.indexOf("{");
        }
        srcStr = srcStr + zs;

        return srcStr;
    }


    /**
     * @return void
     * @Description //验证数据的正确性
     * @Param [config]
     * @Author kimi
     * @Date 2020/3/6
     **/
    public BigDecimal verifyData(RcsMatchMarketConfig config) {
        //1：转换盘口
        BigDecimal subtract = config.getAwayMarketValue().subtract(config.getHomeMarketValue());
        //BigDecimal subtract = config.getHomeMarketValue().subtract(config.getAwayMarketValue());
        double v = subtract.doubleValue();
        int i1 = subtract.intValue();
        if (i1 == v) {
            //如果是整数需要转换为整数
            subtract = new BigDecimal(i1);
        }
        if (v > 0) {
            config.setHomeMarketValue(new BigDecimal(0));
            config.setAwayMarketValue(subtract);
        } else {
            config.setHomeMarketValue(subtract.multiply(new BigDecimal(-1)));
            config.setAwayMarketValue(new BigDecimal(0));
        }

        BigDecimal multipleValue = new BigDecimal(BaseConstants.MULTIPLE_VALUE);
        // 赔率最大最小验证
        String maxOdds = "";
        String minOdds = "";
        if (config.getMaxOdds() == null || config.getMinOdds() == null) {
            throw new RcsServiceException("最大最小赔率不能为空");
        } else if (MarketKindEnum.Malaysia.getValue().equalsIgnoreCase(config.getMarketType())) {
            maxOdds = rcsOddsConvertMappingService.maxEUOddsByMYOdds(config.getMaxOdds().toPlainString());
            minOdds = rcsOddsConvertMappingService.maxEUOddsByMYOdds(config.getMinOdds().toPlainString());
            if (new BigDecimal(maxOdds).compareTo(new BigDecimal(minOdds)) < 0) {
                throw new RcsServiceException("马赔最小赔率比最大赔率大");
            }
        } else if (config.getMaxOdds().compareTo(config.getMinOdds()) < 0) {
            throw new RcsServiceException("欧赔最小赔率比最大赔率大");
        }

        StandardMatchInfo info = standardMatchInfoMapper.selectById(config.getMatchId());
        if (info == null) {
            throw new RcsServiceException("赛事不存在");
        }

        if ("1".equals(String.valueOf(info.getMatchStatus())) || "2".equals(String.valueOf(info.getMatchStatus()))
                || "10".equals(String.valueOf(info.getMatchStatus()))) {
            if ("MTS".equals(info.getLiveRiskManagerCode())) {
                throw new RcsServiceException("滚球MTS操盘，不能修改");
            }
        } else {
            if ("MTS".equals(info.getPreRiskManagerCode())) {
                throw new RcsServiceException("赛前MTS操盘，不能修改");
            }
        }
        // 校验赔率合法性
        List<Map<String, Object>> oddsList = config.getOddsList();
        if (DataSourceTypeEnum.MANUAL.getValue() == config.getDataSource().intValue()) {
            //1：如果是欧赔 赔率小于1不允许进行操作
            if (config.getMarketType().equals(MarketKindEnum.Europe.getValue())) {
                for (Map<String, Object> map : oddsList) {
                    String fieldOddsValue = map.get("fieldOddsValue").toString();
                    if (new BigDecimal(fieldOddsValue).compareTo(new BigDecimal(org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE)) <= org.apache.commons.lang3.math.NumberUtils.INTEGER_ZERO) {
                        throw new RcsServiceException("欧赔设置不合理,超过最大最小赔率限制");
                    }

                }
            } else {
                //两项盘
                for (Map<String, Object> map : oddsList) {
                    String fieldOddsValue = map.get("fieldOddsValue").toString();
                    if (new BigDecimal(fieldOddsValue).compareTo(new BigDecimal(org.apache.commons.lang3.math.NumberUtils.INTEGER_MINUS_ONE)) == 0) {
                        fieldOddsValue = org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE.toString();
                    }
                    String oddsValue = rcsOddsConvertMappingService.getEUOdds(fieldOddsValue);
                    if (new BigDecimal(oddsValue).compareTo(new BigDecimal(0)) == 0) {
                        throw new RcsServiceException("马来赔设置不合理,超过最大最小赔率限制");
                    }

                }
            }
        }

        //4： 验证magin值
        boolean convert = com.panda.sport.rcs.trade.util.MarginUtils.convert(oddsList, config.getDataSource(), config.getMarketType(), config.getMargin(),config.getPlayId());
        if (!convert) {
            throw new RcsServiceException("margin校验没通过");
        }


        //水差设置
        if (DataSourceTypeEnum.AUTOMATIC.getValue() == config.getDataSource().intValue()) {

            if (config.getAwayAutoChangeRate() != null && (Double.parseDouble(config.getAwayAutoChangeRate()) < -0.3 ||
                    Double.parseDouble(config.getAwayAutoChangeRate()) > 0.3)) {
                throw new RcsServiceException("水差限额超过限制：-0.3 - 0.3范围，当前设置：" + config.getAwayAutoChangeRate());
            }
        }

        // 校验数据范围
        tradeVerificationService.checkDataRange(config, org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE);

        //最大投注金额校验
        if (ObjectUtils.isEmpty(config.getMaxSingleBetAmount())) {
            throw new RcsServiceException("最大投注金额不能为空");
        }
        //最大投注金额校验
        BigDecimal maxAmount = Optional.ofNullable(config.getMaxBetAmount()).orElse(new BigDecimal("10000000"));
        if (ObjectUtils.isEmpty(config.getMaxSingleBetAmount()) ||
                ObjectUtils.isEmpty(config.getMaxBetAmount()) ||
                config.getMaxSingleBetAmount().longValue() > maxAmount.longValue()) {
            throw new RcsServiceException("最大投注金额不能大于联赛配置");
        }

        return subtract;
    }





    @Override
    @Transactional(rollbackFor = Exception.class)
    public RcsMatchMarketConfig updateMatchMarketConfig(RcsMatchMarketConfig config) {

        return config;
    }


    @Override
    public void checkChangeMTS(RcsMatchConfigParam config) {
        TradeCloseOpeartorDTO dto = new TradeCloseOpeartorDTO();
        dto.setMatchId(config.getMatchId());
        dto.setDataSourceCode("SR");
        DataRealtimeApiUtils.handleApi(dto, new ApiCall() {
            @Override
            @Trace
            public <R> Response<R> callApi(Request request) {
                Response rs = tradeMarketConfigApi.checkChangeMTS(request);
                return rs;
            }
        });
    }

    /**
     * 更新操盘方式
     *
     * @param config
     * @return
     */
    @Override
    public Response updateRiskManagerCodeByDataManager(RcsMatchConfigParam config) {
        Response response = DataRealtimeApiUtils.handleApi(config, new ApiCall() {
            @Override
            @Trace
            public <R> Response<R> callApi(Request request) {
                RcsSysUser rcsSysUser = rcsSysUserMapper.selectById(TradeUserUtils.getUserIdNoException());
                RiskManagerCodeDTO riskManagerCodeDTO = new RiskManagerCodeDTO();
                try {
                    riskManagerCodeDTO.setMatchId(config.getMatchId());
                    if (config.getMatchType().equals(2)) {
                        riskManagerCodeDTO.setType("LIVE");
                        riskManagerCodeDTO.setRiskManagerCode(config.getLiveRiskManagerCode());
                    } else {
                        riskManagerCodeDTO.setType("PRE");
                        riskManagerCodeDTO.setRiskManagerCode(config.getPreRiskManagerCode());
                    }
                    riskManagerCodeDTO.setCategoryIds(config.getCategoryIds());
                    riskManagerCodeDTO.setUserName(rcsSysUser.getUserCode());
                    request.setData(riskManagerCodeDTO);
                    log.info("::{}::kir-1529-参数request:{}",config.getMatchId(), JsonFormatUtils.toJson(request));
                    return tradeMarketConfigApi.updateRiskManagerCode(request);
                } catch (Exception ex) {
                    log.error("::{}::{}",config.getMatchId(), ex.getMessage(),ex);
                    return Response.failed("失败");
                }
            }
        });
        return response;
    }

    public StandardMatchMarketDTO getCurrentMarketInfo(StandardSportMarket standardSportMarket, List<StandardSportMarketOdds> list, List<Map<String, Object>> oddList, Boolean isInsert) {
        if (standardSportMarket == null) {
            throw new RcsServiceException("该盘口id的数据不存在");
        }
        Long marketId = standardSportMarket.getId();
        if (oddList == null) {
            Map<String, Object> params = new HashMap<>();
            params.put("market", marketId);
            oddList = standardSportMarketCategoryMapper.queryOddsListByMarketId(params);
        }
        StandardMarketDTO bean = BeanCopyUtils.copyProperties(standardSportMarket, StandardMarketDTO.class);
        if (list == null || list.size() == 0) {
            throw new RcsServiceException("当前盘口id数据异常 ！" + standardSportMarket.getId());
        }
        List<StandardMarketOddsDTO> oddsList = new ArrayList<StandardMarketOddsDTO>();
        for (StandardSportMarketOdds obj : list) {
            StandardMarketOddsDTO dto = BeanCopyUtils.copyProperties(obj, StandardMarketOddsDTO.class);
            dto.setThirdOddsFieldSourceId(String.valueOf(obj.getId()));
            if (isInsert) {
                dto.setDataSourceCode(BaseConstants.DATA_SOURCE_CODE);
                dto.setThirdOddsFieldSourceId(null);
            }
            dto.setOddsFieldsTemplateId(obj.getOddsFieldsTempletId());
            dto.setActive(obj.getActive());
            for (Map<String, Object> map : oddList) {
                if (String.valueOf(map.get("oddsType")).equals(obj.getOddsType())) {
                    double fieldOddsValue = Double.parseDouble(map.get("fieldOddsValue").toString());
                    //需要保留2位小数
                    dto.setOddsValue((int) fieldOddsValue);
                    dto.setOriginalOddsValue(dto.getOddsValue());
                    continue;
                }
            }
            oddsList.add(dto);
        }
        bean.setMarketOddsList(oddsList);
        bean.setThirdMarketSourceId(String.valueOf(marketId));
        if (isInsert) {
            bean.setDataSourceCode(BaseConstants.DATA_SOURCE_CODE);
            bean.setThirdMarketSourceId(null);
        }
        StandardMatchMarketDTO standardMatchMarketDTO = new StandardMatchMarketDTO();
        ArrayList<StandardMarketDTO> standardMarketDTOs = new ArrayList<>();
        standardMarketDTOs.add(bean);
        standardMatchMarketDTO.setMarketList(standardMarketDTOs);
        standardMatchMarketDTO.setStandardMatchInfoId(standardSportMarket.getStandardMatchInfoId());
        return standardMatchMarketDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOddsValue(UpdateOddsValueVo updateOddsValueVo) {
        List<OddsValueVo> oddsValueList = updateOddsValueVo.getOddsValueList();
        if (CollectionUtils.isEmpty(oddsValueList)) {
            return;
        }
        Integer integer = rcsTradeConfigService.getDataSource(updateOddsValueVo.getMatchId(), updateOddsValueVo.getPlayId());
        if (integer.equals(DataSourceTypeEnum.AUTOMATIC.getValue())) {
            throw new RcsServiceException("自动不允许修改赔率");
        }
        RcsMatchMarketConfig config = JSONObject.parseObject(JSONObject.toJSONString(updateOddsValueVo),RcsMatchMarketConfig.class);
        List<RcsStandardMarketDTO> playAllMarketList = tradeOddsCommonService.getMatchPlayOdds(config);
        config.setDataSource(integer.longValue());
        RcsMatchMarketConfig rcsMatchMarketConfig = rcsMatchMarketConfigService.getMaxAndMinOddsValue(config.getMatchId(), config.getPlayId());
        for (RcsStandardMarketDTO market :playAllMarketList){
            if (updateOddsValueVo.getMarketId().toString().equalsIgnoreCase(market.getId())){
                config.setSubPlayId(market.getChildStandardCategoryId().toString());
                tradeCommonService.ifOddsChangeAndClearConfig(config,oddsValueList, JSONArray.parseArray(JSONArray.toJSONString(market.getMarketOddsList()),StandardSportMarketOdds.class),1L);
                for (OddsValueVo oddsValueVo : oddsValueList) {
                    BigDecimal oddsValue = new BigDecimal(oddsValueVo.getValue()+"");
                    if (oddsValueVo.getValue() <= 1) {
                        throw new RcsServiceException("赔率小于1不能进行相关操作");
                    }
                    if(Objects.nonNull(rcsMatchMarketConfig)){
                        if(oddsValue.compareTo(rcsMatchMarketConfig.getMinOdds()) < 0){
                            throw new RcsServiceException(oddsValue+"赔率修改低于模板最小赔率"+rcsMatchMarketConfig.getMinOdds());
                        }
                        if(oddsValue.compareTo(rcsMatchMarketConfig.getMaxOdds()) > 0){
                            throw new RcsServiceException(oddsValue+"赔率修改超出模板最大赔率"+rcsMatchMarketConfig.getMaxOdds());
                        }
                    }
                    for (StandardMarketOddsDTO standardMarketOddsDTO : market.getMarketOddsList()) {
                        if (standardMarketOddsDTO.getOddsType().equals(oddsValueVo.getOddsType())) {
                            BigDecimal v = new BigDecimal(String.valueOf(oddsValueVo.getValue())).multiply(new BigDecimal(String.valueOf(BaseConstants.MULTIPLE_VALUE)));
                            standardMarketOddsDTO.setOddsValue(v.intValue());
                            if (oddsValueVo.getActive() != null) {
                                standardMarketOddsDTO.setActive(oddsValueVo.getActive());
                            }
                        }
                    }
                }
            }
        }
        List<StandardMarketDTO> marketList = JSONArray.parseArray(JSONArray.toJSONString(playAllMarketList),StandardMarketDTO.class);
        tradeCommonService.putTradeMarketOdds(config, marketList,null);
    }

    @Override
    public List<Map<String, Object>> getSelectMatchs(MarketLiveOddsQueryVo marketLiveOddsQueryVo) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        Query query = sportMatchViewService.buildMongoQuery(marketLiveOddsQueryVo);
        List<MatchMarketLiveOddsVo> matchLiveInfos = mongoTemplate.find(query, MatchMarketLiveOddsVo.class);
        if (matchLiveInfos == null || matchLiveInfos.size() <= 0) {
            return result;
        }
        for (MatchMarketLiveOddsVo vo : matchLiveInfos) {
            Map<String, Object> temMap = new HashMap<String, Object>();
            temMap.put("matchId", vo.getMatchId());
            temMap.put("matchManageId", String.valueOf(vo.getMatchManageId()));
            temMap.put("teamList", vo.getTeamList());
            result.add(temMap);
        }
        return result;
    }


    @Override
    public void updateRiskManagerCodeSnapshot(RcsMatchConfigParam config) {
        Long matchId = config.getMatchId();
        Integer level = TraderLevelEnum.MATCH.getLevel();
        Integer methodNo = MatchSetEnum.UPDATE_RISKMANAGER_CODE.getCode();
        Criteria criteria = Criteria.where("matchId").is(matchId).and("methodNo").is(methodNo);
        MatchSetVo matchSet = mongoTemplate.findOne(new Query().addCriteria(criteria), MatchSetVo.class);
        if (null != matchSet) {
            if ("MTS".equals(matchSet.getParamValue())) {
                throw new RcsServiceException("只能从PA修改成MTS,不能从MTS修改为PA操盘方式");
            }
        }
        MatchSetVo matchSetVo = new MatchSetVo();
        matchSetVo.setJsonParams(JsonFormatUtils.toJson(config));
        matchSetVo.setMatchId(matchId);
        matchSetVo.setMethodNo(methodNo);
        matchSetVo.setTradeLevel(level);
        matchSetVo.setParamValue(config.getLiveRiskManagerCode());
        matchSetVo.setUpdateTime(DateUtils.getCurrentTime());
        matchSetMongoService.upsertMatchSetMongo(matchSetVo);
        MatchStatusAndDataSuorceVo mqVO = new MatchStatusAndDataSuorceVo()
                .setMatchId(matchId)
                .setRiskManagerCode(config.getLiveRiskManagerCode());
        producerSendMessageUtils.sendMessage(MATCH_SNAPSHOT_MARKET_UPDATE_TOPIC, mqVO);
    }

    @Override
    public MarketConfigMongo getMarketConfigMongo(RcsMatchMarketConfig config) {
        MarketConfigMongo marketConfigMongo = null;
        Query query = new Query();
        MarketConfigMongo market = mongoTemplate.findOne(query.addCriteria(Criteria.where("matchId").is(config.getMatchId()).and("marketId").is(String.valueOf(config.getMarketId()))), MarketConfigMongo.class);
        if (market != null) {
            marketConfigMongo = market;
            if (config.getMarketType().equals(MarketKindEnum.Malaysia.getValue())) {
                List<Map<String, Object>> oddsList = marketConfigMongo.getOddsList();
                if (!CollectionUtils.isEmpty(oddsList)) {
                    for (Map<String, Object> odd : oddsList) {
                        String fieldOddsValue = odd.get("fieldOddsValue").toString();
                        String displayOddsVal = OddsValueConvertUtils.convertAndDefaultDisply(MarketKindEnum.Malaysia, NumberUtils.getBigDecimal(fieldOddsValue).setScale(0).intValue());
                        odd.put("fieldOddsValue", displayOddsVal);
                    }
                }
            }
            if (Arrays.asList(1L, 17L, 111L, 114L, 126L, 129L).contains(marketConfigMongo.getPlayId())) {
                List<Map<String, Object>> oddsList = marketConfigMongo.getOddsList();
                if (!CollectionUtils.isEmpty(oddsList)) {
                    for (Map<String, Object> odd : oddsList) {
                        String originalOddsValue = NumberUtils.getBigDecimal(odd.get("originalOddsValue").toString()).multiply(new BigDecimal(100000)).setScale(0).toPlainString();
                        odd.put("originalOddsValue", originalOddsValue);
                    }
                }
            }
        } else {
            RcsMatchMarketConfig rcsMatchMarketConfig = matchTradeConfigService.queryMatchMarketConfig(config);
            marketConfigMongo = BeanCopyUtils.copyProperties(rcsMatchMarketConfig, MarketConfigMongo.class);
            marketConfigMongo.setMarketId(String.valueOf(rcsMatchMarketConfig.getMarketId()));
        }
        MatchSetVo matchSetTradeType = mongoTemplate.findOne(new Query().addCriteria(Criteria.where("matchId").is(config.getMatchId())
                .and("categoryId").is(config.getPlayId()).and("methodNo").is(MatchSetEnum.UPDATE_MARKET_TRADETYPE.getCode())), MatchSetVo.class);
        if (matchSetTradeType != null) {
            marketConfigMongo.setDataSource(Long.parseLong(matchSetTradeType.getParamValue()));
        }
        MatchSetVo matchSetStatus = mongoTemplate.findOne(new Query().addCriteria(Criteria.where("matchId").is(config.getMatchId())
                .and("categoryId").is(config.getPlayId()).and("marketPlaceNum").is(config.getMarketIndex()).and("methodNo").is(MatchSetEnum.UPDTAE_MARKET_STATUS.getCode())), MatchSetVo.class);
        if (matchSetStatus != null) {
            marketConfigMongo.setMarketStatus(Integer.parseInt(matchSetStatus.getParamValue()));
        }

        return marketConfigMongo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSnapOddsValue(UpdateOddsValueVo updateOddsValueVo) {
        RcsMatchMarketConfig config = new RcsMatchMarketConfig();
        List<OddsValueVo> oddsValueList = updateOddsValueVo.getOddsValueList();
        if (CollectionUtils.isEmpty(oddsValueList)) {
            return;
        }
        List<Map<String, Object>> oddsList = new ArrayList<>();

        oddsValueList.stream().forEach(oddsValueVo -> {
            BigDecimal v = new BigDecimal(String.valueOf(oddsValueVo.getValue())).multiply(new BigDecimal(String.valueOf(BaseConstants.MULTIPLE_VALUE)));
            Map<String, Object> map = new HashMap<>();
            map.put("id", oddsValueVo.getId());
            map.put("fieldOddsValue", v.intValue());
            if (oddsValueVo.getActive() != null)
                map.put("active", oddsValueVo.getActive());
            map.put("oddsType", oddsValueVo.getOddsType());
            oddsList.add(map);
        });
        Long matchId = updateOddsValueVo.getMatchId();
        Long playId = updateOddsValueVo.getPlayId();
        Long marketId = updateOddsValueVo.getMarketId();
        config.setMatchId(matchId);
        config.setPlayId(playId);
        config.setMarketId(marketId);
        config.setOddsList(oddsList);
        MarketConfigMongo marketConfigMongo = BeanCopyUtils.copyProperties(config, MarketConfigMongo.class);
        marketConfigMongo.setMarketId(String.valueOf(marketId));
        marketConfigMongo.setUpdateTime(DateUtils.getCurrentTime());
        Map map = new HashMap();
        map.put("matchId", matchId);
        map.put("playId", playId);
        map.put("marketId", String.valueOf(marketId));
        mongoService.upsert(map, "rcs_match_market_config", marketConfigMongo);
        sendMatchMarketSnapWS(config);

        MatchSetVo matchSetVo = new MatchSetVo();
        matchSetVo.setJsonParams(JsonFormatUtils.toJson(config));
        matchSetVo.setCategoryId(playId);
        matchSetVo.setMarketId(marketId);
        matchSetVo.setMatchId(matchId);
        matchSetVo.setMethodNo(MatchSetEnum.UPDATE_ODDS_VALUE.getCode());
        matchSetVo.setTradeLevel(TraderLevelEnum.MARKET.getLevel());
        matchSetVo.setParamValue(MatchSetEnum.UPDATE_ODDS_VALUE.getValue());
        matchSetVo.setUpdateTime(DateUtils.getCurrentTime());
        matchSetMongoService.upsertMatchSetMongo(matchSetVo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMarketOddsSnap(RcsMatchMarketConfig config) {
        Long matchId = config.getMatchId();
        Long playId = config.getPlayId();
        Long marketId = config.getMarketId();
        MarketConfigMongo marketConfigMongo = getMarketConfigMongo(config);
        // RcsMatchMarketConfig marketConfig= BeanCopyUtils.copyProperties(marketConfigMongo,RcsMatchMarketConfig.class);
        RcsMatchMarketConfig marketConfig = JSONObject.parseObject(JSONObject.toJSONString(marketConfigMongo), RcsMatchMarketConfig.class);
        List<Map<String, Object>> oddsList = marketConfig.getOddsList();

        BigDecimal margin = com.panda.sport.rcs.trade.util.MarginUtils.convert(oddsList, config.getMarketType(),config.getMargin());
        config.setMargin(margin);
        List<Map<String, Object>> odds = com.panda.sport.rcs.trade.util.MarginUtils.calculationOddsByMargin(oddsList, config.getMarketType(), config);
        config.setOddsList(odds);
        marketConfig.setMaxOdds(ObjectUtils.isEmpty(marketConfig.getMaxOdds()) ? new BigDecimal("100") : marketConfig.getMaxOdds());
        marketConfig.setMinOdds(ObjectUtils.isEmpty(marketConfig.getMinOdds()) ? new BigDecimal("1.01") : marketConfig.getMinOdds());
        tradeVerificationService.checkMaxAndMinOdds(marketConfig, config);

        sendMatchMarketSnapWS(marketConfig);
        Map map = new HashMap();
        map.put("matchId", matchId);
        map.put("playId", playId);
        map.put("marketId", String.valueOf(marketId));

        MarketConfigMongo marketConfigUpdate = BeanCopyUtils.copyProperties(marketConfig, MarketConfigMongo.class);
        marketConfigUpdate.setMarketId(String.valueOf(marketId));
        marketConfigUpdate.setUpdateTime(DateUtils.getCurrentTime());
        mongoService.upsert(map, "rcs_match_market_config", marketConfigUpdate);

        MatchSetVo matchSetVo = new MatchSetVo();
        matchSetVo.setJsonParams(JsonFormatUtils.toJson(config));
        matchSetVo.setCategoryId(playId);
        matchSetVo.setMarketId(marketId);
        matchSetVo.setMatchId(matchId);
        matchSetVo.setMethodNo(MatchSetEnum.UPDTAE_MARKET_ODDS_VALUE.getCode());
        matchSetVo.setTradeLevel(TraderLevelEnum.MARKET.getLevel());
        matchSetVo.setParamValue(MatchSetEnum.UPDTAE_MARKET_ODDS_VALUE.getValue());
        matchSetVo.setUpdateTime(DateUtils.getCurrentTime());
        matchSetMongoService.upsertMatchSetMongo(matchSetVo);
    }

    @Override
    @Transactional
    public RcsMatchMarketConfig updateMatchMarketMongo(RcsMatchMarketConfig config) {
        String json = JsonFormatUtils.toJson(config);
        Long matchId = config.getMatchId();
        Long playId = config.getPlayId();
        Long marketId = config.getMarketId();
        if (config.getAwayMarketValue() == null) config.setAwayMarketValue(BigDecimal.ZERO);
        if (config.getHomeMarketValue() == null) config.setHomeMarketValue(BigDecimal.ZERO);
        verifyData(config);
        Map map = new HashMap();
        map.put("matchId", matchId);
        map.put("playId", playId);
        map.put("marketId", String.valueOf(marketId));

        MarketConfigMongo marketConfigMongo = BeanCopyUtils.copyProperties(config, MarketConfigMongo.class);
        marketConfigMongo.setMarketId(String.valueOf(marketId));
        marketConfigMongo.setUpdateTime(DateUtils.getCurrentTime());
        mongoService.upsert(map, "rcs_match_market_config", marketConfigMongo);
        sendMatchMarketSnapWS(config);
        //插入操作执行记录
        MatchSetVo matchSetVo = new MatchSetVo();
        matchSetVo.setJsonParams(json);
        matchSetVo.setCategoryId(playId);
        matchSetVo.setMarketId(marketId);
        matchSetVo.setMatchId(matchId);
        matchSetVo.setMethodNo(MatchSetEnum.UPDTAE_MARKETCONFIG.getCode());
        matchSetVo.setTradeLevel(TraderLevelEnum.MARKET.getLevel());
        matchSetVo.setParamValue(MatchSetEnum.UPDTAE_MARKETCONFIG.getValue());
        matchSetVo.setUpdateTime(DateUtils.getCurrentTime());
        matchSetMongoService.upsertMatchSetMongo(matchSetVo);
        //设置状态
        if (null != config.getMarketStatus()) {
            MarketStatusUpdateVO statusUpdateVO = new MarketStatusUpdateVO()
                    .setMarketStatus(config.getMarketStatus())
                    .setCategoryId(playId)
                    .setMatchId(matchId)
                    .setMarketPlaceNum(config.getMarketIndex())
                    .setTradeLevel(TraderLevelEnum.MARKET.getLevel())
                    .setMarketId(String.valueOf(config.getMarketId()));
            marketStatusService.updatSnapshotStatus(statusUpdateVO);
        }
        return config;
    }

    void sendMatchMarketSnapWS(RcsMatchMarketConfig config) {
        LiveStandardMatchMarketMessageVO marketMessageVO = new LiveStandardMatchMarketMessageVO();
        marketMessageVO.setStandardMatchInfoId(config.getMatchId());
        marketMessageVO.setModifyTime(System.currentTimeMillis());
        marketMessageVO.setMarketList(new ArrayList<LiveStandardMarketMessageVO>());

        //查询玩法下面所有盘口
        Criteria criteria = Criteria.where("matchId").is(String.valueOf(config.getMatchId())).and("id").is(config.getPlayId());
        MarketCategory marketCategory = mongoTemplate.findOne(new Query().addCriteria(criteria), MarketCategory.class);
        if (marketCategory == null) return;
        List<MatchMarketVo> matchMarketVoList = marketCategory.getMatchMarketVoList();
        //获取缓存玩法下所有的位置状态
        Map<Integer, Integer> snapshotStatusMap = marketStatusService.getSnapshotMarketPlaceStatus(config.getMatchId(), config.getPlayId());

        if (!CollectionUtils.isEmpty(matchMarketVoList)) {
            matchMarketVoList.stream().forEach(model -> {
                if (model.getMarketId().equals(config.getMarketId())) {
                    List<Map<String, Object>> oddsList = config.getOddsList();
                    if (!CollectionUtils.isEmpty(oddsList)) {
                        model.getOddsFieldsList().stream().forEach(oddsVo -> {
                            oddsList.stream().forEach(mapOddId -> {
                                if (String.valueOf(mapOddId.get("id")).equals(String.valueOf(oddsVo.getId()))) {
                                    oddsVo.setFieldOddsValue(NumberUtils.getBigDecimal(mapOddId.get("fieldOddsValue")).setScale(0).toPlainString());
                                }
                            });
                        });
                    }
                }
                model.setDiffOddsValue(model.diffOddsValue());
            });
            //修改之后重新排序
            List<MatchMarketVo> collect = matchMarketVoList.stream().sorted(Comparator.comparing(MatchMarketVo::getDiffOddsValue)).collect(Collectors.toList());

            Integer marketPlaceNum = 1;
            for (MatchMarketVo matchMarketVo : collect) {
                LiveStandardMarketMessageVO market = new LiveStandardMarketMessageVO();
                market.setId(matchMarketVo.getId());
                market.setStandardMatchInfoId(config.getMatchId());
                market.setMarketCategoryId(matchMarketVo.getMarketCategoryId());
                market.setMarketType(matchMarketVo.getMarketType());
                market.setModifyTime(System.currentTimeMillis());
                market.setAddition1(matchMarketVo.getAddition1());
                market.setAddition2(matchMarketVo.getAddition2());
                market.setOddsMetric(marketPlaceNum);
                market.setStatus(matchMarketVo.getStatus());
                market.setThirdMarketSourceStatus(matchMarketVo.getThirdMarketSourceStatus());
                if (!CollectionUtils.isEmpty(snapshotStatusMap) && snapshotStatusMap.containsKey(marketPlaceNum)) {
                    market.setStatus(snapshotStatusMap.get(marketPlaceNum));
                }

                market.setMarketOddsList(new ArrayList<LiveStandardMarketOddsMessageVO>());
                List<MatchMarketOddsVo> oddsFieldsList = matchMarketVo.getOddsFieldsList();
                if (CollectionUtils.isEmpty(oddsFieldsList)) continue;
                oddsFieldsList.forEach(oddsVo -> {
                    LiveStandardMarketOddsMessageVO oddsMessageVO = new LiveStandardMarketOddsMessageVO();
                    oddsMessageVO.setId(oddsVo.getId());
                    oddsMessageVO.setActive(oddsVo.getActive());
                    oddsMessageVO.setPaOddsValue(Integer.parseInt(oddsVo.getFieldOddsValue()));
                    oddsMessageVO.setFieldOddsValue(oddsVo.getFieldOddsValue());
                    oddsMessageVO.setMarketId(matchMarketVo.getMarketId());
                    oddsMessageVO.setNextLevelOddsValue(oddsVo.getNextLevelOddsValue());
                    oddsMessageVO.setOddsType(oddsVo.getOddsType());
                    market.getMarketOddsList().add(oddsMessageVO);
                });
                marketMessageVO.getMarketList().add(market);
                marketPlaceNum++;
            }
        }
        producerSendMessageUtils.sendMessage(MARKET_ODDS_UPDTAE_SNAPSHOT_TOPIC, String.valueOf(config.getMatchId()), String.valueOf(config.getMarketId()), JsonFormatUtils.toJson(marketMessageVO));
    }
}
