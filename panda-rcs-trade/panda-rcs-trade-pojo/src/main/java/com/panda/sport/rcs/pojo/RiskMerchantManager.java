package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 商户管控记录表
 * </p>
 *
 * @author lithan
 * @since 2022-03-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RiskMerchantManager implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 商户编号
     */
    private String merchantCode;

    /**
     * 风控类型,1.投注特征标签,2特殊限额,3特殊延时,4提前结算,5赔率分组
     * 枚举 RiskMerchantManagerTypeEnum
     */
    private Integer type;

    /**
     * 风控建议设置值
     */
    private String recommendValue;

    /**
     * 商户后台显示值(备用)
     */
    private String merchantShowValue;

    /**
     * 风控补充说明
     */
    private String supplementExplain;

    /**
     * 风控建议人
     */
    private String riskOperator;

    /**
     * 风控建议时间
     */
    private Long recommendTime;

    /**
     * 商户处理时间
     */
    private Long processTime;

    /**
     * 商户处理人
     */
    private String merchantOperator;

    /**
     * 商户处理说明
     */
    private String merchantRemark;

    /**
     * 状态:0待处理,1同意,2拒绝,3强制执行
     * 枚举 RiskMerchantManagerStatusEnum
     */
    private Integer status;

    /**
     * 接口请求数据(用于原接口请求 原json数据保存)
     */
    private String requestData;

    /**
     * 更新时间
     */
    private Long updateTime;


}
