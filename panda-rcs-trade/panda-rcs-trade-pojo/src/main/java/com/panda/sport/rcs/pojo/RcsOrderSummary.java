package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author :  myname
 * @Project Name :  注单总投注额最高的赔率
 * @Package Name :  com.panda.sport.rcs.pojo
 * @Description :  TODO
 * @Date: 2020-07-07 11:14
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class RcsOrderSummary extends RcsBaseEntity<RcsOrderSummary> {
    private static final long serialVersionUID = -4601996481366551580L;
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 运动种类Id
     */
    private Long sportId;
    /**
     * 赛事id
     */
    private Long matchId;
    /**
     * 玩法Id
     */
    private Long playId;
    /**
     * 盘口Id
     */
    private Long marketId;
    /**
     * 投注项id
     */
    private Long oddsId;
    /**
     * 总投注额最多的赔率   没有的话取当前赔率
     */
    private BigDecimal oddsValueMax;
}
