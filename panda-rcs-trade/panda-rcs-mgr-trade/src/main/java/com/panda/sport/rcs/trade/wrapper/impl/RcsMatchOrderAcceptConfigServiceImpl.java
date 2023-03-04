package com.panda.sport.rcs.trade.wrapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.constants.MatchEventEnum;
import com.panda.sport.rcs.core.utils.BeanCopyUtils;
import com.panda.sport.rcs.enums.DataSourceEnum;
import com.panda.sport.rcs.enums.HalfTimeEnum;
import com.panda.sport.rcs.enums.ModeEnum;
import com.panda.sport.rcs.mapper.RcsMatchOrderAcceptConfigMapper;
import com.panda.sport.rcs.pojo.RcsMatchOrderAcceptConfig;
import com.panda.sport.rcs.pojo.RcsMatchOrderAcceptEventConfig;
import com.panda.sport.rcs.pojo.RcsTournamentOrderAcceptConfig;
import com.panda.sport.rcs.pojo.RcsTournamentOrderAcceptEventConfig;
import com.panda.sport.rcs.trade.wrapper.RcsMatchOrderAcceptConfigService;
import com.panda.sport.rcs.trade.wrapper.RcsMatchOrderAcceptEventConfigService;
import com.panda.sport.rcs.trade.wrapper.RcsTournamentOrderAcceptEventConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class RcsMatchOrderAcceptConfigServiceImpl extends ServiceImpl<RcsMatchOrderAcceptConfigMapper, RcsMatchOrderAcceptConfig> implements RcsMatchOrderAcceptConfigService {
    //最短等待时间
    private final static Integer MIN_WAIT = 3;
    //最长等待时间
    private final static Integer MAX_WAIT = 120;
    @Resource
    private RcsMatchOrderAcceptConfigMapper rcsMatchOrderAcceptConfigMapper;
    @Autowired
    private RcsMatchOrderAcceptEventConfigService rcsMatchOrderAcceptEventConfigService;
    @Autowired
    private RcsTournamentOrderAcceptEventConfigService rcsTournamentOrderAcceptEventConfigService;

    @Override
    public int updateBatch(List<RcsMatchOrderAcceptConfig> list) {
        return rcsMatchOrderAcceptConfigMapper.updateBatch(list);
    }

    @Override
    public int batchInsert(List<RcsMatchOrderAcceptConfig> list) {
        return rcsMatchOrderAcceptConfigMapper.batchInsert(list);
    }

    @Override
    public int insertOrUpdate(RcsMatchOrderAcceptConfig record) {
        return rcsMatchOrderAcceptConfigMapper.insertOrUpdate(record);
    }

    @Override
    public int insertOrUpdateSelective(RcsMatchOrderAcceptConfig record) {
        return rcsMatchOrderAcceptConfigMapper.insertOrUpdateSelective(record);
    }

    @Override
    public RcsMatchOrderAcceptConfig selectRcsMatchOrderAcceptConfigById(Long matchId) {
        return rcsMatchOrderAcceptConfigMapper.selectById(matchId);
    }

    @Override
    public RcsMatchOrderAcceptConfig init(Long matchId) {
        RcsMatchOrderAcceptConfig rcsMatchOrderAcceptConfig = new RcsMatchOrderAcceptConfig();
        rcsMatchOrderAcceptConfig.setDataSource(DataSourceEnum.SR.getDataSource());
        rcsMatchOrderAcceptConfig.setHalfTime(HalfTimeEnum.CLOSE.getValue());
        rcsMatchOrderAcceptConfig.setMatchId(matchId);
        rcsMatchOrderAcceptConfig.setMaxWait(MAX_WAIT);
        rcsMatchOrderAcceptConfig.setMinWait(MIN_WAIT);
        rcsMatchOrderAcceptConfig.setMode(ModeEnum.AUTOMATIC.getValue());
        insertOrUpdate(rcsMatchOrderAcceptConfig);
        List<RcsMatchOrderAcceptEventConfig> rcsMatchOrderAcceptEventConfigList = new ArrayList<>();
        for (MatchEventEnum matchEventEnum : MatchEventEnum.values()) {
            if (matchEventEnum.isaBoolean()) {
                RcsMatchOrderAcceptEventConfig rcsMatchOrderAcceptEventConfig = new RcsMatchOrderAcceptEventConfig(matchEventEnum, matchId);
                if (matchEventEnum.isValidate()) {
                    rcsMatchOrderAcceptEventConfig.setValid(true);
                }
                rcsMatchOrderAcceptEventConfigList.add(rcsMatchOrderAcceptEventConfig);
            }
        }
        rcsMatchOrderAcceptEventConfigService.insertRcsMatchOrderAcceptEventConfigs(rcsMatchOrderAcceptEventConfigList);
        return rcsMatchOrderAcceptConfig;
    }

    @Override
    public RcsMatchOrderAcceptConfig init(RcsTournamentOrderAcceptConfig rcsTournamentOrderAcceptConfig, Long matchId) {
        RcsMatchOrderAcceptConfig rcsMatchOrderAcceptConfig = new RcsMatchOrderAcceptConfig();
        BeanCopyUtils.copyProperties(rcsTournamentOrderAcceptConfig, rcsMatchOrderAcceptConfig);
        rcsMatchOrderAcceptConfig.setMatchId(matchId);
        //查找该联赛所有配置
        List<RcsMatchOrderAcceptEventConfig> rcsMatchOrderAcceptEventConfigList = new ArrayList<>();
        List<RcsTournamentOrderAcceptEventConfig> rcsTournamentOrderAcceptEventConfigs =
            rcsTournamentOrderAcceptEventConfigService.selectRcsTournamentOrderAcceptEventConfigs(rcsTournamentOrderAcceptConfig.getTournamentId());
        for (RcsTournamentOrderAcceptEventConfig rcsTournamentOrderAcceptEventConfig : rcsTournamentOrderAcceptEventConfigs) {
            RcsMatchOrderAcceptEventConfig rcsMatchOrderAcceptEventConfig = new RcsMatchOrderAcceptEventConfig();
            BeanCopyUtils.copyProperties(rcsTournamentOrderAcceptEventConfig, rcsMatchOrderAcceptEventConfig);
            rcsMatchOrderAcceptEventConfig.setMatchId(matchId);
            rcsMatchOrderAcceptEventConfigList.add(rcsMatchOrderAcceptEventConfig);
        }
        rcsMatchOrderAcceptEventConfigService.insertRcsMatchOrderAcceptEventConfigs(rcsMatchOrderAcceptEventConfigList);
        return rcsMatchOrderAcceptConfig;
    }


    @Override
    public void updateRcsMatchOrderAcceptConfig(RcsMatchOrderAcceptConfig rcsMatchOrderAcceptConfig) {
        rcsMatchOrderAcceptConfigMapper.updateById(rcsMatchOrderAcceptConfig);
    }
}
