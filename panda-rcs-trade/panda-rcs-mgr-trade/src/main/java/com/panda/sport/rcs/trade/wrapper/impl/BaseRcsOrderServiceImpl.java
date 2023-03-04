package com.panda.sport.rcs.trade.wrapper.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.common.DateUtils;
import com.panda.sport.rcs.common.NumberUtils;
import com.panda.sport.rcs.enums.DateTypeEnum;
import com.panda.sport.rcs.enums.MatchTypeReportEnum;
import com.panda.sport.rcs.enums.SportTypeEnum;
import com.panda.sport.rcs.mapper.RcsOrderStatisticBetTimeMapper;
import com.panda.sport.rcs.pojo.LanguageInternation;
import com.panda.sport.rcs.pojo.report.RcsOrderStatisticBetTime;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.BaseRcsOrderService;
import com.panda.sport.rcs.trade.wrapper.RcsLanguageInternationService;
import com.panda.sport.rcs.trade.wrapper.StandardSportMarketCategoryService;
import com.panda.sport.rcs.vo.BaseRcsOrderStatisticTimeVo;
import com.panda.sport.rcs.vo.BaseRcsOrderVo;
import com.panda.sport.rcs.vo.TimeBeanVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 多语言 服务实现类
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-09-03
 */
@Service
@Slf4j
public class BaseRcsOrderServiceImpl extends ServiceImpl<RcsOrderStatisticBetTimeMapper, RcsOrderStatisticBetTime> implements BaseRcsOrderService {
    @Autowired
    private RcsOrderStatisticBetTimeMapper rcsOrderStatisticBetTimeMapper;
    
    @Autowired
    private RcsLanguageInternationService rcsLanguageInternationService;

    @Autowired
    StandardSportMarketCategoryService standardSportMarketCategoryService;


    private void checkBaseParams(BaseRcsOrderVo baseRcsOrderVo) {
        if (baseRcsOrderVo.getSportIds() != null && baseRcsOrderVo.getSportIds().size() <= 0) {
            baseRcsOrderVo.setSportIds(null);
        }
        if (baseRcsOrderVo.getMatchTypes() != null && baseRcsOrderVo.getMatchTypes().size() <= 0) {
            baseRcsOrderVo.setMatchTypes(null);
        }
        if (baseRcsOrderVo.getPlayIds() != null && baseRcsOrderVo.getPlayIds().size() <= 0) {
            baseRcsOrderVo.setPlayIds(null);
        }
        if (baseRcsOrderVo.getTournamentIds() != null && baseRcsOrderVo.getTournamentIds().size() <= 0) {
            baseRcsOrderVo.setTournamentIds(null);
        }
        if (baseRcsOrderVo.getTournamentIds() != null && baseRcsOrderVo.getTournamentIds().size() > 0) {
            if (baseRcsOrderVo.getTournamentIds().contains(-1)) {
                baseRcsOrderVo.setAllTournament(1);
            }
        }
        if (baseRcsOrderVo.getPlayIds() != null && baseRcsOrderVo.getPlayIds().size() > 0) {
            if (baseRcsOrderVo.getPlayIds().contains(-1)) {
                baseRcsOrderVo.setAllPlayId(1);
            }
        }
    }

    private void initModel(BaseRcsOrderStatisticTimeVo model) {
        if (model.getPlayId() != null) {
            LanguageInternation language = rcsLanguageInternationService.getLanguageInternationByCategoryId(Long.parseLong(String.valueOf(model.getPlayId())));
            model.setPlayName(language.getText());
        }
        if (model.getMatchType() != null) {
            model.setMatchTypeName(MatchTypeReportEnum.getMatchTypeEnum(model.getMatchType()).getValue());
        }
        if (model.getSportId() != null) {
            model.setSportName(SportTypeEnum.getSportTypeEnum(model.getSportId()).getValue());
        }
        BigDecimal scale = model.getAmountProfitPerOrderCount().setScale(4, RoundingMode.HALF_UP);
        model.setAmountProfitPerOrderCount(scale);
    }

    private Long getUserCounts(BaseRcsOrderStatisticTimeVo model, BaseRcsOrderVo baseRcsOrderVo, TimeBeanVo vo) {
        Long customers = null;
        try {
            customers = queryUserCount(model.getSportId(), model.getPlayId(), model.getMatchType(), model.getTournamentId(), baseRcsOrderVo.getSettleTimeType(), baseRcsOrderVo.getOrderStatuses(), vo);
            log.info("::{}::selectBaseOrders赛事报表查询人数" + customers, CommonUtil.getRequestId());
        } catch (ParseException e) {
            customers = 0L;
            log.error("::{}::获取人数失败：{}",CommonUtil.getRequestId(), e.getMessage(),e);
        }
        if (customers == null) {
            customers = 0L;
        }
        return customers;
    }

    @Override
    public IPage<BaseRcsOrderStatisticTimeVo> selectBaseOrders(Page<BaseRcsOrderStatisticTimeVo> page, BaseRcsOrderVo baseRcsOrderVo) throws Exception {
        checkBaseParams(baseRcsOrderVo);
        List<TimeBeanVo> times = baseRcsOrderVo.getTimeBeanVoList();

        baseRcsOrderVo.setDateType(DateTypeEnum.getDateTypeEnum(baseRcsOrderVo.getJsonTimes().getString("type")).getCode());

        IPage<BaseRcsOrderStatisticTimeVo> pageResult = rcsOrderStatisticBetTimeMapper.selectBaseOrders(page, baseRcsOrderVo);

        List<BaseRcsOrderStatisticTimeVo> baseRcsOrderStatisticTimeVos = pageResult.getRecords();
        if (baseRcsOrderStatisticTimeVos.size() > 0) {
            baseRcsOrderStatisticTimeVos.stream().forEach(model -> {
                initModel(model);
                TimeBeanVo vo = getDateName(times, model.getBaseDate());
                String type = baseRcsOrderVo.getJsonTimes().getString("type");
                String date = "";
                switch (type) {
                    case "y":
                        date = model.getOrderYear()+"年";
                        break;
                    case "p":
                        date = model.getOrderYear() + "年 " + model.getOrderPhase();
                        model.setStartDate(new SimpleDateFormat("MM/dd").format(DateUtils.StringToDate(vo.getStartTime())));
                        break;
                    case "w":
                        date = model.getOrderYear() + "年 " + model.getOrderPhase() + " " + model.getOrderWeek();
                        model.setStartDate(new SimpleDateFormat("MM/dd").format(DateUtils.StringToDate(vo.getStartTime())));
                        break;
                    case "d":
                        date = model.getBaseDate();
                        vo.setStartTime(model.getBaseDate());
                        try {
                            vo.setEndTime(DateUtils.addDate(model.getBaseDate(),1));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        date = model.getBaseDate();
                        break;
                }
                model.setDate(date);
                Long customers = getUserCounts(model, baseRcsOrderVo, vo);
                model.setCustomerCount(customers);
                model.initAmountPerCustomer();
                model.initOrderCountPerCustomer();
                model.initAmountPerOrderCount();

            });
        }
        pageResult.setRecords(baseRcsOrderStatisticTimeVos);
        return pageResult;
    }

    @Override
    public BaseRcsOrderStatisticTimeVo selectSumBaseOrders(BaseRcsOrderVo baseRcsOrderVo) throws Exception {
        checkBaseParams(baseRcsOrderVo);
        List<TimeBeanVo> times = baseRcsOrderVo.getTimeBeanVoList();
        if (baseRcsOrderVo.getTournamentIds() != null && baseRcsOrderVo.getTournamentIds().size() > 0) {
            if (baseRcsOrderVo.getTournamentIds().contains(-1)) {
                baseRcsOrderVo.setTournamentIds(null);
            }
        }
        BaseRcsOrderStatisticTimeVo baseRcs = rcsOrderStatisticBetTimeMapper.selectSumBaseOrders(baseRcsOrderVo);
        initModel(baseRcs);
        Long customers = 0l;
        try {
            customers = queryUserCountTotal(baseRcsOrderVo.getSportIds(),baseRcsOrderVo.getPlayIds(),baseRcsOrderVo.getMatchTypes(),baseRcsOrderVo.getTournamentIds(),baseRcsOrderVo.getSettleTimeType(), baseRcsOrderVo.getOrderStatuses(), times);
        } catch (ParseException e) {
            log.error("::{}::获取人数失败", e.getMessage(),e);
        }
        if (customers == null) {
            customers = 0L;
        }
        log.info("::{}::selectSumBaseOrder赛事报表查询人数" + customers,CommonUtil.getRequestId());

        BigDecimal customerCount = NumberUtils.getBigDecimal(customers);
        baseRcs.setCustomerCount(customers);
        if (customerCount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal amountPerCustomer = NumberUtils.getBigDecimal(baseRcs.getOrderAmountSum()).divide(customerCount, 0, BigDecimal.ROUND_HALF_UP);
            baseRcs.setAmountPerCustomer(amountPerCustomer);
            BigDecimal orderCountPerCustomer = NumberUtils.getBigDecimal(baseRcs.getOrderCount()).divide(customerCount, 0, BigDecimal.ROUND_HALF_UP);
            baseRcs.setOrderCountPerCustomer(orderCountPerCustomer);
        } else {
            baseRcs.setAmountPerCustomer(BigDecimal.ZERO);
            baseRcs.setOrderCountPerCustomer(BigDecimal.ZERO);
        }
        if (NumberUtils.getBigDecimal(baseRcs.getOrderCount()).compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal amountPerOrderCount = NumberUtils.getBigDecimal(baseRcs.getOrderAmountSum()).divide(NumberUtils.getBigDecimal(baseRcs.getOrderCount()), 0, BigDecimal.ROUND_HALF_UP);
            baseRcs.setAmountPerOrderCount(amountPerOrderCount);
        } else {
            baseRcs.setAmountPerOrderCount(BigDecimal.ZERO);
        }
        return baseRcs;
    }

    public TimeBeanVo getDateName(List<TimeBeanVo> times, String date) {
        TimeBeanVo timeBeanVo = new TimeBeanVo();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date parse = sdf.parse(date);
            for (TimeBeanVo vo : times) {
                Date startTime = sdf.parse(vo.getStartTime());
                Date endTime = sdf.parse(vo.getEndTime());
                if (startTime.getTime() <= parse.getTime() && parse.getTime() < endTime.getTime()) {
                    timeBeanVo = vo;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timeBeanVo;
    }


    /**
     * @return java.lang.Long
     * @Description //查询人数
     * @Param [sportId, playId, matchType, tournamentId, settleTimeType, orderStatus, timeBeanVo]
     * @Author kimi
     * @Date 2019/12/31
     **/
    @Override
    public Long queryUserCount(Integer sportId, Integer playId, Integer matchType, Integer tournamentId, Integer settleTimeType, List<Integer> orderStatus, TimeBeanVo timeBeanVo) throws ParseException {
        List<TimeBeanVo> list = new ArrayList<>(1);
        list.add(timeBeanVo);
        return queryUserCount(sportId, playId, matchType, tournamentId, settleTimeType, orderStatus, list);
    }

//    /**
//     * @return java.lang.Long
//     * @Description //查询人数汇总
//     * @Param [sportId, playId, matchType, tournamentId, settleTimeType, orderStatus, timeBeanVo]
//     * @Author kimi
//     * @Date 2019/12/31
//     **/
//    @Override
//    public Long queryUserCountTotal(Integer settleTimeType, List<Integer> orderStatus, TimeBeanVo timeBeanVo) throws ParseException {
//        List<TimeBeanVo> list = new ArrayList<>(1);
//        list.add(timeBeanVo);
//        return queryUserCount(null, null, null, null, settleTimeType, orderStatus, list);
//    }


    /**
     * @return java.lang.Long
     * @Description //查询人数
     * @Param [sportId, playId, matchType, tournamentId, settleTimeType, orderStatus, timeBeanVo]
     * @Author kimi
     * @Date 2019/12/31
     **/
    @Override
    public Long queryUserCount(Integer sportId, Integer playId, Integer matchType, Integer tournamentId, Integer settleTimeType, List<Integer> orderStatus, List<TimeBeanVo> list) throws ParseException {
        if (CollectionUtils.isEmpty(list)) {
            log.error("::{}::数据出现严重错误",CommonUtil.getRequestId());
        }
        for (TimeBeanVo timeBeanVo : list) {
            timeBeanVo.setStartTimeValue(DateUtils.stringToDateAddTwelveHour(timeBeanVo.getStartTime()));
            timeBeanVo.setEndTimeValue(DateUtils.stringToDateAddTwelveHour(timeBeanVo.getEndTime()));
        }
        List<Integer> orderStatus1 = getOrderStatus(orderStatus);
        Long aLong = rcsOrderStatisticBetTimeMapper.queryUserCount(sportId, playId, matchType, tournamentId, settleTimeType, orderStatus1, list);
        if (aLong == 0) {
            System.out.println("12121212");
        }
        return aLong;
    }

    @Override
    public Long queryUserCountTotal(List<Integer> sportIdList, List<Integer> playIdList, List<Integer> matchTypeList, List<Integer> tournamentIdList, Integer settleTimeType,
                                    List<Integer> orderStatusList, List<TimeBeanVo> timeBeanVoList) throws ParseException {
        List<Integer> orderStatus = getOrderStatus(orderStatusList);
        for (TimeBeanVo timeBeanVo : timeBeanVoList) {
            timeBeanVo.setStartTimeValue(DateUtils.stringToDateAddTwelveHour(timeBeanVo.getStartTime()));
            timeBeanVo.setEndTimeValue(DateUtils.stringToDateAddTwelveHour(timeBeanVo.getEndTime()));
        }
        Long aLong = rcsOrderStatisticBetTimeMapper.queryUserCountTotal(sportIdList, playIdList, matchTypeList, tournamentIdList, settleTimeType, orderStatus, timeBeanVoList);
        if (aLong == 0) {
            System.out.println("121212");
        }
        return aLong;
    }

    /**
     * @return java.util.List<java.lang.Integer>
     * @Description //转换成数据库需要的
     * @Param [orderStatusList]
     * @Author kimi
     * @Date 2020/1/10
     **/
    private List<Integer> getOrderStatus(List<Integer> orderStatusList) {
        List<Integer> orderStatus1 = new ArrayList<>();
        for (Integer integer : orderStatusList) {
            //有效投注
            if (integer == 1) {
                orderStatus1.add(6);
                orderStatus1.add(2);
                orderStatus1.add(3);
                orderStatus1.add(4);
                orderStatus1.add(5);
            }//已取消注单
            else if (integer == 2) {
                orderStatus1.add(8);
                orderStatus1.add(7);
            }//已拒绝注单
            else {
                //todo 暂时没有
                orderStatus1.add(10);
                orderStatus1.add(11);
            }
        }
        return orderStatus1;
    }
}
