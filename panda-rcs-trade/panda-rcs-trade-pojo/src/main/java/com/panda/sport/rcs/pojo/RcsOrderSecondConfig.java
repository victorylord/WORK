package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author :  carver
 * @Project Name :  panda-rcs-trade
 * @Package Name :  com.panda.sport.rcs.pojo
 * @Description :  TODO
 * @Date: 2020-11-17 20:20
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RcsOrderSecondConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 表ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 用户ID
     */
    private Long uid;
    /**
     * 标准赛事id
     */
    private Long matchInfoId;
    /**
     * 玩法集id
     */
    private Long playSetId;
    /**
     * 秒接状态 0 关闭秒接  1 开启秒接
     */
    private int secondStatus;
    /**
     *投注金额
     */
    private Long betAmount;
    /**
     * 用户等级
     */
    private String userLevel;
    /**
     * 操盘手名称
     */
    private String trader;

    private Long createTime;
    private Long updateTime;
}
