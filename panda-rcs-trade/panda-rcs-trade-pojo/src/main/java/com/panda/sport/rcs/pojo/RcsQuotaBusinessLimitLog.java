package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * 商户通用设置日志
 *
 * @date 2022/07/23
 */
@Data
public class RcsQuotaBusinessLimitLog implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 操作类别
     */
    private String operateCategory;

    /**
     * 操作对象id
     */
    private String objectId;

    /**
     * 操作对象名称
     */
    private String objectName;

    /**
     * 操作对象拓展id
     */
    private String extObjectId;

    /**
     * 操作对象拓展名称
     */
    private String extObjectName;

    /**
     * 操作类型
     */
    private String operateType;

    /**
     * 操作参数名称
     */
    private String paramName;

    /**
     * 修改前参数值
     */
    private String beforeVal;

    /**
     * 修改后参数值
     */
    private String afterVal;

    /**
     * 操作人id
     */
    private String userId;

    /**
     * 操作人名称
     */
    private String userName;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 修改时间
     */
    private String updateTime;

}