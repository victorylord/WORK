package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 * 结算表
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-10-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TSettle implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 注单号
     */
    @TableId(value = "bet_no")
    private String betNo;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户id
     */
    private Long uid;

    /**
     * 结算状态（1赢，2输，3赢半，4输半，5走盘，6赛事取消，7赛事延期，8赛果不一致，9程序出现异常导致结算失败）
     */
    private Integer outCome;

    /**
     * 结算金额(最终要除以10000，并按照四舍六入五成双取2位小数)
     */
    private Long settleAmount;

    /**
     * 是否已派彩 0：未派彩 1：已派彩
     */
    private Integer settleStatus;

    /**
     * 结算类型(1:自动结算，0：手工结算)
     */
    private Integer settleType;

    /**
     * 结算时间
     */
    private Long settleTime;

    /**
     * 最终赔率
     */
    private Double oddFinally;

    /**
     * 创建用户
     */
    private String createUser;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 修改人
     */
    private String modifyUser;

    /**
     * 修改时间
     */
    private Long modifyTime;

    /**
     * 0:未删除，1已删除
     */
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;

    /**
     * 注单本金，x100后的本金
     */
    private Long betAmount;
    /**
     * 0 未计算 1 已计算
     */
    private Integer calcStatus;
    /**
     * 串关类型(1：单关(默认)  )
     */
    private Integer seriesType;

}
