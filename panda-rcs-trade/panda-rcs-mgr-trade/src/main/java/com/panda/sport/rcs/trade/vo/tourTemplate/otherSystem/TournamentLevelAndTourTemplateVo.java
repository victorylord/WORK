package com.panda.sport.rcs.trade.vo.tourTemplate.otherSystem;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author carver
 */
@Data
public class TournamentLevelAndTourTemplateVo implements Serializable {
    /**
     * 联赛id
     */
    private Long tournamentId;
    /**
     * 早盘等级和当前联赛专用模板
     */
    private List<TournamentLevelTemplateVo> preTemplate;
    /**
     * 滚球等级和当前联赛专用模板
     */
    private List<TournamentLevelTemplateVo> liveTemplate;
}
