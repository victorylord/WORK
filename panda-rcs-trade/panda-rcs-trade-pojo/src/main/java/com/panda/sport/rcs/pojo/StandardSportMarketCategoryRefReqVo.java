package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;

import java.util.Map;

/**
 * <p>
 * 标准玩法ref表
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-10-07
 */
@Data
public class StandardSportMarketCategoryRefReqVo extends RcsBaseEntity<StandardSportMarketCategoryRefReqVo> {

    private static final long serialVersionUID = 1L;
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

    private Map<String, Object> language;

    /**
     * 比分矩阵类型 1.比分全场 2.比分半场 3.比分下半场 4.角球全场 5.角球半场 6.加时全场 7.加时半场 8.点球
     */
    private Integer type;

}
