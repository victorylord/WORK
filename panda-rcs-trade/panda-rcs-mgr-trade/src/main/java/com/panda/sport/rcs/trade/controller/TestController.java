package com.panda.sport.rcs.trade.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.google.common.collect.Lists;
import com.panda.sport.data.rcs.api.Request;
import com.panda.sport.data.rcs.api.Response;
import com.panda.sport.data.rcs.dto.MarketCategoryCetBean;
import com.panda.sport.rcs.constants.BaseConstants;
import com.panda.sport.rcs.constants.RedisKeys;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.trade.log.LogContext;
import com.panda.sport.rcs.log.annotion.format.LogFormatAnnotion;
import com.panda.sport.rcs.trade.log.format.LogFormatBean;
import com.panda.sport.rcs.trade.log.format.LogFormatPublicBean;
import com.panda.sport.rcs.pojo.RcsMarketCategorySet;
import com.panda.sport.rcs.pojo.test.RcsLogFormatTest;
import com.panda.sport.rcs.trade.wrapper.DataSyncService;
import com.panda.sport.rcs.trade.wrapper.MarketCategorySetService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.controller
 * @Description :  TODO
 * @Date: 2020-05-14 10:59
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@RestController
@Slf4j
@RequestMapping("/test")
public class TestController {
    @Resource(name = "marketCategorySetService")
    DataSyncService dataSyncService;

    @Autowired
    MarketCategorySetService marketCategorySetService;

    @Autowired
    private RedisClient redisClient;

    @PostMapping("/test")
    public Response putSportMarketCategorySet(@RequestBody Request<MarketCategoryCetBean> requestParam) {
        if (requestParam.getData() == null) {
            return Response.success(null);
        }
        if (requestParam.getData().getSportId() == null) {
            requestParam.getData().setSportId(1L);
        }
        String cacheKey = String.format(RedisKeys.RCSCACHE_MARKET_CATEGORY_SET, requestParam.getData().getSportId());
        String obj = redisClient.get(cacheKey);
        List<RcsMarketCategorySet> playSet = JsonFormatUtils.fromJsonArray(obj, RcsMarketCategorySet.class);
        if (CollectionUtils.isEmpty(playSet)) {
            playSet = marketCategorySetService.findCategorySetSyncList(requestParam.getData());
            //写入缓存
            redisClient.setExpiry(cacheKey, JsonFormatUtils.toJson(playSet), BaseConstants.CACHE_EXPIRE_SECONDS);
        }

        RcsMarketCategorySet rcsMarketCategorySet = new RcsMarketCategorySet();
        rcsMarketCategorySet.setId(new Long(0));
//        rcsMarketCategorySet.setMarketName("所有投注");
        List<RcsMarketCategorySet> playSetlist = Lists.newArrayList();
        playSetlist.add(rcsMarketCategorySet);
        if (CollectionUtils.isNotEmpty(playSet)) {
            for (RcsMarketCategorySet se : playSet) {
//                se.setMarketName(se.getName());
                playSetlist.add(se);
            }
        }

    	/*if(requestParam.getData() == null){
    	    return Response.success(null);
        }
    	if(requestParam.getData().getSportId() == null){
    	    requestParam.getData().setSportId(1L);
        }
        List<RcsMarketCategorySet> playSet = marketCategorySetService.findCategorySetSyncList(requestParam.getData());
        List<RcsMarketCategorySet> playSetlist = new ArrayList<>();
        RcsMarketCategorySet rcsMarketCategorySet = new RcsMarketCategorySet();
        rcsMarketCategorySet.setId(new Long(0));
        rcsMarketCategorySet.setMarketName("所有投注");
        playSetlist.add(rcsMarketCategorySet);
        if (CollectionUtils.isNotEmpty(playSet)) {
            for (RcsMarketCategorySet se : playSet) {
                se.setMarketName(se.getName());
                playSetlist.add(se);
            }
        }*/
        return Response.success(playSetlist);
    }
    
    
    /**
    * @Title: testGetLog 
    * @Description: TODO 
    * @param @param requestParam
    * @param @return    设定文件 
    * @return Response    返回类型 
    * @throws
     */
    @PostMapping("/testGetLog")
    @LogFormatAnnotion
    public Response testGetLog(@RequestBody RcsLogFormatTest requestParam) {
    	LogFormatPublicBean publicBean = new LogFormatPublicBean("Test_Type", "测试接口日志", requestParam.getId());
    	
    	Map<String, Object> dynamicBean = new HashMap<String, Object>();
    	dynamicBean.put("click_case", "触发条件：点击弹窗");
    	
    	RcsLogFormatTest formatTest = new RcsLogFormatTest("test", "red", "147258369", "20", null);
        LogContext.getContext().addFormatBean(publicBean, dynamicBean, requestParam, formatTest);
        
        //LogContext.getContext().addFormatBean(publicBean, dynamicBean, new LogFormatBean("身高", requestParam.getHeight(), "height：1"));
    	
        return Response.success();
    }
    
    public static void main(String[] args) {
    	RcsLogFormatTest formatTest = new RcsLogFormatTest("test", "red", "147258369", "20", null);
    	System.out.println(JSONObject.toJSONString(formatTest));
	}
}
