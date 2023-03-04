package com.panda.sport.rcs.vo.categoryset;

import lombok.Data;

@Data
public class CategorySetVo {
    /**
     * 玩法ID
     */
    private Long categoryId;
    /**
     * 玩法集名称
     */
    private String name;
    /**
     * 玩法名称
     */
    private String text;

    private String nameCode;
}
