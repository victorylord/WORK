package com.panda.sport.rcs.pojo.statistics;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author :  toney
 * @Project Name :  panda-rcs
 * @Package Name :  com.panda.sport.rcs.pojo.statistics
 * @Description :  盘口级别
 * @Date: 2019-12-11 15:17
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class RcsProfitMarket extends RcsBaseEntity<RcsProfitMarket> implements Serializable {
    /**
     * 赛事Id
     */
    @TableId(value = "match_id")
    private Long matchId;
    /**
     * 1：早盘2滚球
     */
    @TableField(value = "match_type")
    private String matchType;
    /**
     * 玩法Id
     */
    @TableField(value = "play_id")
    private Integer playId;
    /**
     * 盘口值
     */
    @TableField(value = "market_value")
    private String marketValue;
    /**
     * 当play_id=4为主队；当play_id=6为大；当play_id=8为单
     */
    private BigDecimal addition1;

    /**
     * 当play_id=4为客队；当play_id=6为小；当play_id=8为双
     */
    private BigDecimal addition2;
    /**
     * 期望差
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
     * @Description   设置参数1
     * @Param [addition1]
     * @Author  toney
     * @Date  17:23 2019/12/14
     * @return void
     **/
    public void setAddition1(BigDecimal addition1) {
        this.addition1 = addition1;
    }
    /**
     * @Description   设置参数2
     * @Param [addition2]
     * @Author  toney
     * @Date  17:23 2019/12/14
     * @return void
     **/
    public void setAddition2(BigDecimal addition2) {
        this.addition2 = addition2;
    }
    /**
     * @Description   设置期望值
     * @Param [profitValue]
     * @Author  toney
     * @Date  17:23 2019/12/14
     * @return void
     **/
    public void setProfitValue(BigDecimal profitValue) {
        this.profitValue = profitValue;
    }
}
