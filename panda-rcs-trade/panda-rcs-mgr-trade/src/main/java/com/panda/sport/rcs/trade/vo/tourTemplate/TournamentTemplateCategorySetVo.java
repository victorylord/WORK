package com.panda.sport.rcs.trade.vo.tourTemplate;

import lombok.Data;

import java.util.List;

/**
 * @author :  carver
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.vo.tourTemplate
 * @Description :  联赛模板对应配置的玩法
 * @Date: 2020-09-06 17:03
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class TournamentTemplateCategorySetVo{
    /**
     * 主键id
     */
    private Long id;
    /**
     * 玩法集名称
     */
    private String name;
    /**
     * 玩法集下配置的玩法总数量
     */
    private Integer totalNum;
    /**
     * 玩法集下已开售玩法总数量
     */
    private Integer sellNum;
    
    /**
     * 玩法集编码
     * 进球类-FOOTBALL_GOAL
     */
    private String playSetCode;

    /**
     * 多语言编码
     */
    private String nameCode;

    /**
     * 玩法集下配置的玩法
     */
    private List<TournamentTemplatePlayMargainVo> categoryList;
}
