package com.panda.sport.rcs.trade.wrapper.tourTemplate;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.dto.tourTemplate.TemplateMenuListDto;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.pojo.dto.tourTemplate.TournamentTemplateDto;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentPlayMarginTemplate;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplate;
import com.panda.sport.rcs.trade.param.*;
import com.panda.sport.rcs.trade.vo.tourTemplate.LogVo;
import com.panda.sport.rcs.trade.vo.tourTemplate.TournamentTemplatePlayMargainRefVo;
import com.panda.sport.rcs.trade.vo.tourTemplate.TournamentTemplateVo;
import com.panda.sport.rcs.vo.HttpResponse;

import java.util.List;
import java.util.Map;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.wrapper.tourTemplate
 * @Description :  联赛模板
 * @Date: 2020-05-10 20:39
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface IRcsTournamentTemplateService extends IService<RcsTournamentTemplate> {
    /**
     * @param sportId: 赛种
     * @param num:     等级
     * @Description: 根据赛种初始化模板数据
     * @Author carver
     * @Date 2020/10/4 15:23
     * @return: void
     **/
    void initTournament(Integer sportId, int num);

    /**
     * 联赛模板新增玩法
     * @date 2021-2-24
     * @param param
     */
    void addTournamentTemplatePlay(TournamentTemplateUpdateParam param);

    /**
     * 联赛模板滚球接拒单新增事件
     * @date 2021-6-5
     * @param param
     */
    void addTournamentTemplateLiveEvent(TournamentTemplateAddEventParam param);
    /**
     * 新增模板数据
     *
     * @param param
     * @return
     */
    Long initTournamentTemplate(TournamentTemplateParam param);

    /**
     * 变更联赛等级
     *
     * @param param
     * @return
     */
    void updateTournamentLevel(UpdateTournamentLevelParam param);

    /**
     * 变更联赛模板绑定关系
     *
     * @param param
     * @return
     */
    void updateTournamentTemplateRelationConfig(UpdateTournamentTemplateParam param);

    /**
     * 查询当前联赛等级和专用模板
     *
     * @param map
     * @return
     */
    List<TemplateMenuListDto> menuList(Map<String, Object> map);

    /**
     * 获取模板和玩法参数数据
     *
     * @param tournamentTemplateParam
     * @return
     */
    TournamentTemplateVo queryTournamentTemplateAndPlay(TournamentTemplateParam tournamentTemplateParam, String lang);

    /**
     * 获取分时margin节点信息
     *
     * @param tournamentTemplateParam
     * @return
     */
    TournamentTemplatePlayMargainRefVo queryTournamentTemplatePlayMargin(TournamentTemplatePlayMargainRefParam tournamentTemplateParam);

    /**
     * 更新模板信息
     *
     * @param param
     * @return
     * @throws Exception
     */
    void updateTemplate(TournamentTemplateUpdateParam param);

    /**
     * 滚球接拒单取联赛配置
     *
     * @param matchInfo
     * @return
     */
    List<TournamentTemplateDto> query(StandardMatchInfo matchInfo, Integer matchType);

    /**
     * @Description: 删除模板数据
     * @Author  carver
     * @Date  2021/02/03 16:57
     * @param param:
     * @return: void
     **/
    void removeSpecialTemplate(RcsTournamentTemplate param);
    /**
     * 模板玩法赔率源设置
     * @param param
     */
    void updatePlayOddsConfig(RcsTournamentTemplatePlayOddsConfigParam param);
    /**
     * @Description: 复制父联赛专用模板
     * @Author  carver
     * @Date  2021/02/21 16:57
     * @param param:
     * @return: void
     **/
    void copyFatherSpecialTemplate(TournamentTemplateParam param);
    /**
     * @Description: 赛事模板同步联赛模板新增玩法到开售列表
     * @Author  carver
     * @Date  2021-4-29 16:57
     * @param param:
     * @return: void
     **/
    void matchSyncTourTempPlay(TournamentTemplateParam param);
    /**
     * 保存专用模板信息
     *
     * @param param
     * @return
     * @throws Exception
     */
    RcsTournamentTemplate saveSpecialTemplate(TournamentTemplateUpdateParam param);
    /**
     * 获取早盘或者滚球联赛专用模板信息
     *
     * @param param
     * @return
     * @throws Exception
     */
    List<RcsTournamentTemplate> getSpecialTemplateDetail(TournamentTemplateParam param);
}
