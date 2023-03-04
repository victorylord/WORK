package com.panda.sport.rcs.trade.controller;


import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.panda.sport.rcs.trade.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.panda.sport.rcs.pojo.RcsOddsConvertMappingMy;
import com.panda.sport.rcs.trade.wrapper.RcsOddsConvertMappingMyService;
import com.panda.sport.rcs.trade.wrapper.RcsOddsConvertMappingService;
import com.panda.sport.rcs.vo.HttpResponse;
import com.panda.sport.rcs.vo.SportMarketCategoryVo;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author admin
 * @since 2019-10-04
 */
@Controller
@RequestMapping("/rcsOddsConvertMapping")
@Slf4j
public class RcsOddsConvertMappingController {

    @Autowired
    private RcsOddsConvertMappingService rcsOddsConvertMappingService;
    
    @Autowired
    private RcsOddsConvertMappingMyService rcsOddsConvertMappingMyService;

    /**
     * 获取赔率转换映射表数据
     * @return
     */
    @GetMapping("/list")
    @ResponseBody
    public HttpResponse<Map<String, Map<String, String>>> listRcsOddsConvertMapping() {
        Map<String, Map<String, String>> mapRcsOddsConvertMapping = Collections.emptyMap();
        try {
            mapRcsOddsConvertMapping = rcsOddsConvertMappingService.listRcsOddsConvertMapping();
            
            Map<String, Map<String, String>> tempMap = new HashMap<String, Map<String,String>>(mapRcsOddsConvertMapping.size());
            for(String key : mapRcsOddsConvertMapping.keySet()) {
            	if(Double.parseDouble(String.valueOf(mapRcsOddsConvertMapping.get(key).get("EU"))) > 51
            			&&  Double.parseDouble(String.valueOf(mapRcsOddsConvertMapping.get(key).get("EU"))) < 101) {
            		continue;
            	}
            	
            	mapRcsOddsConvertMapping.get(key).remove("EU");
            	tempMap.put(key + "_", mapRcsOddsConvertMapping.get(key));
            }
            
            return HttpResponse.success(tempMap);
        } catch (Exception e) {
            log.error("::{}::execute find rcsOddsConvertMapping list exception{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
        return HttpResponse.success(mapRcsOddsConvertMapping);
    }
    

    /**
     * 根据欧赔获取马来赔，通过马来转欧赔数据表
     * @param ids
     * @return
     */
    @GetMapping("/mapper/euToMy")
    @ResponseBody
    public HttpResponse<Map<String, Map<String, String>>> euToMy() {
    	Map<String, RcsOddsConvertMappingMy> mapRcsOddsConvertMapping = new HashMap<String, RcsOddsConvertMappingMy>();
        try {
        	List<RcsOddsConvertMappingMy> list = rcsOddsConvertMappingMyService.list();
        	list.forEach(bean -> {
        		mapRcsOddsConvertMapping.put(String.valueOf(bean.getEurope()), bean);
        	});
            return HttpResponse.success(mapRcsOddsConvertMapping);
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
        return HttpResponse.success(mapRcsOddsConvertMapping);
    }

    /**
     * 根据玩法配置，获取关联的盘口类型
     * @param ids
     * @return
     */
    @GetMapping("/sportMarketCategory")
    @ResponseBody
    public HttpResponse<List<SportMarketCategoryVo>> listStandardSportMarketCategory(@RequestParam("ids") String ids) {
        List<SportMarketCategoryVo> sportMarketCategoryVoList = Collections.emptyList();
        try {
            sportMarketCategoryVoList = rcsOddsConvertMappingService.listStandardSportMarketCategory(ids);
        } catch (Exception e) {
            log.error("::{}::{}execute find listStandardSportMarketCategory list exception:",CommonUtil.getRequestId(),e.getMessage(), e);
        }
        return HttpResponse.success(sportMarketCategoryVoList);
    }
}

