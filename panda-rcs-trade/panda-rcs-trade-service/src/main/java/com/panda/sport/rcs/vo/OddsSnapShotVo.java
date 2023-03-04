package com.panda.sport.rcs.vo;

import com.panda.sport.rcs.mongo.I18nBean;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author :  myname
 * @Project Name :  xindaima
 * @Package Name :  com.panda.sport.rcs.trade.vo
 * @Description :  TODO
 * @Date: 2020-07-08 16:13
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class OddsSnapShotVo {
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
    private I18nBean playNameCodeList;
    /**
     * 盘口id
     **/
    private Long marketId;
    /**
     * 投注项Id
     **/
    private Long oddsId;
    /**
     * 投注项编码
     **/
    private Long oddsNameCode;
    /**
     * 投注项多语言
     **/
    private Map<String, String> oddsNameCodeList;
    /**
     * 投注项最大赔率
     **/
    private BigDecimal oddsValueMax;
    /**
     * 投注项当前赔率  乘了十万
     **/
    private Integer oddsValue;
    /**
     * 投注数量
     **/
    private BigDecimal betOrderNum = BigDecimal.ZERO;
    /**
     * 投注金额
     **/
    private BigDecimal betAmount = BigDecimal.ZERO;
    /**
     * 投注类型  1未早盘  2为滚球  null表示未没有投注
     **/
    private String matchType;
    /**
     * 盘口具体的值
     **/
    private String marketValue;
    /**
     * 出涨额
     **/
    private BigDecimal amountIncrease;
    /**
     * 比分   全场这类的类型
     **/
    private String oddsType;

    /**
     * 单关/串关 1单关 2串关
     */
    private Integer seriesType;

}
