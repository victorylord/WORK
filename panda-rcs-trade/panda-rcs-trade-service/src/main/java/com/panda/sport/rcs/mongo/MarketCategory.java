package com.panda.sport.rcs.mongo;

import com.panda.sport.rcs.enums.TradeStatusEnum;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author :  enzo
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.mongo
 * @Description :玩法赔率表
 */
@Data
@Document(collection = "rcs_market_category")
public class MarketCategory {

    /**
     * 玩法ID
     */
    @Field(value = "id")
    private Long id;

    /**
     * 体育种类ID
     */
    private Long sportId;

    /**
     * 赛事ID
     */
    private String matchId;

    /**
     * 比赛开始时间
     */
    private String matchStartTime;

    /**
     * 玩法级别数据源，0-自动，1-手动
     */
    private Integer tradeType;

    /**
     * 玩法状态
     */
    private Integer status;
    /**
     * 占位符玩法总状态，默认开
     */
    private Integer mainPlayStatus;
    /**
     * 操盤顯示列排序
     */
    private Integer rollType;

    private String type;

    /**
     * 玩法名称，多语言
     */
    private I18nBean names;

    /**
     * 显示盘口数量
     */
    private Integer marketCount;

    /**
     * 盘口类型
     */
    private String marketType;
    /**
     * 赔率（水差）变动幅度
     */
    private BigDecimal oddsAdjustRange;
    /**
     * 盘口调整幅度
     */
    private BigDecimal marketAdjustRange;

    /**
     * margain值
     */
    private String margain;
    /**
     * 水差关联标志，0-不关联，1-关联
     */
    private Integer relevanceType = 1;
    /**
     * 水差关联标志，子玩法
     */
    private Map<Long, Integer> relevanceTypeMap;

    /**
     * 赔率报警 true:报警   false:解除
     */
    private boolean warningSign;

    private boolean isMain;
    /**
     * 玩法模板ID
     */
    private Integer templateId;

    /**
     * 玩法集排序
     */
    private Integer displaySort = 999;

    /**
     * 玩法排序
     */
    private Integer orderNo = 999;

    /**
     * 创建时间
     */
    private String crtTime;

    /**
     * 是否需要特殊处理，0-否，1-是
     */
    private Integer isIrregular = 0;

    /**
     * 0-默认显示，1-盘口遍历显示盘口名称
     */
    private Integer showFlag = 0;
    /**
     * 是否支持新增盘口，0-否，1-是
     */
    private Integer isNewMarket;

    /**
     * 盘口集合
     */
    private List<MatchMarketVo> matchMarketVoList;
    /**
     * 三方盤口集合
     */
    private List<ThirdSportMarketMessage> thirdMarketList;
    /**
     * 是否是次玩法
     */
    private Boolean isChildCategory;

    /**
     * 列标题
     */
    private List<TemplateTitle> rowTitles;

    /**
     * 玩法期望值
     */
    private List<PredictForecastVo> forecast;

    private String oddsName;

    public Integer getMarketCount() {
        if (marketCount == null) {
            return 1;
        }
        return marketCount;
    }

    /**
     * 行数
     */
    private Integer rowNo;
    /**
     * 列
     */
    private Integer colNo;
    /**
     * 玩法集
     */
    private Long categorySetId;

    public Integer getMainPlayStatus() {
        if (mainPlayStatus == null) {
            return TradeStatusEnum.OPEN.getStatus();
        }
        return mainPlayStatus;
    }

    /**
     * 盘口来源 0：数据商 1：融合构建
     */
    private Integer marketSource;
    /**
     * 15分钟玩法比分
     */
    private Map<String,String> scoreMap;

    private Integer categoryPreStatus;

    /**
     * 数据商是否支持提前结算
     * ＆关系，现存盘口只要一个盘口满足
     */
    private Integer cashOutStatus;
}
