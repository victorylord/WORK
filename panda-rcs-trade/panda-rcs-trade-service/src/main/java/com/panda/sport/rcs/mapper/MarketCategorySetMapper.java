package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.panda.sport.rcs.pojo.RcsMarketCategorySet;
import com.panda.sport.rcs.pojo.RcsMarketCategorySetMargin;
import com.panda.sport.rcs.pojo.StandardSportMarketCategory;
import com.panda.sport.rcs.pojo.bo.FindMarketCategoryListAndNamesBO;
import com.panda.sport.rcs.vo.CategoryConVo;
import com.panda.sport.rcs.vo.RcsMarketCategorySetVo;
import com.panda.sport.rcs.vo.categoryset.CategorySetVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface MarketCategorySetMapper extends BaseMapper<RcsMarketCategorySet> {
    /**
     * 玩法集列表
     * @return
     */
    List<RcsMarketCategorySet> findCategorySetList(@Param("set") RcsMarketCategorySet set);
    /**
     * 分页玩法集列表
     * @param param
     * @return
     */
    IPage<RcsMarketCategorySet> findPageCategorySetList(Page<RcsMarketCategorySet> param, @Param("set") RcsMarketCategorySet set);

    /**
     * 编辑玩法集
     * @param rcsMarketCategorySet
     */
    void updateCategorySet (RcsMarketCategorySet rcsMarketCategorySet);

    /**
     * 新增玩法集
     * @param rcsMarketCategorySet
     */
    int addMarketCategorySet(RcsMarketCategorySet rcsMarketCategorySet);


    /**
     * 玩法列表
     * @return
     */
    List<StandardSportMarketCategory> findStandardSportMarketCategoryList(StandardSportMarketCategory standardSportMarketCategory);

    /**
     * 玩法内容
     * @param id
     * @return
     */
    List<StandardSportMarketCategory> findMarketCategoryContent(Integer id);
    /**
     * 从风控获取玩法id，然后再根据id查询panda库里的玩法
     * @param id
     * @return
     */
    List<Map<String,Object>> findPandaMarketCategoryId(@Param("id") Long id);

    /**
     * 玩法集下的玩法列表，用于玩法集列表下的二级目录
     * @param idList
     * @return
     */
    List<Map<String,Object>> findMarketCategoryList(List<Long> idList);

    /**
     * 玩法集下的玩法列表，用于玩法集列表下的二级目录,并得到国际化
     * @param idList
     * @return
     */
    List<FindMarketCategoryListAndNamesBO> findMarketCategoryListAndNames(@Param("sportId") Long sportId,@Param("list") List<Long> idList);
    /**
     * @Description   根据玩法id查询所在玩法集
     * @Param [id]
     * @Author  Sean
     * @Date  17:45 2019/11/9
     * @return com.panda.sport.rcs.pojo.RcsMarketCategorySet
     **/
    RcsMarketCategorySet findMarketCategoryListByPlayId(@Param("id") Integer id);

    /**
     * 查询出所有风控类型的玩法id，所在的玩法集id
     * @return
     */
    List<Map<String,Long>>  findWindControlTypeAll();

    /**
     * 查询当前玩法集下的玩法 是否已经存在其它风控型玩法集下了
     * @param id
     * @return
     */
    List<Map<String,Long>>  findIsExistWindControlSet(@Param("id") Integer id,@Param("sportId") Integer sportId);

    /*******1.0补丁版本*********/
    /**
     * 根据玩法集ID 移除玩法
     * @param id
     * @return
     */
    boolean deleteCategorySetRelation(@Param("id") Long id);

    /**
     * 根据玩法集ID 获取改玩法集下的所有玩法，然后获取玩法的国际话
     * @param id
     * @return
     */
    List<Map<String,Object>> findLanguageByCategorySetId(@Param("id") Long id);

    /**
     * 根据玩法集ID 获取改玩法集下的所有玩法，然后获取玩法的国际话
     * @param id
     * @return
     */
    List<Map<String,Object>> findLanguageByCategorySetId2(@Param("id") Long id);

    /**
     * 根据玩法ID 查询抽水
     * @param id
     * @return
     */
    List<RcsMarketCategorySetMargin> findMarginByPlayId(@Param("id") Long id,@Param("sportId") Long sportId);
    
    
	List<Map<String, Object>> queryAllCategorySetBySportId(Map<String, Object> params);

	List<Map<String, Object>> queryCategoryListBySetId(Map<String, Object> categorySet);

    List<Map<String, Object>> queryAllCategoryListBySportId(Map<String, Object> params);

    /**
     * 查询玩法集包含哪些玩法
     *
     * @param sportId
     * @return
     */
    List<CategoryConVo> selectCategoryCons(@Param("sportId") Long sportId);


    /**
     * 根据联赛ID，获取配置的玩法
     */
    List<CategorySetVo> queryAllCategorySetList(Map<String, Object> params);
    /**
     * 指派获取玩法集
     * @param sportId
     * @return
     */
    List<RcsMarketCategorySetVo> selectRcsMarketCategorySet(@Param("sportId") Long sportId);

    /**
     * 指派获取玩法集
     * @param sportId
     * @param type
     * @return
     */
    List<RcsMarketCategorySetVo> selectRcsMarketCategorySetByParam(@Param("sportId") Long sportId,@Param("type") Integer type);

    /**
     * 根据跳槽查询满足权限的玩法
     * @param sportId
     * @param matchId
     * @param matchType
     * @param userId
     * @return
     */
    List<Long> selectPlayIdList(@Param("sportId") Long sportId, @Param("matchId") Long matchId, @Param("matchType") Integer matchType, @Param("userId")Long userId);

    /**
     * 满足权限的玩法集
     * @param sportId
     * @param matchId
     * @param matchType
     * @param userId
     * @return
     */
    List<Long> selectPlaySetList(@Param("sportId") Long sportId, @Param("matchId") Long matchId, @Param("matchType") Integer matchType, @Param("userId")Long userId);

    /**
    * @Description: 玩法集里面的所有玩法
    * @Param: [sportId]
    * @return: java.util.List<java.lang.Long>
    * @Author: KIMI
    * @Date: 2020/11/13
    */
    List<Long> selectPlayIdListByPlaySet(@Param("sportId") Long sportId);

    @Select("SELECT id,`name` FROM `rcs_market_category_set` WHERE sport_id = #{sportId} and type =2")
    List<RcsMarketCategorySet> getPerformanceSet(@Param("sportId") Long sportId);

    /**
     * 绩效型
     * @param sportId
     * @param idList
     * @return
     */
    List<FindMarketCategoryListAndNamesBO> findMarketCategoryListAndNamesPerformance(@Param("sportId") Long sportId,@Param("list") List<Long> idList);
}
