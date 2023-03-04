package com.panda.sport.rcs.trade.wrapper.statistics.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.mapper.statistics.RcsTotalValueNearlyOneTimeMapper;
import com.panda.sport.rcs.pojo.statistics.RcsTotalValueNearlyOneTime;
import com.panda.sport.rcs.trade.wrapper.statistics.RcsTotalValueNearlyOneTimeService;
import org.springframework.stereotype.Service;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.mgr.wrapper.statistics.impl
 * @Description :  近一小时货量统计
 * @Date: 2019-12-30 20:21
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Service
public class RcsTotalValueNearlyOneTimeServiceImpl extends ServiceImpl<RcsTotalValueNearlyOneTimeMapper, RcsTotalValueNearlyOneTime> implements RcsTotalValueNearlyOneTimeService {
}
