package com.panda.sport.rcs.trade.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.pojo.RcsUserConfigExt;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.vo.*;
import com.panda.sport.rcs.trade.wrapper.IRcsUserConfigExtService;
import com.panda.sport.rcs.utils.TradeUserUtils;
import com.panda.sport.rcs.vo.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

/**
 * 用户特殊配置扩展
 *
 * @description:
 * @author: magic
 * @create: 2022-05-14 18:15
 **/
@Component
@RestController
@RequestMapping(value = "/rcsUserConfigExt")
@Slf4j
public class RcsUserConfigExtController {


    @Autowired
    IRcsUserConfigExtService rcsUserConfigExtService;

    /**
     * 修改用户赔率分组动态风控开关 默认为开
     * 需求：1782
     * http://lan-confluence.sportxxxr1pub.com/pages/viewpage.action?pageId=63015150
     *
     * @param rcsUserConfigExtReqVo
     * @return HttpResponse
     * @Author magic
     * @Date 2022/05/14
     */
    @RequestMapping(value = "/updateTagMarketLevelStatus", method = RequestMethod.POST)
    private HttpResponse updateTagMarketLevelStatus(@RequestBody RcsUserConfigExtReqVo rcsUserConfigExtReqVo) {
        try {
            if (rcsUserConfigExtReqVo.getTagMarketLevelStatus() == null) {
                log.error("::{}::赔率分组动态风控开关不能为空", CommonUtil.getRequestId());
                return HttpResponse.failToMsg("赔率分组动态风控开关不能为空");
            }
            if (rcsUserConfigExtReqVo.getUserId() == null) {
                log.error("::{}::用户id不能为空", CommonUtil.getRequestId());
                return HttpResponse.failToMsg("用户id不能为空");
            }
            Integer traderId = TradeUserUtils.getUserId();
            rcsUserConfigExtService.saveTagMarketLevelStatus(rcsUserConfigExtReqVo, traderId);
            return HttpResponse.success();
        } catch (RcsServiceException e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg(e.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg("服务器错误");
        }
    }

    /**
     * 修改用户赔率分组动态风控开关 默认为开
     * 需求：1782
     * http://lan-confluence.sportxxxr1pub.com/pages/viewpage.action?pageId=63015150
     *
     * @param userId 用戶id
     * @return HttpResponse
     * @Author magic
     * @Date 2022/05/14
     */
    @RequestMapping(value = "/get", method = RequestMethod.GET)
    private HttpResponse get(Long userId) {
        try {
            if (userId == null) {
                log.error("::{}::用戶id不能为空", CommonUtil.getRequestId());
                return HttpResponse.failToMsg("用戶id不能为空");
            }
            RcsUserConfigExt rcsUserConfigExt = rcsUserConfigExtService.getOne(new LambdaQueryWrapper<RcsUserConfigExt>().eq(RcsUserConfigExt::getUserId, userId));
            if (rcsUserConfigExt == null) {
                //默认为开
                rcsUserConfigExt = new RcsUserConfigExt();
                rcsUserConfigExt.setUserId(userId);
                rcsUserConfigExt.setTagMarketLevelStatus(1);
            }
            return HttpResponse.success(rcsUserConfigExt);
        } catch (RcsServiceException e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg(e.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg("服务器错误");
        }
    }

    /**
     * 批量修改商户下用户赔率分组动态风控开关
     * 需求：1782
     * http://lan-confluence.sportxxxr1pub.com/pages/viewpage.action?pageId=63015150
     *
     * @param rcsMerchantUserTagMarketLevelStatusReqVo
     * @return HttpResponse
     * @Author magic
     * @Date 2022/05/14
     */
    @RequestMapping(value = "/batchUpdateTagMarketLevelStatus", method = RequestMethod.POST)
    private HttpResponse batchUpdateTagMarketLevelStatus(@RequestBody RcsMerchantUserTagMarketLevelStatusReqVo rcsMerchantUserTagMarketLevelStatusReqVo) {
        try {
            if (rcsMerchantUserTagMarketLevelStatusReqVo.getTagMarketLevelStatus() == null) {
                log.error("::{}::赔率分组动态风控开关不能为空", CommonUtil.getRequestId());
                return HttpResponse.failToMsg("赔率分组动态风控开关不能为空");
            }
            if (rcsMerchantUserTagMarketLevelStatusReqVo.getMerchantCode() == null) {
                log.error("::{}::商户编码不能为空", CommonUtil.getRequestId());
                return HttpResponse.failToMsg("商户编码不能为空");
            }
            if (rcsMerchantUserTagMarketLevelStatusReqVo.getPercentageLimit() == null) {
                log.error("::{}::用户限额百分比不能为空", CommonUtil.getRequestId());
                return HttpResponse.failToMsg("用户限额百分比不能为空");
            }
            Integer traderId = TradeUserUtils.getUserId();
            int num = rcsUserConfigExtService.batchSaveTagMarketLevelStatus(rcsMerchantUserTagMarketLevelStatusReqVo, traderId);
            return HttpResponse.success(num);
        } catch (RcsServiceException e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg(e.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg("服务器错误");
        }
    }
}
