package com.panda.sport.rcs.trade.vo.tourTemplate;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * @author: jstyChandler
 * @Package: com.panda.sport.rcs.trade.vo.tourTemplate
 * @ClassName: AoBasketBallTemplateConfigEntity
 * @Description: TODO
 * @Date: 2023/2/6 13:21
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class AoBasketBallTemplateConfigEntity implements java.io.Serializable {
    Integer quarters;
    Integer quarterMin;
    String shotClock;
    /**
     * Default of SD as S2
     */
    Integer useOuSd;
    Integer refresh;
    Integer closingGe;
    Integer closingTime;
    Integer overtime;
    /**
     * Int Handicap Lines
     */
    Integer handicapModel;
    /**
     * AO赛事id
     **/
    String aoMatchId;
    /**
     * 标准赛事ID
     **/
    String standardMatchId;
    /**
     * 联赛等级
     **/
    Integer tournamentLevel;
}
