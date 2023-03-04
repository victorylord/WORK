package com.panda.sport.rcs.trade.controller;

import com.alibaba.fastjson.JSONObject;
import com.panda.sport.rcs.pojo.odds.BalanceReqVo;
import com.panda.sport.rcs.trade.service.BalanceService;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.vo.MarketBalanceVo;
import com.panda.sport.rcs.utils.TradeUserUtils;
import com.panda.sport.rcs.vo.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 平衡值
 * @Author : Paca
 * @Date : 2021-02-11 11:36
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Slf4j
@Component
@RestController
@RequestMapping(value = "/balance")
public class BalanceController {

    @Autowired
    private BalanceService balanceService;

    /**
     * 查询平衡值
     *
     * @param reqVo
     * @return
     */
    @PostMapping("/query")
    public HttpResponse queryBalance(@RequestBody BalanceReqVo reqVo) {
        MarketBalanceVo result = balanceService.queryBalance(reqVo);
        return HttpResponse.success(result);
    }

    /**
     * 清零平衡值
     *
     * @param reqVo
     * @return
     */
    @PostMapping("/clear")
    public HttpResponse clearBalance(@RequestBody BalanceReqVo reqVo) {
        log.info("::{}::清零平衡值:{}，操盘手:{}", CommonUtil.getRequestId(reqVo.getMatchId(),reqVo.getPlayId()), JSONObject.toJSONString(reqVo), TradeUserUtils.getUserIdNoException());
        String result = balanceService.clearBalance(reqVo);
        return HttpResponse.success(result);
    }
}
