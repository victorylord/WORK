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
 * 所有玩法计算表
 * </p>
 *
 * @author admin
 * @since 2019-10-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RcsRectanglePlay extends RcsBaseEntity<RcsRectanglePlay> {


    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * 商户ID
     */
    private Integer businessId;
    /**
     * 项目类型
     */
    private Integer sportId;
    /**
     * 所属日期
     */
    private String dateExpect;

    /**
     * 1：商户维度  2：赛事维度
     */
    private Integer type;

    /**
     * 1：商户维度  2：赛事维度
     */
    private String typeValue;
    /**
     * 赛事id
     */
    private Integer matchId;
    /**
     * 玩法id
     */
    private Integer playId;
    /**
     * 盘口
     */
    private String handicap;
    /**
     * 选项id
     */
    private Integer optionId;

    /**
     * 矩阵类型
     */
    private Integer recType;
    /**
    * 下注金额
    * */
    private BigDecimal orderMoney;
    /**
     * 赔付值
     */
    private BigDecimal paidMoney;
    /**
     * 创建时间
     */
    private LocalDateTime crtTime;
    /**
     * 修改时间
     */
    private LocalDateTime updateTime;


    @Override
    public Serializable pkVal() {
        return this.id;
    }

}
