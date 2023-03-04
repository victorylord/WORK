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
public class TTagMarket implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id")
    private Integer id;

    /**
     * 标签ID
     */
    private Integer tagId;

    /**
     * 等级ID
     */
    private Integer levelId;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 最后修改时间
     */
    private Long updateTime;

}
