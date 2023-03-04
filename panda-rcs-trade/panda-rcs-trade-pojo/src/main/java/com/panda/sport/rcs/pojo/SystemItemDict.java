package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author :  myname
 * @Project Name :  daima1
 * @Package Name :  com.panda.sport.rcs.pojo
 * @Description :  TODO
 * @Date: 2019-10-16 11:41
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SystemItemDict extends RcsBaseEntity<SystemItemDict> {
    private static final long serialVersionUID = 1L;
    /**
     * 表ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 字典类型id.system_type_dict.id
     */
    private Long parentTypeId;

    /**
     * 项目编码
     */
    private String code;
    /**
     * 项目值.
     */
    private String value;
    /**
     * 是否激活.1:激活;0:没有激活.
     */
    private Integer active;
    /**
     * 描述信息.
     */
    private String description;
    /**
     * 字典类型id.system_type_dict.id
     */
    private String addition1;
    /**
     * 备注.remark
     */
    private String remark;
    /**
     * 字典类型id.system_type_dict.id
     */
    @TableField(exist = false)
    private Long createTime;
    /**
     * 字典类型id.system_type_dict.id
     */
    @TableField(exist = false)
    private Long modify_time;
}
