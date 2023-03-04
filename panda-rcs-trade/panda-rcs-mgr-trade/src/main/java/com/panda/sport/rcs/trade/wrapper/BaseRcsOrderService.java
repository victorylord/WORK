package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.report.RcsOrderStatisticBetTime;
import com.panda.sport.rcs.vo.BaseRcsOrderStatisticTimeVo;
import com.panda.sport.rcs.vo.BaseRcsOrderVo;
import com.panda.sport.rcs.vo.TimeBeanVo;
import org.apache.ibatis.annotations.Param;

import java.text.ParseException;
import java.util.List;

/**
 * <p>
 * 投注单详细信息表 服务类
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-10-03
 */
public interface BaseRcsOrderService extends IService<RcsOrderStatisticBetTime> {
    /**
     * 赛事报表查询接口
     * @param page
     * @param baseRcsOrderVo
     * @return
     */
    IPage<BaseRcsOrderStatisticTimeVo> selectBaseOrders( Page<BaseRcsOrderStatisticTimeVo> page ,BaseRcsOrderVo baseRcsOrderVo)throws Exception;

    /**
     * 赛事报表总计
     * @param baseRcsOrderVo
     * @return
     */
    BaseRcsOrderStatisticTimeVo selectSumBaseOrders(BaseRcsOrderVo baseRcsOrderVo) throws Exception;

    /**
     * @return java.lang.Long
     * @Description //查询人数
     * @Param [sportId 体育种类id, playId玩法id, matchType玩法阶段, tournamentId联赛Id, settleTimeType时间类型, orderStatus, startDate开赛时间, endDate 结束时间]
     * @Author kimi
     * @Date 2019/12/30
     **/
    Long queryUserCount(Integer sportId, Integer playId, Integer matchType, Integer tournamentId, Integer settleTimeType, List<Integer> orderStatus, TimeBeanVo timeBeanVo) throws ParseException;

    /**
     * @return java.lang.Long
     * @Description //查询人数
     * @Param [sportId 体育种类id, playId玩法id, matchType玩法阶段, tournamentId联赛Id, settleTimeType时间类型, orderStatus, startDate开赛时间, endDate 结束时间]
     * @Author kimi
     * @Date 2019/12/30
     **/
    Long queryUserCount(Integer sportId, Integer playId, Integer matchType, Integer tournamentId, Integer settleTimeType, List<Integer> orderStatus, List<TimeBeanVo> list) throws ParseException;
//    /**
//     * @return java.lang.Long
//     * @Description //查询人数汇总
//     * @Param [sportId, playId, matchType, tournamentId, settleTimeType, orderStatus, list]
//     * @Author kimi
//     * @Date 2019/12/31
//     **/
//    Long queryUserCountTotal(Integer settleTimeType, List<Integer> orderStatus, TimeBeanVo timeBeanVo) throws ParseException;

    /**
     * @return java.lang.Long
     * @Description //查询人数汇总
     * @Param [sportId, playId, matchType, tournamentId, settleTimeType, orderStatus, list]
     * @Author kimi
     * @Date 2019/12/31
     **/
    Long queryUserCountTotal(List<Integer> sportId, List<Integer> playId, List<Integer> matchType, List<Integer> tournamentId, Integer settleTimeType, List<Integer> orderStatus,
                             List<TimeBeanVo> list) throws ParseException;
}
