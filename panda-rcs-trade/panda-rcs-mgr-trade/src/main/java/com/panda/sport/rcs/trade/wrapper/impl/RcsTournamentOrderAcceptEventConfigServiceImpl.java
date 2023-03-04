package com.panda.sport.rcs.trade.wrapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.mapper.RcsTournamentOrderAcceptEventConfigMapper;
import com.panda.sport.rcs.pojo.RcsTournamentOrderAcceptEventConfig;
import com.panda.sport.rcs.trade.wrapper.RcsTournamentOrderAcceptEventConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author :  myname
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.wrapper.impl
 * @Description :  TODO
 * @Date: 2020-02-01 16:14
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Service
public class RcsTournamentOrderAcceptEventConfigServiceImpl extends ServiceImpl<RcsTournamentOrderAcceptEventConfigMapper, RcsTournamentOrderAcceptEventConfig> implements RcsTournamentOrderAcceptEventConfigService {
    @Autowired
    private RcsTournamentOrderAcceptEventConfigMapper rcsTournamentOrderAcceptEventConfigMapper;

    @Override
    public void insertRcsTournamentOrderAcceptEventConfigs(List<RcsTournamentOrderAcceptEventConfig> list) {
        rcsTournamentOrderAcceptEventConfigMapper.batchInsert(list);
    }

    @Override
    public List<RcsTournamentOrderAcceptEventConfig> selectRcsTournamentOrderAcceptEventConfigs(Long matchId) {
        Map<String, Object> columnMap = new HashMap<>(1);
        columnMap.put("tournament_id", matchId);
        columnMap.put("valid", 1);
        return rcsTournamentOrderAcceptEventConfigMapper.selectByMap(columnMap);
    }

    @Override
    public void updateRcsTournamentOrderAcceptEventConfigs(List<RcsTournamentOrderAcceptEventConfig> list) {
        rcsTournamentOrderAcceptEventConfigMapper.updateBatch(list);
    }
}
