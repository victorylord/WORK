package com.panda.sport.rcs.trade.wrapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.mapper.RcsStandardOutrightMatchInfoMapper;
import com.panda.sport.rcs.pojo.RcsStandardOutrightMatchInfo;
import com.panda.sport.rcs.trade.wrapper.RcsStandardOutrightMatchInfoService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class RcsStandardOutrightMatchInfoServiceImpl extends ServiceImpl<RcsStandardOutrightMatchInfoMapper, RcsStandardOutrightMatchInfo> implements RcsStandardOutrightMatchInfoService {

    @Resource
    private RcsStandardOutrightMatchInfoMapper rcsStandardOutrightMatchInfoMapper;

    @Override
    public int deleteByPrimaryKey(Long id) {
        return rcsStandardOutrightMatchInfoMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insertSelective(RcsStandardOutrightMatchInfo record) {
        return rcsStandardOutrightMatchInfoMapper.insertSelective(record);
    }

    @Override
    public RcsStandardOutrightMatchInfo selectByPrimaryKey(Long id) {
        return rcsStandardOutrightMatchInfoMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(RcsStandardOutrightMatchInfo record) {
        return rcsStandardOutrightMatchInfoMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(RcsStandardOutrightMatchInfo record) {
        return rcsStandardOutrightMatchInfoMapper.updateByPrimaryKey(record);
    }

    @Override
    public int updateBatch(List<RcsStandardOutrightMatchInfo> list) {
        return rcsStandardOutrightMatchInfoMapper.updateBatch(list);
    }

    @Override
    public int updateBatchSelective(List<RcsStandardOutrightMatchInfo> list) {
        return rcsStandardOutrightMatchInfoMapper.updateBatchSelective(list);
    }

    @Override
    public int batchInsert(List<RcsStandardOutrightMatchInfo> list) {
        return rcsStandardOutrightMatchInfoMapper.batchInsert(list);
    }

    @Override
    public int insertOrUpdate(RcsStandardOutrightMatchInfo record) {
        return rcsStandardOutrightMatchInfoMapper.insertOrUpdate(record);
    }

    @Override
    public int insertOrUpdateSelective(RcsStandardOutrightMatchInfo record) {
        return rcsStandardOutrightMatchInfoMapper.insertOrUpdateSelective(record);
    }

    @Override
    public int batchInsertOrUpdate(List<RcsStandardOutrightMatchInfo> standardSportMarketCategories) {
        if(CollectionUtils.isEmpty(standardSportMarketCategories)){return 0;}
        return rcsStandardOutrightMatchInfoMapper.batchInsertOrUpdate(standardSportMarketCategories);

    }

}





