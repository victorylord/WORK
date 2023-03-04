package com.panda.sport.rcs.mongo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;

/**
 * @author :  enzo
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.mongo
 * @Description :  盘口赔率
 */
@Data
public class MatchMarketOddsVo {

    /**
     * 投注项ID
     */
    @Field(value = "id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 投注项名称，多语言
     */
    private I18nBean names;

    /**
     * 投注项名称编码
     */
    private Long nameCode;

    /**
     * 投注项名称中包含的表达式的值
     */
    private String nameExpressionValue;

    /**
     * 投注给哪一方，T1-主队，T2-客队
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
     * 水差值
     */
    private Double marketDiffValue;

    /**
     * 降级后的欧赔数据
     */
    private String nextLevelOddsValue;

    /**
     * 当前投注项是否被激活，1-激活，0-未激活（锁盘）
     */
    private Integer active;

    /**
     * 排序
     */
    private Integer orderOdds;

    /**
     * magin值
     */
    private BigDecimal margin;

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
    private String addition3;

    /**
     * 附加字段4
     */
    private String addition4;

    /**
     * 附加字段5
     */
    private String addition5;

    /**
     * 模板中排序号
     */
    private Integer sortNo = 99;

    /**
     * 模板中分组号
     */
    private Integer groupId = 1;

    /**
     * 模板中标题
     */
    private String titleName;

    /**
     * 列坐标
     */
    private Integer rowIndex = 999;

    /**
     * 行坐标
     */
    private Integer colIndex;
    
    private String name;

    /**
     * 概率差
     */
    private Double probability;

    /**
     * 概率赔率
     */
    private Integer probabilityOdds;

    /**
     * margin概率赔率
     */
    private Integer marginProbabilityOdds;

    /**
     * 描点 ：0(否),1(是)
     */
    private Integer anchor;

    /**
     * 开启/关闭投注项手动模式，0-关闭，1-开启
     */
    private Integer status = 0;

    public String getFieldOddsValue() {
        if (StringUtils.isBlank(fieldOddsValue) || "null".equalsIgnoreCase(fieldOddsValue)) {
            return "0";
        }
        return fieldOddsValue;
    }

    public Integer getSortNo() {
        if (sortNo == null) {
            return 99;
        }
        return sortNo;
    }

    public Integer getOrderOdds() {
        if (orderOdds == null) {
            return getSortNo();
        }
        return orderOdds;
    }

    public Integer getGroupId() {
        if (groupId == null) {
            return 1;
        }
        return groupId;
    }

    public Integer getRowIndex() {
        if (rowIndex == null) {
            return 999;
        }
        return rowIndex;
    }
}
