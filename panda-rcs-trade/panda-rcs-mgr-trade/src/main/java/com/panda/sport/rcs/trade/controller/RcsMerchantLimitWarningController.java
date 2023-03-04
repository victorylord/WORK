package com.panda.sport.rcs.trade.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.panda.sport.rcs.mapper.RcsMerchantLimitWarningMapper;
import com.panda.sport.rcs.pojo.RcsMerchantLimitWarning;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.util.DateUtil;
import com.panda.sport.rcs.trade.vo.RcsMerchantLimitWarningVo;
import com.panda.sport.rcs.vo.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

/**
 * @program: xindaima
 * @description:
 * @author: kimi  商户限额预警
 * @create: 2021-03-06 17:39
 **/
@Slf4j
@RestController
@RequestMapping("/rcsMerchantLimitWarning")
public class RcsMerchantLimitWarningController {
    @Autowired
    private RcsMerchantLimitWarningMapper rcsMerchantLimitWarningMapper;
    @RequestMapping(value = "getList",method = RequestMethod.GET)
    private HttpResponse  getList(@RequestParam(required = false) Integer pageNum, @RequestParam(required = false) Integer pageSize,@RequestParam(required = false) Long time){
        try {
            if (pageNum == null) {
                pageNum = 1;
            }

            if (pageSize == null) {
                pageSize = 30;
            }
            IPage<RcsMerchantLimitWarning> iPage = new Page(pageNum, pageSize);
            IPage<RcsMerchantLimitWarning> rcsMerchantLimitWarningIPage = rcsMerchantLimitWarningMapper.selectByPage(iPage, time);
            Integer currentDayCount = rcsMerchantLimitWarningMapper.getCurrentDayCount();
            RcsMerchantLimitWarningVo rcsMerchantLimitWarningVo = new RcsMerchantLimitWarningVo();
            rcsMerchantLimitWarningVo.setTotal(rcsMerchantLimitWarningIPage.getTotal());
            rcsMerchantLimitWarningVo.setCurrentDayCount(currentDayCount);
            rcsMerchantLimitWarningVo.setPageNum(pageNum);
            rcsMerchantLimitWarningVo.setPageSize(pageSize);
            rcsMerchantLimitWarningVo.setRcsMerchantLimitWarningList(rcsMerchantLimitWarningIPage.getRecords());
            rcsMerchantLimitWarningVo.setPages(rcsMerchantLimitWarningIPage.getPages());
            return HttpResponse.success(rcsMerchantLimitWarningVo);
        }catch (Exception e){
			log.error("::{}::服务器错误{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg("服务器错误");
        }
    }
}
