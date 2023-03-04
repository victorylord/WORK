package com.panda.sport.rcs.pojo;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import com.panda.sport.rcs.utils.LongToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * <p>
 * 玩法维度配置
 * </p>
 *
 * @author admin
 * @since 2019-10-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class  RcsBusinessPlayPaidConfig extends RcsBaseEntity<RcsBusinessPlayPaidConfig> {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 商户id
     * 商户列表
     */
    @JSONField(serializeUsing = LongToStringSerializer.class)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long businessId;

    /**
     * 项目类别
     * 项目类别表
     */
    private Long sportId;

    /**
     * 投注阶段  0未开赛 和 1滚球
     */
    private Integer matchType;

    /**
     * 玩法类型 全场3，上半场 1，下半场2，0-15分钟4
     */
    private Long playType;

    /**
     * 玩法id
     * 玩法列表
     */
    private Long playId;

    /**
     * 玩法id对应的玩法名字
     */
    @TableField(exist = false)
    private String name;
    /**
     * 玩法id对应的英文玩法名字
     */
    @TableField(exist = false)
    private String enName;
    /**
     * 最高单注比例  %
     */
    private Integer orderMaxRate;

    /**
     * 最高用户玩法比例  %
     */
    private Integer playMaxRate;

    /**
     * 最高单注赔付
     */
    private Long orderMaxPay;

    /**
     * 用户最高玩法赔付
     */
    private Long playMaxPay;

    /**
     * 创建时间
     */
    @TableField(exist = false)
    private Timestamp crtTime;
    /**
     * 修改时间
     */
    @TableField(exist = false)
    private Timestamp updateTime;
    /**
     * 状态
     */
    private Integer status;

    @Override
    public Serializable pkVal() {
        return this.id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public Long getSportId() {
        return sportId;
    }

    public void setSportId(Long sportId) {
        this.sportId = sportId;
    }

    public Integer getMatchType() {
        return matchType;
    }

    public void setMatchType(Integer matchType) {
        this.matchType = matchType;
    }

    public Long getPlayType() {
        return playType;
    }

    public void setPlayType(Long playType) {
        this.playType = playType;
    }

    public Long getPlayId() {
        return playId;
    }

    public void setPlayId(Long playId) {
        this.playId = playId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getOrderMaxRate() {
        return orderMaxRate;
    }

    public void setOrderMaxRate(Integer orderMaxRate) {
        this.orderMaxRate = orderMaxRate;
    }

    public Integer getPlayMaxRate() {
        return playMaxRate;
    }

    public void setPlayMaxRate(Integer playMaxRate) {
        this.playMaxRate = playMaxRate;
    }

    public Long getOrderMaxPay() {
        return orderMaxPay;
    }

    public void setOrderMaxPay(Long orderMaxPay) {
        this.orderMaxPay = orderMaxPay;
    }

    public Long getPlayMaxPay() {
        return playMaxPay;
    }

    public void setPlayMaxPay(Long playMaxPay) {
        this.playMaxPay = playMaxPay;
    }

    public Timestamp getCrtTime() {
        return crtTime;
    }

    public void setCrtTime(Timestamp crtTime) {
        this.crtTime = crtTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
