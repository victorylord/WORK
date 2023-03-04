package com.panda.sport.rcs.vo.secondary;

import com.panda.sport.rcs.enums.Basketball;
import com.panda.sport.rcs.mongo.MarketCategory;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 篮球两项盘玩法集信息
 * @Author : Paca
 * @Date : 2021-02-19 11:57
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
@NoArgsConstructor
public class BasketballTwoPlaySetInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 玩法集ID
     */
    private Long playSetId;
    /**
     * 玩法集名称
     */
    private String playSetName;
    /**
     * 玩法ID集合
     */
    private List<Long> playIds;
    /**
     * 玩法信息集合
     */
    private List<MarketCategory> playInfoList;
    /**
     * 排序号
     */
    private Integer sortNo;

    public BasketballTwoPlaySetInfo(Basketball.TwoItemPlaySet twoItemPlaySet) {
        this.playSetId = twoItemPlaySet.getId();
        this.playSetName = twoItemPlaySet.getName();
        this.playIds = twoItemPlaySet.getPlayIds();
        this.sortNo = twoItemPlaySet.getSortNo();
    }
}
