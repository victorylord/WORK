package com.panda.sport.rcs.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;

@Data
public class CategorySellVo {

    /**
     * id
     */
    private Long id;
    /**
     * 玩法类型
     */
    private String type;

    /**
     * 盘口
     */
    private List<MatchMarketSellVo> matchMarketVoList;

    /**
     * 玩法阶段 字典ballPhase
     */
    private Integer playPhaseType;

    /**
     * 玩法阶段对应玩法种类
     */
    private Integer rollType;

    @Data
    public static class MatchMarketSellVo extends MatchMarketSellBaseVo {

        /**
         * 盘口id
         */
        private Long marketCategoryId;
        private Long marketId;


        /**
         * 盘口类型. 属于赛前盘或者滚球盘. 1: 赛前盘; 0: 滚球盘.
         */
        private Integer marketType;

        /**
         * 盘口状态0-5. 0:active, 1:suspended, 2:deactivated, 3:settled, 4:cancelled, 5:handedOver
         */
        private Integer status;

        /**
         * 自动 1 手动0
         */
        private Integer marketTradeType;
        /**
         * 自动水差状态 1调整过 0 未调整
         */
        private Integer changeRateStatus;


        private Integer diffOdds;

        /**
         * 盘口级别，数字越小优先级越高
         */
        private Integer oddsMetric;

        /**
         * 盘口赔率
         */
        private List<MatchMarketOddsFieldSellVo> oddsFieldsList;


    }

    @Data
    public static class MatchMarketOddsFieldSellVo extends MatchMarketSellBaseVo {


        /**
         * 投注项名称中包含的表达式的值
         */
        private String nameExpressionValue;

        /**
         * 投注给哪一方：T1主队，T2客队
         */
        private String targetSide;

        /**
         * 赔率显示值
         */
        private String fieldOddsValue;

        /**
         * 赔率原始值
         */
        private transient Integer fieldOddsOriginValue;

        /**
         * 投注项类型
         */
        private String oddsType;

        /**
         * 投注项类型
         */
        private Long marketId;


    }

    @Data
    public static class MatchMarketSellBaseVo implements Serializable {
        @Field(value = "id")
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
    }
}
