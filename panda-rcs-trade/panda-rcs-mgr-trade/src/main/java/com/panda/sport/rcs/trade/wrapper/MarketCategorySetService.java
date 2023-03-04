package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.data.rcs.api.Response;
import com.panda.sport.data.rcs.dto.MarketCategoryCetBean;
import com.panda.sport.rcs.mongo.CategoryCollection;
import com.panda.sport.rcs.mongo.I18nBean;
import com.panda.sport.rcs.mongo.MarketCategory;
import com.panda.sport.rcs.mongo.MatchMarketLiveBean;
import com.panda.sport.rcs.pojo.RcsMarketCategorySet;
import com.panda.sport.rcs.pojo.RcsMarketCategorySetMargin;
import com.panda.sport.rcs.pojo.StandardSportMarketCategory;
import com.panda.sport.rcs.pojo.StandardSportMarketCategoryRefReqVo;
import com.panda.sport.rcs.trade.vo.TradingAssignmentVo;
import com.panda.sport.rcs.vo.MarketCategoryQueryVO;
import com.panda.sport.rcs.vo.MarketCategorySetResVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Felix
 */
public interface MarketCategorySetService extends IService<RcsMarketCategorySet> {
    /**
     * 获取矩阵玩法ID集合
     *
     * @param sportId    赛种
     * @param matchStage 矩阵类型，1-全场比分，2-半场比分
     * @return
     */
    List<Long> getMatrixPlayIdList(Long sportId, Long matchStage);

    /**
     * 玩法集列表
     *
     * @param rcsMarketCategorySet
     * @return
     */
    List<RcsMarketCategorySet> findCategorySetList(RcsMarketCategorySet rcsMarketCategorySet);

    List<StandardSportMarketCategoryRefReqVo> findMarketCategoryListForSoccer();

    /**
     * 业务模块需要风控提供查询展示型玩法集接口
     *
     * @param marketCategoryCetBean
     * @return
     */
    List<RcsMarketCategorySet> findCategorySetSyncList(MarketCategoryCetBean marketCategoryCetBean);

    /**
     * 分页玩法集列表
     *
     * @param rcsMarketCategorySet
     * @param current
     * @param size
     * @return
     */
    IPage findPageCategorySetList(RcsMarketCategorySet rcsMarketCategorySet, int current, int size);

    /**
     * 批量编辑玩法集
     *
     * @param rcsMarketCategorySetList
     * @return
     */
    Response updateCategorySetList(List<RcsMarketCategorySet> rcsMarketCategorySetList);

    /**
     * 玩法列表
     *
     * @param standardSportMarketCategory
     * @return
     */
    List<StandardSportMarketCategory> findStandardSportMarketCategoryList(StandardSportMarketCategory standardSportMarketCategory);

    /**
     * 玩法内容
     *
     * @param id
     * @return
     */
    List<StandardSportMarketCategory> findMarketCategoryContent(Integer id);

    /**
     * 从风控获取玩法id，然后再根据id查询panda库里的玩法
     *
     * @param id
     * @return
     */
    List<Map<String, Object>> findPandaMarketCategoryId(Long id);

    /**
     * 新建玩法集 & 新增玩法内容
     *
     * @param operatingParam
     * @return
     */
    public Map<String, Object> addCategorySetAndCategory(Map<String, Object> operatingParam);

    /**
     * 编辑玩法集 & 编辑玩法内容
     *
     * @param operatingParam
     * @return
     */
    public Map<String, Object> updateCategorySetAndCategory(Map<String, Object> operatingParam);

    /**
     * 查询出所有风控类型的玩法id，所在的玩法集id
     *
     * @param reCached
     */
    void cacheWindControlTypeAll(boolean reCached);

    /*******1.0补丁版本*********/
    /**
     * 批量删除玩法集里的玩法
     *
     * @return
     */
    Map<String, Object> deleteCategorySetContent(List<Long> id);

    /**
     * 删除玩法集
     *
     * @param id
     * @return
     */
    Map<String, Object> deleteCategorySet(Long id);

    /**
     * 根据玩法ID 查询margin  此集接口提供给融合使用
     *
     * @param obj
     * @return
     */
    LinkedHashMap<String, RcsMarketCategorySetMargin> findMargin(MarketCategoryCetBean obj);

    /**
     * 查询次要玩法集列表
     *
     * @param sportId
     * @param matchId
     * @param matchSnapshot
     * @return
     * @author Paca
     */
    List<MarketCategorySetResVO> list(Long sportId, Long matchId, Integer matchSnapshot);

    /**
     * 查询玩法集下盘口详情
     *
     * @param marketCategoryQueryVO
     * @return
     * @author Paca
     */
    CategoryCollection marketDetail(MarketCategoryQueryVO marketCategoryQueryVO);


    /**
     * 根据赛事ID查询标准赛事基础信息表
     * @author Kir
     * @param sportId
     * @param matchId
     * @return
     */
    MatchMarketLiveBean getMatchInfo(final Long sportId, final Long matchId);


    void multiGroupByColAndRow(MarketCategory category, Map<String, I18nBean> teamMap);

    void groupByColumn(MarketCategory category, Map<String, I18nBean> teamMap);

    void singleGroupByColAndRow(MarketCategory category, Map<String, I18nBean> teamMap);

    /**
     * 通过玩法集ID获取玩法集编码
     *
     * @param playSetId
     * @return
     */
    String getPlaySetCodeByPlaySetId(Long playSetId);

    List<RcsMarketCategorySet> getPerformanceSet(Long sportId);


}
