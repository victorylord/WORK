package com.panda.sport.rcs.mapper.tourTemplate;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateAcceptConfigNew;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateAcceptEvent;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateAcceptEventSettle;
import feign.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Description   联赛模板事件从表
 * @Param 
 * @Author  toney
 * @Date  20:02 2020/5/10
 * @return 
 **/
@Repository
public interface RcsTournamentTemplateAcceptEventMapper extends BaseMapper<RcsTournamentTemplateAcceptEvent> {
    /**
     * @Description   //根据赛事查询接拒单时间和数据源配置
     * @Param [config]
     * @Author  Sean
     * @Date  11:26 2020/9/5
     * @return com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateAcceptConfig
     **/
    List<RcsTournamentTemplateAcceptEvent> queryMatchEventConfig(@org.apache.ibatis.annotations.Param("config") RcsTournamentTemplateAcceptEvent config);
    /**
     * @Description   //更新事件状态
     * @Param [config]
     * @Author  Sean
     * @Date  16:12 2020/9/5
     * @return void
     **/
    void updateMatchEventConfig(@org.apache.ibatis.annotations.Param("list") List<RcsTournamentTemplateAcceptEvent> config);

    /**
     * @Description   //根据赛事查询接拒单事件配置
     * @Param [config]
     * @Author  carver
     * @Date  11:26 2021/1/15
     * @return com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateAcceptConfig
     **/
    List<RcsTournamentTemplateAcceptEvent> queryEventByMatchId(@org.apache.ibatis.annotations.Param("matchId") Long matchId);
    /**
     * @Description   //根据赛事查询接拒单时间和数据源配置
     * @Param [config]
     * @Author  Sean
     * @Date  11:26 2020/9/5
     * @return com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateAcceptConfig
     **/
    List<RcsTournamentTemplateAcceptEventSettle> queryMatchEventConfigSettle(@org.apache.ibatis.annotations.Param("config") RcsTournamentTemplateAcceptEventSettle config);

    List<RcsTournamentTemplateAcceptConfigNew> selectOrderAcceptConfig(@org.apache.ibatis.annotations.Param("matchId") Long matchId);
}