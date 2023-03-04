package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.StandardSportOddsFieldsTemplet;

/**
 * <p>
 * 标准玩法投注项表 服务类
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-09-03
 */
public interface StandardSportOddsFieldsTempletService extends IService<StandardSportOddsFieldsTemplet> {

    /**
     * 缓存玩法投注项模板数据，需要重新缓存的会用当前数据库数据覆盖原缓存
     *
     * @param reCached 重新缓存：true，否则false；
     */
    void cachedOddsFieldsTemplet(boolean reCached);

    /**
     * 根据ID获取缓存中的玩法投注项模板，不存在则从数据库获取并缓存
     *
     * @param id
     * @return
     */
    StandardSportOddsFieldsTemplet getCachedOddsFieldsTempletById(Long id);
}
