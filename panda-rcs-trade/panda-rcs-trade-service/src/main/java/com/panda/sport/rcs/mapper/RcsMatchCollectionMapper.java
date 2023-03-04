package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsMatchCollection;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author :  myname
 * @Project Name :  daima1
 * @Package Name :  com.panda.sport.rcs.mapper
 * @Description :  TODO
 * @Date: 2019-10-25 14:30
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Service
public interface RcsMatchCollectionMapper extends BaseMapper<RcsMatchCollection> {
    //清除收藏
    int taskCleantaskCleanCollection();

    int batchInsertOrUpdate(@Param("list") List<RcsMatchCollection> list);
    
    List<RcsMatchCollection> selectListToCount(@Param("bean")RcsMatchCollection matchCollection);

    List<RcsMatchCollection> selectListTotournament(@Param("bean")RcsMatchCollection matchCollection);

    int insertOrUpdate(RcsMatchCollection record);

    @Select("SELECT *  FROM rcs_match_collection rmc1 LEFT JOIN rcs_match_collection rmc2 ON rmc1.tournament_id = rmc2.tournament_id  WHERE (rmc1.type = 1 or rmc2.type = 2) AND rmc1.match_id =#{matchId} and rmc1.user_id = #{userId}")
    List<RcsMatchCollection> selectListByUnInTournament(@Param("matchId") Long matchId,@Param("userId") Long userId);

    @Select("SELECT 1 FROM rcs_match_collection rmc WHERE rmc.user_id = #{userId} AND rmc.match_id = #{matchId} and rmc.`status` = 1 LIMIT 1")
    Integer queryFavoriteStatus(@Param("userId") Long userId,@Param("matchId") Long matchId ,@Param("beginTime") Long beginTime);

    @Select("SELECT * FROM standard_match_info WHERE standard_tournament_id IN ( SELECT tournament_id FROM rcs_match_collection WHERE user_id = #{userId} AND type = 2 AND " +
        "sport_id = #{sportId}" + " )")
    List<Long> selectStandardMatchInfoId(@Param("userId") Integer userId,@Param("sportId") Integer sportId);    List<RcsMatchCollection> queryMatchColls(@Param("bean")RcsMatchCollection matchCollection);

	int updateByMatchId(RcsMatchCollection rcsMatchCollection);
	
	int updateByTournamentId(RcsMatchCollection rcsMatchCollection);

}
