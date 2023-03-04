package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.data.rcs.dto.tournament.StandardMarketSellQueryDto;
import com.panda.sport.rcs.pojo.RcsMatchCollection;
import com.panda.sport.rcs.pojo.RcsStandardSportMarketSell;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplate;
import com.panda.sport.rcs.vo.HttpResponse;
import com.panda.sport.rcs.vo.StandardMarketSellQueryV2Vo;
import com.panda.sport.rcs.vo.StandardMarketSellQueryVo;
import com.panda.sport.rcs.vo.StandardMarketSellVo;

import java.util.List;
import java.util.Map;

/**
 * @ClassName RcsStandardSportMarketSellService
 * @Description: TODO
 * @Author carver
 * @Date 2019/12/30
 **/
public interface RcsStandardSportMarketSellService extends IService<RcsStandardSportMarketSell> {

    /**
     * 获取预开售赛事数据
     *
     * @param standardMarketSellVo
     * @return
     */
    IPage<StandardMarketSellVo> listStandardMarketSellVo(StandardMarketSellQueryVo standardMarketSellVo);

    /**
     * 确认开售
     *
     * @param standardMarketSellQueryVo
     * @return
     */
    HttpResponse confirmStandardMarketSell(StandardMarketSellQueryVo standardMarketSellQueryVo);

    /**
     * 设置盘口数/角球/罚牌
     *
     * @param standardMarketSellQueryVo
     */
    HttpResponse configPlayShow(StandardMarketSellQueryVo standardMarketSellQueryVo);

    /**
     * 根据赛种获取开售赛事数量统计
     */
    List getMatchNumberByType();

    /**
     * 根据赛事ID查询
     *
     * @param matchInfoId
     * @return
     */
    RcsStandardSportMarketSell selectStandardMarketSellVo(Long matchInfoId);

    int delete(StandardMarketSellQueryVo standardMarketSellQueryVo);

    List<Long> queryTraderMatchIds(RcsMatchCollection co);

    /**
     * 开售的赛事，赔率源权重切换
     * @param standardMarketSellQueryVo
     */
    HttpResponse configChangeWeight(StandardMarketSellQueryDto standardMarketSellQueryVo);

    /**
     * 更新赛事模板赔率源权重优先级
     * @param standardMarketSellQueryVo
     */
    void updatePlayMarginIsSellByPlayId(StandardMarketSellQueryDto standardMarketSellQueryVo);

    /**
     * 更新赛事模板接拒单事件源切换
     * @param standardMarketSellQueryVo
     */
    void updateTemplateEventSourceConfig(StandardMarketSellQueryDto standardMarketSellQueryVo);

    /**
     * 根据用户及赛事id查询  rcs_match_user_memo_ref 表，查询是否存在未读备忘录的赛事
     * @param matchIds
     * @param traderId
     * @return
     */
    Map<Long,List<String>> getNeedRemindMatchMemos(List<Long> matchIds, Long traderId);

    /**
     * kir-修改23427 和 23428 bug  向业务推送数据
     * @param template
     */
    void tournamentTemplateSettle(RcsTournamentTemplate template);
}
