package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 用戶人工备注提醒日志
 *
 * @description:
 * @author: magic
 * @create: 2022-05-29 10:15
 **/
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class RcsUserRemarkRemindLog extends RcsBaseEntity<RcsUserRemarkRemindLog> {
    /**
     * 表ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 用户ID
     */
    private String userId;
    /**
     * 用户名
     */
    private String username;
    /**
     * 商户编码
     */
    private String merchantCode;
    /**
     * 备注
     */
    private String remark;
    /**
     * 提醒日期
     */
    private String remindDate;
    /**
     * 创建时间
     */
    private String createTime;
    /**
     * 创建人id
     */
    private Long createUserId;
    /**
     * 创建人用户名
     */
    private String createUserName;
}
