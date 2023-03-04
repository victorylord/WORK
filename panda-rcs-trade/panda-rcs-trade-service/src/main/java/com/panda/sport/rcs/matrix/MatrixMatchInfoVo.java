package com.panda.sport.rcs.matrix;

import com.panda.sport.rcs.constants.RcsConstant;
import com.panda.sport.rcs.factory.BeanFactory;
import com.panda.sport.rcs.mongo.I18nBean;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 矩阵赛事信息
 * @Author : Paca
 * @Date : 2021-01-17 15:15
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class MatrixMatchInfoVo implements Serializable {

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
     * 矩阵信息
     */
    private Map<String, Object> matrixData;

    /**
     * 主队国际化
     */
    private I18nBean homeTeam = BeanFactory.getHomeTeam();
    /**
     * 客队国际化
     */
    private I18nBean awayTeam = BeanFactory.getAwayTeam();

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

    /**
     * 类型：1-早盘，2-滚球盘，3-冠军盘
     *
     * @return
     */
    public Integer getMatrixMatchType() {
        if (getMatchType() == 1) {
            return 1;
        }
        return 2;
    }

    public String getTraderName() {
        if (getMatchType() == 0) {
            return liveTrader;
        }
        return preTrader;
    }

    public String getRiskManagerCode() {
        if (getMatchType() == 0) {
            return liveRiskManagerCode;
        }
        return preRiskManagerCode;
    }
}
