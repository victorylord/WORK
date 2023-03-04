package com.panda.sport.rcs.trade.mq.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.panda.sport.rcs.pojo.TTagMarket;
import com.panda.sport.rcs.pojo.TUserTag;
import com.panda.sport.rcs.trade.mq.RcsConsumer;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.TTagMarketService;
import com.panda.sport.rcs.trade.wrapper.TUserTagService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * 财务特征类标签同步
 * @author: Kir
 * @create: 2021-04-09 12:05
 **/
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "USER_FINANCE_TAGS_TOPIC",
        consumerGroup = "RCS_TRADE_USER_FINANCE_TAGS_TOPIC",
        messageModel = MessageModel.CLUSTERING,
        consumeMode = ConsumeMode.CONCURRENTLY)
public class RcsUserTagConsumer extends RcsConsumer<Map<String, Object>> {
    @Autowired
    private TUserTagService tUserTagService;

    @Autowired
    private TTagMarketService tagMarketService;

    @Override
    protected String getTopic() {
        return "USER_FINANCE_TAGS_TOPIC";
    }

    @Override
    public Boolean handleMs(Map<String, Object> data) {
        try {
            log.info("::{}::USER_FINANCE_TAGS_TOPIC:{}",CommonUtil.getRequestId(),JSONObject.toJSONString(data));
            if (!CollectionUtils.isEmpty(data)) {
                int type = Integer.parseInt(data.get("type").toString());
                Object entity1 = data.get("entity").toString();
                JSONObject jsonObject = JSONObject.parseObject(entity1.toString());
                Integer id = Integer.parseInt(String.valueOf(jsonObject.get("id")));
                if (type == 3) {
                    TUserTag userTag = new TUserTag();
                    userTag.setTagId(id);
                    tUserTagService.removeById(userTag);

                    //删除关联表数据
                    HashMap<String, Object> rmv = new HashMap<>();
                    rmv.put("tag_id", id);
                    tagMarketService.removeByMap(rmv);
                } else {
                    TUserTag userTag = new TUserTag();
                    userTag.setTagId(id);
                    userTag.setTagName(String.valueOf(jsonObject.get("tagName")));
                    userTag.setTagType(4);
                    tUserTagService.saveOrUpdate(userTag);

                    //修改关联表数据
                    QueryWrapper<TTagMarket> wrapper = new QueryWrapper<>();
                    wrapper.eq("tag_id", id);
                    TTagMarket one = tagMarketService.getOne(wrapper);
                    if(ObjectUtils.isEmpty(one)){
                        //新增
                        one = new TTagMarket();
                        one.setTagId(id);
                        one.setLevelId(NumberUtils.INTEGER_ONE);
                        one.setCreateTime(System.currentTimeMillis());
                        one.setUpdateTime(System.currentTimeMillis());
                        tagMarketService.save(one);
                    }else{
                        //修改
                        one.setTagId(id);
                        one.setLevelId(NumberUtils.INTEGER_ONE);
                        one.setCreateTime(System.currentTimeMillis());
                        one.setUpdateTime(System.currentTimeMillis());
                        tagMarketService.updateById(one);
                    }
                }
            }
        }catch (Exception e){
            log.error("::{}::USER_FINANCE_TAGS_TOPIC:{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
        return true;
    }
}
