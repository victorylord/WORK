package com.panda.sport.rcs.trade.mq.impl;

import com.alibaba.fastjson.JSONObject;
import com.panda.sport.rcs.pojo.TUserLevel;
import com.panda.sport.rcs.trade.mq.RcsConsumer;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.TUserLevelService;
import com.panda.sport.rcs.vo.UserProfileTags;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * @program: xindaima
 * @description:
 *  用户等级同步
 * @author: kimi
 * @create: 2021-02-09 12:05
 **/
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "USER_PROFILE_TAGS_TOPIC",
        consumerGroup = "RCS_TRADE_USER_PROFILE_TAGS_TOPIC",
        messageModel = MessageModel.CLUSTERING,
        consumeMode = ConsumeMode.CONCURRENTLY)
public class RcsUserLevelConsumer extends RcsConsumer<Map<String, Object>> {
    @Autowired
    private TUserLevelService tUserLevelService;

    @Override
    protected String getTopic() {
        return "USER_PROFILE_TAGS_TOPIC";
    }

    @Override
    public Boolean handleMs(Map<String, Object> data) {
        try {
            log.info("::{}::STANDARD_SEND_WINDOW_CONTROLLER:{}",CommonUtil.getRequestId(),JSONObject.toJSONString(data));
            if (!CollectionUtils.isEmpty(data)) {
                int type = Integer.parseInt(data.get("type").toString());
                Object entity1 = data.get("entity");
                UserProfileTags userProfileTags = JSONObject.parseObject(entity1.toString(), UserProfileTags.class);
                if (type == 3) {
                    TUserLevel tUserLevel=new TUserLevel();
                    tUserLevel.setLevelId(userProfileTags.getId().intValue());
                    tUserLevelService.removeById(tUserLevel);
                } else {
                    TUserLevel tUserLevel=new TUserLevel();
                    tUserLevel.setLevelId(userProfileTags.getId().intValue());
                    tUserLevel.setLevelName(userProfileTags.getTagName());
                    tUserLevel.setStatus(1);
                    tUserLevel.setBgColor(userProfileTags.getTagColor());
                    tUserLevel.setColor(userProfileTags.getTagColor());
                    tUserLevelService.saveOrUpdate(tUserLevel);
                }
            }
        }catch (Exception e){
            log.error("::{}::STANDARD_SEND_WINDOW_CONTROLLER:{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
        return true;
    }
}
