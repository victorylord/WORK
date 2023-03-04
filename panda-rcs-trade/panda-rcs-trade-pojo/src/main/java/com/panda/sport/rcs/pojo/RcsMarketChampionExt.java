package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;

import java.util.Date;

/**
    * 盘口表冠军附加表
    */
@Data
public class RcsMarketChampionExt extends RcsBaseEntity<RcsMarketChampionExt> {
    /**
     * 数据库id, 自增
     */
    private Long id;

    /**
     * 运动种类id.  对应表 sport.id
     */
    private Long sportId;

    /**
     * 标准比赛ID   standard_match_info.id
     */
    private Long standardMatchInfoId;

    /**
     * 标准玩法id   standard_sport_market_category.id
     */
    private Long marketCategoryId;

    /**
     * 盘口ID  standard_sport_market.id
     */
    private Long marketId;

    /**
     * 下次封盘时间
     */
    private String nextSealTime;

    private Date createTime;

    private Date updateTime;
    /**
     * 操盘类型，0-自动操盘，1-手动操盘，2-自动加强操盘
     *
     */
    @TableField(exist = false)
    private Integer dataSource;
}