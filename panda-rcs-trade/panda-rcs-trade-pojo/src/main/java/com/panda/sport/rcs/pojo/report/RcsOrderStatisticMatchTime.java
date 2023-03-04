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
public class RcsOrderStatisticMatchTime extends BaseRcsOrderStatisticTime {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 比赛开始日期
     */
    private Date matchDate;

    public  RcsOrderStatisticMatchTime (){}

    public RcsOrderStatisticMatchTime(Date matchDate, Integer sportId, Integer tournamentId, Integer matchType, Integer playId, Integer orderStatus) {
        super(sportId, tournamentId, matchType, playId, orderStatus);
        this.matchDate = matchDate;
    }

    public RcsOrderStatisticMatchTime(CalcSettleItem calcSettleItem) {
        super(calcSettleItem);
        this.matchDate = calcSettleItem.getMatchDate();
    }

}
