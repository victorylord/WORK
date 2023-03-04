package com.panda.sport.rcs.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * 足球赛事盘口表. 使用盘口关联的功能存在以下假设：同一个盘口的显示值不可变更，如果变更需要删除2个盘口之间的关联关系。。
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-09-03
 */
@Data
public class SportMarketOddsQueryVo {

    /**
     * ***********盘口字段***************
     */

    /**
     * 运动种类id。 对应表 sport.id
     */
    private Long sportId;

    /**
     * 所属联赛ID    standard_sport_tournament.id
     */
    private Long standardTournamentId;

    /**
     * 标准比赛ID   standard_match_info.id
     */
    private Long standardMatchInfoId;

    /**
     * 标准玩法id   standard_sport_market_category.id
     */
    private Long marketCategoryId;

    /**
     * 盘口类型。属于赛前盘或者滚球盘。1：赛前盘；0：滚球盘。
     */
    private Integer marketType;

    /**
     * 盘口名称编码. 用于多语言
     */
    private Long nameCode;


    /**
     * 盘口名称,V1.2统一命名规则。
     */
    private String oddsName;
    
    /**
     * 赔率类型 1 ，X， 2
     */
    private String oddsType;

    /**
     * 取值： SR BC分别代表：SportRadar、FeedConstruc。详情见data_source
     */
    private String dataSourceCode;

    /**
     * 1 需要； 0 不需要
     */
    private Integer managerConfirmPrize;

    /**
     * 该字段用于做风控时，需要替换成风控服务商提供的盘口id。 如果数据源发生切换，当前字段需要更新。
     */
    private String thirdMarketSourceId;

    private String addition1;
    private String addition2;
    private String addition3;
    private String addition4;
    private String addition5;
    /**
     * 盘口状态0-5. 0:active, 1:suspended, 2:deactivated, 3:settled, 4:cancelled, 5:handedOver
     */
    private Integer status;

    /**
     * 盘口阶段id. 对应 sport_market_scope.id
     */
    private Long scopeId;
    /**
     * 盘口名称多语言数组json串
     */
    private String i18nNames;

    private String remark;

    private Long createTime;

    private Long modifyTime;

    /**
     * 玩法配置的序号
     */
    private Integer marketTempletOrderNo;
    /**
     * 玩法配置名称编码
     */
    private Long marketTempletNameCode;




    /**
     * ***********盘口交易项字段***************
     */

    private Long marketOddsId;

    private Long oddsFieldsTempletId;

    /**
     * 盘口ID  standard_sport_market.id
     */
    private Long marketId;


    /**
     * 名称编码. 用于多语言。交易项可能有也可能没有该字段。需要的时候填入
     */
    private Long marketOddsNameCode;

    /**
     * 投注项名称。
     */
    private String marketOddsName;

    /**
     * 该交易项在客户端显示的值，空则不显示
     */
    private String nameExpressionValue;

    /**
     * 投注给哪一方：T1主队，T2客队
     */
    private String targetSide;

    /**
     * 交易项赔率。单位：0.0001
     */
    private Integer oddsValue;


    /**
     * 用于排序，大于1，越小越靠前
     */
    private Integer orderOdds;


    /**
     * 取值： SR BC分别代表：SportRadar、FeedConstruc。详情见data_source
     */
    private String marketOddsDataSourceCode;

    /**
     * 该字段用于做风控时，需要替换成风控服务商提供的交易项id。 如果数据源发生切换，当前字段需要更新。
     */
    private String thirdOddsFieldSourceId;

    /**
     * 是否需要人工确认开奖。1 需要； 0 不需要
     */
    private Integer marketOddsManagerConfirmPrize;

    private String marketOddsAddition1;
    private String marketOddsAddition2;
    private String marketOddsAddition3;
    private String marketOddsAddition4;
    private String marketOddsAddition5;
    /**
     * 盘口名称多语言数组json串
     */
    private String marketOddsI18nNames;

    private Long marketOddsCreateTime;

    private Long marketOddsModifyTime;

    /**
     * 投注项模板配置的序号
     */
    private Integer oddsTempletOrderNo;
    /**
     * 投注项模板名称编码
     */
    private Long oddsTempletNameCode;
    /**
     * 当前投注项是否被激活.1激活; 0未激活(锁盘)
     */
    private Integer active;

    /**
     * 实货量
     */
    private BigDecimal betAmount;

    /**
     * 盈利期望值
     */
    private BigDecimal profitValue;

    /**
     * 注单数
     */
    private BigDecimal betOrderNum;


}
