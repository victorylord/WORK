package com.panda.sport.rcs.pojo.mq;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.panda.sport.rcs.log.annotion.format.LogFormatAnnotion;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargain;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 盘口设置表
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-10-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@LogFormatAnnotion
public class SpecialSpreadCalculatePlayVO implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 玩法id
     */
    @LogFormatAnnotion(name = "玩法 ID" )
    private Long playId;
    /**
     * 是否特殊抽水1:是 0:否
     */
    @TableField(exist = false)
    private Integer isSpecialPumping;
    /**
     * 特殊抽水赔率区间
     */
    @TableField(exist = false)
    private String specialOddsInterval;
}
