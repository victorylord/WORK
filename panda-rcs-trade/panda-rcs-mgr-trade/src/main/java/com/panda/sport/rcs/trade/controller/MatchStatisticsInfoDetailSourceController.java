package com.panda.sport.rcs.trade.controller;

import com.panda.sport.rcs.mapper.MatchStatisticsInfoDetailSourceMapper;
import com.panda.sport.rcs.pojo.MatchStatisticsInfoDetailSource;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.MatchStatisticsInfoDetailSourceService;
import com.panda.sport.rcs.vo.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
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
 * @Description :  获取赛事的几个比分
 * @Date: 2020-09-25 13:55
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@RequestMapping("/MatchStatisticsInfoDetailSource")
@RestController
@Slf4j
public class MatchStatisticsInfoDetailSourceController {
    @Autowired
    private MatchStatisticsInfoDetailSourceMapper matchStatisticsInfoDetailSourceMapper;
    @Autowired
    private MatchStatisticsInfoDetailSourceService matchStatisticsInfoDetailSourceService;

    @RequestMapping(value = "/getList",method = RequestMethod.GET)
    public HttpResponse<HashMap<String,MatchStatisticsInfoDetailSource>> getList(Long matchId){
        try {
            Map<String, Object> columnMap = new HashMap<>();
            columnMap.put("standard_match_id", matchId);
            columnMap.put("code", "match_score");
            List<MatchStatisticsInfoDetailSource> matchStatisticsInfoDetailSourceList = matchStatisticsInfoDetailSourceMapper.selectByMap(columnMap);
            Map<String,MatchStatisticsInfoDetailSource> stringMatchStatisticsInfoDetailSourceMap=new HashMap<>();
            if (!CollectionUtils.isEmpty(matchStatisticsInfoDetailSourceList)){
                for (MatchStatisticsInfoDetailSource matchStatisticsInfoDetailSource:matchStatisticsInfoDetailSourceList){
                stringMatchStatisticsInfoDetailSourceMap.put(matchStatisticsInfoDetailSource.getDataSourceCode(),matchStatisticsInfoDetailSource);
                }
            }
            return HttpResponse.success(stringMatchStatisticsInfoDetailSourceMap);
        }catch (Exception e){
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(-1,"服务器执行错误");
        }
    }


    /**
     * 得到三方比分
     * @param matchId
     * @return
     */
    @RequestMapping(value = "/getThirdMatchScoreList",method = RequestMethod.GET)
    public HttpResponse<Map<String,List<MatchStatisticsInfoDetailSource>>> getThirdMatchScoreList(Long matchId,String dataSourceCode){
        try {
            List<MatchStatisticsInfoDetailSource> list = matchStatisticsInfoDetailSourceService.getThirdMatchScoreList(matchId,dataSourceCode);
            return HttpResponse.success(list);
        }catch (Exception e){
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(-1,"服务器执行错误");
        }
    }
}
