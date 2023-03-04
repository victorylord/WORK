package com.panda.sport.rcs.trade.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.impl.MatchForecastService;
import com.panda.sport.rcs.utils.TradeUserUtils;
import com.panda.sport.rcs.vo.HttpResponse;
import com.panda.sport.rcs.vo.statistics.RcsPredictBetStatisVo;
import com.panda.sport.rcs.vo.statistics.RcsPredictForecastVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description   //赛事预测服务接口
 * @Param 
 * @Author  Sean
 * @Date  14:15 2020/7/21
 * @return 
 **/
@RestController
@RequestMapping(value = "/forecast")
@Slf4j
public class MatchForecastController {
    @Autowired
    private MatchForecastService matchForecastService;
    /**
     * @Description   根据玩法，查询赛事forecast、货量分布 、货量分布（带基准分）
     * @Param [RcsPredictForecastVo]
     * @Author  Sean
     * @Date  14:18 2020/7/21
     * @return com.panda.sport.rcs.vo.HttpResponse<java.lang.Integer>
     **/
    @RequestMapping(value = "/playForecast", method = RequestMethod.GET)
    public HttpResponse<Map<String,Object>> queryPlayForecast(RcsPredictForecastVo vo) {
        try {
            Assert.notNull(vo.getSportId(),"运动种类SportId不能为空");
            Assert.notNull(vo.getMatchId(),"赛事MatchId不能为空");
            if (NumberUtils.INTEGER_ONE.intValue() != vo.getSportId()){
                return new HttpResponse(HttpResponse.SUCCESS,"暂时只支持足球");
            }

            Map<String,Object> map = matchForecastService.queryPlayForecast(vo);
            if (MapUtils.isEmpty(map)){
                return new HttpResponse(HttpResponse.SUCCESS,"无赛事数据或者赛事不存在");
            }
            return HttpResponse.success(map);
        } catch (IllegalArgumentException e){
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail(e.getMessage());
        } catch (RcsServiceException e){
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail(e.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail("风控服务器出问题");
        }
    }
    /**
     * @Description   查询订单期货量分布
     * @Param [RcsPredictBetStatisVo]
     * @Author  Sean
     * @Date  16:41 2020/7/21
     * @return com.panda.sport.rcs.vo.HttpResponse<java.lang.Integer>
     **/
    @RequestMapping(value = "/queryFuturesVolume", method = RequestMethod.GET)
    public HttpResponse<Map<String,Object>> queryFuturesVolume(RcsPredictBetStatisVo vo) {
        try {
            Assert.notNull(vo.getSportId(),"运动种类SportId不能为空");
            Assert.notNull(vo.getMatchId(),"赛事MatchId不能为空");
            Assert.notNull(vo.getISBenchmarkScore(),"是否基准分ISBenchmarkScore不能为空");
            Integer userId = TradeUserUtils.getUserId();
            if (NumberUtils.INTEGER_ONE.intValue() != vo.getSportId()){
                return new HttpResponse(HttpResponse.SUCCESS,"暂时只支持足球");
            }
            Map<String,Object> map = matchForecastService.queryFuturesVolumeByPlayId(vo,userId);
            if (MapUtils.isEmpty(map)){
                return new HttpResponse(HttpResponse.SUCCESS,"无赛事数据或者赛事不存在");
            }
            return HttpResponse.success(map);
        } catch (IllegalArgumentException e){
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail(e.getMessage());
        } catch (RcsServiceException e){
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail(e.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail("风控服务器出问题");
        }
    }
    /**
     * @Description   查询赛事级别的forecast
     * @Param [vo]
     * @Author  Sean
     * @Date  11:27 2020/7/23
     * @return com.panda.sport.rcs.vo.HttpResponse<java.util.List<java.util.Map<java.lang.String,java.lang.Object>>>
     **/
    @RequestMapping(value = "/queryMatchForecast", method = RequestMethod.GET)
    public HttpResponse<Map<String,Object>> queryMatchForecast(@RequestParam Map<String,Object> map) {
        try {
            List<Long> matchIds = Lists.newArrayList();
            if (!ObjectUtils.isEmpty(map.get("matchIds"))){
                String idstr = map.get("matchIds").toString();
                String[] ids = idstr.split(",");
                List<String> matchList = Arrays.asList(ids);
                matchIds = matchList.stream().map(e -> Long.valueOf(e)).collect(Collectors.toList());
                map.remove("matchIds");
            }
            RcsPredictForecastVo vo = JSONObject.parseObject(JSONObject.toJSONString(map),RcsPredictForecastVo.class);
            vo.setMatchIds(matchIds);
            Assert.notNull(vo.getSportId(),"运动种类SportId不能为空");
            Integer pageNumber = ObjectUtils.isEmpty(map.get("pageNumber")) ? 1 : Integer.parseInt(map.get("pageNumber").toString());
            Integer pageSize = ObjectUtils.isEmpty(map.get("pageSize")) ? 10 : Integer.parseInt(map.get("pageSize").toString());
            pageNumber = (pageNumber - 1) * pageSize;
            if (NumberUtils.INTEGER_ONE.intValue() != vo.getSportId()){
                return new HttpResponse(HttpResponse.SUCCESS,"暂时只支持足球");
            }
            Integer userId = TradeUserUtils.getUserId();
            map = matchForecastService.queryMatchForecast(vo,userId,pageNumber,pageSize);
            if (MapUtils.isEmpty(map)){
                return new HttpResponse(HttpResponse.SUCCESS,"无赛事数据或者赛事不存在");
            }
            return HttpResponse.success(map);
        } catch (IllegalArgumentException e){
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail(e.getMessage());
        } catch (RcsServiceException e){
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail(e.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail("风控服务器出问题");
        }
    }

}
