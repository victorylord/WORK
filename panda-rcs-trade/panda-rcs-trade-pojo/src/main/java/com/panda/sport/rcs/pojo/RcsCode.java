package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * <p>
 * 数据字典
 * </p>
 *
 * @author admin
 * @since 2019-10-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RcsCode extends RcsBaseEntity<RcsCode> {


    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String fatherKey;

    private String childKey;

    private String value;

    private String remark;

    @TableField(exist = false)
    private Timestamp crtTime;

    @TableField(exist = false)
    private Timestamp updateTime;

    /**
     * 1：有效  2：无效
     */
    private Integer status;


    @Override
    public Serializable pkVal() {
        return this.id;
    }

}
