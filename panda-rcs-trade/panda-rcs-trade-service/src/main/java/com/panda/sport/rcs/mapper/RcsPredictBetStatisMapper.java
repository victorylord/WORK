package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.vo.statistics.ActualVolumeVO;
import com.panda.sport.rcs.vo.statistics.RcsPredictBetStatisVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 多语言 Mapper 接口
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-09-03
 */
@Repository
public interface RcsPredictBetStatisMapper extends BaseMapper<RcsPredictBetStatisVo> {
    /**
     * @Description   根据赛事查询玩法级别货量分布
     * @Param [bet]
     * @Author  Sean
     * @Date  11:55 2020/7/22
     * @return java.util.List<com.panda.sport.rcs.vo.statistics.RcsPredictBetStatisVo>
     **/
    List<RcsPredictBetStatisVo> queryBetPlayStatis(@Param("bet") RcsPredictBetStatisVo bet);
    /**
     * @Description   //根据赛事阶段查询
     * @Param [matchId, playTimeStages, matchType]
     * @Author  kimi
     * @Date   2020/10/7
     * @return java.util.List<com.panda.sport.rcs.vo.statistics.RcsPredictBetStatisVo>
     **/
    List<ActualVolumeVO> getRcsPredictBetStatisVo1(@Param("matchId")Integer matchId, @Param("playIds")List<Integer> playIds,
                                                  @Param("matchType")Integer matchType);
    /**
     * @Description   //根据赛事阶段查询
     * @Param [matchId, playTimeStages, matchType]
     * @Author  kimi
     * @Date   2020/10/7
     * @return java.util.List<com.panda.sport.rcs.vo.statistics.RcsPredictBetStatisVo>
     **/
    List<ActualVolumeVO> getRcsPredictBetStatisVo2(@Param("matchId")Integer matchId, @Param("playIds")List<Integer> playIds,
                                                   @Param("matchType")Integer matchType);
    /**
     * @Description   //根据赛事阶段查询
     * @Param [matchId, playTimeStages, matchType]
     * @Author  kimi
     * @Date   2020/10/7
     * @return java.util.List<com.panda.sport.rcs.vo.statistics.RcsPredictBetStatisVo>
     **/
    List<ActualVolumeVO> getRcsPredictBetStatisVo(@Param("matchId")Integer matchId, @Param("playIds")List<Integer> playIds,
                                                   @Param("matchType")Integer matchType);
}
