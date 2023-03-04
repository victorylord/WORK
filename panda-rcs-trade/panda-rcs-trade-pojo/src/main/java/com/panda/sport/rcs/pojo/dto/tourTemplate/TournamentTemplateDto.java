package com.panda.sport.rcs.pojo.dto.tourTemplate;

import com.panda.sport.rcs.pojo.dto.tourTemplate.TournamentTemplateAcceptEventDto;
import lombok.Data;

import java.util.List;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.data.dto
 * @Description :  联赛模板
 * @Date: 2020-05-28 17:48
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class TournamentTemplateDto {
    /**
     * id
     */
    private Long id;
    /**
     * sportId
     */
    private Integer sportId;
    /**
     * 1：级别  2：联赛id   3：默认
     */
    private Integer type;
    /**
     * 默认使用-1,
     */
    private Long typeVal;
    /**
     * 0：早盘；1：滚球
     */
    private Integer matchType;
    /**
     * 赔率源
     */
    private String dataSourceCode;
    /**
     * 订单接矩事件源
     */
    private String orderAcceptEventCode;
    /**
     * 最小延迟时间  单位秒
     */
    private Integer acceptMinTime;

    /**
     * 最大延迟时间，单位秒
     */
    private Integer acceptMaxTime;


    private List<TournamentTemplateAcceptEventDto> acceptEventList;
}
