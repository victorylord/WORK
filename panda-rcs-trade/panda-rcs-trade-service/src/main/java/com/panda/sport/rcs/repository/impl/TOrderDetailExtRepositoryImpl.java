package com.panda.sport.rcs.repository.impl;

import com.panda.sport.rcs.pojo.vo.OrderTakingVo;
import com.panda.sport.rcs.repository.TOrderDetailExtRepository;
import com.panda.sport.rcs.vo.TOrderDetailExtDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Repository
public class TOrderDetailExtRepositoryImpl implements TOrderDetailExtRepository {
    @Autowired private MongoTemplate mongoTemplate;

    @Override
    public List<String> queryOrderNoByBetNo(List<String> betNos) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("betNo").in(betNos);
        query.addCriteria(criteria);
        return mongoTemplate.findDistinct(query, "orderNo", TOrderDetailExtDO.class, String.class);
    }

    @Override
    public int orderTakingBatch(Integer orderStatus, List<String> ids) {
        Query query =
                new Query(
                        Criteria.where("betNo")
                                .in(ids)
                                .and("orderStatus")
                                .in(Arrays.asList(0, -1))
                                .and("handleStatus")
                                .is(0));
        Update update = new Update().set("updateTime", new Date()).set("orderStatus", orderStatus);
        return (int)
                mongoTemplate.updateMulti(query, update, "t_order_detail_ext").getModifiedCount();
    }

    @Override
    public List<String> queryOrderNo(OrderTakingVo vo) {
        Criteria criteria =
                Criteria.where("orderStatus")
                        .is(-1)
                        .and("handleStatus")
                        .is(0)
                        .and("matchId")
                        .is(vo.getMatchId());
        if (null != vo.getIds() && vo.getIds().size() != 0) {
            criteria.and("betNo").nin(vo.getIds());
        }
        return mongoTemplate.findDistinct(
                new Query(criteria), "orderNo", "t_order_detail_ext", String.class);
    }

    @Override
    public int pauseOrderTakingBatch(OrderTakingVo vo, Integer updateStatus) {
        Criteria criteria =
                Criteria.where("orderStatus")
                        .is(-1)
                        .and("handleStatus")
                        .is(0)
                        .and("matchId")
                        .is(vo.getMatchId());
        if (null != vo.getIds() && vo.getIds().size() != 0) {
            criteria.and("betNo").nin(vo.getIds());
        }
        Query query = new Query(criteria);
        Update update = new Update().set("updateTime", new Date()).set("orderStatus", updateStatus);
        return (int)
                mongoTemplate.updateMulti(query, update, "t_order_detail_ext").getModifiedCount();
    }

    @Override
    public List<TOrderDetailExtDO> searchByCriteria(Criteria criteria) {
        return mongoTemplate.find(new Query(criteria),TOrderDetailExtDO.class,"t_order_detail_ext");
    }
}
