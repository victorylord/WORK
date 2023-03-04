package com.panda.sport.rcs.pojo.statistics;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.pojo.statistics
 * @Description :  近一时货量统计
 * @Date: 2019-12-30 20:02
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Data
public class RcsTotalValueNearlyOneTime {
    /**
     * 赛事Id
     */
    @TableId(value = "match_id", type = IdType.INPUT)
    private Long matchId;
    /**
     * 订单详情Id
     */
    private Long orderDetailId;
    /**
     * 更新时间
     */
    private Long updateTime;
}
