package com.panda.sport.rcs.trade.wrapper.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.panda.sport.rcs.mapper.RcsMarketCategorySetRelationMapper;
import com.panda.sport.rcs.mongo.CategorySetOrderNo;
import com.panda.sport.rcs.pojo.RcsMarketCategorySetRelation;
import com.panda.sport.rcs.trade.wrapper.RcsMarketCategorySetRelationService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author :  myname
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.wrapper.impl
 * @Description :  TODO
 * @Date: 2019-09-11 15:09
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Service
public class RcsMarketCategorySetRelationServiceImp extends ServiceImpl<RcsMarketCategorySetRelationMapper, RcsMarketCategorySetRelation> implements RcsMarketCategorySetRelationService {

    @Override
    public boolean addOrUpdateCategorySetCategory(List<RcsMarketCategorySetRelation> relationList) {
        boolean bool = false;
        List<RcsMarketCategorySetRelation> saveList = new ArrayList<>();
        List<RcsMarketCategorySetRelation> updateList = new ArrayList<>();

        List<RcsMarketCategorySetRelation> isExist = this.list();

        //玩法有传入id为更新，不传id为新增
        for (RcsMarketCategorySetRelation relation : relationList) {
            if (null != relation.getId()) {
                updateList.add(relation);
            } else {
                saveList.add(relation);
            }
        }
        //新增和更新分开处理
        if (saveList.size() > 0) {
            List<RcsMarketCategorySetRelation> saveListNew = new ArrayList<>();
            saveListNew.addAll(saveList);
            for (RcsMarketCategorySetRelation sav : saveListNew) {
                isExist.forEach(exist -> {
                    if (sav.getMarketCategoryId().longValue() == exist.getMarketCategoryId().longValue() && sav.getMarketCategorySetId().longValue() == exist.getMarketCategorySetId()) {
                        saveList.remove(sav);
                    }
                });

                if (saveList.size() < 1) {
                    break;
                }
            }

            if (saveList.size() > 0) {
                saveOrUpdateBatch(saveList);
                bool = true;
            }
        }
        if (updateList.size() > 0) {
            saveOrUpdateBatch(updateList);
            bool = true;
        }
        //return saveOrUpdateBatch(relationList);
        return true;
    }

    /**
     * 批量删除玩法集里的玩法
     *
     * @param id
     * @return
     */
    @Override
    public boolean deleteCategorySetContent(List<Long> id) {
        return this.removeByIds(id);
    }

    @Override
    public List<Long> getCategoryIdByCategorySetId(Long marketCategorySetId) {
        return returnList(this.baseMapper.getCategoryIdByCategorySetId(marketCategorySetId));
    }

    @Override
    public List<Long> getPlayIdByPlaySetCode(String playSetCode) {
        return this.baseMapper.getPlayIdByPlaySetCode(playSetCode);
    }

    @Override
    public List<CategorySetOrderNo> getCategorySetAndCategoryOrderNo() {
        return returnList(this.baseMapper.getCategorySetOrderNo());
    }

    @Override
    public int isContainPlayFromPlaySet(List<Long> ids, Long sportId, Integer type, Long id) {
        if(CollectionUtils.isEmpty(ids)){return 0;}
        return this.baseMapper.isContainPlayFromPlaySet(ids,sportId,type,id);
    }

    private <E> List<E> returnList(List<E> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Lists.newArrayList();
        }
        return list;
    }

}
