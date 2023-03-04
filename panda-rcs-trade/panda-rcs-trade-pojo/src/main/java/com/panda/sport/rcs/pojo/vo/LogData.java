package com.panda.sport.rcs.pojo.vo;

import lombok.Data;

/**
 * @program: xindaima
 * @description:
 * @author: kimi
 * @create: 2021-02-05 16:52
 **/
@Data
public class LogData {
    /**
     * 变化的字段
     */
    private String name;
    /**
     * 老的数据
     */
    private String oldData;
    /**
     * 新的数据
     */
    private String Data;
    /**
     * 1限时  2限额 3备注  4 操作人
     */
    private Integer type;
}
