package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.pojo.StandardSportMarket;
import com.panda.sport.rcs.pojo.StandardSportMarketOdds;
import com.panda.sport.rcs.pojo.dto.StandardMarketPlaceDto;
import com.panda.sport.rcs.vo.SportMatchInfoVo;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 足球赛事盘口表. 使用盘口关联的功能存在以下假设：同一个盘口的显示值不可变更，如果变更需要删除2个盘口之间的关联关系。。 服务类
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-09-03
 */
public interface StandardSportMarketService extends IService<StandardSportMarket> {

    /**
     * 获取有效的盘口（数据源状态为0、1）
     *
     * @param matchId
     * @param playIds
     * @return
     */
    Map<Long, List<StandardSportMarket>> getEffectiveMarket(Long matchId, List<Long> playIds);

    /**
     * 获取标准投注项模板ID
     *
     * @param playId
     * @return
     */
    Map<Integer, Long> getOddsFieldsTemplateId(Long playId);

    /**
     * @return com.panda.sport.rcs.pojo.StandardSportMarket
     * @Description //根据主键获取数据
     * @Param [id]
     * @Author kimi
     * @Date 2019/11/14
     **/
    StandardSportMarket getStandardSportMarketById(long id);

    /**
     * @return java.util.List<com.panda.sport.rcs.pojo.StandardSportMarket>
     * @Description //根据赛事id查询
     * @Param [matchId]
     * @Author kimi
     * @Date 2020/2/18
     **/
    List<StandardSportMarket> getStandardSportMarketByMatchId(Long matchId);

    /**
     * @return java.lang.Integer
     * @Description //根据盘口值去查盘口Id
     * @Param [matchId, marketValue]
     * @Author kimi
     * @Date 2020/1/25
     **/
    Long selectStandardSportMarketIdByMarketValue(Long matchId, Long playId, String marketValue);

    /**
     * @return java.util.List<com.panda.sport.rcs.pojo.StandardSportMarket>
     * @Description //TODO
     * @Param []
     * @Author kimi
     * @Date 2020/2/17
     **/
    List<StandardSportMarket> selectStandardSportMarketByMap(Map<String, Object> columnMap);

    /**
     * @return java.util.List<java.lang.Integer>
     * @Description //TODO
     * @Param [matchId]
     * @Author kimi
     * @Date 2020/2/18
     **/
    List<Long> selectPlayIdByMatchId(Long matchId);

    /**
     * @return java.util.List<com.panda.sport.rcs.pojo.StandardSportMarket>
     * @Description //TODO
     * @Param []
     * @Author kimi
     * @Date 2020/2/20
     **/
    List<StandardSportMarketOdds> selectStandardSportMarketByGiveWay(Long matchId, Long playId);

    /**
     * @return java.util.List<com.panda.sport.rcs.pojo.StandardSportMarketOdds>
     * @Description //TODO
     * @Param []
     * @Author kimi
     * @Date 2020/3/11
     **/
    StandardSportMarket selectById(Long id);

    /**
     * 根据赛事ID查询盘口信息列表
     *
     * @param matchId
     * @return
     * @author Paca
     */
    List<StandardSportMarket> list(Long matchId);

    /**
     * 根据赛事ID和玩法ID查询盘口信息列表
     *
     * @param matchId
     * @param categoryId
     * @return
     * @author Paca
     */
    List<StandardSportMarket> list(Long matchId, Long categoryId);

    /**
     * 根据赛事ID和玩法ID集合查询盘口信息列表
     *
     * @param matchId
     * @param categoryIds
     * @return
     * @author Paca
     */
    List<StandardSportMarket> list(Long matchId, Collection<Long> categoryIds);

    /**
     * 根据赛事ID和盘口ID查询盘口信息
     *
     * @param matchId
     * @param marketId
     * @return
     * @author Paca
     */
    StandardSportMarket get(Long matchId, Long marketId);

    /**
     * 查询盘口信息
     *
     * @param matchId 赛事ID
     * @param playId  玩法ID
     * @return
     */
    List<StandardSportMarket> queryMarketInfo(Long matchId, Long playId);

    /**
     * 查询盘口信息
     *
     * @param matchId 赛事ID
     * @param playIds 玩法ID集合
     * @return
     */
    List<StandardSportMarket> queryMarketInfo(Long matchId, Collection<Long> playIds);

    /**
     * 查询主盘口信息
     *
     * @param matchId 赛事ID
     * @param playId  玩法ID
     * @return
     * @author Paca
     */
    StandardSportMarket queryMainMarketInfo(Long matchId, Long playId);

    /**
     * 查询主盘口信息
     *
     * @param matchId
     * @param playIds
     * @return
     */
    Map<Long, Map<Long, StandardSportMarket>> listMainMarketInfo(Long matchId, Collection<Long> playIds);

    /**
     * 查询主盘口信息
     *
     * @param matchId    赛事ID
     * @param categoryId 玩法ID
     * @return
     * @author Paca
     */
    StandardSportMarket selectMainMarketInfo(Long matchId, Long categoryId, String subPlayId);

    /**
     * 获取主盘口位置信息
     *
     * @param matchId
     * @param playId
     * @return
     * @author Paca
     */
    StandardMarketPlaceDto getMainMarketPlaceInfo(Long matchId, Long playId);

    /**
     * 查询盘口信息列表
     *
     * @param matchId
     * @param playId
     * @param marketType
     * @return
     * @author Paca
     */
    List<StandardSportMarket> list(Long matchId, Long playId, Integer marketType);

    /**
     * 修改PA状态
     *
     * @param marketId
     * @param paStatus PA状态
     * @return
     */
    boolean updatePaStatus(Long marketId, Integer paStatus);

    /**
     * 根据赛事ID和玩法ID查询盘口ID
     *
     * @param matchId
     * @param playId
     * @return
     */
    List<Long> getMarketIdList(Long matchId, Long playId);

    /**
     * 获取子玩法ID
     *
     * @param matchId
     * @param playIds
     * @return
     */
    Map<Long, List<Long>> getSubPlayId(Long matchId, Collection<Long> playIds);
}