package com.panda.rcs.warning.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.panda.rcs.warning.mapper.RcsMatchMonitorListMapper;
import com.panda.rcs.warning.mapper.RcsMatchMonitorMqLicenseMapper;
import com.panda.rcs.warning.mapper.StandardMatchInfoMapper;
import com.panda.rcs.warning.service.MatchOperateExceptionMonitorApi;
import com.panda.rcs.warning.vo.RcsMatchMonitorList;
import com.panda.rcs.warning.vo.RcsMatchMonitorMqLicense;
import com.panda.rcs.warning.vo.StandardMatchInfo;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * @author :  koala
 * @Project Name :  panda-rcs-trade-group
 * @Package Name :  com.panda.rcs.warning.job
 * @Description :  异常监控兜底操作
 * @Date: 2022-07-26 11:15
 * --------  ---------  --------------------------
 */
@JobHandler(value = "backPocketOperationListJob")
@Component
@Slf4j
public class BackPocketOperationListJob extends IJobHandler {
    @Autowired
    RcsMatchMonitorListMapper rcsMatchMonitorListMapper;
    @Autowired
    MatchOperateExceptionMonitorApi matchOperateExceptionMonitorApi;
    @Autowired
    StandardMatchInfoMapper standardMatchInfoMapper;
    @Autowired
    RcsMatchMonitorMqLicenseMapper rcsMatchMonitorMqLicenseMapper;

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        //查询异常监控里面高危的数据
        List<Long> matchIdList = matchOperateExceptionMonitorApi.queryMatchList(1);
        matchIdList.forEach(id -> {
            StandardMatchInfo standardMatchInfo = standardMatchInfoMapper.selectById(id);
            if (Objects.nonNull(standardMatchInfo) && (standardMatchInfo.getMatchStatus() == 3 || standardMatchInfo.getMatchStatus() == 4)) {
                log.info("::开始处理赛事{}异常监控兜底操作", id);
                LambdaQueryWrapper<RcsMatchMonitorList> updateQuery = new LambdaQueryWrapper<>();
                updateQuery.eq(RcsMatchMonitorList::getMatchId, id);
                rcsMatchMonitorListMapper.delete(updateQuery);
                LambdaQueryWrapper<RcsMatchMonitorMqLicense> deleteMq = new LambdaQueryWrapper<>();
                deleteMq.eq(RcsMatchMonitorMqLicense::getMatchId, id);
                rcsMatchMonitorMqLicenseMapper.delete(deleteMq);
            }

        });
        return ReturnT.SUCCESS;
    }
}
