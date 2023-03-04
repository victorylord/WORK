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
public class SpecialSpreadCalculateVO implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 赛事id
     */
    @LogFormatAnnotion(name = "赛事ID" )
    private Long matchId;
    /**
     * 类型：1 ：早盘 ，0： 滚球盘， 3： 冠军盘
     */
    @TableField(exist = false)
    @LogFormatAnnotion(name = "赛事阶段" )
    private Integer matchType;

    private List<SpecialSpreadCalculatePlayVO> plays;
}
