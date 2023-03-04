package com.panda.sport.rcs.pojo.dto.tourTemplate;

import lombok.Data;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.vo.tourTemplate
 * @Description :  菜单
 * @Date: 2020-05-22 20:45
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class TemplateMenuListDto {
    /**
     * 模板id
     */
    private Long id;

    /**
     * 1：级别  2：联赛id   3：赛事id
     */
    private Integer type;

    /**
     * 根据type设置（1：设值联赛等级  2：设值联赛id   3：设值赛事id）
     */
    private Long typeVal;
    /**
     * 模板名稱
     */
    private String templateName;
}
