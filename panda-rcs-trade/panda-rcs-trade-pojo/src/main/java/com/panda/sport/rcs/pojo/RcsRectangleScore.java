package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 只需要在赛事维度和用户赛事维度建立比分矩阵
 * </p>
 *
 * @author admin
 * @since 2019-10-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RcsRectangleScore extends RcsBaseEntity<RcsRectangleScore> {


    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 商户id
     */
    private Integer businessId;

    /**
     * 体育项目id
     */
    private Integer sportId;

    /**
     * 时间期号
     */
    private String dateExpect;

    /**
     * 矩阵类型，1：比分  2：红黄牌。。。
     */
    private Integer scoreType;

    /**
     * 1：商户维度  2：用户维度
     */
    private Integer type;

    /**
     * 商户id/用户id
     */
    private String typeValue;

    private Integer matchId;

    private Integer homeScore;

    private Integer awayScore;

    /**
     * 赔付值
     */
    private BigDecimal paidMoney;

    private LocalDateTime crtTime;

    private LocalDateTime updateTime;


    @Override
    public Serializable pkVal() {
        return this.id;
    }

}
