package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 标准玩法ref表
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-10-07
 */
@Data
public class StandardSportMarketCategoryRef extends RcsBaseEntity<StandardSportMarketCategoryRef> {

    private static final long serialVersionUID = 1L;
    /**
     * 表ID, 自增
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    /**
     * 运动种类id.  对应表 sport.id
     */
    private Long sportId;

    private Integer categoryId;

    private Integer scopeId;

    /**
     * 玩法名称编码. 用于多语言.
     */
    private Long nameCode;

    /**
     * 玩法状态. 0已关闭; 1已创建; 2待二次校验; 3已开启; .  默认已创建
     */
    private Integer status;

    private Integer orderNo;

}
