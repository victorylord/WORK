package com.panda.sport.rcs.pojo;

import lombok.Data;

import java.util.Date;

/**
 * 系统用户信息表
 */
@Data
public class RcsSysUser {
    /**
     * 主键
     */
    private Long id;

    /**
     * 账号(注意为员工的英文名)
     */
    private String userCode;

    /**
     * 工作编号（仅限12位数字）
     */
    private String workCode;
    /**
     * 部门id
     */
    private Integer orgId;
    /**
     * app名称
     */
    private String appName;
    /**
     * 职职id
     */
    private Integer positionId;
    /**
     * 用户标识
     */
    private String userFlag;

    /**
     * 启动,1：启用，0禁用
     */
    private Integer enabled;

    /**
     * 逻辑删除(1删除，0未删除)
     */
    private Integer logicDelete;

    /**
     * 角色
     */
    private String roles;

    private Date createTime;

    private Date updateTime;
}