package com.panda.sport.rcs.trade.controller;

import com.panda.sport.rcs.trade.wrapper.RcsTournamentOperateMarketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author :  Sean
 * @Project Name :  panda-rcs
 * @Package Name :  com.panda.sport.rcs.trade.controller
 * @Description :  滚球操盘
 * @Date: 2020-01-11 16:00
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Slf4j
@RestController
@RequestMapping(value = "grounder")
public class GrounderTraderController {

    @Autowired
    RcsTournamentOperateMarketService rcsTournamentOperateMarketService;

//    @RequestMapping(value = "updateMarketWaterHeadConfig")
//    @LogAnnotion(name = "设置水差", keys = {"playId", "matchId", "marketId", "playOptionsId", "oddsType", "dataSource", "fixDirectionEnum", "marketType", "autoBetStop", "maxOdds", "minOdds",
//        "homeLevelFirstOddsRate", "homeAutoChangeRate", "awayAutoChangeRate", "tieAutoChangeRate", "switchAutoChangeRate", "tournamentId", "playPhaseType", "rollType", "diffOdds", "nameExpressionValue"},
//        title = {"玩法ID", "赛事id", "盘口id", "投注类型ID", "盘口类型", "是否使用数据源", "调价策略", "操盘类型", "是否自动封盘", "最大赔率", "最小赔率", "主一级赔率变化率", "主队水差",
//            "客队水差", "和局水差", "自动水差开关", "联赛id", "玩法阶段", "玩法阶段对应玩法种类", "玩法阶段对应玩法种类", "玩法阶段对应玩法种类"})
//    public HttpResponse saveAndUpdateMarketWaterHeadConfig(@RequestBody List<ThreewayOverLoadTriggerItem> list){
//        if (CollectionUtils.isEmpty(list)){
//            return HttpResponse.success("盘口配置不能为空");
//        }
//
//        try {
//            rcsTournamentOperateMarketService.saveAndUpdateMarketWaterHeadConfig(list);
//            return HttpResponse.success("盘口设置成功");
//        }catch (RcsServiceException e){
//            log.error("一个盘口设置了多个投注项"  + e.getMessage(), e);
//            return HttpResponse.error(HttpResponse.FAIL,e.getMessage());
//        }catch (Exception e){
//            log.error("更新滚球水位差设置失败"  + e.getMessage(), e);
//        }
//        return HttpResponse.error(HttpResponse.FAIL,"更新滚球水位差设置失败额，请稍后重试");
//    }


//    @RequestMapping(value = "queryMarketWaterHeadConfig")
//    public HttpResponse queryMarketWaterHeadConfig(ThreewayOverLoadTriggerItem rcsMatchMarketConfig){
//        if (ObjectUtils.isEmpty(rcsMatchMarketConfig) ||
//                ObjectUtils.isEmpty(rcsMatchMarketConfig.getMatchId()) ||
//                ObjectUtils.isEmpty(rcsMatchMarketConfig.getPlayPhaseType())){
//            return HttpResponse.success("Match Manage Id not be blank");
//        }
//        try {
//            Map<Integer,List<Map<String,Object>>> traderMatchList = rcsTournamentOperateMarketService.queryMarketWaterHeadConfig(rcsMatchMarketConfig);
//            return HttpResponse.success(traderMatchList);
//        }catch (Exception e){
//            log.error("滚球水位差查询失败"  + e.getMessage(), e);
//        }
//        return HttpResponse.fail("查询滚球水位差设置失败额，请稍后重试");
//    }
    /*@RequestMapping(value = "queryMarketWaterHeadConfig")
    public HttpResponse queryMarketWaterHeadConfig(@RequestBody Map<String,Object> map){
        if (ObjectUtils.isEmpty(map) ||
                ObjectUtils.isEmpty(map.get("matchManageId")) ||
                ObjectUtils.isEmpty(map.get("playPhaseType"))){
            return HttpResponse.success("Match Manage Id not be blank");
        }
        try {
            Map<Integer,List<Map<String,Object>>> traderMatchList = rcsTournamentOperateMarketService.queryMarketWaterHeadConfigNew(map);
            return HttpResponse.success(traderMatchList);
        }catch (Exception e){
            log.error("滚球水位差查询失败"  + e.getMessage(), e);
        }
        return HttpResponse.fail("查询滚球水位差设置失败额，请稍后重试");
    }*/
}
