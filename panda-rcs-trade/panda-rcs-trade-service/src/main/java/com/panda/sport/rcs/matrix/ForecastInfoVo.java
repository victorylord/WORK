package com.panda.sport.rcs.matrix;

import com.panda.sport.rcs.constants.RcsConstant;
import com.panda.sport.rcs.factory.BeanFactory;
import com.panda.sport.rcs.mongo.I18nBean;
import com.panda.sport.rcs.mongo.PredictForecastVo;
import com.panda.sport.rcs.vo.CustomizedEventBeanVo;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 矩阵赛事信息
 * @Author : Kir
 * @Date : 2021-05-04 15:15
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class ForecastInfoVo {

    private static final long serialVersionUID = 1L;

    /**
     * 赛事ID
     */
    private Long matchId;
    /**
     * 标准赛事ID
     */
    private String matchManageId;
    /**
     * 比分
     */
    private String score;
    /**
     * 联赛ID
     */
    private Long tournamentId;
    /**
     * 联赛名称
     */
    private Long tournamentNameCode;
    /**
     * 联赛名称国际化
     */
    private String tournamentNames;
    /**
     * 赛事状态
     */
    private Integer matchStatus;
    /**
     * 赛事开始时间
     */
    private Long beginTime;
    /**
     * 比赛进行时间
     */
    private Integer secondsMatchStart;
    /**
     * 是否支持滚球。取值为 1 或 0 。1=支持；0=不支持
     */
    private Integer liveOddBusiness;
    /**
     * 是否属于前十五分钟快照赛事
     * 1是 0否
     */
    private Integer matchSnapshot;
    /**
     * 比赛阶段id. 取自基础表 : match_status.id
     */
    private Long matchPeriodId;
    /**
     * 赛前操盘平台 MTS,PA
     */
    private String preRiskManagerCode;
    /**
     * 滚球操盘平台 MTS,PA
     */
    private String liveRiskManagerCode;

    /**
     * 早盘操盘手ID
     */
    private String preTraderId;
    /**
     * 早盘操盘手
     */
    private String preTrader;
    /**
     * 滚球操盘手ID
     */
    private String liveTraderId;
    /**
     * 滚球操盘手
     */
    private String liveTrader;
    /**
     * 操盘人数
     */
    private Integer traderNum = 0;

    /**
     * forecast集合
     */
    private Map<Long, List<PredictForecastVo>> predictForecastVoList;

    /**
     * 主队国际化
     */
    private I18nBean homeTeam = BeanFactory.getHomeTeam();
    /**
     * 客队国际化
     */
    private I18nBean awayTeam = BeanFactory.getAwayTeam();

    /**
     * 赛事开关封锁 -1 未开 0 :开、2:关、1:封、11
     */
    private Integer matchMarketStatus;

//    /**
//     * 商业事件源编码 如：SR,BC,BG
//     */
//    private String businessEvent;

    /**
     * 事件信息
     */
    private CustomizedEventBeanVo eventBeanVo;

    /**
     * 0-滚球，1-早盘
     *
     * @return
     */
    public Integer getMatchType() {
        if (RcsConstant.LIVE_MATCH_STATUS.contains(matchStatus)) {
            return 0;
        }
        return 1;
    }
}
