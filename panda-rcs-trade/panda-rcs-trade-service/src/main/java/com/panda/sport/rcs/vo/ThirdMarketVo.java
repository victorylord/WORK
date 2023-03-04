package com.panda.sport.rcs.vo;

import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.mongo.ThirdSportMarketMessage;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author :  enzo
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.vo
 * @Description :  玩法集
 * @Date:
 */
@Data
public class ThirdMarketVo implements Serializable {
    /**
     * 玩法id集合
     */
    private Long categoryId;
    /**
     * 盘口球头值
     */
    private List<String> marketHeads;
    /**
     * 玩法id集合
     */
    private List<ThirdSportMarketMessage> thirdMarkets;


}
