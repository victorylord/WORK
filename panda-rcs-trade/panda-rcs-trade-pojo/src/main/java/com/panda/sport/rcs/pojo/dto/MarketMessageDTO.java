package com.panda.sport.rcs.pojo.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class MarketMessageDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 标准比赛ID   standard_match_info.id
     */
    private Long standardMatchInfoId;


    private List<MatchMarketVo> matchMarketVos;

    @Data
    public static class MatchMarketBaseVo implements Serializable {
        private Map<String, String> names;
        private Long nameCode;

        @Field(value = "id")
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
    }

    @Data
    public static class MatchMarketVo extends MatchMarketBaseVo {
        /**
         * 盘口赔率
         */
        private List<MatchMarketOddsFieldVo> oddsFieldsList;

        /**
         * 盘口id
         */
        private Long marketCategoryId;
        private Long marketId;
        /**
         * 平衡值
         */
        private Long balance;

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

        private BigDecimal waterValue;

        private boolean marketActive;

        private Integer diffOdds;

        private BigDecimal marginValue;
        /**
         * 盘口是否关联 1关联
         */
        private Integer relevanceType;

        private String addition1;

        private String addition2;

    }

    @Data
    public static class MatchMarketOddsFieldVo extends MatchMarketBaseVo {

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
        private BigDecimal betNum;

        /**
         * 降级后的欧赔数据
         */
        private String nextLevelOddsValue;

        /**
         * 当前投注项是否被激活.1激活; 0未激活(锁盘)
         */
        private Integer active;

        private Integer sortNo = 0;

        private Integer groupId = 0;
    }
}
