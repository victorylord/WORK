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
import com.baomidou.mybatisplus.annotation.TableId;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;

/**
 * @Description //玩法配置表
 * @Param
 * @Author kimi
 * @Date 2020/1/15
 * @return
 **/
@Data
public class RcsMatchPlayConfigLogs extends RcsBaseEntity<RcsMatchPlayConfigLogs> {
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
     * 玩法状态  0开1封
     */
    private Integer status;
    /**
     * 自动或者手动
     * 是否使用数据源0：手动；1：自动
     */
    private Integer dataSource;
}
