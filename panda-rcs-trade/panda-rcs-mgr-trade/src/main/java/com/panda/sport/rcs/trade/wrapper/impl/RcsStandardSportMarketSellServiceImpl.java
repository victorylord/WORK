package com.panda.sport.rcs.trade.wrapper.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.util.UuidUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.panda.merge.api.ITradeMarketConfigApi;
import com.panda.merge.dto.ResultCode;
import com.panda.merge.dto.TradeCloseOpeartorDTO;
import com.panda.merge.dto.TradeMarketDisplayConfigDTO;
import com.panda.sport.data.association.api.MatchAdvanceSaleApi;
import com.panda.sport.data.association.api.Request;
import com.panda.sport.data.association.api.Response;
import com.panda.sport.data.association.api.enums.PreOrLiveEnum;
import com.panda.sport.data.association.api.enums.SaleMatchSellStausEnum;
import com.panda.sport.data.association.api.param.ConfirmSaleDTO;
import com.panda.sport.data.association.api.vo.LanguageInternationalVO;
import com.panda.sport.data.rcs.dto.tournament.StandardMarketSellQueryDto;
import com.panda.sport.rcs.common.DateUtils;
import com.panda.sport.rcs.common.OddsValueConvertUtils;
import com.panda.sport.rcs.core.utils.BeanCopyUtils;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.enums.SportIdEnum;
import com.panda.sport.rcs.enums.TradeEnum;
import com.panda.sport.rcs.enums.TradeLevelEnum;
import com.panda.sport.rcs.enums.TradeStatusEnum;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.mapper.*;
import com.panda.sport.rcs.mapper.tourTemplate.RcsTournamentTemplateAcceptConfigSettleMapper;
import com.panda.sport.rcs.mapper.tourTemplate.RcsTournamentTemplateAcceptEventSettleMapper;
import com.panda.sport.rcs.mapper.tourTemplate.RcsTournamentTemplateMapper;
import com.panda.sport.rcs.mapper.tourTemplate.RcsTournamentTemplatePlayMargainMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.*;
import com.panda.sport.rcs.pojo.dto.ClearDTO;
import com.panda.sport.rcs.pojo.dto.ClearSubDTO;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplate;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateAcceptConfig;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateAcceptConfigSettle;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateAcceptEventSettle;
import com.panda.sport.rcs.trade.enums.DataSourceWeightEnum;
import com.panda.sport.rcs.trade.enums.LinkedTypeEnum;
import com.panda.sport.rcs.trade.enums.MatchTypeEnum;
import com.panda.sport.rcs.trade.enums.TempTypeEnum;
import com.panda.sport.rcs.trade.service.RcsMatchEventTypeInfoServiceImpl;
import com.panda.sport.rcs.trade.service.TradeStatusService;
import com.panda.sport.rcs.trade.service.impl.TradeModeServiceImpl;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.vo.tourTemplate.templateToBusiness.MatchTournamentTemplateVo;
import com.panda.sport.rcs.trade.vo.tourTemplate.templateToBusiness.TournamentTemplateAcceptConfigSettleVo;
import com.panda.sport.rcs.trade.vo.tourTemplate.templateToBusiness.TournamentTemplateAcceptEventSettleVo;
import com.panda.sport.rcs.trade.wrapper.RcsStandardSportMarketSellService;
import com.panda.sport.rcs.trade.wrapper.RcsTradeConfigService;
import com.panda.sport.rcs.utils.CommonUtils;
import com.panda.sport.rcs.utils.DataRealtimeApiUtils;
import com.panda.sport.rcs.utils.TradeUserUtils;
import com.panda.sport.rcs.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.dubbo.common.utils.Assert;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName RcsStandardSportMarketSellServiceImpl
 * @Description: TODO
 * @Author carver
 * @Date 2019/12/30
 **/
@Service
@Slf4j
public class RcsStandardSportMarketSellServiceImpl extends ServiceImpl<RcsStandardSportMarketSellMapper, RcsStandardSportMarketSell> implements RcsStandardSportMarketSellService {
    @Resource
    private RcsStandardSportMarketSellMapper rcsStandardSportMarketSellMapper;
    @Autowired
    private StandardSportMarketMapper standardSportMarketMapper;
    @Autowired
    private StandardSportMarketOddsMapper standardSportMarketOddsMapper;
    @Reference(retries = 3, lazy = true, check = false)
    private MatchAdvanceSaleApi matchAdvanceSaleApi;
    @Reference(retries = 3, lazy = true, check = false)
    private ITradeMarketConfigApi iTradeMarketConfigApi;
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    private RcsTournamentTemplateAcceptConfigMapper configMapper;
    @Autowired
    private TradeModeServiceImpl tradeModeService;
    @Autowired
    private TradeStatusService tradeStatusService;
    @Autowired
    private RcsTradeConfigService rcsTradeConfigService;
    @Autowired
    private RcsMatchEventTypeInfoServiceImpl rcsMatchEventTypeInfoService;
    @Autowired
    private RcsTournamentTemplateMapper rcsTournamentTemplateMapper;
    @Autowired
    private RcsTournamentTemplatePlayMargainMapper rcsTournamentTemplatePlayMargainMapper;
    @Autowired
    private StandardMatchInfoMapper standardMatchInfoMapper;
    @Autowired
    ProducerSendMessageUtils producerSendMessageUtils;
    @Autowired
    private RcsTournamentTemplateAcceptConfigSettleMapper configSettleMapper;

    @Autowired
    private RcsTournamentTemplateAcceptEventSettleMapper eventSettleMapper;

    @Autowired
    private ProducerSendMessageUtils sendMessage;

    @Override
    public IPage<StandardMarketSellVo> listStandardMarketSellVo(StandardMarketSellQueryVo standardMarketSellQueryVo) {
        //接收前端传值
        Integer historyFlag = standardMarketSellQueryVo.getHistoryFlag();
        Integer isEarlyTrading = standardMarketSellQueryVo.getIsEarlyTrading();
        //构建赛事开始和结束时间
        if (standardMarketSellQueryVo.getBeginTime() != null) {
            String date = DateUtils.transferLongToDateString(standardMarketSellQueryVo.getBeginTime());
            Date matchDate = DateUtils.dateStrToDate(date + " 12:00:00");
            if (historyFlag == 0 && isEarlyTrading == 1) {
                //其他早盘数据，保持7天的数据，例如当前时间7号，那就是到7到14号的数据
                standardMarketSellQueryVo.setBeginTimeMillis(matchDate.getTime());
                long endTimeLong = DateUtils.addNDay(matchDate, 7).getTime();
                standardMarketSellQueryVo.setEndTimeMillis(endTimeLong - 1L);
            } else {
                //中午12点到第二天中午11点59分
                standardMarketSellQueryVo.setBeginTimeMillis(matchDate.getTime());
                long endTimeLong = DateUtils.addNDay(matchDate, 1).getTime();
                standardMarketSellQueryVo.setEndTimeMillis(endTimeLong - 1L);
            }
        }

        IPage<StandardMarketSellVo> iPage = new Page<>(standardMarketSellQueryVo.getCurrentPage(), standardMarketSellQueryVo.getPageSize());
        IPage<StandardMarketSellVo> rtnList = rcsStandardSportMarketSellMapper.selectRcsStandardSportMarketSell(iPage, standardMarketSellQueryVo);
        if (standardMarketSellQueryVo.getMatchInfoId() != null) {
            setMarketOdds(rtnList.getRecords());
        }
        List<StandardMarketSellVo> records = rtnList.getRecords();
        if (records == null || records.size() <= 0) return rtnList;

        List<Long> matchIdList = records.stream().map(mapper -> mapper.getMatchInfoId()).collect(Collectors.toList());

        Request<List<Long>> request = new Request<List<Long>>();
        request.setData(matchIdList);
        request.setLinkId(UUID.randomUUID().toString().replace("-", ""));
        Response<List<LanguageInternationalVO>> response = matchAdvanceSaleApi.getLanguageInternational(request);
        log.info("::{}::开售列表查询融合国际化信息：request：{}，response：{}", request, response,CommonUtil.getRequestId(standardMarketSellQueryVo.getMatchInfoId()));
        if (!response.isSuccess()) {
            throw new RcsServiceException("调用融合接口获取国际话数据失败！" + response.getMsg());
        }
        List<LanguageInternationalVO> i18nList = response.getData();
        for (int i = 0; i < records.size(); i++) {
            StandardMarketSellVo standardMarketSellVo = records.get(i);
            LanguageInternationalVO i18nVo = i18nList.get(i);

            standardMarketSellVo.setTournamentNameCn(i18nVo.getLeagueName().get("zs"));
            standardMarketSellVo.setTournamentNameEn(i18nVo.getLeagueName().get("en"));

            standardMarketSellVo.setTeamHomeNameCn(i18nVo.getHomeTeamleagueName().get("zs"));
            standardMarketSellVo.setTeamHomeNameEn(i18nVo.getHomeTeamleagueName().get("en"));

            standardMarketSellVo.setTeamAwayNameCn(i18nVo.getAwayTeamleagueName().get("zs"));
            standardMarketSellVo.setTeamAwayNameEn(i18nVo.getAwayTeamleagueName().get("en"));

            Long currentTime = System.currentTimeMillis();
            //未售
            String unsold = SaleMatchSellStausEnum.Unsold.toString();
            //逾期未售
            String overdueUnsold = SaleMatchSellStausEnum.Overdue_Unsold.toString();
            //处理赛前操盘手到时间未确认开售，是否警告
            Long preMatchTime = standardMarketSellVo.getPreMatchTime();
            String preMatchSellStatus = standardMarketSellVo.getPreMatchSellStatus();
            if (preMatchTime != null && preMatchTime != 0) {
                boolean existed = currentTime.longValue() > preMatchTime.longValue() && unsold.equals(preMatchSellStatus) || overdueUnsold.equals(preMatchSellStatus) || StringUtils.isEmpty(preMatchSellStatus);
                if (existed) standardMarketSellVo.setPreIsWarn(1);
            }

            //处理滚球操盘手到时间未确认开售，是否警告
            Long beginTime = standardMarketSellVo.getBeginTime();
            String liveMatchSellStatus = standardMarketSellVo.getLiveMatchSellStatus();
            if (beginTime != null && beginTime != 0) {
                boolean existed = currentTime.longValue() > beginTime.longValue() && unsold.equals(liveMatchSellStatus) || overdueUnsold.equals(liveMatchSellStatus) || StringUtils.isEmpty(liveMatchSellStatus);
                if (existed) standardMarketSellVo.setLiveIsWarn(1);
            }

            //设置历史赛程数据，给前端使用，禁用所有操作
            if (historyFlag == 1) {
                standardMarketSellVo.setHistoryFlag(1);
            }
        }
        return rtnList;
    }

    /**
     * 设置盘口赔率
     *
     * @param rtnList
     */
    private void setMarketOdds(List<StandardMarketSellVo> rtnList) {
        try {
            List<Long> marketIds = new ArrayList<>();
            if (CollectionUtils.isEmpty(rtnList)) return;
            List<StandardSportMarket> standardSportMarkets = Lists.newArrayList();
            for (StandardMarketSellVo standardMarketSellVo : rtnList) {
                Long[] plays = {1L, 2L, 4L};
                for (Long id : plays) {
                    QueryWrapper<StandardSportMarket> orderByStandardSportMarket = new QueryWrapper<>();
                    orderByStandardSportMarket.lambda().eq(StandardSportMarket::getMarketCategoryId, id);
                    orderByStandardSportMarket.lambda().in(StandardSportMarket::getStandardMatchInfoId, standardMarketSellVo.getMatchInfoId());
                    orderByStandardSportMarket.orderByAsc("odds_metric");
                    List<StandardSportMarket> orderByList = standardSportMarketMapper.selectList(orderByStandardSportMarket);
                    if (orderByList.size() > 0) {
                        standardSportMarkets.add(orderByList.get(0));
                    }
                }
            }

            for (StandardSportMarket standardSportMarket : standardSportMarkets) {
                marketIds.add(standardSportMarket.getId());
            }
            if (CollectionUtils.isEmpty(marketIds)) return;

            QueryWrapper<StandardSportMarketOdds> standardSportMarketOddsQueryWrapper = new QueryWrapper<>();
            standardSportMarketOddsQueryWrapper.lambda().in(!CollectionUtils.isEmpty(marketIds), StandardSportMarketOdds::getMarketId, marketIds);
            List<StandardSportMarketOdds> standardSportMarketOdds = standardSportMarketOddsMapper.selectList(standardSportMarketOddsQueryWrapper);
            if (CollectionUtils.isEmpty(standardSportMarketOdds)) standardSportMarketOdds = new ArrayList<>();
            for (StandardMarketSellVo standardMarketSellVo : rtnList) {
                Set<Long> playIds = new HashSet<>();
                for (StandardSportMarket standardSportMarket : standardSportMarkets) {
                    if (standardSportMarket.getStandardMatchInfoId().longValue() == standardMarketSellVo.getMatchInfoId().longValue()) {
                        playIds.add(standardSportMarket.getMarketCategoryId());
                    }
                }
                List<CategorySellVo> categorySellVos = new ArrayList<>();
                standardMarketSellVo.setCategorySellVos(categorySellVos);
                for (Long playId : playIds) {
                    for (StandardSportMarket standardSportMarket : standardSportMarkets) {
                        if ((standardSportMarket.getStandardMatchInfoId().longValue() == standardMarketSellVo.getMatchInfoId().longValue()) && standardSportMarket.getMarketCategoryId().longValue() == playId.longValue()) {
                            CategorySellVo categorySellVo = new CategorySellVo();
                            categorySellVo.setId(playId);
                            List<CategorySellVo.MatchMarketSellVo> matchMarketVoList = new ArrayList<>();
                            categorySellVo.setMatchMarketVoList(matchMarketVoList);
                            categorySellVos.add(categorySellVo);

                            CategorySellVo.MatchMarketSellVo matchMarketSellVo = new CategorySellVo.MatchMarketSellVo();
                            matchMarketVoList.add(matchMarketSellVo);
                            matchMarketSellVo.setOddsMetric(standardSportMarket.getOddsMetric());
                            matchMarketSellVo.setId(standardSportMarket.getId());
                            List<CategorySellVo.MatchMarketOddsFieldSellVo> oddsFieldsList = new ArrayList<>();
                            matchMarketSellVo.setOddsFieldsList(oddsFieldsList);
                            for (StandardSportMarketOdds standardSportMarketOdd : standardSportMarketOdds) {
                                if (standardSportMarket.getId().longValue() != standardSportMarketOdd.getMarketId())
                                    continue;
                                CategorySellVo.MatchMarketOddsFieldSellVo matchMarketOddsFieldSellVo = new CategorySellVo.MatchMarketOddsFieldSellVo();
                                oddsFieldsList.add(matchMarketOddsFieldSellVo);
                                matchMarketOddsFieldSellVo.setNameExpressionValue(standardSportMarketOdd.getNameExpressionValue());
                                String displayOddsVal = OddsValueConvertUtils.convertAndDefaultDisply(null, standardSportMarketOdd.getOddsValue());
                                matchMarketOddsFieldSellVo.setFieldOddsValue(displayOddsVal);
                                matchMarketOddsFieldSellVo.setFieldOddsOriginValue(standardSportMarketOdd.getOddsValue());
                                matchMarketOddsFieldSellVo.setId(standardSportMarketOdd.getId());
                                matchMarketOddsFieldSellVo.setMarketId(standardSportMarketOdd.getMarketId());
                                matchMarketOddsFieldSellVo.setOddsType(standardSportMarketOdd.getOddsType());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("开售设定盘口错误" + e.getMessage(), e);
        }
    }


    @Override
    public HttpResponse confirmStandardMarketSell(StandardMarketSellQueryVo standardMarketSellQueryVo) {
        //接收前端传值
        Long id = standardMarketSellQueryVo.getId();
        Long matchInfoId = standardMarketSellQueryVo.getMatchInfoId();
        Integer sellDateType = standardMarketSellQueryVo.getSellDateType();
        Long sellMatchDate = standardMarketSellQueryVo.getSellMatchDate();
        //log.info("::{}::前端参数-执行融合确认开售时间API，返回结果：matchInfoId：" + matchInfoId + "  sellDateType：" + sellDateType + "   sellMatchDate：" + sellMatchDate,CommonUtil.getRequestId(matchInfoId));
        RcsStandardSportMarketSell result = rcsStandardSportMarketSellMapper.selectById(id);
        if (result == null) throw new RcsServiceException("开售数据不完整");
        boolean preBoolean = true;
        boolean liveBoolean = true;
        if (StringUtils.isBlank(result.getPreRiskManagerCode()) || StringUtils.isBlank(result.getPreMatchDataProviderCode()) || StringUtils.isBlank(result.getPreTraderStatus()) || (!result.getPreTraderStatus().equals("Setted"))
        ) {
            preBoolean = false;
        }
        if (StringUtils.isBlank(result.getLiveRiskManagerCode()) || StringUtils.isBlank(result.getLiveMatchDataProviderCode()) || StringUtils.isBlank(result.getLiveTraderStatus()) || (!result.getLiveTraderStatus().equals("Setted"))
        ) {
            liveBoolean = false;
        }
        if (!(preBoolean || liveBoolean)) {
            throw new RcsServiceException("开售数据不完整,还不能够开盘");
        }
        //构建风控确认开售时间对象
        RcsStandardSportMarketSell rcsStandardSportMarketSell = new RcsStandardSportMarketSell();
        rcsStandardSportMarketSell.setId(id);
        rcsStandardSportMarketSell.setModifyTime(System.currentTimeMillis());
        //构建融合确认开售时间API对象
        ConfirmSaleDTO confirmSaleDTO = new ConfirmSaleDTO();
        confirmSaleDTO.setStandardMatchId(matchInfoId);
        confirmSaleDTO.setMatchONnSaleTime(sellMatchDate);
        if (sellDateType == 1) {
            //校验数据是否设置操盘手
            Assert.notNull(result.getPreTrader(), "赛前操盘手未设置！");
            //赛前盘数据
            confirmSaleDTO.setPreOrLiveEnum(PreOrLiveEnum.PRE);
            rcsStandardSportMarketSell.setPreMatchTime(sellMatchDate);
            rcsStandardSportMarketSell.setPreMatchSellStatus(SaleMatchSellStausEnum.Sold.toString());
        } else if (sellDateType == 2) {
            //校验数据是否设置操盘手
            Assert.notNull(result.getLiveTrader(), "滚球操盘手未设置！");
            //滚球盘数据
            confirmSaleDTO.setPreOrLiveEnum(PreOrLiveEnum.LIVE);
            rcsStandardSportMarketSell.setLiveOddTime(sellMatchDate);
            rcsStandardSportMarketSell.setLiveMatchSellStatus(SaleMatchSellStausEnum.Sold.toString());
        } else {
            throw new IllegalArgumentException("开售时间类型有误，请核实！1:赛前开售时间 2：滚球开售时间");
        }
        Request<ConfirmSaleDTO> request = new Request();
        request.setData(confirmSaleDTO);
        String linkId = UuidUtils.generateUuid();
        request.setLinkId(linkId);
        Response response = matchAdvanceSaleApi.confirmSale(request);
        log.info("::{}::调用融合-执行融合确认开售时间APImatchInfoId={}, 返回结果={}, 消息={}" , linkId, response.isSuccess(), response.getMsg());
        if (response.isSuccess()) {
            rcsStandardSportMarketSellMapper.updateById(rcsStandardSportMarketSell);
            return HttpResponse.success();
        } else {
            return HttpResponse.error(201, "执行融合确认开售时间API，返回结果有误:" + response + ">>>请求参数：" + confirmSaleDTO);
        }
    }

    @Override
    public HttpResponse configPlayShow(StandardMarketSellQueryVo standardMarketSellQueryVo) {
        //接收前端传值
        Long matchInfoId = standardMarketSellQueryVo.getMatchInfoId();
        Integer cornerShow = standardMarketSellQueryVo.getCornerShow();
        Integer cardShow = standardMarketSellQueryVo.getCardShow();
        // 发送信息给融合
        try {
            TradeMarketDisplayConfigDTO tradeMarketDisplayConfigDTO = new TradeMarketDisplayConfigDTO();
            tradeMarketDisplayConfigDTO.setMatchId(matchInfoId);
            tradeMarketDisplayConfigDTO.setDisplayCorner(cornerShow == 1 ? true : false);
            tradeMarketDisplayConfigDTO.setDisplayPenalty(cardShow == 1 ? true : false);
            com.panda.merge.dto.Request request = new com.panda.merge.dto.Request();
            request.setData(tradeMarketDisplayConfigDTO);
            String uuid = UuidUtils.generateUuid();
            request.setLinkId(uuid);
            request.setOperaterId(TradeUserUtils.getUserIdNoException().longValue());
            com.panda.merge.dto.Response response = iTradeMarketConfigApi.putTradeMarketDisplayConfig(request);
            log.info("::{}::执行融合确认开售，设置盘口数/角球/罚牌API，返回结果：matchInfoId：" + matchInfoId + "  linkId：" + uuid + "  响应结果：" + response.getMsg(),CommonUtil.getRequestId(matchInfoId));
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(matchInfoId), e.getMessage(), e);
            return HttpResponse.error(500, "执行融合确认开售，设置盘口数/角球/罚牌API，调用异常：" + e.getMessage());
        }

        //保存数据
        UpdateWrapper<RcsStandardSportMarketSell> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("match_info_id", matchInfoId);
        RcsStandardSportMarketSell rcsStandardSportMarketSell = new RcsStandardSportMarketSell();
        rcsStandardSportMarketSell.setCardShow(cardShow);
        rcsStandardSportMarketSell.setCornerShow(cornerShow);
        rcsStandardSportMarketSell.setModifyTime(System.currentTimeMillis());
        rcsStandardSportMarketSellMapper.update(rcsStandardSportMarketSell, updateWrapper);
        return HttpResponse.success();
    }

    @Override
    public List getMatchNumberByType() {
        //构建赛事开始和结束时间
        String date = DateUtils.getStandardCurrentTime();
        Date matchDate = DateUtils.dateStrToDate(date + " 12:00:00");
        //构建开始账务日期中午12点
        long beginTimeMillis = matchDate.getTime();
        long currentTime = System.currentTimeMillis();
        if (currentTime < beginTimeMillis) {
            //当前日期小于12点，所以需要往前推一天，构建昨天日期
            long oneDay = 24 * 60 * 60 * 1000;
            beginTimeMillis = beginTimeMillis - oneDay;
        }
        //构建结束账务日期，因为开售列表数据，其他早盘只统计延后7天的数据，所以这里需控制时间维度，加上日期条件和早盘日期条件共14天的维度
        long endTimeLong = DateUtils.addNDay(matchDate, 14).getTime();
        long endTimeMillis = endTimeLong - 1L;
        return rcsStandardSportMarketSellMapper.getMatchNumberByType(beginTimeMillis, endTimeMillis);
    }

    @Override
    public RcsStandardSportMarketSell selectStandardMarketSellVo(Long matchInfoId) {
        QueryWrapper<RcsStandardSportMarketSell> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(RcsStandardSportMarketSell::getMatchInfoId, matchInfoId);
        return rcsStandardSportMarketSellMapper.selectOne(wrapper);
    }

    @Override
    public int delete(StandardMarketSellQueryVo standardMarketSellQueryVo) {
        UpdateWrapper<RcsStandardSportMarketSell> wrapper = new UpdateWrapper<>();
        wrapper.lambda().eq(RcsStandardSportMarketSell::getId, standardMarketSellQueryVo.getId());
        RcsStandardSportMarketSell rcsStandardSportMarketSell = new RcsStandardSportMarketSell();
        rcsStandardSportMarketSell.setIsDelete(1);
        return rcsStandardSportMarketSellMapper.update(rcsStandardSportMarketSell, wrapper);
    }

    @Override
    public List<Long> queryTraderMatchIds(RcsMatchCollection co) {
        return rcsStandardSportMarketSellMapper.queryTraderMatchIds(co);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HttpResponse configChangeWeight(StandardMarketSellQueryDto standardMarketSellQueryVo) {
        //数据源切换赛事是否关盘   1：赛事关盘   2：赛事不关盘
        Integer infoMsg = null;
        String event = "";
        if (Objects.nonNull(standardMarketSellQueryVo.getSrWeight()) && standardMarketSellQueryVo.getSrWeight().equals(DataSourceWeightEnum.SR.getId())) {
            event = "SR";
        } else if (Objects.nonNull(standardMarketSellQueryVo.getBcWeight()) && standardMarketSellQueryVo.getBcWeight().equals(DataSourceWeightEnum.BC.getId())) {
            event = "BC";
        } else if (Objects.nonNull(standardMarketSellQueryVo.getBgWeight()) && standardMarketSellQueryVo.getBgWeight().equals(DataSourceWeightEnum.BG.getId())) {
            event = "BG";
        } else if (Objects.nonNull(standardMarketSellQueryVo.getTxWeight()) && standardMarketSellQueryVo.getTxWeight().equals(DataSourceWeightEnum.TX.getId())) {
            event = "TX";
        } else if (Objects.nonNull(standardMarketSellQueryVo.getRbWeight()) && standardMarketSellQueryVo.getRbWeight().equals(DataSourceWeightEnum.RB.getId())) {
            event = "RB";
        } else if (Objects.nonNull(standardMarketSellQueryVo.getPdWeight()) && standardMarketSellQueryVo.getPdWeight().equals(DataSourceWeightEnum.PD.getId())) {
            event = "PD";
        } else if (Objects.nonNull(standardMarketSellQueryVo.getAoWeight()) && standardMarketSellQueryVo.getAoWeight().equals(DataSourceWeightEnum.AO.getId())) {
            event = "AO";
        } else if (Objects.nonNull(standardMarketSellQueryVo.getPiWeight()) && standardMarketSellQueryVo.getPiWeight().equals(DataSourceWeightEnum.PI.getId())) {
            event = "PI";
        } else if (Objects.nonNull(standardMarketSellQueryVo.getLsWeight()) && standardMarketSellQueryVo.getLsWeight().equals(DataSourceWeightEnum.LS.getId())) {
            event = "LS";
        }

        //1.开售的赛事，赔率源权重切换
        if (standardMarketSellQueryVo.getMatchSellStatus().equals("Sold")) {
            StandardMatchInfo standardMatchInfo = standardMatchInfoMapper.selectById(standardMarketSellQueryVo.getMatchId());
            log.info("::{}::已开售赛事-赔率源切换-赛事信息:{}",CommonUtil.getRequestId(standardMarketSellQueryVo.getMatchId()), JsonFormatUtils.toJson(standardMatchInfo));
            //更新赛事模板赔率源权重优先级
            this.updateTemplateWeight(standardMarketSellQueryVo);

            //赛事关盘逻辑处理
            if (standardMarketSellQueryVo.getMarketType().equals("PRE")) {
                //早盘关盘
                infoMsg = this.closeMarket(standardMarketSellQueryVo, standardMatchInfo);
            } else if (standardMarketSellQueryVo.getMarketType().equals("LIVE")) {
                if (standardMatchInfo.getOddsLive().equals(NumberUtils.INTEGER_ZERO)) {
                    //标识赛事不关盘
                    infoMsg = 2;
                    //特殊情况，早盘期间切换滚球数据源
                    TradeCloseOpeartorDTO dto = new TradeCloseOpeartorDTO();
                    dto.setMatchId(standardMarketSellQueryVo.getMatchId());
                    //只会滚球调用，默认为0
                    dto.setMarketType(0);
                    dto.setDataSourceCode(event);
                    com.panda.merge.dto.Response response = DataRealtimeApiUtils.handleApi(dto, new DataRealtimeApiUtils.ApiCall() {
                        @Override
                        @Trace
                        public <R> com.panda.merge.dto.Response<R> callApi(com.panda.merge.dto.Request request) {
                            return iTradeMarketConfigApi.checkChangeOpeartor(request);
                        }
                    });
                    //融合校验是否有赔率，是否需要关盘处理   600：关盘   601：不关盘
                    long code = ResultCode.NEEDCLOSE.getCode();
                    if (response.getCode() == code) {
                        //滚球关盘
                        infoMsg = this.closeMarket(standardMarketSellQueryVo, standardMatchInfo);
                    }
                } else {
                    //滚球关盘
                    infoMsg = this.closeMarket(standardMarketSellQueryVo, standardMatchInfo);
                }
            }

        }

        //2.更新赛事模板滚球接拒单事件源
        if (standardMarketSellQueryVo.getMarketType().equals("LIVE")) {
            if (!standardMarketSellQueryVo.getTxWeight().equals(DataSourceWeightEnum.TX.getId())) {
                standardMarketSellQueryVo.setDataSouceCode(event);
                this.updateTemplateEventSourceConfig(standardMarketSellQueryVo);
            }
        }
        return HttpResponse.success(infoMsg);
    }

    /**
     * 赛事关盘
     *
     * @param standardMarketSellQueryVo
     */
    private Integer closeMarket(StandardMarketSellQueryDto standardMarketSellQueryVo, StandardMatchInfo standardMatchInfo) {
        //赛事封盘
        MarketStatusUpdateVO statusVo = new MarketStatusUpdateVO()
                .setTradeLevel(TradeLevelEnum.MATCH.getLevel())
                .setLinkedType(LinkedTypeEnum.DATA_SOURCE_WEIGHT.getCode())
                .setMatchId(standardMarketSellQueryVo.getMatchId())
                .setMarketStatus(TradeStatusEnum.CLOSE.getStatus());
        log.info("::{}::已开售赛事-赔率源切换-赛事关盘:{}",CommonUtil.getRequestId(standardMatchInfo.getId()), JsonFormatUtils.toJson(statusVo));
        tradeStatusService.updateTradeStatus(statusVo);

        // 非A模式切换成A模式
        Map<Long, Integer> tradeModeMap = rcsTradeConfigService.getTradeMode(standardMarketSellQueryVo.getMatchId(), null);
        if (!CollectionUtils.isEmpty(tradeModeMap)) {
            List<Long> notAutoPlayIds = Lists.newArrayList();
            tradeModeMap.forEach((playId, tradeMode) -> {
                // 非A模式切换成A模式，L模式不切换成A模式
                if (!TradeEnum.isAuto(tradeMode) && !TradeEnum.isLinkage(tradeMode)) {
                    notAutoPlayIds.add(playId);
                }
            });
            if (!CollectionUtils.isEmpty(notAutoPlayIds)) {
                MarketStatusUpdateVO tradeTypeVo = new MarketStatusUpdateVO()
                        .setTradeLevel(TradeLevelEnum.BATCH_PLAY.getLevel())
                        .setLinkedType(LinkedTypeEnum.DATA_SOURCE_WEIGHT.getCode())
                        .setMatchId(standardMarketSellQueryVo.getMatchId())
                        .setCategoryIdList(notAutoPlayIds)
                        .setTradeType(TradeEnum.AUTO.getCode());
                log.info("::{}::已开售赛事-赔率源切换-玩法切换成自动:{}",CommonUtil.getRequestId(standardMarketSellQueryVo.getMatchId()), JsonFormatUtils.toJson(tradeTypeVo));
                tradeModeService.updateTradeMode(tradeTypeVo);
            }
        }

        ClearDTO clearDTO = new ClearDTO();
        clearDTO.setType(0);
        clearDTO.setClearType(8);
        clearDTO.setMatchId(standardMatchInfo.getId());
        clearDTO.setBeginTime(standardMatchInfo.getBeginTime());
        ArrayList<ClearSubDTO> objects = new ArrayList<>();
        ClearSubDTO clearSubDTO = new ClearSubDTO();
        clearSubDTO.setMatchId(standardMatchInfo.getId());
        objects.add(clearSubDTO);
        clearDTO.setList(objects);
        producerSendMessageUtils.sendMessage("RCS_CLEAR_MATCH_MARKET_TAG", null, UuidUtils.generateUuid(), clearDTO);

        //数据源切换赛事是否关盘   1：赛事关盘   2：赛事不关盘
        Integer infoMsg = 1;
        return infoMsg;
    }

    /**
     * 更新赛事模板权重优先级
     *
     * @param standardMarketSellQueryVo
     * @return
     */
    private RcsTournamentTemplate updateTemplateWeight(StandardMarketSellQueryDto standardMarketSellQueryVo) {
        String weight = DataSourceWeightEnum.SR.getName();
        if (Objects.nonNull(standardMarketSellQueryVo.getSrWeight()) && standardMarketSellQueryVo.getSrWeight().equals(DataSourceWeightEnum.SR.getId())) {
            weight = DataSourceWeightEnum.SR.getName();
        } else if (Objects.nonNull(standardMarketSellQueryVo.getBcWeight()) && standardMarketSellQueryVo.getBcWeight().equals(DataSourceWeightEnum.BC.getId())) {
            weight = DataSourceWeightEnum.BC.getName();
        } else if (Objects.nonNull(standardMarketSellQueryVo.getBgWeight()) && standardMarketSellQueryVo.getBgWeight().equals(DataSourceWeightEnum.BG.getId())) {
            weight = DataSourceWeightEnum.BG.getName();
        } else if (Objects.nonNull(standardMarketSellQueryVo.getTxWeight()) && standardMarketSellQueryVo.getTxWeight().equals(DataSourceWeightEnum.TX.getId())) {
            weight = DataSourceWeightEnum.TX.getName();
        } else if (Objects.nonNull(standardMarketSellQueryVo.getRbWeight()) && standardMarketSellQueryVo.getRbWeight().equals(DataSourceWeightEnum.RB.getId())) {
            weight = DataSourceWeightEnum.RB.getName();
        } else if (Objects.nonNull(standardMarketSellQueryVo.getPdWeight()) && standardMarketSellQueryVo.getPdWeight().equals(DataSourceWeightEnum.PD.getId())) {
            weight = DataSourceWeightEnum.PD.getName();
        } else if (Objects.nonNull(standardMarketSellQueryVo.getAoWeight()) && standardMarketSellQueryVo.getAoWeight().equals(DataSourceWeightEnum.AO.getId())) {
            weight = DataSourceWeightEnum.AO.getName();
        } else if (Objects.nonNull(standardMarketSellQueryVo.getPiWeight()) && standardMarketSellQueryVo.getPiWeight().equals(DataSourceWeightEnum.PI.getId())) {
            weight = DataSourceWeightEnum.PI.getName();
        } else if (Objects.nonNull(standardMarketSellQueryVo.getLsWeight()) && standardMarketSellQueryVo.getLsWeight().equals(DataSourceWeightEnum.LS.getId())) {
            weight = DataSourceWeightEnum.LS.getName();
        }
        Integer marketType = null;
        if (standardMarketSellQueryVo.getMarketType().equals("PRE")) {
            marketType = MatchTypeEnum.EARLY.getId();
        } else if (standardMarketSellQueryVo.getMarketType().equals("LIVE")) {
            marketType = MatchTypeEnum.LIVE.getId();
        }
        RcsTournamentTemplate template = new RcsTournamentTemplate();
        template.setDataSourceCode(weight);
        template.setTypeVal(standardMarketSellQueryVo.getMatchId());
        template.setMatchType(marketType);
        template.setSportId(standardMarketSellQueryVo.getSportId().intValue());
        log.info("::{}::早盘/滚球/开售，修改赔率源权重优先级：{}",CommonUtil.getRequestId(standardMarketSellQueryVo.getMatchId()), JsonFormatUtils.toJson(template));
        rcsTournamentTemplateMapper.updateTemplateWeight(template);
        return template;
    }

    /**
     * 更新赛事模板赔率源权重优先级
     *
     * @param standardMarketSellQueryVo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePlayMarginIsSellByPlayId(StandardMarketSellQueryDto standardMarketSellQueryVo) {
        //跟新赛事模板权重优先级
        RcsTournamentTemplate template = this.updateTemplateWeight(standardMarketSellQueryVo);
        //开售更新赛事模板玩法，标记已开售
        rcsTournamentTemplatePlayMargainMapper.updatePlayMargainIsSellByPlayId(template, standardMarketSellQueryVo.getMarketCategoryIds());
        //判断该足球滚球赛事是否配置接拒单事件
        if (standardMarketSellQueryVo.getMarketType().equals("LIVE") && standardMarketSellQueryVo.getSportId().intValue() == NumberUtils.INTEGER_ONE) {
            rcsMatchEventTypeInfoService.queryEventByMatchId(standardMarketSellQueryVo.getMatchId());
            //更新赛事模板接拒单事件源
            QueryWrapper<RcsStandardSportMarketSell> wrapper = new QueryWrapper<>();
            wrapper.lambda().eq(RcsStandardSportMarketSell::getMatchInfoId, standardMarketSellQueryVo.getMatchId());
            RcsStandardSportMarketSell marketSell = rcsStandardSportMarketSellMapper.selectOne(wrapper);
            if (ObjectUtils.isNotEmpty(marketSell)) {
                standardMarketSellQueryVo.setDataSouceCode(marketSell.getBusinessEvent());
                this.updateTemplateEventSourceConfig(standardMarketSellQueryVo);
            }
        }
    }

    /**
     * 更新赛事模板接拒单事件源切换
     *
     * @param standardMarketSellQueryVo
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTemplateEventSourceConfig(StandardMarketSellQueryDto standardMarketSellQueryVo) {
        QueryWrapper<RcsTournamentTemplate> templateQueryWrapper = new QueryWrapper();
        templateQueryWrapper.lambda().eq(RcsTournamentTemplate::getType, TempTypeEnum.MATCH.getId())
                .eq(RcsTournamentTemplate::getTypeVal, standardMarketSellQueryVo.getMatchId())
                .eq(RcsTournamentTemplate::getMatchType, MatchTypeEnum.LIVE.getId())
                .eq(RcsTournamentTemplate::getSportId, standardMarketSellQueryVo.getSportId());
        RcsTournamentTemplate template = rcsTournamentTemplateMapper.selectOne(templateQueryWrapper);
        log.info("::{}::接拒单数据源切换-滚球赛事模板:{}",CommonUtil.getRequestId(standardMarketSellQueryVo.getMatchId()), JsonFormatUtils.toJson(template));
        if (!ObjectUtils.isEmpty(template) && StringUtils.isNotEmpty(standardMarketSellQueryVo.getDataSouceCode())) {
            QueryWrapper<RcsTournamentTemplateAcceptConfig> configQueryWrapper = new QueryWrapper();
            configQueryWrapper.lambda().eq(RcsTournamentTemplateAcceptConfig::getTemplateId, template.getId());
            List<RcsTournamentTemplateAcceptConfig> list = configMapper.selectList(configQueryWrapper);
            log.info("::{}::接拒单数据源切换-旧接拒单数据源:{}",CommonUtil.getRequestId(standardMarketSellQueryVo.getMatchId()), JsonFormatUtils.toJson(list));
            if (!CollectionUtils.isEmpty(list)) {
                for (RcsTournamentTemplateAcceptConfig config : list) {
                    config.setDataSource(standardMarketSellQueryVo.getDataSouceCode());
                    //log.info("::{}::接拒单数据源切换-新接拒单数据源配置:{}",CommonUtil.getRequestId(standardMarketSellQueryVo.getMatchId()), JsonFormatUtils.toJson(config));
                    configMapper.updateMatchDataSourceAndTimeConfig(config);
                }
                //清除接拒单缓存配置
                RcsTournamentTemplateAcceptConfig config = new RcsTournamentTemplateAcceptConfig();
                config.setMatchId(standardMarketSellQueryVo.getMatchId());
                rcsMatchEventTypeInfoService.deleteEventListToRedis(config);
            }
            QueryWrapper<RcsTournamentTemplateAcceptConfigSettle> configSettleQueryWrapper = new QueryWrapper();
            configSettleQueryWrapper.lambda().eq(RcsTournamentTemplateAcceptConfigSettle::getTemplateId, template.getId());
            List<RcsTournamentTemplateAcceptConfigSettle> settleList = configSettleMapper.selectList(configSettleQueryWrapper);
            if (!CollectionUtils.isEmpty(list)) {
                for (RcsTournamentTemplateAcceptConfigSettle config : settleList) {
                    config.setDataSource(standardMarketSellQueryVo.getDataSouceCode());
                    //log.info("::{}::接拒单结算数据源切换-新接拒单结算数据源配置:{}",CommonUtil.getRequestId(standardMarketSellQueryVo.getMatchId()), JsonFormatUtils.toJson(config));
                    configSettleMapper.updateMatchDataSourceAndTimeConfigSettle(config);
                }
            }

            //kir-修改23427 和 23428 bug
            if (SportIdEnum.isFootball(standardMarketSellQueryVo.getSportId())) {
                tournamentTemplateSettle(template);
            }
        }
    }

    /**
     * kir-修改23427 和 23428 bug 向业务推送数据
     *
     * @param template
     */
    @Override
    public void tournamentTemplateSettle(RcsTournamentTemplate template) {
        //kir-修改23427 和 23428 bug
        MatchTournamentTemplateVo rtnVo = getMatchTournamentTemplateVo(template);
        log.info("::{}::rtnVo参数为:{}",CommonUtil.getRequestId(template.getTypeVal()), JSONObject.toJSONString(rtnVo));
        if (rtnVo == null) return;
        String linkId = CommonUtils.getLinkId("template_settle_init");
        com.panda.sport.data.rcs.api.Request request = new com.panda.sport.data.rcs.api.Request();
        request.setData(rtnVo);
        request.setGlobalId(linkId);
        log.info("::{}::发送mq推送联赛模板结算数据:Message:{}", linkId, JSONObject.toJSON(request));
        sendMessage.sendMessage("Tournament_Template_Settle", linkId, String.valueOf(template.getTypeVal()), JSONObject.toJSON(request));
    }

    private MatchTournamentTemplateVo getMatchTournamentTemplateVo(RcsTournamentTemplate param) {
        MatchTournamentTemplateVo rtnVo = new MatchTournamentTemplateVo();
        List<TournamentTemplateAcceptConfigSettleVo> acceptConfigList = Lists.newArrayList();
        rtnVo.setSportId(param.getSportId());
        rtnVo.setMatchId(param.getTypeVal());
        rtnVo.setTemplateId(param.getId());
        rtnVo.setAcceptConfigList(acceptConfigList);
        //初始化赛事参数配置的所有玩法
        Map<String, Object> map = Maps.newHashMap();
        map.put("template_id", param.getId());
        List<RcsTournamentTemplateAcceptConfigSettle> configSettleList = configSettleMapper.selectByMap(map);
        if (CollectionUtils.isEmpty(configSettleList)) {
            log.info("::{}::未找到赛事模板接拒结算数据:{}", CommonUtil.getRequestId(param.getTypeVal()), JSONObject.toJSON(rtnVo));
            return null;
        }
        for (RcsTournamentTemplateAcceptConfigSettle configSettle : configSettleList) {
            TournamentTemplateAcceptConfigSettleVo vo = new TournamentTemplateAcceptConfigSettleVo();
            vo.setId(configSettle.getId());
            vo.setCategorySetId(configSettle.getCategorySetId());
            vo.setDataSourceCode(configSettle.getDataSource());
            vo.setNormalDelayTime(configSettle.getNormal());
            vo.setDelayTime(configSettle.getMinWait());
            vo.setMaxDelayTime(configSettle.getMaxWait());
            Map<String, Object> eventSettleMap = Maps.newHashMap();
            eventSettleMap.put("accept_config_settle_id", configSettle.getId());
            List<RcsTournamentTemplateAcceptEventSettle> eventSettleList = eventSettleMapper.selectByMap(eventSettleMap);
            if (!CollectionUtils.isEmpty(eventSettleList)) {
                List<TournamentTemplateAcceptEventSettleVo> eventSettleVo = BeanCopyUtils.copyPropertiesList(eventSettleList, TournamentTemplateAcceptEventSettleVo.class);
                vo.setAcceptEventList(eventSettleVo);
            }
            acceptConfigList.add(vo);
        }
        return rtnVo;
    }

    @Override
    public Map<Long, List<String>> getNeedRemindMatchMemos(List<Long> matchIds, Long traderId) {
        if (traderId == null) {
            return new HashMap<>();
        }
        if (CollectionUtils.isEmpty(matchIds)) {
            return new HashMap<>();
        }
        List<String> readMatchMemos = baseMapper.getReadMemoIds(matchIds, String.valueOf(traderId));
        List<RcsMatchTradeMemo> needRemindMemos = baseMapper.getMatchMemoMatchIds(matchIds, String.valueOf(traderId), readMatchMemos);
        if (CollectionUtils.isEmpty(needRemindMemos)) {
            return new HashMap<>();
        }
        Map<Long, List<String>> needRemindMatchMemos = new HashMap<>();
        needRemindMemos.forEach(e -> {
            List<String> memoIds = needRemindMatchMemos.get(e.getStandardMatchId());
            if (CollectionUtils.isEmpty(memoIds)) {
                memoIds = new ArrayList<>();
                needRemindMatchMemos.put(e.getStandardMatchId(), memoIds);
            }
            memoIds.add(e.getId());
        });
        return needRemindMatchMemos;
    }
}
