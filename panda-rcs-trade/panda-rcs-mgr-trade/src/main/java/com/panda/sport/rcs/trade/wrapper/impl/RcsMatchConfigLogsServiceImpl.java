package com.panda.sport.rcs.trade.wrapper.impl;

import com.panda.sport.rcs.core.utils.BeanCopyUtils;
import com.panda.sport.rcs.enums.ChangeLevelEnum;
import com.panda.sport.rcs.mapper.RcsMatchConfigLogsMapper;
import com.panda.sport.rcs.mapper.RcsMatchMarketConfigLogsMapper;
import com.panda.sport.rcs.mapper.StandardSportMarketMapper;
import com.panda.sport.rcs.pojo.RcsMatchConfigLogs;
import com.panda.sport.rcs.trade.wrapper.RcsMatchConfigLogsService;
import com.panda.sport.rcs.vo.MarketLiveOddsQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author :  myname
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.console.service.impl
 * @Description :  TODO
 * @Date: 2020-02-10 15:55
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Service
public class RcsMatchConfigLogsServiceImpl implements RcsMatchConfigLogsService {
    @Autowired
    private RcsMatchConfigLogsMapper rcsMatchConfigLogsMapper;
    @Autowired
    private RcsMatchMarketConfigLogsMapper rcsMatchMarketConfigLogsMapper;
    @Autowired
    private StandardSportMarketMapper standardSportMarketMapper;
    @Override
    public void insertRcsMatchConfig(MarketLiveOddsQueryVo marketLiveOddsQueryVo) {
        RcsMatchConfigLogs rcsMatchConfigLogs = new RcsMatchConfigLogs();
        BeanCopyUtils.copyProperties(marketLiveOddsQueryVo, rcsMatchConfigLogs);
        rcsMatchConfigLogs.setId(null);
        rcsMatchConfigLogsMapper.insert(rcsMatchConfigLogs);
        List<Long> ids = standardSportMarketMapper.selectMarketIdByMatchId(marketLiveOddsQueryVo.getMatchId());
        if (!CollectionUtils.isEmpty(ids)) {
            if (marketLiveOddsQueryVo.getTradeType() == null) {
                rcsMatchMarketConfigLogsMapper.insertRcsMatchMarketConfigLogs(ids, marketLiveOddsQueryVo.getMatchId(), marketLiveOddsQueryVo.getOperateMatchStatus(), null,
                        ChangeLevelEnum.MATCH.getLevel());
            } else {
                rcsMatchMarketConfigLogsMapper.insertRcsMatchMarketConfigLogs(ids, marketLiveOddsQueryVo.getMatchId(), marketLiveOddsQueryVo.getOperateMatchStatus(),
                        1 - marketLiveOddsQueryVo.getTradeType(), ChangeLevelEnum.MATCH.getLevel());
            }
        }
    }
}
