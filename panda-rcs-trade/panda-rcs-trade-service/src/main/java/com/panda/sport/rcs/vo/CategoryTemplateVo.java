package com.panda.sport.rcs.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author :  enzo
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.vo
 * @Description :  玩法模板
 * @Date:
 */
@Data
public class CategoryTemplateVo implements Serializable {
    /**
     * 玩法ID
     **/
    private Long categoryId;
    /**
     * 赛事种类
     */
    private Long sportId;
    /**
     * 玩法名称编码
     */
    private String nameCode;
    /**
     * 所属阶段
     */
    private Integer scopeId;
    /**
     * 模板Id
     */
    private Integer templateId;
    /**
     * 排序
     */
    private Integer orderNo;
}
