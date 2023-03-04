package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.panda.sport.data.rcs.dto.ExtendBean;
import com.panda.sport.data.rcs.dto.OrderItem;
import com.panda.sport.rcs.pojo.TOrderDetail;
import com.panda.sport.rcs.vo.*;
import com.panda.sport.rcs.vo.statistics.SettleAmountVo;
import com.panda.sport.rcs.vo.statistics.SumMatchAmountVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * 投注单详细信息表 Mapper 接口
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-10-03
 */
@Service
public interface TOrderDetailMapper extends BaseMapper<TOrderDetail> {
    int insertAndUpdate(TOrderDetail orderDetail);

    int updateOrderDetailStatusBatch(List<TOrderDetail> orders);

    /**
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.panda.sport.rcs.vo.OrderDetailVo>
     * @Description //分页查询
     * @Param [page, matchId, playId, marketId, orderOdds]
     * @Author kimi
     * @Date 2020/1/22
     **/
    IPage<OrderDetailVo> selectTOrderDetailByMarketIdPage(IPage<OrderDetailVo> page, @Param("matchId") Long matchId, @Param("playId") Integer playId,
                                                          @Param("marketId") Long marketId, @Param("orderOdds") String orderOdds
            , @Param("matchType") Integer matchType, @Param("placeNum") Integer placeNum, @Param("sportId") Integer sportId);

    /**
     * @return java.util.List<com.panda.sport.rcs.vo.OrderDetailVo>
     * @Description //订单查询
     * @Param [matchId, playId, marketId, orderOdds]
     * @Author kimiselectTOrderDetailByMarketId
     * @Date 2020/1/22
     **/
    List<OrderDetailVo> selectTOrderDetailByMarketId(@Param("matchId") Long matchId, @Param("playId") Integer playId, @Param("marketId") Long marketId,
                                                     @Param("orderOdds") Integer orderOdds, @Param("matchType") Integer matchType);

    ExtendBean queryOrderDetail(Map<String, Object> map);

    List<TOrderDetail> getMatrixValList(Map<String, Object> map);

    /**
     * @return void
     * @Description 跟新注单状态
     * @Param [map]
     * @Author Sean
     * @Date 20:18 2019/11/6
     **/
    int updateOrderDetailStatus(Map<String, Object> map);
    //@Update("update t_order_detail set win_money=#{winMoney},result=#{result},stage=#{stage} where id=#{id}")
    //boolean updateOrderDetailAfterRefund(TOrderDetail detail);

    /**
     * 获取报表
     *
     * @param marketId
     * @return
     */
    List<OrderDetailStatReportVo> getStatReportByPlayOptions(@Param("marketId") Long marketId,@Param("orderNo")String orderNo,@Param("matchType") String matchType);

    /**
     * 查询平衡值明细
     *
     * @param matchId
     * @param marketCategoryId
     * @return
     */
    List<OrderDetailStatReportVo> getMarketStatByMatchIdAndPlayIdAndMatchStatus(@Param("matchId") Long matchId, @Param("marketCategoryId") Long marketCategoryId, @Param("matchStatus") Integer matchStatus);

    /**
     * 获取赛事维度下已结算
     *
     * @param matchId
     */
    SettleAmountVo getSettleBetAmount(@Param("matchId") Long matchId);

    /**
     * 获取赛事总货量
     *
     * @param orderNo,matchId
     * @return
     */
    SumMatchAmountVo getMatchSumBetAmount(@Param("orderNo") String orderNo,@Param("matchId") Long matchId);

    /**
     * @return java.lang.String
     * @Description 查询OptionValue
     * @Param [bean]
     * @Author Sean
     * @Date 17:54 2019/12/7
     **/
    String queryOptionValue(@Param("bean") OrderItem bean);

    /**
     * @return java.util.List<com.panda.sport.rcs.vo.operation.CalcProfitDetailVo>
     * @Description 查询期望详情
     * @Param [playName, matchId]
     * @Author toney
     * @Date 16:47 2019/12/10
     **/
    List<com.panda.sport.rcs.vo.operation.CalcProfitDetailVo> queryCalcProfitDetail(@Param("playName") String playName, @Param("matchId") Long matchId);

    /**
     * @return java.util.List<com.panda.sport.rcs.vo.OrderDetailStatisticVo>
     * @Description //查询结算的订单数据
     * @Param []
     * @Author kimi
     * @Date 2019/12/24
     **/
    List<OrderDetailStatisticVo> selectOrderDetailStatisticVoList(@Param("betNoSet") Set<String> betNoSet);

    /**
     * @param orderNo
     * @param mtsOrderStatus
     * @Description 更新MTS订单状态
     */
    void updateMtsOrder(@Param("orderNo") String orderNo, @Param("mtsOrderStatus") Integer mtsOrderStatus);

    /**
     * @Description   按ID升序取topX条数据
     * @Param [id,matchId]
     * @Author  toney
     * @Date  11:47 2020/1/27
     * @return java.util.List<com.panda.sport.rcs.pojo.TOrderDetail>
     **/
    List<TOrderDetail> getTopById(@Param("id") Long id, @Param("matchId") Long matchId, @Param("limit")Integer limit);
    /**
     * @Description   根据订单号查询所有的注单
     * @Param [orderNo]
     * @Author  Sean
     * @Date  16:54 2020/2/4
     * @return java.util.List<com.panda.sport.rcs.pojo.TOrderDetail>
     **/
    List<OrderItem> queryOrderDetailList(@Param("orderNo")String orderNo);

	void updateOrderDetailOdds(Map<String, Object> info);
    /**
     * @Description   //根据条件分页查询注单查询
     * @Param [vo]
     * @Author  Sean
     * @Date  16:21 2020/9/29
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.panda.sport.rcs.vo.OrderDetailVo>
     **/
    List<OrderDetailVo> queryBetList(@Param("vo") OrderDetailVo vo);
    /**
     * @Description   //查询记录总数
     * @Param [vo]
     * @Author  Sean
     * @Date  15:06 2020/10/2
     * @return java.lang.Long
     **/
    Long queryBetListCount(@Param("vo") OrderDetailVo vo);

    List<Integer> getOrderOdds(@Param("matchId")Integer matchId, @Param("playId")Integer playId);

    /**
     * @Description   //TODO
     * @Param [s]
     * @Author  kimi
     * @Date   2020/10/28
     * @return java.util.List<java.lang.Integer>getOrderOdds
     **/
    List<PlayVo> getPlayId();


    List<MarketChartResVo> queryMarketChart(@Param("matchId")Long matchId, @Param("playId")Integer playId, @Param("matchType")Integer matchType,
                                            @Param("marketId")Long marketId, @Param("userLevel")List<Integer> userLevel);

}
