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
 * 标准玩法投注项表
 * </p>
 * @author CodeGenerator
 * @since 2019-10-07
 */
@Data
/*@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)*/
public class StandardSportOddsFieldsTemplet extends RcsBaseEntity<StandardSportOddsFieldsTemplet> {

    private static final long serialVersionUID = 1L;

    /**
     * 表ID, 自增
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    /**
     * 运动种类id.  对应表 sport.id
     */
    private Long marketCategoryId;

    /**
     * 玩法名称编码. 用于多语言.
     */
    private Long nameCode;

    /**
     * 投注项名称. 
     */
    private String name;

    /**
     * 排序值. 
     */
    private Integer orderNo;

    /**
     * 附件字段1
     */
    private String addition1;

    /**
     * 附件字段2
     */
    private String addition2;

    /**
     * 附件字段3
     */
    private String addition3;

    /**
     * 创建时间. UTC时间, 精确到毫秒
     */
    private Long createTime;

    /**
     * 更新时间. UTC时间, 精确到毫秒
     */
    private Long modifyTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        StandardSportOddsFieldsTemplet that = (StandardSportOddsFieldsTemplet) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id);
    }
}
