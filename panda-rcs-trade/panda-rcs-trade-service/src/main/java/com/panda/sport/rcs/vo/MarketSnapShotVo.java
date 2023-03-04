package com.panda.sport.rcs.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author :  myname
 * @Project Name :  panda-rcs-trade-group
 * @Package Name :  com.panda.sport.rcs.vo
 * @Description :  TODO
 * @Date: 2020-07-09 14:09
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class MarketSnapShotVo {
    /**
     * 玩法
     **/
    private Long playId;
    /**
     * 玩法编码
     **/
    private Long playNameCode;
    /**
     * 玩法编码多语言
     **/
    private Map<String, String> playNameCodeList;
    /**
     * 盘口id
     **/
    private Long marketId;
    /**
     * 盘口具体的值
     **/
    private String marketValue;
    /**
     * 投注项数据
     **/
    private List<OddsSnapShotVo> oddsSnapShotVoList = new ArrayList<>();

}
