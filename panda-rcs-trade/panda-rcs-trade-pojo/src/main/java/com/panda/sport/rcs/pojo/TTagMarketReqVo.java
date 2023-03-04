package com.panda.sport.rcs.pojo;

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
public class TTagMarketReqVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private int id;

    /**
     * 标签ID
     */
    private Integer tagId;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 等级ID
     */
    private Integer levelId;

    /**
     * 等级ID
     */
    private String levelName;

    /**
     * 赔率增减
     */
    private BigDecimal oddsValue;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 最后修改时间
     */
    private Long updateTime;

}
