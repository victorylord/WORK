package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsMarketCategorySetMargin;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author :  Felix
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.mapper
 * @Description :  TODO
 * @Date: 2019-10-04 17:10
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Repository
public interface RcsMarketCategorySetMarginMapper extends BaseMapper<RcsMarketCategorySetMargin> {
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
    boolean updateMargin(List<Long> marginId);
}
