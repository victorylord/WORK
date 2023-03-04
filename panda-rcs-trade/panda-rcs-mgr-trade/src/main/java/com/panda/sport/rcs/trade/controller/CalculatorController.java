package com.panda.sport.rcs.trade.controller;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.pojo.RcsFirstMarket;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.RcsFirstMarketService;
import com.panda.sport.rcs.vo.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 计算器相关
 */
@RestController
@RequestMapping(value = "/calculator")
@Slf4j
public class CalculatorController {

    @Autowired
    RcsFirstMarketService rcsFirstMarketService;

    /**
     * 得到赛事终盘value
     * @param dto
     * @return
     */
    @RequestMapping(value = "/getPreEndMarketValue", method = RequestMethod.POST)
    public HttpResponse getPreEndMarketValue(@RequestBody StandardMatchInfo dto) {
        try {
            if(null==dto||null==dto.getId()){
                return new HttpResponse(HttpResponse.FAIL,"参数为空");
            }
            Map map=rcsFirstMarketService.getPreEndMarketValue(dto);
            return HttpResponse.success(map);
        } catch (RcsServiceException e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL,e.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL,"风控服务器出问题");
        }
    }

    /**
     * 设定终盘赛事终盘value
     * @param list
     * @return
     */
    @RequestMapping(value = "/setEndMarketValue", method = RequestMethod.POST)
    public HttpResponse setEndMarketValue(@RequestBody List<RcsFirstMarket> list) {
        try {
            if(CollectionUtils.isEmpty(list)){
                return new HttpResponse(HttpResponse.FAIL,"参数为空");
            }
            int i=rcsFirstMarketService.batchInsertOrUpdateEndMarket(list);
            return HttpResponse.success();
        } catch (RcsServiceException e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL,e.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return new HttpResponse(HttpResponse.FAIL,"风控服务器出问题");
        }
    }
}
