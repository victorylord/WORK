package com.panda.sport.rcs.trade.mq;

import com.panda.sport.rcs.mq.bean.RocketProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("rocketmq.trade")
public class RocketRpcProperties extends RocketProperties{

}
