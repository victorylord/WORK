package com.panda.sport.rcs.trade.vo.profit;

import lombok.Data;

import java.util.List;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.vo
 * @Description :  期望详情矩阵
 * @Date: 2020-03-05 11:22
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class ProfitPlayRectangleVo {
    /**
     *  玩法id
     */
    private Integer playId;
    /**
     * @Description   玩法名称
     * @Param 
     * @Author  toney
     * @Date  15:04 2020/3/5
     * @return 
     **/
    private String playName;
    /**
     * @Description   大小球
     * @Param 
     * @Author  toney
     * @Date  11:44 2020/3/5
     * @return 
     **/
    private List<ProfiRectangleVo> profiRectangleVos;
}
