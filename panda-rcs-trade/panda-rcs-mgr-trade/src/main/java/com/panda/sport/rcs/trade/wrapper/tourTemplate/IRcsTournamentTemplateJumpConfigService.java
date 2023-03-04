package com.panda.sport.rcs.trade.wrapper.tourTemplate;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateJumpConfig;
import com.panda.sport.rcs.trade.param.TournamentTemplateJumpConfigParam;

/**
 * @author :  carver
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.pojo.tourTemplate
 * @Description :  综合操盘跳分设置表
 * @Date: 2021-09-29 16:41
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface IRcsTournamentTemplateJumpConfigService extends IService<RcsTournamentTemplateJumpConfig> {
    /**
     * 保存综合操盘联赛模板跳分设置
     *
     * @author carver
     * @date 2021-9-29
     */
    void saveTournamentTemplateJumpConfig(TournamentTemplateJumpConfigParam param);

    /**
     * kir 特殊抽水数据初始化
     */
    void initTournamentSpecialOddsInterval();

    void initTournamentSpecialBettingIntervalHigh();


    /**
     * kir 初始化非自己操盘赛种的赔率变动范围数据
     */
    void initMTSOddsChangeValue();

    /**
     * 赔率接拒变动范围初始化
     *
     * @author kir
     * @date 2022-03-04
     */
    void initOddsChangeValue();

    /**
     * 综合操盘初始化联赛跳分机制数据
     * @param sportId
     * @param tournamentId
     */
    void initTournamentJump(Long sportId,Long tournamentId);
    /**
     * @Description   //清空赛事水差
     * @Param [matchId]
     * @Author  sean
     * @Date   2021/11/7
     * @return void
     **/
    void clearMatchWater(RcsMatchMarketConfig config);

    /**
     * 增加百家陪配置信息
     */
    void addBaiJiaPaiWeight(JSONObject jsonObject);

    void addDataSourceCode();

    void addTemplateAcceptConfigAutoChange();

    void addDataSourceCodeCommon(String key,Integer val);

    void updateTemplateAoConfigData();
}
