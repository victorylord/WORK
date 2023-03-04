package com.panda.sport.rcs.pojo.bo;

import lombok.Data;

@Data
public class FindMarketCategoryListAndNamesBO {
    /**
     * 玩法id
     */
    private Long id;
    /**
     * 玩法集id
     */
    private Long marketCategorySetId;
    /**
     * 赛种id
     */
    private Long sportId;
    /**
     * 国际化code
     */
    private String nameCode;
    /**
     * 国际化
     */
    private String text;

}
