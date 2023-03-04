package com.panda.sport.rcs.vo;

import lombok.Data;

/**
 * 分页查询条件基类
 */
@Data
public class PageQuery {

    /**
     * 当前页
     */
    private Integer currentPage = 0;
    /**
     * 每页显示条数
     */
    private Integer pageSize = Integer.MAX_VALUE;

}
