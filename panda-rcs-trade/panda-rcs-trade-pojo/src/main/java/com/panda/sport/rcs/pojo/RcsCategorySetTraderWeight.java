package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
    * 操盘手玩法集权重表
    */
@Data
public class RcsCategorySetTraderWeight {
    /**
    * 表ID，自增
    */
    private Long id;

    /**
    * 运动种类id。 对应表 sport.id
    */
    private Long sportId;

    /**
    * 赛事id
    */
    private Long matchId;

    /**
    * 0:滚球 1:早盘
    */
    private Integer marketType;

    /**
    * 主键
    */
    private Long traderId;

    /**
    * 账号(注意为员工的英文名)
    */
    private String traderCode;

    /**
    * 权重
    */
    private Integer weight;

    /**
    * 玩法集id
    */
    //private Long setId;
    private Long setNo;
    /*
    verison 0 玩法集id 1玩法id
     */
    private Long typeId;

    /**
    * 玩法集名称
    */
    //private String setName;

    /**
     * 玩法集名国际化
     */
    @TableField(exist = false)
    private Object setNames;

    private Date createTime;

    private Date updateTime;
    private Integer version;
}