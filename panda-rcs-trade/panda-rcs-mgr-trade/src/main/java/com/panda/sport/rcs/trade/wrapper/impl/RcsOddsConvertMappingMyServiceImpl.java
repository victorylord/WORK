package com.panda.sport.rcs.trade.wrapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.constants.BaseConstants;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.mapper.RcsOddsConvertMappingMyMapper;
import com.panda.sport.rcs.pojo.RcsOddsConvertMappingMy;
import com.panda.sport.rcs.trade.util.MarginUtils;
import com.panda.sport.rcs.trade.wrapper.RcsOddsConvertMappingMyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author :  myname
 * @Project Name :  daima1
 * @Package Name :  com.panda.sport.rcs.trade.wrapper.impl
 * @Description :  TODO
 * @Date: 2019-12-27 18:00
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Service
@Slf4j
public class RcsOddsConvertMappingMyServiceImpl extends ServiceImpl<RcsOddsConvertMappingMyMapper, RcsOddsConvertMappingMy> implements RcsOddsConvertMappingMyService {
    private HashMap<String, RcsOddsConvertMappingMy> hashMap = new HashMap<>();
    @Autowired
    private RcsOddsConvertMappingMyMapper rcsOddsConvertMappingMyMapper;

    @Override
    public String listRcsOddsConvertMappingMy(String oddsValue) {
        //数据初始化
        double v = Double.parseDouble(oddsValue);
        if (v > 100 || v < -100) {
            BigDecimal divide = new BigDecimal(oddsValue).divide(new BigDecimal(BaseConstants.MULTIPLE_VALUE), 2, BigDecimal.ROUND_DOWN);
            oddsValue = divide.toPlainString();
        }
        oddsValue = new BigDecimal(oddsValue).setScale(2).toPlainString();

        oddsValue = MarginUtils.checkMyOdds(new BigDecimal(oddsValue)).toPlainString();

        if (CollectionUtils.isEmpty(hashMap)) {
            Map<String, Object> columnMap = new HashMap<>();
            List<RcsOddsConvertMappingMy> rcsOddsConvertMappingMIES = rcsOddsConvertMappingMyMapper.selectByMap(columnMap);
            for (RcsOddsConvertMappingMy rcsOddsConvertMappingMy : rcsOddsConvertMappingMIES) {
                hashMap.put(rcsOddsConvertMappingMy.getMalaysia(), rcsOddsConvertMappingMy);
            }
        }
        RcsOddsConvertMappingMy rcsOddsConvertMappingMy = hashMap.get(oddsValue);
        if (rcsOddsConvertMappingMy == null) {
            log.error("马来转欧赔出问题");
            throw new RcsServiceException("马来赔率设置错误，该马来赔率不能转换为欧赔");
        } else {
            return rcsOddsConvertMappingMy.getEurope();
        }

    }
}
