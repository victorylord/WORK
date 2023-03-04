package com.panda.sport.rcs.mapper.tourTemplate;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsStandardSportMarketSell;
import com.panda.sport.rcs.pojo.dto.TemplateNameForMatchDto;
import com.panda.sport.rcs.pojo.dto.tourTemplate.TemplateMenuListDto;
import com.panda.sport.rcs.pojo.dto.tourTemplate.TournamentTemplateDto;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplate;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @Description 联赛模板配置
 * @Param
 * @Author toney
 * @Date 20:02 2020/5/10
 * @return
 **/
@Repository
public interface RcsTournamentTemplateMapper extends BaseMapper<RcsTournamentTemplate> {
    /**
     * @param rcsTournamentTemplate:
     * @Description: 插入模板数据
     * @Author carver
     * @Date 2020/10/9 19:11
     * @return: int
     **/
    int insertBatch(RcsTournamentTemplate rcsTournamentTemplate);

    /**
     * 查询列表
     *
     * @param map
     * @return
     */
    List<TemplateMenuListDto> menuList(Map<String, Object> map);

    /**
     * 开售修改权重赛事数据
     *
     * @param param
     * @return
     */
    int updateTemplateWeight(RcsTournamentTemplate param);

    /**
     * 根据id修改模板数据
     *
     * @param param
     * @return
     */
    int updateTemplateById(RcsTournamentTemplate param);

    /**
     * 按联赛 id进行搜索，取联赛配置
     *
     * @param
     * @return
     */
    List<TournamentTemplateDto> queryByTournamentId(@Param("tournamentId") Long tournamentId, @Param("sportId") Integer sportId, @Param("matchType") Integer matchType);

    /**
     * 按联赛级别进行搜索,取模板
     *
     * @param tournamentLevel
     * @return
     */
    List<TournamentTemplateDto> queryByTournamentLevel(@Param("tournamentLevel") Integer tournamentLevel, @Param("sportId") Integer sportId, @Param("matchType") Integer matchType);

    /**
     * 获取所有等级模板和当前联赛等级专用模板
     *
     * @param params
     * @return
     */
    List<Map<String, Object>> queryTournamentLevelTemplate(Map<String, Object> params);

    /**
     * 获取当前联赛所关联得模板id
     *
     * @param params
     * @return
     */
    Long queryTemplateId(Map<String, Object> params);

    /**
     * 根据模板id，获取模板信息和联赛名称
     *
     * @param id
     * @return
     */
    RcsTournamentTemplate queryTemplateById(Long id);

    /**
     * @Description: 处理线上已设置操盘手的赛事，生成赛事模板数据
     * @Author carver
     * @Date 2020/10/4 15:34
     * @return: com.panda.sport.rcs.pojo.RcsStandardSportMarketSell
     **/
    List<RcsStandardSportMarketSell> querySoldMatch(@Param("matchId") Long matchId, @Param("sportId") Long sportId);

    /**
     * @param id: templateId
     * @Description: 根据赛事模板id，获取联赛等级模板数据
     * @Author carver
     * @Date 2020/10/27 19:15
     * @return: com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplate
     **/
    RcsTournamentTemplate queryLevelTempByMatchTemp(Long id);

    /**
     * @Description: 根据赛事id，查询模板生成来源
     * @Author carver
     * @Date 2020/12/03 15:41
     * @return: com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplate
     **/
    List<RcsTournamentTemplate> queryTemplateSourceByMatchId(@Param("param") RcsTournamentTemplate param, @Param("matchIds") List<Long> matchIds);

    /**
     * @Description: 由于篮球联赛设置参数调整，线上已开篮球赛事需同步联赛数据
     * @Author carver
     * @Date 2020/12/10 15:25
     * @return: void
     **/
    List<RcsTournamentTemplate> queryBasketballMatchTemplate(@Param("matchId") Long matchId);


    List<String> queryGamePlay(@Param("sportId") Integer sportId, @Param("categorySetId") Integer categorySetId);

	/**
	 * 	根据赛事id查询联赛模板名
	 * 
	 * @param sportId
	 * @param matchIds
	 * @param matchTypeTemplate
	 * @return
	 */
	List<TemplateNameForMatchDto> getTemplateNameForMatch(@Param("sportId") Long sportId,@Param("matchIds") List<Long> matchIds, @Param("matchTypeTemplate") Integer matchTypeTemplate);
}