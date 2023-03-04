package com.panda.rcs.warning.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;


/**
 * <p>
 *  赛事
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-10-07
 */
@Data
public class StandardMatchInfo extends RcsBaseEntity<StandardMatchInfo> {

    private static final long serialVersionUID = 1L;

    /**
     * id. id
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    /**
     * 赛事状态.  比如:未开赛, 滚球, 取消, 延迟等.
     */
    private Integer matchStatus;
    /**
     * 比赛开盘标识. 0: 未开盘; 1: 开盘; 2: 关盘; 3: 封盘; 开盘后用户可下注
     */
    private Integer operateMatchStatus;

}
