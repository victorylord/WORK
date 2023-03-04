package com.panda.sport.rcs.trade.wrapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.panda.sport.rcs.bean.RcsMarketSellPersonGroup;
import com.panda.sport.rcs.mapper.RcsMarketSellPersonGroupMapper;
import com.panda.sport.rcs.trade.wrapper.RcsMarketSellPersonGroupService;
import com.panda.sport.rcs.utils.StringUtils;
import com.panda.sport.rcs.vo.StandardMarketSellQueryV2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RcsMarketSellPersonGroupServiceImpl extends ServiceImpl<RcsMarketSellPersonGroupMapper, RcsMarketSellPersonGroup> implements RcsMarketSellPersonGroupService {

    @Autowired
    private RcsMarketSellPersonGroupMapper rcsMarketSellPersonGroupMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveSpecialGroupPerson(StandardMarketSellQueryV2Vo standardMarketSellQueryVo) {
        List<RcsMarketSellPersonGroup> list = rcsMarketSellPersonGroupMapper.selectHistoryPerson(standardMarketSellQueryVo);
        try {
            if (list.size() > 0) {
                //1.根据此次数据,查询历史关注用户，进行失效处理
                for (RcsMarketSellPersonGroup p : list) {
                    p.setIsValid(0);
                    p.setUpdateTime(System.currentTimeMillis());
                    rcsMarketSellPersonGroupMapper.updateById(p);
                }
            }

            //2.添加此次关注的人员数据
            if (StringUtils.isNotEmpty(standardMarketSellQueryVo.getPersons())) {
                List<RcsMarketSellPersonGroup> addList = Lists.newArrayList();
                String[] uid = standardMarketSellQueryVo.getPersons().split(",");
                for (String pid : uid) {
                    RcsMarketSellPersonGroup gg = new RcsMarketSellPersonGroup();
                    gg.setUserId(Long.valueOf(standardMarketSellQueryVo.getUserId()));
                    gg.setSportId(standardMarketSellQueryVo.getSportId());
                    gg.setPersonId(Long.valueOf(pid));
                    gg.setIsValid(1);
                    gg.setCreateTime(System.currentTimeMillis());
                    gg.setUpdateTime(System.currentTimeMillis());
                    addList.add(gg);
                }
                rcsMarketSellPersonGroupMapper.batchInsertOrUpdate(addList);
            }
        } catch (Exception ex) {
            throw ex;
        }
        return true;
    }
}
