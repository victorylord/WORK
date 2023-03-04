package com.panda.sport.rcs.trade.wrapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.mapper.SystemItemDictMapper;
import com.panda.sport.rcs.pojo.SystemItemDict;
import com.panda.sport.rcs.trade.wrapper.SystemItemDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author :  myname
 * @Project Name :  daima1
 * @Package Name :  com.panda.sport.rcs.trade.wrapper.impl
 * @Description :  TODO
 * @Date: 2019-10-16 11:53
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Service
public class SystemItemDictServiceImpl extends ServiceImpl<SystemItemDictMapper, SystemItemDict> implements SystemItemDictService {
    @Autowired
    private SystemItemDictMapper systemItemDictMapper;

    @Override
    public List<SystemItemDict> getPlayingTimes() {
        Map<String, Object> columnMap = new HashMap<>(1);
        columnMap.put("parent_type_id", 7);
        return systemItemDictMapper.selectByMap(columnMap);
    }
}
