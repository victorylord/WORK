package com.panda.sport.rcs.trade.wrapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.constants.MatchEventEnum;
import com.panda.sport.rcs.enums.DataSourceEnum;
import com.panda.sport.rcs.enums.HalfTimeEnum;
import com.panda.sport.rcs.enums.ModeEnum;
import com.panda.sport.rcs.mapper.RcsTournamentOrderAcceptConfigMapper;
import com.panda.sport.rcs.pojo.RcsTournamentOrderAcceptConfig;
import com.panda.sport.rcs.pojo.RcsTournamentOrderAcceptEventConfig;
import com.panda.sport.rcs.trade.wrapper.RcsTournamentOrderAcceptConfigService;
import com.panda.sport.rcs.trade.wrapper.RcsTournamentOrderAcceptEventConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author :  myname
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.wrapper.impl
 * @Description :  TODO
 * @Date: 2020-02-01 13:39
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Service
@Slf4j
public class RcsTournamentOrderAcceptConfigServiceImpl extends ServiceImpl<RcsTournamentOrderAcceptConfigMapper, RcsTournamentOrderAcceptConfig> implements RcsTournamentOrderAcceptConfigService {
    @Autowired
    private RcsTournamentOrderAcceptConfigMapper rcsTournamentOrderAcceptConfigMapper;
    @Autowired
    private RcsTournamentOrderAcceptEventConfigService rcsTournamentOrderAcceptEventConfigService;
    //最短等待时间
    private final static Integer MIN_WAIT = 3;
    //最长等待时间
    private final static Integer MAX_WAIT = 20;
    @Override
    public RcsTournamentOrderAcceptConfig selectByTournamentId(Long tournamentId) {
        return rcsTournamentOrderAcceptConfigMapper.selectById(tournamentId);
    }

    @Override
    public void insertByTournamentId(RcsTournamentOrderAcceptConfig rcsTournamentOrderAcceptConfig) {
        rcsTournamentOrderAcceptConfigMapper.insert(rcsTournamentOrderAcceptConfig);
    }

    @Override
    public void update(RcsTournamentOrderAcceptConfig rcsTournamentOrderAcceptConfig) {
        rcsTournamentOrderAcceptConfigMapper.updateById(rcsTournamentOrderAcceptConfig);
    }

    @Override
    public RcsTournamentOrderAcceptConfig init(Long tournamentId) {
        RcsTournamentOrderAcceptConfig rcsTournamentOrderAcceptConfig = new RcsTournamentOrderAcceptConfig();
        rcsTournamentOrderAcceptConfig.setTournamentId(tournamentId);
        rcsTournamentOrderAcceptConfig.setDataSource(DataSourceEnum.SR.getDataSource());
        rcsTournamentOrderAcceptConfig.setMinWait(MIN_WAIT);
        rcsTournamentOrderAcceptConfig.setMaxWait(MAX_WAIT);
        rcsTournamentOrderAcceptConfig.setMode(ModeEnum.AUTOMATIC.getValue());
        rcsTournamentOrderAcceptConfig.setHalfTime(HalfTimeEnum.CLOSE.getValue());
        insertByTournamentId(rcsTournamentOrderAcceptConfig);
        //把所有的联赛事件放进去
        List<RcsTournamentOrderAcceptEventConfig> rcsTournamentOrderAcceptEventConfigList = new ArrayList<>();
        for (MatchEventEnum matchEventEnum : MatchEventEnum.values()) {
            if (matchEventEnum.isaBoolean()) {
                RcsTournamentOrderAcceptEventConfig rcsTournamentOrderAcceptEventConfig = new RcsTournamentOrderAcceptEventConfig(matchEventEnum, tournamentId);
                if (matchEventEnum.getCode().equals("bet_stop")) {
                    rcsTournamentOrderAcceptEventConfig.setValid(true);
                }
                rcsTournamentOrderAcceptEventConfigList.add(rcsTournamentOrderAcceptEventConfig);
            }
        }
        rcsTournamentOrderAcceptEventConfigService.insertRcsTournamentOrderAcceptEventConfigs(rcsTournamentOrderAcceptEventConfigList);
        return rcsTournamentOrderAcceptConfig;
    }
}
