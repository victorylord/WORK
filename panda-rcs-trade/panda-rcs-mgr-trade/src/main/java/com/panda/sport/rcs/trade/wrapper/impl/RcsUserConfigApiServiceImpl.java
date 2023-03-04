package com.panda.sport.rcs.trade.wrapper.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.panda.sport.data.rcs.api.RcsUserConfigApiService;
import com.panda.sport.data.rcs.dto.special.RcsUserConfigVo;
import com.panda.sport.data.rcs.dto.special.RcsUserSpecialBetLimitConfigVo;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.RcsUserConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;

/**
 * @program: xindaima
 * @description:
 * @author: kimi
 * @create: 2021-02-05 14:57
 **/
@Slf4j
@Service
public class RcsUserConfigApiServiceImpl implements RcsUserConfigApiService {
    @Autowired
    private RcsUserConfigService rcsUserConfigService;
    @Override
    public RcsUserSpecialBetLimitConfigVo getList(Long userId) {
        try {
        	com.panda.sport.rcs.trade.vo.RcsUserSpecialBetLimitConfigVo vo = rcsUserConfigService.getList(userId);
        	return JSONObject.parseObject(JSONObject.toJSONString(vo),RcsUserSpecialBetLimitConfigVo.class);
        }catch (Exception e){
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return null;
        }
    }

    @Override
    public HashMap<Long, RcsUserConfigVo> getRcsUserConfigVo(List<Long> userIdList) {
        try {
        	HashMap<Long, com.panda.sport.rcs.trade.vo.RcsUserConfigVo> map = rcsUserConfigService.getRcsUserConfigVo(userIdList);
        	return JSONObject.parseObject(JSONObject.toJSONString(map),new TypeReference<HashMap<Long, RcsUserConfigVo>>(){});
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return null;
        }
    }
}
