package com.panda.sport.rcs.trade.controller;

import com.panda.sport.rcs.mapper.RcsResourceMapper;
import com.panda.sport.rcs.pojo.RcsResource;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.vo.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author :  kimi
 * @Project Name :  rcs-parent
 * @Package Name :
 * @Description :  权限管理接口
 * @Date: 2019-10-07 16:40
 */
@RestController
@RequestMapping(value = "/resourceManager")
@Slf4j
public class ResourceManagerController {
    @Autowired
    private RcsResourceMapper rcsResourceMapper;

    @RequestMapping(value = "/getList", method = RequestMethod.GET)
    public HttpResponse<List<RcsResource>> getList(int id) {
        List<RcsResource> resourceList;
        try {
            resourceList = rcsResourceMapper.getResourceList(id);
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail("数据库操作出问题");
        }
        if (resourceList.size() == 0) {
            return null;
        }
        return HttpResponse.success(resourceList);
    }
}
