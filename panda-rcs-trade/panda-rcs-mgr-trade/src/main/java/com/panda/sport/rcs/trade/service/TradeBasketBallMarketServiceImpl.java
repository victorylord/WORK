package com.panda.sport.rcs.trade.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;

import com.panda.sport.rcs.enums.BalanceTypeEnum;

import com.panda.sport.rcs.enums.SportIdEnum;
import com.panda.sport.rcs.enums.TradeLevelEnum;
import com.panda.sport.rcs.enums.YesNoEnum;
import com.panda.sport.rcs.mapper.RcsMatchMarketConfigMapper;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfigSub;
import com.panda.sport.rcs.pojo.RcsMatchMarketMarginConfig;
import com.panda.sport.rcs.pojo.StandardSportMarketOdds;
import com.panda.sport.rcs.pojo.constants.TradeConstant;
import com.panda.sport.rcs.pojo.dto.odds.MatchOddsConfig;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargain;
import com.panda.sport.rcs.trade.enums.LinkedTypeEnum;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.util.JavaBeanUtils;
import com.panda.sport.rcs.trade.wrapper.IRcsMatchMarketConfigService;
import com.panda.sport.rcs.trade.wrapper.MatchStatisticsInfoService;
import com.panda.sport.rcs.trade.wrapper.RcsTournamentOperateMarketService;
import com.panda.sport.rcs.trade.wrapper.RcsTradeConfigService;
import com.panda.sport.rcs.trade.wrapper.impl.MarketStatusServiceImpl;
import com.panda.sport.rcs.trade.wrapper.tourTemplate.IRcsTournamentTemplatePlayMargainService;
import com.panda.sport.rcs.utils.MarketUtils;
import com.panda.sport.rcs.vo.MarketStatusUpdateVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.HEAD;
import java.util.List;

/**
 * @author :  Sean
 * @Project Name :  panda-rcs-trade
 * @Package Name :  com.panda.sport.rcs.trade.service
 * @Description :  操盘服务类
 * @Date: 2020-08-13 14:01
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Service
@Slf4j
public class TradeBasketBallMarketServiceImpl {
    @Autowired
    private RcsTradeConfigService rcsTradeConfigService;
    @Autowired
    private TradeMarketSetServiceImpl tradeMarketSetService;
    @Autowired
    private MarketStatusServiceImpl marketStatusService;
    @Autowired
    private TradeStatusService tradeStatusService;
    @Autowired
    RcsTournamentOperateMarketService rcsTournamentOperateMarketService;
    @Autowired
    MatchStatisticsInfoService matchStatisticsInfoService;
    @Autowired
    private IRcsMatchMarketConfigService rcsMatchMarketConfigService;
    @Autowired
    private TradeCommonService tradeCommonService;
    @Autowired
    private BalanceService balanceService;
    @Autowired
    private TradeVerificationService tradeVerificationService;
    @Autowired
    private RcsMatchMarketConfigMapper rcsMatchMarketConfigMapper;
    @Autowired
    private IRcsTournamentTemplatePlayMargainService rcsTournamentTemplatePlayMargainService;
    @Autowired
    private TradeOddsCommonService tradeOddsCommonService;

    /**
     * @Description   //篮球修改赔率
     * @Param [config]
     * @Author  sean
     * @Date   2021/2/4
     * @return void
     **/
    public String updateEUMarketOddsOrWater(RcsMatchMarketConfig config) {

        Integer dataSource = rcsTradeConfigService.getDataSource(config.getMatchId(), config.getPlayId());
        config.setDataSource(dataSource.longValue());
        config.setRelevanceType(NumberUtils.INTEGER_ZERO);
        RcsTournamentTemplatePlayMargain rcsTournamentTemplatePlayMargin = rcsTournamentTemplatePlayMargainService.getRcsTournamentTemplateConfig(config);
        log.info("::{}::,模板={}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()),JSONObject.toJSONString(rcsTournamentTemplatePlayMargin));
        List<RcsMatchMarketConfig> list = Lists.newArrayList();
        if (SportIdEnum.isFootball(config.getSportId())){
            RcsMatchMarketMarginConfig marginConfig =  rcsMatchMarketConfigService.getMarketWaterDiff(config);
            config.setAwayAutoChangeRate(marginConfig.getAwayAutoChangeRate());
            RcsMatchMarketConfig conf = rcsMatchMarketConfigService.queryMatchMarketConfigNew(config);
            log.info("::{}::,水差配置={}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()),JSONObject.toJSONString(conf));
            config.setMargin(tradeVerificationService.getConfigMargin(conf));
            config.setMaxOdds(conf.getMaxOdds());
            config.setMinOdds(conf.getMinOdds());
        }else {
            Boolean flag = tradeVerificationService.tennisAndPingPongNewPlayNoRadioLimit(config.getSportId().longValue(),(config.getPlayId()));
            if(flag && !MarketUtils.isAuto(dataSource)){
                List<RcsMatchMarketConfigSub> configSubList = tradeOddsCommonService.getMatchMarketSubConfigs(config,rcsTournamentTemplatePlayMargin);
                list = JSONArray.parseArray(JSONArray.toJSONString(configSubList),RcsMatchMarketConfig.class);
            }else{
                list =  tradeMarketSetService.getRcsMatchMarketConfigs(config,rcsTournamentTemplatePlayMargin);
            }
            if (CollectionUtils.isNotEmpty(list)){
                config.setAwayAutoChangeRate(list.get(0).getAwayAutoChangeRate());
                config.setMargin(list.get(0).getMargin());
                config.setMaxOdds(list.get(0).getMaxOdds());
                config.setMinOdds(list.get(0).getMinOdds());
            }
            config.setMargin(tradeVerificationService.getConfigMargin(config));
        }
        // 获取赔率
        List<StandardSportMarketOdds> oddsList = tradeCommonService.getStandardSportMarketOdds(config);
        // 赔率变化
        config.setOddsChange(rcsTournamentTemplatePlayMargin.getOddsAdjustRange().multiply(config.getOddsChange()));
        String msg = null;
        if (MarketUtils.isAuto(dataSource)) {
            // 计算水差
            msg = tradeCommonService.caluBasketBallEuWater(oddsList,config,rcsTournamentTemplatePlayMargin);
            if (StringUtils.isNotBlank(msg)){
                // 水差超过限制封盘
                MarketStatusUpdateVO vo = new MarketStatusUpdateVO()
                        .setTradeLevel(TradeLevelEnum.PLAY.getLevel())
                        .setMatchId(config.getMatchId())
                        .setCategoryId(config.getPlayId())
                        .setSubPlayId(Long.parseLong(config.getSubPlayId()))
                        .setIsPushOdds(YesNoEnum.N.getValue())
                        .setLinkedType(LinkedTypeEnum.TRADE_OVER_LIMIT.getCode())
                        .setRemark(LinkedTypeEnum.TRADE_OVER_LIMIT.getRemark())
                        .setMarketStatus(NumberUtils.INTEGER_ONE);
                tradeStatusService.updateTradeStatus(vo);
            }
            config.setOddsType(tradeVerificationService.getBasketBallUnderOddsType(config.getOddsType()));
            tradeMarketSetService.sendBasketBallWaterToDataCenter(config,null);
            tradeOddsCommonService.updateRedisWater(config);
        }else {
            // 计算赔率
            tradeCommonService.caluBasketBallEuOddsByMargin(oddsList,config);
            // 比较大小
            msg = tradeCommonService.getCheckOddsLimitAndUpdateStatus(config,null);

            if (StringUtils.isNotBlank(msg)) return msg;
            // 封装赛事数据
            MatchOddsConfig matchConfig = tradeMarketSetService.buildMatchOddsConfig(config, null,NumberUtils.INTEGER_ZERO,null,null,null);
            // 发送消息
            tradeMarketSetService.sendMatchOddsMessage(matchConfig);
        }
        balanceService.clearAllBalance(BalanceTypeEnum.JUMP_ODDS.getType(), config.getSportId().longValue(), config.getMatchId(), config.getPlayId(), config.getDateExpect(),config.getSubPlayId());
        return msg;
    }
}
