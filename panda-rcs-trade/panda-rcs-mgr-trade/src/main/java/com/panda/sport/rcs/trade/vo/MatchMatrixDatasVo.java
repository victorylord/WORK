package com.panda.sport.rcs.trade.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @program: xindaima
 * @description:
 * @author: kimi
 * @create: 2021-01-13 14:05
 **/
@Data
public class MatchMatrixDatasVo {
    private List<Map<String, Object>> list;
    private Integer pageNum;
    private Integer pageSize;
    private Integer total;
}
