package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.RcsMerchantCommonConfig;
/**
 * 商户动态风控全局开关配置
 *
 * @description:
 * @author: magic
 * @create: 2022-05-15 11:15
 **/
public interface IRcsMerchantCommonConfigService extends IService<RcsMerchantCommonConfig> {


    /**
     * 修改配置
     * @param rcsMerchantCommonConfig 配置参数
     * @param traderId 操作人
     */
    void update(RcsMerchantCommonConfig rcsMerchantCommonConfig,int traderId);
}
