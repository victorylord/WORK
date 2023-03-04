package com.panda.sport.rcs.vo.operation;

import lombok.Data;
import org.omg.CORBA.INTERNAL;

import java.math.BigDecimal;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.rpc.operation.bean
 * @Description :  实货量查询传递bean
 * @Date: 2019-10-25 14:11
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class RealTimeVolumeBean {
    /**
     * 赛事编号
     */
    private Long matchId;

    /**
     * 赛事类型
     */
    private String matchType;


    /**
     * 标准联赛 id.
     */
    private Long standardTournamentId;

    /**
     * 玩法ID
     */
    private Integer playId;


    private Integer sportId;

    /**
     * 盘口ID
     */
    private Long matchMarketId;


    /**
     * 投注项ID
     */
    private Long playOptionsId;
    /**
     * 实货量
     */
    private BigDecimal sumMoney;
    /**
     * 期望值
     */
    private BigDecimal profitValue;
    /**
     * 投注量
     */
    private BigDecimal betOrderNum;
    /**
     * @Description   /最大赔付金额
     * @Param
     * @Author  toney
     * @Date  14:11 2020/1/27
     * @return
     **/
    private BigDecimal paidAmount;

}
