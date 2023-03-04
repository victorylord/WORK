package com.panda.rcs.warning.job;

import com.panda.rcs.warning.service.MatchOperateExceptionMonitorApi;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import groovy.util.logging.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author :  davidqiang
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.task.job
 * @Description :  早盘中级警报操作监控
 * @Date: 2022-06-08 13:02
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@JobHandler(value = "preMidMatchOperateExceptionMonitorJob")
@Component
@Slf4j
public class PreMidMatchOperateExceptionMonitorJob extends IJobHandler {
    @Autowired
    private MatchOperateExceptionMonitorApi matchOperateExceptionMonitorApi;


    @Override
    public ReturnT<String> execute(String s) throws Exception {
        XxlJobLogger.log("PreMidMatchOperateExceptionMonitorJob-JOB, Hello World.");
        matchOperateExceptionMonitorApi.rollTheBallApproach(1,2);
        return SUCCESS;
    }
}
