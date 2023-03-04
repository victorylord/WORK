package com.panda.sport.rcs.pojo.statistics;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jboss.logging.Field;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.pojo.statistics
 * @Description :  赛事维度统计信息
 * @Date: 2019-11-05 14:13
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RcsMatchDimensionStatistics extends RcsBaseEntity<RcsMatchDimensionStatistics> implements Serializable {
    /**
     * 赛事Id
     */
    @TableId(type = IdType.INPUT)
    private Long matchId;
    /**
     * 总货量
     */
    private BigDecimal totalValue;
    /**
     * 总注数
     */
    private Long totalOrderNums;
    /**
     * 已结算货量
     */
    private BigDecimal settledRealTimeValue;
    /**
     * 已结算盈亏
     */
    private BigDecimal settledProfitValue;

    /**
     * 近一小时实货量
     */
    /*@TableField(exist = false)
    private Long nearlyOneHoureRealTimeValue;*/

    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 修改时间
     */
    private Long modifyTime;

    /**
     * 转化成字符串
     * @return
     */
    @Override
    public String toString(){
        return "matchId=" + matchId +";totalValue="+totalValue+";settledRealTimeValue="
                +settledRealTimeValue +";settledProfitValue=" +settledProfitValue +
                ";createTime="+createTime.toString()+";modifyTime="+modifyTime;
    }
}
