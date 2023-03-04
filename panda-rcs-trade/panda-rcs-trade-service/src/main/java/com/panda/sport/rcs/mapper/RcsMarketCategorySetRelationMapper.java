package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.mongo.CategorySetOrderNo;
import com.panda.sport.rcs.pojo.RcsMarketCategorySetRelation;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author :  myname
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.mapper
 * @Description :  TODO
 * @Date: 2019-09-11 15:11
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Repository
public interface RcsMarketCategorySetRelationMapper extends BaseMapper<RcsMarketCategorySetRelation> {

    @Select("SELECT DISTINCT" +
            " r.market_category_id" +
            " FROM" +
            " rcs_market_category_set_relation r" +
            " LEFT JOIN rcs_market_category_set s ON r.market_category_set_id = s.id" +
            " WHERE" +
            " r.market_category_set_id = #{marketCategorySetId}" +
            " AND s.type = 1" +
            " AND s.`status` = 2" +
            " ORDER BY" +
            " r.order_no")
    List<Long> getCategoryIdByCategorySetId(@Param("marketCategorySetId") Long marketCategorySetId);

    /**
     * 通过玩法集编码获取玩法ID
     *
     * @param playSetCode
     * @return
     */
    @Select("SELECT DISTINCT" +
            " r.market_category_id" +
            " FROM" +
            " rcs_market_category_set_relation r" +
            " LEFT JOIN rcs_market_category_set s ON r.market_category_set_id = s.id" +
            " WHERE" +
            " s.play_set_code = #{playSetCode}" +
            " AND s.type = 1" +
            " AND s.`status` = 2" +
            " ORDER BY" +
            " r.order_no")
    List<Long> getPlayIdByPlaySetCode(@Param("playSetCode") String playSetCode);

    @Select("SELECT" +
            " b.market_category_id," +
            " b.market_category_set_id," +
            " a.display_sort," +
            " b.order_no" +
            " FROM" +
            " rcs_market_category_set_relation b" +
            " LEFT JOIN rcs_market_category_set a ON a.id = b.market_category_set_id" +
            " WHERE" +
            " a.type = 1" +
            " AND a.`status` = 2" +
            " ORDER BY" +
            " a.display_sort," +
            " b.order_no")
    List<CategorySetOrderNo> getCategorySetOrderNo();



    int isContainPlayFromPlaySet(@Param("list") List<Long> ids, @Param("sportId") Long sportId, @Param("type") Integer type,@Param("setId") Long id);
}
