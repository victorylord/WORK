package com.panda.sport.rcs.vo.secondary;

import com.panda.sport.rcs.enums.Basketball;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 篮球两项盘玩法集
 * @Author : Paca
 * @Date : 2021-02-21 17:18
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
@NoArgsConstructor
public class BasketballTwoPlaySet implements Serializable {

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
     * 查询标志
     */
    private Integer queryFlag;
    /**
     * 排序号
     */
    private Integer sortNo;

    private String enName;

    public BasketballTwoPlaySet(Basketball.TwoItemPlaySet twoItemPlaySet) {
        this.playSetId = twoItemPlaySet.getId();
        this.playSetName = twoItemPlaySet.getName();
        this.playIds = twoItemPlaySet.getPlayIds();
        this.enName = twoItemPlaySet.getEnName();
    }
}
