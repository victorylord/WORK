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
public class RcsOrderStatisticBetTime extends BaseRcsOrderStatisticTime {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 投注日期
     */
    private Date betDate;

    public RcsOrderStatisticBetTime(){}
    /**
     * @return
     * @Description 初始化
     * @Param []
     * @Author toney
     * @Date 21:41 2019/12/24
     **/
    public RcsOrderStatisticBetTime(CalcSettleItem calcSettleItem) {
        super(calcSettleItem);
        this.betDate = calcSettleItem.getBetDate();
    }


    public RcsOrderStatisticBetTime(Date calcDate, Integer sportId, Integer tournamentId, Integer matchType, Integer playId, Integer orderStatus) {
        super(sportId, tournamentId, matchType, playId, orderStatus);
        this.betDate = calcDate;
    }

}
