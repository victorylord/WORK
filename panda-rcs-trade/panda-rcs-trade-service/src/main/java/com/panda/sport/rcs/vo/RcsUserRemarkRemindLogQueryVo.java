package com.panda.sport.rcs.vo;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * @author derre
 * @date 2022-03-28
 */
@Data
public class RcsUserRemarkRemindLogQueryVo extends PageQuery {

    /**
     * 商户编号
     */
    private Set<String> merchantCode = new HashSet<>();

    /**
     * 用户名/用户id
     */
    private String username;

    /**
     * 操作用户
     */
    private String createUserName;

    /**
     * 提醒日期
     */
    private String remindDate;
    /**
     * 备注开始时间
     */
    private String startCreateTime;
    /**
     * 备注结束时间
     */
    private String endCreateTime;
    /**
     * 导出总数
     * */
    private Long totalCount;
}
