package com.panda.sport.rcs.vo.statistics;

import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import com.panda.sport.rcs.vo.OrderDetailStatReportVo;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.vo.statistics
 * @Description :  TODO
 * @Date: 2020-01-13 16:30
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class ProfitDetailStatReportVo extends RcsBaseEntity<ProfitDetailStatReportVo> implements Serializable {
    /**
     * 赛事id
     */
    private Long matchId;

    /**
     * 玩法id
     */
    private Integer playId;

    /**
     * @Description   盘口id
     * @Param
     * @Author  myname
     * @Date  20:46 2020/1/15
     * @return
     **/
    private Long marketId;

    /**
     * 数据集
     */
    private List<OrderDetailStatReportVo> data;
}
