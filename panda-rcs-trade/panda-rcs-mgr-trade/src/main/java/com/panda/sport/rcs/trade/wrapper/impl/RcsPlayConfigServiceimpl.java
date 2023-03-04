package com.panda.sport.rcs.trade.wrapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.mapper.RcsPlayConfigMapper;
import com.panda.sport.rcs.pojo.RcsMatchPlayConfig;
import com.panda.sport.rcs.pojo.RcsPlayConfig;
import com.panda.sport.rcs.trade.wrapper.RcsPlayConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author :  myname
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.wrapper.impl
 * @Description :  TODO
 * @Date: 2020-02-17 22:31
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Service
public class RcsPlayConfigServiceimpl extends ServiceImpl<RcsPlayConfigMapper, RcsPlayConfig> implements RcsPlayConfigService {
    @Autowired
    private RcsPlayConfigMapper rcsPlayConfigMapper;

    @Override
    public Boolean insertOrUpdateRcsPlayConfig(RcsPlayConfig rcsPlayConfig) {
        return false;
    }

    @Override
    public List<RcsPlayConfig> selectRcsPlayConfigByMap(Map<String, Object> columnMap) {
        return rcsPlayConfigMapper.selectByMap(columnMap);
    }

    @Override
    public void insert(RcsPlayConfig rcsPlayConfig) {
        rcsPlayConfigMapper.insert(rcsPlayConfig);
    }

    @Override
    public void updateOrInsertRcsPlayConfigList(RcsMatchPlayConfig rcsPlayConfig, List<Long> playIdList) {
        rcsPlayConfigMapper.updateOrInsertRcsPlayConfigList(rcsPlayConfig.getMatchId(), rcsPlayConfig.getDataSource(), rcsPlayConfig.getStatus(), playIdList);
    }
}
