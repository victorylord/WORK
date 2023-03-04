package com.panda.sport.rcs.trade.wrapper.impl;

import com.panda.sport.rcs.enums.ChangeLevelEnum;
import com.panda.sport.rcs.mapper.RcsMatchMarketConfigLogsMapper;
import com.panda.sport.rcs.mapper.RcsMatchPlayConfigLogsMapper;
import com.panda.sport.rcs.mapper.StandardSportMarketMapper;
import com.panda.sport.rcs.pojo.RcsMatchPlayConfig;
import com.panda.sport.rcs.pojo.RcsMatchPlayConfigLogs;
import com.panda.sport.rcs.pojo.RcsPlayConfig;
import com.panda.sport.rcs.trade.wrapper.RcsMatchPlayConfigLogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author :  myname
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.console.service.impl
 * @Description :  TODO
 * @Date: 2020-02-10 15:35
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Service
public class RcsMatchPlayConfigLogsServiceImpl implements RcsMatchPlayConfigLogsService {
    @Autowired
    private RcsMatchPlayConfigLogsMapper rcsMatchPlayConfigLogsMapper;
    @Autowired
    private StandardSportMarketMapper standardSportMarketMapper;
    @Autowired
    private RcsMatchMarketConfigLogsMapper rcsMatchMarketConfigLogsMapper;
    @Override
    public List<RcsMatchPlayConfigLogs> selectByMatchId(Long matchId) {
        Map<String, Object> columnMap = new HashMap<>(1);
        columnMap.put("matchId", matchId);
        return rcsMatchPlayConfigLogsMapper.selectByMap(columnMap);
    }

    @Override
    public void insertRcsMatchPlayConfigLogs(RcsMatchPlayConfig rcsMatchPlayConfig) {
        List<Long> ids = standardSportMarketMapper.selectMarketIdByState(rcsMatchPlayConfig.getPlayId(), rcsMatchPlayConfig.getMatchId());
        if (!CollectionUtils.isEmpty(ids)) {
            rcsMatchMarketConfigLogsMapper.insertRcsMatchMarketConfigLogs(ids, rcsMatchPlayConfig.getMatchId(), rcsMatchPlayConfig.getStatus(), rcsMatchPlayConfig.getDataSource(), ChangeLevelEnum.PLAY_STATE.getLevel());
        }
    }

    @Override
    public void insertRcsMatchPlayConfigLogs(RcsPlayConfig rcsPlayConfig) {
        List<Long> ids = standardSportMarketMapper.selectMarketIdByState(rcsPlayConfig.getPlayId(), rcsPlayConfig.getMatchId());
        if (!CollectionUtils.isEmpty(ids)) {
            rcsMatchMarketConfigLogsMapper.insertRcsMatchMarketConfigLogs(ids, rcsPlayConfig.getMatchId(), rcsPlayConfig.getStatus(), rcsPlayConfig.getDataSource(),
                    ChangeLevelEnum.PLAY.getLevel());
        }
    }
}
