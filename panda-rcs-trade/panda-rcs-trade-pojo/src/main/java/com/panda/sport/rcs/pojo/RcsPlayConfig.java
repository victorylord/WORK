package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;

/**
 * @author :  myname
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.pojo.statistics
 * @Description :  TODO
 * @Date: 2020-02-17 18:46
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class RcsPlayConfig extends RcsBaseEntity<RcsPlayConfig> {
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
     * 玩法id
     */
    private Integer playId;
    /**
     * 00, "开盘"  2, "关盘" 1, "封盘"11, "锁盘"
     */
    private Integer status;
    /**
     * 自动或者手动
     * 是否使用数据源0：手动；1：自动
     */
    private Integer dataSource;
}
