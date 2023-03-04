package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @program: xindaima
 * @description:
 * @author: kimi
 * @create: 2021-01-13 20:26
 **/
@Data
public class RcsFirstMarket {
    /**
     * 表ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 表ID，自增
     */
    private Long standardMatchId;
    /**
     * 表ID，自增
     */
    private Integer playId;
    /**
     * 表ID，自增
     */
    private String value;

    private Integer type;
}
