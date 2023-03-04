package com.panda.sport.rcs.pojo;

/**
 * @author :  myname
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.pojo
 * @Description :  TODO
 * @Date: 2020-01-15 15:08
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;
import org.apache.commons.lang3.math.NumberUtils;

import java.math.BigDecimal;

/**
 * @Description //玩法配置表
 * @Param
 * @Author kimi
 * @Date 2020/1/15
 * @return
 **/
@Data
public class RcsMatchPlayConfig extends RcsBaseEntity<RcsMatchPlayConfig> {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 赛事id
     */
    private Long matchId;
    /**
     * 玩法阶段id
     */
    private Integer playId;
    /**
     * 盘口差
     */
    private BigDecimal marketHeadGap;
    /**
     * 玩法水差
     */
    private BigDecimal awayAutoChangeRate;
    /**
     * 水差是否关联，0-不关联，1-关联
     */
    private Integer relevanceType;
    /**
     * 玩法状态  0开1封
     */
    private Integer status;
    /**
     *  子玩法id
     */
    private String subPlayId;
    /**
     * 自动或者手动
     * 是否使用数据源0：自动；1：手动
     */
    private Integer dataSource;

    public Long subPlayId() {
        if (subPlayId == null) {
            return playId.longValue();
        }
        return NumberUtils.toLong(subPlayId);
    }
}
