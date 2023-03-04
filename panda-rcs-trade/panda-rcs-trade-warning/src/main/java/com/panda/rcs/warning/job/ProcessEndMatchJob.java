package com.panda.rcs.warning.job;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.panda.rcs.warning.mapper.MatchOperateExceptionMonitorMapper;
import com.panda.rcs.warning.mapper.RcsMatchMonitorListMapper;
import com.panda.rcs.warning.mapper.RcsMatchMonitorMqLicenseMapper;
import com.panda.rcs.warning.service.MatchOperateExceptionMonitorApi;
import com.panda.rcs.warning.vo.MatchOperateExListVo;
import com.panda.rcs.warning.vo.RcsMatchMonitorList;
import com.panda.rcs.warning.vo.RcsMatchMonitorMqLicense;
import com.panda.rcs.warning.vo.RollBallMatchInfo;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author :  myname
 * @Project Name :  panda-rcs-trade-group
 * @Package Name :  com.panda.rcs.warning.job
 * @Description :  TODO
 * @Date: 2022-08-06 14:56
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@JobHandler(value = "processEndMatchJob")
@Component
@Slf4j
public class ProcessEndMatchJob extends IJobHandler {
    @Autowired
    private RcsMatchMonitorListMapper rcsMatchMonitorListMapper;
    @Autowired
    private MatchOperateExceptionMonitorApi matchOperateExceptionMonitorApi;
    @Autowired
    private MatchOperateExceptionMonitorMapper matchOperateExceptionMonitorMapper;
    @Autowired
    private RcsMatchMonitorMqLicenseMapper rcsMatchMonitorMqLicenseMapper;

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        List<RcsMatchMonitorList> list = rcsMatchMonitorListMapper.selectList(new LambdaQueryWrapper<RcsMatchMonitorList>().eq(RcsMatchMonitorList::getMatchType, 1).le(RcsMatchMonitorList::getBeginTime, System.currentTimeMillis()));
        List<Long> matchIds = list.stream().map(RcsMatchMonitorList::getMatchId).collect(Collectors.toList());
        log.info("::开始执行滚球不开售赛事操作:{},赛事ID:{}", list.size(), JSON.toJSONString(matchIds));
        if (!list.isEmpty()) {
            list.forEach(rcsMatchMonitorList -> {
                //清空监控列表数据
                rcsMatchMonitorListMapper.deleteById(rcsMatchMonitorList);
                //写入恢复断链的日志
                RollBallMatchInfo rollBallMatchInfo = new RollBallMatchInfo();
                rollBallMatchInfo.setMatchId(rcsMatchMonitorList.getMatchId());
                rollBallMatchInfo.setPlayId(rcsMatchMonitorList.getPlayId());
                rollBallMatchInfo.setMatchManageId(Long.valueOf(rcsMatchMonitorList.getMatchManageId()));
                MatchOperateExListVo operateExListVo = matchOperateExceptionMonitorMapper.queryMatchByTimerAndMatchStatus(rcsMatchMonitorList.getMatchId());
                RcsMatchMonitorMqLicense monitorExBean = rcsMatchMonitorMqLicenseMapper.selectOne(new LambdaQueryWrapper<RcsMatchMonitorMqLicense>().eq(RcsMatchMonitorMqLicense::getMatchId, rcsMatchMonitorList.getMatchId()).eq(RcsMatchMonitorMqLicense::getPlayId, rcsMatchMonitorList.getPlayId()).eq(RcsMatchMonitorMqLicense::getMatchType, 1));
                matchOperateExceptionMonitorApi.insertErrorLog(rollBallMatchInfo, rcsMatchMonitorList.getPlayIdCode(), operateExListVo, monitorExBean);
            });
        }
        return ReturnT.SUCCESS;
    }


}
