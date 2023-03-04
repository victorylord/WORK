package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RcsMtsOrderExt {
    /**
     * 表ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 订单编码
     */
    private String orderNo;

    /**
     * 返回状态
     */
    private String status;
    
    private String paAmount;
    
    private String mtsAmount;
    
    /**
     * 请求参数
     */
    private String requestJson;

    /**
     * 返回json文本
     */
    private String result;

    /**
     * 创建时间
     */
    private Date creTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 是否取消 0否  1是
     */
    private Integer cancelStatus;

    /**
     * 取消编码 101 普通 102 超时  103 后台取消 104 技术问题取消 105 后台异常取消  106 现金返还促销
     */
    private Integer cancelId;

    /**
     * 取消返回结果
     */
    private String  cancelResult;


}