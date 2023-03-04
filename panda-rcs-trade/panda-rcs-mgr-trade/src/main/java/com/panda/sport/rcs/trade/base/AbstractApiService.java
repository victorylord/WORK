package com.panda.sport.rcs.trade.base;

import com.panda.sport.data.rcs.api.ExceptionData;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.shaded.com.google.common.collect.Lists;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * API接口实现基类
 */
@Slf4j
public abstract class AbstractApiService<T> {
    @Resource(name = "asyncPoolTaskExecutor")
    protected ThreadPoolTaskExecutor asyncPoolTaskExecutor;

    /**
     * 异常信息转为接口需要的格式
     *
     * @param exceptionMap
     * @return
     */
    public List<ExceptionData> toApiResult(Map<String, String> exceptionMap) {
        if (CollectionUtils.isEmpty(exceptionMap)) {
            return Collections.emptyList();
        }
        List<ExceptionData> resultList = Lists.newArrayListWithCapacity(exceptionMap.size());
        for (Map.Entry<String, String> entry : exceptionMap.entrySet()) {
            ExceptionData data = new ExceptionData();
            data.setId(entry.getKey());
            data.setDescription(entry.getValue());
            resultList.add(data);
        }
        return resultList;
    }

    /**
     * 接口入参校验
     *
     * @return
     */
    protected abstract List<ExceptionData> validation(T requestData);

    /**
     * 处理业务逻辑
     *
     * @param requestData
     * @return
     */
    protected abstract Map<String, String> doRequest(T requestData);

    /**
     * 请求处理驱动方法
     *
     * @param requestData
     * @return
     */
    public List<ExceptionData> dispatch(T requestData) {
        log.info("Receive data:{}", JsonFormatUtils.toJson(requestData));

        // 校验数据合法性
        List<ExceptionData> invalidList = this.validation(requestData);

        // 异步通知下游系统
        /*log.info("Notify downstream system...");
        asyncPoolTaskExecutor.submit(() -> notifyDownstreamSystem(requestData));*/

        // 处理风控业务逻辑
        Map<String, String> errorMap = this.doRequest(requestData);

        List<ExceptionData> errorList = this.toApiResult(errorMap);

        if (invalidList == null) {
            invalidList = Lists.newArrayList();
        }
        // 汇总不合法数据：业务处理失败日志+数据校验失败日志
        if (!CollectionUtils.isEmpty(errorList)) {
            invalidList.addAll(errorList);
        }
        log.info("Response Data:{}", JsonFormatUtils.toJson(invalidList));
        return invalidList;
    }

    /**
     * 通知下游系统：异步
     *
     * @param requestData
     */
    protected abstract void notifyDownstreamSystem(T requestData);


}
