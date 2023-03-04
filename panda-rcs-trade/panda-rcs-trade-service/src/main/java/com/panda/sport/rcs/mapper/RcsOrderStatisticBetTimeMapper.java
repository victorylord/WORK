package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.panda.sport.rcs.pojo.report.RcsOrderStatisticBetTime;
import com.panda.sport.rcs.vo.BaseRcsOrderStatisticTimeVo;
import com.panda.sport.rcs.vo.BaseRcsOrderVo;
import com.panda.sport.rcs.vo.TimeBeanVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author :  myname
 * @Project Name :  daima1
 * @Package Name :  com.panda.sport.rcs.mapper
 * @Description :  TODO
 * @Date: 2019-12-25 20:54
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Service
public interface RcsOrderStatisticBetTimeMapper extends BaseMapper<RcsOrderStatisticBetTime> {
    void updateRcsOrderStatisticBetTime(@Param("list") List<RcsOrderStatisticBetTime> list);

    void updateRcsOrderStatisticBet(@Param("bet") RcsOrderStatisticBetTime bet);

    /**
     * 赛事报表查询接口（年、期、周）
     * @param page
     * @param baseRcsOrderVo
     * @return
     */
    IPage<BaseRcsOrderStatisticTimeVo> selectBaseOrders(Page<BaseRcsOrderStatisticTimeVo> page, @Param("base") BaseRcsOrderVo baseRcsOrderVo);

    void deleteInfoByDate(@Param("startDate")String startDate);
    /**
     * 赛事报表查询接口(日)
     * @param page
     * @param baseRcsOrderVo
     * @return
     */
    IPage<BaseRcsOrderStatisticTimeVo> selectBaseOrdersByDay(Page<BaseRcsOrderStatisticTimeVo> page, @Param("base") BaseRcsOrderVo baseRcsOrderVo);

    /**
     * 赛事报表总计
     * @param baseRcsOrderVo
     * @return
     */
    BaseRcsOrderStatisticTimeVo selectSumBaseOrders(@Param("base") BaseRcsOrderVo baseRcsOrderVo);


    /**
     * @Description   //查询人数   settleTimeType时间种类  list
     * @return java.lang.Long
     * @Description //查询人数   settleTimeType时间种类  list
     * @Param [sportId, playId, matchType, tournamentId, settleTimeType, list]
     * @Author kimi
     * @Date 2019/12/28
     * @return java.lang.Long
     **/
    Long queryUserCount(@Param("sportId") Integer sportId, @Param("playId") Integer playId, @Param("matchType") Integer matchType, @Param("tournamentId") Integer tournamentId,
                        @Param("settleTimeType") Integer settleTimeType, @Param("orderStatus") List<Integer> orderStatus, @Param("list") List<TimeBeanVo> list);

    /**
     * @return java.lang.Long
     * @Description //查询汇总人数
     * @Param [sportIdList, playIdList, matchTypeList, tournamentIdList, settleTimeType, orderStatusList, timeBeanVoList]
     * @Author kimi
     * @Date 2020/1/7
     **/
    Long queryUserCountTotal(@Param("sportIdList") List<Integer> sportIdList, @Param("playIdList") List<Integer> playIdList, @Param("matchTypeList") List<Integer> matchTypeList,
                             @Param("tournamentIdList") List<Integer> tournamentIdList, @Param("settleTimeType") Integer settleTimeType, @Param("orderStatusList")
                                     List<Integer> orderStatusList, @Param("timeBeanVoList") List<TimeBeanVo> timeBeanVoList);
}

