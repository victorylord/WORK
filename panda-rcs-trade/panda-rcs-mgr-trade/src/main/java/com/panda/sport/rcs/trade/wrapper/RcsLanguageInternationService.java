package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.mongo.I18nBean;
import com.panda.sport.rcs.pojo.LanguageInternation;
import com.panda.sport.rcs.pojo.RcsLanguageInternation;
import com.panda.sport.rcs.pojo.dto.PlayLanguageInternation;
import com.panda.sport.rcs.vo.ConditionVo;
import com.panda.sport.rcs.vo.I18nItemVo;
import com.panda.sport.rcs.vo.TournamentBeanVo;
import com.panda.sport.rcs.vo.TournamentResultVo;

import java.util.List;
import java.util.Map;

public interface RcsLanguageInternationService extends IService<RcsLanguageInternation> {
    /**
     * 新增或修改多语言
     *
     * @param list
     * @return
     */
    int batchInsertOrUpdate(List<RcsLanguageInternation> list);

    /**
     * 根据编码，获取多语言
     *
     * @param nameCodes
     * @return
     */
    List<RcsLanguageInternation> getLanguageInternationByCode(List<String> nameCodes);

    /**
     * 获取玩法多语言
     *
     * @param sportId
     * @param playId
     * @return
     */
    I18nBean getPlayLanguage(Long sportId, Long playId);


    I18nBean getPlayerLanguage(String nameCode);
    
    
    /**
     * 	根据请求头lang返回对应的多语言文字
     * 
     * @param nameCode
     * @return
     */
    String getPlayerLanguageStr(String nameCode);

	/**
	 * 	根据玩法id 赛种id 玩法国际化
	 * 
	 * @param id
	 * @param sportId
	 * @return
	 */
    I18nBean getCategoryLanguage(Long id, Long sportId); 
    
    /**
     * @return java.util.List<com.panda.sport.rcs.pojo.LanguageInternation>
     * @Description //TODO
     * @Param []
     * @Author kimi
     * @Date 2020/8/8
     **/
    List<PlayLanguageInternation> getByMultilingualism(Long sportId);
    
    List<PlayLanguageInternation> getAllRefMultilingualism();
    
    /**
     * @Description //获取所有的玩法
     * @Param []
     * @Author kimi
     * @Date 2019/10/8
     **/
    List<LanguageInternation> getPlayList();
    
    /**
     * @return java.util.List<com.panda.sport.rcs.pojo.LanguageInternation>
     * @Description //TODO
     * @Param [matchStage]
     * @Author kimi
     * @Date 2019/12/27
     **/
    List<LanguageInternation> getPlayList(String matchStage);
    

    /**
     * 通过赛种获取联赛列表
     *
     * @param sportId
     * @return
     */
    List<TournamentResultVo> getTournamentList(Long sportId);
    
    /**
     * 获取联赛列表
     *
     * @return
     */
    List<ConditionVo> getMarketCategoryList(List<Long> sportIds,List<Long> playSetIds,String lang);
    

    /**
     * 查询联赛对应字典
     *
     * @param tournamentId
     * @return
     */
    TournamentBeanVo getLanguageInternationByTournamentId(Long tournamentId);
    
    /**
     * 查询玩法对应字典
     *
     * @param categoryId
     * @return
     */
    LanguageInternation getLanguageInternationByCategoryId(Long categoryId);
    
    /**
     * @return java.util.Map<java.lang.String, java.util.Map < java.lang.String, java.lang.String>>
     * @Description //批量操作
     * @Param [nameCodes]
     * @Author kimi
     * @Date 2020/7/12
     **/
    Map<Long, Map<String, String>> getCachedNamesMapByCodes(List<Long> nameCodes);
    
    /**
     * 根据nameCode从缓存获取所有语言集
     *
     * @param nameCode
     * @return
     */
    List<I18nItemVo> getCachedNamesByCode(Long nameCode);

    /**
     * 根据nameCode从缓存获取所有语言集
     * 获取顺序：本地缓存->redis->database
     * 返回格式为MAP
     *
     * @param nameCode
     * @return
     */
    Map<String, String> getCachedNamesMapByCode(Long nameCode);

 

    /**
     * 批量查询国际话
     *
     * @param nameCode
     * @return
     * @author Felix
     */
    Map<String, List<I18nItemVo>> getCachedNamesByCode(List<Long> nameCode);

    Map<String, List<I18nItemVo>> getCachedNamesByCode(List<Long> nameCodeList, boolean isParse);
	
}
