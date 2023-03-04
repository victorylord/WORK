package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 * @program: xindaima
 * @description:
 * @author: kimi
 * @create: 2021-03-06 16:26
 **/
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class RcsMerchantLimitWarning extends RcsBaseEntity<RcsMerchantLimitWarning> implements Serializable {
    /**
     * 表ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 商户ID
     */
    private Long businessId;
    /**
     * 商户单日额度
     */
    private Integer businessSingleDayLimit;
    /**
     * 已用额度
     */
    private BigDecimal amountUsed;
    /**
     * 商户id
     */
    @TableField(exist = false)
    private String businessName;
    /**
     * 预警消息及创建时间
     */
    private String createTime;

    /**
     * 代理名称
     */
    private String creditName;

    /**
     * 信用代理ID
     */
    private String creditId;
}
