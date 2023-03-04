package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 行情等级表
 * </p>
 *
 * @author CodeGenerator
 * @since 2021-04-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TTagMarketLevel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id")
    private Integer id;

    /**
     * 等级
     */
    private String levelName;

    /**
     * 赔率增减
     */
    private BigDecimal oddsValue;

}
