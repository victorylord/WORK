package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.RcsUserConfig;
import com.panda.sport.rcs.trade.vo.RcsUserConfigVo;
import com.panda.sport.rcs.trade.vo.RcsUserSpecialBetLimitConfigVo;
import com.panda.sport.rcs.trade.vo.RcsUserSpecialBetLimitConfigsVo;
import com.panda.sport.rcs.vo.HttpResponse;

import java.util.HashMap;
import java.util.List;

public interface RcsUserConfigService extends IService<RcsUserConfig> {
    /**
     *
     * @param userId
     * @return
     */
    RcsUserSpecialBetLimitConfigVo getList(Long userId);

    /**
     *
     * @param userIdList
     * @return
     */
    HashMap<Long, RcsUserConfigVo> getRcsUserConfigVo(List<Long> userIdList);

    /**
     *
     * @param rcsUserSpecialBetLimitConfigsVo
     * @param traderId
     * @return
     */
    HttpResponse updateRcsUserSpecialBetLimitConfigsVo(RcsUserSpecialBetLimitConfigsVo rcsUserSpecialBetLimitConfigsVo,Integer traderId, boolean isTrade);
}
