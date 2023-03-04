package com.panda.sport.rcs.trade.vo.tourTemplate.templateToBusiness;

import lombok.Data;

import java.util.List;

@Data
public class TournamentTemplateAcceptConfigSettleVo {
    /**
     * id
     */
    private Long id;
    /**
     * 玩法集id
     */
    private Integer categorySetId;
    /**
     * 数据源
     */
    private String dataSourceCode;
    /**
     * T常规
     */
    private Integer normalDelayTime;
    /**
     * T延时
     */
    private Integer delayTime;
    /**
     * 最大延时
     */
    private Integer maxDelayTime;
    /**
     * 接拒单事件
     */
    private List<TournamentTemplateAcceptEventSettleVo> acceptEventList;
}
