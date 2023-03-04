package com.panda.sport.rcs.pojo;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 商户单场限额监控表
 * </p>
 *
 * @author lithan
 * @since 2021-11-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class MerchantsSinglePercentage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 体育id
     */
    private Integer sportId;

    /**
     * 赛事id
     */
    private Long matchId;

    /**
     * 赛事match_manage_id
     */
    private String matchManageId;


    /**
     * 商户id
     */
    private Long merchantsId;

    /**
     * 商户名称
     */
    private String merchantsName;

    /**
     * 阶段 早盘/滚球
     */
    private Integer matchType;

    /**
     * 主队名字称
     */
    private String homeName;

    /**
     * 客队名字
     */
    private String awayName;

    /**
     *
     */
    private String matchInfo;


    /**
     * 单场限额
     */
    private Long matchLimit;

    /**
     * 额度已使用百分比
     */
    private BigDecimal percentage;


    /**
     * 数据状态 0已过期 1正在使用
     */
    private Integer status;

}
