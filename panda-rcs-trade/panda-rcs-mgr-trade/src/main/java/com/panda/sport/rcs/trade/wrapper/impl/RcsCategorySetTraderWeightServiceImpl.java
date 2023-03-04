package com.panda.sport.rcs.trade.wrapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.mapper.RcsCategorySetTraderWeightMapper;
import com.panda.sport.rcs.pojo.RcsCategorySetTraderWeight;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.trade.wrapper.RcsCategorySetTraderWeightService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class RcsCategorySetTraderWeightServiceImpl extends ServiceImpl<RcsCategorySetTraderWeightMapper, RcsCategorySetTraderWeight> implements RcsCategorySetTraderWeightService {

    @Resource
    private RcsCategorySetTraderWeightMapper rcsCategorySetTraderWeightMapper;


    @Override
    public int insertOrUpdate(RcsCategorySetTraderWeight record) {
        return rcsCategorySetTraderWeightMapper.insertOrUpdate(record);
    }

    @Override
    public int batchInsertOrUpdate(List<RcsCategorySetTraderWeight> list) {
        return rcsCategorySetTraderWeightMapper.batchInsertOrUpdate(list);
    }

    @Override
    public Integer selectPlayIdBySetId(RcsMatchMarketConfig config,Integer userId) {
        return rcsCategorySetTraderWeightMapper.selectPlayIdBySetId(config,userId);
    }

}
