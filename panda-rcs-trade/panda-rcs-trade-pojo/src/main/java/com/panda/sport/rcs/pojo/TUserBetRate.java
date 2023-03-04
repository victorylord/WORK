package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Description  用户货量百分比表
 * @Param
 * @Author  kir
 * @Date  17:02 2021/8/26
 * @return
 **/
@Data
public class TUserBetRate implements Serializable {
    /**
     * 用户id
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 用户id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    /**
     * 赛种id
     */
    private Integer sportId;

    /**
     * 货量百分比（3位整数2位小数）
     */
    private BigDecimal betRate;
}
