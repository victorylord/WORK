package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfigSub;
import com.panda.sport.rcs.pojo.RcsMatchMarketMarginConfig;
import com.panda.sport.rcs.pojo.StandardSportMarket;
import com.panda.sport.rcs.pojo.config.BuildMarketConfigDto;
import com.panda.sport.rcs.pojo.dto.odds.MatchMarketPlaceConfig;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 赛事设置表 服务类
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-10-24
 */
public interface IRcsMatchMarketConfigService extends IService<RcsMatchMarketConfig> {

    /**
     * 获取最大最小赔率
     * @return
     */
    RcsMatchMarketConfig getMaxAndMinOddsValue(Long matchId, Long playId);

    /**
     * 获取盘口差
     * @param matchId
     * @param playId
     * @return
     */
    BigDecimal getMarketHeadGap(Long matchId, Long playId);

    /**
     * @return void
     * @Description //插入操作
     * @Param [rcsMatchMarketConfig]
     * @Author kimi
     * @Date 2019/11/9
     **/
    void insert(RcsMatchMarketConfig rcsMatchMarketConfig);

    /**
     * @Description   //查询盘口配置
     * @Param [rcsMatchMarketConfig]
     * @Author  sean
     * @Date   2021/1/9
     * @return com.panda.sport.rcs.pojo.RcsMatchMarketConfig
     **/
    RcsMatchMarketConfig selectRcsMatchMarketConfig(RcsMatchMarketConfig rcsMatchMarketConfig);

    /**
     * @Description   //盘口位置变化发送margin变化和最大最小赔率到融合
     * @Param [rcsMatchMarketConfig]
     * @Author  Sean
     * @Date  9:16 2020/6/30
     * @return void
     **/
//    void updateMarketConfigFromDataCenter(List<MatchMarketLiveOddsVo.MatchMarketVo> msg, List<RcsMatchMarketConfig> configs, List<Long> ids, StandardSportMarket market);
    /**
     * @Description   根据盘口信息查询盘口配置--操盘1.6新需求
     * @Param [rcsMatchMarketConfig]
     * @Author  Sean
     * @Date  17:21 2020/6/26
     * @return com.panda.sport.rcs.pojo.RcsMatchMarketConfig
     **/
    RcsMatchMarketConfig queryMatchMarketConfigNew(RcsMatchMarketConfig rcsMatchMarketConfig);
    /**
     * @Description   //查询当前比分
     * @Param [matchId, playId]
     * @Author  Sean
     * @Date  16:01 2020/9/18
     * @return java.lang.Integer
     **/
    Integer queryCurrentTypeScore(Long matchId, Long playId);

    Map<Long, Map<Integer, RcsMatchMarketConfig>> queryConfigs(Long matchId);
    /**
     * @Description   //根据位置获取盘口配置
     * @Param [config]
     * @Author  sean
     * @Date   2021/1/9
     * @return com.panda.sport.rcs.pojo.RcsMatchMarketConfig
     **/
    RcsMatchMarketConfig getRcsMatchMarketConfig(RcsMatchMarketConfig config);
    /**
     * @Description   //查询赛事模板配置
     * @Param [config]
     * @Author  sean
     * @Date   2021/1/9
     * @return com.panda.sport.rcs.pojo.RcsMatchMarketConfig
     **/
    RcsMatchMarketConfig getRcsMatchMarketConfigByConfig(RcsMatchMarketConfig config,Integer sportId);
    /**
     * @Description   //设置最大可投金额
     * @Param [marketConfig]
     * @Author  sean
     * @Date   2021/1/9
     * @return java.math.BigDecimal
     **/
    BigDecimal getBetMax(RcsMatchMarketConfig marketConfig);
    /**
     * @Description   //获取玩法对应的比分
     * @Param [marketConfig]
     * @Author  sean
     * @Date   2021/1/9
     * @return java.lang.String
     **/
    String getScoreByPlayId(RcsMatchMarketConfig marketConfig);
    /**
     * @Description   //根据不同球类获取水差
     * @Param [config]
     * @Author  sean
     * @Date   2021/1/10
     * @return com.panda.sport.rcs.pojo.RcsMatchMarketMarginConfig
     **/
    RcsMatchMarketMarginConfig getMarketWaterDiff(RcsMatchMarketConfig config);
    /**
     * @Description   //获取盘口配置
     * @Param [config]
     * @Author  sean
     * @Date   2021/1/10
     * @return com.panda.sport.rcs.pojo.RcsMatchMarketMarginConfig
     **/
    RcsMatchMarketMarginConfig getRcsMatchMarketMarginConfig(RcsMatchMarketConfig config);
    /**
     * @Description   //获取篮球水差
     * @Param [config]
     * @Author  sean
     * @Date   2021/1/10
     * @return com.panda.sport.rcs.pojo.RcsMatchMarketMarginConfig
     **/
    RcsMatchMarketMarginConfig getBasketballWaterDiff(RcsMatchMarketConfig config);
    /**
     * @Description   //获取位置水差
     * @Param [config]
     * @Author  sean
     * @Date   2021/1/14
     * @return java.lang.String
     **/
    String queryPlaceWaterDiff(RcsMatchMarketConfig config);
    /**
     * @Description   //获取玩法和位置总水差
     * @Param [config]
     * @Author  sean
     * @Date   2021/1/15
     * @return java.util.List<com.panda.sport.rcs.pojo.dto.odds.MatchMarketPlaceConfig>
     **/
    List<MatchMarketPlaceConfig> queryPlaceAllWaterDiff(RcsMatchMarketConfig config,BigDecimal water,List<RcsMatchMarketConfig> list,Boolean isClear);
    /**
     * @Description   //清除
     * @Param [standardMatchInfo, ids]
     * @Author  sean
     * @Date   2021/1/17
     * @return void
     **/
    void clearConfig(StandardSportMarket market, Long sportId);
    /**
     * @Description   // 查询获取设置margin最大最小赔率等
     * @Param [config]
     * @Author  sean
     * @Date   2021/2/4
     * @return com.panda.sport.rcs.pojo.RcsMatchMarketConfig
     **/
    void getAndSetRcsMatchMarketConfig(RcsMatchMarketConfig config);

    /**
     * 获取构建盘口配置
     *
     * @param matchId
     * @param playId
     * @return
     * @author Paca
     */
    BuildMarketConfigDto getBuildMarketConfig(Long matchId, Long playId);

    /**
     * 清除水差
     *
     * @param matchId
     * @param playIds
     */
    void clearWaterDiff(Long matchId, Collection<Long> playIds);

    /**
     * 玩法是否开售
     *
     * @param matchId
     * @param playId
     * @return
     * @author Paca
     */
    boolean playIsSell(Long matchId, Long playId);

    /**
     * 查询L模式开售玩法
     *
     * @param matchId
     * @param matchType
     * @return
     */
    List<Long> queryLinkageSellPlay(Long matchId, Integer matchType);

    /**
     * 获取子玩法配置
     *
     * @param matchId
     * @param playId
     * @param placeNum
     * @return
     */
    Map<Long, RcsMatchMarketConfigSub> getSubPlayConfig(Long matchId, Long playId, Integer placeNum);

    void clearNewConfig(StandardSportMarket market, Long sportId);
}
