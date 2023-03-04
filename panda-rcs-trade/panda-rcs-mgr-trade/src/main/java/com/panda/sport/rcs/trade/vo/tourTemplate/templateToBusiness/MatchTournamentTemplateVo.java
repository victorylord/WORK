package com.panda.sport.rcs.trade.vo.tourTemplate.templateToBusiness;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class MatchTournamentTemplateVo implements Serializable {
    /**
     * 运行种类
     */
    private Integer sportId;
    /**
     * 赛事id
     */
    private Long matchId;
    /**
     * 模板id
     */
    private Long templateId;
    /**
     * 列表
     */
    private List<TournamentTemplateAcceptConfigSettleVo> acceptConfigList;
}
