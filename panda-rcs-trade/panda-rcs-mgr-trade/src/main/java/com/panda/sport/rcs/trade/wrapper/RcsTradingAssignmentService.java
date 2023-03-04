package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.RcsTradingAssignment;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.pojo.vo.TradingAssignmentDataVo;
import com.panda.sport.rcs.trade.vo.ChangePersonLiableVo;
import com.panda.sport.rcs.trade.vo.TradingAssignmentVo;
import com.panda.sport.rcs.vo.HttpResponse;
import com.panda.sports.api.vo.ShortSysUserVO;
import com.panda.sports.api.vo.SysTraderVO;

import java.util.List;

/**
 * @program: xindaima
 * @description:指派
 * @author: kimi
 * @create: 2020-11-07 16:50
 **/
public interface RcsTradingAssignmentService extends IService<RcsTradingAssignment> {

//    /**
//     * 开售初始化指派数据
//     * @param matchId isConfirm是否确认需要插入
//     */
//    List<RcsTradingAssignment>  sellAddRcsTradingAssignment(Long sportId,Long matchId);
    
     /**
     * @Description: 操盘权限  matchType 0滚球1早盘  sportId,matchType可以为空
     * @Param: [sportId, matchId, playId, matchType,userId]
     * @return: boolean  true有权限 flase 没权限
     * @Author: KIMI
     * @Date: 2020/11/8
     */
     boolean tradeJurisdictionByPlayId(Long sportId,Long matchId,Long playId,Integer matchType);

    boolean tradeJurisdictionByPlayId(Long playId, StandardMatchInfo matchInfo);
    /**
     * 多玩法权限0滚球1早盘  sportId,matchType可以为空
     * @param sportId
     * @param matchId
     * @param playIdList
     * @param matchType
     * @return
     */
     boolean tradeJurisdictionByPlayIdList(Long sportId, Long matchId, List<Long> playIdList, Integer matchType);

    boolean tradeJurisdictionByPlayIdList(List<Long> playIdList, StandardMatchInfo matchInfo);
     /**
     * @Description: 操盘权限 玩法集查询0滚球1早盘  sportId,matchType可以为空
     * @Param: [sportId, matchId, playSet 玩法集id, matchType, userId]
     * @return: boolean
     * @Author: KIMI
     * @Date: 2020/11/11
     */
     boolean tradeJurisdictionByPlaySet(Long sportId,Long matchId,Long playSet,Integer matchType);
     
     /**
     * @Description: sportId,matchType可以为空
     * @Param: [market, matchType]
     * @return: boolean
     * @Author: KIMI
     * @Date: 2020/11/12
     */
     boolean tradeJurisdictionByMarketId(Long marketId,Integer matchType);

    /**
     *  赛事操作权限  0滚球1早盘
     * @param matchId
     * @return
     */
     boolean tradeJurisdictionByMatchId(Long matchId);


     HttpResponse add(TradingAssignmentDataVo tradingAssignmentDataVo,String tradeId);

     SysTraderVO getTraderDataById(Integer tradeId);

     SysTraderVO  getTraderDataByName(String tradeName);

     HttpResponse update( List<RcsTradingAssignment> rcsTradingAssignmentList,Integer userId);

     void  deleteByIdAndMatchId(Integer matchId,Integer matchType,Integer traderId);

     ShortSysUserVO getShortSysUserById(Integer tradeId);

     HttpResponse changePersonLiable(ChangePersonLiableVo changePersonLiableVo,Integer userId,Integer appId);
    /**
     * @Description   //校验是否有操盘权限
     * @Param [sportId, matchId, playId, matchType]
     * @Author  sean
     * @Date   2022/5/20
     * @return boolean
     **/
    boolean hasTraderJurisdiction(RcsMatchMarketConfig config);

    void setWeights(List<TradingAssignmentVo> tradingAssignmentVos);
}
