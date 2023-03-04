package com.panda.sport.rcs.trade.wrapper.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.mapper.RcsMerchantCommonConfigMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.RcsMerchantCommonConfig;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.IRcsMerchantCommonConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 商户动态风控全局开关配置
 *
 * @description:
 * @author: magic
 * @create: 2022-05-15 11:15
 **/
@Slf4j
@Service
public class RcsMerchantCommonConfigServiceImpl extends ServiceImpl<RcsMerchantCommonConfigMapper, RcsMerchantCommonConfig> implements IRcsMerchantCommonConfigService {

    @Autowired
    ProducerSendMessageUtils sendMessage;

    @Override
    @Transactional
    public void update(RcsMerchantCommonConfig rcsMerchantCommonConfig, int traderId) {
        RcsMerchantCommonConfig o = baseMapper.selectOne(new LambdaQueryWrapper<>());
        if (o == null) {
            throw new RcsServiceException("商户动态风控全局开关配置未初始化");
        }
        log.info("::{}::操作员：{}，修改商户动态风控全局开关配置:{},历史配置:{}", CommonUtil.getRequestId(), traderId, JSONObject.toJSONString(rcsMerchantCommonConfig), JSONObject.toJSONString(o));
        o.setBetLimitStatus(rcsMerchantCommonConfig.getBetLimitStatus());
        o.setTagMarketLevelStatus(rcsMerchantCommonConfig.getTagMarketLevelStatus());
        o.setPreSettlementStatus(rcsMerchantCommonConfig.getPreSettlementStatus());
        o.setBetDelayStatus(rcsMerchantCommonConfig.getBetDelayStatus());
        o.setBetVolumeStatus(rcsMerchantCommonConfig.getBetVolumeStatus());
        o.setSportIds(rcsMerchantCommonConfig.getSportIds());
        o.setUpdateTime(new Date());
        baseMapper.updateById(o);
        sendMerchantCommonConfig(o);
    }


    /**
     * 给业务发生mq
     *
     * @param rcsMerchantCommonConfig
     */
    private void sendMerchantCommonConfig(RcsMerchantCommonConfig rcsMerchantCommonConfig) {
        if (rcsMerchantCommonConfig!=null) {
            sendMessage.sendMessage("rcs_merchant_common_config", rcsMerchantCommonConfig);
        }
    }
}
