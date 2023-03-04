package com.panda.sport.rcs.trade.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.panda.sport.rcs.log.annotion.LogAnnotion;
import com.panda.sport.rcs.trade.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.panda.sport.rcs.pojo.RcsMatchCollection;
import com.panda.sport.rcs.trade.wrapper.RcsMatchCollectionService;
import com.panda.sport.rcs.utils.TradeUserUtils;
import com.panda.sport.rcs.vo.HttpResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * @author :  myname
 * @Project Name :  daima1
 * @Package Name :  com.panda.sport.rcs.bootstrap.controller
 * @Description :  我的收藏操作
 * @Date: 2019-10-25 14:44
 * @ModificationHistory Who    When    What
 */
@Slf4j
@RestController
@RequestMapping("/collection")
public class RcsMatchCollectionController {
    @Autowired
    private RcsMatchCollectionService rcsMatchCollectionService;

    /**
     * @return com.panda.sport.rcs.vo.HttpResponse<java.util.List < com.panda.sport.rcs.pojo.RcsMatchCollection>>
     * @Description //查询我的收藏
     * @Param []
     * @Author kimi
     * @Date 2019/10/28
     **/
    @RequestMapping(value = "/getRcsMatchCollection", method = RequestMethod.GET)
    public HttpResponse<List<RcsMatchCollection>> getRcsMatchCollection(Long sportId) throws Exception {
        Integer userId = TradeUserUtils.getUserId();
        Map<String, Object> columnMap = new HashMap<>();
        columnMap.put("sport_id", sportId);
        columnMap.put("user_id",userId);
        List<RcsMatchCollection> rcsMatchCollections;
        try {
            rcsMatchCollections = rcsMatchCollectionService.selectByMap(columnMap);
        } catch (Exception e) {
            log.error("::{}::风控服务器出问题{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail("风控服务器出问题" + ":" + e.getMessage());
        }
        return HttpResponse.success(rcsMatchCollections);
    }
    
    
    @RequestMapping(value = "/updateCollect", method = RequestMethod.POST)
    @LogAnnotion(name = "更新我的收藏", keys = {"tournamentId", "matchId", "userId", "sportId", "type", "status", "matchType"},
        title = {"联赛ID", "赛事ID", "玩家id", "体育种类id", "1为赛事  2为联赛", "0取消收藏 1添加收藏", "类型"})
    public HttpResponse<Boolean> updateCollect(@RequestBody RcsMatchCollection rcsMatchCollection) throws Exception {
        log.info("::{}::更新我的收藏:{},操盘手:{}",CommonUtil.getRequestId(rcsMatchCollection.getMatchId()), JSONObject.toJSONString(rcsMatchCollection), TradeUserUtils.getUserIdNoException());
        boolean result = false;
        Integer userId = TradeUserUtils.getUserId();
        rcsMatchCollection.setUserId(userId.longValue());
        try {
            result = rcsMatchCollectionService.updateRcsMatchCollection(rcsMatchCollection);
        } catch (Exception e) {
            log.error("::{}::风控服务器出问题{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail("服务器异常" + ":" + e.getMessage());
        }
        return HttpResponse.success(result);
    }
}
