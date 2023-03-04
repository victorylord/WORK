package com.panda.sport.rcs.trade.wrapper.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.panda.sport.rcs.common.DateUtils;
import com.panda.sport.rcs.common.NumberUtils;
import com.panda.sport.rcs.pojo.RcsCode;
import com.panda.sport.rcs.trade.wrapper.RcsCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.mapper.RcsBusinessConPlayConfigMapper;
import com.panda.sport.rcs.pojo.RcsBusinessConPlayConfig;
import com.panda.sport.rcs.trade.wrapper.RcsBusinessConPlayConfigService;

/**
 * @author :  Administrator
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.wrapper.impl
 * @Description :  TODO
 * @Date: 2019-11-22 15:04
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */

@Service
public class RcsBusinessConPlayConfigServiceImpl extends ServiceImpl<RcsBusinessConPlayConfigMapper, RcsBusinessConPlayConfig> implements RcsBusinessConPlayConfigService {

    @Autowired
    RcsBusinessConPlayConfigService businessConPlayConfigService;

    @Autowired
    RcsCodeService rcsCodeService;

    @Override
    public List<RcsBusinessConPlayConfig> selectConPlays(Long businessId) {
        QueryWrapper<RcsBusinessConPlayConfig> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(RcsBusinessConPlayConfig::getBusinessId, businessId);
        List<RcsBusinessConPlayConfig> rcsBusinessConPlayConfigs = businessConPlayConfigService.list(wrapper);
        if (rcsBusinessConPlayConfigs.size() <= 0) {
            Map map = new HashMap<>();
            map.put("father_key", "rcsBusinessConPlay");
            List<RcsCode> rcsCodeList = rcsCodeService.getRcsCodeList(map);
            if (rcsCodeList.size() > 0) {
                rcsCodeList.stream().forEach(code -> {
                    RcsBusinessConPlayConfig businessConPlayConfig = new RcsBusinessConPlayConfig();
                    businessConPlayConfig.setBusinessId(businessId);
                    businessConPlayConfig.setPlayType(Integer.parseInt(code.getChildKey()));
                    businessConPlayConfig.setStatus(1);
                    businessConPlayConfig.setPlayValue(NumberUtils.getBigDecimal(code.getValue()));
                    businessConPlayConfig.setPlayRate(NumberUtils.getBigDecimal(100));
                    businessConPlayConfigService.save(businessConPlayConfig);
                });
            }
            rcsBusinessConPlayConfigs = businessConPlayConfigService.list(wrapper);
        }
        return rcsBusinessConPlayConfigs;
    }

    @Override
    public boolean updateConPlayConfig(List<RcsBusinessConPlayConfig> conPlayConfigs) {
        conPlayConfigs.stream().forEach(model->{
            Long value = rcsCodeService.getRcsCodeList("rcsBusinessConPlay", String.valueOf(model.getPlayType()));
            BigDecimal playValue = NumberUtils.getBigDecimal(value).multiply(model.getPlayRate()).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP);
            model.setPlayValue(playValue);
        });

        return businessConPlayConfigService.updateBatchById(conPlayConfigs);
    }
}


