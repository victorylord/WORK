package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.RcsMatchMarketMarginConfig;
import com.panda.sport.rcs.pojo.RcsMatchMarketProbabilityConfig;
import com.panda.sport.rcs.pojo.StandardSportMarket;
import com.panda.sport.rcs.pojo.config.BuildMarketPlaceConfig;
import com.panda.sport.rcs.pojo.config.BuildMarketPlayConfig;
import com.panda.sport.rcs.pojo.config.MarketBuildConfig;
import com.panda.sport.rcs.pojo.config.MarketBuildPlayConfig;
import com.panda.sport.rcs.pojo.dto.ClearSubDTO;
import com.panda.sport.rcs.pojo.dto.odds.MatchMarketPlaceConfig;
import com.panda.sport.rcs.pojo.dto.odds.RcsStandardMarketDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>
 * 赛事设置表 Mapper 接口
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-10-24
 */
@Component
public interface RcsMatchMarketConfigMapper extends BaseMapper<RcsMatchMarketConfig> {
    /**
     * @return com.panda.sport.rcs.pojo.RcsMatchMarketConfig
     * @Description //通过赛事id、玩法id和位置更新盘口id
     * @Param [rcsMatchMarketConfig]
     * @Author Sean
     * @Date 11:11 2020/6/28
     **/
    int initAndUpdateMatchMarketIdByIndex(@Param("config") RcsMatchMarketConfig rcsMatchMarketConfig);
//

    /**
     * 操盘优化需求 改版
     *
     * @param params
     */
    void updateMarketConfigByMarketId(RcsMatchMarketConfig params);

    void updateMarketConfigByIndex(RcsMatchMarketConfig params);

    /**
     * @return java.util.List<com.panda.sport.rcs.pojo.RcsMatchMarketConfig>
     * @Description //根据盘口id查询已经配置过的盘口
     * @Param [ids]
     * @Author Sean
     * @Date 15:38 2020/9/18
     **/
    List<RcsMatchMarketConfig> selectMarketListByIds(@Param("ids") List<Long> ids);

    /**
     * @return void
     * @Description //清空盘口配置
     * @Param [ids]
     * @Author Sean
     * @Date 11:18 2020/8/14
     **/
    void clearMatchMarketConfig(@Param("ids") List<Long> ids);

    /**
     * @return java.util.List<com.panda.sport.rcs.pojo.RcsMatchMarketConfig>
     * @Description //查询未及时同步赛事信息的数据
     * @Param []
     * @Author Sean
     * @Date 10:40 2020/10/3
     **/
    List<RcsMatchMarketConfig> selectIncompleteList();

    /**
     * @return void
     * @Description //更新配置状态
     * @Param [config]
     * @Author Sean
     * @Date 11:58 2020/10/3
     **/
    void updateActive(@Param("config") RcsMatchMarketConfig config);

    /**
     * @return void
     * @Description //兼容并发情况，保存或者修改盘口配置表
     * @Param [config]
     * @Author Sean
     * @Date 20:24 2020/9/13
     **/
    void insertOrUpdateMarketConfig(@Param("config") RcsMatchMarketConfig config);

    /**
     * @return void
     * @Description //兼容并发情况，保存或者修改盘口配置水差和margin表
     * @Param [config]
     * @Author Sean
     * @Date 20:24 2020/9/13
     **/
    void insertOrUpdateMarketMarginConfig(@Param("config") RcsMatchMarketConfig config);

    /**
     * @return com.panda.sport.rcs.pojo.RcsMatchMarketConfig
     * @Description //篮球操盘需求查询盘口配置
     * @Param [rcsMatchMarketConfig]
     * @Author Sean
     * @Date 15:53 2020/10/2
     **/
    RcsMatchMarketConfig queryMatchMarketConfig(@Param("config") RcsMatchMarketConfig config);

    /**
     * @return com.panda.sport.rcs.pojo.RcsMatchMarketConfig
     * @Description //配置表修改盘口id
     * @Param [config]
     * @Author Sean
     * @Date 14:57 2020/10/16
     **/
    Integer updateMatchMarketConfig(@Param("config") RcsMatchMarketConfig config);

    /**
     * @return com.panda.sport.rcs.pojo.RcsMatchMarketConfig
     * @Description //修改水差配置表盘口id
     * @Param [config]
     * @Author Sean
     * @Date 14:58 2020/10/16
     **/
    Integer updateMatchMarketMarginConfig(@Param("config") RcsMatchMarketConfig config);

    /**
     * @return java.lang.Integer
     * @Description //根据赛事更新水差
     * @Param [standardMatchInfo]
     * @Author sean
     * @Date 2020/12/12
     **/
    void updateMatchMarketMarginConfigByMatch(@Param("match") StandardSportMarket market);
    /**
     * @return java.lang.Integer
     * @Description //根据赛事更新水差
     * @Param [standardMatchInfo]
     * @Author sean
     * @Date 2020/12/12
     **/
    int updateMatchWaterConfigByMatch(@Param("matchId") String matchId);

    /**
     * @return java.util.List<com.panda.sport.rcs.pojo.RcsMatchMarketConfig>
     * @Description //根据盘口值获取水差
     * @Param [standardMarketDTOList]
     * @Author Sean
     * @Date 17:48 2020/10/23
     **/
    List<RcsMatchMarketConfig> queryAwayAutoChangeRate(@Param("list") List<RcsStandardMarketDTO> standardMarketDTOList, @Param("config") RcsMatchMarketConfig config);

    /**
     * @return com.panda.sport.rcs.pojo.RcsMatchMarketMarginConfig
     * @Description //查询margin和水差
     * @Param [config]
     * @Author sean
     * @Date 2020/12/3
     **/
    RcsMatchMarketMarginConfig queryMarginAndRatio(@Param("config") RcsMatchMarketConfig config);

    /**
     * @return void
     * @Description //根据赛事id和玩法批量清空水差
     * @Param [list]
     * @Author sean
     * @Date 2021/1/9
     **/
    int clearMarketDiffByMatchAndPlay(@Param("list") List<ClearSubDTO> list);

    int updatePlaceConfig(@Param("config") RcsMatchMarketConfig config);

    /**
     * 查询盘口构建位置配置
     *
     * @param matchId
     * @param playId
     * @return
     * @author Paca
     */
    List<MarketBuildConfig> listMarketBuildConfig(@Param("matchId") Long matchId, @Param("playId") Long playId);

    /**
     * 查询子玩法表盘口构建位置配置
     * @param matchId
     * @param playId
     * @return
     */
    List<MarketBuildConfig> listMarketBuildSubConfig(@Param("matchId") Long matchId, @Param("playId") Long playId);

    /**
     * 查询盘口构建玩法配置
     *
     * @param matchId
     * @param playId
     * @return
     */
    MarketBuildPlayConfig queryMarketBuildPlayConfig(@Param("matchId") Long matchId, @Param("playId") Integer playId);

    /**
     * 查询玩法是否开售
     *
     * @param matchId
     * @param playId
     * @return
     */
    Integer queryPlayIsSell(@Param("matchId") Long matchId, @Param("playId") Integer playId, @Param("oddsLive") Integer oddsLive);

    /**
     * 查询L模式开售玩法
     *
     * @param matchId
     * @param matchType
     * @return
     */
    List<Integer> queryLinkageSellPlay(@Param("matchId") Long matchId, @Param("matchType") Integer matchType);

    /**
     * @return void
     * @Description //修复位置水差
     * @Param [list]
     * @Author sean
     * @Date 2021/1/24
     **/
    Integer updateMatchMarketWaters(@Param("config") RcsMatchMarketConfig config, @Param("list") List<MatchMarketPlaceConfig> list);

    /**
     * @return java.lang.Integer
     * @Description //清楚篮球位置水差
     * @Param [standardMatchInfo]
     * @Author sean
     * @Date 2021/1/26
     **/
    Integer updateMatchMarketConfigByMatch(@Param("match") StandardSportMarket market);
    /**
     * @Description   //更新子玩法水差
     * @Param [config, placeConfigs]
     * @Author  sean
     * @Date   2021/7/29
     * @return void
     **/
    int updateMatchMarketSubWaters(@Param("config")RcsMatchMarketConfig config, @Param("list")List<MatchMarketPlaceConfig> placeConfigs);
    /**
     * @Description   //更新子玩法水差
     * @Param [config, placeConfigs]
     * @Author  sean
     * @Date   2021/7/29
     * @return void
     **/
    int updateMatchMarketSubWater(@Param("config")RcsMatchMarketConfig config);
    /**
     * @Description   //初始化配置
     * @Param [config]
     * @Author  sean
     * @Date   2021/8/5
     * @return void
     **/
    void initMarketConfig(@Param("config")RcsMatchMarketConfig config);

    /**
     * 获取构建盘口位置配置
     *
     * @param matchId
     * @param playId
     * @return
     */
    List<BuildMarketPlaceConfig> getBuildMarketPlaceConfig(@Param("matchId") Long matchId, @Param("playId") Long playId);

    /**
     * 获取盘口差
     *
     * @param matchId
     * @param playId
     * @return
     */
    BuildMarketPlayConfig getMarketHeadGap(@Param("matchId") Long matchId, @Param("playId") Long playId);

    /**
     * 获取最大最小赔率
     *
     * @param matchId
     * @param playId
     * @return
     */
    RcsMatchMarketConfig getMaxAndMinOddsValue(@Param("matchId") Long matchId, @Param("playId") Long playId);

    RcsMatchMarketConfig getSubMaxAndMinOddsValue(@Param("matchId") Long matchId, @Param("playId") Long playId);
}
