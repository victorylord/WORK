package com.panda.sport.rcs.trade.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.panda.sport.data.rcs.api.Response;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.mapper.TOrderDetailMapper;
import com.panda.sport.rcs.mapper.TUserLevelMapper;
import com.panda.sport.rcs.pojo.TOrderDetail;
import com.panda.sport.rcs.pojo.TUserLevel;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.util.ForecastPlayIds;
import com.panda.sport.rcs.trade.wrapper.ITOrderDetailService;
import com.panda.sport.rcs.trade.wrapper.StandardMatchInfoService;
import com.panda.sport.rcs.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author :  myname
 * @Project Name :  daima1
 * @Package Name :  com.panda.sport.rcs.trade.controller
 * @Description :  TODO
 * @Date: 2019-12-12 10:21
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@RestController
@Slf4j
@RequestMapping(value = "/orderDetail")
public class OrderDetailController {
    @Autowired
    private ITOrderDetailService itOrderDetailService;
    @Autowired
    private TUserLevelMapper userLevelMapper;
    @Autowired
    private TOrderDetailMapper orderDetailMapper;
    @Autowired
    private StandardMatchInfoService standardMatchInfoService;

    /**
     * @return
     * @Description //获取盘口注单详情
     * @Param [marketId, page]
     * @Author kimi
     * @Date 2019/12/12
     **/
    @RequestMapping(value = "/getMarketOrder", method = RequestMethod.POST)
    public HttpResponse<IPage<OrderDetailVo>> getMarketOrder(@RequestBody RequestMarketOrderVo requestMarketOrderVo) {
        try {
            //滚球
            IPage<OrderDetailVo> iPage = itOrderDetailService.selectTOrderDetailByMarketIdPage(requestMarketOrderVo, requestMarketOrderVo.getMatchType());
            //页面显示的时候区分谁让的球
            return HttpResponse.success(dealWithSting(iPage));
        } catch (Exception e) {
            log.error("::{}::风控服务器出问题{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failure("风控服务器出问题");
        }
    }

    private IPage<OrderDetailVo> dealWithSting(IPage<OrderDetailVo> orderDetailVoList) {
        if (CollectionUtils.isEmpty(orderDetailVoList.getRecords())) {
            for (OrderDetailVo orderDetailVo : orderDetailVoList.getRecords()) {
                String marketValue = orderDetailVo.getMarketValue();
                String[] split = marketValue.split("/");
                if (split.length == 2) {
                    double v = Double.parseDouble(split[0]) + Double.parseDouble(split[1]);
                    orderDetailVo.setMarketValue(String.valueOf(v / 2));
                }
            }
        }
        if (!CollectionUtils.isEmpty(orderDetailVoList.getRecords())) {
            for (OrderDetailVo orderDetailVo : orderDetailVoList.getRecords()) {
                String marketValue = orderDetailVo.getMarketValue();
                //足球
                if (orderDetailVo.getSportId() == 1 && String.valueOf(orderDetailVo.getPlayId()).matches("4|19|113|121")) {
                    changeMarketValue(orderDetailVo);
                }
                //篮球
                if (orderDetailVo.getSportId() == 2 && String.valueOf(orderDetailVo.getPlayId()).matches("19|39|46|52|58|64|143")) {
                    changeMarketValue(orderDetailVo);
                }
            }
        }
        return orderDetailVoList;
    }

    //盘口值展示  主客队取反
    private void changeMarketValue(OrderDetailVo orderDetailVo) {
        if (orderDetailVo.getPlayOptions() != null && orderDetailVo.getPlayOptions().equals("2")) {
            String marketValue = orderDetailVo.getMarketValue();
            if (StringUtils.isEmpty(marketValue)) {
                return;
            }
            if (marketValue.equals("0")) {
                return;
            }
            if (marketValue.startsWith("-")) {
                orderDetailVo.setMarketValue("+" + marketValue.substring(1));
            } else {
                if (marketValue.startsWith("+")) {
                    orderDetailVo.setMarketValue("-" + marketValue.substring(1));
                } else {
                    orderDetailVo.setMarketValue("-" + marketValue);
                }
            }
        }
    }


    /**
     * 获取派奖信息
     * 成功返回注单号
     */

    @RequestMapping(value = "/getMatrix")
    @ResponseBody
    public Response getMatrix(String orderNo) {
        QueryWrapper<TOrderDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TOrderDetail::getOrderNo, orderNo);
        return Response.success(itOrderDetailService.getOne(queryWrapper));
    }

    /**
     * @return com.panda.sport.rcs.vo.HttpResponse<com.baomidou.mybatisplus.core.metadata.IPage < com.panda.sport.rcs.vo.OrderDetailVo>>
     * @Description //查询注单列表
     * @Param [vo]
     * @Author Sean
     * @Date 11:41 2020/9/30
     **/
    @RequestMapping(value = "queryBetList", method = RequestMethod.POST)
    public HttpResponse<IPage<OrderDetailVo>> queryBetList(@RequestBody OrderDetailVo vo) {
        try {
            Assert.notNull(vo.getMatchId(), "赛事Id不能为空");
//            Assert.notNull(vo.getMinBetTime(),"事件范围不能为空");
//            Assert.notNull(vo.getMaxBetTime(),"事件范围不能为空");
            IPage<OrderDetailVo> list = itOrderDetailService.queryBetList(vo);
            return HttpResponse.success(list);
        } catch (IllegalArgumentException e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(HttpResponse.FAIL, e.getMessage());
        } catch (RcsServiceException e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(HttpResponse.FAIL, "服务繁忙，稍后重试");
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
        return HttpResponse.success(null);
    }

    /**
     * @return com.panda.sport.rcs.vo.HttpResponse<java.util.List < com.panda.sport.rcs.pojo.TUserLevel>>
     * @Description //查询用户等级
     * @Param []
     * @Author Sean
     * @Date 20:41 2020/9/30
     **/
    @RequestMapping(value = "queryUserLevelList", method = RequestMethod.GET)
    public HttpResponse<List<TUserLevel>> queryUserLevelList() {
        try {
            QueryWrapper<TUserLevel> queryWrapper = new QueryWrapper();
            List<TUserLevel> list = userLevelMapper.selectList(queryWrapper);
            return HttpResponse.success(list);
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
        return HttpResponse.success(null);
    }

    @RequestMapping(value = "queryMarketChart", method = RequestMethod.POST)
    @ResponseBody
    public HttpResponse<MarketChartResVo> queryMarketChart(@RequestBody MarketChartReqVo reqVo) {
        try {


            List<MarketChartResVo> list = orderDetailMapper.queryMarketChart(reqVo.getMatchId(), reqVo.getPlayId(), reqVo.getMatchType(), reqVo.getMarketId(), reqVo.getUserLevel());

            Integer letPoint[] = ForecastPlayIds.letPoint;
            Integer bigSmall[] = ForecastPlayIds.bigSmall;
            List<MarketChartResVo> resVoList = new ArrayList<>();
            Map<Integer, List<MarketChartResVo>> groupMap = list.stream().collect(Collectors.groupingBy(bean -> bean.getUserLevel()));
            groupMap.forEach((k, v) -> {
                if (v.size() > 1) {
                    resVoList.addAll(v);
                } else if (v.size() == 1) {
                    //如果是让球
                    if (Arrays.asList(letPoint).contains(reqVo.getPlayId())) {
                        MarketChartResVo vo = v.get(0);
                        resVoList.add(vo);
                        MarketChartResVo oherVo = new MarketChartResVo();
                        BeanUtils.copyProperties(vo, oherVo);
                        oherVo.setTotalBetAmount(0L);
                        oherVo.setPlayOptionsId(0L);
                        if(vo.getPlayOptions().equals("1")){
                            oherVo.setPlayOptions("2");
                        }else {
                            oherVo.setPlayOptions("1");
                        }
                        resVoList.add(oherVo);
                    } else if (Arrays.asList(bigSmall).contains(reqVo.getPlayId())) {
                        MarketChartResVo vo = v.get(0);
                        resVoList.add(vo);
                        MarketChartResVo oherVo = new MarketChartResVo();
                        BeanUtils.copyProperties(vo, oherVo);
                        oherVo.setTotalBetAmount(0L);
                        oherVo.setPlayOptionsId(0L);
                        if (vo.getPlayOptions().equals("Over")) {
                            oherVo.setPlayOptions("Under");
                        } else {
                            oherVo.setPlayOptions("Over");
                        }
                        resVoList.add(oherVo);
                    } else {//独赢
                        resVoList.addAll(v);
                    }
                }
            });
            return HttpResponse.success(resVoList);
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
        return HttpResponse.success(null);
    }

}