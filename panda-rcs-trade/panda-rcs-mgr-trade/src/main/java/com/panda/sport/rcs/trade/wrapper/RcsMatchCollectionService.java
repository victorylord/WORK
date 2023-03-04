package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.RcsMatchCollection;
import com.panda.sport.rcs.pojo.dto.QueryPreLiveMatchDto;

import java.util.List;
import java.util.Map;

/**
 * @author :  myname
 * @Project Name :  daima1
 * @Package Name :  com.panda.sport.rcs.trade.wrapper
 * @Description :  TODO
 * @Date: 2019-10-25 14:31
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface RcsMatchCollectionService extends IService<RcsMatchCollection> {

    /**
     * @return java.util.List<com.panda.sport.rcs.pojo.RcsMatchCollection>
     * @Description //查询操作
     * @Param [columnMap]
     * @Author kimi
     * @Date 2019/10/25
     **/
    List<RcsMatchCollection> selectByMap(Map<String, Object> columnMap);

    boolean exist(Long matchId, Long tournamentId, Long tradeId);

    List<QueryPreLiveMatchDto> existList(List<QueryPreLiveMatchDto> matchDtos,Long tradeId,String marketType);

    Integer selectMatchCollectionCount(RcsMatchCollection matchCollection) ;

    boolean updateRcsMatchCollection(RcsMatchCollection matchCollection);

    List<RcsMatchCollection> getSyUserColletCondition(RcsMatchCollection matchCollection);
    /**
     * @Description   //根据用户id和赛事id查询用户是否收藏该赛事 1 表示已收藏  2 表示 未收藏
     * @Param [userId, matchId]
     * @Author  Sean
     * @Date  15:23 2020/5/29
     * @return java.lang.Integer
     **/
    Integer queryFavoriteStatus(Long userId,Long matchId,Long beginTime);

    /**
     * 查询收藏赛事ID
     * @param collection
     * @return
     */
    List<Long> queryCollMatchIds(RcsMatchCollection collection,List<Long> traderMatchIds);

    /**
     * 查询收藏联赛ID
     * @param collection
     * @return
     */
    List<Long> querytourColl(RcsMatchCollection collection);

    /**
     * 查询取消的收藏赛事ID
     * @param collection
     * @return
     */
    List<Long> queryNoMatchIds(RcsMatchCollection collection);
}
