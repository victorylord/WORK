package com.panda.sport.rcs.mongo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ThirdSportMarketOdds implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     *表ID,自增
     **/
    private String id;

    private String oddsId;
    /**
     *盘口IDthird_sport_market.id
     **/
    private String marketId;
    /**
     *当前投注项是否被激活.1激活;0未激活(锁盘)
     **/
    private Integer active;
    /**
     *投注项类型
     **/
    private String oddsType;
    /**
     *附加字段1
     **/
    private String addition1;
    /**
     *附加字段2
     **/
    private String addition2;
    /**
     *附加字段3
     **/
    private String addition3;
    /**
     *附加字段4
     **/
    private String addition4;
    /**
     *附加字段5
     **/
    private String addition5;
    /**
     *第三方投注项原始ID.
     **/
    private String thirdOddsFieldSourceId;
    /**
     *用于排序,大于1,越小越靠前
     **/
    private Integer orderOdds;
    /**
     *投注项名称
     **/
    private String name;
    /**
     *投注项名称中包含的表达式的值
     **/
    private String nameExpressionValue;
    /**
     *投注项赔率.单位:0.0001
     **/
    private Integer oddsValue;
    /**
     *投注项PA赔率.单位:0.0001
     **/
    private Integer paOddsValue;
    /**
     *投注项原始赔率.单位:0.0001
     **/
    private Integer originalOddsValue;

    private String  fieldOddsValue;
    /**
     *取值:SRBC分别代表:SportRadar、FeedConstruc.详情见data_source
     **/
    private String dataSourceCode;


    //警示值
    private Boolean isWarn;


}