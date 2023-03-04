package com.panda.rcs.warning.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.panda.rcs.warning.vo.*;

import java.util.List;
import java.util.Map;

/**
 * @author :  myname
 * @Project Name :  panda-rcs-trade-group
 * @Package Name :  com.panda.rcs.warning.service.impl
 * @Description :  TODO
 * @Date: 2022-07-19 14:50
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface MatchOperateExceptionMonitorApi {


    void rollTheBallApproach(Integer type, Integer level);

    Map<Integer, Object> queryRcsMatchMonitorSetting();

    void updateRcsMatchMonitorSetting(RcsMatchMonitorSettingUpdate rcsMatchMonitorSetting);

    IPage<RcsMatchMonitorList> selectMatchOperateList(MatchOperateListQuery matchOperateListQuery);

    List<Long> queryMatchList(Integer type);

    IPage<RcsMatchMonitorErrorLog> queryErrorLogInfo(PageQuery pageQuery, String lang);

    void insertErrorLog(RollBallMatchInfo rollBallMatchInfo, Long playCode, MatchOperateExListVo operateExListVo, RcsMatchMonitorMqLicense monitorExBean);
}
