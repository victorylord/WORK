package com.panda.sport.rcs.trade.wrapper.impl;

import com.alibaba.fastjson.JSON;
import com.panda.sport.data.rcs.dto.MarketCategoryCetBean;
import com.panda.sport.rcs.pojo.RcsMarketCategorySet;
import com.panda.sport.rcs.trade.wrapper.DataSyncService;
import com.panda.sport.rcs.trade.wrapper.MarketCategorySetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author :  Felix
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.task.wrapper.impl
 * @Description :  TODO
 * @Date: 2019-10-03 15:16
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */

@Service("marketCategorySetService")
@Slf4j
public class MarketCategorySetSyncServiceImpl implements DataSyncService<Long> {
    @Autowired
    MarketCategorySetService marketCategorySetService;
    @Override
    public Map<String, String> receive(Long data) {
    	MarketCategoryCetBean bean = new MarketCategoryCetBean();
    	bean.setSportId(1l);//默认足球
    	bean.setSetId(data);
        List<RcsMarketCategorySet> list= marketCategorySetService.findCategorySetSyncList(bean);
        String categorySetList=JSON.toJSON(list).toString();
        Map<String, String> resultMap=new HashMap<>(1);
        resultMap.put("categorySetList",categorySetList);
        return resultMap;
    }
}
