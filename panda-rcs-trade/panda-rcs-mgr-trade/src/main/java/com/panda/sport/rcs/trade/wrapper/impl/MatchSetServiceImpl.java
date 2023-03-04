package com.panda.sport.rcs.trade.wrapper.impl;

import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.mongo.MatchCatgorySetVo;
import com.panda.sport.rcs.mongo.MatchMarketLiveBean;
import com.panda.sport.rcs.trade.wrapper.MongoService;
import com.panda.sport.rcs.vo.MarketLiveOddsQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.panda.sport.rcs.trade.wrapper.MatchSetService;
import com.panda.sport.rcs.vo.MarketStatusUpdateVO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Project Name: panda-rcs-trade-group
 * @Package Name: com.panda.sport.rcs.trade.wrapper.impl
 * @Description : 盘口状态服务实现类
 * @Author : Paca
 * @Date : 2020-07-17 11:03
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Slf4j
@Service
public class MatchSetServiceImpl implements MatchSetService {
    @Autowired
    private MongoTemplate mongotemplate;
    @Autowired
    private MongoService mongoService;

    @Override
    public void updateCategorySetShow(MarketLiveOddsQueryVo vo) {
        Query matchQuery = new Query();
        matchQuery.fields().include("matchId");
        matchQuery.fields().include("setInfos");
        matchQuery.addCriteria(Criteria.where("matchId").is(vo.getMatchId()));
        MatchMarketLiveBean marketLive = mongotemplate.findOne(matchQuery, MatchMarketLiveBean.class);
        if(null!=marketLive){
            List<MatchCatgorySetVo> setInfos = marketLive.getSetInfos();
            if(!CollectionUtils.isEmpty(setInfos)){
                setInfos.stream().forEach(setVo -> {
                    if(vo.getCategorySetId().equals(setVo.getCatgorySetId())){
                        setVo.setCategorySetShow(vo.isCategorySetShow());
                    }
                });
                marketLive.setSetInfos(setInfos);
                Map matchMap = new HashMap<>();
                matchMap.put("matchId", vo.getMatchId());
                mongoService.upsert(matchMap, "match_market_live", marketLive);
            }
        }else {
            throw new RcsServiceException("未查询到次赛事");
        }

    }

    @Override
    public void updateMarketTradeType(MarketStatusUpdateVO marketStatusUpdateVO) {

    }

    @Override
    public void updateMarketStatus(MarketStatusUpdateVO marketStatusUpdateVO) {

    }
}
