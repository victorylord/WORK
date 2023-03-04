package com.panda.sport.rcs.trade.wrapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.mapper.RcsMatchOrderAcceptEventConfigMapper;
import com.panda.sport.rcs.pojo.RcsMatchOrderAcceptEventConfig;
import com.panda.sport.rcs.trade.wrapper.RcsMatchOrderAcceptEventConfigService;
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
 * @Date: 2020-02-01 18:54
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Service
public class RcsMatchOrderAcceptEventConfigServiceImpl extends ServiceImpl<RcsMatchOrderAcceptEventConfigMapper, RcsMatchOrderAcceptEventConfig> implements RcsMatchOrderAcceptEventConfigService {
    @Autowired
    private RcsMatchOrderAcceptEventConfigMapper rcsMatchOrderAcceptEventConfigMapper;

    @Override
    public void insertRcsMatchOrderAcceptEventConfigs(List<RcsMatchOrderAcceptEventConfig> list) {
        rcsMatchOrderAcceptEventConfigMapper.batchInsert(list);
    }

    @Override
    public List<RcsMatchOrderAcceptEventConfig> selectRcsMatchOrderAcceptEventConfig(Long matchId) {
        Map<String, Object> columnMap = new HashMap<>(1);
        columnMap.put("match_id", matchId);
        List<RcsMatchOrderAcceptEventConfig> rcsMatchOrderAcceptEventConfigList = rcsMatchOrderAcceptEventConfigMapper.selectByMap(columnMap);
        return rcsMatchOrderAcceptEventConfigList;
    }

    /**
     * @Description   批量添加或者更改
     * @Param [list]
     * @Author  toney
     * @Date  11:34 2020/5/3
     * @return void
     **/
    @Override
    public void insertOrUpdate(List<RcsMatchOrderAcceptEventConfig> list){
        for(RcsMatchOrderAcceptEventConfig bean : list) {
            rcsMatchOrderAcceptEventConfigMapper.insertOrUpdate(bean);
        }
    }
}
