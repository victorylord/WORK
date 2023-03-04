package com.panda.sport.rcs.pojo.statistics;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author :  toney
 * @Project Name :  panda-rcs
 * @Package Name :  com.panda.sport.rcs.pojo.statistics
 * @Description :  期望矩阵
 * @Date: 2019-12-11 16:36
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class RcsProfitRectangle extends RcsBaseEntity<RcsProfitRectangle> implements Serializable {
    /**
     * 赛事Id
     */
    @TableId(value = "match_id")
    private Long matchId;
    /**
     * 玩注id
     */
    @TableField(value = "play_id")
    private Integer playId;
    /**
     * 分数
     */
    @TableField(value = "score")
    private Integer score;
    /**
     * 期望值
     */
    private BigDecimal profitValue;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 类型：1 ：早盘 ，2： 滚球盘， 3： 冠军盘
     */
    private Integer matchType;
}
