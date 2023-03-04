package com.panda.sport.rcs.vo;

import com.panda.sport.rcs.pojo.RcsMarketCategorySet;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

/**
 * @Project Name: panda-rcs-trade-group
 * @Package Name: com.panda.sport.rcs.vo
 * @Description : 玩法集返回结果
 * @Author : Paca
 * @Date : 2020-07-22 10:55
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
@Accessors(chain = true)
public class MarketCategorySetResVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 玩法集ID
     */
    private Long categorySetId;

    /**
     * 运动种类ID
     */
    private Long sportId;

    /**
     * 玩法集类型。0-展示型，1-风控型，默认展示型
     */
    private Integer type;

    /**
     * 玩法集名称
     */
    private String name;

    /**
     * 玩法集名称国际化
     */
    private Map<String, String> names;

    /**
     * 关联联赛等级。1-一级联赛，2-二级联赛，3-三级联赛……
     */
    private Integer tournamentLevel;

    /**
     * 排序值
     */
    private Integer orderNo;

    /**
     * 返回率。乘以10000
     */
    private Integer returnRate;

    /**
     * 玩法集数据源，0-自动，1-手动
     */
    @Deprecated
    private Integer tradeType;

    /**
     * 玩法集状态
     */
    private Integer status;

    /**
     * 备注。长度不超过130个字符
     */
    private String remark;

    /**
     * 创建时间。UTC时间，精确到毫秒
     */
    private Long createTime;

    /**
     * 更新时间。UTC时间，精确到毫秒
     */
    private Long modifyTime;

    public MarketCategorySetResVO() {
    }

    public MarketCategorySetResVO(RcsMarketCategorySet marketCategorySet) {
        this.categorySetId = marketCategorySet.getId();
        this.sportId = marketCategorySet.getSportId();
        this.type = marketCategorySet.getType();
        this.name = marketCategorySet.getName();
        this.tournamentLevel = marketCategorySet.getTournamentLevel();
        this.orderNo = marketCategorySet.getOrderNo();
        this.returnRate = marketCategorySet.getReturnRate();
        this.remark = marketCategorySet.getRemark();
        this.createTime = marketCategorySet.getCreateTime();
        this.modifyTime = marketCategorySet.getModifyTime();
    }

}
