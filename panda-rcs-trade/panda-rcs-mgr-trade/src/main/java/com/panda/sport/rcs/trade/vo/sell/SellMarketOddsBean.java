package com.panda.sport.rcs.trade.vo.sell;

import java.io.Serializable;

import lombok.Data;

/**
 * @Description  :  查询赛事赔率
 * @author       :  Vito
 * @Date:  2020年5月15日 下午1:00:23
 * @ModificationHistory   Who    When    What
 * --------  ---------  --------------------------
*/
@Data
public class SellMarketOddsBean implements Serializable {

    private Long id;

    private Long relationMarketOddsId;

    /**
     * 盘口ID  standard_sport_market.id
     */
    private Long marketId;

    private Long relationMarketId;

    /**
     * 标准投注项模板id 对应standard_sport_odds_fields_templet.id
     */
    private Long oddsFieldsTemplateId;

    /**
     * 三方投注项模板源ID
     */
//    private String thirdTemplateSourceId;

    /**
     * 当前投注项是否被激活.1激活; 0未激活(锁盘)
     */
    private Integer active;
    
    private String marketValue;

    /**
     * 投注项结算结果文本
     */
//    private String settlementResultText;

    /**
     * 投注项结算结果文本
     */
//    private String settlementResult;

    /**
     * 赛果已确认: Confirmed, 盘中事件确认: LiveScouted, 未知: Unknown
     */
//    private String betSettlementCertainty;

    /**
     * 投注项类型
     */
    private String oddsType;

    /**
     * 附加字段1
     */
    private String addition1;

    /**
     * 附加字段2
     */
//    private String addition2;

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
     * 投注项赔率. 单位: 0.0001
     */
//    private Integer oddsValue;

    /**
     * 投注项PA赔率. 单位: 0.0001
     */
    private Integer paOddsValue;

    /**
     * 投注项原始赔率. 单位: 0.0001
     */
//    private Integer originalOddsValue;

    /**
     * 用于排序, 大于1, 越小越靠前
     */
//    private Integer orderOdds;

    /**
     * 取值:  SR BC分别代表: SportRadar、FeedConstruc. 详情见data_source
     */
//    private String dataSourceCode;

    /**
     * 该字段用于做风控时, 需要替换成风控服务商提供的投注项id.  如果数据源发生切换, 当前字段需要更新. 
     */
//    private String thirdOddsFieldSourceId;

//    private String remark;

//    private Long modifyTime;
}
