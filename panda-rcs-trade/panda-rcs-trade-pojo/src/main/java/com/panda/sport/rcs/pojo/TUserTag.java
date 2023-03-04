package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 * 行情等级表
 * </p>
 *
 * @author CodeGenerator
 * @since 2021-04-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TUserTag implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 标签ID
     */
    @TableId(value="tag_id")
    private Integer tagId;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 标签类型
     */
    private Integer tagType;

}
