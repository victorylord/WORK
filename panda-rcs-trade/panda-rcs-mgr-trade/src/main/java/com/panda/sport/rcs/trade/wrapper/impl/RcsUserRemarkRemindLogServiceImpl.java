package com.panda.sport.rcs.trade.wrapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.common.DateUtils;
import com.panda.sport.rcs.mapper.RcsUserRemarkRemindLogMapper;
import com.panda.sport.rcs.pojo.RcsUserRemarkRemindLog;
import com.panda.sport.rcs.trade.wrapper.IRcsUserRemarkRemindLogService;
import com.panda.sport.rcs.trade.wrapper.RcsTradingAssignmentService;
import com.panda.sports.api.vo.ShortSysUserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 用戶人工备注提醒日志
 *
 * @description:
 * @author: magic
 * @create: 2022-05-29 10:15
 **/
@Slf4j
@Service
public class RcsUserRemarkRemindLogServiceImpl extends ServiceImpl<RcsUserRemarkRemindLogMapper, RcsUserRemarkRemindLog> implements IRcsUserRemarkRemindLogService {

    @Autowired
    RcsTradingAssignmentService rcsTradingAssignmentService;

    @Override
    public void updateRemark(RcsUserRemarkRemindLog rcsUserRemarkRemindLog, int traderId) {
        if ("".equals(rcsUserRemarkRemindLog.getRemindDate())) {
            rcsUserRemarkRemindLog.setRemindDate(null);
        }
        //插入日志
        ShortSysUserVO traderData = rcsTradingAssignmentService.getShortSysUserById(traderId);
        rcsUserRemarkRemindLog.setCreateTime(DateUtils.changeDateToString(new Date()));
        rcsUserRemarkRemindLog.setCreateUserId(traderData.getId().longValue());
        rcsUserRemarkRemindLog.setCreateUserName(traderData.getUserCode());
        baseMapper.insert(rcsUserRemarkRemindLog);
    }
}
