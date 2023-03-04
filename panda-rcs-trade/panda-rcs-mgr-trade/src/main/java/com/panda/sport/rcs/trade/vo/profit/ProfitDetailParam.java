package com.panda.sport.rcs.trade.vo.profit;

import java.util.List;

import lombok.Data;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.vo.profit
 * @Description :  请求参数
 * @Date: 2020-03-05 11:53
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class ProfitDetailParam {
    /**
     * @Description   联赛id
     * @Param 
     * @Author  toney
     * @Date  11:53 2020/3/5
     * @return 
     **/
    private List<Long> standardTournamentIds;
    /**
     * 其它早盘
     */
    private Integer otherMorningMarket;
    
    /**
     * @Description   运动类型id
     * @Param 
     * @Author  toney
     * @Date  17:15 2020/3/5
     * @return 
     **/
    private Long sprotId;
    
    /**
     * @Description   联赛类型
     * 1:早盘；2：滚球；3:其它早盘
     * @Param 
     * @Author  toney
     * @Date  11:54 2020/3/5
     * @return 
     **/
    private Integer matchType;
    /**
     * @Description   日期
     * @Param 
     * @Author  toney
     * @Date  12:26 2020/3/5
     * @return 
     **/
    private Long date;
}
