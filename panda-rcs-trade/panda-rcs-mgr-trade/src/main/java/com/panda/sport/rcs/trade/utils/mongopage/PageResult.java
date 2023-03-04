package com.panda.sport.rcs.trade.utils.mongopage;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果.
 * @author Ryan
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class PageResult<T> {

    private Integer pageNum;

    private Integer pageSize;

    private Long total;

    private Integer pages;

    private List<T> list;
}