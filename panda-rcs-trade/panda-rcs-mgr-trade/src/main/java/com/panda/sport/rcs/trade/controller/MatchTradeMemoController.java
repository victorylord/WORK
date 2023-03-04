package com.panda.sport.rcs.trade.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.panda.sport.rcs.log.annotion.LogAnnotion;
import com.panda.sport.rcs.pojo.RcsMatchTradeMemo;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.vo.MatchTradeMemoDetailVo;
import com.panda.sport.rcs.trade.wrapper.RcsMatchTradeMemoService;
import com.panda.sport.rcs.utils.TradeUserUtils;
import com.panda.sport.rcs.vo.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Description   赛事操盘备忘录
 * @Param
 * @Author  Rriben
 * @Date  13:58 2021/2/3
 * @return
 **/
@RestController
@RequestMapping(value = "/matchTradeMemo")
@Slf4j
public class MatchTradeMemoController {

    @Autowired
    RcsMatchTradeMemoService matchTradeMemoService;

    @PostMapping("/save")
    @LogAnnotion(name = "赛事操盘备忘录保存",
    keys = {"standardMatchId", "operateStage", "traderId","traderName", "text",
            "tournamentName","home","away","homeScore","awayScore"},
    title = {"赛事ID", "操盘阶段", "操盘手id","操盘手名称","备忘录内容",
            "联赛名称","主场队名称","客场队名称","主队比分","客队比分" })
    public HttpResponse<String> saveMemo(@RequestBody RcsMatchTradeMemo matchTradeMemo){
        log.info("::{}::赛事操盘备忘录保存:{},操盘手:{}",CommonUtil.getRequestId(matchTradeMemo.getId()), JSONObject.toJSONString(matchTradeMemo), TradeUserUtils.getUserIdNoException());
        if(!matchTradeMemoService.saveMemo(matchTradeMemo)){
            HttpResponse.fail("新增赛事备忘录保存失败，赛事id：" + matchTradeMemo.getStandardMatchId());
        }
        return HttpResponse.success();
    }

    @PostMapping("/list")
    @LogAnnotion(name = "获取赛事操盘备忘录列表",
            keys = {"standardMatchId", "traderId", "operateStage", "text", "startTime", "endTime"},
            title = { "赛事ID", "操盘手id", "操盘阶段", "备忘录内容", "查询时间开始时间戳", "查询时间结束时间戳"})
    public HttpResponse<IPage<RcsMatchTradeMemo>> list(@RequestBody RcsMatchTradeMemo matchTradeMemo){
        log.info("::{}::matchTradeMemo---根据条件查询备忘录列表",CommonUtil.getRequestId());
        if(matchTradeMemo.getStandardMatchId() == null){
            return HttpResponse.fail("查询备忘录列表，标准赛事ID不能为空！");
        }
        IPage<RcsMatchTradeMemo> memoPage = matchTradeMemoService.getMemoPage(matchTradeMemo, true);
        return HttpResponse.success(memoPage);
    }


    @PostMapping("/memoDetail")
    @LogAnnotion(name = "获取赛事操盘备忘录列表",
            keys = {"id", "standardMatchId", "traderId", "traderName", "id"},
            title = {"备忘录ID", "操盘手id", "操盘手名称", "备忘录Id"})
    public HttpResponse<MatchTradeMemoDetailVo> getMemoDetail(@RequestBody RcsMatchTradeMemo matchTradeMemo){
        log.info("::{}::matchTradeMemo---查询备忘录明细列表",CommonUtil.getRequestId());
        if(StringUtils.isBlank(matchTradeMemo.getId()) && matchTradeMemo.getStandardMatchId() == null){
            return HttpResponse.fail("查询备忘录明细，备忘录ID及标准赛事ID不能同时为空！");
        }
        MatchTradeMemoDetailVo memoDetail = matchTradeMemoService.getMemoDetail(matchTradeMemo);
        if(memoDetail == null){
            return HttpResponse.fail("未获取到备忘录详情！");
        }
        return HttpResponse.success(memoDetail);
    }


}
