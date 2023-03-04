package com.panda.sport.rcs.pojo.statistics;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.panda.sport.rcs.utils.LongToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 投注项/坑位-期望值/货量
 * </p>
 *
 * @author author
 * @since 2021-02-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("rcs_predict_bet_odds")
public class RcsPredictBetOdds implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 体育种类
     */
    private Long sportId;

    /**
     * 赛事ID
     */
    private Long matchId;

    /**
     * 玩法ID
     */
    private Integer playId;

    /**
     * 类型  1投注项 2坑位
     */
    private Integer dataType;

    /**
     * 盘口ID /坑位ID
     */
    @JSONField(serializeUsing = LongToStringSerializer.class)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dataTypeValue;

    /**
     * 投注项标识
     */
    private String oddsType;

    /**
     * 注单次数（投注项）
     */
    private BigDecimal betOrderNum;

    /**
     * 货量-纯投注额
     */
    private BigDecimal betAmount;

    /**
     * 货量-纯赔付额
     */
    private BigDecimal betAmountPay;

    /**
     * 货量-混合型（注单亚赔大于1的，货量为注单额*亚赔；注单亚赔小于或等于1的，货量为注单额）
     */
    private BigDecimal betAmountComplex;


    /**
     * 注单次数（投注项）
     */
    private BigDecimal betOrderNumTemp;

    /**
     * 货量-纯投注额
     */
    private BigDecimal betAmountTemp;

    /**
     * 货量-纯赔付额
     */
    private BigDecimal betAmountPayTemp;

    /**
     * 货量-混合型（注单亚赔大于1的，货量为注单额*亚赔；注单亚赔小于或等于1的，货量为注单额）
     */
    private BigDecimal betAmountComplexTemp;


    /**
     * 期望 盈利值(不包含本金)
     */
    private BigDecimal profitValue;

    /**
     * 联赛id
     */
    private Long standardTournamentId;

    /**
     * 修改时间
     */
    private Long modifyTime;

    /**
     * 主客 home 主 away 客
     */
    private String isHome;

    /**
     * 1.早盘  2.滚球
     */
    private Integer matchType;

    /**
     * 最大赔付金额
     */
    private BigDecimal paidAmount;

    /**
     * 子玩法ID  sub_play_id
     */
    private String subPlayId;

    public String groupField() {
        if (StringUtils.isBlank(subPlayId)) {
            subPlayId = String.valueOf(playId);
        }
        return playId + "_" + subPlayId + "_" + dataTypeValue;
    }
}
