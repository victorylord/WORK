package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author admin
 * @since 2019-10-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RcsControlLog extends RcsBaseEntity<RcsControlLog> {


    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 调用id
     */
    private String traceId;

    /**
     * 服务id
     */
    private String serviceId;

    /**
     * 操作模块
     */
    private String handleModel;

    /**
     * 操作名称
     */
    private String handleName;

    /**
     * 操作用户
     */
    private String userId;

    private LocalDateTime crtTime;

    /**
     * 修改之前的值
     */
    private String oldData;

    /**
     * 修改后的值
     */
    private String newData;

    private LocalDateTime updateTime;


    @Override
    public Serializable pkVal() {
        return this.id;
    }

}
