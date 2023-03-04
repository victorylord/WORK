package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

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
public class RcsMatchMarketMarginConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 赛事id
     */
    @TableField(exist = false)
    private Long sportId;
    /**
     * 赛事id
     */
    private Long matchId;

    /**
     * 联赛id
     */
    private Long tournamentId;

    /**
     * 玩法id
     */
    private Long playId;

    /**
     * 盘口id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long marketId;


    /**
     * 创建时间
     */
    @TableField(exist = false)
    private Timestamp createTime;

    /**
     * 创建人
     */
    private String createUser;

    /**
     * 修改时间
     */
    @TableField(exist = false)
    private Timestamp modifyTime;

    /**
     * 修改人
     */
    private String modifyUser;
    /**
     * @Description   客队水差
     * @Param
     * @Author  Sean
     * @Date  15:36 2020/1/11
     * @return
     **/
    private  String awayAutoChangeRate;
    /**
     * @Description   玩法水差
     * @Param
     * @Author  Sean
     * @Date  15:36 2020/1/11
     * @return
     **/
    @TableField(exist = false)
    private  String playAwayAutoChangeRate;
    /**
     * 主胜margin
     */
    private BigDecimal homeMargin;
    /**
     * 客胜margin
     */
    private BigDecimal awayMargin;
    /**
     * 平局margin
     */
    private BigDecimal tieMargin;
    /**
     * margin
     */
    @TableField(exist = false)
    private BigDecimal margin;
    /**
     * 暂停margin
     */
    @TableField(exist = false)
    private BigDecimal timeOutMargin;
}
