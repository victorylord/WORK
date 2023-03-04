package com.panda.sport.rcs.trade.controller;

import com.panda.sport.rcs.mapper.RcsCodeMapper;
import com.panda.sport.rcs.pojo.RcsCode;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.RcsCodeService;
import com.panda.sport.rcs.vo.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author :  myname
 * @Project Name :  xindaima
 * @Package Name :  com.panda.sport.rcs.trade.controller
 * @Description :  TODO
 * @Date: 2020-08-25 9:30
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@RestController
@Slf4j
@RequestMapping("/rcsCode")
public class RcsCodeController {

    @Autowired
    private RcsCodeMapper rcsCodeMapper;
    @RequestMapping(value = "/getList",method = RequestMethod.GET)
    public HttpResponse<List<RcsCode>> getList( RcsCode rcsCode){
        try {
            if (rcsCode.getFatherKey() == null || "".equals(rcsCode.getFatherKey())) {
                return HttpResponse.fail("一级目录不能为空");
            }
            Map<String, Object> columnMap = new HashMap<>();
            columnMap.put("father_key", rcsCode.getFatherKey());
            if (rcsCode.getChildKey() != null && !"".equals(rcsCode.getChildKey())) {
                columnMap.put("child_key", rcsCode.getChildKey());
            }
            columnMap.put("status", 1);
            List<RcsCode> rcsCodeList = rcsCodeMapper.selectByMap(columnMap);
            return HttpResponse.success(rcsCodeList);
        }catch (Exception e){
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(-1,"服务器出现问题");
        }
    }
}
