package com.panda.sport.rcs.trade.vo.sell;


import java.io.Serializable;
import java.util.Map;

import lombok.Data;

@Data
public class SellMarketBean implements Serializable {

    /**
     * 盘口id
     */
    private Long id;

    /**
     * 标准比赛ID   standard_match_info.id
     */
    private Long standardMatchInfoId;

    /**
     * 所属联赛ID    standard_sport_tournament.id
     */
    private Long relationMarketId;

    /**
     * 标准玩法id   standard_sport_market_category.id
     */
    private Long marketCategoryId;

    /**
     * 盘口类型. 属于赛前盘或者滚球盘. 1: 赛前盘; 0: 滚球盘. 
     */
    private Integer marketType;
    /**
     * 盘口阶段id. 对应 对应 system_item_dict.value
     */
    private String scopeId;
    
    private String margain;
    
    private String marketValue;
    
    /**
     * 该字段用于做风控时，需要替换成风控服务商提供的盘口id。 如果数据源发生切换，当前字段需要更新。
     */
//    private String thirdMarketSourceId;

    /**
     * 附加字段1
     */
    private String addition1;

    /**
     * 附加字段2
     */
    private String addition2;

    /**
     * 附加字段3
     */
//    private String addition3;

    /**
     * 附加字段4
     */
//    private String addition4;

    /**
     * 附加字段5
     */
//    private String addition5;

    /**
     * 取值:  SR BC分别代表: SportRadar、FeedConstruc. 详情见data_source
     */
//    private String dataSourceCode;

    /**
     * 盘口状态0-5. 0:active, 1:suspended, 2:deactivated, 3:settled, 4:cancelled, 5:handedOver
     */
    private Integer status;

//    private Long modifyTime;
    
    private Map<String, Object> odds;
    
}

