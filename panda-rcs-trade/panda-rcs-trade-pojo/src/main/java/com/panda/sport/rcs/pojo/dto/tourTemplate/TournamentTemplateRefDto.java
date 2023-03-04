package com.panda.sport.rcs.pojo.dto.tourTemplate;

import lombok.Data;

import java.io.Serializable;

@Data
public class TournamentTemplateRefDto implements Serializable {
    /**
     * 联赛Id
     */
    private Long tournamentId;

    /**
     * 早盘模板id
     */
    private Long templateId;

    /**
     * 滚球所属模板Id
     */
    private Long liveTemplateId;
    /**
     * 早盘专用模板名称
     */
    private String preTemplateName;
    /**
     * 滚球专用模板名称
     */
    private String liveTemplateName;

}
