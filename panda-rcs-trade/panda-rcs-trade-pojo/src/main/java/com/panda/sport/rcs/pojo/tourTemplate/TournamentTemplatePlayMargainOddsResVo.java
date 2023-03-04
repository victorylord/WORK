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
public class TournamentTemplatePlayMargainOddsResVo extends RcsTournamentTemplatePlayMargain {
    /**
     * 根据type设置（1：设值联赛等级  2：设值联赛id   3：设值赛事id）
     */
    private Long typeVal;
}
