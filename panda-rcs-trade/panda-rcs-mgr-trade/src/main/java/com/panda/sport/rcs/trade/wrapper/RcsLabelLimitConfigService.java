package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.RcsLabelLimitConfig;
import com.panda.sport.rcs.vo.HttpResponse;
import com.panda.sport.rcs.vo.RcsLabelLimitConfigVo;

import java.util.List;

public interface RcsLabelLimitConfigService extends IService<RcsLabelLimitConfig> {
    /**
     *
     * @param rcsLabelLimitConfigVoList
     * @param tradeId
     * @return
     */
    HttpResponse updateRcsLabelLimitConfigVo(List<RcsLabelLimitConfigVo> rcsLabelLimitConfigVoList,Integer tradeId);
}
