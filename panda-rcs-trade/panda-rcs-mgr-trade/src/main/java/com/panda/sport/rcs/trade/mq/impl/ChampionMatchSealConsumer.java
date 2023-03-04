package com.panda.sport.rcs.trade.mq.impl;

import com.alibaba.fastjson.JSONObject;
import com.panda.merge.dto.Request;
import com.panda.sport.rcs.enums.TradeLevelEnum;
import com.panda.sport.rcs.mapper.RcsLanguageInternationMapper;
import com.panda.sport.rcs.mapper.RcsStandardOutrightMatchInfoMapper;
import com.panda.sport.rcs.mapper.StandardSportMarketMapper;
import com.panda.sport.rcs.mapper.StandardSportTournamentMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.*;
import com.panda.sport.rcs.trade.mq.RcsConsumer;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.vo.dto.RcsBroadCastDTO;
import com.panda.sport.rcs.trade.wrapper.RcsTradeConfigService;
import com.panda.sport.rcs.vo.MarketStatusUpdateVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 冠军赛事到了封盘时间的盘口进行关盘处理
 *
 * @author carver
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "RCS_TRADE_CHAMPION_MATCH_SEAL",
        consumerGroup = "RCS_TRADE_CHAMPION_MATCH_SEAL",
        messageModel = MessageModel.CLUSTERING,
        consumeMode = ConsumeMode.CONCURRENTLY)
public class ChampionMatchSealConsumer extends RcsConsumer<Request<List<RcsMarketChampionExt>>> {

    @Autowired
    RcsTradeConfigService rcsTradeConfigService;
    @Autowired
    private RcsStandardOutrightMatchInfoMapper standardOutrightMatchInfoMapper;
    @Autowired
    private ProducerSendMessageUtils producerSendMessageUtils;
    @Autowired
    private StandardSportTournamentMapper standardSportTournamentMapper;
    @Autowired
    private StandardSportMarketMapper standardSportMarketMapper;
    @Autowired
    private RcsLanguageInternationMapper rcsLanguageInternationMapper;

    @Override
    protected String getTopic() {
        return "RCS_TRADE_CHAMPION_MATCH_SEAL";
    }

    @Override
    public Boolean handleMs(Request<List<RcsMarketChampionExt>> msg) {
        String linkId = msg.getLinkId();
        try {
            List<RcsMarketChampionExt> list = msg.getData();
            if (!CollectionUtils.isEmpty(list)) {
                List<Long> marketIdList = list.stream().map(RcsMarketChampionExt::getMarketId).collect(Collectors.toList());
                log.info("::{}::冠军赛事自动封盘开始:{}", linkId, JSONObject.toJSONString(marketIdList));
                for (RcsMarketChampionExt m : list) {
                    if (ObjectUtils.isEmpty(m.getDataSource())) {
                        //为空设置为自动操盘
                        m.setDataSource(NumberUtils.INTEGER_ZERO);
                    }
                    //调用融合RPC服务
                    MarketStatusUpdateVO config = new MarketStatusUpdateVO();
                    config.setMatchId(m.getStandardMatchInfoId());
                    config.setMarketId(m.getMarketId().toString());
                    config.setTradeLevel(TradeLevelEnum.MARKET.getLevel());
                    config.setTradeType(m.getDataSource());
                    config.setMarketStatus(NumberUtils.INTEGER_TWO);
                    //兜底冠军赛事封盘预警获取赛种id
                    if(null == m.getSportId()){
                        RcsStandardOutrightMatchInfo outrightMatchInfo = standardOutrightMatchInfoMapper.selectById(m.getStandardMatchInfoId());
                        if(null != outrightMatchInfo){
                            m.setSportId(outrightMatchInfo.getSportId());
                        }
                    }
                    try {
                        rcsTradeConfigService.championMatchTradeStatus(config);
                        RcsMatchMarketConfig rcsMatchMarketConfig = new RcsMatchMarketConfig();
                        if(null != m.getSportId()){
                            rcsMatchMarketConfig.setSportId(m.getSportId().intValue());
                        }
                        rcsMatchMarketConfig.setMatchType(3);
                        rcsMatchMarketConfig.setMatchId(m.getStandardMatchInfoId());
                        String closeMsg = this.getCloseMsg(m);
                        log.info("::{}::冠军赛事自动封盘预警:盘口ID={},msg={}", linkId, m.getMarketId(), closeMsg);
                        this.sendCloseMarketMessage(rcsMatchMarketConfig, closeMsg, msg.getLinkId());
                    } catch (Exception e) {
                        log.error("::{}::冠军赛事自动封盘,盘口id={},封盘时间={},异常:", linkId, m.getMarketId(), m.getNextSealTime(), e);
                    }
                }
            } else {
                log.info("::{}::冠军赛事自动封盘错误:No Data Available", linkId);
            }
        } catch (Exception e) {
            log.error("::{}::冠军赛事自动封盘异常:", linkId, e);
        }
        return true;
    }

    /**
     * @return void
     * @Description //发送封盘消息
     * @Param []
     * @Author sean
     * @Date 2021/1/16
     **/
    public void sendCloseMarketMessage(RcsMatchMarketConfig config, String msg, String linkId) {
        RcsBroadCastDTO cast = new RcsBroadCastDTO();
        cast.setSportId(config.getSportId().longValue());
        cast.setMatchType(config.getMatchType());
        RcsBroadCast broad = new RcsBroadCast();
        broad.setMsgType(3);
        broad.setContent(msg);
        broad.setExtendsField(config.getMatchId().toString());
        broad.setExtendsField1(NumberUtils.INTEGER_ZERO.toString());
        broad.setStatus(NumberUtils.INTEGER_ONE);
        broad.setMsgId(linkId);
        cast.setRcsBroadCast(broad);
        producerSendMessageUtils.sendMessage("risk_msg_alarm", null, linkId, cast);
    }

    /**
     * 构建消息体
     *
     * @param m
     * @return
     */
    public String getCloseMsg(RcsMarketChampionExt m) {
        StringBuilder sb = new StringBuilder();
        sb.append("冠军：");
        HashSet<Long> nameCodes = new HashSet<>();
        RcsStandardOutrightMatchInfo outrightMatchInfo = standardOutrightMatchInfoMapper.selectById(m.getStandardMatchInfoId());
        StandardSportTournament tournament = standardSportTournamentMapper.selectById(outrightMatchInfo.getStandardTournamentId());
        if (!ObjectUtils.isEmpty(tournament)) {
            nameCodes.add(tournament.getNameCode());
        }
        StandardSportMarket market = standardSportMarketMapper.selectById(m.getMarketId());
        if (!ObjectUtils.isEmpty(market)) {
            nameCodes.add(market.getNameCode());
        }
        List<LanguageInternation> list = rcsLanguageInternationMapper.getLanguageNameCodes(nameCodes);
        String tournamentName = "";
        String marketName = "";
        if (!CollectionUtils.isEmpty(list)) {
            Map<Long, List<LanguageInternation>> map = list.stream().collect(Collectors.groupingBy(LanguageInternation::getNameCode));
            List<LanguageInternation> tournamentList = map.get(tournament.getNameCode());
            if (!CollectionUtils.isEmpty(tournamentList)) {
                for (LanguageInternation e : tournamentList) {
                    if ("zs".equals(e.getLanguageType())) {
                        tournamentName = e.getText();
                    }
                }
            }
            List<LanguageInternation> marketList = map.get(market.getNameCode());
            if (!CollectionUtils.isEmpty(marketList)) {
                for (LanguageInternation e : marketList) {
                    if ("zs".equals(e.getLanguageType())) {
                        marketName = e.getText();
                    }
                }
            }
        }
        sb.append(tournamentName).append("(").append(outrightMatchInfo.getStandardOutrightManagerId()).append(")-");
        sb.append(marketName).append(":盘口按设置自动关盘，请及时检查开启。");
        return sb.toString();
    }
}

