package com.panda.sport.rcs.vo;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class MarketChartReqVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 赛事Id
     */
    private Long matchId;

    /**
     * 类型：玩法ID
     */
    private Integer playId;

    /**
     * 类型：1 ：早盘 ，2： 滚球盘， 3： 冠军盘
     */
    private Integer matchType;

    /**
     * 盘口id
     */
    private Long marketId;


    /**
     * 标签
     */
    private List<Integer> userLevel;


    public static void main(String[] args) {
        MarketChartReqVo vo = new MarketChartReqVo();
        List<Integer> userLevel = new ArrayList<>();
        userLevel.add(1);
        userLevel.add(2);
        vo.setUserLevel(userLevel);
        vo.setMarketId(2L);
        System.out.println(JSONObject.toJSONString(vo));
    }
}
