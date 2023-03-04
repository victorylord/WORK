package com.panda.sport.rcs.pojo.report;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author :  holly
 * @Project Name :rcs-parent
 * @Package Name :com.panda.sport.data.rcs.dto
 * @Description :
 * @Date: 2019-10-08 17:08
 */
@Data
public class CalcSettleItem implements Serializable {

    private static final long serialVersionUID = 3470224029895086736L;

    /**
     * 赛事日期
     */
    private Date matchDate;
    /**
     * 投注日期
     */
    private Date betDate;
    /**
     * 结算日期
     */
    private Date settleDate;
    /**
     * 赛种
     */
    private Integer sportId;
    /**
     * 联赛ID
     */
    private Integer tournamentId;
    /**
     * 盘口阶段
     */
    private Integer matchType;
    /**
     * 玩法ID
     */
    private Integer playId;


    /**
     * 用户id
     */
    private Long uid;

    /**
     * 注单金额
     */
    private BigDecimal betAmount;

    /**
     * 结算金额(最终要除以100，并按照四舍六入五成双取2位小数)
     */
    private BigDecimal settleAmount;
    /**
     * 是否已派彩 0：未派彩 1：已派彩
     */
    private Integer settleStatus;

    /**
     * 结算类型(1:自动结算，0：手工结算)
     */
    private Integer settleType = 1;


    /**
     * 订注单状态(1 有效 2 无效 3 已拒绝）
     */
    private Integer orderStatus;

    private Long betNo;

    /**
     * @return java.lang.String
     * @Description //key
     * @Param []
     * @Author kimi
     * @Date 2019/12/24
     **/
    public String getMatchKey() {
        return this.getMatchDate() + "," + this.getSportId() + "," + this.getTournamentId() + "," + this.getMatchType() + "," + this.getPlayId() + "," + this.getOrderStatus();
    }

    /**
     * @return java.lang.String
     * @Description //key
     * @Param []
     * @Author kimi
     * @Date 2019/12/24
     **/
    public String getBetKey() {
        return this.getBetDate() + "," + this.getSportId() + "," + this.getTournamentId() + "," + this.getMatchType() + "," + this.getPlayId() + "," + this.getOrderStatus();
    }

    /**
     * @return java.lang.String
     * @Description //key
     * @Param []
     * @Author kimi
     * @Date 2019/12/24
     **/
    public String getSettleKey() {
        return this.getSettleDate() + "," + this.getSportId() + "," + this.getTournamentId() + "," + this.getMatchType() + "," + this.getPlayId() + "," + this.getOrderStatus();
    }
}
