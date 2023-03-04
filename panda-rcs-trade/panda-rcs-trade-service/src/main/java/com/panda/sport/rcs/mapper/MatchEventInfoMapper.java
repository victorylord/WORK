package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.MatchEventInfo;
import com.panda.sport.rcs.vo.CustomizedEventBeanVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @ClassName MatchEventInfoMapper
 * @Description: TODO
 * @Author Vector
 * @Date 2019/10/10
 **/
@Repository
public interface MatchEventInfoMapper extends BaseMapper<MatchEventInfo> {

    int updateBatch(List<MatchEventInfo> list);

    int batchInsert(@Param("list") List<MatchEventInfo> list);

    int insertOrUpdate(MatchEventInfo record);

    List<CustomizedEventBeanVo> selectMatchEventInfoByMatchId(@Param("matchId") Long matchId, @Param("dataSource") String dataSource, @Param("eventTime") Long eventTime, @Param("list") List<Integer> eventTypes, @Param("sort") Integer sort, @Param("limit") Integer limit);

    List<CustomizedEventBeanVo> selectMatchEventInfoByMatchIdByFootball(@Param("matchId") Long matchId, @Param("dataSource") String dataSource, @Param("eventTime") Long eventTime, @Param("list") List<Integer> eventTypes, @Param("sort") Integer sort, @Param("limit") Integer limit ,@Param("unFilterEvents") List<String> unFilterEvents);

    List<MatchEventInfo> selectMatchEventInfoSocre(@Param("matchId") Long matchId,@Param("dataSource") String dataSource,@Param("sportId")Integer sportId);
}