package com.panda.sport.rcs.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

@Data
public class RcsMarketSellPersonGroup implements Serializable {
    /**
     * 表ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 操盘者用户ID
     */
    private Long userId;
    /**
     * 运动ID
     */
    private Long sportId;
    /**
     * 特殊关注人员ID
     */
    private Long personId;
    /**
     * 0失效 1有效
     */
    private Integer isValid;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 更新时间
     */
    private Long updateTime;
}
