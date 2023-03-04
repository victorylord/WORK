package com.panda.sport.rcs.trade.vo.profit;

import com.panda.sport.rcs.vo.I18nItemVo;
import lombok.Data;

import java.util.List;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.vo.profit
 * @Description :  联赛期望详情
 * @Date: 2020-03-05 12:15
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class ProfitDetailVo {
    /**
     * 联赛id
     */
    private Long standardTournamentId;
    /**
     * 联赛名称
     */
    private List<I18nItemVo> turnamentName;
    /**
     * @Description   赛事矩阵信息
     * @Param 
     * @Author  toney
     * @Date  12:17 2020/3/5
     * @return 
     **/
    private List<ProfitMatchVo> matchVoList;
}
