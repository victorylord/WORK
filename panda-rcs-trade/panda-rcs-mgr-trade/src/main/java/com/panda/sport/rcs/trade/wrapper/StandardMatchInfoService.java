package com.panda.sport.rcs.trade.wrapper;


import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.vo.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-09-03
 */
public interface StandardMatchInfoService extends IService<StandardMatchInfo> {

    /**
     * @return java.util.List<com.panda.sport.rcs.vo.TournamentVoBySport>
     * @Description //根据条件获取列表
     * @Param [sportId]
     * @Author kimi
     * @Date 2019/11/29
     **/
    List<TournamentVoBySport> getTournamentList(Long sportId, Long beginTime, Long endTime,Integer type);

    /**
     * 组合条件查询数据库赛事数据
     *
     * @param marketLiveOddsQueryVo
     * @return
     */
    List<StandardMatchInfo> queryMatches(MarketLiveOddsQueryVo marketLiveOddsQueryVo);

    List<Long> selectByMap(Map<String, Object> map);

    /***
     * 获取同联赛赛事列表
     * @param tournamentId 赛事ID
     * @return
     */
    List<TournamentMatchInfoVo> selectMacthInfo(long tournamentId,String dateTime);


    /**
     * @return java.util.List<com.panda.sport.rcs.vo.TeamVo>
     * @Description //根据赛事id获取战队名字
     * @Param [matchId]
     * @Author kimi
     * @Date 2020/1/11
     **/
    List<TeamVo> selectTeamNameByMatchId(Long matchId);

    /**
     * @return java.lang.Integer
     * @Description //获取滚球数量
     * @Param []
     * @Author kimi
     * @Date 2020/1/13
     **/
    Integer getGrounderNumber();
    /**
     * @Description   根据联赛id查询赛事对阵
     * @Param [map]
     * @Author  Sean
     * @Date  11:29 2020/2/3
     * @return java.util.List<com.panda.sport.rcs.vo.BaseMatchInfoVo>
     **/
    List<BaseMatchInfoVo> queryMatchsByTournamentList(Map<String, Object> map);
    /**
     * @Description   //更加联赛id和操盘类型查询赛事
     * @Param [map]
     * @Author  Sean
     * @Date  10:37 2020/6/12
     * @return java.util.List<com.panda.sport.rcs.vo.BaseMatchInfoVo>
     **/
    List<BaseMatchInfoVo> queryBetMatchsByTournamentList(Map<String, Object> map);
    /**
     * @Description   查询手动操盘的赛事，先查赛事配置再查联赛配置
     * @Param [map]
     * @Author  Sean
     * @Date  11:30 2020/2/3
     * @return java.util.List<com.panda.sport.rcs.vo.BaseMatchInfoVo>
     **/
    List<BaseMatchInfoVo> queryManualTradeMatch(Map<String, Object> map);

    /**
     * @return com.panda.sport.rcs.vo.TournamentMatchInfoVo
     * @Description //根据主键查找
     * @Param [id]
     * @Author kimi
     * @Date 2020/2/1
     **/
    StandardMatchInfo selectById(Long id);
    /**
     * @Description   查询联赛
     * @Param [map]
     * @Author  Sean
     * @Date  12:21 2020/2/5
     * @return java.util.List<com.panda.sport.rcs.vo.BaseMatchInfoVo>
     **/
    List<BaseMatchInfoVo> queryTournamentList(Map<String, Object> map);
    /**
     * @Description   //查询实时注单所有的联赛名称
     * @Param [map]
     * @Author  Sean
     * @Date  16:35 2020/6/11
     * @return java.util.List<com.panda.sport.rcs.vo.BaseMatchInfoVo>
     **/
    List<BaseMatchInfoVo> queryBetTournamentList(Map<String, Object> map);

    Map<Long,Integer> queryOddLiveMap (List<Long> matchIds);

    /**
     * 根据赛事ID查询赛事状态
     * @Author Kir
     * @param id
     * @return
     * @Date 2021/1/3
     */
    Integer selectMatchStatusById(@Param("id") Long id);

    /**
     * 获取赛事账务日
     *
     * @param matchId
     * @return
     * @author Paca
     */
    String getMatchDateExpect(Long matchId);

}
