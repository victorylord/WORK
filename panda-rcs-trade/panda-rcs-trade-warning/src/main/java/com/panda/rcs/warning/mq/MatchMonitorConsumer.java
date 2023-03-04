package com.panda.rcs.warning.mq;

import com.alibaba.fastjson.JSON;
import com.panda.rcs.warning.mapper.RcsMatchMonitorMqLicenseMapper;
import com.panda.rcs.warning.utils.ConstantUtil;
import com.panda.rcs.warning.vo.MatchMonitorMqAndPayBean;
import com.panda.rcs.warning.vo.RcsMatchMonitorMqLicense;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.utils.i18n.CommonUtils;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.skywalking.apm.toolkit.trace.TraceCrossThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 玩法forecast
 *
 * @author enzo
 */
@Component
@Slf4j
@TraceCrossThread
@RocketMQMessageListener(
        topic = "MATCH_OPERATE_EX",
        consumerGroup = "rcs_task_MATCH_OPERATE_EX",
        messageModel = MessageModel.CLUSTERING,
        consumeMode = ConsumeMode.CONCURRENTLY)
public class MatchMonitorConsumer implements RocketMQListener<Object> {
    @Autowired
    private RcsMatchMonitorMqLicenseMapper rcsMatchMonitorMqLicenseMapper;

    @Override
    public void onMessage(Object msg) {
        String str = JsonFormatUtils.toJson(msg);
        if (StringUtil.isEmpty(str)) {
            return;
        }
        log.info("MatchMonitorConsumer:{}", JsonFormatUtils.toJson(msg));
        List<MatchMonitorMqAndPayBean> matchMonitorMqAndPayBeans = JSON.parseArray(JSON.parseObject(str).getString("data"), MatchMonitorMqAndPayBean.class);
        if (matchMonitorMqAndPayBeans.isEmpty()) {
            return;
        }
        try {
            for (MatchMonitorMqAndPayBean monitor : matchMonitorMqAndPayBeans) {
                Long matchId = monitor.getMatchId();
                Integer playId = monitor.getCategoryId();
                if (ConstantUtil.NORMAL_PLAY_ID.contains(playId)) {
                    Long dataSourceTime = monitor.getDataSourceTime();
                    log.info("赛事id:{}，玩法ID:{},最新下发记录:{}", matchId, playId, dataSourceTime);
                    int marketType = monitor.getMarketType();
                    RcsMatchMonitorMqLicense monitorMqBean = new RcsMatchMonitorMqLicense();
                    monitorMqBean.setMatchId(matchId);
                    monitorMqBean.setPlayId(playId);
                    monitorMqBean.setEventTime(dataSourceTime);
                    monitorMqBean.setUpdateTime(dataSourceTime);
                    monitorMqBean.setMatchType(marketType);
                    rcsMatchMonitorMqLicenseMapper.insertOrUpdate(monitorMqBean);
                }
            }
        } catch (Exception e) {
            log.error("::{}::赛事玩法同步错误{}", CommonUtils.getLinkId(),e.getMessage(),e);
        }

    }

}
