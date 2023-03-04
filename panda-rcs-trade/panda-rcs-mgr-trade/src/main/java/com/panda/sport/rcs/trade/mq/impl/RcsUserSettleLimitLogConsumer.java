package com.panda.sport.rcs.trade.mq.impl;

import com.alibaba.fastjson.JSONObject;
import com.panda.sport.rcs.mapper.RcsOperationLogMapper;
import com.panda.sport.rcs.pojo.RcsOperationLog;
import com.panda.sport.rcs.pojo.vo.LogData;
import com.panda.sport.rcs.trade.enums.UserLogTypeEnum;
import com.panda.sport.rcs.trade.mq.RcsConsumer;
import com.panda.sport.rcs.trade.util.CommonUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 派彩限额-特殊用户设置 记录用户管控日志
 *
 * @author: lz
 * @create: 2022-06-08 12:05
 **/
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "STANDARD_SEND_WINDOW_CONTROLLER",
        consumerGroup = "STANDARD_SEND_WINDOW_CONTROLLER",
        messageModel = MessageModel.CLUSTERING,
        consumeMode = ConsumeMode.CONCURRENTLY)
public class RcsUserSettleLimitLogConsumer extends RcsConsumer<RcsUserSettleLimitLogConsumer.UserSettleLimitLogConsumerVo> {
    @Data
    public static class UserSettleLimitLogConsumerVo {
        private String uid;
        private String trader;
        List<SportPayoutInit> sportPayoutInit = new ArrayList<>();

        @Data
        public static class SportPayoutInit {
            private String name;
            private String value;
            private String oldValue;
        }
    }


    @Autowired
    RcsOperationLogMapper rcsOperationLogMapper;

    @Override
    protected String getTopic() {
        return "STANDARD_SEND_WINDOW_CONTROLLER";
    }

    @Override
    public Boolean handleMs(UserSettleLimitLogConsumerVo userSettleLimitLogConsumerVo) {
        try {
            log.info("STANDARD_SEND_WINDOW_CONTROLLER 派彩限额-特殊用户设置 变更日志记录收到消息：{}", JSONObject.toJSONString(userSettleLimitLogConsumerVo));
            if (CollectionUtils.isEmpty(userSettleLimitLogConsumerVo.getSportPayoutInit())) {
                throw new Exception("变更数据为空");
            }
            RcsOperationLog rcsOperationLog = new RcsOperationLog();
            List<LogData> logDataList = new ArrayList<>();
            userSettleLimitLogConsumerVo.getSportPayoutInit().forEach(e->{
                LogData logData = new LogData();
                logData.setType(UserLogTypeEnum.SETTLE_LIMIT.getValue());
                logData.setName(e.getName());
                logData.setData(e.getValue());
                logData.setOldData(e.getOldValue());
                logDataList.add(logData);
            });

            LogData logDataUser = new LogData();
            logDataUser.setType(UserLogTypeEnum.TRADER.getValue());
            logDataUser.setName("操作人");
            logDataUser.setData(userSettleLimitLogConsumerVo.getTrader());
            logDataList.add(logDataUser);

            rcsOperationLog.setHandleCode("user_config_history");
            rcsOperationLog.setHanlerId(userSettleLimitLogConsumerVo.getUid());
            rcsOperationLog.setUpdateContent(JSONObject.toJSONString(logDataList));
            rcsOperationLog.setShowContent("25");
            rcsOperationLogMapper.saveBatchRcsOperationLog(Arrays.asList(rcsOperationLog));
        } catch (Exception e) {
            log.error("::{}::STANDARD_SEND_WINDOW_CONTROLLER 派彩限额-特殊用户设置 变更日志记录执行失败：{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
        return true;
    }
}
