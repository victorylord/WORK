package com.panda.sport.rcs.vo;

import lombok.Data;

/**
 * @program: xindaima
 * @description:
 * @author: kimi
 * @create: 2020-11-07 11:20
 **/
@Data
public class RcsMarketCategorySetVo {
    /**
     * id
     */
    private Long id;
    /**
     * 名字
     */
    private String name;

    /**
     * 国际化
     */
    private String names;
}
