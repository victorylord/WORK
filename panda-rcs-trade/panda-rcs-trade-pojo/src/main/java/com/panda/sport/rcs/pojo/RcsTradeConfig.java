package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author :  myname
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.pojo
 * @Description :  操盘操作配置表
 * @Date: 2020-03-05 16:25
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class RcsTradeConfig extends RcsBaseEntity<RcsTradeConfig> {

    private static final long serialVersionUID = 1L;

    /**
     * 表ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * 赛事Id
     */
    private String matchId;
    /**
     * 级别  1赛事  2玩法  3盘口   如果是玩法阶段 id为玩法用，隔开
     */
    private Integer traderLevel;
    /**
     * 赛事id 玩法id 盘口id
     */
    private String targerData;
    /**
     * 附加字段1，保存盘口位置状态时传玩法ID
     */
    private String addition1;
    /**
     * 子玩法ID
     */
    private Long subPlayId;
    /**
     * 1手动 0自动
     */
    private Integer dataSource;
    /**
     * 0开2关1封11锁
     */
    private Integer status;

    /**
     * 来源类型
     */
    private Integer sourceType;

    /**
     * 操盘平台 MTS ，PA
     */
    @TableField(exist = false)
    private String riskManagerCode;

    /**
     * 更新玩家
     */
    private String updateUser;

    /**
     * 状态公共配置
     *
     * @param matchId
     * @param tradeLevel
     * @param status
     * @param linkedType
     * @param userId
     */
    public RcsTradeConfig(Long matchId, Integer tradeLevel, Integer status, Integer linkedType, Integer userId) {
        this.matchId = String.valueOf(matchId);
        this.traderLevel = tradeLevel;
        this.status = status;
        this.sourceType = linkedType;
        this.updateUser = String.valueOf(userId);
    }

}
