package com.panda.sport.rcs.pojo;

import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 标准体育种类表.
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-10-07
 */
@Data
/*@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)*/
public class StandardSportType extends RcsBaseEntity<StandardSportType> {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    //@TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 体育名称编码. 用于多语言.存放体育种类名称
     */
    private Long nameCode;

    /**
     * 当前运动的介绍. 默认为空
     */
    private String introduction;

    private String remark;

    private Long createTime;

    private Long modifyTime;


}
