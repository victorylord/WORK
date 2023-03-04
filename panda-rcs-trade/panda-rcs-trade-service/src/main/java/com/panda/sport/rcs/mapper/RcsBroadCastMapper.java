package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsBroadCast;
import com.panda.sport.rcs.pojo.vo.OperateMessageVo;
import com.panda.sport.rcs.pojo.vo.RcsBroadCastVo;
import com.panda.sport.rcs.vo.RcsBroadCastONEVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author :  enzo
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.mapper
 * @Description :  预警消息
 * @Date: 2020-09-16 16:38
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Component
public interface RcsBroadCastMapper extends BaseMapper<RcsBroadCast> {

    List<RcsBroadCast> queryMessageByTrader(@Param("matchIds") List<Long> matchIds, @Param("traderId") String traderId,@Param("createTime")Long createTime,@Param("matchStatus")Integer matchStatus);

    List<RcsBroadCastVo> queryRcsBroadCastVo(@Param("pageNum") Integer pageNum,@Param("pageSize") Integer pageSize, @Param("userId")Integer userId, @Param("sportIdList")List<Integer> sportIdList,
                                              @Param("time") Long time,@Param("isTrade") Integer isTrade);

    OperateMessageVo queryRcsBroadCastVoIsNoRead(@Param("userId")Integer userId, @Param("sportIdList")List<Integer> sportIdList, @Param("time") Long time,@Param("isTrade") Integer isTrade);

    /**
     * @Description  查询收藏赛事的玩家
     * @Param [matchId]
     * @Author  kimi
     * @Date   2020/10/23
     * @return java.util.List<java.lang.Integer>
     **/
    List<Integer> selectUserIdByCollection(@Param("matchId") Integer matchId);

    /**
     * @Description   //TODO
     * @Param [s]
     * @Author  kimi
     * @Date   2020/10/23
     * @return com.panda.sport.rcs.pojo.RcsBroadCast
     **/
    RcsBroadCast selectRcsBroadCast(@Param("s") String s);

    /**
     * @Description   //TODO
     * @Param [matchId]
     * @Author  kimi
     * @Date   2020/10/23
     * @return java.lang.Integer
     **/
    Integer selectSportIdByMatchId(@Param("matchId") Integer matchId);

    /**
     * 查询预警消息和封盘消息已读未读的数量
     * @param traderId
     * @param createTime
     * @return
     */
    List<RcsBroadCastONEVO> selectRcsBroadCastCountByWarningAndSealing(@Param("msgType") Integer msgtype, @Param("traderId") Integer traderId, @Param("createTime")Long createTime);
    /**
     * 查询预警消息和封盘消息已读未读的数量
     * @param traderId
     * @param createTime
     * @return
     */
    List<RcsBroadCastONEVO> selectRcsBroadCastCountByMatchErrorEventEnd(@Param("msgType") Integer msgtype, @Param("traderId") Integer traderId, @Param("createTime")Long createTime);


    /**
     * 查询具体的消息
     * @param msgtype
     * @param traderId
     * @param createTime
     * @return
     */
    List<RcsBroadCastVo> selectRcsBroadCastByWarningAndSealing(@Param("msgType") Integer msgtype, @Param("traderId") Integer traderId, @Param("createTime")Long createTime,@Param("pageNum") Integer pageNum,@Param("pageSize") Integer pageSize);

    /**
     * 查询具体的消息
     * @param msgtype
     * @param traderId
     * @param createTime
     * @return
     */
    List<RcsBroadCastVo> selectRcsBroadCastByMatchErrorEventEnd(@Param("msgType") Integer msgtype, @Param("traderId") Integer traderId, @Param("createTime")Long createTime,@Param("pageNum") Integer pageNum,@Param("pageSize") Integer pageSize);

    /**
     * 查询所有未读消息的Id
     * @param msgtype
     * @param traderId
     * @param createTime
     * @return
     */
    List<Integer> selectNoReadRcsBroadCastIdByWarningAndSealing(@Param("msgType") Integer msgtype, @Param("traderId") Integer traderId, @Param("createTime")Long createTime);

    /**
     * 查询所有未读消息的id
     * @param userId
     * @param sportIdList
     * @param time
     * @param isTrade
     * @return
     */
    List<Integer> selectNoReadRcsBroadCastIdBySettlement( @Param("userId")Integer userId, @Param("sportIdList")List<Integer> sportIdList, @Param("time") Long time,@Param("isTrade") Integer isTrade);

    /**
     * 查询所有未读消息的Id
     * @param traderId
     * @param createTime
     * @return
     */
    List<RcsBroadCastVo> selectNoticeByWarningAndSealing(@Param("matchStatus") Integer matchStatus, @Param("traderId") Integer traderId, @Param("createTime")Long createTime);

}
