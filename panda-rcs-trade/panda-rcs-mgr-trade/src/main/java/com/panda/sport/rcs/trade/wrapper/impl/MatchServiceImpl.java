package com.panda.sport.rcs.trade.wrapper.impl;

import com.panda.sport.rcs.mongo.MatchMarketLiveBean;
import com.panda.sport.rcs.trade.wrapper.MatchService;
import com.panda.sport.rcs.trade.wrapper.MongoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


/**
 * @author :  enzo
 * @Project Name :  trade
 * @Package Name :  com.panda.sport.rcs.trade.wrapper
 * @Description :  TODO
 * @Date: 2020-11-07 17:58
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Slf4j
@Service
public class MatchServiceImpl implements MatchService {

    @Autowired
    MongoTemplate mongotemplate;

    @Autowired
    MongoService mongoService;

    @Override
    public void updateTraderNums(Long matchId, Integer num) {
        try {
            log.info("::{}::更新指派数量赛事ID:"+matchId+"数量"+num,matchId);
            Query matchQuery = new Query();
            matchQuery.fields().include("matchId");
            matchQuery.fields().include("traderNum");
            matchQuery.addCriteria(Criteria.where("matchId").is(matchId));
            MatchMarketLiveBean match = mongotemplate.findOne(matchQuery, MatchMarketLiveBean.class);
            if(null!=match){
                Map matchMap = new HashMap<>();
                matchMap.put("matchId", matchId);
                match.setTraderNum(num);
                mongoService.upsert(matchMap, "match_market_live", match);
            }
        }catch (Exception e){
            log.error("::{}::更新操盘人数报错:{}",matchId,e.getMessage(),e);
        }

    }
}
