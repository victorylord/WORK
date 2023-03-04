package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.StandardSportMarketCategory;
import com.panda.sport.rcs.vo.CategoryTemplateVo;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标准玩法表 服务类
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-09-03
 */
public interface StandardSportMarketCategoryService extends IService<StandardSportMarketCategory> {

    /**
     * 根据ID获取缓存中的玩法，不存在则从数据库获取并缓存
     *
     * @param id
     * @return
     */
    StandardSportMarketCategory getCachedMarketCategoryById(Long id);

    /**
     * 根据玩法ID查找对应玩法阶段
     *
     * @param categoryId
     * @return
     */
    Integer selectPlayPhase(Long categoryId);

    List<Integer> selectCategoryIds(List<Integer> playPhases);

    /**
     * 查找玩法模板，task项目方法
     *
     * @param sportId
     * @param categoryId
     * @return
     */
    CategoryTemplateVo queryCategoryTemplate(Long sportId, Long categoryId);

    /**
     * 获取玩法模板
     *
     * @param sportId
     * @param categoryIds
     * @return
     */
    Map<Long, CategoryTemplateVo> getCategoryTemplateByCache(Long sportId, List<Long> categoryIds);

    /**
     * 获取所有玩法模板ID
     *
     * @param sportId
     * @return
     */
    Map<Long, Integer> getAllCategoryTemplateId(Long sportId);

    StandardSportMarketCategory getCachedMarketCategoryById(Integer sportId, Long id);

}
