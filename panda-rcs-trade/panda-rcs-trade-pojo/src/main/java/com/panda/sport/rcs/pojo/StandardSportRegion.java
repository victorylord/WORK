package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.Objects;

import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 标准体育区域表
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-09-26
 */
@Data
/*@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)*/
public class StandardSportRegion extends RcsBaseEntity<StandardSportRegion> {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 如果当前记录对外起作用，则该visible 为 1，否则为 0。默认true
     */
    private Integer visible;

    /**
     * 区域名称编码. 用于多语言。存放体育区域名称
     */
    private Long nameCode;

    /**
     * 介绍，默认为空
     */
    private String introduction;

    /**
     * 区域名称大写字母拼写
     */
    private String spell;

    private String remark;

    private Long createTime;

    private Long modifyTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        StandardSportRegion that = (StandardSportRegion) o;
        return Objects.equals(id, that.id) ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id);
    }
}
