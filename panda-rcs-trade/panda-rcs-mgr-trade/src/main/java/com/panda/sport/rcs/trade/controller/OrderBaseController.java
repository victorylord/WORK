package com.panda.sport.rcs.trade.controller;

import com.panda.sport.rcs.mapper.TOrderMapper;
import com.panda.sport.rcs.pojo.TOrder;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.vo.TOrderDTO;
import com.panda.sport.rcs.vo.HttpResponse;
import com.panda.sport.rcs.vo.TOrderDetailVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @program: xindaima
 * @description:查询订单基础数据
 * @author: kimi
 * @create: 2020-11-26 16:43
 **/
@RestController
@RequestMapping(value = "/orderBase")
@Slf4j
@Component
public class OrderBaseController {
    @Autowired
    private TOrderMapper  tOrderMapper;
    @RequestMapping(value = "/getList",method = RequestMethod.POST)
    public HttpResponse<TOrder> getList(@RequestBody TOrderDTO tOrderDTO){
        try {
            List<String> orderNoList = tOrderDTO.getOrderNoList();
            if (CollectionUtils.isEmpty(orderNoList)) {
                return HttpResponse.success();
            }
            List<TOrderDetailVo > tOrders = tOrderMapper.selectTOrderByOrderNoList(orderNoList);
            return HttpResponse.success(tOrders);
        }catch (Exception e){
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(-1,"服务器出问题");
        }
    }
}
