package com.panda.sport.rcs.trade.wrapper.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.core.db.annotation.Master;
import com.panda.sport.rcs.mapper.StandardSportTypeMapper;
import com.panda.sport.rcs.pojo.StandardSportType;
import com.panda.sport.rcs.trade.wrapper.IStandardSportTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * 标准体育种类表.  服务实现类
 * </p>
 *
 * @author author
 * @since 2019-09-27
 */
@Service
public class StandardSportTypeServiceImpl extends ServiceImpl<StandardSportTypeMapper, StandardSportType> implements IStandardSportTypeService {

    @Autowired
    StandardSportTypeMapper standardSportTypeMapper;

    @Override
    public List<StandardSportType> getStandardSportTypeList() {
        HashMap<String, Object> hashMap = new HashMap<>(1);
        List<StandardSportType> standardSportTypes = standardSportTypeMapper.selectByMap(hashMap);
        return standardSportTypes;
    }
}
