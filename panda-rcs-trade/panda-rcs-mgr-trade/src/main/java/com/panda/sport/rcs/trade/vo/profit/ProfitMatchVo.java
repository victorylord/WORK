package com.panda.sport.rcs.trade.vo.profit;

import com.panda.sport.rcs.vo.MatchMarketLiveOddsVo;
import lombok.Data;

import java.util.List;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.vo
 * @Description :  TODO
 * @Date: 2020-03-05 11:08
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class ProfitMatchVo {
    /**
     * 赛事id
     */
    private Long matchId;
    /**
     * 赛事开始时间
     */
    private Long beginTime;
    /**
     * @Description   队伍信息
     * @Param 
     * @Author  toney
     * @Date  18:54 2020/3/5
     * @return 
     **/
    List<MatchMarketLiveOddsVo.MatchMarketTeamVo> teamList;
    /**
     * @Description  矩阵分数
     * @Param 
     * @Author  toney
     * @Date  11:52 2020/3/5
     * @return 
     **/
    private List<ProfitPlayRectangleVo> profitPlayRectangleVos;
}
