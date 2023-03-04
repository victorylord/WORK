package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author :  kimi
 * @Project Name :  daima1
 * @Package Name :  com.panda.sport.rcs.pojo
 * @Description :我的收藏表
 * @Date: 2019-10-25 14:26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RcsMatchCollection extends RcsBaseEntity<RcsMatchCollection> {
    /**
     * 表ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 联赛ID
     */
    private Long tournamentId;
    /**
     * 赛事ID
     */
    private Long matchId;
    /**
     * 玩家id
     */
    private Long userId;

    /**
     * 体育种类id
     */
    private Long sportId;

    /**
     * 1为赛事  2为联赛
     */
    private Integer type;
    

    /**
     * 0取消收藏 1添加收藏
     */
    private Integer status;

    /**
     * 类型：1 ：早盘 ，2： 滚球盘， 3： 冠军盘
     */
    @TableField(exist = false)
    private Integer matchType;

    /*** 盘口类型  PRE :早盘    LIVE :滾球 */
    @TableField(exist = false)
    private String marketType;
    
    private Long beginTime;

    private Date createTime;

    private Date updateTime;

}
