package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * <p>
 * 资源表
 * </p>
 *
 * @author admin
 * @since 2019-10-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RcsResource extends RcsBaseEntity<RcsResource> {


    @TableId(value = "res_id", type = IdType.AUTO)
    private Integer resId;

    /**
     * 资源名称
     */
    private String resName;

    /**
     * 资源值
     */
    private String resVal;

    /**
     * 资源编码
     */
    private String resCode;

    /**
     * 备注
     */
    private String note;
    @TableField(exist = false)
    private Timestamp crtTime;
    @TableField(exist = false)
    private Timestamp updateTime;

    private Integer status;


    @Override
    public Serializable pkVal() {
        return this.resId;
    }

}
