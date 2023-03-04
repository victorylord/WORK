package com.panda.sport.rcs.trade.wrapper.tourTemplate;


import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateAcceptConfig;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateEvent;
import com.panda.sport.rcs.trade.param.*;
import com.panda.sport.rcs.trade.vo.tourTemplate.otherSystem.TournamentLevelTemplateVo;

import java.util.List;

public interface IRcsMatchTemplateModifyService {
    /**
     * 更新模板数据
     *
     * @param param
     */
    void modifyTemplate(TournamentTemplateUpdateParam param);

    /**
     * 更新滚球结算审核事件
     *
     * @param param
     */
    void modifyTemplateEvent(List<RcsTournamentTemplateEvent> param);

    /**
     * 更新玩法数据
     *
     * @param param
     */
    void modifyPlayMargain(List<TournamentTemplatePlayMargainParam> param);

    /**
     * 更新分时margin数据
     * type  1:联赛模板   2：赛事模板
     * @param param
     */
    void modifyMargainRef(TournamentTemplatePlayMargainRefParam param, Integer type) throws Exception;

    /**
     * 更新接拒单事件数据
     *
     * @param param
     */
    void modifyAcceptConfig(List<RcsTournamentTemplateAcceptConfig> param);

    /**
     * @param param:
     * @Description: 删除联赛模板和赛事模板分时margin数据
     * @Author carver
     * @Date 2020/10/6 16:16
     * @return: void
     **/
    void removeMarginRef(TournamentTemplatePlayMargainRefParam param);

    /**
     * @param param:
     * @Description: 赛事模板，同步联赛模板数据
     * @Author carver
     * @Date 2020/10/27 17:51
     * @return: void
     **/
    void modifyMatchTempByLevelTemp(TournamentTemplateUpdateParam param);

    /**
     * @Description: 根据1级联赛接拒单配置，刷新其他联赛等级下的接拒单配置
     * @Author carver
     * @Date 2020/11/20 15:25
     * @return: void
     **/
    void processTemplateByOneLevel();

    /**
     * @Description: 根据赛事所在联赛, 获取所使用的联赛模板和所有等级模板
     * @Author carver
     * @Date 2020/12/15 15:25
     * @return: void
     **/
    List<TournamentLevelTemplateVo> findLevelTempByMatchId(TournamentTemplateUpdateParam param, String lang);

    /**
     * @Description: 复制接拒单事件配置
     * @Author carver
     * @Date 2021/1/15 15:25
     * @return: void
     **/
    void liveTemplateAccept(Long matchId, Long levelTemplateId, Long templateId);
    /**
     * 赛事玩法赔率源设置
     *
     * @param param
     * @Author carver
     * @Date 2021/2/15 15:25
     */
    void updatePlayOddsConfig(RcsTournamentTemplatePlayOddsConfigParam param);
    /**
     * 赛事玩法赔率源设置
     *
     * @param param
     * @Author kir
     * @Date 2022/2/6 15:25
     */
    void modifyBaiJiaConfig(RcsTournamentTemplateBaijiaConfigParam param);

    /**
     * 篮球操盘更新玩法跳水和跳盘最大值
     *
     * @param param
     * @Author carver
     * @Date 2021/4/15 15:25
     */
    void updateOddsMarketMaxValue(TournamentTemplatePlayMargainParam param);
    /**
     * 更新网球其他玩法全部应用
     *
     * @param param
     * @Author carver
     * @Date 2021/4/15 15:25
     */
    void updateTennisAllPlayValue(TournamentTemplatePlayMargainParam param);
    /**
     * 更新赛事模板提前结算开关
     *
     * @param param
     * @Author carver
     * @Date 2021/4/15 15:25
     */
    void modifySettleSwitch(TournamentTemplateUpdateParam param);

    /**
     * 更新预约投注开关
     * @param param
     */
    void modifyPendingOrderStatus(TournamentTemplateUpdateParam param);

    /**
     * 更新赛事模板赔率变动接拒开关
     *
     * @param param
     * @Author Kir
     * @Date 2021/4/15 15:25
     */
    void modifyOddsChangeStatus(TournamentTemplateUpdateParam param);

    /**
     * 更新赛事模板赔率变动接拒开关
     *
     * @param param
     * @Author Kir
     * @Date 2021/4/15 15:25
     */
    void modifyWarnSuspended(TournamentTemplateUpdateParam param);

    /**
     * 更新赛事MTS接距配置信息
     * @param param
     */
    void modifyMtsSwitchConfig(TournamentTemplateUpdateParam param);

    /**
     * 更新接距开关
     * @param param
     */
    void modifyDistanceSwitch(TournamentTemplateUpdateParam param);

    /**
     * 盘口配置修改 消息
     * @param sportId
     * @param matchId
     * @param level
     * @param template
     * @param playMargainList
     */
    //void updateMarketConfig(Long sportId, Long matchId, Integer level, RcsTournamentTemplate template, List<RcsTournamentTemplatePlayMargain> playMargainList);
}
