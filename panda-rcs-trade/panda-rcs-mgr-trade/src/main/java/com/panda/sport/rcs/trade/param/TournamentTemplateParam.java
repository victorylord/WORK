package com.panda.sport.rcs.trade.param;

import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplate;
import lombok.Data;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.param
 * @Description :  默认请求
 * @Date: 2020-05-17 17:56
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class TournamentTemplateParam extends RcsTournamentTemplate {
    /**
     * 玩法名称
     */
    private String playName;
    /**
     * 玩法集id
     */
    private Long categorySetId;
    /**
     * 等级
     */
    private Long level;
    /**
     * 联赛id
     */
    private String fatherTournamentId;
    /**
     * 操作類別編碼
     */
    private Integer operatePageCode;

}
