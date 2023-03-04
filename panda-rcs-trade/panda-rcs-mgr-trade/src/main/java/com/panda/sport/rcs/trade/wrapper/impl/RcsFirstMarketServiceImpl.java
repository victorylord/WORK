package com.panda.sport.rcs.trade.wrapper.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.mapper.RcsFirstMarketMapper;
import com.panda.sport.rcs.pojo.RcsFirstMarket;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.trade.wrapper.RcsFirstMarketService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RcsFirstMarketServiceImpl extends ServiceImpl<RcsFirstMarketMapper, RcsFirstMarket> implements RcsFirstMarketService {

    @Resource
    private RcsFirstMarketMapper rcsFirstMarketMapper;

    @Override
    public Map getPreEndMarketValue(StandardMatchInfo dto) {
        QueryWrapper<RcsFirstMarket> rcsFirstMarketQueryWrapper = new QueryWrapper<>();
        rcsFirstMarketQueryWrapper.lambda().eq(RcsFirstMarket::getStandardMatchId,dto.getId());
        rcsFirstMarketQueryWrapper.lambda().in(RcsFirstMarket::getPlayId,38,39);
        rcsFirstMarketQueryWrapper.lambda().eq(RcsFirstMarket::getType,2);
        List<RcsFirstMarket> rcsFirstMarkets = rcsFirstMarketMapper.selectList(rcsFirstMarketQueryWrapper);
        HashMap<String, String> objectObjectHashMap = new HashMap<>();
        for (RcsFirstMarket rcsFirstMarket : rcsFirstMarkets) {
            objectObjectHashMap.put(String.valueOf(rcsFirstMarket.getPlayId()),rcsFirstMarket.getValue());
        }
        return objectObjectHashMap;
    }

    @Override
    public int batchInsertOrUpdateEndMarket(List<RcsFirstMarket> list) {
        if(CollectionUtils.isEmpty(list)){return 1;}
        for (RcsFirstMarket rcsFirstMarket : list) {
            if(!(rcsFirstMarket.getPlayId()==38||rcsFirstMarket.getPlayId()==39)){continue;}
            QueryWrapper<RcsFirstMarket> rcsFirstMarketQueryWrapper = new QueryWrapper<>();
            rcsFirstMarketQueryWrapper.lambda().eq(RcsFirstMarket::getStandardMatchId,rcsFirstMarket.getId());
            rcsFirstMarketQueryWrapper.lambda().eq(RcsFirstMarket::getPlayId,rcsFirstMarket.getPlayId());
            rcsFirstMarketQueryWrapper.lambda().eq(RcsFirstMarket::getType,2);
            List<RcsFirstMarket> rcsFirstMarkets = rcsFirstMarketMapper.selectList(rcsFirstMarketQueryWrapper);
            if(!CollectionUtils.isEmpty(rcsFirstMarkets)){continue;}
            rcsFirstMarket.setType(2);
            rcsFirstMarketMapper.insertOrUpdate(rcsFirstMarket);
        }
        return 0;
    }

}


