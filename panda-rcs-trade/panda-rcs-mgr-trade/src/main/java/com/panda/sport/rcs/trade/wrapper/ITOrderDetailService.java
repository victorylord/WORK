package com.panda.sport.rcs.trade.wrapper;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.data.rcs.dto.OrderItem;
import com.panda.sport.rcs.pojo.RcsMatrixInfo;
import com.panda.sport.rcs.pojo.RcsMatrixInfoReqVo;
import com.panda.sport.rcs.pojo.TOrderDetail;
import com.panda.sport.rcs.vo.MatrixVo;
import com.panda.sport.rcs.vo.OrderDetailStatReportVo;
import com.panda.sport.rcs.vo.OrderDetailVo;
import com.panda.sport.rcs.vo.RequestMarketOrderVo;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 投注单详细信息表 服务类
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-10-03
 */
public interface ITOrderDetailService extends IService<TOrderDetail> {

    /**
     * 查询矩阵信息（新表）
     * @param vo
     * @param score 比分
     * @return
     */
    MatrixVo[][] getMatrixInfoData(RcsMatrixInfoReqVo vo, String score);

    /**
     *
     * @param item 参数（matchId 赛事ID，isSettlement： 1 未结算 2 已计算，matchType： 1 早盘 2 滚球盘）
     * @param startTime 开始时间
     * @param endTime  结束时间
     * @param tenantIds 商户ID
     * @param playIds playId 玩法ID，
     * @param unit 单位 1 10 100 1000
     * @param size 矩阵大小 5*5 / 6*6
     * @return 赛事矩阵
     */
    MatrixVo[][] getHalfMatrixByMatchId(TOrderDetail item, Date startTime, Date endTime, List<Long> playIds, List<Long> tenantIds, Integer unit, Integer size);

    /**
     * @Description  查询玩法管理中 赛事订单对应的全场比分矩阵
     * @Param [tenantId, matchId, matchType, settleStatus, playIds, unit]
     * @Param tenantId 商户ID
     * @Param matchId 赛事ID
     * @Param matchType  类型：1 ：早盘 ，2： 滚球盘， 3： 冠军盘 , 全部 null
     * @Param settleStatus 结算状态 0 未结算 1：已结算  全部 null
     * @Param playIds 玩法ID集合
     * 单位 1 10 100 1000
     * @Author  max
     * @Date  11:25 2019/11/8
     * @return com.panda.sport.rcs.vo.MatrixVo[][] 默认返回 12*12 大小的矩阵
     */
    MatrixVo[][] queryMatrixByMatchId(List<Long> tenantIds, Long matchId, Integer matchType, Integer settleStatus, List<Long> playIds, Integer unit, Integer size);

    /**
     * 查询平衡值明细
     * @param matchId
     * @param marketCategoryId
     * @return
     */
    List<OrderDetailStatReportVo> getMarketStatByMatchIdAndPlayIdAndMatchStatus(Long matchId, Long marketCategoryId,Integer matchStatus);

    /**
     * @return java.util.List<com.panda.sport.rcs.vo.OrderDetailVo>
     * @Description //TODO
     * @Param [marketId, orderOdds]
     * @Author kimi
     * @Date 2019/12/12
     **/
    IPage<OrderDetailVo> selectTOrderDetailByMarketIdPage(RequestMarketOrderVo requestMarketOrderVo, Integer matchType);
    /**
     * @Description   //根据条件查询注单记录
     * @Param [OrderDetailVo]
     * @Author  Sean
     * @Date  15:54 2020/9/29
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.panda.sport.rcs.vo.OrderDetailVo>
     **/
    IPage<OrderDetailVo> queryBetList(OrderDetailVo vo);

    /**
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.panda.sport.rcs.vo.OrderDetailVo>
     * @Description //TODO
     * @Param [requestMarketOrderVo, orderOdds]
     * @Author kimi
     * @Date 2020/1/22
     **/
    List<OrderDetailVo> selectTOrderDetailByMarketId(RequestMarketOrderVo requestMarketOrderVo, Integer orderOdds, Integer matchType);

    /**
     * @return java.lang.String
     * @Description 查询OptionValue
     * @Param [bean]
     * @Author Sean
     * @Date 17:54 2019/12/7
     **/
    List<OrderItem> queryOptionValue(String orderNo);

    /**
     * @Description   初始化30秒只能的未处理订单
     * @Param [map]
     * @Author  Sean
     * @Date  17:14 2020/2/13
     * @return java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     **/
    List<JSONObject> initBetRecode(Long matchId);
    /**
     * @Description   分页查询投注信息
     * @Param [map]
     * @Author  Sean
     * @Date  17:19 2020/2/20
     * @return java.util.List<com.panda.sport.data.rcs.dto.OrderItem>
     **/
    Map<String,Object> queryOrderByPage(Map<String,Object> map);

    public String queryOptionValue(OrderItem bean);

    public String getCurrentScore(Long matchId);

    public String getHalfScore(Long matchId);

    List<Integer> getOrderOdds(Integer matchId,Integer playId);
}
