package com.panda.sport.rcs.trade.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.panda.sport.rcs.common.DateUtils;
import com.panda.sport.rcs.common.NumberUtils;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.BaseRcsOrderService;
import com.panda.sport.rcs.trade.wrapper.RcsLanguageInternationService;
import com.panda.sport.rcs.utils.TradeUserUtils;
import com.panda.sport.rcs.vo.BaseRcsOrderStatisticTimeVo;
import com.panda.sport.rcs.vo.BaseRcsOrderVo;
import com.panda.sport.rcs.vo.ConditionVo;
import com.panda.sport.rcs.vo.HttpResponse;
import com.panda.sport.rcs.vo.TimeBeanVo;
import com.panda.sport.rcs.vo.TournamentResultVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static com.panda.sport.rcs.common.DateUtils.addDate;

/**
 * @author :  Administrator
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.controller
 * @Description :  TODO
 * @Date: 2019-12-24 16:33
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Component
@RestController
@RequestMapping(value = "report")
@Slf4j
public class ReportController {

    @Autowired
    private RcsLanguageInternationService rcsLanguageInternationService;

    @Autowired
    private BaseRcsOrderService baseRcsOrderService;

    @RequestMapping(value = "/getplayList", method = RequestMethod.GET)
    public HttpResponse<ConditionVo> getplayList(@RequestParam(required = false) List<Long> sportIds, @RequestParam(required = false) List<Long> playSetIds) throws Exception {

        String lang = TradeUserUtils.getLang();
        List<ConditionVo> playList = rcsLanguageInternationService.getMarketCategoryList(sportIds, playSetIds,lang);
        return HttpResponse.success(playList);

    }

    @RequestMapping(value = "/getTournamentList", method = RequestMethod.GET)
    public HttpResponse<TournamentResultVo> getTournamentList(Long sportId) {
        List<TournamentResultVo> tournamentList = rcsLanguageInternationService.getTournamentList(sportId);
        return HttpResponse.success(tournamentList);
    }

    @RequestMapping(value = "/getTotalMatchReport", method = RequestMethod.POST)
    public HttpResponse<BaseRcsOrderStatisticTimeVo> getTotalMatchReport(@RequestBody BaseRcsOrderVo base) {

        if (base.getJsonTimes() == null) {
            return HttpResponse.fail("时间不能为空");
        }
        if (base.getSettleTimeType() == null) {
            return HttpResponse.fail("时间类型设置不能为空");
        }
        if (base.getOrderStatuses() != null && base.getOrderStatuses().size() <= 0) {
            return HttpResponse.fail("受注量设置不能为空");
        }
        try {
            List<TimeBeanVo> startToEnd = getStartToEnd(base.getJsonTimes());
            base.setTimeBeanVoList(startToEnd);
        } catch (Exception e) {
			log.error("::{}::时间格式错误", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail("时间格式错误");
        }
        BaseRcsOrderStatisticTimeVo baseRcsOrderStatisticTimeVo = null;
        try {
            baseRcsOrderStatisticTimeVo = baseRcsOrderService.selectSumBaseOrders(base);
        } catch (Exception e) {
			log.error("::{}::汇总查询失败", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail("汇总查询失败");
        }
        return HttpResponse.success(baseRcsOrderStatisticTimeVo);
    }

    @RequestMapping(value = "/getMatchReport", method = RequestMethod.POST)
    public HttpResponse<IPage<BaseRcsOrderStatisticTimeVo>> getMatchReport(@RequestBody BaseRcsOrderVo base) {
        if (base.getJsonTimes() == null) {
            return HttpResponse.fail("时间不能为空");
        }
        if (base.getSettleTimeType() == null) {
            return HttpResponse.fail("时间类型设置不能为空");
        }
        if (base.getOrderStatuses() != null && base.getOrderStatuses().size() <= 0) {
            return HttpResponse.fail("受注量设置不能为空");
        }
        if (NumberUtils.getBigDecimal(base.getPageNo()).compareTo(BigDecimal.ZERO) <= 0 ||
                NumberUtils.getBigDecimal(base.getPageSize()).compareTo(BigDecimal.ZERO) <= 0) {
            base.setPageNo(1);
            base.setPageSize(10);
        }
        try {
            List<TimeBeanVo> startToEnd = getStartToEnd(base.getJsonTimes());
            base.setTimeBeanVoList(startToEnd);
        } catch (Exception e) {
			log.error("::{}::时间格式错误", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail("时间格式错误");
        }
        IPage<BaseRcsOrderStatisticTimeVo> baseRcsOrderStatisticTimeVoIPage = null;
        try {
            baseRcsOrderStatisticTimeVoIPage = baseRcsOrderService.selectBaseOrders(new Page<>(base.getPageNo(), base.getPageSize()), base);
        } catch (Exception e) {
			log.error("::{}::查询失败", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail("查询失败");
        }
        return HttpResponse.success(baseRcsOrderStatisticTimeVoIPage);
    }

    @RequestMapping(value = "/getFinanceReport", method = RequestMethod.GET)
    public HttpResponse<BaseRcsOrderStatisticTimeVo> getFinanceReport() {
        return HttpResponse.success();

    }

    /**
     * 根据具体年份周数获取日期范围
     *
     * @param jsonObject
     * @return
     */
    public static List<TimeBeanVo> getStartToEnd(JSONObject jsonObject) {
        String type = jsonObject.getString("type");
        List<TimeBeanVo> jsonObjects = new ArrayList<>();
        JSONArray objects = jsonObject.getJSONArray("list");
        if ("y".equals(type)) {
            for (Object object : objects) {
                JSONObject o = JSONObject.parseObject(JSONObject.toJSONString(object));
                String startTime = o.getString("y") + "-01-01";
                String endTime = o.getString("y") + "-12-31";
                TimeBeanVo vo = new TimeBeanVo(startTime, endTime, o.getString("y"));
                jsonObjects.add(vo);
            }
        } else if ("p".equals(type)) {
            for (Object object : objects) {
                JSONObject o = JSONObject.parseObject(JSONObject.toJSONString(object));
                String baseTime = o.getString("y") + "-01-01";
                Long p = Long.parseLong(o.getString("p"));
                String startTime = "";
                String endTime = "";
                try {
                    startTime = addDate(baseTime, (p - 1) * 28L);
                    endTime = addDate(baseTime, p * 28L);
                } catch (ParseException e) {
                    log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
                    e.printStackTrace();
                }
                TimeBeanVo vo = new TimeBeanVo(startTime, endTime, o.getString("y") + " 第" + p + "期");
                jsonObjects.add(vo);
            }
        } else if ("w".equals(type)) {
            for (Object object : objects) {
                JSONObject o = JSONObject.parseObject(JSONObject.toJSONString(object));
                String baseTime = o.getString("y") + "-01-01";
                Long p = Long.parseLong(o.getString("p"));
                JSONArray weeks = o.getJSONArray("w");
                for (Object obj : weeks) {
                    String startTime = "";
                    String endTime = "";
                    Long w = Long.parseLong(String.valueOf(obj));
                    try {
                        startTime = addDate(baseTime, (p - 1) * 28L + (w - 1) * 7);
                        endTime = addDate(baseTime, (p - 1) * 28L + w * 7);
                    } catch (ParseException e) {
                        log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
                        e.printStackTrace();
                    }
                    TimeBeanVo vo = new TimeBeanVo(startTime, endTime, o.getString("y") + " 第" + p + "期  第" + w + "周");
                    jsonObjects.add(vo);
                }
            }
        } else {
            for (Object object : objects) {
                JSONObject o = JSONObject.parseObject(JSONObject.toJSONString(object));
                String startTime = o.getString("startTime");
                String endTime = o.getString("endTime");
                try {
                    endTime = DateUtils.addDate(endTime, 1);
                } catch (ParseException e) {
                    log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
                    e.printStackTrace();
                }
                TimeBeanVo vo = new TimeBeanVo(startTime, endTime, "");
                jsonObjects.add(vo);
            }
        }
        return jsonObjects;
    }
}
