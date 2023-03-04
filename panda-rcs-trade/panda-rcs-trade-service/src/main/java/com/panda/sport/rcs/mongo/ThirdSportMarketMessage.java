package com.panda.sport.rcs.mongo;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;


@Data
public class ThirdSportMarketMessage implements Serializable {

    /**
     * id
     */
    @Field(value = "id")
    private String id;

    /**
     * 标准盘口id
     */
    private String relationMarketId;

    /**
     * 三方玩法id
     */
    private String marketCategoryId;

    /**
     * 第三提供的id
     */
    private String thirdMarketSourceId;

    /**
     * 1:赛前盘;0:滚球盘
     */
    private Integer marketType;

    /**
     * 详情见data_source
     */
    private String dataSourceCode;

    /**
     * 盘口状态0-5.0:active,1:suspended,2:deactivated,3:settled,4:cancelled,5:handedOver
     */
    private Integer status;

    /**
     * 三方盘口源状态
     */
    private Integer thirdMarketSourceStatus;

    /**
     * 玩法的中文名称.仅用用于数据库操作人员使用
     */
    private String oddsTypeName;

    /**
     * 该盘口具体显示的值.例如:大小球中,大小界限是:3.5
     */
    private String oddsValue;

    /**
     * 排序类型
     */
    private String orderType;

    /**
     * 盘口名称
     */
    private String oddsName;

    /**
     * TX坑位
     */
    private Integer offerLineId;

    /**
     * 盘口级别，数字越小优先级越高
     */
    private Long oddsMetric;

    private String addition1;

    private String addition2;

    private String addition3;

    private String addition4;

    private String addition5;

    private Long createTime;

    private Long modifyTime;

    private static final long serialVersionUID = 1L;

    /**
     * 三方盘口投注项集合
     */
    private List<ThirdSportMarketOdds> thirdSportMarketOddsList;

}
