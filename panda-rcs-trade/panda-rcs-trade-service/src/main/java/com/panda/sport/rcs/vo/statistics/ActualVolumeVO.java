package com.panda.sport.rcs.vo.statistics;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

/**
 * @author :  myname
 * @Project Name :  xindaima
 * @Package Name :  com.panda.sport.rcs.vo.statistics
 * @Description :  TODO
 * @Date: 2020-10-08 11:29
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class ActualVolumeVO {
    /**
     * 标准赛事id
     */
    private Long matchId;

    /**
     * 赛事类型:1赛前,2滚球
     */
    private Integer matchType;
    /**
     * 玩法id
     */
    private Integer playId;
    /**
     * 子玩法ID
     */
    private String subPlayId;
    /**
     * 盘口id
     */
    private String marketId;
    /**
     * 投注项
     */
    private String oddsItem;

    /**
     * 投注笔数
     */
    private Long betNum;
    /**
     * 货量
     */
    private BigDecimal betAmount;

    /**
     * 比分
     */
    private String betScore;
//
//    /**
//     * 总投注笔数
//     */
//    private Long marketBetNum;
    /**
     * 总货量
     */
    private BigDecimal marketBetAmount;
    /**
     * 完整让球(盘口值)
     */
    private String marketValueComplete;
    /**
     * 当前盘口位置
     */
    private Integer marketIndex;
    /**
     * 盈利
     */
    private BigDecimal profit;
    /**
     * 货货量-纯赔付额
     */
    private BigDecimal betAmountPay;
    /**
     * 盘口 货量-纯赔付额
     */
    private BigDecimal marketBetAmountPay;
    /**
     * 货量-混合型
     */
    private BigDecimal betAmountComplex;
    /**
     * 盘口 货量-混合型
     */
    private BigDecimal marketBetAmountComplex;

    @TableField(exist = false)
    private BigDecimal sort;

    public BigDecimal getSort() {
        if(StringUtils.isBlank(marketValueComplete)){
            return BigDecimal.ZERO;
        }else {
            return new BigDecimal(marketValueComplete).abs();
        }
    }

    public void setSort(String marketValueComplete) {
        this.sort = new BigDecimal(marketValueComplete).abs();
    }
}
