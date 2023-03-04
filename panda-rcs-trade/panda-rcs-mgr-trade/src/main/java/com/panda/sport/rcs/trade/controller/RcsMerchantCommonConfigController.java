package com.panda.sport.rcs.trade.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.pojo.RcsMerchantCommonConfig;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.pojo.constants.TradeConstant;
import com.panda.sport.rcs.trade.wrapper.IRcsMerchantCommonConfigService;
import com.panda.sport.rcs.utils.StringUtils;
import com.panda.sport.rcs.utils.TradeUserUtils;
import com.panda.sport.rcs.vo.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * 商户动态风控全局开关配置
 * @description:
 * @author: magic
 * @create: 2022-05-15 11:15
 **/
@Component
@RestController
@RequestMapping(value = "/rcsMerchantCommonConfig")
@Slf4j
public class RcsMerchantCommonConfigController {

    @Autowired
    IRcsMerchantCommonConfigService rcsMerchantCommonConfigService;
    @Autowired
    private RedisClient redisClient;
    /**
     * 商户动态风控全局开关配置获取
     * 需求：1782
     * http://lan-confluence.sportxxxr1pub.com/pages/viewpage.action?pageId=63015150
     *
     * @return HttpResponse
     * @Author magic
     * @Date 2022/05/17
     */
    @RequestMapping(value = "/get", method = RequestMethod.GET)
    private HttpResponse get() {
        try {
            RcsMerchantCommonConfig rcsMerchantCommonConfig = rcsMerchantCommonConfigService.getOne(new LambdaQueryWrapper<>());
            if (rcsMerchantCommonConfig == null) {
                return HttpResponse.failToMsg("商户动态风控全局开关配置未初始化");
            }
            if (org.apache.commons.lang3.StringUtils.isNotBlank(rcsMerchantCommonConfig.getSportIds())){
                List<String> sportIdList = Arrays.asList(rcsMerchantCommonConfig.getSportIds().split(","));
                rcsMerchantCommonConfig.setSportIdList(sportIdList);
            }
            return HttpResponse.success(rcsMerchantCommonConfig);
        } catch (RcsServiceException e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg(e.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg("服务器错误");
        }
    }

    /**
     * 商户动态风控全局开关配置修改
     * 需求：1782
     * http://lan-confluence.sportxxxr1pub.com/pages/viewpage.action?pageId=63015150
     *
     * @param rcsMerchantCommonConfig
     * @return HttpResponse
     * @Author magic
     * @Date 2022/05/15
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    private HttpResponse update(@RequestBody RcsMerchantCommonConfig rcsMerchantCommonConfig) {
        try {
            if (rcsMerchantCommonConfig.getTagMarketLevelStatus() == null) {
                log.error("::{}::赔率分组动态风控开关不能为空", CommonUtil.getRequestId());
                return HttpResponse.failToMsg("赔率分组动态风控开关不能为空");
            }
            if (rcsMerchantCommonConfig.getPreSettlementStatus() == null) {
                log.error("::{}::提前结算动态风控开关不能为空", CommonUtil.getRequestId());
                return HttpResponse.failToMsg("提前结算动态风控开关不能为空");
            }
            if (rcsMerchantCommonConfig.getBetDelayStatus() == null) {
                log.error("::{}::投注延时动态风控开关不能为空", CommonUtil.getRequestId());
                return HttpResponse.failToMsg("投注延时动态风控开关不能为空");
            }
            if (rcsMerchantCommonConfig.getBetLimitStatus() == null) {
                log.error("::{}::投注限额动态风控开关不能为空", CommonUtil.getRequestId());
                return HttpResponse.failToMsg("投注限额动态风控开关不能为空");
            }
            if (rcsMerchantCommonConfig.getBetVolumeStatus() == null){
                log.error("投注货量动态风控开关不能为空");
                return HttpResponse.failToMsg("投注货量动态风控开关不能为空");
            }
            if (rcsMerchantCommonConfig.getSportIdList() != null && rcsMerchantCommonConfig.getSportIdList().size() > 0){
                String sportIds = StringUtils.join(rcsMerchantCommonConfig.getSportIdList(),",");
                rcsMerchantCommonConfig.setSportIds(sportIds);
                redisClient.hSet(TradeConstant.RCS_BET_VOLUME_CONFIG,"sportIds",sportIds);
            }
            redisClient.hSet(TradeConstant.RCS_BET_VOLUME_CONFIG,"status",rcsMerchantCommonConfig.getBetVolumeStatus().toString());
            Integer traderId = TradeUserUtils.getUserId();
            rcsMerchantCommonConfigService.update(rcsMerchantCommonConfig, traderId);
            return HttpResponse.success();
        } catch (RcsServiceException e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg(e.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg("服务器错误");
        }
    }
}
