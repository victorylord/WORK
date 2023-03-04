package com.panda.sport.rcs.pojo.tourTemplate;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author :  koala
 * @Project Name :  panda-rcs-trade-group
 * @Package Name :  com.panda.sport.rcs.trade.vo.tourTemplate
 * @Description :  po模板设置承载类
 * @Date: 2022-03-05 14:54
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class AoParameterTemplateVo implements Serializable {
    private static final long serialVersionUID = 3938339006953812768L;
    /**
     * peroid
     */
    private Integer perId;
    /**
     * 1st inj Time
     */
    private Integer oneInjTime;
    /**
     * 2nd Inj Time
     */
    private Integer twoInjTime;
    /**
     * HT Draw.adj
     */
    private Integer htDrawAdj;
    /**
     * FT Draw.adj
     */
    private Integer ftDrawAdj;
    /**
     * refresh
     */
    private Integer refresh;

    /**
     * 00-15
     */
    private BigDecimal zeroOneFive;
    /**
     * 15-30
     */
    private BigDecimal oneFiveThree;
    /**
     * 30-HT
     */
    private BigDecimal threeHt;

    /**
     * HT-60
     */
    private BigDecimal htSix;

    /**
     * 60-75
     */
    private BigDecimal sixSevenFive;
    /**
     * 75-HT
     */
    private BigDecimal sevenFiveFt;

    /**
     * g_goal、g_corner、g_booking、g_yc、g_rc
     * ex_goal、ex_corner
     **/
    String tempType;

}
