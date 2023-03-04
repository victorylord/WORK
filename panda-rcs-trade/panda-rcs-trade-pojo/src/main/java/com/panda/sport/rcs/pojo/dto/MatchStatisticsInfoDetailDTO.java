package com.panda.sport.rcs.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author :  vector 2.3
 * @Project Name :  data-realtime
 * @Package Name :  com.panda.sport.data.realtime.api.message
 * @Description :  TODO
 * @Date: 2019-10-07 17:10
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class MatchStatisticsInfoDetailDTO implements Serializable{

	private static final long serialVersionUID = 1L;
    /**
     * 比分类型编码：字典表system_item_dict parent_type_id=15
     */
    private String code;

    /**
     * 上下半场，盘、节等概念的第几盘，不传为全场
     */
    private Integer firstNum;

    /**
     * 盘中有局的第几局
     */
    private Integer secondNum;

    /**
     * 主队得分
     */
    private Integer t1;
    /**
     * 客队得分
     */
    private Integer t2;
}
