package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 盘口设置表
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-10-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RcsMatchEventTypeInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    private static Integer DEFAULT_EVENT_PAGE_SIZE = 50;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**赛事种类*/
    private Integer sportId;
    /**事件源编码*/
    private String dataSourceCode;
    /**事件编码*/
    private String eventCode;
    /**事件类型*/
    private Integer eventType;
    /**事件编码*/
    private String eventName;
    /**事件说明*/
    private String eventDescription;
    /**事件备注*/
    private String eventRemark;
    /**创建时间*/
    private Long createTime;
    /**修改时间*/
    private Long modifyTime;
    /**修改时间*/
    private Date updateTime;
    /**页码*/
    @TableField(exist = false)
    private Integer pageNumber;
    /**每页数量*/
    @TableField(exist = false)
    private Integer pageSize;
    /**每页数量*/
    @TableField(exist = false)
    private Integer current;

    private String lang;

    private String eventEnName;

    public Integer getCurrent(){
        Integer current = 0;
        if (ObjectUtils.isNotEmpty(this.pageNumber) && ObjectUtils.isNotEmpty(this.pageSize)){
            current = (this.pageNumber - 1) * this.pageSize;
        }
        return current;
    }
}
