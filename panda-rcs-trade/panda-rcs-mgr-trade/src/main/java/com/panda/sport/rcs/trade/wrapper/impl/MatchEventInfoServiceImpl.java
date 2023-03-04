package com.panda.sport.rcs.trade.wrapper.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.merge.dto.Request;
import com.panda.sport.rcs.mapper.MatchEventInfoMapper;
import com.panda.sport.rcs.mapper.StandardMatchInfoMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.MatchEventInfo;
import com.panda.sport.rcs.pojo.MatchStatisticsInfo;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.pojo.dto.CheckMatchEndDTO;
import com.panda.sport.rcs.trade.service.TradeVerificationService;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.MatchEventInfoService;
import com.panda.sport.rcs.trade.wrapper.MatchStatisticsInfoService;
import com.panda.sport.rcs.vo.CustomizedEventBeanVo;
import com.panda.sport.rcs.vo.MatchMarketLiveOddsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName MatchEventInfoServiceImpl
 * @Description: TODO
 * @Author Vector
 * @Date 2019/10/10
 **/
@Service
public class MatchEventInfoServiceImpl extends ServiceImpl<MatchEventInfoMapper, MatchEventInfo> implements MatchEventInfoService {

    @Autowired
    private MatchEventInfoMapper matchEventInfoMapper;

    @Autowired
    private StandardMatchInfoMapper standardMatchInfoMapper;

    @Autowired
    MatchStatisticsInfoService matchStatisticsInfoService;

    @Autowired
    private ProducerSendMessageUtils sendMessage;

    @Autowired
    private TradeVerificationService tradeVerificationService;

    @Override
    public Integer getSencondStart(Long matchId) {
        Integer sencondStart = 0;
        QueryWrapper<MatchEventInfo> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(MatchEventInfo::getStandardMatchId, matchId);
        wrapper.lambda().orderByDesc(MatchEventInfo::getCreateTime);
        wrapper.last("limit 1");
        MatchEventInfo matchEventInfo = matchEventInfoMapper.selectOne(wrapper);
        if (matchEventInfo != null) {
            if (null != matchEventInfo.getSecondsFromStart()) {
                sencondStart = matchEventInfo.getSecondsFromStart().intValue();
            } else {
                sencondStart = 0;
            }
        } else {
            sencondStart = 0;
        }
        return sencondStart;
    }

    @Override
    public void selectRecentMatchEventInfo(MatchMarketLiveOddsVo matchMarketLiveBean) {
        MatchStatisticsInfo matchInfo = matchStatisticsInfoService.getMatchInfoByMatchId(matchMarketLiveBean.getMatchId());
        if (matchInfo != null) {
            matchMarketLiveBean.setScore(matchInfo.getScore());
            matchMarketLiveBean.setSecondsMatchStart(matchInfo.getSecondsMatchStart());
            matchMarketLiveBean.setPeriod(matchInfo.getPeriod());
        }
        if (null == matchMarketLiveBean.getPeriod() || null == matchMarketLiveBean.getSecondsMatchStart() || 0 == matchMarketLiveBean.getSecondsMatchStart()) {
            QueryWrapper<MatchEventInfo> wrapper = new QueryWrapper<>();
            wrapper.lambda().eq(MatchEventInfo::getStandardMatchId, matchMarketLiveBean.getMatchId());
            wrapper.lambda().orderByDesc(MatchEventInfo::getCreateTime);
            wrapper.last("limit 1");
            MatchEventInfo matchEventInfo = matchEventInfoMapper.selectOne(wrapper);
            if (matchEventInfo != null) {
                matchMarketLiveBean.setSecondsMatchStart(matchEventInfo.getSecondsFromStart().intValue());
                matchMarketLiveBean.setPeriod(matchEventInfo.getMatchPeriodId().intValue());
            }
        }
    }

    @Override
    public List<CustomizedEventBeanVo> selectMatchEventInfoByMatchId(Long matchId, String dataSource, Long eventTime, List<Integer> eventTypes, Integer sort, Integer limit,List unFilterEvents) {
        StandardMatchInfo standardMatchInfo = standardMatchInfoMapper.selectById(matchId);
        List<CustomizedEventBeanVo> customizedEventBeanVos;
        if (standardMatchInfo.getSportId().intValue() == 1) {
            customizedEventBeanVos = matchEventInfoMapper.selectMatchEventInfoByMatchIdByFootball(matchId, dataSource, eventTime,eventTypes,sort,limit,unFilterEvents);
        } else {
            customizedEventBeanVos = matchEventInfoMapper.selectMatchEventInfoByMatchId(matchId, dataSource, eventTime,eventTypes,sort,limit);
        }
        return customizedEventBeanVos;
    }

    @Override
    public void checkErrorMatchEnd(CheckMatchEndDTO dto) {
        StandardMatchInfo standardMatchInfo = standardMatchInfoMapper.selectById(dto.getMatchId());
        if(1==dto.getIsEnd()&&1==standardMatchInfo.getSportId()){
            Request<CheckMatchEndDTO> sendRequest = new Request<>();
            sendRequest.setLinkId(CommonUtil.getRequestId());
            sendRequest.setData(dto);
            sendMessage.sendMessage("FROM_RCS_MATCH_IS_END", null, CommonUtil.getRequestId(), sendRequest);
        }else{
            if(1==standardMatchInfo.getSportId()){
                Request<HashMap> sendRequest = new Request<>();
                sendRequest.setLinkId(CommonUtil.getRequestId());
                HashMap<String, Object> map = new HashMap<>();
                map.put("sportId",standardMatchInfo.getSportId());
                map.put("standardMatchId",dto.getMatchId());
                map.put("isErrorEndEvent",0);
                sendRequest.setData(map);
                sendMessage.sendMessage("MATCH_EVENT_INFO_ERROR_END_BY_RCS", null, CommonUtil.getRequestId(), sendRequest);
            }
        }
    }
}
