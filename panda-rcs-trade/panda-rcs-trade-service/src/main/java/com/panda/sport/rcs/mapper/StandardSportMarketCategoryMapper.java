package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.*;
import com.panda.sport.rcs.vo.CategoryTemplateVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标准玩法表 Mapper 接口
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-09-03
 */
@Repository
public interface StandardSportMarketCategoryMapper extends BaseMapper<StandardSportMarketCategory> {

//    Map<String, Object> getMatchMarketConfig(Map<String, Object> params);

    Map<String, Object> getTournamentConfig(Map<String, Object> params);

    List<Map<String, Object>> queryOddsListByMarketId(Map<String, Object> params);

    RcsMatchMarketConfig queryRcsMatchMarketConfig(RcsMatchMarketConfig config);

//    RcsMatchMarketConfig queryRcsMatchMarketConfigByPlayId(RcsMatchMarketConfig config);

//    int updateMatchMarketConfig(RcsMatchMarketConfig config);

    int saveupdateTournamentConfig(RcsTournamentMarketConfig config);

    Map<String, Object> getTournamentConfigByMatchId(Map<String, Object> params);

    StandardSportMarketCategory queryCategoryInfoByMap(Map<String, Object> map);

    List<StandardSportMarketCategory> queryCategoryList(String sportId);

    /**
     * 查询足球全部玩法
     * @return
     */
    List<StandardSportMarketCategoryRefReqVo> findMarketCategoryListForSoccer();

    List<CategoryTemplateVo> queryCategoryTemplate(@Param("sportId") Long sportId);

    List<Long> queryCategoryIds(@Param("sportId") Long sportId,@Param("list") List<Long> categoryIds);

    List<Long>  selectPlayIdByTheirTime(@Param("matchStage") Long matchStage);

    /**
     * 	根据ids查询多个玩法
     * 
     * @param ids
     * @param sportId
     * @return
     */
    List<StandardSportMarketCategory> queryCategoryInfoByIds(@Param("ids") List<Long> ids, @Param("sportId") Long sportId, @Param("status") Integer status);
}
