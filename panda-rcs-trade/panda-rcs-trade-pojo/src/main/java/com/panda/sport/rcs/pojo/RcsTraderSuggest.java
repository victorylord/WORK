package com.panda.sport.rcs.pojo;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 操盘手贴标提醒风控
 * </p>
 *
 * @author ${author}
 * @since 2022-04-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RcsTraderSuggest implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * 主键
     */
      @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 商户id
     */
    private Long businessId;

    /**
     * 商户名称
     */
    private String businessName;

    /**
     * 操盘手建议
     */
    private String traderSuggest;

    /**
     * 操盘手建议补充说明
     */
    private String traderSuggestReplenish;

    /**
     * 建议时间,新建时间
     */
      @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 操盘建议人账户名
     */
    private String proposer;
    
    
    /**
     * 	风控处理类型：0：其他、1:殊限额、2:赔率分组、3：提前结算、4：投注延时、5：投注特征标签
     */
    private Integer riskProcesType;

    /**
     * 风控处理状态：0:未处理、1:已设置、2:忽略
     */
    private Integer riskProcesStatus;

    /**
     * 风控处理人
     */
    private String riskHandler;

    /**
     * 风控处理时间
     */
    private Date riskProcesTime;

    /**
     * 风控处理说明
     */
    private String riskRemark;


}
