package com.panda.sport.rcs.pojo.tourTemplate;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.param
 * @Description :  玩法margain值
 * @Date: 2020-05-12 19:32
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class TournamentTemplatePlayMargainResVo extends RcsTournamentTemplatePlayMargain {
    /**
     * 单注投注/赔付限额
     */
    private BigDecimal orderSinglePayVal;
}
