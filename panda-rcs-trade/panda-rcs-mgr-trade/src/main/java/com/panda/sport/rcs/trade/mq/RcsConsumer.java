package com.panda.sport.rcs.trade.mq;

import com.alibaba.fastjson.JSONObject;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQPushConsumerLifecycleListener;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 消费基类
 * @Author : Paca
 * @Date : 2021-11-17 14:08
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Slf4j
public abstract class RcsConsumer<T> implements RocketMQListener<String>, RocketMQPushConsumerLifecycleListener {

    @Override
    public void prepareStart(DefaultMQPushConsumer consumer) {
        consumer.setConsumeThreadMin(32);
        consumer.setConsumeThreadMax(64);
    }

    @Override
    public void onMessage(String message) {
        long startTime = System.currentTimeMillis();
        CommonUtils.mdcPut();
        try {
            String linkId= CommonUtil.getLinkId(message);
            log.info("::{}::收到消息：message={}", this.getTopic()+linkId, message);
            T obj = this.convert(message);
            this.handleMs(obj);
        } catch (RcsServiceException e) {
            log.error(String.format("::{}::消息消费失败：class=%s,topic=%s,message=%s",this.getTopic(), this.getClass(), this.getTopic(), message), e);
            throw e;
        } catch (com.alibaba.fastjson.JSONException e) {
            log.error(String.format("::{}::消息解析失败：class=%s,topic=%s,message=%s",this.getTopic(), this.getClass(), this.getTopic(), message), e);
        } catch (Exception e) {
            log.error(String.format("::{}::消息消费异常：class=%s,topic=%s,message=%s",this.getTopic(), this.getClass(), this.getTopic(), message), e);
        } finally {
            long execTime = System.currentTimeMillis() - startTime;
            if (execTime > 1000L) {
                log.error("::{}::消费完成：消费时长={},class={},topic={}",this.getTopic(), execTime, this.getClass(), this.getTopic());
            } else {
                log.info("::{}::消费完成：消费时长={},class={},topic={}",this.getTopic(), execTime, this.getClass(), this.getTopic());
            }
            CommonUtils.mdcRemove();
        }
    }

    protected abstract String getTopic();

    protected abstract Boolean handleMs(T message);

    private T convert(String msg) {
        Type genericSuperclass = this.getClass().getGenericSuperclass();
        Type type = ((ParameterizedType) genericSuperclass).getActualTypeArguments()[0];
        return (T) JSONObject.parseObject(msg, type);
    }

}
