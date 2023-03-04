package com.panda.sport.rcs.trade.controller;

import com.alibaba.fastjson.JSONObject;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.enums.SportIdEnum;
import com.panda.sport.rcs.mapper.RcsTradeConfigMapper;
import com.panda.sport.rcs.mapper.StandardMatchInfoMapper;
import com.panda.sport.rcs.mapper.statistics.MatchStatisticsInfoDetailMapper;
import com.panda.sport.rcs.mongo.MatchMarketLiveBean;
import com.panda.sport.rcs.pojo.MatchStatisticsInfoDetail;
import com.panda.sport.rcs.pojo.RcsStandardSportMarketSell;
import com.panda.sport.rcs.trade.enums.TheirTimeEnum;
import com.panda.sport.rcs.trade.service.MongoDbService;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.vo.StandardMatchInfoBaseVo;
import com.panda.sport.rcs.trade.vo.dto.ThirdMatchDTO;
import com.panda.sport.rcs.trade.wrapper.RcsStandardSportMarketSellService;
import com.panda.sport.rcs.vo.HttpResponse;
import com.panda.sport.rcs.vo.MatchInfoLanguageVo;
import com.panda.sport.rcs.vo.StandardMatchInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author :  myname
 * @Project Name :  xindaima
 * @Package Name :  com.panda.sport.rcs.trade.controller
 * @Description : 查询赛事基础数据
 * @Date: 2020-10-27 11:10
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@RestController
@Slf4j
@RequestMapping("/StandardMatchInfoBase")
public class StandardMatchInfoBaseController {
    @Autowired
    private StandardMatchInfoMapper standardMatchInfoMapper;
    @Autowired
    private RedisClient redisClient;
    @Autowired
    private MongoDbService mongoDbService;
    @Autowired
    private RcsTradeConfigMapper rcsTradeConfigMapper;
    @Autowired
    private MatchStatisticsInfoDetailMapper detailMapper;
    @Autowired
    private RcsStandardSportMarketSellService rcsStandardSportMarketSellService;

    @RequestMapping(value = "/get", method = RequestMethod.GET)
    private HttpResponse<StandardMatchInfoBaseVo> get(Long matchId, Integer liveOddBusiness) {
        try {
            Map<String, Object> columnMap = new HashMap<>();
            columnMap.put("standard_match_id", matchId);
            StandardMatchInfoVo standardMatchInfo = standardMatchInfoMapper.selectStandardMatchInfoBaseByMatchId(matchId);
            StandardMatchInfoBaseVo standardMatchInfoBaseVo = new StandardMatchInfoBaseVo();
            MatchMarketLiveBean matchInfo = mongoDbService.getMatchInfo(matchId, liveOddBusiness);
            if (matchInfo != null) {
                standardMatchInfo.setTradeType(matchInfo.getTradeType());
                standardMatchInfo.setSecondsMatchStart(matchInfo.getSecondsMatchStart());
                standardMatchInfo.setSetNum(matchInfo.getSetNum());
                standardMatchInfo.setRoundType(matchInfo.getRoundType());
                standardMatchInfoBaseVo.setMatchSnapshot(matchInfo.getMatchSnapshot());
                if (SportIdEnum.isSnooker(matchInfo.getSportId()))
                    standardMatchInfoBaseVo.setScore(matchInfo.getScore());
            }
            standardMatchInfoBaseVo.setStandardMatchInfo(standardMatchInfo);
            Long beginTime = standardMatchInfo.getBeginTime();

            //比分查询match_statistics_info_detail
//            standardMatchInfoBaseVo.setScore(detailMapper.queryMatchScore(matchId));
            Integer liveodds = standardMatchInfo.getLiveOddBusiness();
            if (liveodds != null && liveodds == 1 && beginTime < System.currentTimeMillis()) {
                standardMatchInfo.setMatchStatus(1);
            }
            Integer integer = rcsTradeConfigMapper.selectStatusByMatchId(String.valueOf(matchId));
            if (integer != null) {
                standardMatchInfo.setOperateMatchStatus(integer);
            } else {
                standardMatchInfo.setOperateMatchStatus(0);
            }
            RcsStandardSportMarketSell rcsStandardSportMarketSell = rcsStandardSportMarketSellService.selectStandardMarketSellVo(matchId);
            standardMatchInfoBaseVo.setBusinessEvent(rcsStandardSportMarketSell.getBusinessEvent());
            List<MatchStatisticsInfoDetail> matchStatisticsInfoDetailList = detailMapper.selectScoreTotal(matchId.intValue());
            if (!CollectionUtils.isEmpty(matchStatisticsInfoDetailList)) {
                for (MatchStatisticsInfoDetail matchStatisticsInfoDetail1 : matchStatisticsInfoDetailList) {
                    if (matchStatisticsInfoDetail1.getCode().equals(TheirTimeEnum.FULL_COURT.getCode())) {
                        standardMatchInfoBaseVo.setScore(matchStatisticsInfoDetail1.getT1() + ":" + matchStatisticsInfoDetail1.getT2());
                    }
                    Map<Integer, String> totalScore = standardMatchInfoBaseVo.getTotalScore();
                    if (totalScore == null) {
                        totalScore = new HashMap<>();
                        standardMatchInfoBaseVo.setTotalScore(totalScore);
                    }
                    totalScore.put(TheirTimeEnum.getTheirTimeEnumByCode(matchStatisticsInfoDetail1.getCode(), matchStatisticsInfoDetail1.getFirstNum()).getValue(), matchStatisticsInfoDetail1.getT1() + ":" + matchStatisticsInfoDetail1.getT2());
                }
            }
            //添加赛事对阵数据
            List<MatchInfoLanguageVo> matchInfoLanguageVos = standardMatchInfoMapper.selectMatchInfoLanguage(matchId);
            HashMap<String, Map<String, String>> hashMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(matchInfoLanguageVos)) {
                for (MatchInfoLanguageVo matchInfoLanguageVo : matchInfoLanguageVos) {
                    Map<String, String> stringStringMap = hashMap.get(matchInfoLanguageVo.getMatchPosition());
                    if (stringStringMap == null) {
                        stringStringMap = new HashMap<>();
                        hashMap.put(matchInfoLanguageVo.getMatchPosition(), stringStringMap);
                    }
                    stringStringMap.put(matchInfoLanguageVo.getLanguageType(), matchInfoLanguageVo.getText());
                }
            }
            standardMatchInfoBaseVo.setMatchInfoLanguage(hashMap);
            standardMatchInfo.setBusinessEvent(rcsStandardSportMarketSell.getBusinessEvent());
            String aoId = CommonUtil.getId(standardMatchInfo.getThirdMatchListStr(), "AO");
            standardMatchInfo.setAoId(aoId);

            return HttpResponse.success(standardMatchInfoBaseVo);
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(-1, "服务器出问题");
        }
    }


}
