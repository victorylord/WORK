package com.panda.sport.rcs.trade.controller;

import com.alibaba.fastjson.JSONObject;
import com.panda.sport.rcs.enums.OperateLogEnum;
import com.panda.sport.rcs.log.annotion.OperateLog;
import com.panda.sport.rcs.pojo.tourTemplate.AoParameterTemplateReq;
import com.panda.sport.rcs.trade.service.AoParameterTemplateService;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.utils.TradeUserUtils;
import com.panda.sport.rcs.vo.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.utils.Assert;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * @author :  koala
 * @Project Name :  panda-rcs-trade-group
 * @Package Name :  com.panda.sport.rcs.trade.controller
 * @Description :  联赛（早滚模版）/赛事模版 - 新增AO Parameter
 * @Date: 2022-03-05 14:51
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */

@RestController
@RequestMapping(value = "/aoParameterTemplate")
@Slf4j
@Component
public class AoParameterTemplateController {

    private final AoParameterTemplateService aoParameterTemplateServiceImpl;

    public AoParameterTemplateController(AoParameterTemplateService aoParameterTemplateServiceImpl) {
        this.aoParameterTemplateServiceImpl = aoParameterTemplateServiceImpl;
    }

    /**
     * 修改po模板
     *
     * @param request
     * @return
     */
    @PostMapping("/updateAoParameterTemplate")
    @OperateLog(operateType = OperateLogEnum.OPERATE_SETTING)
    public HttpResponse updateAoParameterTemplate(@RequestBody AoParameterTemplateReq request) {
        try {
            log.info("::{}::更新赛事AO数据:{}，操盘手:{}",CommonUtil.getRequestId(request.getMatchId()), JSONObject.toJSONString(request), TradeUserUtils.getUserIdNoException());
            Assert.notNull(request.getMatchId(), "赛事id不能为空");
            Assert.notNull(request.getMatchType(), "赛事状态不能为空");
            Assert.notNull(request.getSportId(), "赛事赛种不能为空");
            Assert.notNull(request.getTemplateId(), "模板id不能为空");
            log.info("::{}::更新赛事AO数据:{}",CommonUtil.getRequestId(request.getMatchId()),JSONObject.toJSONString(request));
            aoParameterTemplateServiceImpl.updateAoParameterTemplate(request);
        } catch (Exception e) {
            log.error("::{}::修改ao数据源出问题:{}", CommonUtil.getRequestId(request.getMatchId()), e.getMessage(), e);
            return HttpResponse.error(-1, "修改ao数据源出问题");
        }
        return HttpResponse.success();
    }


}
