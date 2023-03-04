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
 * 
 * </p>
 *
 * @author admin
 * @since 2019-10-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RcsRectanglePlayMoney extends RcsBaseEntity<RcsRectanglePlayMoney> {


	@TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer businessId;

    private Integer sportId;

    private String dateExpect;

    private Integer type;

    private String typeValue;

    private Integer matchId;

    private Integer playId;

    private String handicap;

    /**
     * 总下注金额
     */
    private BigDecimal money;

    /**
     * 总返奖金额
     */
    private BigDecimal prizeMoney;

    private LocalDateTime crtTime;

    private LocalDateTime updateTime;


    @Override
    public Serializable pkVal() {
        return this.id;
    }

}
