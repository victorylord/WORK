package com.panda.sport.rcs.trade.wrapper;

import com.panda.sport.rcs.pojo.RcsMarketCategorySetMargin;

import java.util.List;

/**
 * @author :  Felix
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.wrapper
 * @Description :  TODO
 * @Date: 2019-10-04 16:58
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface RcsMarketCategorySetMarginService {

    /**
     * 根据玩法集ID 查询抽水
     * @param categorySetId
     * @return
     */
    List<RcsMarketCategorySetMargin> findMargin(List<Long> categorySetId);

    /**
     * 根据玩法集ID 查询抽水
     * @param categorySetId
     * @return
     */
    List<RcsMarketCategorySetMargin> findMargin(Long categorySetId);
    /**
     * 根据MarginId修改抽水值
     * @param marginId
     * @return
     */
    boolean updateMargin(List<RcsMarketCategorySetMargin> marginId);

    /**
     * 批量删除margin
     * @param marginId
     * @return
     */
    boolean deleteMargin(List<Long> marginId);

    /**
     * 批量新增抽水值
     * @param margin
     * @return
     */
    boolean addMargin(List<RcsMarketCategorySetMargin> margin);
}
