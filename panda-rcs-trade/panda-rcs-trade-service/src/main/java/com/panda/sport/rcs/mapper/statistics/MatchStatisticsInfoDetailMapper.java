package com.panda.sport.rcs.mapper.statistics;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.MatchStatisticsInfoDetail;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author :  myname
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.mapper.statistics
 * @Description :  TODO
 * @Date: 2020-03-02 21:45
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Repository
public interface MatchStatisticsInfoDetailMapper extends BaseMapper<MatchStatisticsInfoDetail> {

    @Select("SELECT * from match_statistics_info_detail WHERE `code` ='red_card_score' and standard_match_id= #{matchId} ORDER BY update_time DESC LIMIT 1")
    MatchStatisticsInfoDetail selectRedScore(@Param("matchId") Long matchId);

    @Select("SELECT sum(t1) t1 ,sum(t2) t2 from match_statistics_info_detail t WHERE `code` ='set_score' and standard_match_id=#{matchId} AND first_num IN (1,2)")
    MatchStatisticsInfoDetail selectMatchScore(@Param("matchId") Long matchId);
    /**
     * @Description   //跟玩法类型获取比分
     * @Param [marketConfig, stageValue]
     * @Author  sean
     * @Date   2020/11/21
     * @return java.lang.String
     **/
    @Select("SELECT concat(t1,\":\",t2) from match_statistics_info_detail t WHERE standard_match_id=#{matchId}\n" +
            "AND t.`code` = CASE WHEN #{stage} IN (1,2,3,4) THEN 'set_score' WHEN #{stage} IN (5,6) THEN 'period_score' WHEN #{stage}=7 THEN 'match_score' END \n" +
            "AND t.first_num = CASE WHEN #{stage} IN (1,2,3,4) THEN #{stage} WHEN #{stage}=5 THEN 1 WHEN #{stage}=6 THEN 2 WHEN #{stage}=7 THEN 0 END LIMIT 1")
    String selectScoreByMatchStage(@Param("matchId") Long matchId,@Param("stage") Integer stageValue);

    @Select("SELECT concat(t1,\":\",t2) from match_statistics_info_detail t WHERE `code` = 'match_score' and standard_match_id = #{matchId} ORDER BY update_time DESC LIMIT 1")
    String queryMatchScore(@Param("matchId") Long matchId);
    
    /**
     * 查询赛事比分
     * @param matchId
     * @return
     */
    @Select("SELECT t1,t2 FROM match_statistics_info_detail WHERE standard_match_id=#{matchId} AND `code`=\"match_score\"")
    MatchStatisticsInfoDetail selectScore(@Param("matchId") Integer matchId);


    /**
     * 查询赛事比分
     * @param matchId
     * @return
     */
    @Select("SELECT * from match_statistics_info_detail t WHERE `code` like '%score' and standard_match_id = #{matchId} ")
    List<MatchStatisticsInfoDetail> selectScoreTotal(@Param("matchId") Integer matchId);

    /**
     * @Description   //获取网球比分
     * @Param [map]
     * @Author  sean
     * @Date   2021/9/28
     * @return java.lang.String
     **/
    String selectTennisScoreByPlayId(Map<String, Long> map);

    /**
     * 根据code查询15分钟比分
     * @param matchId
     * @return
     */
    List<MatchStatisticsInfoDetail> selectScoreByCode(@Param("matchId") Long matchId,@Param("code") String code);

}
