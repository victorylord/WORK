package com.panda.sport.rcs.mapper.tourTemplate;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplate;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargain;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargainRef;
import com.panda.sport.rcs.pojo.vo.TournamentTemplatePlayMargainVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @Description 联赛玩法margin配置表
 * @Param
 * @Author toney
 * @Date 20:02 2020/5/10
 * @return
 **/
@Repository
public interface RcsTournamentTemplatePlayMargainMapper extends BaseMapper<RcsTournamentTemplatePlayMargain> {

    /**
     * 根据赛种，获取关闭的玩法
     *
     * @param sportId
     * @return
     */
    List<Integer> listClosePlayIdBySportId(@Param("sportId") Integer sportId);

    /**
     * 获取有效的玩法
     *
     * @param sportId
     * @return
     */
    List<RcsTournamentTemplatePlayMargain> listPlayIdBySportId(@Param("sportId") Integer sportId);

    /**
     * 插入玩法参数配置数据
     *
     * @param list
     * @return
     */
    int insertBatch(List<RcsTournamentTemplatePlayMargain> list);

    /**
     * 获取联赛配置在玩法集下的开售玩法
     *
     * @param map
     * @return
     */
    List<RcsTournamentTemplatePlayMargain> searchSellPlay(Map<String, Object> map);

    /**
     * 获取联赛未配置玩法集下的开售玩法
     *
     * @param map
     * @return
     */
    List<RcsTournamentTemplatePlayMargain> searchOtherSellPlay(Map<String, Object> map);

    /**
     * 根据id修改玩法参数数据
     *
     * @param param
     * @return
     */
    int updatePlayMargainById(RcsTournamentTemplatePlayMargain param);

    /**
     * @return com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargainRef
     * @Description //根据玩法id，赛事id，赛事阶段查询margin和最大可投注金额
     * @Param [rcsMatchMarketConfig]
     * @Author Sean
     * @Date 15:42 2020/9/23
     **/
    RcsTournamentTemplatePlayMargainRef queryMarginByPlayId(@Param("config") RcsMatchMarketConfig rcsMatchMarketConfig);
    /**
     * @Description   //查询联赛配置
     * @Param [rcsMatchMarketConfig]
     * @Author  sean
     * @Date   2020/12/20
     * @return com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargainRef
     **/
    RcsTournamentTemplatePlayMargainRef queryMatchConfig(@Param("config") RcsMatchMarketConfig rcsMatchMarketConfig,@Param("sportId") Integer sportId);
    /**
     * @Description   //查询联赛盘口差和赔率变化配置
     * @Param [rcsMatchMarketConfig]
     * @Author  Sean
     * @Date  9:48 2020/10/11
     * @return com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargainRef
     **/
    RcsTournamentTemplatePlayMargain queryTournamentAdjustRangeByPlayId(@Param("config") RcsMatchMarketConfig rcsMatchMarketConfig);

    
    /**
     * 根据玩法id，修改赛事模板是否开售
     *
     * @param param
     * @return
     */
    int updatePlayMargainIsSellByPlayId(@Param("param") RcsTournamentTemplate param, @Param("playIds") List<Long> playIds);

    /**
     * 根据玩法id，修改赛事模板是否开售
     *
     * @param param
     * @return
     */
    int closeAllPlaysSell(@Param("param") RcsTournamentTemplate param);

    /**
     * 获取赛事玩法模板配置
     *
     * @param matchId 赛事ID
     * @param playId  玩法ID
     * @return
     * @author Paca
     */
    RcsTournamentTemplatePlayMargain getMatchPlayTemplateConfig(@Param("matchId") Long matchId, @Param("playId") Integer playId);

    /**
     *查询多个赛事下面的玩法数据源
     * @param matchIds
     * @return
     */
    List<TournamentTemplatePlayMargainVo> queryDataSource(@Param("matchIds")List<Long> matchIds);

    /**
     * @Description   //查询足球分时配置
     * @Param [config, sportId]
     * @Author  sean
     * @Date   2021/2/23
     * @return com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargainRef
     **/
    RcsTournamentTemplatePlayMargainRef queryFootballMatchConfig(@Param("config")RcsMatchMarketConfig config);
    /**
     * @Description   //查询网球
     * @Param [config]
     * @Author  sean
     * @Date   2021/9/16
     * @return com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargainRef
     **/
    RcsTournamentTemplatePlayMargainRef selectAllTemplatesByTennis(@Param("config")RcsMatchMarketConfig config);
    /**
     * @Description   //查询冰球
     * @Param [config]
     * @Author  sean
     * @Date   2021/9/16
     * @return com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargainRef
     **/
    RcsTournamentTemplatePlayMargainRef selectAllTemplatesByIce(@Param("config")RcsMatchMarketConfig config);
    /**
     * @Description   //查询羽毛球
     * @Param [config]
     * @Author  sean
     * @Date   2021/9/16
     * @return com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargainRef
     **/
    RcsTournamentTemplatePlayMargainRef selectAllTemplatesByBadminton(@Param("config")RcsMatchMarketConfig config);
    /**
     * 根据模板id批量修改玩法参数数据
     *
     * @param param
     * @return
     */
    int updatePlayMarginByTemplateId(RcsTournamentTemplatePlayMargain param);
    /**
     * @Description   //查找snooker联赛模板
     * @Param [config]
     * @Author  sean
     * @Date   2022/1/25
     * @return com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargainRef
     **/
    RcsTournamentTemplatePlayMargainRef selectAllTemplatesBySnooker(@Param("config")RcsMatchMarketConfig config);
    /**
     * @Description   //获取棒球模板
     * @Param [config]
     * @Author  sean
     * @Date   2022/3/21
     * @return com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargainRef
     **/
    RcsTournamentTemplatePlayMargainRef selectAllTemplatesByBaseBall(@Param("config")RcsMatchMarketConfig config);
}