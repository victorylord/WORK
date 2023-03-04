package com.panda.sport.rcs.pojo.dto;

import lombok.Data;

/**
 * 	赛事查询模板名称
 */
@Data
public class TemplateNameForMatchDto {
    /**
     *	赛事id
     */
    private Long matchId;

    /**
     * 1：级别  2：联赛id   3：赛事id
     */
    private Integer type;

    /**
     * 	type为1时的联赛等级
     */
    private Integer levelNum;
    
    /**
     * 模板名稱
     */
    private String templateName;
}
