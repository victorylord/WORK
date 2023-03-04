package com.panda.sport.rcs.pojo.report;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * @author :  toney
 * @Project Name :  panda-rcs
 * @Package Name :  com.panda.rcs.flink.pojo
 * @Description :  投注日期
 * @Date: 2019-12-24 13:49
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class RcsOrderStatisticSettleTime extends BaseRcsOrderStatisticTime {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 投注日期
     */
    private Date settleDate;

    public RcsOrderStatisticSettleTime(){}

    public RcsOrderStatisticSettleTime(Date settleDate, Integer sportId, Integer tournamentId, Integer matchType, Integer playId, Integer orderStatus) {
        super(sportId, tournamentId, matchType, playId, orderStatus);
        this.settleDate = settleDate;
    }

    public RcsOrderStatisticSettleTime(CalcSettleItem calcSettleItem) {
        super(calcSettleItem);
        this.settleDate = calcSettleItem.getSettleDate();
    }
}
