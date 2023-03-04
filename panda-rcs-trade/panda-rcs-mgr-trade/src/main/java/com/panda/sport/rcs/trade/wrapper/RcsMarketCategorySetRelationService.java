package com.panda.sport.rcs.trade.wrapper;

import com.panda.sport.rcs.mongo.CategorySetOrderNo;
import com.panda.sport.rcs.pojo.RcsMarketCategorySetRelation;

import java.util.List;

/**
 * @author :  myname
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.wrapper
 * @Description :  TODO
 * @Date: 2019-09-11 15:04
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface RcsMarketCategorySetRelationService {

    /**
     * 批量往玩法集里添加或修改玩法
     *
     * @param relationList
     * @return
     */
    boolean addOrUpdateCategorySetCategory(List<RcsMarketCategorySetRelation> relationList);

    /**
     * 批量删除玩法集里的玩法
     *
     * @return
     */
    boolean deleteCategorySetContent(List<Long> id);

    /**
     * 通过玩法集ID查询所有下级玩法ID
     *
     * @param marketCategorySetId
     * @return
     */
    List<Long> getCategoryIdByCategorySetId(Long marketCategorySetId);

    /**
     * 通过玩法集编码获取玩法ID
     *
     * @param playSetCode
     * @return
     */
    List<Long> getPlayIdByPlaySetCode(String playSetCode);

    /**
     * 获取玩法集和玩法排序号
     *
     * @return
     */
    List<CategorySetOrderNo> getCategorySetAndCategoryOrderNo();

    int isContainPlayFromPlaySet(List<Long> ids, Long sportId, Integer type, Long id);
}
