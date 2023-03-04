package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.LanguageInternation;
import com.panda.sport.rcs.pojo.RcsLanguageInternation;
import com.panda.sport.rcs.pojo.dto.PlayLanguageInternation;
import com.panda.sport.rcs.vo.ConditionVo;
import com.panda.sport.rcs.vo.TournamentBeanVo;
import com.panda.sport.rcs.vo.TournamentConditionVo;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public interface RcsLanguageInternationMapper extends BaseMapper<RcsLanguageInternation> {

    int batchInsertOrUpdate(List<RcsLanguageInternation> list);

    /**
     * 获取玩法多语言
     *
     * @param sportId
     * @param playId
     * @return
     */
    String getPlayLanguage(@Param("sportId") Integer sportId, @Param("playId") Long playId);

    /**
     * 根据namecode获取多语言信息
     * @param nameCode
     * @return
     */
    String getPlayLanguageByNamecode(@Param("nameCode") String nameCode);

	/**
	 * 	查询玩法表的namecode
	 * 
	 * @param categoryId
	 * @param sportId
	 * @return
	 */
	String getCategoryLanguage(@Param("categoryId") Long categoryId, @Param("sportId") Long sportId);
	
    /**
     * @return java.util.List<com.panda.sport.rcs.pojo.LanguageInternation>
     * @Description //TODO
     * @Param []
     * @Author kimi
     * @Date 2020/8/8
     **/

    List<PlayLanguageInternation> getByMultilingualism(@Param("sportId") Long sportId);
    
    List<PlayLanguageInternation> getAllRefMultilingualism();
    
    /**
     * @Description //获取所有的玩法的id和中文编码
     * @Author kimi
     * @Date 2019/10/8
     **/

    List<LanguageInternation> getStandardSportMarketCategoryList();
    

    /*
     * @Description   //获取所有的玩法的id和中文编码
     * @Param [matchStage] 赛事阶段  1:比分全场矩阵；2:比分上半场矩阵
     * @Author  toney
     * @Date  17:01 2019/11/20
     * @return java.util.List<com.panda.sport.rcs.pojo.LanguageInternation>
     **/
    List<LanguageInternation> getStandardSportMarketCategoryList(String matchStage);
    

    /**
     * 通过赛种获取赛事列表
     *
     * @param sportId
     * @return
     */
    List<TournamentConditionVo> getTournamentList(@Param("sportId") Long sportId);
    

    /**
     * 	获取玩法列表
     *
     * @return
     */
    List<ConditionVo> getMarketCategoryList(@Param("sportIds")List<Long> sportIds,@Param("playSetIds")List<Long> playSetIds,@Param("lang")String lang);
    

    /**
     * 获取联赛对应字典
     *
     * @param tournamentId
     * @return
     */
    TournamentBeanVo getLanguageInternationByTournamentId(@Param("tournamentId") Long tournamentId);
    


    /**
     * 获取玩法对应字典
     *
     * @param categoryId
     * @return
     */
    LanguageInternation getLanguageInternationByCategoryId(@Param("categoryId") Long categoryId);
    
    /**
     * @return java.util.List<com.panda.sport.rcs.pojo.LanguageInternation>
     * @Description //TODO
     * @Param [ids]
     * @Author kimi
     * @Date 2020/7/14
     **/
    List<LanguageInternation> getLanguageNameCodes(@Param("ids") Set<Long> ids);
    


    /**
     * @return java.util.List<com.panda.sport.rcs.pojo.LanguageInternation>
     * @Description //根据赛事id查询对阵球队名称
     * @Param [matchId]
     * @Author Sean
     * @Date 19:11 2020/7/31
     **/
    List<Map<String, String>> queryTeamNameByMatchId(@Param("matchId") Long matchId);
    
    /**
     * @return java.util.List<com.panda.sport.rcs.pojo.LanguageInternation>
     * @Description //根据赛事id查询联赛名称
     * @Param [matchId]
     * @Author Sean
     * @Date 19:12 2020/7/31
     **/
    List<Map<String, String>> queryTournamentNameByMatchId(@Param("matchId") Long matchId);
    
    /**
     * 根據玩法ID、球種ID查詢玩法名稱
     * @param playId
     * @param sportId
     * @return
     */
    LanguageInternation getPlayNameByCategoryIdAndSportId(@Param("playId") Long playId, @Param("sportId") Integer sportId);
    
    /**
     * 根據玩法集合、球種ID查詢對應名字
     * @param playIds
     * @param sportId
     * @return
     */
    List<LanguageInternation> getPlayNameByPlayIds(@Param("playIds") List<Long> playIds, @Param("sportId") Integer sportId);

    /**
     * 	根據聯賽Id，球種ID查詢聯賽名稱
     * @return
     */
    LanguageInternation getTournamentNameByIdAndSprotId(@Param("tournamentId") Long id, @Param("sportId") Integer sportId);

    /**
     * 	原有方法调用，不再新引用
     * 	根据namecode查询多个languageType
     * 
     * @return
     */
    @Deprecated
    List<LanguageInternation> getByNameCodeDeprecated(@Param("nameCodes") List<Long> nameCodes);

}