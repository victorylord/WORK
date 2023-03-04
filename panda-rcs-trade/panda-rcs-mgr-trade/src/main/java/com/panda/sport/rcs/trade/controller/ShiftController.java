package com.panda.sport.rcs.trade.controller;

import com.panda.sport.rcs.pojo.TOrder;
import com.panda.sport.rcs.pojo.dto.ShiftDto;
import com.panda.sport.rcs.pojo.vo.ShiftGroupVo;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.RcsShiftService;
import com.panda.sport.rcs.trade.wrapper.RcsSysUserService;
import com.panda.sport.rcs.vo.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.utils.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 换班
 */
@Component
@RestController
@RequestMapping(value = "shift")
@Slf4j
public class ShiftController {

    @Autowired
    RcsShiftService rcsShiftService;
    @Autowired
    RcsSysUserService rcsSysUserService;

/*    @RequestMapping(value = "/noDesignateUserList",method = RequestMethod.POST)
    public HttpResponse<TOrder> getList(@RequestBody ShiftDto shiftDto){
        try {
            List<String> orderNoList = tOrderDTO.getOrderNoList();
            if (CollectionUtils.isEmpty(orderNoList)) {
                return HttpResponse.success();
            }
            //List<TOrderDetailVo> tOrders = tOrderMapper.selectTOrderByOrderNoList(orderNoList);
            return HttpResponse.success(tOrders);
        }catch (Exception e){
            log.error(e.getMessage(),e);
            return HttpResponse.error(-1,"服务器出问题");
        }
    }*/

    @RequestMapping(value = "/updateShiftList",method = RequestMethod.POST)
    public HttpResponse<TOrder> getList(@RequestBody ShiftDto shiftDto){
        try {
            rcsShiftService.updateShiftList(shiftDto);
            return HttpResponse.success();
        }catch (Exception e){
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(-1,"服务器出问题");
        }
    }

    @RequestMapping(value = "/shiftUserList",method = RequestMethod.POST)
    public HttpResponse shiftUserList(@RequestBody ShiftDto shiftDto){
        try {
            Map map = rcsShiftService.shiftUserList(shiftDto);
            return HttpResponse.success(map);
        }catch (Exception e){
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(-1,"服务器出问题");
        }
    }

    @RequestMapping(value = "/shiftUserGroupList",method = RequestMethod.POST)
    public HttpResponse shiftUserGroupList(@RequestBody ShiftDto shiftDto,@RequestHeader("lang") String lang){
        try {
            Assert.notNull(lang, "国际化lang不能为空");
            List<ShiftGroupVo> list = rcsShiftService.shiftUserGroupList(shiftDto,lang);
            return HttpResponse.success(list);
        }catch (Exception e){
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(-1,"服务器出问题");
        }
    }

    /**
     * 用户名联想
     * @param shiftDto
     * @return
     */
    @RequestMapping(value = "/associatingUserName",method = RequestMethod.POST)
    public HttpResponse associatingUserName(@RequestBody ShiftDto shiftDto){
        try {
            String userName = shiftDto.getUserName();
            if (StringUtils.isBlank(userName)) {
                return HttpResponse.success();
            }
           List<String> names =  rcsSysUserService.associatingUserName (userName);
            return HttpResponse.success(names);
        }catch (Exception e){
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(-1,"服务器出问题");
        }
    }

}
