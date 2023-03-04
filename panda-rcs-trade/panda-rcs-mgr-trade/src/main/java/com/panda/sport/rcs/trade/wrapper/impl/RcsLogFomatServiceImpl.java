package com.panda.sport.rcs.trade.wrapper.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.mapper.RcsLogFomatMapper;
import com.panda.sport.rcs.pojo.RcsLogFomat;
import com.panda.sport.rcs.trade.wrapper.RcsLogFomatService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class RcsLogFomatServiceImpl  extends ServiceImpl<RcsLogFomatMapper, RcsLogFomat> implements RcsLogFomatService
{
    @Resource
    private RcsLogFomatMapper rcsLogFomatMapper;

    @Override
    public int updateBatch(List<RcsLogFomat> list) {
        return rcsLogFomatMapper.updateBatch(list);
    }

    @Override
    public int batchInsert(List<RcsLogFomat> list) {
        return rcsLogFomatMapper.batchInsert(list);
    }

    @Override
    public int insertOrUpdate(RcsLogFomat record) {
        return rcsLogFomatMapper.insertOrUpdate(record);
    }

    @Override
    public int insertOrUpdateSelective(RcsLogFomat record) {
        return rcsLogFomatMapper.insertOrUpdateSelective(record);
    }

    @Override
    public List<RcsLogFomat> getChampionMatchOperateLogs(String matchId) {
        return rcsLogFomatMapper.getChampionMatchOperateLogs(matchId);
    }
}
